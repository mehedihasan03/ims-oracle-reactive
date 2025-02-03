package net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.StatusYesNo;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.ResetCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionDetailView;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberAndLoanAccountEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedLoanData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.LoanAdjustmentMemberGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.LoanAdjustmentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.RebateAdjustmentAccount;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.SettleRebateRequestDto;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.RebatePaymentInfo;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.ServiceChargeChartResponseDTO;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawStagingDataQueryDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.WithdrawPaymentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataResponseDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.WriteOffCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.dto.LoanWriteOffCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out.WriteOffClientPort;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out.WriteOffCollectionPort;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.LoanInfo;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.LoanWriteOffCollection;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.WriteOffPaymentInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.REBATE;
import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.WRITE_OFF;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
public class WriteOffCollectionService implements WriteOffCollectionUseCase {

    private final CommonRepository commonRepository;
    private final IStagingDataUseCase stagingDataUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final WriteOffCollectionPort writeOffCollectionPort;
    private final WriteOffClientPort writeOffClientPort;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final TransactionalOperator rxtx;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final LoanAccountUseCase loanAccountUseCase;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final ISmsNotificationUseCase smsNotificationUseCase;
    private final ResetCollectionUseCase resetCollectionUseCase;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final LoanAdjustmentPersistencePort loanAdjustmentPersistencePort;
    private final IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort;


    public WriteOffCollectionService(CommonRepository commonRepository,
                                     IStagingDataUseCase stagingDataUseCase,
                                     ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                                     SamityEventTrackerUseCase samityEventTrackerUseCase,
                                     OfficeEventTrackerUseCase officeEventTrackerUseCase,
                                     WriteOffCollectionPort writeOffCollectionPort,
                                     WriteOffClientPort writeOffClientPort,
                                     ModelMapper modelMapper,
                                     LoanAdjustmentUseCase loanAdjustmentUseCase,
                                     PaymentCollectionUseCase paymentCollectionUseCase,
                                     TransactionalOperator rxtx,
                                     CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase, LoanAccountUseCase loanAccountUseCase, ServiceChargeChartUseCase serviceChargeChartUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, TransactionUseCase transactionUseCase, PassbookUseCase passbookUseCase, ISmsNotificationUseCase smsNotificationUseCase, ResetCollectionUseCase resetCollectionUseCase, CollectionStagingDataPersistencePort collectionStagingDataPersistencePort, LoanAdjustmentPersistencePort loanAdjustmentPersistencePort, IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort) {
        this.commonRepository = commonRepository;
        this.stagingDataUseCase = stagingDataUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.writeOffCollectionPort = writeOffCollectionPort;
        this.writeOffClientPort = writeOffClientPort;
        this.collectionStagingDataQueryUseCase = collectionStagingDataQueryUseCase;
        this.loanAccountUseCase = loanAccountUseCase;
        this.serviceChargeChartUseCase = serviceChargeChartUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.smsNotificationUseCase = smsNotificationUseCase;
        this.resetCollectionUseCase = resetCollectionUseCase;
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.loanAdjustmentPersistencePort = loanAdjustmentPersistencePort;
        this.withdrawStagingDataPersistencePort = withdrawStagingDataPersistencePort;
        this.gson = CommonFunctions.buildGson(this);
        this.modelMapper = modelMapper;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.paymentCollectionUseCase = paymentCollectionUseCase;
        this.rxtx = rxtx;
    }

