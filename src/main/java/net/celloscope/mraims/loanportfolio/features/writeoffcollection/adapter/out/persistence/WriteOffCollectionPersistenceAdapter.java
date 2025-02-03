package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.LoanWriteOffDetailsResponseDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository.LoanWriteOffCollectionHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository.LoanWriteOffCollectionRepository;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.dto.LoanWriteOffCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out.WriteOffCollectionPort;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.LoanWriteOffCollection;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.WriteOffPaymentInfo;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteOffCollectionPersistenceAdapter implements WriteOffCollectionPort {

    private final ModelMapper modelMapper;
    private final LoanWriteOffCollectionRepository loanWriteOffCollectionRepository;
    private final LoanWriteOffCollectionHistoryRepository historyRepository;

    @Override
    public Mono<LoanWriteOffCollection> saveWriteOffCollection(LoanWriteOffCollection writeOffCollection) {
        log.info("Saving Loan Write Off Collection to DB : {}", writeOffCollection);
        return Mono.just(writeOffCollection)
                .map(data -> modelMapper.map(data, LoanWriteOffCollectionEntity.class))
                .flatMap(loanWriteOffCollectionRepository::save)
                .map(entity -> modelMapper.map(entity, LoanWriteOffCollection.class))
                .doOnRequest(value -> log.info("Save Request Received for Loan Write Off Collection : {}", value))
                .doOnSubscribe(subscription -> log.info("subscribe for save Loan Write Off Collection"))
                .doOnSuccess(entity -> log.info("successfully saved Loan Write Off Collection to DB: {}", entity))
                .doOnError(err -> log.info("Found error while saving Loan Write Off Collection to DB : {}", err.getLocalizedMessage()));
    }

    @Override
    public Mono<LoanWriteOffCollection> getWriteOffCollectionById(String oid) {
        return loanWriteOffCollectionRepository.findById(oid)
                .map(entity -> {
                    LoanWriteOffCollection writeOffCollection = modelMapper.map(entity, LoanWriteOffCollection.class);
                    writeOffCollection.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), WriteOffPaymentInfo.class));
                    return writeOffCollection;
                })
                .doOnRequest(req -> log.info("Request received for getting loan write off collection by oid: {}", oid))
                .doOnSuccess(res -> log.info("Successfully get loan write off collection by oid: {}", res))
                .doOnError(e -> log.error("Error occurred while getting loan write off collection by oid: {}", e.getMessage()));
    }

    @Override
    public Mono<LoanWriteOffCollection> getWriteOffCollectionByLoanAccountId(String loanAccountId) {
        return loanWriteOffCollectionRepository.findByLoanAccountId(loanAccountId)
                .map(entity -> {
                    LoanWriteOffCollection writeOffCollection = modelMapper.map(entity, LoanWriteOffCollection.class);
                    writeOffCollection.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), WriteOffPaymentInfo.class));
                    return writeOffCollection;
                })
                .doOnRequest(req -> log.info("Request received for getting loan write off collection by loan account id: {}", loanAccountId))
                .doOnSuccess(res -> log.info("Successfully get loan write off collection by loan account id: {}", res))
                .doOnError(e -> log.error("Error occurred while getting loan write off collection by loan account id: {}", e.getMessage()));
    }

    @Override
    public Mono<LoanWriteOffCollection> saveWriteOffCollectionHistory(LoanWriteOffCollection writeOffCollection) {
        return Mono.just(writeOffCollection)
                .map(data -> modelMapper.map(data, LoanWriteOffCollectionHistoryEntity.class))
                .flatMap(historyRepository::save)
                .map(entity -> {
                    LoanWriteOffCollection savedWriteOffCollection = modelMapper.map(entity, LoanWriteOffCollection.class);
                    savedWriteOffCollection.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), WriteOffPaymentInfo.class));
                    return savedWriteOffCollection;
                })
                .doOnRequest(value -> log.info("Save Request Received for Loan Write Off Collection History : {}", value))
                .doOnSubscribe(subscription -> log.info("subscribe for save Loan Write Off Collection History "))
                .doOnSuccess(entity -> log.info("successfully saved Loan Write Off Collection History to DB: {}", entity))
                .doOnError(err -> log.info("Found error while saving Loan Write Off Collection History to DB : {}", err.getLocalizedMessage()));
    }

    @Override
    public Flux<LoanWriteOffCollection> getCollectedWriteOffDataByOfficeId(String officeId, LocalDateTime startDate, LocalDateTime endDate) {
        return loanWriteOffCollectionRepository.getLoaWriteOffDataByOfficeIdInASpecificDateRange(officeId, startDate, endDate)
                .map(loanWriteOffCollectionEntity -> {
                    log.info("Found Loan Write Off Collection : {}", loanWriteOffCollectionEntity);
                    LoanWriteOffCollection writeOffCollection = modelMapper.map(loanWriteOffCollectionEntity, LoanWriteOffCollection.class);
                    writeOffCollection.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(loanWriteOffCollectionEntity.getPaymentInfo(), WriteOffPaymentInfo.class));
                    return writeOffCollection;
                })
                .doOnRequest(value -> log.info("Request Received for Loan Write Off Collection : {}", value))
                .doOnNext(loanWriteOffCollection -> log.info("Getting Response for Loan Write Off Collection : {}", loanWriteOffCollection))
                .doOnComplete(() -> log.info("Successfully Completed getting loan write off office id: {}", officeId))
                .doOnError(e -> log.error("Error occurred while getting loan write off office id: {}", e.getMessage()));
    }

    @Override
    public Mono<LoanWriteOffCollection> getDetailsOfLoanWriteOffCollection(String oid) {
        return loanWriteOffCollectionRepository.findWriteOffCollectionDetailsInfoByOid(oid)
                .map(loanWriteOffCollectionEntity -> {
                    log.info("Found Loan Write Off Collection Details Data: {}", loanWriteOffCollectionEntity);
                    LoanWriteOffCollection writeOffCollection = modelMapper.map(loanWriteOffCollectionEntity, LoanWriteOffCollection.class);
                    writeOffCollection.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(loanWriteOffCollectionEntity.getPaymentInfo(), WriteOffPaymentInfo.class));
                    return writeOffCollection;
                })
                .doOnRequest(value -> log.info("Request Received for Loan Write Off Details Collection : {}", value))
                .doOnNext(loanWriteOffCollection -> log.info("Getting Response for Loan Write Off Details Collection : {}", loanWriteOffCollection))
                .doOnError(e -> log.error("Error occurred while getting loan write off Details By id: {}", e.getMessage()));
    }

    @Override
    public Mono<List<LoanWriteOffCollectionEntity>> getAllWrittenOffCollectionDataByManagementProcessId(String managementProcessId) {
        return loanWriteOffCollectionRepository.findAllByManagementProcessId(managementProcessId)
                .collectList()
                .doOnRequest(value -> log.info("Request Received for getting all written off collection data by management process id: {}", managementProcessId))
                .doOnError(e -> log.error("Error occurred while getting all written off collection data by management process id: {}", e.getMessage()));
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return loanWriteOffCollectionRepository.findAllByManagementProcessId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndLockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No WrittenOff Loan Collection Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return loanWriteOffCollectionRepository.findAllBySamityIdIn(List.of(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndUnlockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No WrittenOff Loan Collection Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return loanWriteOffCollectionRepository.getSamityIdListLockedByUserForAuthorization(loginId)
                .distinct(String::chars)
                .collectList();
    }

    @Override
    public Mono<List<LoanWriteOffCollectionDTO>> getAllLoanWriteOffCollectionDataBySamityIdList(List<String> samityIdList) {
        return loanWriteOffCollectionRepository.findAllBySamityIdIn(samityIdList)
                .map(entity -> modelMapper.map(entity, LoanWriteOffCollectionDTO.class))
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateLoanWriteOffCollectionDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return loanWriteOffCollectionRepository.findAllByManagementProcessId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .doOnNext(entityList -> log.info("Samity Id: {}, Total Loan WriteOff Collection Data: {}",
                        samityId,
                        entityList.size()))
                .flatMap(entityList -> {
                    if (!entityList.isEmpty()) {
                        return this.validateAndUpdateLoanWriteOffCollectionDataForRejection(samityId,
                                entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    @Override
    public Flux<LoanWriteOffCollection> getLoanWriteOffCollectionBySamityId(String samityId, String managementProcessId) {
        return loanWriteOffCollectionRepository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .map(entity -> modelMapper.map(entity, LoanWriteOffCollection.class));
    }

    @Override
    public Mono<List<LoanWriteOffCollection>> updateStatusOfLoanWriteOffDataForAuthorization(String samityId, String loginId, String managementProcessId) {
        return loanWriteOffCollectionRepository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .flatMap(writeOffCollectionEntity -> loanWriteOffCollectionRepository
                        .updateLoanWriteOffEntitiesForAuthorization(writeOffCollectionEntity.getOid(), loginId, LocalDateTime.now(), Status.STATUS_APPROVED.getValue())
                        .doOnRequest(l -> log.info("Request received to Update Loan Write Off for Authorization"))
                        .doOnComplete(() -> log.info("Loan Write Off Data for Samity Updated for Authorization"))
                        .then(Mono.just(writeOffCollectionEntity)))
                .doOnComplete(() -> log.info("Loan Rebate Data for Samity Updated for Authorization"))
                .map(entity -> modelMapper.map(entity, LoanWriteOffCollection.class))
                .collectList();
    }

    @Override
    public Mono<LoanWriteOffCollection> updateLoanWriteOffDataOnUnAuthorization(LoanWriteOffCollectionDTO loanWriteOffCollection) {
        return loanWriteOffCollectionRepository.findByLoanWriteOffCollectionDataId(loanWriteOffCollection.getLoanWriteOffCollectionDataId())
                .doOnRequest(l -> log.info("Request received to get Write Off Entity to Update Loan Write Off for UnAuthorization: {}", loanWriteOffCollection))
                .map(entity -> {
//                    entity.setStatus(Status.STATUS_UNAUTHORIZED.getValue());
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setApprovedBy(null);
                    entity.setApprovedOn(null);
                    entity.setIsLocked("No");
                    entity.setLockedOn(null);
                    entity.setLockedBy(null);
                    return entity;
                })
                .flatMap(loanWriteOffCollectionRepository::save)
                .map(entity -> modelMapper.map(entity, LoanWriteOffCollection.class));
    }

    @Override
    public Mono<String> deleteAllByManagementProcessId(String managementProcessId) {
        return loanWriteOffCollectionRepository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Deleted All LoanWrite Off Data By Management Process Id"));
    }

    @Override
    public Mono<String> deleteWriteOffCollectionDataByOid(String oid) {
        return loanWriteOffCollectionRepository.deleteByOid(oid)
                .then(Mono.just("Deleted Loan Write Off Data By Oid"));
    }

    private Mono<String> validateAndLockSamity(List<LoanWriteOffCollectionEntity> loanWriteOffCollectionEntityList, String samityId, String loginId) {
        return Mono.just(loanWriteOffCollectionEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan WriteOff Collection Data is Not Submitted")))
                .filter(entityList -> entityList.stream().filter(loanWriteOffCollectionEntity -> Strings.isNotNullAndNotEmpty(loanWriteOffCollectionEntity.getIsLocked())).allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan WriteOff Collection Data is Already Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked(Status.STATUS_YES.getValue());
                        entity.setLockedBy(loginId);
                        entity.setLockedOn(LocalDateTime.now());
                        entity.setEditCommit(Status.STATUS_NO.getValue());
                    });
                    return entityList;
                })
                .flatMapMany(loanWriteOffCollectionRepository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan WriteOff Collection Data are Locked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUnlockSamity(List<LoanWriteOffCollectionEntity> loanWriteOffCollectionEntityList,
                                                 String samityId, String loginId) {
        return Mono.just(loanWriteOffCollectionEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Waiver Data is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Waiver Data is Not Locked")))
                .flatMapMany(Flux::fromIterable)
                .filter(entity -> entity.getLockedBy().equals(loginId))
                .collectList()
                .filter(entityList -> !entityList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " is locked by Someone Else")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked(Status.STATUS_NO.getValue());
                        entity.setLockedBy(null);
                        entity.setLockedOn(null);
                        entity.setEditCommit(Status.STATUS_YES.getValue());
                    });
                    return entityList;
                })
                .flatMapMany(loanWriteOffCollectionRepository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan WriteOff Collection Data are Unlocked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUpdateLoanWriteOffCollectionDataForRejection(String samityId,
                                                                                 List<LoanWriteOffCollectionEntity> loanWriteOffCollectionEntityList, String loginId) {
        return this.checkIfAllLoanWriteOffCollectionDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
                        loanWriteOffCollectionEntityList)
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_REJECTED.getValue());
                            entity.setRejectedBy(loginId);
                            entity.setRejectedOn(LocalDateTime.now());
                            entity.setIsSubmitted(Status.STATUS_NO.getValue());
                            entity.setSubmittedBy(null);
                            entity.setSubmittedOn(null);
                            entity.setIsLocked(Status.STATUS_NO.getValue());
                            entity.setLockedBy(null);
                            entity.setLockedOn(null);
                            entity.setEditCommit(Status.STATUS_YES.getValue());
                        })
                        .toList())
                .flatMapMany(loanWriteOffCollectionRepository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Loan Write Off Collection Data Updated For Rejection"))
                .map(entityList -> samityId);
    }

    private Mono<List<LoanWriteOffCollectionEntity>> checkIfAllLoanWriteOffCollectionDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
            List<LoanWriteOffCollectionEntity> LoanWriteOffCollectionEntityList) {
        return Mono.just(LoanWriteOffCollectionEntityList)
                .filter(entityList -> entityList.stream()
                        .allMatch(loanWriteOffCollectionEntity -> loanWriteOffCollectionEntity
                                .getStatus()
                                .equals(Status.STATUS_SUBMITTED.getValue())
                                || loanWriteOffCollectionEntity.getStatus().equals(
                                Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Collection Data is not Submitted for Authorization Process")))
                .filter(entityList -> entityList.stream()
                        .allMatch(loanWriteOffCollectionEntity -> loanWriteOffCollectionEntity
                                .getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Collection Data is not Locked for Authorization Process")))
                .doOnNext(entityList -> log
                        .info("Loan Write Off Collection Data Validated For Authorization Process"));
    }
}
