package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.repository.LoanAdjustmentRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class LoanAdjustmentPersistenceAdapter implements LoanAdjustmentPersistencePort {

    private final LoanAdjustmentRepository repository;
    private final Gson gson;
    private final CommonValidation commonValidation;
    private final ModelMapper mapper;

    public LoanAdjustmentPersistenceAdapter(LoanAdjustmentRepository repository, CommonValidation commonValidation, ModelMapper mapper, ModelMapper modelMapper) {
            this.repository = repository;
        this.commonValidation = commonValidation;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<List<LoanAdjustmentData>> createAndSaveLoanAdjustmentData(
                    List<LoanAdjustmentData> loanAdjustmentDataList) {
            return Flux.fromIterable(loanAdjustmentDataList)
                            .flatMap(loanAdjustmentData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getLoanAccountId())) {
                                            return repository
                                                            .findFirstByLoanAccountId(
                                                                            loanAdjustmentData.getLoanAccountId())
                                                            .switchIfEmpty(Mono.just(LoanAdjustmentDataEntity
                                                                            .builder().build()))
                                                            .flatMap(loanAdjustmentDataEntity -> {
                                                                    if (!HelperUtil.checkIfNullOrEmpty(
                                                                                    loanAdjustmentDataEntity
                                                                                                    .getLoanAccountId())) {
                                                                            return Mono.error(
                                                                                            new ExceptionHandlerUtil(
                                                                                                            HttpStatus.BAD_REQUEST,
                                                                                                            "Loan Adjusted Account Already Found"));
                                                                    }
                                                                    return Mono.just(loanAdjustmentData);
                                                            });
                                    }
                                    return Mono.just(loanAdjustmentData);
                            })
                            .map(loanAdjustmentData -> gson.fromJson(loanAdjustmentData.toString(),
                                            LoanAdjustmentDataEntity.class))
                            .collectList()
                            .doOnNext(loanAdjustmentDataEntityList -> log.debug("Loan Adjustment Entity List: {}",
                                            loanAdjustmentDataEntityList))
                            .flatMapMany(repository::saveAll)
                            .collectList()
                            .map(entityList -> loanAdjustmentDataList);
    }

    @Override
    public Flux<LoanAdjustmentData> getLoanAdjustmentDataBySamity(String samityId) {
            return repository.findAllBySamityId(samityId)
                            .map(entity -> gson.fromJson(entity.toString(), LoanAdjustmentData.class));
    }

    @Override
    public Mono<List<LoanAdjustmentData>> updateStatusOfLoanAdjustmentDataForAuthorization(String samityId,
                    String loginId) {
            return repository.findAllBySamityId(samityId)
                            .map(loanAdjustmentDataEntity -> {
                                    loanAdjustmentDataEntity.setStatus(Status.STATUS_APPROVED.getValue());
                                    loanAdjustmentDataEntity.setApprovedBy(loginId);
                                    loanAdjustmentDataEntity.setApprovedOn(LocalDateTime.now());
                                    loanAdjustmentDataEntity.setIsLocked("No");
                                    loanAdjustmentDataEntity.setLockedOn(null);
                                    loanAdjustmentDataEntity.setLockedBy(null);
                                    return loanAdjustmentDataEntity;
                            })
                            .collectList()
                            .flatMapMany(repository::saveAll)
                            .map(entity -> gson.fromJson(entity.toString(), LoanAdjustmentData.class))
                            .collectList();
    }

    @Override
    public Mono<String> updateStatusToSubmitLoanAdjustmentDataForAuthorization(String managementProcessId, String samityId, String loginId) {
            return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                    .collectList()
                    .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is Locked for Authorization")))
                    .map(entityList -> entityList.stream()
                            .peek(entity -> {
                                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                                    entity.setIsSubmitted("Yes");
                                    entity.setSubmittedBy(loginId);
                                    entity.setSubmittedOn(LocalDateTime.now());
                            })
                            .toList())
                    .flatMapMany(repository::saveAll)
                    .collectList()
                    .map(entityList -> "Loan Adjustment Data Updated to Submit For Authorization");
    }

    @Override
    public Mono<String> updateStatusToSubmitLoanAdjustmentDataForAuthorizationByManagementProcessId(String managementProcessId,
                                                                                                    String processId,
                                                                                                    String loginId) {
        return repository.findAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .collectList()
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is Locked for Authorization")))
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                            entity.setIsSubmitted("Yes");
                            entity.setSubmittedBy(loginId);
                            entity.setSubmittedOn(LocalDateTime.now());
                        })
                        .toList())
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> "Loan Adjustment Data Updated to Submit For Authorization");
    }

    @Override
        public Flux<LoanAdjustmentData> getLoanAdjustmentDataByMemberId(String memberId) {
                return repository.findAllByMemberIdAndStatusIsNot(memberId, Status.STATUS_APPROVED.getValue())
                                .map(loanAdjustmentDataEntity -> gson.fromJson(loanAdjustmentDataEntity.toString(),
                                                LoanAdjustmentData.class));
        }

    @Override
    public Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataBySamityIdList(List<String> samityIdList) {
            return repository.findAllBySamityIdIn(samityIdList)
                            .map(entity -> gson.fromJson(entity.toString(), LoanAdjustmentData.class))
                            .collectList();
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
            return repository.findAllBySamityId(samityId)
                    .collectList()
                    .flatMap(list -> {
                            if (!list.isEmpty()) {
                                return this.validateAndLockSamity(list, samityId, loginId);
                            }
                            return Mono.just(samityId)
                                    .doOnNext(id -> log.info("No Loan Adjustment Data for Samity: {}", id));
                    });
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
            return repository.findAllBySamityId(samityId)
                            .collectList()
                            .flatMap(list -> {
                                    if (!list.isEmpty()) {
                                            return this.validateAndUnlockSamity(list, samityId, loginId);
                                    }
                                    return Mono.just(samityId).doOnNext(id -> log.info("No Loan Adjustment Data for Samity: {}", id));
                            });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
            return repository.getSamityIdListLockedByUserForAuthorization(lockedBy)
                            .distinct(String::chars)
                            .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(String managementProcessId,
                    String samityId, String loginId) {
            return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                            .collectList()
                            .doOnNext(entityList -> log.info("Samity Id: {}, Total Loan Adjustment Data: {}",
                                            samityId,
                                            entityList.size()))
                            .flatMap(entityList -> {
                                    if (!entityList.isEmpty()) {
                                            return this.validateAndUpdateLoanAdjustmentDataForRejection(samityId,
                                                            entityList, loginId);
                                    }
                                    return Mono.just(samityId);
                            });
    }

    @Override
    public Mono<String> validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(String managementProcessId,
                    String samityId, String loginId) {
            return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                            "No Loan Adjustment Data is Found for Samity")))
                            .filter(data -> data.getIsLocked().equals("Yes"))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                            "Loan Adjustment Data is Not Locked for Unauthorization")))
                            .filter(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                            .switchIfEmpty(Mono.error(
                                            new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                                            "Loan Adjustment Data is Not Authorized")))
                            .map(entity -> {
//                                    entity.setStatus(Status.STATUS_UNAUTHORIZED.getValue());
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
                            .map(entityList -> "Loan Adjustment Data is successfully updated for Unauthorization");
    }

    @Override
    public Mono<String> archiveAndDeleteLoanAdjustmentData(String managementProcessId) {
        return repository.findAllByManagementProcessId(managementProcessId)
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return repository.deleteAll(list)
                                .then(Mono.just("Loan Adjustment Data Archived and Deleted Successfully"));
                    }
                    return Mono.just("No Loan Adjustment Data Found for Management Process Id: " + managementProcessId);
                });
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessId(String managementProcessId) {
        return repository.findAllByManagementProcessId(managementProcessId)
                .map(entity -> gson.fromJson(entity.toString(), LoanAdjustmentData.class))
                .collectList();
    }

    @Override
    public Mono<String> deleteAllLoanAdjustmentDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Collection Data Deleted Successfully"));
    }

    @Override
    public Mono<String> deleteAllLoanAdjustmentDataByManagementProcessIdAndLoanAdjustmentProcessId(String managementProcessId, String loanAdjustmentProcessId) {
        return repository.deleteAllByManagementProcessIdAndLoanAdjustmentProcessId(managementProcessId, loanAdjustmentProcessId)
                .then(Mono.just("Collection Data Deleted Successfully"));
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return repository.findAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .map(entity -> gson.fromJson(entity.toString(), LoanAdjustmentData.class))
                .collectList();
    }

    @Override
    public Flux<LoanAdjustmentData> getAllAdjustmentCollectionDataByLoginId(String managementProcessId, String loginId, Integer limit, Integer offset) {
        return repository.findAllByManagementProcessIdAndCreatedBy(managementProcessId, loginId, limit, offset)
                .doOnNext(entity -> log.info("Collection Loan Adjustment data : {}", entity))
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Mono<Long> getCountLoanAdjustment(String managementProcessId, String loginId) {
        log.info("Getting Count of Collection Loan Adjustment data between date: {} {}", managementProcessId, loginId);
        return repository.getCountLoanDataEntity(managementProcessId, loginId)
                .doOnNext(count -> log.info("Count of Collection Loan Adjustment data : {}", count));
    }

    @Override
    public Flux<LoanAdjustmentData> getAllAdjustmentByManagementProcessIdAndLoanAdjustmentProcessId(String managementProcessId, String loanAdjustmentProcessId) {
        return repository.findAllByManagementProcessIdAndLoanAdjustmentProcessId(managementProcessId, loanAdjustmentProcessId)
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataByOid(String oid) {
        return repository.findByOid(oid)
                .doOnNext(entity -> log.info("Collection Loan Adjustment data by oid : {}", entity))
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataBySavingsAccountId(String savingsAccountId) {
        return repository.findFirstBySavingsAccountId(savingsAccountId)
                .doOnNext(entity -> log.info("Collection Loan Adjustment data by savings account id : {}", entity))
                .doOnError(error -> log.error("Error in getting Collection Loan Adjustment data by savings account id : {}", error.getMessage()))
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    private Mono<String> validateAndUpdateLoanAdjustmentDataForRejection(String samityId,
                        List<LoanAdjustmentDataEntity> loanAdjustmentDataEntityList, String loginId) {
                return this
                                .checkIfAllLoanAdjustmentDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
                                                loanAdjustmentDataEntityList)
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
                                .doOnNext(entityList -> log.info("Samity Loan Adjustment Data Updated For Rejection"))
                                .map(entityList -> samityId);
        }

    private Mono<List<LoanAdjustmentDataEntity>> checkIfAllLoanAdjustmentDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
                    List<LoanAdjustmentDataEntity> loanAdjustmentDataEntityList) {
            return Mono.just(loanAdjustmentDataEntityList)
                            .filter(entityList -> entityList.stream()
                                            .allMatch(loanAdjustmentDataEntity -> loanAdjustmentDataEntity
                                                            .getStatus()
                                                            .equals(Status.STATUS_SUBMITTED.getValue())
                                                            || loanAdjustmentDataEntity.getStatus().equals(
                                                                            Status.STATUS_UNAUTHORIZED.getValue())))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                            "Withdraw Data is not Submitted for Authorization Process")))
                            .filter(entityList -> entityList.stream()
                                            .allMatch(loanAdjustmentDataEntity -> loanAdjustmentDataEntity
                                                            .getIsLocked().equals("Yes")))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                            "Withdraw Data is not Locked for Authorization Process")))
                            .doOnNext(entityList -> log
                                            .info("Samity Withdraw Data Validated For Authorization Process"));
    }

    private Mono<String> validateAndUnlockSamity(List<LoanAdjustmentDataEntity> loanAdjustmentDataEntityList,
                    String samityId, String loginId) {
            return Mono.just(loanAdjustmentDataEntityList)
                            .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Adjustment Data is Not Submitted")))
                            .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("Yes")))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Adjustment Data is Not Locked")))
                            .map(entityList -> {
                                    entityList.forEach(entity -> {
                                            entity.setIsLocked("No");
                                            entity.setLockedBy(null);
                                            entity.setLockedOn(null);
                                    });
                                    return entityList;
                            })
                            .flatMapMany(repository::saveAll)
                            .collectList()
                            .doOnNext(list -> log.info("Samity {} Loan Adjustment Data are Unlocked for Authorization", samityId))
                            .map(list -> samityId);
    }

    private Mono<String> validateAndLockSamity(List<LoanAdjustmentDataEntity> loanAdjustmentDataEntityList, String samityId, String loginId) {
            return Mono.just(loanAdjustmentDataEntityList)
                    .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Adjustment Data is Not Submitted")))
                    .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Adjustment Data is Already Locked")))
                    .map(entityList -> {
                            entityList.forEach(entity -> {
                                    entity.setIsLocked("Yes");
                                    entity.setLockedBy(loginId);
                                    entity.setLockedOn(LocalDateTime.now());
                            });
                            return entityList;
                    })
                    .flatMapMany(repository::saveAll)
                    .collectList()
                    .doOnNext(list -> log.info("Samity {} Loan Adjustment Data are Locked for Authorization", samityId))
                    .map(list -> samityId);
    }



    @Override
    public Mono<LoanAdjustmentData> saveEditedData(LoanAdjustmentData loanAdjustmentData) {
        return Mono.just(loanAdjustmentData)
                .map(data -> mapper.map(data, LoanAdjustmentDataEntity.class))
                .flatMap(repository::save)
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Flux<LoanAdjustmentData> getLoanAdjustmentCollectionDataByOidList(List<String> oidList) {
        return repository.findAllByOidIn(oidList)
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class))
                .doOnNext(data -> log.info("Loan Adjustment Data by oid list : {}", data));
    }

    @Override
    public Flux<String> updateStatusOfLoanAdjustmentDataForSubmission(AdjustmentEntitySubmitRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        return repository.findAllByManagementProcessIdAndLoanAdjustmentProcessId(loanAdjustmentData.getManagementProcessId(), loanAdjustmentData.getLoanAdjustmentProcessId())
                .map(entity -> {
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setIsSubmitted("Yes");
                    entity.setSubmittedBy(requestDto.getLoginId());
                    entity.setSubmittedOn(LocalDateTime.now());
                    return entity;
                })
                .flatMap(repository::save)
                .map(entity -> "Loan Adjustment Data Updated to Submit For Authorization");
    }

    @Override
    public Flux<LoanAdjustmentData> getLoanAdjustmentDataByManagementProcessIdAndSamity(String managementProcessId, String samityId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataByLoanAccountId(String loanAccountId) {
        return repository.findFirstByLoanAccountId(loanAccountId)
                .doOnNext(entity -> log.info("Collection Loan Adjustment data by Loan account id : {}", entity))
                .doOnError(error -> log.error("Error in getting Collection Loan Adjustment data by loan account id : {}", error.getMessage()))
                .map(entity -> mapper.map(entity, LoanAdjustmentData.class));
    }

    @Override
    public Mono<String> deleteLoanAdjustmentByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return repository.deleteAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnRequest(data -> log.info("Loan Adjustment Data Requested for Deletion"))
                .doOnError(error -> log.error("Error in deleting Loan Adjustment Data by management process id and process id : {}", error.getMessage()))
                .then(Mono.just("Loan Adjustment Data Deleted Successfully"));
    }

    @Override
    public Mono<Long> getCountLoanAdjustmentByManagementProcessIdAndSamityId(String managementProcessId, String samityId) {
        return repository.countByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .doOnNext(count -> log.info("Count of Loan Adjustment Data by management process id and samity id : {}", count));
    }
}
