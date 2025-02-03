package net.celloscope.mraims.loanportfolio.features.rebate.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.StatusYesNo;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionDetailView;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberAndLoanAccountEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedLoanData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.LoanRebateUseCase;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebateDataEditHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebateHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanInfo;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.RebatePaymentInfo;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RebateInfoResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.REBATE;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Slf4j
public class LoanRebateService implements LoanRebateUseCase {

    private final PassbookUseCase passbookUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final IStagingDataUseCase iStagingDataUseCase;
    private final CommonRepository commonRepository;
    private final ISavingsAccountUseCase iSavingsAccountUseCase;
    private final LoanRebatePersistencePort loanRebatePersistencePort;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final ModelMapper modelMapper;
    private final TransactionalOperator rxtx;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final LoanAccountUseCase loanAccountUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final LoanRebateHistoryPersistencePort loanRebateHistoryPersistencePort;
    private final Gson gson;
    private final CommonValidation commonValidation;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;
    private final TransactionUseCase transactionUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final ISmsNotificationUseCase smsNotificationUseCase;
    private final LoanRebateDataEditHistoryPersistencePort loanRebateDataEditHistoryPersistencePort;
    private final RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final LoanAdjustmentPersistencePort loanAdjustmentPersistencePort;
    private final IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort;

