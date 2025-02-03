package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository.LoanWaiverRepository;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanAccountDetails;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
//@RequiredArgsConstructor
@Component
public class LoanWaiverPersistenceAdapter implements LoanWaiverPersistencePort {

        private final LoanWaiverRepository repository;
        private final ModelMapper modelMapper;
        private final TransactionalOperator rxtx;
        private final Gson gson;


    public LoanWaiverPersistenceAdapter(LoanWaiverRepository repository, ModelMapper modelMapper, TransactionalOperator rxtx) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
        public Flux<LoanWaiver> getLoanWaiverList() {
                return repository.findAll()
                        .map(loanWaiverEntity -> modelMapper.map(loanWaiverEntity, LoanWaiver.class))
                        .doOnError(throwable -> log.error("Exception encountered in getLoanWaiverList\nReason - {}", throwable.getMessage()))
                        .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while fetching loan waiver data"))
                        .doOnNext(dto -> log.info("after map loan waiver list : {}", dto));
        }

        @Override
        public Mono<LoanWaiver> getLoanWaiverById(String id) {
                return repository.findById(id)
                        .doOnNext(entity -> log.info("loan waiver entity by id : {} {}", entity, id))
                        .map(loanWaiverEntity -> {
                            LoanWaiver loanWaiver = modelMapper.map(loanWaiverEntity, LoanWaiver.class);
                            LoanAccountDetails loanAccountDetails = gson.fromJson(loanWaiverEntity.getLoanInfo(), LoanAccountDetails.class);
                            loanWaiver.setLoanInfo(loanAccountDetails);
                            return loanWaiver;
                        })
                        .doOnNext(dto -> log.info("after map loan waiver by id : {}", dto));
        }

        @Override
        public Mono<LoanWaiver> saveLoanWaiver(LoanWaiver loanWaiver) {
                return repository.save(modelMapper.map(loanWaiver, LoanWaiverEntity.class))
                        .map(loanWaiverEntity -> modelMapper.map(loanWaiverEntity, LoanWaiver.class))
                        .as(rxtx::transactional)
                        .doOnError(throwable -> log.error("Exception encountered in saveLoanWaiver\nReason - {}", throwable.getMessage()))
                        .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while saving loan waiver data"))
                        .doOnNext(dto -> log.info("after map loan waiver dto : {}", dto));
        }

        @Override
        public Mono<LoanWaiver> getLoanWaiverByLoanAccountId(String loanAccountId) {
                return repository.findFirstByLoanAccountId(loanAccountId)
                        .doOnRequest(value -> log.info("loan waiver entity by loan account id : {} {}", value, loanAccountId))
                        .switchIfEmpty(Mono.just(new LoanWaiverEntity()))
                        .doOnNext(entity -> log.info("loan waiver entity by loanAccountId : {} {}", entity, loanAccountId))
                        .map(loanWaiverEntity -> modelMapper.map(loanWaiverEntity, LoanWaiver.class))
                        .doOnError(throwable -> log.error("Exception encountered in getLoanWaiverByLoanAccountId\nReason - {}", throwable.getMessage()));
        }

    @Override
    public Flux<LoanWaiver> getLoanWaiverDataBySamity(String samityId) {
        return repository.findAllBySamityId(samityId)
                .map(entity -> modelMapper.map(entity.toString(), LoanWaiver.class));
    }

    @Override
    public Mono<List<LoanWaiverEntity>> getAllLoanWaiverDataByManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList()
                ;
    }

    @Override
    public Mono<String> deleteAllByManagementProcessId(String managementProcessId) {
        return repository
                .deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Loan waiver data deleted successfully"));
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndLockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No Loan Waiver Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<List<LoanWaiverDTO>> getAllLoanWaiverDataBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdIn(samityIdList)
                .map(entity -> modelMapper.map(entity, LoanWaiverDTO.class))
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateLoanWaiverDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .filter(entity -> entity.getSamityId().equals(samityId))
                .collectList()
                .doOnNext(entityList -> log.info("Samity Id: {}, Total Loan Waiver Data: {}",
                        samityId,
                        entityList.size()))
                .flatMap(entityList -> {
                    if (!entityList.isEmpty()) {
                        return this.validateAndUpdateLoanWaiverDataForRejection(samityId, entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return repository.getSamityIdListLockedByUserForAuthorization(loginId)
                .distinct(String::chars)
                .collectList();
    }


    private Mono<String> validateAndUpdateLoanWaiverDataForRejection(String samityId,
                                                                     List<LoanWaiverEntity> loanWaiverEntityList, String loginId) {
        return this.checkIfAllLoanWaiverDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
                        loanWaiverEntityList)
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
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Loan Waiver Data Updated For Rejection"))
                .map(entityList -> samityId);
    }

    private Mono<List<LoanWaiverEntity>> checkIfAllLoanWaiverDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(
            List<LoanWaiverEntity> loanWaiverEntityList) {
        return Mono.just(loanWaiverEntityList)
                .filter(entityList -> entityList.stream()
                        .allMatch(loanWaiverEntity -> loanWaiverEntity
                                .getStatus()
                                .equals(Status.STATUS_SUBMITTED.getValue())
                                || loanWaiverEntity.getStatus().equals(
                                Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Waiver Data is not Submitted for Authorization Process")))
                .filter(entityList -> entityList.stream()
                        .allMatch(loanWaiverEntity -> loanWaiverEntity
                                .getIsLocked().equals(Status.STATUS_YES.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Waiver Data is not Locked for Authorization Process")))
                .doOnNext(entityList -> log
                        .info("Waiver Data Validated For Authorization Process"));
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return repository.findAllBySamityIdIn(List.of(samityId))
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndUnlockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId).doOnNext(id -> log.info("No Loan Waiver Data for Samity: {}", id));
                });
    }

    private Mono<String> validateAndLockSamity(List<LoanWaiverEntity> loanWaiverEntityList, String samityId, String loginId) {
        return Mono.just(loanWaiverEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Waiver Data is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Loan Waiver Data is Already Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked(Status.STATUS_YES.getValue());
                        entity.setLockedBy(loginId);
                        entity.setLockedOn(LocalDateTime.now());
                        entity.setEditCommit(Status.STATUS_NO.getValue());
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan Waiver Data are Locked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUnlockSamity(List<LoanWaiverEntity> loanWaiverEntityList,
                                                 String samityId, String loginId) {
        return Mono.just(loanWaiverEntityList)
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
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Loan Waiver Data are Unlocked for Authorization", samityId))
                .map(list -> samityId);
    }

}
