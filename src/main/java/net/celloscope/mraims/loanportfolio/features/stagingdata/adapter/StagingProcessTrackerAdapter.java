package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingProcessTrackerEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.SamityMemberCount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataGenerationStatusDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingProcessTrackerDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class StagingProcessTrackerAdapter implements IStagingProcessTrackerPersistencePort {

    private final IStagingProcessTrackerRepository repository;
    private final IStagingProcessTrackerEditHistoryRepository editHistoryRepository;
    private final ModelMapper mapper;
    private final Gson gson;

    public StagingProcessTrackerAdapter(IStagingProcessTrackerRepository repository, IStagingProcessTrackerEditHistoryRepository editHistoryRepository, ModelMapper mapper) {
        this.repository = repository;
        this.editHistoryRepository = editHistoryRepository;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Flux<StagingDataGenerationStatusDTO> getStagingDataGenerationStatusFlux(String officeId) {
        return repository.getStagingDataGenerationStatusByOfficeId(officeId)
                .map(entity -> mapper.map(entity, StagingDataGenerationStatusDTO.class))
                .flatMap(dto -> repository.getTotalMemberOfSamityToGenerateStagingData(dto.getSamityId())
                        .map(count -> {
                            dto.setTotalMember(count);
                            return dto;
                        })
                );
    }

    @Override
    public Mono<Integer> getTotalMemberForOneSamity(String samityId) {
        return repository.getTotalMemberForOneSamity(samityId);
    }

    @Override
    public Flux<StagingDataGenerationStatusDTO> saveProcessTrackerWithWaitingStatus(List<StagingDataGenerationStatusDTO> statusList) {
        return Flux.fromIterable(statusList)
                .mapNotNull(status -> StagingProcessTrackerEntity.builder()
                        .managementProcessId(status.getManagementProcessId())
                        .processId(status.getProcessId())
                        .officeId(status.getOfficeId())
                        .samityId(status.getSamityId())
                        .status(Status.STATUS_WAITING.getValue())
                        .build())
                .collectList()
                .flatMap(entityList -> repository.saveAll(entityList).collectList())
                .flatMapMany(stagingProcessTrackerEntities ->  Flux.fromIterable(statusList));
    }

    @Override
    public Mono<StagingDataGenerationStatusDTO> updateProcessTrackerStatusToProcessing(StagingDataGenerationStatusDTO statusDTO) {
        return repository.getStagingProcessTrackerEntityBySamityId(statusDTO.getSamityId())
                .map(entity -> {
                    entity.setProcessStartTime(LocalDateTime.now());
                    entity.setStatus(Status.STATUS_PROCESSING.getValue());
                    return entity;
                })
                .flatMap(repository::save)
                .map(e -> statusDTO);
    }

    @Override
    public Mono<StagingDataGenerationStatusDTO> updateProcessTrackerStatusToFinishedBySamity(StagingDataGenerationStatusDTO statusDTO) {
        return repository.getStagingProcessTrackerEntityBySamityId(statusDTO.getSamityId())
                .flatMap(entity -> repository.getStagingDataIdBySamity(entity.getSamityId(), statusDTO.getManagementProcessId())
                        .collectList()
                        .zipWith(Mono.just(entity))
                        .map(tuple -> {
                            String stagingDataIds = gson.toJson(tuple.getT1(), ArrayList.class);
                            tuple.getT2().setStagingDataIds(stagingDataIds);
                            entity.setProcessEndTime(LocalDateTime.now());
                            entity.setStatus(Status.STATUS_FINISHED.getValue());
                            return tuple.getT2();
                        })
                        .flatMap(updatedEntity -> repository.save(updatedEntity)
                                .map(e -> statusDTO)));
    }


    @Override
    public Flux<StagingDataGenerationStatusDTO> getStagingDataGenerationStatusResponse(String officeId) {
        return repository.getStagingDataGenerationStatusResponse(officeId)
                .map(entity -> mapper.map(entity, StagingDataGenerationStatusDTO.class));
    }

    @Override
    public Mono<StagingProcessTrackerDTO> getStagingProcessTrackerEntityBySamityId(String samityId) {
        return repository.getStagingProcessTrackerEntityBySamityId(samityId)
                .map(entity -> {
                    StagingProcessTrackerDTO dto = mapper.map(entity, StagingProcessTrackerDTO.class);
                    List<String> stagingDataIdList = gson.fromJson(String.valueOf(entity.getStagingDataIds()), ArrayList.class);
                    dto.setStagingDataIds(stagingDataIdList);
                    return dto;
                });
    }

    @Override
    public Mono<Integer> getTotalMemberOfSamityToGenerateStagingData(String samityId) {
        return repository.getTotalMemberOfSamityToGenerateStagingData(samityId);
    }

    @Override
    public Flux<SamityMemberCount> getTotalMemberListOfSamityByOfficeId(String officeId) {
        return repository.getTotalMemberListOfSamityByOfficeId(officeId);
    }

    @Override
    public Mono<String> deleteStagingProcessTrackerByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .thenReturn(managementProcessId);
    }


    @Override
    public Mono<List<String>> getAllSamityIdListByOfficeId(String officeId) {
        return repository.getAllSamityIdListByOfficeId(officeId)
                .collectList();
    }

    @Override
    public Mono<List<StagingProcessTrackerEntity>> getAllStagingProcessTrackerEntityByManagementProcessId(String managementProcessId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<String> deleteAllStagingProcessTrackerEntityByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Staging Process Tracker Deleted Successfully"));
    }

    @Override
    public Flux<StagingProcessTrackerEntity> getStagingProcessTrackerEntityBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdInOrderBySamityId(samityIdList);
    }