    @Override
    public Mono<WriteOffCollectionAccountDataResponseDto> getWriteOffCollectionAccountData(WriteOffCollectionAccountDataRequestDto request) {
        return commonRepository.getMemberInfoByLoanAccountId(request.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Member data not found for loan account " + request.getLoanAccountId())))
                .map(memberEntity -> WriteOffCollectionAccountDataResponseDto.builder()
                        .memberId(memberEntity.getMemberId())
                        .memberNameEn(memberEntity.getMemberNameEn())
                        .memberNameBn(memberEntity.getMemberNameBn())
                        .build())
                .flatMap(responseDto -> loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(request.getLoanAccountId())
                        .filter(loanAccountResponseDTO -> loanAccountResponseDTO.getStatus().equalsIgnoreCase(Status.STATUS_CLOSED_WRITTEN_OFF.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Not Found any Write Off Account by loan account id")))
                        .flatMap(loanAccountResponseDTO -> Mono.zip(Mono.just(loanAccountResponseDTO), Mono.just(responseDto))))
                .flatMap(loanAccountResponseDtoAndResponseDto -> stagingDataUseCase.getStagingAccountDataByLoanAccountId(loanAccountResponseDtoAndResponseDto.getT1().getLoanAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "StagingAccountData not found by loan account id")))
                        .flatMap(stagingAccountData -> buildResponseOfLoanAccountDetailsForWriteOff(loanAccountResponseDtoAndResponseDto.getT2(), loanAccountResponseDtoAndResponseDto.getT1(), stagingAccountData)));
    }

    private Mono<WriteOffCollectionAccountDataResponseDto> buildResponseOfLoanAccountDetailsForWriteOff(WriteOffCollectionAccountDataResponseDto responseDto, LoanAccountResponseDTO loanAccountResponseDTO, StagingAccountData stagingAccountData) {
        return Mono.zip(serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()),
                        commonRepository.getDisbursementDateByLoanAccountId(stagingAccountData.getLoanAccountId()).switchIfEmpty(Mono.empty()),
                        commonRepository.getMemberOfficeAndSamityEntityByMemberId(stagingAccountData.getMemberId()))
                .map(tupleOfServiceChargeDetailDisbursementDateAndOfficeSamityData -> {
                    responseDto.setSamityId(tupleOfServiceChargeDetailDisbursementDateAndOfficeSamityData.getT3().getSamityId());
                    responseDto.setOfficeId(tupleOfServiceChargeDetailDisbursementDateAndOfficeSamityData.getT3().getOfficeId());
                    responseDto.setLoanAccountId(stagingAccountData.getLoanAccountId());
                    responseDto.setLoanProductId(stagingAccountData.getProductCode());
                    responseDto.setLoanProductNameEn(stagingAccountData.getProductNameEn());
                    responseDto.setLoanProductNameBn(stagingAccountData.getProductNameBn());
                    responseDto.setLoanAmount(stagingAccountData.getLoanAmount());
                    responseDto.setServiceCharge(stagingAccountData.getServiceCharge());
                    responseDto.setTotalLoanAmount(stagingAccountData.getLoanAmount().add(stagingAccountData.getServiceCharge()));
                    responseDto.setPrincipalPaid(stagingAccountData.getTotalPrincipalPaid());
                    responseDto.setServiceChargePaid(stagingAccountData.getTotalServiceChargePaid());
                    responseDto.setTotalPrincipalPaid(stagingAccountData.getTotalPrincipalPaid());
                    responseDto.setPrincipalRemaining(stagingAccountData.getTotalPrincipalRemaining());
                    responseDto.setServiceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining());
                    responseDto.setTotalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()));
                    responseDto.setTotalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()));
                    responseDto.setServiceChargeRate(tupleOfServiceChargeDetailDisbursementDateAndOfficeSamityData.getT1().getServiceChargeRate());
                    responseDto.setLoanTerm(loanAccountResponseDTO.getLoanTerm());
                    responseDto.setNoOfInstallment(loanAccountResponseDTO.getNoInstallment());
                    responseDto.setInstallmentAmount(loanAccountResponseDTO.getInstallmentAmount());
                    responseDto.setDisbursementDate(tupleOfServiceChargeDetailDisbursementDateAndOfficeSamityData.getT2().toString());
                    responseDto.setAdvancePaid(stagingAccountData.getTotalAdvance() == null ? BigDecimal.ZERO : stagingAccountData.getTotalAdvance());
                    return responseDto;
                });
    }


    @Override
    public Mono<LoanWriteOffDetailsResponseDto> getWriteOffCollectionAccountDataV2(WriteOffCollectionAccountDataRequestDto request) {
        return this.getWriteOffCollectionAccountData(request)
                .flatMap(writeOffCollectionAccountResponseDto -> writeOffCollectionPort.getWriteOffCollectionByLoanAccountId(writeOffCollectionAccountResponseDto.getLoanAccountId())
                        .switchIfEmpty(Mono.just(LoanWriteOffCollection.builder().build()))
                        .flatMap(loanWriteOffCollection -> {
                            LoanWriteOffDetailsResponseDto response = modelMapper.map(writeOffCollectionAccountResponseDto, LoanWriteOffDetailsResponseDto.class);
                            log.info("Loan Account detail mapped into Loan Write Off details response: {}", response);
                            LoanWriteOffDetailsResponseDto responseWithBtnStatus = setBtnStatusForLoanWriteOffDetail(response, loanWriteOffCollection);
                            return setSavingsAccountDetailsForWriteOff(responseWithBtnStatus).zipWith(Mono.just(loanWriteOffCollection));
                        })
                        .flatMap(tupleOfResponseAndLoanWriteOffCollection -> HelperUtil.checkIfNullOrEmpty(tupleOfResponseAndLoanWriteOffCollection.getT2().getOid()) ? Mono.just(tupleOfResponseAndLoanWriteOffCollection.getT1()) : buildLoanWriteOffDetailsResponseDto(tupleOfResponseAndLoanWriteOffCollection.getT1(), tupleOfResponseAndLoanWriteOffCollection.getT2()))
                        .doOnError(throwable -> log.error("Error while setting Loan Write Off detail into response: {}", throwable.getMessage()))
                )
                .doOnSuccess(response -> log.info("Successfully get Loan write off detail Response: {}", response))
                .doOnError(throwable -> log.error("Error while getting Loan Write Off detail with account detail: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }


    private Mono<LoanWriteOffDetailsResponseDto> setSavingsAccountDetailsForWriteOff(LoanWriteOffDetailsResponseDto responseDto) {
        return stagingDataUseCase.getStagingAccountDataListByMemberId(responseDto.getMemberId())
                .collectList()
                .flatMap(this::getSavingsAccountDetailsForWriteOff)
                .map(savingsAccountList -> {
                    responseDto.setSavingsAccountList(savingsAccountList);
                    return responseDto;
                });
    }


    private LoanWriteOffDetailsResponseDto setBtnStatusForLoanWriteOffDetail(LoanWriteOffDetailsResponseDto response, LoanWriteOffCollection loanWriteOffCollection) {
        response.setBtnSubmitEnabled(
                StringUtils.isBlank(loanWriteOffCollection.getOid()) ? StatusYesNo.No.toString() : StringUtils.isNotBlank(loanWriteOffCollection.getIsSubmitted()) &&
                        loanWriteOffCollection.getIsSubmitted().equalsIgnoreCase(StatusYesNo.Yes.toString()) ?
                        StatusYesNo.No.toString() : StatusYesNo.Yes.toString()
        );
        response.setBtnUpdateEnabled(
                StringUtils.isBlank(loanWriteOffCollection.getOid()) ? StatusYesNo.No.toString() : StringUtils.isNotBlank(loanWriteOffCollection.getIsLocked()) && loanWriteOffCollection.getIsLocked().equalsIgnoreCase(StatusYesNo.No.toString())
                        && (loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                        ? StatusYesNo.Yes.toString()
                        : StatusYesNo.No.toString()
        );
        return response;
    }

    private Mono<LoanWriteOffDetailsResponseDto> buildLoanWriteOffDetailsResponseDto(LoanWriteOffDetailsResponseDto responseDto, LoanWriteOffCollection loanWriteOffCollection) {
        return Mono.zip(collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(loanWriteOffCollection.getLoanAccountId(), loanWriteOffCollection.getManagementProcessId(), loanWriteOffCollection.getProcessId(), String.valueOf(loanWriteOffCollection.getCurrentVersion())),
                        loanAdjustmentUseCase.getAdjustedLoanAccountListByManagementProcessId(LoanAdjustmentRequestDTO.builder()
                                .managementProcessId(loanWriteOffCollection.getManagementProcessId())
                                .processId(loanWriteOffCollection.getProcessId())
                                .memberId(loanWriteOffCollection.getMemberId())
                                .currentVersion(loanWriteOffCollection.getCurrentVersion()).build()),
                        stagingDataUseCase.getStagingAccountDataListByMemberId(loanWriteOffCollection.getMemberId()).collectList()
                )
                .map(tupleOfCollcetionAdjustmentStagingAccountDataList -> {
                    responseDto.setPaymentMethod(loanWriteOffCollection.getPaymentMode());
                    responseDto.setWriteOffCollectionDate(loanWriteOffCollection.getLoanWriteOffCollectionDate());
                    responseDto.setWriteOffCollectionAmount(loanWriteOffCollection.getWriteOffCollectionAmount());
                    responseDto.setStatus(loanWriteOffCollection.getStatus());
                    responseDto.setRemarks(loanWriteOffCollection.getRemarks());
                    responseDto.setRejectedBy(loanWriteOffCollection.getRejectedBy());
                    responseDto.setRejectedOn(loanWriteOffCollection.getRejectedOn());
                    responseDto.setSubmittedBy(loanWriteOffCollection.getSubmittedBy());
                    responseDto.setSubmittedOn(loanWriteOffCollection.getSubmittedOn());
                    responseDto.setApprovedBy(loanWriteOffCollection.getApprovedBy());
                    responseDto.setApprovedOn(loanWriteOffCollection.getApprovedOn());
                    responseDto.setPayableAmountAfterWriteOffCollection((responseDto.getPrincipalRemaining().add(responseDto.getServiceChargeRemaining()).subtract(loanWriteOffCollection.getWriteOffCollectionAmount())));
//                    responseDto.setCollection(LoanWriteOffCashCollection.builder()
//                            .oid(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getOid())
//                            .collectionStagingDataId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getCollectionStagingDataId())
//                            .managementProcessId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getManagementProcessId())
//                            .processId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getProcessId())
//                            .stagingDataId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getStagingDataId())
//                            .samityId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getSamityId())
//                            .accountType(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getAccountType())
//                            .loanAccountId(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getLoanAccountId())
//                            .amount(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getAmount())
//                            .paymentMode(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getPaymentMode())
//                            .collectionType(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getCollectionType())
//                            .status(tupleOfCollcetionAdjustmentStagingAccountDataList.getT1().getStatus())
//                            .build());
                    responseDto.setCollection(ObjectUtils.isEmpty(loanWriteOffCollection.getPaymentInfo().getCollection()) ? null : loanWriteOffCollection.getPaymentInfo().getCollection());
//                    responseDto.setAdjustedLoanAccountList(tupleOfCollcetionAdjustmentStagingAccountDataList.getT2().getAdjustedLoanAccountList());
                    responseDto.setAdjustedLoanAccountList(ObjectUtils.isEmpty(loanWriteOffCollection.getPaymentInfo().getAdjustment()) ? new ArrayList<>() : List.of(loanWriteOffCollection.getPaymentInfo().getAdjustment()));
                    responseDto.setLoanWriteOffOid(loanWriteOffCollection.getOid());
                    return responseDto;
                });
    }


    @Override
    public Mono<LoanWriteOffMsgCommonResponseDto> createWriteOffCollection(WriteOffCollectionAccountDataRequestDto request) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .doOnNext(managementProcessTrackerAtomicReference::set)
                .flatMap(managementProcessTracker -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(), request.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized.")))
                        .flatMap(samityEventList -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), request.getOfficeId())
                                .filter(officeEventTracker -> HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                                .map(OfficeEventTracker::getOfficeEvent)
                                .collectList()
                                .filter(officeEvenetList -> officeEvenetList.isEmpty() || officeEvenetList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is not Generated yet.")))
                                .map(officeEvenetList -> managementProcessTracker)))
                .flatMap(managementProcessTracker -> getWriteOffCollectionAccountData(request)
                        .filter(data -> validateCollectionAmount(request, data.getTotalDue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Total collected amount should be valid for write off collection.")))
                        .map(responseDto -> buildWriteOffCollectionDomain(request, responseDto, managementProcessTracker))
                        .flatMap(writeOffCollection -> writeOffCollectionPort.saveWriteOffCollection(addPaymentInfoIntoLoanWriteOff(writeOffCollection, request)))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Error while saving write off collection data")))
                        .flatMap(writeOffCollection -> saveCollectionData(request, managementProcessTracker, writeOffCollection))
                        .map(loanAdjustmentResponseDTO -> LoanWriteOffMsgCommonResponseDto.builder()
                                .userMessage("write-off collection is Successfully Created")
                                .build())
                        .as(rxtx::transactional)
                )
                .doOnSuccess(response -> log.info("Loan write off collection success Response: {}", response))
                .doOnError(throwable -> log.error("Error while creating Loan Write Off Collection: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    private Mono<?> saveCollectionData(WriteOffCollectionAccountDataRequestDto request, ManagementProcessTracker managementProcessTracker, LoanWriteOffCollection writeOffCollection) {
        return switch (request.getCollectionType()) {
            case "Cash" -> savePaymentCollection(request, writeOffCollection);
            case "Adjustment" -> saveLoanAdjustment(request, managementProcessTracker, writeOffCollection);
            case "Combine" ->
                    savePaymentCollection(request, writeOffCollection).then(saveLoanAdjustment(request, managementProcessTracker, writeOffCollection));
            default -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid Collection Type"));
        };
    }

    private Mono<CollectionMessageResponseDTO> savePaymentCollection(WriteOffCollectionAccountDataRequestDto request,
                                                                     LoanWriteOffCollection writeOffCollection) {
        return stagingDataUseCase.getStagingDataByAccountId(request.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "StagingData not found by loan account id")))
                .flatMap(stagingData -> paymentCollectionUseCase.collectPaymentBySamity(buildPaymentCollectionBySamity(request, writeOffCollection, stagingData.getStagingDataId())));
    }

    private Mono<LoanAdjustmentResponseDTO> saveLoanAdjustment(WriteOffCollectionAccountDataRequestDto request,
                                                               ManagementProcessTracker managementProcessTracker,
                                                               LoanWriteOffCollection writeOffCollection) {
        return loanAdjustmentUseCase.createLoanAdjustmentForMember(buildLoanAdjustmentRequest(request, managementProcessTracker, writeOffCollection));
    }

    private LoanAdjustmentRequestDTO buildLoanAdjustmentRequest(WriteOffCollectionAccountDataRequestDto request,
                                                                ManagementProcessTracker managementProcessTracker,
                                                                LoanWriteOffCollection writeOffCollection) {
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .processId(writeOffCollection.getProcessId())
                .officeId(request.getOfficeId())
                .loginId(request.getLoginId())
                .mfiId(request.getMfiId())
                .samityId(request.getSamityId())
                .memberId(writeOffCollection.getMemberId())
                .adjustmentType(WRITE_OFF.getValue())
                .data(Collections.singletonList(AdjustedLoanData.builder()
                        .loanAccountId(request.getLoanAccountId())
                        .adjustedAccountList(request.getAdjustedAccountList().stream()
                                .map(src -> modelMapper.map(src, AdjustedAccount.class)).toList())
                        .build()))
                .build();
    }

    private PaymentCollectionBySamityCommand buildPaymentCollectionBySamity(WriteOffCollectionAccountDataRequestDto request,
                                                                            LoanWriteOffCollection writeOffCollection,
                                                                            String stagingDataId) {
        return PaymentCollectionBySamityCommand.builder()
                .mfiId(request.getMfiId())
                .loginId(request.getLoginId())
                .officeId(request.getOfficeId())
                .managementProcessId(writeOffCollection.getManagementProcessId())
                .processId(writeOffCollection.getProcessId())
                .samityId(request.getSamityId())
                .collectionType(WRITE_OFF.getValue())
                .data(Collections.singletonList(CollectionData.builder()
                        .stagingDataId(stagingDataId)
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(writeOffCollection.getLoanAccountId())
                        .amount(request.getCollectedAmountByCash())
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .collectionType(WRITE_OFF.getValue())
                        .build()))
                .build();
    }

    private LoanWriteOffCollection buildWriteOffCollectionDomain(WriteOffCollectionAccountDataRequestDto request,
                                                                 WriteOffCollectionAccountDataResponseDto accountDataResponseDto,
                                                                 ManagementProcessTracker managementProcessTracker) {
        return LoanWriteOffCollection.builder()
                .loanWriteOffCollectionDataId(UUID.randomUUID().toString())
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .processId(UUID.randomUUID().toString())
                .samityId(request.getSamityId())
                .loanAccountId(accountDataResponseDto.getLoanAccountId())
                .memberId(accountDataResponseDto.getMemberId())
                .writeOffCollectionAmount(request.getWriteOffCollectionAmount())
                .paymentMode(request.getCollectionType())
                .loanInfo(gson.toJson(buildLoanInfo(accountDataResponseDto)))
                .isNew(StatusYesNo.Yes.toString())
                .currentVersion(1)
                .status(Status.STATUS_STAGED.getValue())
                .createdOn(LocalDateTime.now())
                .createdBy(request.getLoginId())
                .loanWriteOffCollectionDate(managementProcessTracker.getBusinessDate())
                .isLocked(StatusYesNo.No.toString())
                .build();
    }

    private Boolean validateCollectionAmount(WriteOffCollectionAccountDataRequestDto request, BigDecimal totalDue) {
        switch (request.getCollectionType()) {
            case "Cash" -> {
                return validateCashCollectionAmount(request, totalDue);
            }
            case "Adjustment" -> {
                return validateAdjustmentCollectionAmount(request, totalDue);
            }
            case "Combine" -> {
                return validateCombineCollectionAmount(request, totalDue);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean validateCashCollectionAmount(WriteOffCollectionAccountDataRequestDto request, BigDecimal totalDue) {
        return request.getCollectionType().equalsIgnoreCase("Cash") &&
                request.getWriteOffCollectionAmount() != null &&
                request.getWriteOffCollectionAmount().compareTo(BigDecimal.ZERO) > 0 &&
                request.getCollectedAmountByCash().equals(request.getWriteOffCollectionAmount()) &&
                request.getWriteOffCollectionAmount().compareTo(totalDue) <= 0;
    }

    private boolean validateAdjustmentCollectionAmount(WriteOffCollectionAccountDataRequestDto request, BigDecimal totalDue) {
        return request.getCollectionType().equalsIgnoreCase("Adjustment") &&
                request.getWriteOffCollectionAmount() != null &&
                request.getWriteOffCollectionAmount().compareTo(BigDecimal.ZERO) > 0 &&
                request.getAdjustedAccountList().stream().map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).equals(request.getWriteOffCollectionAmount()) &&
                request.getAdjustedAccountList().stream().map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(totalDue) <= 0;
    }

    private boolean validateCombineCollectionAmount(WriteOffCollectionAccountDataRequestDto request, BigDecimal totalDue) {
        return (request.getCollectionType().equalsIgnoreCase("Combine") &&
                request.getWriteOffCollectionAmount() != null &&
                request.getWriteOffCollectionAmount().compareTo(BigDecimal.ZERO) > 0 &&
                request.getCollectedAmountByCash().add(request.getAdjustedAccountList().stream().map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)).compareTo(request.getWriteOffCollectionAmount()) == 0 &&
                request.getWriteOffCollectionAmount().compareTo(totalDue) <= 0) &&
                request.getCollectedAmountByCash().compareTo(BigDecimal.ZERO) > 0 &&
                request.getAdjustedAccountList().stream().map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(BigDecimal.ZERO) > 0;
    }

    private LoanInfo buildLoanInfo(WriteOffCollectionAccountDataResponseDto accountDataResponseDto) {
        return LoanInfo.builder()
                .loanAccountId(accountDataResponseDto.getLoanAccountId())
                .loanProductId(accountDataResponseDto.getLoanProductId())
                .loanProductNameEn(accountDataResponseDto.getLoanProductNameEn())
                .loanProductNameBn(accountDataResponseDto.getLoanProductNameBn())
                .loanAmount(accountDataResponseDto.getLoanAmount())
                .serviceCharge(accountDataResponseDto.getServiceCharge())
                .totalLoanAmount(accountDataResponseDto.getTotalLoanAmount())
                .principalPaid(accountDataResponseDto.getPrincipalPaid())
                .serviceChargePaid(accountDataResponseDto.getServiceChargePaid())
                .totalPrincipalPaid(accountDataResponseDto.getTotalPrincipalPaid())
                .principalRemaining(accountDataResponseDto.getPrincipalRemaining())
                .serviceChargeRemaining(accountDataResponseDto.getServiceChargeRemaining())
                .totalDue(accountDataResponseDto.getTotalDue())
                .build();
    }

    @Override
    public Mono<LoanWriteOffMsgCommonResponseDto> updateWriteOffCollection(WriteOffCollectionAccountDataRequestDto request) {
        return writeOffCollectionPort.getWriteOffCollectionById(request.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Write Off Collection data not found by id : " + request.getId())))
                .filter(writeOffCollection -> !HelperUtil.checkIfNullOrEmpty(writeOffCollection.getCreatedBy()) && writeOffCollection.getCreatedBy().equals(request.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Write Off Collection data can only be updated by creator")))
                .filter(writeOffCollection -> !HelperUtil.checkIfNullOrEmpty(writeOffCollection.getStatus()) && !writeOffCollection.getStatus().equals(Status.STATUS_SUBMITTED.getValue()) && !writeOffCollection.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Write Off Collection data is already submitted or approved")))
                .flatMap(writeOffCollection -> validateAgainstSamityAndOfficeEventTracker(writeOffCollection, request))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "write off collection data is not valid for update")))
                .filter(writeOffCollection -> {
                    BigDecimal totalDue = gson.fromJson(writeOffCollection.getLoanInfo(), LoanInfo.class).getTotalDue();
                    return validateCollectionAmount(request, totalDue);
                })
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Total collected amount should be valid for write off collection.")))
                .flatMap(loanWriteOffCollection -> Mono.just(loanWriteOffCollection)
                        .zipWith(stagingDataUseCase
                                .getStagingDataByMemberId(loanWriteOffCollection.getMemberId())
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Staging Data not found by member id")))))
                .flatMap(tuple -> {
                    LoanWriteOffCollection writeOffCollectionHistory = buildWriteOffCollectionHistory(gson.fromJson(tuple.getT1().toString(), LoanWriteOffCollection.class));
                    log.info("Write Off Collection Data for history : {}", writeOffCollectionHistory);
                    LoanWriteOffCollection writeOffCollection = buildUpdatedLoanWriteOffCollection(tuple.getT1(), request);
                    log.info("Write Off Collection Data : {}", writeOffCollection);
                    return writeOffCollectionPort.saveWriteOffCollectionHistory(addPaymentInfoIntoLoanWriteOff(writeOffCollectionHistory, request))
                            .then(writeOffCollectionPort.saveWriteOffCollection(addPaymentInfoIntoLoanWriteOff(writeOffCollection, request)))
                            .then(updateLoanAdjustmentAndPaymentCollection(writeOffCollection, request, writeOffCollectionHistory.getPaymentMode(), tuple.getT2()));
                })
                .map(response -> LoanWriteOffMsgCommonResponseDto.builder()
                        .userMessage("write-off collection is Successfully Updated")
                        .build())
                .as(rxtx::transactional)
                .doOnNext(response -> log.info("Loan Write off collection update Response: {}", response))
                .doOnError(throwable -> log.error("Error Updating Loan Write off collection: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    public Mono<String> updateLoanAdjustmentAndPaymentCollection(LoanWriteOffCollection writeOffCollection,
                                                                 WriteOffCollectionAccountDataRequestDto request,
                                                                 String collectionType, StagingData stagingData) {
        collectionType = collectionType.trim().toUpperCase();
        String newCollectionType = request.getCollectionType().trim().toUpperCase();
        log.info("Original Type: {}, New Type: {}", collectionType, newCollectionType);
        return switch (CollectionTypeChange.valueOf(collectionType + "_TO_" + newCollectionType)) {
            case CASH_TO_CASH -> paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .map(data -> "Collection Payment Data Updated Successfully");
            case CASH_TO_ADJUSTMENT -> paymentCollectionUseCase.removeCollectionPayment(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .then(loanAdjustmentUseCase.createLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection)))
                    .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Created Successfully");
            case CASH_TO_COMBINE -> paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .then(loanAdjustmentUseCase.createLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection)))
                    .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Created Successfully");
            case ADJUSTMENT_TO_CASH ->
                    loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection))
                            .then(paymentCollectionUseCase.collectPaymentBySamity(buildPaymentCollectionBySamity(request, writeOffCollection, stagingData.getStagingDataId())))
                            .map(data -> "Loan Adjustment Data Deleted and Collection Payment Data Created Successfully");
            case ADJUSTMENT_TO_ADJUSTMENT -> loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection))
                    .map(data -> "Loan Adjustment Data Updated Successfully");
            case ADJUSTMENT_TO_COMBINE -> loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection))
                    .then(paymentCollectionUseCase.collectPaymentBySamity(
                            buildPaymentCollectionBySamity(request, writeOffCollection, stagingData.getStagingDataId())))
                    .map(data -> "Loan Adjustment Data Updated and Collection Payment Data Created Successfully");
            case COMBINE_TO_CASH -> paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .then(loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection)))
                    .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Deleted Successfully");
            case COMBINE_TO_ADJUSTMENT -> paymentCollectionUseCase.removeCollectionPayment(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .then(loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection)))
                    .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Updated Successfully");
            case COMBINE_TO_COMBINE -> paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                            buildUpdatePaymentCollectionBySamityCommand(request, writeOffCollection, stagingData))
                    .then(loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                            buildUpdateLoanAdjustmentRequestDTO(request, writeOffCollection)))
                    .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Updated Successfully");
            default -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, "Invalid Collection Type Change"));
        };
    }


    private Mono<LoanWriteOffCollection> validateAgainstSamityAndOfficeEventTracker(LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto requestDto) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(writeOffCollection.getManagementProcessId(), writeOffCollection.getSamityId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Samity Event Found for Samity Id: " + writeOffCollection.getSamityId())))
                .collectList()
                .filter(samityEventList -> samityEventList.stream().noneMatch(samityEvent -> samityEvent.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event is already 'Authorized'")))
                .filter(samityEventList -> samityEventList.stream().noneMatch(samityEvent -> samityEvent.getSamityEvent().equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event is Already Canceled")))
                .flatMap(samityEvent -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(writeOffCollection.getManagementProcessId(), requestDto.getOfficeId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Office Event not found for office id : " + requestDto.getOfficeId())))
                        .collectList()
                        .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                                .anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                        .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                                .noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
                        .map(officeEventTrackerList -> writeOffCollection));
    }


    private LoanWriteOffCollection buildWriteOffCollectionHistory(LoanWriteOffCollection writeOffCollection) {
        writeOffCollection.setLoanWriteOffCollectionOid(writeOffCollection.getOid());
        writeOffCollection.setOid(null);
        return writeOffCollection;
    }

    private PaymentCollectionBySamityCommand buildUpdatePaymentCollectionBySamityCommand(WriteOffCollectionAccountDataRequestDto request, LoanWriteOffCollection writeOffCollection, StagingData stagingData) {
        return PaymentCollectionBySamityCommand.builder()
                .managementProcessId(writeOffCollection.getManagementProcessId())
                .processId(writeOffCollection.getProcessId())
                .mfiId(request.getMfiId())
                .officeId(request.getOfficeId())
                .loginId(request.getLoginId())
                .collectionType(WRITE_OFF.getValue())
                .data(Collections.singletonList(CollectionData.builder()
                        .stagingDataId(stagingData.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(request.getLoanAccountId())
                        .amount(request.getCollectedAmountByCash())
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .collectionType(WRITE_OFF.getValue())
                        .currentVersion(writeOffCollection.getCurrentVersion())
                        .build()))
                .build();
    }

    private LoanAdjustmentRequestDTO buildUpdateLoanAdjustmentRequestDTO(WriteOffCollectionAccountDataRequestDto request, LoanWriteOffCollection writeOffCollection) {
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(writeOffCollection.getManagementProcessId())
                .processId(writeOffCollection.getProcessId())
                .mfiId(request.getMfiId())
                .officeId(request.getOfficeId())
                .loginId(request.getLoginId())
                .samityId(writeOffCollection.getSamityId())
                .memberId(writeOffCollection.getMemberId())
                .currentVersion(writeOffCollection.getCurrentVersion())
                .adjustmentType(WRITE_OFF.getValue())
                .data(Collections.singletonList(AdjustedLoanData.builder()
                        .loanAccountId(request.getLoanAccountId())
                        .adjustedAccountList(request.getAdjustedAccountList().stream()
                                .map(src -> modelMapper.map(src, AdjustedAccount.class)).toList()).build()))
                .build();
    }

    private LoanWriteOffCollection buildUpdatedLoanWriteOffCollection(LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto request) {
        writeOffCollection.setUpdatedBy(request.getLoginId());
        writeOffCollection.setUpdatedOn(LocalDateTime.now());
        writeOffCollection.setCurrentVersion(writeOffCollection.getCurrentVersion() + 1);
        writeOffCollection.setIsNew(StatusYesNo.No.toString());
        writeOffCollection.setLoanAccountId(request.getLoanAccountId());
        writeOffCollection.setWriteOffCollectionAmount(request.getWriteOffCollectionAmount());
        writeOffCollection.setPaymentMode(request.getCollectionType());
        return writeOffCollection;
    }

    @Override
    public Mono<LoanWriteOffMsgCommonResponseDto> submitLoanWriteOffCollectionData(WriteOffCollectionAccountDataRequestDto requestDto) {
        return writeOffCollectionPort.getWriteOffCollectionById(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No Loan write off collection data Found")))
                .doOnSuccess(writeOffCollectionData -> log.info("Loan write off collection data Found: {} by oid {}", writeOffCollectionData, requestDto.getId()))
                .filter(writeOffCollection -> !HelperUtil.checkIfNullOrEmpty(writeOffCollection.getCreatedBy()) && writeOffCollection.getCreatedBy().equals(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Write Off Collection data can only be submitted by creator")))
                .filter(writeOffCollection -> !writeOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_SUBMITTED.getValue()) || !writeOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan write off collection data is already submitted or approved")))
                .flatMap(writeOffCollection -> Mono.zip(
                        writeOffCollectionPort.saveWriteOffCollection(updateStatusToSubmitLoanWriteOffCollectionDataForAuthorization(writeOffCollection, requestDto.getLoginId())),
                        loanAdjustmentUseCase.submitLoanAdjustmentDataForAuthorization(writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId(), requestDto.getLoginId())
                                .onErrorResume(error -> Mono.empty()),
                        paymentCollectionUseCase.submitCollectionPaymentForAuthorization(writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId(), requestDto.getLoginId())
                                .onErrorResume(error -> Mono.empty())
                ))
                .map(tuple -> LoanWriteOffMsgCommonResponseDto.builder()
                        .userMessage("Loan Write Off Collection Is Successfully Submitted")
                        .build())
                .as(rxtx::transactional)
                .doOnSuccess(response -> log.info("Loan Write Off Collection submit Response: {}", response))
                .doOnError(throwable -> log.error("Error Submitting Loan Write Off Collection : {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    private LoanWriteOffCollection updateStatusToSubmitLoanWriteOffCollectionDataForAuthorization(LoanWriteOffCollection writeOffCollection, String loginId) {
        writeOffCollection.setStatus(Status.STATUS_SUBMITTED.getValue());
        writeOffCollection.setIsSubmitted(StatusYesNo.Yes.toString());
        writeOffCollection.setSubmittedBy(loginId);
        writeOffCollection.setSubmittedOn(LocalDateTime.now());
        return writeOffCollection;
    }

    @Override
    public Mono<LoanWriteOffDetailsResponseDto> getDetailsCollectedWriteOffData(WriteOffCollectionAccountDataRequestDto request) {
        return writeOffCollectionPort.getDetailsOfLoanWriteOffCollection(request.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No Write Off Collection Details Found By Id")))
                .flatMap(loanWriteOffCollection -> {
                    log.info("Loan Write off collection Response : {}", loanWriteOffCollection.getPaymentMode());
                    return commonRepository.getMemberInfoByLoanAccountId(loanWriteOffCollection.getLoanAccountId())
                            .zipWhen(memberEntity ->
                                    Mono.zip(Mono.just(loanWriteOffCollection),
                                            stagingDataUseCase.getStagingAccountDataListByMemberId(loanWriteOffCollection.getMemberId()).collectList(),
                                            collectionStagingDataQueryUseCase.getStagingAccountDataByLoanAccountId(loanWriteOffCollection.getLoanAccountId(), loanWriteOffCollection.getManagementProcessId())
                                                    .map(stagingAccountData -> modelMapper.map(stagingAccountData, StagingAccountData.class)),
                                            loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanWriteOffCollection.getLoanAccountId()),
                                            serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(loanWriteOffCollection.getLoanAccountId()),
                                            commonRepository.getDisbursementDateByLoanAccountId(loanWriteOffCollection.getLoanAccountId()).switchIfEmpty(Mono.empty())
                                    ))
                            .flatMap(tuple -> {
                                MemberEntity memberEntity = tuple.getT1();
                                LoanWriteOffCollection loanWriteOff = tuple.getT2().getT1();
                                List<StagingAccountData> stagingAccountDataList = tuple.getT2().getT2();
                                StagingAccountData stagingAccountData = tuple.getT2().getT3();
                                LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO = tuple.getT2().getT4();
                                ServiceChargeChartResponseDTO serviceChargeChartResponseDTO = tuple.getT2().getT5();
                                LocalDate disbursementDate = tuple.getT2().getT6();
                                log.info("Payment Mode is : {}", loanWriteOff.getPaymentMode());

                                return switch (loanWriteOff.getPaymentMode()) {
                                    case "Combine" ->
                                            buildCombineResponse(memberEntity, loanWriteOff, request, stagingAccountDataList, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO);
                                    case "Adjustment" ->
                                            buildAdjustmentResponse(memberEntity, loanWriteOff, request, stagingAccountDataList, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO);
                                    case "Cash" ->
                                            buildCashResponse(memberEntity, loanWriteOff, stagingAccountDataList, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO);
                                    default ->
                                            Mono.error(new IllegalArgumentException("Invalid payment mode: " + loanWriteOff.getPaymentMode()));
                                };
                            });
                });
    }

    @Override
    public Mono<List<LoanWriteOffCollectionDTO>> getLoanWriteOffDataBySamityId(String samityId, String managementProcessId) {
        return writeOffCollectionPort.getAllWrittenOffCollectionDataByManagementProcessId(managementProcessId)
                .flatMapMany(Flux::fromIterable)
                .filter(loanWriteOffCollectionEntity -> loanWriteOffCollectionEntity.getSamityId().equals(samityId))
                .map(loanWriteOffCollectionEntity -> modelMapper.map(loanWriteOffCollectionEntity, LoanWriteOffCollectionDTO.class))
                .collectList();
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return writeOffCollectionPort.lockSamityForAuthorization(samityId, managementProcessId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return writeOffCollectionPort.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return writeOffCollectionPort.getSamityIdListLockedByUserForAuthorization(loginId);
    }

    @Override
    public Mono<List<LoanWriteOffCollectionDTO>> getAllLoanWriteOffCollectionDataBySamityIdList(List<String> samityIdList) {
        return writeOffCollectionPort.getAllLoanWriteOffCollectionDataBySamityIdList(samityIdList);
    }

    @Override
    public Mono<String> validateAndUpdateLoanWriteOffCOllectionDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return writeOffCollectionPort.validateAndUpdateLoanWriteOffCollectionDataForRejectionBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<String> authorizeSamityForLoanWriteOff(LoanWriteOffAuthorizationCommand command) {
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
                        .validateAndUpdateLoanWriteOffDataForAuthorization(command))
                .doOnNext(loanWriteOffCollectionList -> log.info("Loan Write Off Data List: {}",
                        loanWriteOffCollectionList))
//                .flatMap(loanWriteOffDataList ->
//                        loanRepaymentScheduleUseCase
//                                .archiveAndUpdateRepaymentScheduleForLoanRebate(loanWriteOffDataList)
//                                .map(aBoolean -> loanWriteOffDataList))
                .flatMap(loanWriteOffCollectionList -> this.createTransactionForLoanWriteOff(loanWriteOffCollectionList, managementProcess.get().getManagementProcessId(),
                                command.getMfiId(), command.getOfficeId(), command.getLoginId(), transactionProcessId, command.getSamityId())
                        .flatMap(transactionList -> this.createSMSNotificationEntryForLoanWriteOff(transactionList, command.getSmsNotificationMetaPropertyList()))
                        .flatMap(transactionList -> this.createPassbookEntryForLoanWriteOff(transactionList, command.getLoginId(), managementProcess.get().getManagementProcessId(), passbookProcessId)))
                .flatMap(passbookResponseDTOS -> writeOffCollectionPort.updateStatusOfLoanWriteOffDataForAuthorization(command.getSamityId(), command.getLoginId(), command.getManagementProcessId()))
                .as(rxtx::transactional)
                .map(data -> "Loan Write Off Authorization Successful for Samity")
                .doOnNext(response -> log.info("Loan Write Off Authorization Response: {}", response))
                .doOnError(throwable -> log.error("Error in Loan Write Off Authorization: {}",
                        throwable.getMessage()));
    }


    @Override
    public Mono<LoanWriteOffCollection> updateLoanWriteOffDataOnUnAuthorization(LoanWriteOffCollectionDTO writeOffCollection) {
        return writeOffCollectionPort.updateLoanWriteOffDataOnUnAuthorization(writeOffCollection)
                .flatMap(collection -> loanAccountUseCase.updateLoanAccountStatus(collection.getLoanAccountId(), Status.STATUS_CLOSED_WRITTEN_OFF.getValue())
                        .thenReturn(collection));
    }

    @Override
    public Mono<LoanWriteOffGridViewByOfficeResponseDto> getWriteOffEligibleAccountList(LoanWriteOffGridViewByOfficeRequestDto request) {
        return commonRepository.getAllMemberEntityByOfficeId(request.getOfficeId())
                .flatMap(member -> commonRepository.getSamityByMemberId(member.getMemberId())
                        .zipWith(stagingDataUseCase.getStagingAccountDataListByMemberId(member.getMemberId()).collectList())
                        .flatMapMany(samityAndStagingAccountDataList -> loanAccountUseCase.getAllLoanAccountsByMemberIdAndStatus(member.getMemberId(), Status.STATUS_CLOSED_WRITTEN_OFF.getValue())
                                .filter(loanAccount -> samityAndStagingAccountDataList.getT2().stream().anyMatch(stagingAccountData -> StringUtils.isNotBlank(stagingAccountData.getLoanAccountId()) && stagingAccountData.getLoanAccountId().equals(loanAccount.getLoanAccountId())))
                                .zipWith(Mono.just(samityAndStagingAccountDataList.getT1())))
                        .flatMap(loanAccountAndSamity -> buildWriteOffEligibleLoanAccountGridData(member, loanAccountAndSamity.getT2(), loanAccountAndSamity.getT1()))
                )
                .skip((long) request.getOffset() * request.getLimit())
                .take(request.getLimit())
                .collectList()
                .zipWith(managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId()))
                .map(listOfLoanWriteOffGridDataAndManagementProcessTracker -> LoanWriteOffGridViewByOfficeResponseDto.builder()
                        .officeId(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeId())
                        .officeNameBn(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeNameBn())
                        .officeNameEn(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeNameEn())
                        .data(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT1())
                        .totalCount(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT1().size())
                        .build())
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())));
    }

    @Override
    public Mono<LoanWriteOffMsgCommonResponseDto> deleteWriteOffData(WriteOffCollectionAccountDataRequestDto requestDTO) {
        return writeOffCollectionPort.getWriteOffCollectionById(requestDTO.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No write off collection data found for oid: " + requestDTO.getId())))
                .filter(loanWriteOffCollection -> loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Write Off data is not eligible to reset!")))
                .flatMap(writeOffCollection -> writeOffCollectionPort.deleteWriteOffCollectionDataByOid(writeOffCollection.getOid())
                        .then(this.deleteWriteOffRelatedData(writeOffCollection))
                )
                .as(rxtx::transactional)
                .doOnSuccess(responseDTO -> log.info("Write Off data deleted successfully"))
                .thenReturn(LoanWriteOffMsgCommonResponseDto.builder()
                        .userMessage("Write Off data deleted successfully.")
                        .build())
                .doOnError(throwable -> log.error("Error in delete write off data: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), throwable -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.")));
    }


    private Mono<Void> deleteWriteOffRelatedData(LoanWriteOffCollection writeOffCollection) {
        if (writeOffCollection.getPaymentMode().equalsIgnoreCase("Cash")) {
            return collectionStagingDataQueryUseCase.getCollectionStagingDataByManagementProcessIdAndProcessId(writeOffCollection.getLoanAccountId(), writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId())
                    .flatMap(collectionStagingData -> resetCollectionUseCase.resetSpecialCollection(collectionStagingData.getOid()))
                    .then();
        } else if (writeOffCollection.getPaymentMode().equalsIgnoreCase("Adjustment")) {
            return loanAdjustmentUseCase.getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId())
                    .flatMap(loanAdjustment -> loanAdjustment.isEmpty() ? Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No loan adjustment data found to delete")) : loanAdjustmentUseCase.resetLoanAdjustmentDataByEntity(LoanAdjustmentRequestDTO.builder().id(loanAdjustment.get(0).getOid()).build()))
                    .then();
        } else {
            return collectionStagingDataQueryUseCase.getCollectionStagingDataByManagementProcessIdAndProcessId(writeOffCollection.getLoanAccountId(), writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId())
                    .flatMap(collectionStagingData -> resetCollectionUseCase.resetSpecialCollection(collectionStagingData.getOid()))
                    .then(loanAdjustmentUseCase.getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId()))
                    .flatMap(loanAdjustment -> loanAdjustment.isEmpty() ? Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No loan adjustment data found to delete")) : loanAdjustmentUseCase.resetLoanAdjustmentDataByEntity(LoanAdjustmentRequestDTO.builder().id(loanAdjustment.get(0).getOid()).build()))
                    .as(rxtx::transactional)
                    .then();
        }
    }


    private Mono<LoanWriteOffGridData> buildWriteOffEligibleLoanAccountGridData(MemberEntity member, Samity samity, LoanAccount loanAccount) {
        return writeOffCollectionPort.getWriteOffCollectionByLoanAccountId(loanAccount.getLoanAccountId())
                .switchIfEmpty(Mono.just(LoanWriteOffCollection.builder().build()))
                .map(loanWriteOffCollection -> LoanWriteOffGridData.builder()
                        .memberId(member.getMemberId())
                        .memberNameBn(member.getMemberNameBn())
                        .memberNameEn(member.getMemberNameEn())
                        .samityId(samity.getSamityId())
                        .samityNameBn(samity.getSamityNameBn())
                        .samityNameEn(samity.getSamityNameEn())
                        .loanAmount(loanAccount.getLoanAmount())
                        .loanAccountId(loanAccount.getLoanAccountId())
                        .btnSubmitEnabled(
                                StringUtils.isBlank(loanWriteOffCollection.getOid()) ? StatusYesNo.No.toString() : StringUtils.isNotBlank(loanWriteOffCollection.getIsSubmitted()) &&
                                        loanWriteOffCollection.getIsSubmitted().equalsIgnoreCase(StatusYesNo.Yes.toString()) ?
                                        StatusYesNo.No.toString() : StatusYesNo.Yes.toString()
                        )
                        .btnUpdateEnabled(
                                StringUtils.isBlank(loanWriteOffCollection.getOid()) ? StatusYesNo.No.toString() : StringUtils.isNotBlank(loanWriteOffCollection.getIsLocked()) && loanWriteOffCollection.getIsLocked().equalsIgnoreCase(StatusYesNo.No.toString())
                                        && (loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                                        ? StatusYesNo.Yes.toString()
                                        : StatusYesNo.No.toString()
                        )
                        .build());
    }


    private Mono<List<PassbookResponseDTO>> createPassbookEntryForLoanWriteOff(
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
                .filter(passbookResponseDTO -> passbookResponseDTO.get(0).getTransactionCode().equals(TransactionCodes.LOAN_WRITE_OFF.getValue()))
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
            return passbookResponseDTO.getScRemainForThisInst().toString().equals("0.00") && passbookResponseDTO.getPrinRemainForThisInst().toString().equals("0.00");
        } else return false;
    }


    private Mono<List<Transaction>> createTransactionForLoanWriteOff(List<LoanWriteOffCollection> loanWriteOffCollectionList,
                                                                     String managementProcessId, String mfiId, String officeId,
                                                                     String loginId, String transactionProcessId, String samityId) {
        return managementProcessTrackerUseCase
                .getCurrentBusinessDateForOffice(managementProcessId, officeId)
                .flatMapMany(businessDate -> Flux.fromIterable(loanWriteOffCollectionList)
                        .map(writeOffCollection -> Transaction.builder()
                                .transactionId(UUID.randomUUID().toString())
                                .mfiId(mfiId)
                                .managementProcessId(managementProcessId)
                                .processId(transactionProcessId)
                                .officeId(officeId)
                                .memberId(writeOffCollection.getMemberId())
                                .accountType(ACCOUNT_TYPE_LOAN.getValue())
                                .loanAccountId(writeOffCollection.getLoanAccountId())
                                .amount(writeOffCollection.getWriteOffCollectionAmount())
                                .transactionCode(TransactionCodes.LOAN_WRITE_OFF.getValue())
                                .paymentMode(PAYMENT_MODE_CASH.getValue())
                                .status(Status.STATUS_APPROVED.getValue())
                                .transactionDate(businessDate)
                                .transactedBy(loginId)
                                .createdBy(loginId)
                                .createdOn(LocalDateTime.now())
                                .samityId(samityId)
                                .build()))
                .collectList()
                .doOnNext(transactionList -> log.debug("Transaction List For Loan Write Off: {}",
                        transactionList))
                .flatMap(transactionUseCase::createTransactionEntryForLoanAdjustmentForSamity);

    }


    private Mono<List<Transaction>> createSMSNotificationEntryForLoanWriteOff(List<Transaction> transactionList, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()))
                .flatMap(transaction -> this.createSMSNotificationRequestForTransaction(transaction, smsNotificationMetaPropertyList))
                .collectList()
                .map(transactions -> transactionList)
                .onErrorResume(throwable -> {
                    log.error("Error in Creating SMS Notification Entry for Loan Write Off: {}", throwable.getMessage());
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


    private Mono<List<LoanWriteOffCollection>> validateAndUpdateLoanWriteOffDataForAuthorization(
            LoanWriteOffAuthorizationCommand requestDTO) {
        return writeOffCollectionPort.getLoanWriteOffCollectionBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                .collectList()
                .filter(loanWriteOffCollections -> !loanWriteOffCollections.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Data Not Found for Samity")))
                .filter(loanWriteOffCollections -> loanWriteOffCollections.stream()
                        .noneMatch(loanWriteOffData -> !HelperUtil.checkIfNullOrEmpty(loanWriteOffData.getStatus())
                                && loanWriteOffData.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Data is Already Authorized for Samity")))
                .filter(writeOffCollections -> !writeOffCollections.isEmpty() && writeOffCollections.stream().allMatch(
                        loanWriteOffData -> !HelperUtil.checkIfNullOrEmpty(loanWriteOffData.getStatus())
                                && (loanWriteOffData.getStatus().equals(Status.STATUS_SUBMITTED.getValue())
                                || loanWriteOffData.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Data is not Submitted for Authorization")))
                .filter(loanWriteOffCollections -> loanWriteOffCollections.stream()
                        .allMatch(loanWriteOffData -> !HelperUtil
                                .checkIfNullOrEmpty(loanWriteOffData.getIsLocked())
                                && loanWriteOffData.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Write Off Data is Not Locked for Samity")));
    }


    private Mono<? extends LoanWriteOffDetailsResponseDto> buildCombineResponse(MemberEntity member, LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto request, List<StagingAccountData> stagingAccountDataList, StagingAccountData stagingAccountData, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, LocalDate disbursementDate, LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(writeOffCollection.getLoanAccountId(), writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId(), String.valueOf(writeOffCollection.getCurrentVersion()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Collection Staging data not found for loan account : " + writeOffCollection.getLoanAccountId() + " Management Process Id : " + writeOffCollection.getManagementProcessId() + " Process Id : " + writeOffCollection.getProcessId())))
                .flatMap(collectionStagingDetails -> loanAdjustmentUseCase.getAdjustedLoanAccountListByManagementProcessId(
                                LoanAdjustmentRequestDTO.builder()
                                        .managementProcessId(writeOffCollection.getManagementProcessId())
                                        .processId(writeOffCollection.getProcessId())
                                        .currentVersion(writeOffCollection.getCurrentVersion())
                                        .memberId(writeOffCollection.getMemberId())
                                        .mfiId(request.getMfiId())
                                        .build())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "loan Adjustment data not found for Loan account " + writeOffCollection.getLoanAccountId() + " Management Process Id " + writeOffCollection.getManagementProcessId() + " Process Id " + writeOffCollection.getProcessId()))).zipWith(Mono.just(collectionStagingDetails)))
                .flatMap(responseDto -> this.getSavingsAccountDetailsForWriteOff(stagingAccountDataList)
                        .map(savingsAccountForLoanWriteOffDtoList -> buildCombineModeWriteOffDetailsResponseDto(responseDto.getT2(), responseDto.getT1(), member, writeOffCollection, savingsAccountForLoanWriteOffDtoList, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO)));
    }

    private Mono<LoanWriteOffDetailsResponseDto> buildAdjustmentResponse(MemberEntity member, LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto request, List<StagingAccountData> stagingAccountDataList, StagingAccountData stagingAccountData, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, LocalDate disbursementDate, LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return loanAdjustmentUseCase.getAdjustedLoanAccountListByManagementProcessId(
                        LoanAdjustmentRequestDTO.builder()
                                .managementProcessId(writeOffCollection.getManagementProcessId())
                                .processId(writeOffCollection.getProcessId())
                                .currentVersion(writeOffCollection.getCurrentVersion())
                                .memberId(writeOffCollection.getMemberId())
                                .mfiId(request.getMfiId())
                                .build())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "loan Adjustment data not found for Loan account " + writeOffCollection.getLoanAccountId() + " Management Process Id " + writeOffCollection.getManagementProcessId() + " Process Id " + writeOffCollection.getProcessId())))
                .flatMap(responseDto -> this.getSavingsAccountDetailsForWriteOff(stagingAccountDataList)
                        .map(savingsAccountListForWriteOff -> buildAdjustmentModeWriteOffDetailsResponseDto(responseDto, member, writeOffCollection, savingsAccountListForWriteOff, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO)));
    }

    private LoanWriteOffDetailsResponseDto buildAdjustmentModeWriteOffDetailsResponseDto(LoanAdjustmentMemberGridViewResponseDTO loanAdjustmentMemberGridViewResponseDTO, MemberEntity member, LoanWriteOffCollection loanWriteOffCollection, List<SavingsAccountForLoanWriteOffDto> savingsAccountForLoanWriteOffDtoList, StagingAccountData stagingAccountData, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, LocalDate disbursementDate, LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return LoanWriteOffDetailsResponseDto.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(stagingAccountData.getLoanAccountId())
                .loanProductId(stagingAccountData.getProductCode())
                .loanProductNameEn(stagingAccountData.getProductNameEn())
                .loanProductNameBn(stagingAccountData.getProductNameBn())
                .loanAmount(stagingAccountData.getLoanAmount())
                .serviceCharge(stagingAccountData.getServiceCharge())
                .totalLoanAmount(stagingAccountData.getLoanAmount().add(stagingAccountData.getServiceCharge()))
                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()))
                .paymentMethod(loanWriteOffCollection.getPaymentMode())
                .writeOffCollectionDate(loanWriteOffCollection.getLoanWriteOffCollectionDate())
                .writeOffCollectionAmount(loanWriteOffCollection.getWriteOffCollectionAmount())
                .status(loanWriteOffCollection.getStatus())
                .remarks(loanWriteOffCollection.getRemarks())
                .rejectedBy(loanWriteOffCollection.getRejectedBy())
                .rejectedOn(loanWriteOffCollection.getRejectedOn())
                .submittedBy(loanWriteOffCollection.getSubmittedBy())
                .submittedOn(loanWriteOffCollection.getSubmittedOn())
                .approvedBy(loanWriteOffCollection.getApprovedBy())
                .approvedOn(loanWriteOffCollection.getApprovedOn())
                .samityId(member.getOfficeId())
                .officeId(member.getOfficeId())
                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate())
                .loanTerm(loanWriteOffLoanAccountResponseDTO.getLoanTerm())
                .installmentAmount(loanWriteOffLoanAccountResponseDTO.getInstallmentAmount())
                .noOfInstallment(loanWriteOffLoanAccountResponseDTO.getNoInstallment())
                .disbursementDate(disbursementDate.toString())
                .advancePaid(stagingAccountData.getTotalAdvance() == null ? BigDecimal.ZERO : stagingAccountData.getTotalAdvance())
                .payableAmountAfterWriteOffCollection((stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()).subtract(loanWriteOffCollection.getWriteOffCollectionAmount())))
                .adjustedLoanAccountList(loanAdjustmentMemberGridViewResponseDTO.getAdjustedLoanAccountList())
                .savingsAccountList(savingsAccountForLoanWriteOffDtoList)
                .build();
    }

    private Mono<LoanWriteOffDetailsResponseDto> buildCashResponse(MemberEntity member, LoanWriteOffCollection writeOffCollection, List<StagingAccountData> stagingAccountDataList, StagingAccountData stagingAccountData, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, LocalDate disbursementDate, LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(writeOffCollection.getLoanAccountId(), writeOffCollection.getManagementProcessId(), writeOffCollection.getProcessId(), String.valueOf(writeOffCollection.getCurrentVersion()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "Collection Staging data not found for loan account " + writeOffCollection.getLoanAccountId())))
                .flatMap(collectionStagingData -> this.getSavingsAccountDetailsForWriteOff(stagingAccountDataList)
                        .map(savingsAccountForWriteOffList -> buildCashModeWriteOffDetailsResponseDto(collectionStagingData, member, writeOffCollection, savingsAccountForWriteOffList, stagingAccountData, serviceChargeChartResponseDTO, disbursementDate, loanWriteOffLoanAccountResponseDTO)));
    }

    private LoanWriteOffDetailsResponseDto buildCashModeWriteOffDetailsResponseDto(CollectionStagingData collectionStagingData,
                                                                                   MemberEntity member, LoanWriteOffCollection loanWriteOffCollection,
                                                                                   List<SavingsAccountForLoanWriteOffDto> savingsAccountForWriteOffList,
                                                                                   StagingAccountData stagingAccountData,
                                                                                   ServiceChargeChartResponseDTO serviceChargeChartResponseDTO,
                                                                                   LocalDate disbursementDate,
                                                                                   LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return LoanWriteOffDetailsResponseDto.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(stagingAccountData.getLoanAccountId())
                .loanProductId(stagingAccountData.getProductCode())
                .loanProductNameEn(stagingAccountData.getProductNameEn())
                .loanProductNameBn(stagingAccountData.getProductNameBn())
                .loanAmount(stagingAccountData.getLoanAmount())
                .serviceCharge(stagingAccountData.getServiceCharge())
                .totalLoanAmount(stagingAccountData.getLoanAmount().add(stagingAccountData.getServiceCharge()))
                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()))
                .paymentMethod(loanWriteOffCollection.getPaymentMode())
                .writeOffCollectionDate(loanWriteOffCollection.getLoanWriteOffCollectionDate())
                .writeOffCollectionAmount(loanWriteOffCollection.getWriteOffCollectionAmount())
                .status(loanWriteOffCollection.getStatus())
                .remarks(loanWriteOffCollection.getRemarks())
                .rejectedBy(loanWriteOffCollection.getRejectedBy())
                .rejectedOn(loanWriteOffCollection.getRejectedOn())
                .submittedBy(loanWriteOffCollection.getSubmittedBy())
                .submittedOn(loanWriteOffCollection.getSubmittedOn())
                .approvedBy(loanWriteOffCollection.getApprovedBy())
                .approvedOn(loanWriteOffCollection.getApprovedOn())
                .samityId(loanWriteOffCollection.getSamityId())
                .officeId(member.getOfficeId())
                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate())
                .loanTerm(loanWriteOffLoanAccountResponseDTO.getLoanTerm())
                .installmentAmount(loanWriteOffLoanAccountResponseDTO.getInstallmentAmount())
                .noOfInstallment(loanWriteOffLoanAccountResponseDTO.getNoInstallment())
                .disbursementDate(disbursementDate.toString())
                .advancePaid(stagingAccountData.getTotalAdvance() == null ? BigDecimal.ZERO : stagingAccountData.getTotalAdvance())
                .payableAmountAfterWriteOffCollection((stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()).subtract(loanWriteOffCollection.getWriteOffCollectionAmount())))
                .collection(LoanWriteOffCashCollection.builder()
                        .oid(collectionStagingData.getOid())
                        .collectionStagingDataId(collectionStagingData.getCollectionStagingDataId())
                        .managementProcessId(collectionStagingData.getManagementProcessId())
                        .processId(collectionStagingData.getProcessId())
                        .stagingDataId(collectionStagingData.getStagingDataId())
                        .samityId(collectionStagingData.getSamityId())
                        .accountType(collectionStagingData.getAccountType())
                        .loanAccountId(collectionStagingData.getLoanAccountId())
                        .amount(collectionStagingData.getAmount())
                        .paymentMode(collectionStagingData.getPaymentMode())
                        .collectionType(collectionStagingData.getCollectionType())
                        .status(collectionStagingData.getStatus())
                        .build())
                .savingsAccountList(savingsAccountForWriteOffList)
                .build();
    }

    private LoanWriteOffDetailsResponseDto buildCombineModeWriteOffDetailsResponseDto(CollectionStagingData collectionStagingData, LoanAdjustmentMemberGridViewResponseDTO loanAdjustmentMemberGridViewResponseDTO, MemberEntity member, LoanWriteOffCollection loanWriteOffCollection, List<SavingsAccountForLoanWriteOffDto> savingsAccountForWriteOffList, StagingAccountData stagingAccountData, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, LocalDate disbursementDate, LoanAccountResponseDTO loanWriteOffLoanAccountResponseDTO) {
        return LoanWriteOffDetailsResponseDto.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(stagingAccountData.getLoanAccountId())
                .loanProductId(stagingAccountData.getProductCode())
                .loanProductNameEn(stagingAccountData.getProductNameEn())
                .loanProductNameBn(stagingAccountData.getProductNameBn())
                .loanAmount(stagingAccountData.getLoanAmount())
                .serviceCharge(stagingAccountData.getServiceCharge())
                .totalLoanAmount(stagingAccountData.getLoanAmount().add(stagingAccountData.getServiceCharge()))
                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()))
                .paymentMethod(loanWriteOffCollection.getPaymentMode())
                .writeOffCollectionDate(loanWriteOffCollection.getLoanWriteOffCollectionDate())
                .writeOffCollectionAmount(loanWriteOffCollection.getWriteOffCollectionAmount())
                .status(loanWriteOffCollection.getStatus())
                .remarks(loanWriteOffCollection.getRemarks())
                .rejectedBy(loanWriteOffCollection.getRejectedBy())
                .rejectedOn(loanWriteOffCollection.getRejectedOn())
                .submittedBy(loanWriteOffCollection.getSubmittedBy())
                .submittedOn(loanWriteOffCollection.getSubmittedOn())
                .approvedBy(loanWriteOffCollection.getApprovedBy())
                .approvedOn(loanWriteOffCollection.getApprovedOn())
                .samityId(member.getOfficeId())
                .officeId(member.getOfficeId())
                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate())
                .loanTerm(loanWriteOffLoanAccountResponseDTO.getLoanTerm())
                .installmentAmount(loanWriteOffLoanAccountResponseDTO.getInstallmentAmount())
                .noOfInstallment(loanWriteOffLoanAccountResponseDTO.getNoInstallment())
                .disbursementDate(disbursementDate.toString())
                .advancePaid(stagingAccountData.getTotalAdvance() == null ? BigDecimal.ZERO : stagingAccountData.getTotalAdvance())
                .payableAmountAfterWriteOffCollection((stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()).subtract(loanWriteOffCollection.getWriteOffCollectionAmount())))
                .collection(LoanWriteOffCashCollection.builder()
                        .oid(collectionStagingData.getOid())
                        .collectionStagingDataId(collectionStagingData.getCollectionStagingDataId())
                        .managementProcessId(collectionStagingData.getManagementProcessId())
                        .processId(collectionStagingData.getProcessId())
                        .stagingDataId(collectionStagingData.getStagingDataId())
                        .samityId(collectionStagingData.getSamityId())
                        .accountType(collectionStagingData.getAccountType())
                        .loanAccountId(collectionStagingData.getLoanAccountId())
                        .amount(collectionStagingData.getAmount())
                        .paymentMode(collectionStagingData.getPaymentMode())
                        .collectionType(collectionStagingData.getCollectionType())
                        .status(collectionStagingData.getStatus())
                        .build())
                .adjustedLoanAccountList(loanAdjustmentMemberGridViewResponseDTO.getAdjustedLoanAccountList())
                .savingsAccountList(savingsAccountForWriteOffList)
                .build();
    }


    private Mono<List<SavingsAccountForLoanWriteOffDto>> getSavingsAccountDetailsForWriteOff(List<StagingAccountData> stagingAccountDataList) {
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
                            return SavingsAccountForLoanWriteOffDto.builder()
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

    @Override
    public Mono<LoanWriteOffGridViewByOfficeResponseDto> getCollectedWriteOffAccountData(LoanWriteOffGridViewByOfficeRequestDto request) {
        return writeOffCollectionPort.getCollectedWriteOffDataByOfficeId(request.getOfficeId(), request.getStartDate(), request.getEndDate())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No Write Off Collection Data Found By Office")))
                .flatMap(loanWriteOffData -> commonRepository.getMemberAndLoanAccountByLoanAccountId(loanWriteOffData.getLoanAccountId()).zipWith(Mono.just(loanWriteOffData)))
                .flatMap(memberAndLoanAccountAndWriteOffData -> Mono.zip(Mono.just(memberAndLoanAccountAndWriteOffData.getT1()),
                                Mono.just(memberAndLoanAccountAndWriteOffData.getT2()),
                                commonRepository.getSamityBySamityId(memberAndLoanAccountAndWriteOffData.getT2().getSamityId()))
                        .doOnNext(tuple3 -> log.info("No Write Off Collection Data found By Office: {}", tuple3)))
                .filter(memberAndLoanAccountAndLoanWriteOffAndSamity -> memberAndLoanAccountAndLoanWriteOffAndSamity.getT3().getOfficeId().equalsIgnoreCase(request.getOfficeId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No Write Off Collection Data Found By Office")))
                .filter(memberAndLoanAccountAndLoanWriteOffAndSamity -> memberAndLoanAccountAndLoanWriteOffAndSamity.getT3().getOfficeId().equalsIgnoreCase(request.getOfficeId()) &&
                        (HelperUtil.checkIfNullOrEmpty(request.getSamityId()) || memberAndLoanAccountAndLoanWriteOffAndSamity.getT2().getSamityId().equals(request.getSamityId())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(NOT_FOUND, "No Write Off Collection Data Found By Office")))
                .map(memberAndLoanAccountAndLoanWriteOffAndSamity -> buildLoanWriteOffGridData(memberAndLoanAccountAndLoanWriteOffAndSamity.getT1(), memberAndLoanAccountAndLoanWriteOffAndSamity.getT2(), memberAndLoanAccountAndLoanWriteOffAndSamity.getT3()))
                .skip((long) request.getOffset() * request.getLimit())
                .take(request.getLimit())
                .collectList()
                .zipWith(managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                        .switchIfEmpty(Mono.just(ManagementProcessTracker.builder().build())))
                .map(listOfLoanWriteOffGridDataAndManagementProcessTracker -> LoanWriteOffGridViewByOfficeResponseDto.builder()
                        .officeId(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeId())
                        .officeNameBn(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeNameBn())
                        .officeNameEn(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT2().getOfficeNameEn())
                        .data(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT1())
                        .totalCount(listOfLoanWriteOffGridDataAndManagementProcessTracker.getT1().size())
                        .build())
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage())));
    }


    private LoanWriteOffGridData buildLoanWriteOffGridData(MemberAndLoanAccountEntity memberAndLoanAccount, LoanWriteOffCollection loanWriteOffCollection, Samity samity) {
        BigDecimal totalDue = gson.fromJson(loanWriteOffCollection.getLoanInfo(), LoanInfo.class).getTotalDue();
        return LoanWriteOffGridData.builder()
                .oid(loanWriteOffCollection.getOid())
                .memberId(memberAndLoanAccount.getMemberId())
                .memberNameBn(memberAndLoanAccount.getMemberNameBn())
                .memberNameEn(memberAndLoanAccount.getMemberNameEn())
                .samityId(loanWriteOffCollection.getSamityId())
                .samityNameBn(samity.getSamityNameBn())
                .samityNameEn(samity.getSamityNameEn())
                .loanAmount(memberAndLoanAccount.getLoanAmount())
                .loanAccountId(loanWriteOffCollection.getLoanAccountId())
                .loanWriteOffCollectionDate(loanWriteOffCollection.getLoanWriteOffCollectionDate() == null ? null : loanWriteOffCollection.getLoanWriteOffCollectionDate())
                .writeOffCollectionAmount(loanWriteOffCollection.getWriteOffCollectionAmount())
                .payableAmountAfterWriteOffCollection(totalDue.subtract(loanWriteOffCollection.getWriteOffCollectionAmount()))
                .status(loanWriteOffCollection.getStatus())
                .btnSubmitEnabled(
                        StringUtils.isNotBlank(loanWriteOffCollection.getIsSubmitted()) &&
                                loanWriteOffCollection.getIsSubmitted().equalsIgnoreCase(StatusYesNo.Yes.toString()) ?
                                StatusYesNo.No.toString() : StatusYesNo.Yes.toString()
                )
                .btnUpdateEnabled(
                        StringUtils.isNotBlank(loanWriteOffCollection.getIsLocked()) && loanWriteOffCollection.getIsLocked().equalsIgnoreCase(StatusYesNo.No.toString())
                                && (loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanWriteOffCollection.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                                ? StatusYesNo.Yes.toString()
                                : StatusYesNo.No.toString()
                )
                .build();
    }


    private LoanWriteOffCollection addPaymentInfoIntoLoanWriteOff(LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto requestDto) {
        if(requestDto.getCollectionType().equalsIgnoreCase("Combine")) {
            writeOffCollection.setPaymentInfo(WriteOffPaymentInfo.builder().adjustment(buildAdjustmentLoanAccountForAMember(writeOffCollection, requestDto))
                    .collection(buildCollectionDetailForWriteOff(writeOffCollection, requestDto)).build());
        } else if(requestDto.getCollectionType().equalsIgnoreCase("Adjustment")) {
            writeOffCollection.setPaymentInfo(WriteOffPaymentInfo.builder().adjustment(buildAdjustmentLoanAccountForAMember(writeOffCollection, requestDto)).build());
        } else {
            writeOffCollection.setPaymentInfo(WriteOffPaymentInfo.builder().collection(buildCollectionDetailForWriteOff(writeOffCollection, requestDto)).build());
        }
        return writeOffCollection;
    }


    private AdjustedLoanAccount buildAdjustmentLoanAccountForAMember(LoanWriteOffCollection writeOffCollection,
                                                                     WriteOffCollectionAccountDataRequestDto requestDto) {
        return AdjustedLoanAccount.builder()
                .loanAccountId(writeOffCollection.getLoanAccountId())
                .adjustmentDate(writeOffCollection.getCreatedOn().toLocalDate())
                .adjustedAmount(requestDto.getAdjustedAccountList().stream()
                        .map(AdjustedAccount::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .adjustedSavingsAccountList(this
                        .buildAdjustedSavingsAccountListFromLoanAdjustmentData(
                                requestDto.getAdjustedAccountList()))
                .build();
    }


    private List<AdjustedSavingsAccount> buildAdjustedSavingsAccountListFromLoanAdjustmentData(
            List<AdjustedAccount> adjustmentAccounts) {
        return adjustmentAccounts.stream()
                .filter(adjustmentAccount -> !HelperUtil
                        .checkIfNullOrEmpty(adjustmentAccount.getSavingsAccountId())
                        && adjustmentAccount.getAmount() != null)
                .map(savingsAccountAdjustedData -> AdjustedSavingsAccount.builder()
                        .savingsAccountId(savingsAccountAdjustedData.getSavingsAccountId())
                        .amount(savingsAccountAdjustedData.getAmount())
                        .build())
                .sorted(Comparator.comparing(AdjustedSavingsAccount::getSavingsAccountId))
                .toList();
    }

    private LoanWriteOffCashCollection buildCollectionDetailForWriteOff(LoanWriteOffCollection writeOffCollection, WriteOffCollectionAccountDataRequestDto requestDto) {
        return LoanWriteOffCashCollection.builder()
                .managementProcessId(writeOffCollection.getManagementProcessId())
                .processId(writeOffCollection.getProcessId())
//                .stagingDataId(writeOffCollection.getStagingDataId())
                .samityId(writeOffCollection.getSamityId())
                .accountType(ACCOUNT_TYPE_LOAN.getValue())
                .loanAccountId(writeOffCollection.getLoanAccountId())
                .amount(requestDto.getCollectedAmountByCash())
                .paymentMode(PAYMENT_MODE_CASH.getValue())
                .collectionType(WRITE_OFF.getValue())
                .build();
    }
}



