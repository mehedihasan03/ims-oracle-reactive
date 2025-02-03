package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import net.celloscope.mraims.loanportfolio.features.withdraw.domain.Withdraw;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository.IWithdrawStagingDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository.IWithdrawStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class WithdrawStagingDataPersistenceAdapter implements IWithdrawStagingDataPersistencePort {

    private final IWithdrawStagingDataRepository repository;
    private final IWithdrawStagingDataEditHistoryRepository editHistoryRepository;
    private final Gson gson;
    private final ModelMapper mapper;

    public WithdrawStagingDataPersistenceAdapter(IWithdrawStagingDataRepository repository, IWithdrawStagingDataEditHistoryRepository editHistoryRepository, ModelMapper mapper) {
        this.repository = repository;
        this.editHistoryRepository = editHistoryRepository;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<StagingWithdrawData> getWithdrawStagingDataBySavingsAccountId(String savingsAccountId) {
        return repository.findWithdrawStagingDataEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.just(StagingWithdrawDataEntity.builder().build()))
                .map(withdrawStagingDataEntity -> gson.fromJson(withdrawStagingDataEntity.toString(), StagingWithdrawData.class))
                .doOnNext(withdrawStagingData -> log.info("withdrawStagingData: {}", withdrawStagingData));
    }

    @Override
    public Flux<StagingWithdrawData> getWithdrawStagingDataBySamityIdAndWithdrawType(String samityId, String withdrawType) {
        return repository.findWithdrawStagingDataEntitiesBySamityIdAndWithdrawType(samityId, withdrawType)
                .map(withdrawStagingDataEntity -> gson.fromJson(withdrawStagingDataEntity.toString(), StagingWithdrawData.class))
                .doOnNext(withdrawStagingData -> log.info("withdrawStagingData: {}", withdrawStagingData));
    }

    @Override
    public Mono<BigDecimal> getTotalWithdrawAmountOfASamity(String samityId) {
        return repository.getTotalWithdrawAmountOfASamity(samityId)
                .switchIfEmpty(Mono.just(BigDecimal.valueOf(0.0)))
                .doOnNext(amount -> log.info("Total Withdraw By {} Samity: {}", samityId, amount));
    }

    @Override
    public Flux<StagingWithdrawData> getAllWithdrawStagingDataBySamity(String samityId) {
        return repository.findAllBySamityIdOrderByCreatedOn(samityId)
                .map(entity -> gson.fromJson(entity.toString(), StagingWithdrawData.class));
    }

    @Override
    public Mono<List<StagingWithdrawData>> saveStagingWithdrawData(List<StagingWithdrawData> stagingWithdrawDataList) {
        List<StagingWithdrawDataEntity> entityList = stagingWithdrawDataList.stream().map(stagingWithdrawData -> gson.fromJson(stagingWithdrawData.toString(), StagingWithdrawDataEntity.class)).toList();
        List<String> savingsAccountIdList = stagingWithdrawDataList.stream().map(StagingWithdrawData::getSavingsAccountId).toList();
        return repository.findAllBySavingsAccountIdIn(savingsAccountIdList)
                .doOnRequest(l -> log.info("savingsAccountList TEST : {}", savingsAccountIdList))
                .collectList()
                .flatMap(stagingWithdrawDataEntities -> {
                    if (!stagingWithdrawDataEntities.isEmpty()) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data Already Exists"));
                    } else
                        return Mono.just(stagingWithdrawDataEntities);
                })
                /*.filter(stagingWithdrawDataEntities -> !stagingWithdrawDataEntities.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data Already Exists")))*/
                .map(list -> entityList)
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(stagingWithdrawDataEntities -> stagingWithdrawDataList);
    }

    @Override
    public Mono<List<StagingWithdrawData>> updateWithdrawPayment(List<StagingWithdrawData> stagingWithdrawDataList, String loginId) {
        List<String> accountIdList = stagingWithdrawDataList.stream().map(StagingWithdrawData::getSavingsAccountId).toList();
        return repository.findAllBySavingsAccountIdIn(accountIdList)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data Found")))
                .filter(entity -> entity.getCreatedBy().equals(loginId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "User Cannot update Data, user mismatch")))
                .collectList()
                .filter(entityList -> entityList.size() == accountIdList.size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Data Mismatch, please try again")))
                .flatMap(entityList -> {
                    List<StagingWithdrawDataEditHistoryEntity> editHistoryEntityList = entityList.stream().map(entity -> {
                        StagingWithdrawDataEditHistoryEntity editHistoryEntity = gson.fromJson(entity.toString(), StagingWithdrawDataEditHistoryEntity.class);
                        editHistoryEntity.setOid(null);
                        editHistoryEntity.setStagingWithdrawDataEditHistoryId(UUID.randomUUID().toString());
                        return editHistoryEntity;
                    }).toList();
                    return editHistoryRepository.saveAll(editHistoryEntityList).collectList().map(editList -> entityList);
                })
                .map(entityList -> entityList.stream().peek(entity -> stagingWithdrawDataList.forEach(data -> {
                    if(data.getStagingDataId().equals(entity.getStagingDataId()) && data.getSavingsAccountId().equals(entity.getSavingsAccountId())){
                        entity.setAmount(data.getAmount());
                        entity.setIsNew("No");
                        entity.setCurrentVersion(entity.getCurrentVersion() + 1);
                        entity.setUpdatedOn(LocalDateTime.now());
                        entity.setUpdatedBy(loginId);
                    }
                })).toList())
                .flatMapMany(repository::saveAll)
                .map(entity -> gson.fromJson(entity.toString(), StagingWithdrawData.class))
                .collectList();
    }

    @Override
    public Flux<StagingWithdrawData> getAllWithdrawDataForSamity(String managementProcessId, String samityId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .switchIfEmpty(Mono.just(StagingWithdrawDataEntity.builder().build()))
                .map(entity -> gson.fromJson(entity.toString(), StagingWithdrawData.class));
    }

    @Override
    public Mono<List<StagingWithdrawData>> getAllWithdrawDataByManagementProcessIdAndSamityId(String managementProcessId, String samityId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .map(entity -> gson.fromJson(entity.toString(), StagingWithdrawData.class))
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawDataForSubmission(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is already Locked for Authorization")))
                .flatMapIterable(entityList -> entityList)
                .map(entity -> {
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setSubmittedBy(loginId);
                    entity.setSubmittedOn(LocalDateTime.now());
                    entity.setIsSubmitted("Yes");
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> "Withdraw is successfully updated for submission");
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
        return repository.findAllBySamityId(samityId)
                .collectList()
                .flatMap(list -> {
                    if(!list.isEmpty()){
                        return this.validateAndLockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId).doOnNext(id -> log.info("No Withdraw Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return repository.findAllBySamityId(samityId)
                .collectList()
                .flatMap(list -> {
                    if(!list.isEmpty()){
                        return this.validateAndUnlockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No Withdraw Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
        return repository.getSamityIdListLockedByUserForAuthorization(lockedBy)
                .distinct(String::chars)
                .collectList();
    }

    @Override
    public Mono<List<StagingWithdrawData>> getWithdrawStagingDataListBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdIn(samityIdList)
                .map(entity -> gson.fromJson(entity.toString(), StagingWithdrawData.class))
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Id: {}, Total Withdraw Data: {}", samityId, entityList.size()))
                .flatMap(entityList -> {
                    if(!entityList.isEmpty()){
                        return this.validateAndUpdateWithdrawDataForAuthorization(samityId, entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Id: {}, Total Withdraw Data: {}", samityId, entityList.size()))
                .flatMap(entityList -> {
                    if(!entityList.isEmpty()){
                        return this.validateAndUpdateWithdrawDataForRejection(samityId, entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Withdraw Data is Found for Samity")))
                .filter(data -> data.getIsLocked().equals("Yes"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is Not Locked for Unauthorization")))
                .filter(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is Not Authorized")))
                .map(entity -> {
//                    entity.setStatus(Status.STATUS_UNAUTHORIZED.getValue());
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setApprovedBy(null);
                    entity.setApprovedOn(null);
                    entity.setIsLocked("No");
                    entity.setLockedBy(null);
                    entity.setLockedOn(null);
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> "Withdraw Data is successfully updated for Unauthorization");
    }

    @Override
    public Flux<WithdrawStagingData> getAllWithdrawCollectionDataByLoginId(String managementProcessId, String loginId, Integer limit, Integer offset) {
        return repository.findAllWithdrawStagingDataByLoginId(managementProcessId, loginId, limit, offset)
                .doOnNext(entity -> log.info("Collection withdraw Staging data : {}", entity))
                .map(entity -> mapper.map(entity, WithdrawStagingData.class));
    }

    @Override
    public Mono<WithdrawStagingData> getWithdrawCollectionDataByOid(String oid) {
        return repository.findByOid(oid)
                .doOnNext(entity -> log.info("Collection Staging data by Oid: {}", entity))
                .map(entity -> mapper.map(entity, WithdrawStagingData.class));
    }

    @Override
    public Mono<StagingWithdrawData> getWithdrawCollectionDataBySavingsAccountIdAndManagementProcessId(String savingsAccountId, String managementProcessId) {
        return repository.findFirstByManagementProcessIdAndSavingsAccountId(managementProcessId, savingsAccountId)
                .doOnNext(entity -> log.info("Collection Staging data by savingsAccountId: {}", savingsAccountId))
                .map(entity -> mapper.map(entity, StagingWithdrawData.class));
    }

    @Override
    public Mono<StagingWithdrawDataEditHistoryEntity> saveWithdrawEditHistory(StagingWithdrawDataEditHistoryEntity withdrawDataEditHistoryEntity) {
        return editHistoryRepository.save(withdrawDataEditHistoryEntity)
                .doOnNext(entity -> log.info("Withdraw Edit History: {}", entity))
                .doOnSuccess(entity -> log.info("Withdraw Edit History Saved Successfully"));
    }

    private Mono<String> validateAndUpdateWithdrawDataForRejection(String samityId, List<StagingWithdrawDataEntity> stagingWithdrawDataEntityList, String loginId) {
        return this.checkIfAllWithdrawDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(stagingWithdrawDataEntityList)
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_REJECTED.getValue());
                            entity.setRejectedBy(loginId);
                            entity.setRejectedOn(LocalDateTime.now());
                            entity.setIsSubmitted("No");
                            entity.setSubmittedBy(null);
                            entity.setSubmittedOn(null);
                            entity.setIsLocked("No");
                            entity.setLockedBy(null);
                            entity.setLockedOn(null);
                        })
                        .toList())
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Withdraw Data Updated For Rejection"))
                .map(entityList -> samityId);
    }

    private Mono<String> validateAndUpdateWithdrawDataForAuthorization(String samityId, List<StagingWithdrawDataEntity> stagingWithdrawDataEntityList, String loginId) {
        return this.checkIfAllWithdrawDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(stagingWithdrawDataEntityList)
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_APPROVED.getValue());
                            entity.setApprovedBy(loginId);
                            entity.setApprovedOn(LocalDateTime.now());
                            entity.setIsLocked("No");
                            entity.setLockedBy(null);
                            entity.setLockedOn(null);
                        })
                        .toList())
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Withdraw Data Updated For Authorization"))
                .map(entityList -> samityId);
    }

    private Mono<List<StagingWithdrawDataEntity>> checkIfAllWithdrawDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(List<StagingWithdrawDataEntity> stagingWithdrawDataEntityList){
        return Mono.just(stagingWithdrawDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(stagingWithdrawDataEntity -> stagingWithdrawDataEntity.getStatus().equals(Status.STATUS_SUBMITTED.getValue()) || stagingWithdrawDataEntity.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is not Submitted for Authorization Process")))
                .filter(entityList -> entityList.stream().allMatch(stagingWithdrawDataEntity -> stagingWithdrawDataEntity.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is not Locked for Authorization Process")))
                .doOnNext(entityList -> log.info("Samity Withdraw Data Validated For Authorization Process"));
    }

    private Mono<String> validateAndLockSamity(List<StagingWithdrawDataEntity> stagingWithdrawDataEntityList, String samityId, String loginId) {
        return Mono.just(stagingWithdrawDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().trim().equalsIgnoreCase(Status.STATUS_YES.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Withdraw is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().trim().equalsIgnoreCase(Status.STATUS_NO.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Withdraw is Already Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked(Status.STATUS_YES.getValue());
                        entity.setLockedBy(loginId);
                        entity.setLockedOn(LocalDateTime.now());
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Withdraws are Locked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUnlockSamity(List<StagingWithdrawDataEntity> stagingWithdrawDataEntityList, String samityId, String loginId) {
        return Mono.just(stagingWithdrawDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getStatus().trim().equalsIgnoreCase(Status.STATUS_SUBMITTED.getValue()) || entity.getStatus().equalsIgnoreCase(Status.STATUS_UNAUTHORIZED.getValue()) || entity.getStatus().equalsIgnoreCase(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Withdraw is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().trim().equalsIgnoreCase(Status.STATUS_YES.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Withdraw is Not Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked(Status.STATUS_NO.getValue());
                        entity.setLockedBy(null);
                        entity.setLockedOn(null);
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Withdraws are Unlocked for Authorization", samityId))
                .map(list -> samityId);
    }

    @Override
    public Flux<WithdrawStagingData> getWithdrawDataByOidList(List<String> oidList) {
        return repository.findAllByOidIn(oidList)
                .map(withdrawEntity -> mapper.map(withdrawEntity, WithdrawStagingData.class))
                .doOnNext(withdraw -> log.info("Successfully got withdraw data in database by oid list"));
    }

    @Override
    public Mono<String> updateSubmittedWithdrawData(String loginId, String oid) {
        return repository.findByOid(oid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Withdraw Data Found for Submission")))
                .map(entity -> {
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setSubmittedBy(loginId);
                    entity.setSubmittedOn(LocalDateTime.now());
                    entity.setIsSubmitted("Yes");
                    return entity;
                })
                .flatMap(repository::save)
                .map(entityList -> "Withdraw data is successfully updated for submission");
    }

    @Override
    public Mono<String> deleteWithdrawData(String oid) {
        return repository.deleteById(oid)
                .doOnRequest(l -> log.info("Request received to delete withdraw data by oid: {}", oid))
                .doOnSuccess(entity -> log.info("Withdraw Data Deleted Successfully"))
                .thenReturn("Withdraw Data Deleted Successfully");
    }

    @Override
    public Mono<Long> countWithdrawData(String managementProcessId, String loginId) {
        log.info("Getting Count of Collection Withdraw data between date: {} {}", managementProcessId, loginId);
        return repository.countWithdrawData(managementProcessId, loginId)
                .doOnNext(count -> log.info("Count of Collection Withdraw data : {}", count));
    }
}