//    Process Management v2
    @Override
    public Flux<String> getSamityIdListByOfficeId(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .map(StagingProcessTrackerEntity::getSamityId)
                .sort(Comparator.comparing(String::toString));
    }

    @Override
    public Mono<List<StagingProcessTrackerEntity>> saveStagingProcessTrackerEntityList(List<StagingProcessTrackerEntity> stagingProcessTrackerEntityList) {
        stagingProcessTrackerEntityList.forEach(entity -> {
            entity.setCreatedOn(LocalDateTime.now());
        });
        return repository.saveAll(stagingProcessTrackerEntityList)
                .collectList();
    }

    @Override
    public Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityToProcessing(String samityId, String status) {
        return repository.getStagingProcessTrackerEntityBySamityId(samityId)
                .map(entity -> {
                    entity.setStatus(status);
                    entity.setProcessStartTime(LocalDateTime.now());
                    return entity;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityToFinished(String samityId, String status, Integer totalMemberStaged, Integer totalAccountStaged) {
        return repository.getStagingProcessTrackerEntityBySamityId(samityId)
                .flatMap(entity -> repository.getStagingDataIdBySamity(samityId, entity.getManagementProcessId())
                        .collectList()
                        .doOnNext(strings -> log.info("staging data id list : {}", strings))
                        .doOnNext(stagingDataIdList -> log.info("samity id : {} , total member staged : {} , Staging Data List size: {}",samityId, totalMemberStaged, stagingDataIdList.size()))
                        .filter(stagingDataIdList -> stagingDataIdList.size() == totalMemberStaged)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something Went Wrong, Staging Data Mismatch Found")))
                        .map(stagingDataIdList -> {
                            entity.setStagingDataIds(gson.toJson(stagingDataIdList, ArrayList.class));
                            entity.setTotalMemberStaged(totalMemberStaged);
                            entity.setTotalAccountStaged(totalAccountStaged);
                            entity.setStatus(status);
                            entity.setProcessEndTime(LocalDateTime.now());
                            return entity;
                        }))
                .flatMap(repository::save);
    }


    @Override
    public Mono<StagingProcessTrackerEntity> editUpdateAndDeleteProcessTracker(StagingProcessTrackerEntity trackerEntity, String loginId, String remarks) {
        AtomicReference<String> processTrackerOid = new AtomicReference<>();
        return Mono.just(trackerEntity)
                .map(entity -> {
                    processTrackerOid.set(entity.getOid());
                    StagingProcessTrackerEditHistoryEntity editHistoryEntity = gson.fromJson(entity.toString(), StagingProcessTrackerEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .flatMap(editHistoryRepository::save)
                .map(editHistoryEntity -> {
                    trackerEntity.setOid(processTrackerOid.get());
//                    trackerEntity.setStatus(Status.STATUS_EXCEPTION.getValue());
                    trackerEntity.setStatus(Status.STATUS_INVALIDATED.getValue());
                    trackerEntity.setInvalidatedBy(loginId);
                    trackerEntity.setInvalidatedOn(LocalDateTime.now());
//                    trackerEntity.setExceptionMarkedBy(loginId);
                    trackerEntity.setRemarks(remarks);
                    return trackerEntity;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityForRegeneration(StagingProcessTrackerEntity trackerEntity, String loginId) {
        AtomicReference<String> processTrackerOid = new AtomicReference<>();
        return Mono.just(trackerEntity)
                .map(entity -> {
                    processTrackerOid.set(entity.getOid());
                    StagingProcessTrackerEditHistoryEntity editHistoryEntity = gson.fromJson(entity.toString(), StagingProcessTrackerEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .flatMap(editHistoryRepository::save)
                .map(editHistoryEntity -> {
                    trackerEntity.setOid(processTrackerOid.get());
                    trackerEntity.setRegeneratedBy(loginId);
                    trackerEntity.setStatus(Status.STATUS_WAITING.getValue());
                    trackerEntity.setCurrentVersion(trackerEntity.getCurrentVersion() + 1);
                    trackerEntity.setProcessStartTime(null);
                    trackerEntity.setProcessEndTime(null);
                    return trackerEntity;
                })
                .flatMap(repository::save);
    }

    @Override
    public Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityListForOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeIdOrderBySamityId(managementProcessId, officeId)
                .collectList();
    }

    @Override
    public Flux<StagingProcessTrackerEntity> getStagingProcessEntityListBySamityIdList(String managementProcessId, List<String> samityIdList){
        return repository.findAllByManagementProcessIdAndSamityIdInOrderBySamityId(managementProcessId, samityIdList);
    }

    @Override
    public Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityListForFieldOfficer(String managementProcessId, String fieldOfficerId) {
        return repository.findAllByManagementProcessIdAndFieldOfficerIdOrderBySamityId(managementProcessId, fieldOfficerId)
                .collectList();
    }

    @Override
    public Mono<StagingProcessTrackerEntity> getStagingProcessEntityForSamity(String managementProcessId, String samityId) {
        return repository.findFirstByManagementProcessIdAndSamityId(managementProcessId, samityId);
    }

    @Override
    public Flux<StagingProcessTrackerEntity> getStagingProcessEntityByOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId);
    }

    @Override
    public Mono<List<String>> updateStagingProcessTrackerDataToEditHistoryTable(String managementProcessId, String processId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .collectList()
                .flatMap(trackerEntityList -> {
                    List<StagingProcessTrackerEditHistoryEntity> editHistoryEntityList = trackerEntityList.stream()
                            .map(trackerEntity -> {
                                StagingProcessTrackerEditHistoryEntity editHistoryEntity = gson.fromJson(trackerEntity.toString(), StagingProcessTrackerEditHistoryEntity.class);
                                editHistoryEntity.setOid(null);
                                return editHistoryEntity;
                            })
                            .sorted(Comparator.comparing(StagingProcessTrackerEditHistoryEntity::getSamityId))
                            .toList();
                    return editHistoryRepository.saveAll(editHistoryEntityList)
                            .collectList();
                })
                .map(editHistoryEntityList -> editHistoryEntityList.stream().map(StagingProcessTrackerEditHistoryEntity::getSamityId).sorted().toList());
    }

    @Override
    public Mono<String> updateProcessTrackerForDownloadedStagingDataDeletionByFieldOfficer(String managementProcessId, String fieldOfficerId) {
        return repository.findAllByManagementProcessIdAndFieldOfficerIdOrderBySamityId(managementProcessId, fieldOfficerId)
                .map(entity -> {
                    entity.setIsDownloaded("No");
                    entity.setDownloadedBy(null);
                    entity.setDownloadedOn(null);
                    return entity;
                })
                .flatMap(repository::save)
                .collectList()
                .map(list -> "Staging Process Tracker Entity Updated Successfully For Staging Data Deletion By Field Officer")
                .doOnNext(log::info);
    }

    @Override
    public Mono<List<StagingProcessTrackerEntity>> UpdateStagingProcessEntityListForDownloadByFieldOfficer(String managementProcessId, String fieldOfficerId, String loginId) {
        return repository.findAllByManagementProcessIdAndFieldOfficerIdOrderBySamityId(managementProcessId, fieldOfficerId)
                .map(entity -> {
                    entity.setIsDownloaded("Yes");
                    entity.setDownloadedBy(loginId);
                    entity.setDownloadedOn(LocalDateTime.now());
                    return entity;
                })
                .flatMap(repository::save)
                .collectList()
                .doOnNext(list -> log.info("Staging Process Tracker Entity Updated Successfully For Staging Data Download By Field Officer"));
    }

    @Override
    public Mono<String> resetStagingProcessTrackerEntriesByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Staging Process Reset Successfully"));
    }
}
