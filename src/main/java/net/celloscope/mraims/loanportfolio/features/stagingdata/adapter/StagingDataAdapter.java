package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.dto.StagingDataTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataDetailViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataMemberInfoDetailViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class StagingDataAdapter implements IStagingDataPersistencePort {

    private final IStagingDataRepository repository;
    private final IStagingDataEditHistoryRepository editHistoryRepository;
    private final ModelMapper mapper;
    private final Gson gson;

    public StagingDataAdapter(IStagingDataRepository repository, IStagingDataEditHistoryRepository editHistoryRepository, ModelMapper mapper) {
        this.repository = repository;
        this.editHistoryRepository = editHistoryRepository;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }


    @Override
    public Flux<StagingData> getStagingDataMemberInfoBySamityId(String samityId) {
        return repository.getMemberInfoBySamityId(samityId)
                .map(s -> mapper.map(s, StagingData.class));
    }


    @Override
    public Flux<StagingData> save(List<StagingData> stagingDataList) {
        return Flux.fromIterable(stagingDataList)
//                .map(stagingData -> {
//                    StagingDataEntity entity = mapper.map(stagingData, StagingDataEntity.class);
//                    entity.setStagingDataId(UUID.randomUUID().toString());
////					entity.setProcessId(stagingData.getProcessId());
//                    return entity;
//                })
                .map(stagingData -> gson.fromJson(stagingData.toString(), StagingDataEntity.class))
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .flatMapMany(entityList -> Flux.fromIterable(stagingDataList));
    }

    @Override
    public Mono<StagingDataDetailViewResponseDTO> getSamityInfoForStagingDataDetailView(String samityId) {
        return repository.getSamityInfoForStagingDataDetailView(samityId)
                .map(e -> mapper.map(e, StagingDataDetailViewResponseDTO.class));
    }


    @Override
    public Flux<MemberInfoDTO> getMemberInfoListForStagingDataDetailViewBySamityId(String samityId) {
        return repository.findAllBySamityId(samityId)
                .map(this::extractMobileNumberFromMobileDetails);
    }

    @Override
    public Mono<StagingDataMemberInfoDetailViewResponseDTO> getSamityInfoForStagingDataDetailViewByAccountId(String accountId) {
        return repository.getSamityInfoForStagingDataDetailViewByLoanAccountId(accountId)
                .map(e -> mapper.map(e, StagingDataMemberInfoDetailViewResponseDTO.class));
    }

    @Override
    public Mono<MemberInfoDTO> getMemberInfoForStagingDataDetailViewByAccountId(String accountId) {
        return repository.getMemberInfoForStagingDataDetailViewByAccountId(accountId)
                .map(this::extractMobileNumberFromMobileDetails);
    }

    @Override
    public Flux<StagingDataTransactionDTO> getStagingDataBySamityId(String samityId) {
        return repository
                .findAllBySamityId(samityId)
                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingDataTransactionDTO.class));
    }

    @Override
    public Mono<StagingDataMemberInfoDetailViewResponseDTO> getSamityInfoForStagingDataDetailViewByMemberId(String memberId) {
        return repository.getSamityInfoForStagingDataDetailViewByMemberId(memberId)
                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingDataMemberInfoDetailViewResponseDTO.class));
    }

    @Override
    public Mono<MemberInfoDTO> getMemberInfoForStagingDataDetailViewByMemberId(String memberId) {
        return repository.getStagingDataEntityByMemberId(memberId)
                .map(this::extractMobileNumberFromMobileDetails);
    }

    @Override
    public Flux<StagingData> getStagingDataByFieldOfficer(String fieldOfficerId) {
        return repository.findAllByFieldOfficerId(fieldOfficerId)
                .map(stagingDataEntity -> gson.fromJson(stagingDataEntity.toString(), StagingData.class))
                .doOnNext(stagingData -> log.info("Staging Data with field officer {}: {}", fieldOfficerId, stagingData));
    }

    @Override
    public Flux<String> findSamityIdListByFieldOfficerIdList(List<String> fieldOfficerIdList, Integer limit, Integer offset) {
        return repository.findSamityIdListByFieldOfficerIdList(fieldOfficerIdList, limit, offset)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_SAMITY_FOUND_FOR_OFFICER_LIST.getValue())));
    }

    @Override
    public Mono<Integer> getTotalCountOfStagingDataByFieldOfficerList(List<String> fieldOfficerIdList) {
        return repository.getTotalCountByFieldOfficerList(fieldOfficerIdList);
    }

    @Override
    public Flux<StagingData> getStagingDataBySamity(String samityId) {
        return repository.findAllBySamityId(samityId)
                .map(stagingDataEntity -> gson.fromJson(stagingDataEntity.toString(), StagingData.class));
    }

    @Override
    public Flux<String> findSamityIdListByFieldOfficerIdListForSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset) {
        return repository.findSamityIdListByFieldOfficerIdListForSamityDay(fieldOfficerIdList, samityDay, limit, offset);
    }

    @Override
    public Flux<String> findSamityIdListByFieldOfficerIdListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset) {
        return repository.findSamityIdListByFieldOfficerIdListForNonSamityDay(fieldOfficerIdList, samityDay, limit, offset);
    }

    @Override
    public Mono<Integer> getTotalCountByFieldOfficerListForSamityDay(List<String> fieldOfficerIdList, String samityDay) {
        return repository.getTotalCountByFieldOfficerListForSamityDay(fieldOfficerIdList, samityDay);
    }

    @Override
    public Mono<Integer> getTotalCountByFieldOfficerListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay) {
        return repository.getTotalCountByFieldOfficerListForNonSamityDay(fieldOfficerIdList, samityDay);
    }

    @Override
    public Flux<String> getSamityIdListByFieldOfficer(String fieldOfficerId) {
        return repository.getSamityIdListByFieldOfficer(fieldOfficerId)
                .doOnNext(s -> log.info("SamityId: {}", s));
    }

    @Override
    public Mono<String> deleteStagingDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .thenReturn(managementProcessId);
    }


    @Override
    public Mono<List<StagingDataEntity>> getAllStagingDataByManagementProcessId(String managementProcessId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<String> deleteAllStagingDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Staging Data Deleted Successfully"));
    }

    @Override
    public Mono<List<String>> saveAllStagingData(List<StagingData> stagingDataList) {
        return Flux.fromIterable(stagingDataList)
                .map(stagingData -> gson.fromJson(stagingData.toString(), StagingDataEntity.class))
                .map(stagingDataEntity -> {
                    stagingDataEntity.setOid(null);
                    return stagingDataEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .map(StagingDataEntity::getMemberId)
                .collectList();
    }

    @Override
    public Mono<List<String>> editUpdateAndDeleteStagingDataOfASamity(String processId, String samityId) {
        List<String> memberIdList = new ArrayList<>();
        return repository.findAllByProcessIdAndSamityId(processId, samityId)
                .map(entity -> {
                    StagingDataEditHistoryEntity editHistoryEntity = gson.fromJson(entity.toString(), StagingDataEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .collectList()
                .doOnNext(list -> memberIdList.addAll(list.stream().map(StagingDataEditHistoryEntity::getMemberId).toList()))
                .flatMap(editHistoryEntityList -> editHistoryRepository.saveAll(editHistoryEntityList)
                        .collectList())
                .flatMap(list -> repository.deleteAllByProcessIdAndSamityId(processId, samityId))
                .then(Mono.just(memberIdList));
    }

    @Override
    public Mono<StagingData> getStagingDataByMemberId(String memberId) {
        return repository.findFirstByMemberId(memberId)
//                .switchIfEmpty(Mono.just(StagingDataEntity.builder().build()))
                .map(stagingDataEntity -> gson.fromJson(stagingDataEntity.toString(), StagingData.class));
    }

    @Override
    public Mono<List<String>> updateStagingDataToEditHistoryTable(String managementProcessId, String processId, List<String> samityIdList) {
        return repository.findAllByManagementProcessIdAndProcessIdAndSamityIdIn(managementProcessId, processId, samityIdList)
                .map(stagingDataEntity -> {
                    StagingDataEditHistoryEntity editHistoryEntity = gson.fromJson(stagingDataEntity.toString(), StagingDataEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .sort(Comparator.comparing(StagingDataEditHistoryEntity::getMemberId))
                .collectList()
                .flatMapMany(editHistoryRepository::saveAll)
                .map(StagingDataEditHistoryEntity::getMemberId)
                .sort()
                .collectList();
    }

    @Override
    public Flux<StagingData> getAllStagingDataBySamity(String managementProcessId, String samityId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .map(stagingDataEntity -> gson.fromJson(stagingDataEntity.toString(), StagingData.class));
    }

    @Override
    public Mono<List<String>> getMemberIdListFromSatgingDataBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdIn(samityIdList)
                .map(StagingDataEntity::getMemberId)
                .distinct()
                .collectList();
    }

    @Override
    public Mono<StagingData> getStagingDataByStagingDataId(String stagingDataId) {
        return repository.findFirstByStagingDataId (stagingDataId)
                .map(stagingDataEntity -> gson.fromJson(stagingDataEntity.toString(), StagingData.class));
    }

    //TODO NUll Check
    private MemberInfoDTO extractMobileNumberFromMobileDetails(StagingDataEntity entity) {
        MemberInfoDTO member = mapper.map(entity, MemberInfoDTO.class);
        ArrayList mobileList = gson.fromJson(member.getMobile(), ArrayList.class);
        if (!mobileList.isEmpty()) {
            MobileInfoDTO mobileInfoDTO;
            try {
                mobileInfoDTO = gson.fromJson(mobileList.get(0).toString(), MobileInfoDTO.class);
            } catch (Exception e) {
                log.error("Error in parsing mobile info: {}", e.getMessage());
                mobileInfoDTO = new MobileInfoDTO();
            }
            member.setMobile(mobileInfoDTO.getContactNo());
        }
        return member;
    }
}
