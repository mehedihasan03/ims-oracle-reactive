package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanInfo;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.RebatePaymentInfo;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoanRebatePersistenceAdapter implements LoanRebatePersistencePort {
    private final LoanRebateRepository loanRebateRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;

    @Override
    public Mono<LoanRebate> saveLoanRebate(LoanRebate loanRebate) {
        return Mono.just(loanRebate)
                .map(domain -> {
                    modelMapper.getConfiguration()
                            .setSkipNullEnabled(true)
                            .setMatchingStrategy(MatchingStrategies.STRICT);
                    LoanRebateEntity entity = modelMapper.map(domain, LoanRebateEntity.class);
                    entity.setLoanInfo(new Gson().toJson(domain.getLoanInfo()));
                    log.info("Mapped Loan Rebate Domain to Entity before saving into DB: {}", entity);
                    return entity;
                })
                .flatMap(loanRebateEntity -> {
                    log.info("Requested Loan Rebate Entity for saving into DB: {}", loanRebateEntity);
                    return loanRebateRepository.save(loanRebateEntity)
                            .doOnSuccess(res -> log.info("Successfully saved loan rebate into DB: {}", res));
                })
                .doOnRequest(req -> log.info("Request received for saving loan rebate into DB: {}", loanRebate))
                .map(entity -> modelMapper.map(entity, LoanRebate.class))
                .doOnSuccess(res -> log.info("Mapped LoanRebateEntity to LoanRebate after saving into DB: {}", res))
                .doOnError(e -> log.error("Error occurred while saving loan rebate into DB: {}", e.getMessage()))
                .onErrorMap(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()))
                .onErrorMap(ExceptionHandlerUtil.class, e -> new ExceptionHandlerUtil(e.getCode(), e.getMessage()));
    }

    @Override
    public Mono<LoanRebate> getLoanRebateByOid(String oid) {
        return loanRebateRepository.findById(oid)
                .doOnSuccess(entity -> log.info("Loan Rebate Entity found by oid: {}", entity))
                .map(entity -> {
                    LoanRebate rebate = modelMapper.map(entity, LoanRebate.class);
                    rebate.setLoanInfo(new Gson().fromJson(entity.getLoanInfo(), LoanInfo.class));
                    rebate.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), RebatePaymentInfo.class));
                    return rebate;
                })
                .doOnRequest(req -> log.info("Request received for getting loan rebate by oid: {}", oid))
                .doOnSuccess(res -> log.info("Successfully completed getting loan rebate by oid: {}", res))
                .doOnError(e -> log.error("Error occurred while getting loan rebate by oid: {}", e.getMessage()));
    }

    @Override
    public Mono<LoanRebate> updateLoanRebate(LoanRebate loanRebate) {
        return null;
    }

    @Override
    public Flux<LoanRebate> getLoanRebateDataByOfficeId(String officeId, LocalDateTime startDate, LocalDateTime endDate) {
        return loanRebateRepository.getLoanRebateDataByOfficeIdInASpecificDateRange(officeId, startDate, endDate)
                .map(entity -> {
                    LoanRebate rebate = modelMapper.map(entity, LoanRebate.class);
                    rebate.setLoanInfo(new Gson().fromJson(entity.getLoanInfo(), LoanInfo.class));
                    rebate.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), RebatePaymentInfo.class));
                    return rebate;
                })
                .doOnRequest(req -> log.info("Request received for getting loan rebate by office id: {}", officeId))
                .doOnNext(res -> log.info("Getting response for loan rebate by office id:{}", res))
                .doOnComplete(() -> log.info("Successfully completed getting loan rebate by office id: {}", officeId))
                .doOnError(e -> log.error("Error occurred while getting loan rebate by office id: {}", e.getMessage()));
    }

    @Override
    public Mono<List<LoanRebateEntity>> getAllLoanRebateDataByManagementProcessId(String managementProcessId) {
        return loanRebateRepository
                .findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList()
                ;
    }

    @Override
    public Mono<String> deleteAllByManagementProcessId(String managementProcessId) {
        return loanRebateRepository
                .deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Deleted all loan rebate data"))
                ;
    }


    @Override
    public Flux<LoanRebateDTO> getLoanRebateDataBySamityId(String samityId, String managementProcessId) {
        return loanRebateRepository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .map(entity -> {
                    LoanRebateDTO dto = modelMapper.map(entity, LoanRebateDTO.class);
                    return dto;
                });
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return loanRebateRepository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndLockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No Loan Rebate Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return loanRebateRepository.getSamityIdListLockedByUserForAuthorization(loginId)
                .distinct(String::chars)
                .collectList();
    }

    @Override
    public Mono<List<LoanRebateDTO>> getAllLoanRebateDataBySamityIdList(List<String> samityIdList) {
        return loanRebateRepository.findAllBySamityIdIn(samityIdList)
                .map(entity -> modelMapper.map(entity, LoanRebateDTO.class))
                .collectList();
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return loanRebateRepository.findAllBySamityIdIn(List.of(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndUnlockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId).doOnNext(id -> log.info("No Loan Rebate Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<String> validateAndUpdateLoanRebateDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return loanRebateRepository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .doOnNext(entityList -> log.info("Samity Id: {}, Total Loan Rebate Data: {}",
                        samityId,
                        entityList.size()))
                .flatMap(entityList -> {
                    if (!entityList.isEmpty()) {
                        return this.validateAndUpdateLoanRebateDataForRejection(samityId,
                                entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }


    public Mono<List<LoanRebateDTO>> updateStatusOfLoanRebateDataForAuthorization(String samityId, String loginId, String managementProcessId) {
        return loanRebateRepository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .flatMap(loanRebateEntity -> loanRebateRepository
                        .updateLoanRebateEntitiesForAuthorization(loanRebateEntity.getOid(), loginId, LocalDateTime.now(), Status.STATUS_APPROVED.getValue())
                        .doOnRequest(l -> log.info("Request received to Updated Loan Rebate for Authorization"))
                        .doOnComplete(() -> log.info("Loan Rebate Data for Samity Updated for Authorization"))
                        .then(Mono.just(loanRebateEntity)))
                .doOnComplete(() -> log.info("Loan Rebate Data for Samity Updated for Authorization"))
                .map(entity -> modelMapper.map(entity, LoanRebateDTO.class))
                .collectList();
    }

    @Override
    public Mono<LoanRebateDTO> updateLoanRebateDataOnUnAuthorization(LoanRebateDTO loanRebateDTO) {
        return loanRebateRepository.getLoanRebateEntityByLoanRebateDataId(loanRebateDTO.getLoanRebateDataId())
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
                .flatMap(loanRebateRepository::save)
                .map(entity -> modelMapper.map(entity, LoanRebateDTO.class));
    }

    private Mono<String> validateAndLockSamity(List<LoanRebateEntity> loanRebateEntityList, String samityId, String loginId) {
        return Mono.just(loanRebateEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Rebate Data is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Rebate Data is Already Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked("Yes");
                        entity.setLockedBy(loginId);
                        entity.setLockedOn(LocalDateTime.now());
                        entity.setEditCommit(Status.STATUS_NO.getValue());
                    });
                    return entityList;
                })
                .flatMapMany(loanRebateRepository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan Rebate Data are Locked for Authorization", samityId))
                .map(list -> samityId);
    }


    private Mono<String> validateAndUnlockSamity(List<LoanRebateEntity> loanRebateEntityList,
                                                 String samityId, String loginId) {
        return Mono.just(loanRebateEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Rebate Data is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Rebate Data is Not Locked")))
                .flatMapMany(Flux::fromIterable)
                .filter(entity -> entity.getLockedBy().equals(loginId))
                .collectList()
                .filter(entityList -> !entityList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " is locked by Someone Else")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked("No");
                        entity.setLockedBy(null);
                        entity.setLockedOn(null);
                        entity.setEditCommit(Status.STATUS_YES.getValue());
                    });
                    return entityList;
                })
                .flatMapMany(loanRebateRepository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan Rebate Data are Unlocked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUpdateLoanRebateDataForRejection(String samityId,
                                                                         List<LoanRebateEntity> loanRebateEntityList, String loginId) {
        return this.checkIfAllLoanRebateDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
                        loanRebateEntityList)
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
                .flatMapMany(loanRebateRepository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Loan Rebate Data Updated For Rejection"))
                .map(entityList -> samityId);
    }

    private Mono<List<LoanRebateEntity>> checkIfAllLoanRebateDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
            List<LoanRebateEntity> loanRebateEntityList) {
        return Mono.just(loanRebateEntityList)
                .filter(entityList -> entityList.stream()
                        .allMatch(loanAdjustmentDataEntity -> loanAdjustmentDataEntity
                                .getStatus()
                                .equals(Status.STATUS_SUBMITTED.getValue())
                                || loanAdjustmentDataEntity.getStatus().equals(
                                Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Rebate Data is not Submitted for Authorization Process")))
                .filter(entityList -> entityList.stream()
                        .allMatch(loanRebateEntity -> loanRebateEntity
                                .getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Rebate Data is not Locked for Authorization Process")))
                .doOnNext(entityList -> log
                        .info("Rebate Data Validated For Authorization Process"));
    }

    @Override
    public Mono<String> deleteLoanRebateByOid(String oid) {
        return loanRebateRepository.deleteById(oid)
                .doOnRequest(req -> log.info("Request received for deleting Loan Rebate Data by Oid: {}", oid))
                .doOnError(e -> log.error("Error occurred while deleting Loan Rebate Data by Oid: {}", e.getMessage()))
                .then(Mono.just("Deleted Loan Rebate Data by Oid: " + oid));
    }

    @Override
    public Mono<LoanRebate> getLoanRebateByLoanAccountId(String loanAccountId) {
        return loanRebateRepository.findByLoanAccountId(loanAccountId)
                .map(entity -> {
                    LoanRebate rebate = modelMapper.map(entity, LoanRebate.class);
                    rebate.setLoanInfo(new Gson().fromJson(entity.getLoanInfo(), LoanInfo.class));
                    rebate.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), RebatePaymentInfo.class));
                    return rebate;
                });
    }
}
