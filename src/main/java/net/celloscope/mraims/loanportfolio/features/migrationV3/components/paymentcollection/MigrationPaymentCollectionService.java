package net.celloscope.mraims.loanportfolio.features.migrationV3.components.paymentcollection;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.MemberAttendanceUseCase;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceInfo;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceRequestDTO;
import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionDataVerifyDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.LoanProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct.LoanProduct;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct.MigrationLoanProductRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.celloscope.mraims.loanportfolio.core.util.Constants.STATUS_STAGED;
import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.REGULAR;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_DPS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_FDR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationPaymentCollectionService {
    private final CollectionStagingDataPersistencePort persistencePort;
    private final TransactionalOperator rxtx;
    private final IStagingDataUseCase stagingDataUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final MemberAttendanceUseCase memberAttendanceUseCase;
    private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
    private final CommonValidation commonValidation;
    private final Gson gson;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final CommonRepository commonRepository;
    private final MigrationLoanProductRepositoryV3 migrationLoanProductRepositoryV3;

    public Mono<CollectionMessageResponseDTO> collectPaymentBySamity(PaymentCollectionBySamityCommand command) {
//        log.info("Collection Data Command: {}", gson.toJson(command));
        AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
        final String processId = StringUtils.isNotBlank(command.getProcessId()) ? command.getProcessId() : UUID.randomUUID().toString();
        return this.validateIfCollectionIsPossibleForSamity(command.getOfficeId(), command.getSamityId(), command.getCollectionType())
                .doOnNext(managementProcessTrackerAtomicReference::set)
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfCollectionTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), command.getSamityId())
                        .map(string -> managementProcessTracker))
                .flatMap(managementProcessTracker -> this.validateIfCollectionDataIsValidForPayment(managementProcessTracker, command))
                .flatMap(managementProcessTracker -> this.validateIfLoanAccountIsValidForPayment(managementProcessTracker, command))
                .flatMap(managementProcessTracker -> this.validateDpsAmountForCollection(managementProcessTracker, command))
                .flatMap(managementProcessTracker -> this.validateFdrAmountForCollection(managementProcessTracker, command))
                .flatMap(managementProcessTracker -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(managementProcessTracker.getManagementProcessId(),
                                command.getSamityId(), SamityEvents.COLLECTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
//                                log.info("Samity Event Tracker is Empty");
                                return samityEventTrackerUseCase
                                        .insertSamityEvent(managementProcessTracker.getManagementProcessId(), processId,
                                                command.getOfficeId(), command.getSamityId(),
                                                SamityEvents.COLLECTED.getValue(), command.getLoginId())
                                        .map(newSamityEventTracker -> Tuples.of(managementProcessTracker,
                                                newSamityEventTracker.getSamityEventTrackerId()));
                            }
                            else if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) &&
                                    !HelperUtil.checkIfNullOrEmpty(command.getManagementProcessId()) &&
                                    !HelperUtil.checkIfNullOrEmpty(command.getProcessId()))
                            {
//                                log.info("Samity Event Tracker is Not Empty and process id is provided");
                                return Mono.just(Tuples.of(managementProcessTracker, processId));
                            }

                            else {
//                                log.info("Samity Event Tracker is Not Empty process id not provided");
                                return Mono.just(Tuples.of(managementProcessTracker, samityEventTracker.getSamityEventTrackerId()));
                            }
                        }))
                .map(tuple -> {
//                    log.info("Process Id to be build: {}", tuple.getT2());
                    return this.buildCollectionDataToSave(tuple.getT1(), command, tuple.getT2());
                })
                .flatMap(persistencePort::saveAllCollectionDataToDatabase)
                .flatMap(collectionStagingDataList -> {
                    if(command.getCollectionType().equals(REGULAR.getValue())){
                        return this.saveMemberAttendanceForSamity(command)
                                .map(memberAttendanceList -> collectionStagingDataList);
                    }
                    return Mono.just(collectionStagingDataList);
                })
                .flatMap(list -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(list.get(0).getManagementProcessId(), command.getSamityId(),
                                SamityEvents.COLLECTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
                                return samityEventTrackerUseCase.insertSamityEvent(list.get(0).getManagementProcessId(),
                                        list.get(0).getProcessId(), command.getOfficeId(), command.getSamityId(),
                                        SamityEvents.COLLECTED.getValue(), command.getLoginId());
                            }
                            return Mono.just(samityEventTracker);
                        }))
                .as(this.rxtx::transactional)
                .map(samityEventTracker -> CollectionMessageResponseDTO.builder()
                        .userMessage("Collection is completed for samity")
                        .build());
    }

    private Mono<ManagementProcessTracker> validateFdrAmountForCollection(ManagementProcessTracker managementProcessTracker, PaymentCollectionBySamityCommand command) {
        return Flux.fromIterable(command.getData())
                .filter(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getSavingsTypeId()) && collectionData.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue()))
                .filter(fdrAccountData -> fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .map(fdrAccountData -> {
                    log.info("FDR Account Data condition1: {}", !(fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) >= 0));
                    log.info("FDR Account Data condition2: {}", fdrAccountData.getAmount().compareTo(fdrAccountData.getTargetAmount()) != 0);
                    if (!(fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) > 0) ||fdrAccountData.getAmount().compareTo(fdrAccountData.getTargetAmount()) != 0){
                        return Tuples.of("Invalid", fdrAccountData.getSavingsAccountId());
                    }
                    return Tuples.of("Valid", fdrAccountData.getSavingsAccountId());
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .flatMap(msgMap -> {
                    if(msgMap.containsKey("Invalid")){
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate FDR amount for account: " + msgMap.get("Invalid")));
                    }
                    else
                        return Mono.just(managementProcessTracker);
                });
    }


    private Mono<ManagementProcessTracker> validateDpsAmountForCollection(ManagementProcessTracker managementProcessTracker, PaymentCollectionBySamityCommand command) {
        return Flux.fromIterable(command.getData())
                .filter(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getSavingsTypeId()) && collectionData.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue()))
                .filter(dpsAccountData -> dpsAccountData.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .map(dpsAccountData -> {
                    if (!(dpsAccountData.getAmount().compareTo(BigDecimal.ZERO) > 0) ||dpsAccountData.getAmount().remainder(dpsAccountData.getTargetAmount()).compareTo(BigDecimal.ZERO) != 0 || dpsAccountData.getAmount().compareTo(dpsAccountData.getTargetAmount().multiply(BigDecimal.valueOf(dpsAccountData.getDpsPendingInstallmentNo()))) > 0){
                        return Tuples.of("Invalid", dpsAccountData.getSavingsAccountId());
                    }
                    return Tuples.of("Valid", dpsAccountData.getSavingsAccountId());
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .flatMap(msgMap -> {
                    if(msgMap.containsKey("Invalid")){
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate DPS amount for account: " + msgMap.get("Invalid")));
                    }
                    else
                        return Mono.just(managementProcessTracker);
                });
    }



    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsStartedForOffice(ManagementProcessTracker managementProcessTracker) {
        return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office")))
                .map(dayEndProcessTrackers -> managementProcessTracker);
    }

    private Mono<String> validateIfCollectionTransactionIsStillAvailableForSamity(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
//				.filter(samityEvent -> !samityEvent.equals(SamityEvents.CANCELED.getValue()))
                .collectList()
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity is Already Canceled and Cannot Create Transaction")))
                .flatMap(samityEventList -> persistencePort.getAllCollectionDataBySamity(samityId)
                        .filter(collectionStagingData -> !HelperUtil.checkIfNullOrEmpty(collectionStagingData.getStatus()))
                        .collectList())
                .filter(collectionStagingDataList -> collectionStagingDataList.isEmpty() || collectionStagingDataList.stream().noneMatch(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Collection Data is Already Authorized or Unauthorized")))
                .filter(collectionStagingDataList -> collectionStagingDataList.isEmpty() || collectionStagingDataList.stream().allMatch(data -> data.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Collection Transaction is Already Locked for Authorization for Samity")))
                .map(loanAdjustmentDataList -> managementProcessId);
    }

    private Mono<List<MemberAttendance>> saveMemberAttendanceForSamity(PaymentCollectionBySamityCommand command) {
        List<MemberAttendanceInfo> memberAttendanceInfoList = command.getAttendanceList().stream()
                .distinct()
                .map(attendance -> MemberAttendanceInfo.builder()
                        .memberId(attendance.getMemberId())
                        .status(attendance.getStatus())
                        .build())
                .toList();
        log.info("Member Attendance List size: {}", memberAttendanceInfoList.size());
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
                .map(managementProcessTracker -> MemberAttendanceRequestDTO.builder()
                        .mfiId(managementProcessTracker.getMfiId())
                        .samityId(command.getSamityId())
                        .attendanceDate(managementProcessTracker.getBusinessDate())
                        .loginId(command.getLoginId())
                        .data(memberAttendanceInfoList)
                        .build())
                .flatMap(memberAttendanceUseCase::saveMemberAttendanceListForSamity);
    }




    private List<CollectionStagingData> buildCollectionDataToSave(ManagementProcessTracker managementProcessTracker,
                                                                  PaymentCollectionBySamityCommand command, String processId) {
        return command.getData().stream().map(collectionData -> CollectionStagingData.builder()
                .collectionStagingDataId(UUID.randomUUID().toString())
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .stagingDataId(collectionData.getStagingDataId())
                .processId(processId)
                .samityId(command.getSamityId())
                .accountType(collectionData.getAccountType())
                .loanAccountId(collectionData.getLoanAccountId())
                .savingsAccountId(collectionData.getSavingsAccountId())
                .collectionType(command.getCollectionType())
                .amount(collectionData.getAmount())
                .paymentMode(collectionData.getPaymentMode())
                .status(Status.STATUS_STAGED.getValue())
                .createdBy(command.getLoginId())
                .createdOn(LocalDateTime.now())
                .isNew(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? "No" : "Yes")
                .currentVersion(collectionData.getCurrentVersion() == null ? "1" : collectionData.getCurrentVersion().toString())
                .editCommit("No")
                .isLocked("No")
                .isSubmitted("No")
                .updatedBy(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? command.getLoginId() : null)
                .updatedOn(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? LocalDateTime.now() : null)
                .build()).toList();
    }

    private Mono<ManagementProcessTracker> validateIfCollectionIsPossibleForSamity(String officeId, String samityId, String collectionType) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .doOnNext(managementProcessTrackerAtomicReference::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase
                        .getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
                        .collectList())
                .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                        .anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                        .noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
                .flatMap(officeEventTrackerList -> stagingDataUseCase
                        .getStagingProcessEntityForSamity(managementProcessTrackerAtomicReference.get().getManagementProcessId(), samityId)
                        .doOnRequest(l -> log.info("Staging Data Requested For Samity: {}, {}", managementProcessTrackerAtomicReference.get().getManagementProcessId(), samityId))
                        .doOnNext(trackerEntity -> log.info("Tracker Entity: {}", trackerEntity))
                        .filter(trackerEntity -> !HelperUtil.checkIfNullOrEmpty(trackerEntity.getStatus()) && (trackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()) || trackerEntity.getStatus().equals(Status.STATUS_REGENERATED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Valid For Samity"))))
                .flatMap(trackerEntity -> this.validateIfSamityIsRegularOrSpecialForCollection(managementProcessTrackerAtomicReference.get(), samityId, trackerEntity.getSamityDay(), collectionType));
    }

    private Mono<ManagementProcessTracker> validateIfSamityIsRegularOrSpecialForCollection(
            ManagementProcessTracker managementProcessTracker, String samityId, String samityDay,
            String collectionType) {
        return Mono.just(managementProcessTracker)
                .filter(tracker -> !HelperUtil.checkIfNullOrEmpty(collectionType)
                        && (collectionType.equals(REGULAR.getValue())
                        || collectionType.equals(CollectionType.SPECIAL.getValue())
                        || collectionType.equals(CollectionType.REBATE.getValue())
                        || collectionType.equals(CollectionType.WAIVER.getValue())
                        || collectionType.equals(CollectionType.WRITE_OFF.getValue()))
                        || collectionType.equals(CollectionType.SINGLE.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Collection Type Must be 'Regular', 'Special', 'Rebate' or 'Waiver' or 'Single' For Samity")))
                /*.filter(tracker -> (collectionType.equals(CollectionType.REGULAR.getValue())
                        && samityDay.equals(managementProcessTracker.getBusinessDay()))
                        || (collectionType.equals(CollectionType.SPECIAL.getValue())
                                && !samityDay.equals(managementProcessTracker.getBusinessDay()))
                        || (collectionType.equals(CollectionType.REBATE.getValue())
                        && !samityDay.equals(managementProcessTracker.getBusinessDay()))
                        || (collectionType.equals(CollectionType.WAIVER.getValue())
                        && !samityDay.equals(managementProcessTracker.getBusinessDay())))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Type Mismatch For Samity")))*/
                .flatMap(tracker -> {
                    if (collectionType.equals(REGULAR.getValue())) {
                        return samityEventTrackerUseCase
                                .getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(),
                                        samityId)
                                .collectList()
                                .filter(samityEventTrackerList -> samityEventTrackerList.isEmpty()
                                        || samityEventTrackerList.stream()
                                        .noneMatch(samityEventTracker -> !HelperUtil
                                                .checkIfNullOrEmpty(samityEventTracker.getSamityEvent())
                                                && samityEventTracker.getSamityEvent()
                                                .equals(SamityEvents.COLLECTED.getValue())))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                        "Collection Is Already Completed For This Regular Samity")))
                                .map(samityEventTrackerList -> managementProcessTracker);
                    }
                    return Mono.just(managementProcessTracker);
                });
    }

    private Mono<ManagementProcessTracker> validateIfCollectionDataIsValidForPayment(
            ManagementProcessTracker managementProcessTracker,
            PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
        return Mono.just(paymentCollectionBySamityCommand)
                .filter(command -> command.getData() != null && !command.getData().isEmpty())
                .switchIfEmpty(
                        Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data is Provided")))
                .filter(command -> command.getData().stream()
                        .noneMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getStagingDataId())))
                .switchIfEmpty(Mono.error(
                        new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Id Field Cannot Be Empty")))
                .filter(command -> command.getData().stream()
                        .noneMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getPaymentMode())))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Payment Mode Field Cannot Be Empty")))
                .filter(command -> command.getData().stream()
                        .allMatch(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getAccountType())
                                && (collectionData.getAccountType().equals(Constants.ACCOUNT_TYPE_LOAN.getValue())
                                || collectionData.getAccountType().equals(Constants.ACCOUNT_TYPE_SAVINGS.getValue()))))
                .switchIfEmpty(Mono.error(
                        new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Account Type Must be 'Loan' or 'Savings'")))
                .filter(command -> command.getData().stream()
                        .allMatch(item -> item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) >= 0))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Collection Amount Cannot be Empty or Negative for : " + paymentCollectionBySamityCommand.getData())))
                .flatMap(command -> this.validateIfStagingDataIdAndAccountIdListIsValidForSamity(
                        managementProcessTracker.getManagementProcessId(), command))
                .flatMap(map -> this.validateIfCollectionAmountExceedsTotalOutstandingAmount(managementProcessTracker.getManagementProcessId(), paymentCollectionBySamityCommand))
                .map(data -> managementProcessTracker);
    }

    private Mono<List<Boolean>> validateIfCollectionAmountExceedsTotalOutstandingAmount(String managementProcessId, PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
        return Flux.fromIterable(paymentCollectionBySamityCommand.getData())
                .flatMap(collectionData -> {
                    if (collectionData.getLoanAccountId() != null && !collectionData.getLoanAccountId().isEmpty()) {
                        return this.validateCollectionAmountForLoanAccounts(managementProcessId, collectionData);
                    }
                    return Mono.just(false);
                })
                .collectList()
                .doOnError(throwable -> log.error("Error validating collection amount: {}", throwable.getMessage()));
    }

    private Mono<Boolean> validateCollectionAmountForLoanAccounts(String managementProcessId, CollectionData collectionData) {
        return this.getStagingAccountDataByLoanAccountId(managementProcessId, collectionData.getLoanAccountId())
                .flatMap(stagingAccountData -> validateCollectionAmount(stagingAccountData, collectionData))
                .doOnError(throwable -> log.error("Error validating collection amount: {}", throwable.getMessage()));
    }



    private Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String managementProcessId, String loanAccountId) {
        return collectionStagingDataQueryUseCase.getStagingAccountDataByLoanAccountId(loanAccountId, managementProcessId);
    }



    private Mono<Boolean> validateCollectionAmount(StagingAccountData stagingAccountData, CollectionData collectionData) {
        BigDecimal collectionAmount = collectionData.getAmount();
        BigDecimal totalOutstandingAmount = stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining());
        if (collectionAmount.compareTo(totalOutstandingAmount) > 0) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Amount Exceeds Total Outstanding Amount for Loan Account : " + collectionData.getLoanAccountId()));
        } else {
            return Mono.just(true);
        }
    }

    private Mono<ManagementProcessTracker> validateIfLoanAccountIsValidForPayment(
            ManagementProcessTracker managementProcessTracker,
            PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
        if (!HelperUtil.checkIfNullOrEmpty(paymentCollectionBySamityCommand.getCollectionType()) &&
                (!paymentCollectionBySamityCommand.getCollectionType().equals(REGULAR.getValue()) ||
                        !paymentCollectionBySamityCommand.getCollectionType().equals(CollectionType.SPECIAL.getValue()))) {
            return Mono.just(managementProcessTracker);
        }
        return Flux.fromIterable(paymentCollectionBySamityCommand.getData())
                .map(CollectionData::getLoanAccountId)
                .flatMap(commonValidation::checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId)
                .doOnNext(response -> log.info("Loan Account Validation Response in payment collection: {}", response))
                .any(Boolean.FALSE::equals)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection/Adjustment data already found for this loan account"));
                    } else {
                        return Mono.just(managementProcessTracker);
                    }
                });
    }

    private Mono<Map<String, String>> validateIfStagingDataIdAndAccountIdListIsValidForSamity(
            String managementProcessId, PaymentCollectionBySamityCommand command) {
        return stagingDataUseCase.getAllStagingDataBySamity(managementProcessId, command.getSamityId())
                .collectList()
                .flatMap(stagingDataList -> {
                    List<String> memberIdList = stagingDataList.stream().map(StagingData::getMemberId).toList();
                    return stagingDataUseCase.getAllStagingAccountDataByMemberIdList(managementProcessId, memberIdList).collectList()
                            .zipWith(/*commonRepository
                                    .getAllRegularLoanProducts()*/
                                    migrationLoanProductRepositoryV3
                                        .findAll()
                                        .switchIfEmpty(Mono.just(LoanProduct.builder().loanProductId("").build()))
                                        .collectList())
                            .map(stagingAccountDataAndLoanProductTuple -> {
                                List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> stagingAccountDataList = stagingAccountDataAndLoanProductTuple.getT1();
                                List<String> regularLoanProductIds = stagingAccountDataAndLoanProductTuple.getT2().stream().map(LoanProduct::getLoanProductId).toList();
                                log.info("Regular Loan Product Ids: {}", regularLoanProductIds);

                                List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> savingsAccountList =
                                        new ArrayList<>(stagingAccountDataList.stream().filter(stagingAccountData -> stagingAccountData.getSavingsAccountId() != null).toList());
                                List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> loanAccountList = stagingAccountDataList.stream().filter(stagingAccountData -> stagingAccountData.getLoanAccountId() != null).toList();
                                loanAccountList = loanAccountList.stream().filter(stagingAccountData -> regularLoanProductIds.contains(stagingAccountData.getProductCode())).toList();

                                savingsAccountList.addAll(loanAccountList);
                                return savingsAccountList;
                            })
                            .map(stagingAccountDataList -> Tuples.of(stagingDataList, stagingAccountDataList));
                })
                .doOnNext(tuple2 -> log.info("Staging Account Data List size: {} | command.getData size : {}", tuple2.getT2().size(), command.getData().size()))
                .filter(tuple -> (command.getCollectionType().equals(REGULAR.getValue()) && tuple.getT2().size() == command.getData().size())
                        || command.getCollectionType().equals(CollectionType.SPECIAL.getValue())
                        || command.getCollectionType().equals(CollectionType.REBATE.getValue())
                        || command.getCollectionType().equals(CollectionType.WAIVER.getValue())
                        || command.getCollectionType().equals(CollectionType.WRITE_OFF.getValue())
                        || command.getCollectionType().equals(CollectionType.SINGLE.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Not Enough Collection Data is Provided for this regular Samity")))
                .map(tuple -> {
                    Map<String, String> accountIdWithStagingDataIdMap = new HashMap<>();
                    tuple.getT1().forEach(stagingData -> tuple.getT2().forEach(stagingAccountData -> {
                        if (stagingData.getMemberId().equals(stagingAccountData.getMemberId())) {
                            if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
                                accountIdWithStagingDataIdMap.put(stagingAccountData.getLoanAccountId(),
                                        stagingData.getStagingDataId());
                            } else if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                                accountIdWithStagingDataIdMap.put(stagingAccountData.getSavingsAccountId(),
                                        stagingData.getStagingDataId());
                            }
                        }
                    }));
                    return accountIdWithStagingDataIdMap;
                })
                .doOnNext(accountIdWithStagingDataIdMap -> log.info("AccountId with StagingDataId Map For Samity: {}",
                        accountIdWithStagingDataIdMap))
                .filter(accountIdWithStagingDataIdMap -> command.getData().stream()
                        .allMatch(collectionData -> (collectionData.getAccountType().equals("Loan")
                                && collectionData.getStagingDataId()
                                .equals(accountIdWithStagingDataIdMap.get(collectionData.getLoanAccountId())))
                                || (collectionData.getAccountType().equals("Savings")
                                && collectionData.getStagingDataId()
                                .equals(accountIdWithStagingDataIdMap
                                        .get(collectionData.getSavingsAccountId())))))
                .switchIfEmpty(Mono.error(
                        new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Id Mismatch with AccountId")));
    }



    private Mono<CollectionDataVerifyDTO> getCollectionDataToVerifyPayment(PaymentCollectionBySamityCommand command) {
        List<String> stagingDataIdForLoanList = new ArrayList<>();
        List<String> loanAccountIdList = new ArrayList<>();
        List<String> stagingDataIdForSavingsList = new ArrayList<>();
        List<String> savingsAccountIdList = new ArrayList<>();

        command.getData().forEach(data -> {
            if (data.getStagingDataId() != null && !data.getStagingDataId().isEmpty()
                    && data.getAccountType().equals("Loan")) {
                stagingDataIdForLoanList.add(data.getStagingDataId());
                if (data.getLoanAccountId() != null && !data.getLoanAccountId().isEmpty()) {
                    loanAccountIdList.add(data.getLoanAccountId());
                }
            }
            if (data.getStagingDataId() != null && !data.getStagingDataId().isEmpty()
                    && data.getAccountType().equals("Savings")) {
                stagingDataIdForSavingsList.add(data.getStagingDataId());
                if (data.getSavingsAccountId() != null && !data.getSavingsAccountId().isEmpty()) {
                    savingsAccountIdList.add(data.getSavingsAccountId());
                }
            }
        });

        CollectionDataVerifyDTO verifyDTO = CollectionDataVerifyDTO.builder()
                .officeId(command.getOfficeId())
                .samityId(command.getSamityId())
                .stagingDataIdForLoanList(stagingDataIdForLoanList)
                .loanAccountIdList(loanAccountIdList)
                .stagingDataIdForSavingsList(stagingDataIdForSavingsList)
                .savingsAccountIdList(savingsAccountIdList)
                .totalCount(0)
                .build();

        // log.info("CollectionDataVerifyDTO Build: {}", verifyDTO);
        return persistencePort.getCollectionDataToVerifyPayment(verifyDTO)
                // .map(dto -> this.checkForRegularOrSpecialCollectionVerification(command,
                // dto))
                .flatMap(dto -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(dto.getOfficeId())
                        .map(managementProcessTracker -> Tuples.of(dto, managementProcessTracker)))
                .doOnNext(tuple -> log.info("CollectionDataVerifyDTO: {}\n managementProcessTracker: {}", tuple.getT1(),
                        tuple.getT2()))
                .filter(tuple -> tuple.getT1().getTotalCount() > 0 || tuple.getT1().getSamityDay()
                        .equalsIgnoreCase(tuple.getT2().getBusinessDay()))
                .switchIfEmpty(Mono.error(
                        new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_SAMITY
                                        .getValue()
                                        .concat(" Samity ID: " + verifyDTO
                                                .getSamityId()))))
                .map(Tuple2::getT1)
                .filter(dto -> dto.getTotalCount() == command.getData().size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_STAGING_DATA_ID_AND_ACCOUNT_ID
                                .getValue()
                                .concat(" Samity ID: " + verifyDTO.getSamityId()))));
//				.filter(dto -> command.getData().stream()
//						.allMatch(data -> data.getCollectionType()
//								.equals(dto.getCollectionType())))
//				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
//						ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_COLLECTION_TYPE
//								.getValue()
//								.concat(" Samity ID: " + verifyDTO.getSamityId()))));
    }

}