    public LoanRebateService(PassbookUseCase passbookUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, IStagingDataUseCase iStagingDataUseCase, CommonRepository commonRepository, ISavingsAccountUseCase iSavingsAccountUseCase, LoanRebatePersistencePort loanRebatePersistencePort, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, LoanAdjustmentUseCase loanAdjustmentUseCase, PaymentCollectionUseCase paymentCollectionUseCase, ModelMapper modelMapper, TransactionalOperator rxtx, CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase, LoanAccountUseCase loanAccountUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, LoanRebateHistoryPersistencePort loanRebateHistoryPersistencePort, CommonValidation commonValidation, ServiceChargeChartUseCase serviceChargeChartUseCase, TransactionUseCase transactionUseCase, IStagingDataUseCase stagingDataUseCase, ISmsNotificationUseCase smsNotificationUseCase, LoanRebateDataEditHistoryPersistencePort loanRebateDataEditHistoryPersistencePort, RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort, CollectionStagingDataPersistencePort collectionStagingDataPersistencePort, LoanAdjustmentPersistencePort loanAdjustmentPersistencePort, IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort) {
        this.passbookUseCase = passbookUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.iStagingDataUseCase = iStagingDataUseCase;
        this.commonRepository = commonRepository;
        this.iSavingsAccountUseCase = iSavingsAccountUseCase;
        this.loanRebatePersistencePort = loanRebatePersistencePort;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.paymentCollectionUseCase = paymentCollectionUseCase;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.collectionStagingDataQueryUseCase = collectionStagingDataQueryUseCase;
        this.loanAccountUseCase = loanAccountUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.loanRebateHistoryPersistencePort = loanRebateHistoryPersistencePort;
        this.commonValidation = commonValidation;
        this.serviceChargeChartUseCase = serviceChargeChartUseCase;
        this.transactionUseCase = transactionUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.smsNotificationUseCase = smsNotificationUseCase;
        this.loanRebateDataEditHistoryPersistencePort = loanRebateDataEditHistoryPersistencePort;
        this.repaymentScheduleHistoryPersistencePort = repaymentScheduleHistoryPersistencePort;
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.loanAdjustmentPersistencePort = loanAdjustmentPersistencePort;
        this.withdrawStagingDataPersistencePort = withdrawStagingDataPersistencePort;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<LoanRebateResponseDTO> getLoanRebateInfoByLoanAccountId(ServerRequest serverRequest) {
        /*
         * get Total Loan Payable details (RebateInfoResponseDTO) from Repayment Schedule Table
         * get last passbook entry from passbook service
         * get principal paid till date & service charge paid till date from last passbook entry
         * calculate outstanding loan details using RebateInfoResponseDTO & last passbook entry
         * calculate rebate-able amount
         * build LoanRebateResponseDTO & return
         * */

        String loanAccountId = serverRequest.queryParam("loanAccountId").orElse("");

        return loanRepaymentScheduleUseCase
                .getRebateInfoByLoanAccountId(loanAccountId)
                .doOnNext(rebateInfoResponseDTO -> log.info("RebateInfoResponseDTO received : {}", rebateInfoResponseDTO))
                .doOnError(throwable -> log.error("Error happened while fetching RebateInfoResponseDTO : {}", throwable.getMessage()))
                .zipWith(passbookUseCase
                        .getLastPassbookEntry(loanAccountId)
                        .doOnNext(passbook -> log.info("last passbook entry received : {}", passbook))
                        .doOnError(throwable -> log.error("Error happened while fetching last passbook entry : {}", throwable.getMessage())))
                .map(this::calculateLoanRebate)
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    @Override
    public Mono<GetDetailsByMemberResponseDto> collectAccountDetailsByMemberId(GetDetailsByMemberRequestDto requestDto) {
        return iStagingDataUseCase.getStagingAccountDataListByMemberId(requestDto.getMemberId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, ExceptionMessages.NO_STAGING_DATA_FOUND_WITH_MEMBER_ID.getValue() + requestDto.getMemberId())))
                .doOnError(throwable -> log.error("Error happened while fetching staging account data list for memberId: {}", throwable.getMessage()))
                .doOnRequest(req -> log.info("Request sent for collecting staging account data list for memberId: {}", requestDto))
                .doOnComplete(() -> log.info("Staging account data collected successfully for memberId: {}", requestDto.getMemberId()))
                .collectList()
                .flatMap(stagingAccountDataList -> commonRepository.getMemberOfficeAndSamityEntityByMemberId(requestDto.getMemberId())
                        .doOnSuccess(memberEntity -> log.info("MemberEntity received for memberId: {}", memberEntity))
                        .flatMap(memberAndOfficeAndSamityEntity -> getLoanAccountDetailsForRebate(stagingAccountDataList, memberAndOfficeAndSamityEntity.getOfficeId()).zipWith(getSavingsAccountDetailsForRebate(stagingAccountDataList))
                                .map(tupleOfLoanAccountListAndSavingsAccountList -> Tuples.of(memberAndOfficeAndSamityEntity, tupleOfLoanAccountListAndSavingsAccountList.getT1(), tupleOfLoanAccountListAndSavingsAccountList.getT2()))
                        .map(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList -> GetDetailsByMemberResponseDto.builder()
                                .memberId(requestDto.getMemberId())
                                .memberNameBn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getMemberNameBn())
                                .memberNameEn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getMemberNameEn())
                                .officeId(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getOfficeId())
                                .officeNameEn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getOfficeNameEn())
                                .officeNameBn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getOfficeNameBn())
                                .samityId(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getSamityId())
                                .samityNameEn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getSamityNameEn())
                                .samityNameBn(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT1().getSamityNameBn())
                                .loanAccountList(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT2())
                                .savingsAccountList(tupleOfMemberOfficeAndSamityEntityLoanAccountListAndSavingsAccountList.getT3())
                                .build())))
                .doOnRequest(req -> log.info("Request sent for collecting account details for memberId: {}", requestDto))
                .doOnSuccess(res -> log.info("Response received for collecting account details for memberId: {}", res))
                .doOnError(throwable -> log.error("Error happened while fetching account details for memberId: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    private Mono<LocalDate> getCurrentBusinessDateByOfficeId(String officeId) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .map(ManagementProcessTracker::getBusinessDate)
                .doOnError(throwable -> log.error("Error happened while fetching business date for officeId: {}", throwable.getMessage()));
    }

    @Override
    public Mono<SettleRebateResponseDto> settleRebate(SettleRebateRequestDto requestDto) {
        final String processId = UUID.randomUUID().toString();
        log.info("process id after generated: {}", processId);
        return this.getRebateableAmountByLoanAccountId(requestDto.getLoanAccountId(), requestDto.getOfficeId())
                .doOnError(throwable -> log.error("Error happened while fetching rebate amount for loanAccountId: {}", throwable.getMessage()))
                .doOnRequest(req -> log.info("Request sent for fetching rebate amount for loanAccountId: {}", requestDto.getLoanAccountId()))
                .doOnSuccess(res -> log.info("Response received for fetching rebate amount for loanAccountId: {}", res))
                .flatMap(rebateAmount -> validateRebatedAmount(rebateAmount, requestDto))
                .flatMap(validatedRebatedAmount -> iStagingDataUseCase.getStagingAccountDataByLoanAccountId(requestDto.getLoanAccountId()).zipWith(Mono.just(validatedRebatedAmount)))
                .flatMap(stagingLoanAccountDataAndRebateAmount -> validatePayableAmount(requestDto, stagingLoanAccountDataAndRebateAmount.getT1().getTotalPrincipalRemaining().add(stagingLoanAccountDataAndRebateAmount.getT1().getTotalServiceChargeRemaining()).subtract(stagingLoanAccountDataAndRebateAmount.getT2())).zipWith(Mono.just(stagingLoanAccountDataAndRebateAmount.getT1())))
                .flatMap(totalPayableAmountAndStagingAccountData -> Mono.zip(managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId()), commonRepository.getMemberInfoByLoanAccountId(requestDto.getLoanAccountId()), Mono.just(totalPayableAmountAndStagingAccountData.getT1()), Mono.just(totalPayableAmountAndStagingAccountData.getT2())))
                .flatMap(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData -> iStagingDataUseCase.getStagingDataByMemberId(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT2().getMemberId())
                        .map(stagingData -> buildLoanRebateDomain(requestDto, stagingData, managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData, processId)))
                .flatMap(loanRebate -> validateAgainstSamityAndOfficeEventTracker(loanRebate, requestDto)
                        .switchIfEmpty(Mono.just(loanRebate)))
                .flatMap(loanRebate -> commonValidation.checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId(loanRebate.getLoanAccountId())
                        .flatMap(validationResult -> {
                            if (validationResult) {
                                return Mono.just(loanRebate);
                            }
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data or adjustment data already found for this loan account"));
                        }))
                .flatMap(loanRebate -> loanRebatePersistencePort.saveLoanRebate(addPaymentInfoIntoLoanRebate(loanRebate, requestDto))
                        .then(collectAndAdjustSettlementAmount(requestDto, loanRebate, processId))
                        .thenReturn(loanRebate))
                .as(rxtx::transactional)
                .map(loanRebate -> SettleRebateResponseDto.builder()
                        .userMessage("Early Settlement Is Successfully Created")
                        .build())
                .doOnError(throwable -> log.error("Error happened while settling rebate for loanAccountId: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    private Mono<Void> collectAndAdjustSettlementAmount(SettleRebateRequestDto requestDto, LoanRebate loanRebate, String processId) {
        log.info("process id for adjustment: {}", processId);
        if (requestDto.getCollectionType().equalsIgnoreCase("Combine")) {
            return loanAdjustmentUseCase.createLoanAdjustmentForRebate(buildLoanAdjustmentRequestDto(requestDto, loanRebate, processId))
                    .then(paymentCollectionUseCase.collectRebatePayment(buildPaymentCollectionRequestDto(requestDto, loanRebate, processId)))
                    .then();
        } else if (requestDto.getCollectionType().equalsIgnoreCase("Adjustment")) {
            return loanAdjustmentUseCase.createLoanAdjustmentForRebate(buildLoanAdjustmentRequestDto(requestDto, loanRebate, processId)).then();
        } else {
            return paymentCollectionUseCase.collectRebatePayment(buildPaymentCollectionRequestDto(requestDto, loanRebate, processId)).then();
        }
    }

    private PaymentCollectionBySamityCommand buildPaymentCollectionRequestDto(SettleRebateRequestDto requestDto, LoanRebate loanRebate, String processId) {
        return PaymentCollectionBySamityCommand.builder()
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(requestDto.getSamityId())
                .collectionType(REBATE.getValue())
                .processId(processId)
                .managementProcessId(loanRebate.getManagementProcessId())
                .data(Arrays.asList(CollectionData.builder()
                        .stagingDataId(loanRebate.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(requestDto.getLoanAccountId())
                        .amount(BigDecimal.valueOf(Double.parseDouble(requestDto.getCollectedAmountByCash())))
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .currentVersion(loanRebate.getCurrentVersion())
                        .build()))
                .build();
    }

    private LoanAdjustmentRequestDTO buildLoanAdjustmentRequestDto(SettleRebateRequestDto requestDto, LoanRebate loanRebate, String processId) {
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(loanRebate.getManagementProcessId())
                .processId(processId)
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(requestDto.getSamityId())
                .memberId(requestDto.getMemberId())
                .adjustmentType(CollectionType.REBATE.getValue())
                .currentVersion(loanRebate.getCurrentVersion())
                .data(Arrays.asList(AdjustedLoanData.builder()
                        .loanAccountId(requestDto.getLoanAccountId())
                        .adjustedAccountList(requestDto.getAdjustedAccountList().stream()
                                .map(src -> modelMapper.map(src, AdjustedAccount.class)).toList()).build()))
                .build();

    }

    private LoanRebate buildLoanRebateDomain(SettleRebateRequestDto requestDto, StagingData stagingData, Tuple4<ManagementProcessTracker, MemberEntity, BigDecimal, StagingAccountData> managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData, String processId) {
        log.info("process id for rebate data: {}", processId);
        return LoanRebate.builder()
                .loanRebateDataId(UUID.randomUUID().toString())
                .managementProcessId(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT1().getManagementProcessId())
                .memberId(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT2().getMemberId())
                .processId(processId)
                .stagingDataId(stagingData.getStagingDataId())
                .samityId(requestDto.getSamityId())
                .loanAccountId(requestDto.getLoanAccountId())
                .rebateAmount(BigDecimal.valueOf(Double.parseDouble(requestDto.getRebatedAmount())))
                .paymentMode(requestDto.getCollectionType())
                .createdBy(requestDto.getLoginId())
                .createdOn(LocalDateTime.now())
                .isNew(StatusYesNo.Yes.toString())
                .currentVersion(1)
                .status(Status.STATUS_STAGED.getValue())
                .isLocked(StatusYesNo.No.toString())
                .editCommit(Status.STATUS_YES.getValue())
                .earlySettlementDate(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT1().getBusinessDate())
                .payableAmount(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT3())
                .loanInfo(LoanInfo.builder()
                        .loanAmount(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getLoanAmount())
                        .serviceCharge(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getServiceCharge())
                        .totalLoanAmount(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getLoanAmount().add(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getServiceCharge()))
                        .principalPaid(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalPrincipalPaid())
                        .serviceChargePaid(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalServiceChargePaid())
                        .totalPaid(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalPrincipalPaid().add(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalServiceChargePaid()))
                        .principalRemaining(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalPrincipalRemaining())
                        .serviceChargeRemaining(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalServiceChargeRemaining())
                        .totalDue(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalPrincipalRemaining().add(managementProcessTrackerAndMemberAndTotalPayableAmountAndStagingAccountData.getT4().getTotalServiceChargeRemaining()))
                        .build())
                .build();
    }

    @Override
    public Mono<LoanRebateGridViewByOfficeResponseDto> getLoanRebateGridViewByOfficeId(LoanRebateGridViewByOfficeRequestDto requestDto) {
        return loanRebatePersistencePort.getLoanRebateDataByOfficeId(requestDto.getOfficeId(), requestDto.getStartDate(), requestDto.getEndDate())
                .flatMap(loanRebateData -> commonRepository.getMemberAndLoanAccountByLoanAccountId(loanRebateData.getLoanAccountId()).zipWith(Mono.just(loanRebateData)))
                .flatMap(memberAndLoanAccountAndLoanRebate ->
                        Mono.zip(
                                        Mono.just(memberAndLoanAccountAndLoanRebate.getT1()),
                                        Mono.just(memberAndLoanAccountAndLoanRebate.getT2()),
                                        commonRepository.getSamityBySamityId(memberAndLoanAccountAndLoanRebate.getT2().getSamityId()))
                                .doOnNext(tuple3 -> log.info("MemberAndLoanAccountEntity, LoanRebate, SamityEntity received: {}", tuple3)))
                .filter(memberAndLoanAccountAndLoanRebateAndSamity -> memberAndLoanAccountAndLoanRebateAndSamity.getT3().getOfficeId().equalsIgnoreCase(requestDto.getOfficeId()))
                .filter(memberAndLoanAccountAndLoanRebateAndSamity -> StringUtils.isEmpty(requestDto.getSamityId()) || memberAndLoanAccountAndLoanRebateAndSamity.getT2().getSamityId().equals(requestDto.getSamityId()))
                .filter(memberAndLoanAccountAndLoanRebateAndSamity -> StringUtils.isEmpty(requestDto.getStatus()) || memberAndLoanAccountAndLoanRebateAndSamity.getT2().getStatus().equals(requestDto.getStatus()))
                .map(memberAndLoanAccountAndLoanRebateAndSamity -> buildLoanRebateGridData(memberAndLoanAccountAndLoanRebateAndSamity.getT1(), memberAndLoanAccountAndLoanRebateAndSamity.getT2(), memberAndLoanAccountAndLoanRebateAndSamity.getT3()))
                .skip((long) requestDto.getOffset() * requestDto.getLimit())
                .take(requestDto.getLimit())
                .collectList()
                .zipWith(managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                        .switchIfEmpty(Mono.just(ManagementProcessTracker.builder().build())))
                .map(listOfLoanRebateGridDataAndManagementProcessTracker -> LoanRebateGridViewByOfficeResponseDto.builder()
                        .officeId(listOfLoanRebateGridDataAndManagementProcessTracker.getT2().getOfficeId())
                        .officeNameBn(listOfLoanRebateGridDataAndManagementProcessTracker.getT2().getOfficeNameBn())
                        .officeNameEn(listOfLoanRebateGridDataAndManagementProcessTracker.getT2().getOfficeNameEn())
                        .data(listOfLoanRebateGridDataAndManagementProcessTracker.getT1())
                        .totalCount(listOfLoanRebateGridDataAndManagementProcessTracker.getT1().size())
                        .build())
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())));
    }

    @Override
    public Mono<GetLoanRebateDetailResponseDto> getLoanRebateDetail(GetLoanRebateDetailRequestDto requestDto) {
        return loanRebatePersistencePort.getLoanRebateByOid(requestDto.getId())
                .switchIfEmpty(loanRebateHistoryPersistencePort.getLastLoanRebateHistoryByLoanRebateDataOid(requestDto.getId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Rebate Data not found")))
                .flatMap(loanRebate -> iStagingDataUseCase.getStagingAccountDataListByMemberId(loanRebate.getMemberId())
                        .collectList()
                        .zipWith(Mono.just(loanRebate)))
                .flatMap(stagingAccountDataListAndLoanRebate -> Mono.zip(Mono.just(stagingAccountDataListAndLoanRebate.getT2()), commonRepository.getMemberEntityByMemberId(stagingAccountDataListAndLoanRebate.getT2().getMemberId()),
                        collectionStagingDataQueryUseCase.getStagingAccountDataByLoanAccountId(stagingAccountDataListAndLoanRebate.getT2().getLoanAccountId(), stagingAccountDataListAndLoanRebate.getT2().getManagementProcessId())
                                .map(stagingAccountData -> modelMapper.map(stagingAccountData, StagingAccountData.class)),
//                        collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(stagingAccountDataListAndLoanRebate.getT2().getLoanAccountId(), stagingAccountDataListAndLoanRebate.getT2().getManagementProcessId(), stagingAccountDataListAndLoanRebate.getT2().getProcessId(), String.valueOf(stagingAccountDataListAndLoanRebate.getT2().getCurrentVersion())),
//                        loanAdjustmentUseCase.getAdjustedLoanAccountListByManagementProcessId(LoanAdjustmentRequestDTO.builder()
//                                .managementProcessId(stagingAccountDataListAndLoanRebate.getT2().getManagementProcessId())
//                                .processId(stagingAccountDataListAndLoanRebate.getT2().getProcessId())
//                                .memberId(stagingAccountDataListAndLoanRebate.getT2().getMemberId())
//                                .currentVersion(stagingAccountDataListAndLoanRebate.getT2().getCurrentVersion()).build()
//                        ),
                        Mono.just(stagingAccountDataListAndLoanRebate.getT1()), loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(stagingAccountDataListAndLoanRebate.getT2().getLoanAccountId())))
                .flatMap(tuple -> buildLoanRebateDetailViewResponseDto(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()));
    }

    @Override
    public Mono<SettleRebateResponseDto> submitLoanRebate(GetLoanRebateDetailRequestDto requestDto) {
        return loanRebatePersistencePort.getLoanRebateByOid(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Loan Rebate data Found")))
                .doOnSuccess(loanRebate -> log.info("Loan Rebate Found: {} by oid {}", loanRebate, requestDto.getId()))
                .flatMap(loanRebate -> validateIfExistingRebateDataUpdatable(loanRebate, requestDto.getLoginId()))
                .flatMap(this::validateUpdateRequest)
                .flatMap(loanRebate -> Mono.zip(
                        loanRebatePersistencePort.saveLoanRebate(updateStatusToSubmitLoanRebateDataForAuthorization(loanRebate, requestDto.getLoginId())),
                        loanAdjustmentUseCase.submitLoanAdjustmentDataForAuthorization(loanRebate.getManagementProcessId(), loanRebate.getProcessId(), requestDto.getLoginId())
                                .onErrorResume(error -> Mono.empty()),
                        paymentCollectionUseCase.submitCollectionPaymentForAuthorization(loanRebate.getManagementProcessId(), loanRebate.getProcessId(), requestDto.getLoginId())
                                .onErrorResume(error -> Mono.empty())
                ))
                .as(rxtx::transactional)
                .map(data -> SettleRebateResponseDto.builder()
                        .userMessage("Early Settlement Is Successfully Submited")
                        .build())
                .doOnSuccess(response -> log.info("Loan Rebate submit Response: {}", response))
                .doOnError(throwable -> log.error("Error Submitting Loan Rebate: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    @Override
    public Mono<SettleRebateResponseDto> updateLoanRebate(SettleRebateRequestDto requestDto) {
        return loanRebatePersistencePort.getLoanRebateByOid(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Loan Rebate data Found")))
                .flatMap(loanRebate -> validateIfExistingRebateDataUpdatable(loanRebate, requestDto.getLoginId()))
                .flatMap(loanRebate -> validateAgainstSamityAndOfficeEventTracker(loanRebate, requestDto))
                .flatMap(loanRebate -> this.getRebateableAmountByLoanAccountId(requestDto.getLoanAccountId(), requestDto.getOfficeId())
                        .doOnError(throwable -> log.error("Error happened while fetching rebate amount for loanAccountId: {}", throwable.getMessage()))
                        .doOnRequest(req -> log.info("Request sent for fetching rebate amount for loanAccountId: {}", requestDto.getLoanAccountId()))
                        .doOnSuccess(res -> log.info("Response received for fetching rebate amount for loanAccountId: {}", res))
                        .flatMap(rebateAmount -> validateRebatedAmount(rebateAmount, requestDto))
                        .flatMap(validatedRebatedAmount -> iStagingDataUseCase.getStagingAccountDataByLoanAccountId(requestDto.getLoanAccountId())
                                .zipWith(Mono.just(validatedRebatedAmount)))
                        .flatMap(stagingLoanAccountDataAndRebatedAmount -> validatePayableAmount(requestDto, stagingLoanAccountDataAndRebatedAmount.getT1().getTotalPrincipalRemaining().add(stagingLoanAccountDataAndRebatedAmount.getT1().getTotalServiceChargeRemaining()).subtract(stagingLoanAccountDataAndRebatedAmount.getT2())))
                        .map(totalPayableAmount -> loanRebate)
                )
                .flatMap(loanRebate -> iStagingDataUseCase.getStagingDataByMemberId(loanRebate.getMemberId())
                        .doOnRequest(req -> log.info("Request sent for fetching staging data for memberId: {}", loanRebate.getMemberId()))
                        .doOnSuccess(res -> log.info("Response received for fetching staging data for memberId: {}", res))
                        .doOnError(throwable -> log.error("Error happened while fetching staging data for memberId: {}", throwable.getMessage()))
                        .zipWith(Mono.just(loanRebate)))
                .flatMap(stagingDataAndLoanRebate -> {
                    requestDto.setMemberId(stagingDataAndLoanRebate.getT2().getMemberId());
                    requestDto.setSamityId(stagingDataAndLoanRebate.getT2().getSamityId());
                    LoanRebate loanRebateHistory = buildLoanRebateHistory(gson.fromJson(stagingDataAndLoanRebate.getT2().toString(), LoanRebate.class));
                    log.info("Loan Rebate History Data to be saved: {}", loanRebateHistory);
                    LoanRebate loanRebate = buildUpdatedLoanRebateData(stagingDataAndLoanRebate.getT2(), requestDto);
                    log.info("Loan Rebate Data to be updated: {}", loanRebate);
                    return loanRebateDataEditHistoryPersistencePort.saveLoanRebateEditHistory(loanRebateHistory)
                            .then(loanRebatePersistencePort.saveLoanRebate(addPaymentInfoIntoLoanRebate(loanRebate, requestDto)))
                            .then(updateLoanAdjustmentAndPaymentCollection(loanRebate, requestDto, loanRebateHistory.getPaymentMode(), stagingDataAndLoanRebate.getT1()))
                            .thenReturn(loanRebate);
                })
                .as(rxtx::transactional)
                .map(data -> SettleRebateResponseDto.builder()
                        .userMessage("Loan Rebate Data is Updated Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan Rebate update Response: {}", response))
                .doOnError(throwable -> log.error("Error Updating Loan Rebate: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));

    }


    @Override
    public Mono<List<LoanRebateDTO>> getLoanRebateDataBySamityId(String samityId, String managementProcessId) {
        return loanRebatePersistencePort
                .getLoanRebateDataBySamityId(samityId, managementProcessId)
                .collectList()
                .doOnError(throwable -> log.error("Error happened while fetching Loan Rebate Data for SamityId: {}", throwable.getMessage()));
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return loanRebatePersistencePort.lockSamityForAuthorization(samityId, managementProcessId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return loanRebatePersistencePort.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return loanRebatePersistencePort.getSamityIdListLockedByUserForAuthorization(loginId);
    }

    @Override
    public Mono<List<LoanRebateDTO>> getAllLoanRebateDataBySamityIdList(List<String> samityIdList) {
        return loanRebatePersistencePort.getAllLoanRebateDataBySamityIdList(samityIdList);
    }

    @Override
    public Mono<String> validateAndUpdateLoanRebateDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return loanRebatePersistencePort.validateAndUpdateLoanRebateDataForRejectionBySamityId(managementProcessId, samityId, loginId);
    }

    
    public Mono<String> authorizeSamityForLoanRebate(LoanRebateAuthorizeCommand command) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        final String transactionProcessId = !HelperUtil.checkIfNullOrEmpty(command.getTransactionProcessId())
                ? command.getTransactionProcessId()
                : UUID.randomUUID().toString();
        final String passbookProcessId = !HelperUtil.checkIfNullOrEmpty(command.getPassbookProcessId())
                ? command.getPassbookProcessId()
                : UUID.randomUUID().toString();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this
                        .validateAndUpdateLoanRebateDataForAuthorization(command))
                .doOnNext(loanRebateDTOList -> log.info("Loan Rebate Data List: {}",
                        loanRebateDTOList))
                .flatMap(loanRebateDTOList ->
                        loanRepaymentScheduleUseCase
                                .archiveAndUpdateRepaymentScheduleForLoanRebate(loanRebateDTOList)
                                .map(aBoolean -> loanRebateDTOList))
                .flatMap(loanRebateDTOList -> this.createTransactionForLoanRebate(loanRebateDTOList, managementProcess.get().getManagementProcessId(),
                                command.getMfiId(), command.getOfficeId(), command.getLoginId(), transactionProcessId, command.getSamityId())
                                    .flatMap(transactionList -> this.createSMSNotificationEntryForLoanRebate(transactionList, command.getSmsNotificationMetaPropertyList()))
                                    .flatMap(transactionList -> this.createPassbookEntryForLoanRebate(transactionList, command.getLoginId(), managementProcess.get().getManagementProcessId(), passbookProcessId)))
                .flatMap(passbookResponseDTOS -> loanRebatePersistencePort.updateStatusOfLoanRebateDataForAuthorization(command.getSamityId(), command.getLoginId(), command.getManagementProcessId()))
                .as(rxtx::transactional)
                .map(data -> "Loan Rebate Authorization Successful for Samity")
                .doOnNext(response -> log.info("Loan Rebate Authorization Response: {}", response))
                .doOnError(throwable -> log.error("Error in Loan Rebate Authorization: {}",
                        throwable.getMessage()));
    }


    @Override
    public Mono<LoanRebateDTO> updateLoanRebateDataOnUnAuthorization(LoanRebateDTO loanRebateDTO) {
        return loanRebatePersistencePort.updateLoanRebateDataOnUnAuthorization(loanRebateDTO)
                .flatMap(loanRebateDTO1 -> loanAccountUseCase.updateLoanAccountStatus(loanRebateDTO1.getLoanAccountId(), Status.STATUS_ACTIVE.getValue())
                        .thenReturn(loanRebateDTO1));
    }


    private Mono<List<PassbookResponseDTO>> createPassbookEntryForLoanRebate(
            List<Transaction> transactionList, String loginId, String managementProcessId,
            String passbookProcessId) {
        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()))
                .map(transaction -> PassbookRequestDTO.builder()
                        .managementProcessId(managementProcessId)
                        .processId(passbookProcessId)
                        .officeId(transaction.getOfficeId())
                        .loanAccountId(transaction.getLoanAccountId())
                        .amount(transaction.getAmount())
                        .transactionId(transaction.getTransactionId())
                        .transactionCode(transaction.getTransactionCode())
                        .transactionDate(transaction.getTransactionDate())
                        .mfiId(transaction.getMfiId())
                        .paymentMode(transaction.getPaymentMode())
                        .loginId(loginId)
                        .loanAdjustmentProcessId(transaction.getLoanAdjustmentProcessId())
                        .samityId(transaction.getSamityId())
                        .build())
                .flatMap(passbookUseCase::createPassbookEntryForLoanRebateAndWriteOff)
                .collectList()
                .filter(passbookResponseDTO -> passbookResponseDTO.get(0).getTransactionCode().equals(TransactionCodes.LOAN_REBATE.getValue()))
                .flatMap(passbookResponseDTOList -> Mono.just(getFullyPaidInstallmentNos(passbookResponseDTOList))
                .flatMapMany(tuple2 -> {
                    if (!tuple2.getT2().isEmpty()) {
                        return loanRepaymentScheduleUseCase.updateInstallmentStatus(tuple2.getT2(), Status.STATUS_PAID.getValue(), tuple2.getT1(), managementProcessId);
                    }
                    return Flux.just(RepaymentScheduleResponseDTO.builder().build());
                })
                .collectList()
                        .map(repaymentScheduleResponseDTOS -> passbookResponseDTOList));
    }

    private Tuple2<String, List<Integer>> getFullyPaidInstallmentNos(List<PassbookResponseDTO> passbookResponseDTOList) {
        AtomicReference<String> loanAccountId = new AtomicReference<>();
        log.debug("passbookResponseDTOList : {}", passbookResponseDTOList);
        List<Integer> fulfilledInstallments = passbookResponseDTOList
                .stream()
                .filter(passbookResponseDTO -> !passbookResponseDTO.getStatus().equals(Status.STATUS_REBATED.getValue()))
                .peek(passbookResponseDTO -> log.debug("before filter passbook response dto : {}", passbookResponseDTO))
                .filter(this::isThisInstallmentFullyPaid)
                .peek(passbookResponseDTO -> log.debug("after filter passbook response dto : {}", passbookResponseDTO))
                .peek(passbookResponseDTO -> loanAccountId.set(passbookResponseDTO.getLoanAccountId()))
                .map(PassbookResponseDTO::getInstallNo)
                .peek(integer -> log.debug("fulfilled installments : {}", integer))
                .toList();
        log.debug("fulfilledInstallments : {}", fulfilledInstallments);
        Tuple2<String, List<Integer>> tuples;
        if (fulfilledInstallments.isEmpty()) {
            log.debug("I was here {}", loanAccountId);
            tuples = Tuples.of("", new ArrayList<>());
        } else tuples = Tuples.of(loanAccountId.get(), fulfilledInstallments);
        log.debug("Tuple2<String, List<Integer>> {}", tuples);
        return tuples;
    }

    private boolean isThisInstallmentFullyPaid(PassbookResponseDTO passbookResponseDTO) {
        if (passbookResponseDTO.getScRemainForThisInst() != null && passbookResponseDTO.getPrinRemainForThisInst() != null) {
            return passbookResponseDTO.getScRemainForThisInst().compareTo(BigDecimal.ZERO) == 0 && passbookResponseDTO.getPrinRemainForThisInst().compareTo(BigDecimal.ZERO) == 0;
        } else return false;
    }


    private Mono<List<Transaction>> createTransactionForLoanRebate(List<LoanRebateDTO> loanRebateDTOList, String managementProcessId, String mfiId, String officeId, String loginId, String transactionProcessId, String samityId) {
            return managementProcessTrackerUseCase
                    .getCurrentBusinessDateForOffice(managementProcessId, officeId)
                    .flatMapMany(businessDate -> Flux.fromIterable(loanRebateDTOList)
                    .map(loanRebateDTO -> Transaction.builder()
                            .transactionId(UUID.randomUUID().toString())
                            .mfiId(mfiId)
                            .managementProcessId(managementProcessId)
                            .processId(transactionProcessId)
                            .officeId(officeId)
                            .memberId(loanRebateDTO.getMemberId())
                            .accountType(ACCOUNT_TYPE_LOAN.getValue())
                            .loanAccountId(loanRebateDTO.getLoanAccountId())
                            .amount(loanRebateDTO.getPayableAmount())
                            .transactionCode(TransactionCodes.LOAN_REBATE.getValue())
                            .paymentMode(PAYMENT_MODE_REBATE.getValue())
                            .status(Status.STATUS_APPROVED.getValue())
                            .transactionDate(businessDate)
                            .transactedBy(loginId)
                            .createdBy(loginId)
                            .createdOn(LocalDateTime.now())
                            .samityId(samityId)
                            .build()))
                    .collectList()
                    .doOnNext(transactionList -> log.debug("Transaction List For Loan Rebate: {}",
                            transactionList))
                    .flatMap(transactionUseCase::createTransactionEntryForLoanAdjustmentForSamity);

    }


    private Mono<List<Transaction>> createSMSNotificationEntryForLoanRebate(List<Transaction> transactionList, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()))
//                .flatMap(transaction -> this.createSMSNotificationRequestForTransaction(transaction, smsNotificationMetaPropertyList))
                .collectList()
                .map(transactions -> transactionList)
                .onErrorResume(throwable -> {
                    log.error("Error in Creating SMS Notification Entry for Loan Rebate: {}", throwable.getMessage());
                    return Mono.just(transactionList);
                });
    }


    private Mono<Transaction> createSMSNotificationRequestForTransaction(Transaction transaction, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.fromSupplier(() -> smsNotificationMetaPropertyList.stream()
                        .filter(metaProperty -> metaProperty.getType().equalsIgnoreCase(transaction.getTransactionCode()))
                        .findFirst()
                        .get())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SMS Notification Meta Property Not Found for Transaction Type: " + transaction.getTransactionCode())))
                .flatMap(smsNotificationMetaProperty -> {
                    if (smsNotificationMetaProperty.getIsSMSNotificationEnabled().equals("Yes")) {
                        return this.createAndSaveSMSNotificationRequest(transaction, smsNotificationMetaProperty);
                    }
                    log.info("SMS Notification is Disabled for Transaction Type: {}", transaction.getTransactionCode());
                    return Mono.just(transaction);
                });
    }

    private Mono<Transaction> createAndSaveSMSNotificationRequest(Transaction transaction, SMSNotificationMetaProperty smsNotificationMetaProperty) {
        log.info("Creating and Saving SMS Notification Entry for Transaction with AccountId: {}, Transaction Code: {} and transaction Amount: {}", !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId(), transaction.getTransactionCode(), transaction.getAmount());
        return stagingDataUseCase.getStagingDataByAccountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                .flatMap(stagingData -> commonRepository.getInstituteOidByMFIId(transaction.getMfiId())
                        .map(instituteOid -> Tuples.of(stagingData, instituteOid)))
                .map(tuples -> {
                    StagingData stagingData = tuples.getT1();
                    MobileInfoDTO mobileInfoDTO = gson.fromJson(gson.fromJson(stagingData.getMobile(), ArrayList.class)
                            .get(0)
                            .toString(), MobileInfoDTO.class);
                    return SmsNotificationRequestDTO.builder()
                            .type(transaction.getTransactionCode())
                            .id(transaction.getTransactionId())
                            .amount(String.valueOf(transaction.getAmount()))
                            .datetime(String.valueOf(transaction.getTransactionDate()))
                            .accountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                            .memberId(stagingData.getMemberId())
                            .mobileNumber(mobileInfoDTO.getContactNo())
                            .template(smsNotificationMetaProperty.getTemplate())
                            .mfiId(transaction.getMfiId())
                            .instituteOid(tuples.getT2())
                            .build();
                })
                .doOnNext(smsNotificationRequestDTO -> log.info("SMS Notification Entry Request for Account: {}, Mobile: {} and Amount: {}", smsNotificationRequestDTO.getAccountId(), smsNotificationRequestDTO.getMobileNumber(), smsNotificationRequestDTO.getAmount()))
                .flatMap(smsNotificationUseCase::saveSmsLog)
                .doOnNext(smsLog -> log.info("SMS Notification Entry Saved for Mobile No: {}, with SMS Body: {}", smsLog.getMobileNo(), smsLog.getSms()))
                .map(smsLog -> transaction)
                .onErrorResume(throwable -> {
//                    log.error("Error in Creating and Saving SMS Notification Entry: {}", throwable.getMessage());
                    return Mono.just(transaction);
                });
    }


    private Mono<List<LoanRebateDTO>> validateAndUpdateLoanRebateDataForAuthorization(
            LoanRebateAuthorizeCommand requestDTO) {
        return loanRebatePersistencePort.getLoanRebateDataBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                .collectList()
                .filter(loanRebateDTOList -> !loanRebateDTOList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Rebate Data Not Found for Samity")))
                .filter(loanRebateDTOList -> loanRebateDTOList.stream()
                        .noneMatch(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getStatus())
                                && loanAdjustmentData.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Rebate Data is Already Authorized for Samity")))
                .filter(loanRebateDTOList -> !loanRebateDTOList.isEmpty() && loanRebateDTOList.stream().allMatch(
                        loanRebateDTO -> !HelperUtil.checkIfNullOrEmpty(loanRebateDTO.getStatus())
                                && (loanRebateDTO.getStatus().equals(Status.STATUS_SUBMITTED.getValue())
                                || loanRebateDTO.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Rebate Data is not Submitted for Authorization")))
                .filter(loanRebateDTOList -> loanRebateDTOList.stream()
                        .allMatch(loanRebateDTO -> !HelperUtil
                                .checkIfNullOrEmpty(loanRebateDTO.getIsLocked())
                                && loanRebateDTO.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Rebate Data is Not Locked for Samity")));
                /*.flatMap(loanRebateDTOList -> loanRebatePersistencePort
                        .updateStatusOfLoanRebateDataForAuthorization(
                                requestDTO.getSamityId(), requestDTO.getLoginId(), requestDTO.getManagementProcessId())
                        .thenReturn(loanRebateDTOList));*/
    }


    private LoanRebate buildLoanRebateHistory(LoanRebate rebate) {
        rebate.setLoanRebateDataOid(rebate.getOid());
        rebate.setOid(null);
        return rebate;
    }


//    private Mono<Void> updateCollectionAndAdjustmentData(SettleRebateRequestDto requestDto, LoanRebate loanRebate, StagingData stagingData) {
//        log.info("process id for adjustment: {}", loanRebate.getProcessId());
//        if (requestDto.getCollectionType().equalsIgnoreCase("Combine")) {
//            return loanAdjustmentUseCase.updateLoanAdjustmentForMember(buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate))
//                    .then(paymentCollectionUseCase.updateCollectionPaymentByManagementId(buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData)))
//                    .then();
//        } else if (requestDto.getCollectionType().equalsIgnoreCase("Adjustment")) {
//            return loanAdjustmentUseCase.updateLoanAdjustmentForMember(buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate))
//                    .then(paymentCollectionUseCase.removeCollectionPayment(buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData)))
//                    .then();
//        } else {
//            return paymentCollectionUseCase.updateCollectionPaymentByManagementId(buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
//                    .then(loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(buildDeleteLoanAdjustmentRequestDTO(requestDto, loanRebate)))
//                    .then();
//        }
//    }


    public Mono<String> updateLoanAdjustmentAndPaymentCollection(LoanRebate loanRebate, SettleRebateRequestDto requestDto, String originalType, StagingData stagingData) {
        originalType = originalType.trim().toUpperCase();
        String newType = requestDto.getCollectionType().trim().toUpperCase();
        log.info("Original Type: {}, New Type: {}", originalType, newType);

        switch (CollectionTypeChange.valueOf(originalType + "_TO_" + newType)) {
            case CASH_TO_CASH:
                return paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .map(data -> "Collection Payment Data Updated Successfully");
            case CASH_TO_ADJUSTMENT:
                return paymentCollectionUseCase.removeCollectionPayment(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .then(loanAdjustmentUseCase.createLoanAdjustmentForMember(
                                buildLoanAdjustmentRequestDto(requestDto, loanRebate, loanRebate.getProcessId())))
                        .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Created Successfully");
            case CASH_TO_COMBINE:
                return paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .then(loanAdjustmentUseCase.createLoanAdjustmentForMember(
                                buildLoanAdjustmentRequestDto(requestDto, loanRebate, loanRebate.getProcessId())))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Created Successfully");
            case ADJUSTMENT_TO_CASH:
                return loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(buildDeleteLoanAdjustmentRequestDTO(requestDto, loanRebate))
                        .then(paymentCollectionUseCase.collectPaymentBySamity(
                                buildPaymentCollectionRequestDto(requestDto, loanRebate, loanRebate.getProcessId())))
                        .map(data -> "Loan Adjustment Data Deleted and Collection Payment Data Created Successfully");
            case ADJUSTMENT_TO_ADJUSTMENT:
                return loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                                buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate))
                        .map(data -> "Loan Adjustment Data Updated Successfully");
            case ADJUSTMENT_TO_COMBINE:
                return loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                                buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate))
                        .then(paymentCollectionUseCase.collectPaymentBySamity(
                                buildPaymentCollectionRequestDto(requestDto, loanRebate, loanRebate.getProcessId())))
                        .map(data -> "Loan Adjustment Data Updated and Collection Payment Data Created Successfully");
            case COMBINE_TO_CASH:
                log.info("loan rebate for combine to cash: {}", loanRebate);
                return paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .then(loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(buildDeleteLoanAdjustmentRequestDTO(requestDto, loanRebate)))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Deleted Successfully");
            case COMBINE_TO_ADJUSTMENT:
                return paymentCollectionUseCase.removeCollectionPayment(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .then(loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                                buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate)))
                        .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Updated Successfully");
            case COMBINE_TO_COMBINE:
                return paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                                buildUpdatePaymentCollectionBySamityCommand(requestDto, loanRebate, stagingData))
                        .then(loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                                buildUpdateLoanAdjustmentRequestDTO(requestDto, loanRebate)))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Updated Successfully");
            default:
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Unsupported Collection Type Change"));
        }
    }


    private Mono<LoanRebate> validateAgainstSamityAndOfficeEventTracker(LoanRebate loanRebate, SettleRebateRequestDto requestDto) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(loanRebate.getManagementProcessId(), loanRebate.getSamityId())
                .doOnNext(samityEventTracker -> log.info("Samity Event Tracker TEST: {}", samityEventTracker))
                .filter(samityEventTracker -> samityEventTracker.getSamityEvent() != null)
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Samity Event Found for Samity Id: " + loanRebate.getSamityId())))
                .flatMap(samityEvent -> {
                            if (samityEvent.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event is already 'Authorized'"));
                            } else if (samityEvent.getSamityEvent().equals(SamityEvents.CANCELED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event is Already Canceled"));
                            } else
                                return Mono.just(samityEvent);
                        }
                )
                .flatMap(samityEvent ->
                        officeEventTrackerUseCase.getAllOfficeEventsForOffice(loanRebate.getManagementProcessId(), requestDto.getOfficeId())
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Office Event Found for Office Id: " + requestDto.getOfficeId())))
                                .filter(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                                .filter(officeEventTracker -> !officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
                )
                .then(Mono.just(loanRebate));
    }


    private Mono<LoanRebate> validateIfExistingRebateDataUpdatable(LoanRebate loanRebate, String loginId) {
        return Mono.just(loanRebate)
                .filter(loanRebateData -> !HelperUtil.checkIfNullOrEmpty(loanRebateData.getCreatedBy()) && loanRebateData.getCreatedBy().equals(loginId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Rebate Data is not updatable by this user")))
                .filter(loanRebateData -> !HelperUtil.checkIfNullOrEmpty(loanRebateData.getStatus()) && !loanRebateData.getStatus().equals(Status.STATUS_SUBMITTED.getValue()) && !loanRebateData.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan rebate data is already 'Submitted' or 'Approved'")));
    }


    public LoanRebate buildUpdatedLoanRebateData(LoanRebate loanRebate, SettleRebateRequestDto requestDto) {
        loanRebate.setUpdatedBy(requestDto.getLoginId());
        loanRebate.setUpdatedOn(LocalDateTime.now());
        loanRebate.setCurrentVersion(loanRebate.getCurrentVersion() + 1);
        loanRebate.setIsNew("No");
        loanRebate.setLoanAccountId(requestDto.getLoanAccountId());
        loanRebate.setRebateAmount(BigDecimal.valueOf(Double.parseDouble(requestDto.getRebatedAmount())));
        loanRebate.setPayableAmount(BigDecimal.valueOf(Double.parseDouble(requestDto.getPayableAmount())));
        loanRebate.setPaymentMode(requestDto.getCollectionType());
        loanRebate.setEditCommit(Status.STATUS_YES.getValue());
        return loanRebate;
    }


    public LoanAdjustmentRequestDTO buildUpdateLoanAdjustmentRequestDTO(SettleRebateRequestDto
                                                                                requestDto, LoanRebate loanRebate) {
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(loanRebate.getManagementProcessId())
                .processId(loanRebate.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(loanRebate.getSamityId())
                .memberId(loanRebate.getMemberId())
                .adjustmentType(REBATE.getValue())
                .currentVersion(loanRebate.getCurrentVersion())
                .data(Arrays.asList(AdjustedLoanData.builder()
                        .loanAccountId(requestDto.getLoanAccountId())
                        .adjustedAccountList(requestDto.getAdjustedAccountList().stream()
                                .map(src -> modelMapper.map(src, AdjustedAccount.class)).toList()).build()))
                .build();
    }


    public LoanAdjustmentRequestDTO buildDeleteLoanAdjustmentRequestDTO(SettleRebateRequestDto
                                                                                requestDto, LoanRebate loanRebate) {
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(loanRebate.getManagementProcessId())
                .processId(loanRebate.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(loanRebate.getSamityId())
                .memberId(loanRebate.getMemberId())
                .adjustmentType(REBATE.getValue())
                .currentVersion(loanRebate.getCurrentVersion())
                .build();
    }


    public PaymentCollectionBySamityCommand buildUpdatePaymentCollectionBySamityCommand(SettleRebateRequestDto
                                                                                                requestDto, LoanRebate loanRebate, StagingData stagingData) {
        return PaymentCollectionBySamityCommand.builder()
                .managementProcessId(loanRebate.getManagementProcessId())
                .processId(loanRebate.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .collectionType(REBATE.getValue())
                .data(Arrays.asList(CollectionData.builder()
                        .stagingDataId(stagingData.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(requestDto.getLoanAccountId())
                        .amount(requestDto.getCollectionType().equalsIgnoreCase(COLLECTION_TYPE_ADJUSTMENT.getValue()) ? BigDecimal.ZERO : BigDecimal.valueOf(Double.parseDouble(requestDto.getCollectedAmountByCash())))
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .collectionType(REBATE.getValue())
                        .currentVersion(loanRebate.getCurrentVersion())
                        .build()))
                .build();
    }


    public Mono<GetLoanRebateDetailResponseDto> buildLoanRebateDetailViewResponseDto(LoanRebate
                                                                                             loanRebate, MemberEntity memberEntity,
                                                                                     StagingAccountData stagingAccountData, List<StagingAccountData> stagingAccountDataList, LoanAccountResponseDTO
                                                                                             loanAccountResponseDTO
    ) {
        log.info("Loan Rebate Data member entity: {}", memberEntity);
        return  Mono.zip(
                loanRebate.getStatus().equalsIgnoreCase(Status.STATUS_APPROVED.getValue()) ? Mono.just(BigDecimal.ZERO) : this.getRebateableAmountByLoanAccountId(stagingAccountData.getLoanAccountId(), memberEntity.getOfficeId()),
                        serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()),
                        commonRepository.getDisbursementDateByLoanAccountId(stagingAccountData.getLoanAccountId()).switchIfEmpty(Mono.empty()), getSavingsAccountDetailsForRebate(stagingAccountDataList))
                .map(rebatableAmountAndServiceChargeDetailsDisbursementDateAndSavingsAccountList -> GetLoanRebateDetailResponseDto.builder()
                        .memberId(memberEntity.getMemberId())
                        .memberNameEn(memberEntity.getMemberNameEn())
                        .memberNameBn(memberEntity.getMemberNameBn())
                        .rebatePaymentMethod(loanRebate.getPaymentMode())
                        .rebateableAmount(rebatableAmountAndServiceChargeDetailsDisbursementDateAndSavingsAccountList.getT1())
                        .payableAmountAfterRebate(loanRebate.getPayableAmount())
                        .rebatedAmount(loanRebate.getRebateAmount())
                        .rebateDate(loanRebate.getEarlySettlementDate())
                        .status(loanRebate.getStatus())
                        .remarks(loanRebate.getRemarks())
                        .rejectedBy(loanRebate.getRejectedBy())
                        .rejectedOn(loanRebate.getRejectedOn())
                        .submittedBy(loanRebate.getSubmittedBy())
                        .submittedOn(loanRebate.getSubmittedOn())
                        .approvedBy(loanRebate.getApprovedBy())
                        .approvedOn(loanRebate.getApprovedOn())
                        .loanAccountId(stagingAccountData.getLoanAccountId())
                        .loanProductId(stagingAccountData.getProductCode())
                        .loanProductNameEn(stagingAccountData.getProductNameEn())
                        .loanProductNameBn(stagingAccountData.getProductNameBn())
                        .serviceCharge(stagingAccountData.getServiceCharge())
                        .totalLoanAmount(stagingAccountData.getLoanAmount().add(stagingAccountData.getServiceCharge()))
                        .loanAmount(stagingAccountData.getLoanAmount())
                        .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                        .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                        .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                        .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                        .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()))
                        .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                        .officeId(memberEntity.getOfficeId())
                        .samityId(loanRebate.getSamityId())
                        .serviceChargeRate(rebatableAmountAndServiceChargeDetailsDisbursementDateAndSavingsAccountList.getT2().getServiceChargeRate())
                        .loanTerm(loanAccountResponseDTO.getLoanTerm())
                        .installmentAmount(loanAccountResponseDTO.getInstallmentAmount())
                        .noOfInstallment(loanAccountResponseDTO.getNoInstallment())
                        .disbursementDate(rebatableAmountAndServiceChargeDetailsDisbursementDateAndSavingsAccountList.getT3().toString())
                        .advancePaid(stagingAccountData.getTotalAdvance() == null ? BigDecimal.ZERO : stagingAccountData.getTotalAdvance())
                        .collection(ObjectUtils.isEmpty(loanRebate.getPaymentInfo().getCollection()) ? null : loanRebate.getPaymentInfo().getCollection())
                        .adjustedLoanAccountList(ObjectUtils.isEmpty(loanRebate.getPaymentInfo().getAdjustment()) ? new ArrayList<>() : List.of(loanRebate.getPaymentInfo().getAdjustment()))
                        .savingsAccountList(rebatableAmountAndServiceChargeDetailsDisbursementDateAndSavingsAccountList.getT4())
                        .loanRebateOid(loanRebate.getOid())
                        .build());
    }


    private Mono<LoanRebate> validateUpdateRequest(LoanRebate loanRebate) {
        if (loanRebate.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanRebate.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())) {
            return Mono.just(loanRebate);
        } else {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Rebate data is already submitted or approved"));
        }
    }

    private LoanRebate updateStatusToSubmitLoanRebateDataForAuthorization(LoanRebate loanRebate, String loginId) {
        loanRebate.setStatus(Status.STATUS_SUBMITTED.getValue());
        loanRebate.setIsSubmitted(StatusYesNo.Yes.toString());
        loanRebate.setSubmittedBy(loginId);
        loanRebate.setSubmittedOn(LocalDateTime.now());
        loanRebate.setEditCommit(Status.STATUS_NO.getValue());
        return loanRebate;
    }


    private LoanRebateGridData buildLoanRebateGridData(MemberAndLoanAccountEntity memberAndLoanAccount, LoanRebate
            rebate, Samity samity) {
        return LoanRebateGridData.builder()
                .oid(rebate.getOid())
                .memberId(memberAndLoanAccount.getMemberId())
                .memberNameBn(memberAndLoanAccount.getMemberNameBn())
                .memberNameEn(memberAndLoanAccount.getMemberNameEn())
                .samityId(rebate.getSamityId())
                .samityNameEn(samity.getSamityNameEn())
                .samityNameBn(samity.getSamityNameBn())
                .loanAmount(memberAndLoanAccount.getLoanAmount())
                .loanAccountId(rebate.getLoanAccountId())
                .earlySettlementDate(rebate.getEarlySettlementDate() == null ? null : rebate.getEarlySettlementDate())
                .rebatedAmount(rebate.getRebateAmount())
                .payableAmountAfterRebate(rebate.getPayableAmount())
                .status(rebate.getStatus())
                .btnSubmitEnabled(StringUtils.isNotBlank(rebate.getIsSubmitted()) && rebate.getIsSubmitted().equalsIgnoreCase(StatusYesNo.Yes.toString()) ? StatusYesNo.No.toString() : StatusYesNo.Yes.toString())
                .btnUpdateEnabled(StringUtils.isNotBlank(rebate.getIsLocked()) && rebate.getIsLocked().equalsIgnoreCase(StatusYesNo.No.toString()) && (rebate.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || rebate.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())) ? StatusYesNo.Yes.toString() : StatusYesNo.No.toString())
                .build();
    }


    private Mono<BigDecimal> validateRebatedAmount(BigDecimal rebatedAmount, SettleRebateRequestDto requestDto) {
        return Mono.just(requestDto)
                .flatMap(request -> {
                    if (StringUtils.isNotBlank(request.getRebatedAmount()) && BigDecimal.valueOf(Double.parseDouble(request.getRebatedAmount())).compareTo(BigDecimal.ZERO) > 0 && BigDecimal.valueOf(Double.parseDouble(request.getRebatedAmount())).compareTo(rebatedAmount) <= 0) {
                        return Mono.just(BigDecimal.valueOf(Double.parseDouble(request.getRebatedAmount())));
                    } else {
                        return Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Rebated Amount is not valid"));
                    }
                })
                .doOnRequest(req -> log.info("Request sent for validating rebated amount: {}", requestDto))
                .doOnSuccess(res -> log.info("Successfully validated rebated amount: {}", res));
    }

    private Mono<BigDecimal> validatePayableAmount(SettleRebateRequestDto requestDto, BigDecimal totalPayableAmount) {
        return Mono.just(requestDto)
                .flatMap(request -> {
                    if (StringUtils.isNotBlank(request.getCollectionType()) && request.getCollectionType().equalsIgnoreCase("Combine")) {
                        return validateAdjustableAmount(request)
                                .map(totalAdjustableAmount -> totalAdjustableAmount.add(BigDecimal.valueOf(Double.parseDouble(request.getCollectedAmountByCash()))));
                    } else if (StringUtils.isNotBlank(request.getCollectionType()) && request.getCollectionType().equalsIgnoreCase("Adjustment")) {
                        return validateAdjustableAmount(request);
                    } else {
                        return Mono.just(BigDecimal.valueOf(Double.parseDouble(request.getCollectedAmountByCash())));
                    }
                })
                .doOnRequest(req -> log.info("Request sent to validate total payable amount: {}", requestDto))
                .flatMap(totalPayableAmountFromRequest -> {
                    log.info("total payable amount from request: {}", totalPayableAmountFromRequest);
                    log.info("request dto payable amount : {}", requestDto.getPayableAmount());
                    log.info("total payable amount: {}", totalPayableAmount);
                    if (totalPayableAmountFromRequest.compareTo(BigDecimal.valueOf(Double.parseDouble(requestDto.getPayableAmount()))) == 0 && totalPayableAmountFromRequest.compareTo(totalPayableAmount) == 0) {
                        return Mono.just(totalPayableAmountFromRequest);
                    } else {
                        return Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Total Payable Amount is not valid"));
                    }
                })
                .doOnSuccess(res -> log.info("Successfully validated total payable amount: {}", res))
                .doOnError(throwable -> log.error("Error happened while validating total payable amount: {}", throwable.getMessage()));
    }


    private Mono<BigDecimal> validateAdjustableAmount(SettleRebateRequestDto requestDto) {
        return Flux.fromIterable(requestDto.getAdjustedAccountList())
                .flatMap(rebateAdjustmentAccount -> iStagingDataUseCase.getStagingDataSavingsAccountDetailBySavingsAccountId(rebateAdjustmentAccount.getSavingsAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Savings Account Details not found in staging data")))
                        .flatMap(savingsAccountDetails -> {
                            if (savingsAccountDetails.getSavingsProductType().equalsIgnoreCase("GS") || savingsAccountDetails.getSavingsProductType().equalsIgnoreCase("VS")) {
                                if (savingsAccountDetails.getSavingsAvailableBalance() != null && !savingsAccountDetails.getSavingsAvailableBalance().equals(BigDecimal.ZERO) && savingsAccountDetails.getSavingsAvailableBalance().subtract(BigDecimal.valueOf(Double.parseDouble(rebateAdjustmentAccount.getAmount()))).compareTo(BigDecimal.valueOf(5.00)) >= 0)
                                    return Mono.just(BigDecimal.valueOf(Double.parseDouble(rebateAdjustmentAccount.getAmount())));
                                else
                                    return Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Savings Account Balance is not sufficient"));
                            } else {
                                return Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Savings Account Type is not valid"));
                            }
                        })
                )
                .doOnRequest(req -> log.info("Request received to validate adjustable amount of savings account: {}", requestDto.getAdjustedAccountList()))
                .doOnNext(res -> log.info("Validating adjustable amount for savings account: {}", res))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doOnSuccess(total -> log.info("Successfully validated adjustable amounts with total of: {}", total));
    }

    private Mono<List<LoanAccountForRebateDto>> getLoanAccountDetailsForRebate
            (List<StagingAccountData> stagingAccountDataList, String officeId) {
        return Flux.fromIterable(stagingAccountDataList)
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .flatMap(stagingAccountData -> Mono.zip(Mono.just(stagingAccountData), this.getRebateableAmountByLoanAccountId(stagingAccountData.getLoanAccountId(), officeId), loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()), serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()))
                        .map(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge -> LoanAccountForRebateDto.builder()
                                .loanAccountId(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getLoanAccountId())
                                .loanProductId(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getProductCode())
                                .loanProductNameEn(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getProductNameEn())
                                .loanProductNameBn(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getProductNameBn())
                                .loanAmount(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getLoanAmount())
                                .serviceCharge(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getServiceCharge())
                                .totalLoanAmount(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getLoanAmount()
                                        .add(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getServiceCharge()))
                                .principalPaid(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalPrincipalPaid())
                                .serviceChargePaid(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalServiceChargePaid())
                                .totalPaid(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalPrincipalPaid()
                                        .add(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalServiceChargePaid()))
                                .principalRemaining(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalPrincipalRemaining())
                                .serviceChargeRemaining(
                                        tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalServiceChargeRemaining())
                                .totalDue(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalPrincipalRemaining().add(
                                        tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalServiceChargeRemaining()))
                                .rebatableAmount(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT2())
                                .disbursementDate(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getDisbursementDate() == null ? null : tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getDisbursementDate().toString())
                                .loanTerm(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT3().getLoanTerm())
                                .installmentAmount(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT3().getInstallmentAmount())
                                .noOfInstallment(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT3().getNoInstallment())
                                .serviceChargeRate(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT4().getServiceChargeRate())
                                .advancePaid(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalAdvance() == null ? BigDecimal.ZERO : tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalAdvance())
                                .payableAmount(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalPrincipalRemaining().add(
                                        tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT1().getTotalServiceChargeRemaining()).subtract(tupleOfStagingAccountDataAndTotalRebateAmountAndLoanAccountDetailsAndServiceCharge.getT2()))
                                .build()))
                .collectList();
    }

    private Mono<BigDecimal> getRebateableAmountByLoanAccountId(String loanAccountId, String officeId) {
        return this.getCurrentBusinessDateByOfficeId(officeId)
                .flatMap(businessDate -> commonRepository.getTotalRebateAmountByLoanAccountId(loanAccountId, businessDate)
                    .doOnRequest(req -> log.info("Request sent to get rebateable amount by loan account id: {}", loanAccountId))
                    .doOnError(throwable -> log.error("Error happened while fetching rebateable amount by loan account id: {}", throwable.getMessage()))
                    .flatMap(totalServiceChargeRemaining -> passbookUseCase.getLastPassbookEntry(loanAccountId)
                            .filter(passbook -> passbook.getInstallDate() != null && passbook.getInstallDate().isAfter(businessDate)) // we need to subtract only the advance paid service charge amount
                            .switchIfEmpty(Mono.just(Passbook.builder().serviceChargePaid(BigDecimal.ZERO).build()))
                            .map(passbook -> totalServiceChargeRemaining.subtract(passbook.getServiceChargePaid())))
                .doOnNext(bigDecimal -> log.info("Rebateable amount for loan account id: {} is: {}", loanAccountId, bigDecimal)));
    }

    private Mono<List<SavingsAccountForRebateDto>> getSavingsAccountDetailsForRebate
            (List<StagingAccountData> stagingAccountDataList) {
        return Flux.fromIterable(stagingAccountDataList)
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId()))
                .filter(savingsStagingAccountData -> savingsStagingAccountData.getSavingsProductType().equalsIgnoreCase("GS") || savingsStagingAccountData.getSavingsProductType().equalsIgnoreCase("VS"))
                .flatMap(savingsStagingAccountData -> Mono.zip(collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(savingsStagingAccountData.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(CollectionStagingData.builder().build())),
                        loanAdjustmentPersistencePort.getLoanAdjustmentCollectionDataBySavingsAccountId(savingsStagingAccountData.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build())),
                        withdrawStagingDataPersistencePort.getWithdrawStagingDataBySavingsAccountId(savingsStagingAccountData.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(StagingWithdrawData.builder().build()))
                )
                        .map(tupleOfCollectionDataAdjustmentDataAndWithdrawData -> {
                            BigDecimal collectionAmount = tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT1().getAmount() == null ? BigDecimal.ZERO : tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT1().getAmount();
                            BigDecimal adjustmentAmount = tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT2().getAmount() == null ? BigDecimal.ZERO : tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT2().getAmount();
                            BigDecimal withdrawnAmount = tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT3().getAmount() == null ? BigDecimal.ZERO : tupleOfCollectionDataAdjustmentDataAndWithdrawData.getT3().getAmount();
                            return SavingsAccountForRebateDto.builder()
                                    .savingsAccountId(savingsStagingAccountData.getSavingsAccountId())
                                    .savingsProductId(savingsStagingAccountData.getSavingsProductCode())
                                    .savingsProductNameEn(savingsStagingAccountData.getSavingsProductNameEn())
                                    .savingsProductNameBn(savingsStagingAccountData.getSavingsProductNameBn())
                                    .balance(savingsStagingAccountData.getBalance())
                                    .availableBalance(savingsStagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : savingsStagingAccountData.getSavingsAvailableBalance()
                                            .add(collectionAmount)
                                            .subtract(adjustmentAmount)
                                            .subtract(withdrawnAmount)
                                    )
                                    .build();
                        }))
                .collectList();
    }

    private LoanRebateResponseDTO calculateLoanRebate(Tuple2<RebateInfoResponseDTO, Passbook> tuples) {
        RebateInfoResponseDTO rebateInfoResponseDTO = tuples.getT1();
        Passbook lastPassbookEntry = tuples.getT2();

        BigDecimal principalPaidTillDate = lastPassbookEntry.getPrinPaidTillDate();
        BigDecimal serviceChargePaidTillDate = lastPassbookEntry.getScPaidTillDate();

        BigDecimal principalRemaining = rebateInfoResponseDTO.getTotalPrincipal().subtract(principalPaidTillDate);
        BigDecimal serviceChargeRemaining = rebateInfoResponseDTO.getTotalServiceCharge().subtract(serviceChargePaidTillDate);

        return LoanRebateResponseDTO
                .builder()
                .totalLoanPayable(rebateInfoResponseDTO.getTotalPayable())
                .totalPrincipal(rebateInfoResponseDTO.getTotalPrincipal())
                .totalServiceCharge(rebateInfoResponseDTO.getTotalServiceCharge())
                .totalTransaction(principalPaidTillDate.add(serviceChargePaidTillDate))
                .totalPrincipalPaid(principalPaidTillDate)
                .totalServiceChargePaid(serviceChargePaidTillDate)
                .totalOutstandingAmount(principalRemaining.add(serviceChargeRemaining))
                .totalPrincipalRemaining(principalRemaining)
                .totalServiceChargeRemaining(serviceChargeRemaining)
                // TODO: 7/25/23 if rebateAble amount is given into input, payable after rebate will be (principalRemaining - rebateAbleAmount)
                .rebateAbleAmount(serviceChargeRemaining)
                .payableAfterRebate(principalRemaining)
                .build();
    }


    private LoanRebate addPaymentInfoIntoLoanRebate(LoanRebate loanRebate, SettleRebateRequestDto requestDto) {
        if(requestDto.getCollectionType().equalsIgnoreCase("Combine")) {
            loanRebate.setPaymentInfo(RebatePaymentInfo.builder().adjustment(buildAdjustmentLoanAccountForAMember(loanRebate, requestDto))
                    .collection(buildCollectionDetailViewForRebate(loanRebate, requestDto)).build());
        } else if(requestDto.getCollectionType().equalsIgnoreCase("Adjustment")) {
            loanRebate.setPaymentInfo(RebatePaymentInfo.builder().adjustment(buildAdjustmentLoanAccountForAMember(loanRebate, requestDto)).build());
        } else {
            loanRebate.setPaymentInfo(RebatePaymentInfo.builder().collection(buildCollectionDetailViewForRebate(loanRebate, requestDto)).build());
        }
        return loanRebate;
    }


    private AdjustedLoanAccount buildAdjustmentLoanAccountForAMember(LoanRebate loanRebate,
                                                                           SettleRebateRequestDto requestDto) {
        return AdjustedLoanAccount.builder()
                        .loanAccountId(loanRebate.getLoanAccountId())
                        .adjustmentDate(loanRebate.getCreatedOn().toLocalDate())
                        .adjustedAmount(requestDto.getAdjustedAccountList().stream()
                                .map(RebateAdjustmentAccount::getAmount)
                                .map(BigDecimal::new)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .adjustedSavingsAccountList(this
                                .buildAdjustedSavingsAccountListFromLoanAdjustmentData(
                                        requestDto.getAdjustedAccountList()))
                        .build();
    }


    private List<AdjustedSavingsAccount> buildAdjustedSavingsAccountListFromLoanAdjustmentData(
            List<RebateAdjustmentAccount> adjustmentAccounts) {
        return adjustmentAccounts.stream()
                .filter(adjustmentAccount -> !HelperUtil
                        .checkIfNullOrEmpty(adjustmentAccount.getSavingsAccountId())
                        && !HelperUtil
                        .checkIfNullOrEmpty(adjustmentAccount.getAmount()))
                .map(savingsAccountAdjustedData -> AdjustedSavingsAccount.builder()
                        .savingsAccountId(savingsAccountAdjustedData.getSavingsAccountId())
                        .amount(new BigDecimal(savingsAccountAdjustedData.getAmount()))
                        .build())
                .sorted(Comparator.comparing(AdjustedSavingsAccount::getSavingsAccountId))
                .toList();
    }

    private CollectionDetailView buildCollectionDetailViewForRebate(LoanRebate loanRebate, SettleRebateRequestDto requestDto) {
        return CollectionDetailView.builder()
                .managementProcessId(loanRebate.getManagementProcessId())
                .processId(loanRebate.getProcessId())
                .stagingDataId(loanRebate.getStagingDataId())
                .samityId(loanRebate.getSamityId())
                .accountType(ACCOUNT_TYPE_LOAN.getValue())
                .loanAccountId(loanRebate.getLoanAccountId())
                .amount(new BigDecimal(requestDto.getCollectedAmountByCash()))
                .paymentMode(PAYMENT_MODE_CASH.getValue())
                .collectionType(REBATE.getValue())
                .build();
    }

    @Override
    public Mono<SettleRebateResponseDto> resetLoanRebate(GetLoanRebateDetailRequestDto requestDto) {
        return loanRebatePersistencePort.getLoanRebateByOid(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Loan Rebate Data not found")))
                .filter(loanRebate -> loanRebate.getCreatedBy().equals(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Only the creator can reset the loan rebate data")))
                .filter(loanRebate -> loanRebate.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanRebate.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Loan Rebate Data is already submitted or approved")))
                .flatMap(this::deleteChildDataForLoanRebate)
                .flatMap(loanRebate -> switch (loanRebate.getPaymentMode().toLowerCase()) {
                    case "cash" -> checkAndDeleteCollectedSamityTracker(requestDto, loanRebate);
                    case "adjustment" -> checkAndDeleteAdjustedSamityTracker(requestDto, loanRebate);
                    case "combine" -> checkAndDeleteCollectedSamityTracker(requestDto, loanRebate)
                            .then(checkAndDeleteAdjustedSamityTracker(requestDto, loanRebate));
                    default -> Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Invalid Loan Rebate Payment Mode"));
                })
                .flatMap(loanRebate -> loanRebatePersistencePort.deleteLoanRebateByOid(loanRebate.getOid()))
                .as(rxtx::transactional)
                .map(isDeleted -> SettleRebateResponseDto.builder().userMessage("Loan Rebate Data Reset Successfully").build());
    }

    private Mono<LoanRebate> checkAndDeleteCollectedSamityTracker(GetLoanRebateDetailRequestDto requestDto, LoanRebate loanRebate) {
        return collectionStagingDataQueryUseCase.countCollectionStagingData(loanRebate.getManagementProcessId(), loanRebate.getSamityId())
                .flatMap(count -> {
                    if (count == 0) {
                        return samityEventTrackerUseCase.getLastCollectedSamityEventBySamityAndManagementProcessId(loanRebate.getSamityId(), loanRebate.getManagementProcessId())
                                .doOnNext(eventTracker -> log.info("Samity Event Tracker after collected filter : {}", eventTracker))
                                .flatMap(eventTracker -> samityEventTrackerUseCase.saveSamityEventTrackerIntoHistoryAndDeleteSamityEventTrackerData(eventTracker, requestDto.getLoginId()))
                                .doOnSuccess(loanRebate1 -> log.info("Loan Rebate Data after deleting collected samity tracker: {}", loanRebate1))
                                .thenReturn(loanRebate);
                    } else {
                        return Mono.just(loanRebate);
                    }
                });
    }

    private Mono<LoanRebate> checkAndDeleteAdjustedSamityTracker(GetLoanRebateDetailRequestDto requestDto, LoanRebate loanRebate) {
        return loanAdjustmentUseCase.countLoanAdjustmentData(loanRebate.getManagementProcessId(), loanRebate.getSamityId())
                .flatMap(count -> {
                    if (count == 0) {
                        return samityEventTrackerUseCase.getLastAdjustedSamityEventBySamityAndManagementProcessId(loanRebate.getSamityId(), loanRebate.getManagementProcessId())
                                .doOnNext(eventTracker -> log.info("Samity Event Tracker after adjusted filter : {}", eventTracker))
                                .flatMap(eventTracker -> samityEventTrackerUseCase.saveSamityEventTrackerIntoHistoryAndDeleteSamityEventTrackerData(eventTracker, requestDto.getLoginId()))
                                .doOnSuccess(loanRebate1 -> log.info("Loan Rebate Data after deleting adjusted samity tracker: {}", loanRebate1))
                                .thenReturn(loanRebate);
                    } else {
                        return Mono.just(loanRebate);
                    }
                });
    }

    private Mono<LoanRebate> deleteChildDataForLoanRebate(LoanRebate loanRebate) {
        return switch (loanRebate.getPaymentMode().toLowerCase()) {
            case "combine" ->
                    loanAdjustmentPersistencePort.deleteLoanAdjustmentByManagementProcessIdAndProcessId(loanRebate.getManagementProcessId(), loanRebate.getProcessId())
                            .then(collectionStagingDataPersistencePort.deleteCollectionDataByManagementProcessIdAndProcessId(loanRebate.getManagementProcessId(), loanRebate.getProcessId()))
                            .then(Mono.just(loanRebate));
            case "adjustment" ->
                    loanAdjustmentPersistencePort.deleteLoanAdjustmentByManagementProcessIdAndProcessId(loanRebate.getManagementProcessId(), loanRebate.getProcessId())
                            .then(Mono.just(loanRebate));
            case "cash" ->
                    collectionStagingDataPersistencePort.deleteCollectionDataByManagementProcessIdAndProcessId(loanRebate.getManagementProcessId(), loanRebate.getProcessId())
                            .then(Mono.just(loanRebate));
            default -> Mono.error(new ExceptionHandlerUtil(BAD_REQUEST, "Invalid Loan Rebate Payment Mode"));
        };
    }
}
