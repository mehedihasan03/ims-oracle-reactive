package net.celloscope.mraims.loanportfolio.features.passbook.application.service;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_DPS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_FDR;
import static net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum.SERVICE_CHARGE_DEDUCTION_METHOD_RATE_BASED;
import static net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum.SERVICE_CHARGE_DEDUCTION_METHOD_SCHEDULE_BASED;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberSamityOfficeEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.LoanCalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.SavingsDepositCommand;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.InstallmentCalculationDTO;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.in.PassbookHistoryUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.DpsRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.DPSRepaymentCommand;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookCalculationDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Service
@Slf4j
public class PassbookService implements PassbookUseCase {

    private final PassbookPersistencePort port;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final ModelMapper modelMapper;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final FDRPersistencePort fdrPersistencePort;
    private final LoanAccountUseCase loanAccountUseCase;
    private final DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase;
    private final TransactionalOperator rxtx;
    private final HolidayUseCase holidayUseCase;
    private final CommonRepository commonRepository;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final PassbookHistoryUseCase passbookHistoryUseCase;

    public PassbookService(PassbookPersistencePort port, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase,
                           ModelMapper modelMapper, ISavingsAccountUseCase savingsAccountUseCase,
                           LoanAccountUseCase loanAccountUseCase, FDRPersistencePort fdrPersistencePort, DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase, TransactionalOperator rxtx, HolidayUseCase holidayUseCase, CommonRepository commonRepository, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, CommonRepository commonRepository1, MetaPropertyUseCase metaPropertyUseCase, PassbookHistoryUseCase passbookHistoryUseCase) {
        this.port = port;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.loanAccountUseCase = loanAccountUseCase;
        this.dpsRepaymentScheduleUseCase = dpsRepaymentScheduleUseCase;
        this.rxtx = rxtx;
        this.holidayUseCase = holidayUseCase;
        this.commonRepository = commonRepository1;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.passbookHistoryUseCase = passbookHistoryUseCase;
        this.modelMapper = new ModelMapper();
        this.fdrPersistencePort = fdrPersistencePort;
        this.savingsAccountUseCase = savingsAccountUseCase;
    }

    @Override
    public Mono<List<PassbookResponseDTO>> getRepaymentScheduleAndCreatePassbookEntryForLoan(
            PassbookRequestDTO passbookRequestDTO) {

        AtomicReference<String> loanProductId = new AtomicReference<>();
        AtomicReference<Integer> loanTerm = new AtomicReference<>();
        return loanRepaymentScheduleUseCase
                .getRepaymentScheduleByLoanAccountId(passbookRequestDTO.getLoanAccountId())
                .map(repaymentScheduleResponseDTOS -> repaymentScheduleResponseDTOS
                        .stream()
                        .filter(repaymentSchedule -> !repaymentSchedule.getStatus().equalsIgnoreCase(Status.STATUS_REBATED.getValue())
                                && !repaymentSchedule.getStatus().equalsIgnoreCase(Status.STATUS_REVISED.getValue()))
                        .toList())
                .doOnNext(repaymentScheduleResponseDTOS -> log.debug("Repay Schedule List: {}", repaymentScheduleResponseDTOS))
                .flatMap(repaymentScheduleList ->
                        loanAccountUseCase
                                .getLoanAccountDetailsByLoanAccountId(passbookRequestDTO.getLoanAccountId())
                                .doOnNext(loanAccountResponseDTO -> loanProductId.set(loanAccountResponseDTO.getProductCode()))
                                .doOnNext(loanAccountResponseDTO -> loanTerm.set(loanAccountResponseDTO.getLoanTerm()))
                                .map(LoanAccountResponseDTO::getOid)
                                .flatMap(loanAccountOid -> port
                                        .getLastPassbookEntryByLoanAccountOidAndTransactionCodes(loanAccountOid, List.of(TRANSACTION_CODE_LOAN_REPAY.getValue(), TRANSACTION_CODE_ADJUSTMENT_LOAN_REPAY.getValue()))
                                        .map(passbookResponseDTO -> modelMapper.map(passbookResponseDTO, Passbook.class)))
                                .filter(passbook -> !HelperUtil.checkIfNullOrEmpty(passbook.getLoanAccountId()))
                                .doOnNext(passbook -> log.info("Last Passbook Entry Loan Account Id: {} with Loan Account Oid: {}", passbook.getLoanAccountId(), passbook.getLoanAccountOid()))
                                .switchIfEmpty(port.getDisbursementPassbookEntryByDisbursedLoanAccountId(passbookRequestDTO.getLoanAccountId())
                                        .filter(passbook -> !HelperUtil.checkIfNullOrEmpty(passbook.getDisbursedLoanAccountId()))
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Disbursement Passbook Entry Found For Loan Account")))
                                        .doOnNext(passbook -> log.info("Disbursement Passbook Entry Loan Account Id: {} With  Loan Account Oid: {}", passbook.getDisbursedLoanAccountId(), passbook.getLoanAccountOid())))
                                .flatMap(lastPassbookEntry -> {
                                    return commonRepository.getServiceChargeChartEntityByLoanProductId(loanProductId.get())
                                            .map(serviceChargeChartEntity -> {
                                                BigDecimal annualServiceChargeRate = CommonFunctions.getAnnualInterestRate(serviceChargeChartEntity.getServiceChargeRate(), serviceChargeChartEntity.getServiceChargeRateFreq());
                                                annualServiceChargeRate = annualServiceChargeRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                                                double annualServiceChargeRateMultipliedByLoanTerm = annualServiceChargeRate.multiply(getLoanTermInYears(loanTerm.get())).doubleValue();
                                                return BigDecimal.valueOf(annualServiceChargeRateMultipliedByLoanTerm);
                                            })
                                            .flatMap(effectiveServiceChargeRate -> metaPropertyUseCase.getLoanCalculationMetaProperty(passbookRequestDTO.getTransactionDate())
                                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.META_PROPERTY_NOT_FOUND.getValue())))
                                                    .map(loanCalculationMetaProperty -> {
                                                        if (loanCalculationMetaProperty.getServiceChargeDeductionMethod().equalsIgnoreCase(SERVICE_CHARGE_DEDUCTION_METHOD_RATE_BASED.getValue())) {
                                                            return allocatePaymentFlatRatio(effectiveServiceChargeRate, repaymentScheduleList, lastPassbookEntry, passbookRequestDTO.getAmount(), loanCalculationMetaProperty);
                                                        } else if (loanCalculationMetaProperty.getServiceChargeDeductionMethod().equalsIgnoreCase(SERVICE_CHARGE_DEDUCTION_METHOD_SCHEDULE_BASED.getValue())) {
                                                            return allocatePayment(repaymentScheduleList, lastPassbookEntry, passbookRequestDTO.getAmount());
                                                        } else {
                                                            return allocatePaymentFlatRatio(effectiveServiceChargeRate, repaymentScheduleList, lastPassbookEntry, passbookRequestDTO.getAmount(), loanCalculationMetaProperty);
                                                        }
                                                    }))
                                            .flatMapIterable(installmentCalculationDTOS -> installmentCalculationDTOS)
                                            .map(installmentCalculationDTO -> buildPassbook(installmentCalculationDTO, lastPassbookEntry, passbookRequestDTO))
                                            .collectList();
                                }))
                .doOnNext(passbookList -> passbookList.forEach(passbook -> passbook.setLoanAdjustmentProcessId(passbookRequestDTO.getLoanAdjustmentProcessId())))
                .flatMap(this::insertPassbookEntry);
    }

    private BigDecimal getLoanTermInYears(Integer loanTermInMonth) {
        if (loanTermInMonth == null)
            return BigDecimal.ONE;
        return BigDecimal.valueOf(loanTermInMonth).divide(BigDecimal.valueOf(12), 12, RoundingMode.HALF_UP);
    }

    private List<InstallmentCalculationDTO> allocatePaymentFlatRatio(BigDecimal effectiveServiceChargeRate, List<RepaymentScheduleResponseDTO> repaymentScheduleList, Passbook lastPassbookEntry, BigDecimal paidAmount, LoanCalculationMetaProperty loanCalculationMetaProperty) {

        int installmentNo = lastPassbookEntry.getInstallNo() == null ? 1 : lastPassbookEntry.getInstallNo();
        BigDecimal remainingAmount = paidAmount;
        List<InstallmentCalculationDTO> paidInstallmentsList = new ArrayList<>();
        BigDecimal prinPaidTillLastInstallment = lastPassbookEntry.getPrinPaidTillDate() == null ? BigDecimal.ZERO : lastPassbookEntry.getPrinPaidTillDate();
        BigDecimal scPaidTillLastInstallment = lastPassbookEntry.getScPaidTillDate() == null ? BigDecimal.ZERO : lastPassbookEntry.getScPaidTillDate();
        BigDecimal installmentBeginPrinBalance = (lastPassbookEntry.getInstallmentEndPrinBalance() == null || lastPassbookEntry.getInstallmentEndPrinBalance().compareTo(BigDecimal.ZERO) == 0) ? lastPassbookEntry.getLoanAmount() : lastPassbookEntry.getInstallmentEndPrinBalance();

        if (lastPassbookEntry.getInstallNo() == null) {
            installmentNo = 1;
        } else if (lastPassbookEntry.getScRemainForThisInst().add(lastPassbookEntry.getPrinRemainForThisInst()).compareTo(BigDecimal.ZERO) > 0) {
            PassbookCalculationDTO dueAdjustmentDTO = adjustLastInstallmentDueFlatRatio(lastPassbookEntry, remainingAmount, effectiveServiceChargeRate, loanCalculationMetaProperty);
            paidInstallmentsList.add(buildDueInstallmentCalculationDTO(dueAdjustmentDTO, lastPassbookEntry, lastPassbookEntry.getInstallNo(), prinPaidTillLastInstallment, scPaidTillLastInstallment, installmentBeginPrinBalance, repaymentScheduleList.get(lastPassbookEntry.getInstallNo() - 1)));
            remainingAmount = dueAdjustmentDTO.getRemainingAmount();
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) installmentNo += 1;
        } else {
            installmentNo += 1;
        }

        log.debug("installment no after last installment adjustment : {}", installmentNo);

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.debug("Remaining Amount in Loop : {}", remainingAmount);

            RepaymentScheduleResponseDTO currentInstallmentData = repaymentScheduleList.get(installmentNo - 1);
            BigDecimal currentInstallmentDue = currentInstallmentData.getPrincipal().add(currentInstallmentData.getServiceCharge());
            log.debug("current installment no : {}", currentInstallmentData.getInstallNo());
            log.debug("current installment due : {}", currentInstallmentDue);
            PassbookCalculationDTO scAdjustmentDTO;
            PassbookCalculationDTO prinAdjustmentDTO;

            if (remainingAmount.compareTo(currentInstallmentDue) >= 0) {
                log.debug("current installment due fully paid");
                remainingAmount = remainingAmount.subtract(currentInstallmentDue);
                scAdjustmentDTO = PassbookCalculationDTO
                        .builder()
                        .scPaid(currentInstallmentData.getServiceCharge())
                        .isSCFullyPaid(true)
                        .scRemainForThisInst(BigDecimal.ZERO)
                        .remainingAmount(remainingAmount)
                        .build();

                prinAdjustmentDTO = PassbookCalculationDTO
                        .builder()
                        .prinPaid(currentInstallmentData.getPrincipal())
                        .isPrincipalFullyPaid(true)
                        .prinRemainForThisInst(BigDecimal.ZERO)
                        .remainingAmount(remainingAmount)
                        .build();
                log.debug("remaining amount after fully paying current installment : {}", remainingAmount);
            } else {
                log.debug("current installment due partially paid");
                BigDecimal prinPaid = remainingAmount.divide(effectiveServiceChargeRate.add(BigDecimal.ONE),
                        loanCalculationMetaProperty.getServiceChargeAmountPrecision(),
                        CommonFunctions.getRoundingMode(loanCalculationMetaProperty.getRoundingLogic()));
                BigDecimal scPaid = remainingAmount.subtract(prinPaid);

                log.debug("prin paid : {} | sc paid : {}", prinPaid, scPaid);

                scAdjustmentDTO = PassbookCalculationDTO
                        .builder()
                        .scPaid(scPaid)
                        .scRemainForThisInst(currentInstallmentData.getServiceCharge().subtract(scPaid))
                        .isSCFullyPaid(false)
                        .remainingAmount(BigDecimal.ZERO)
                        .build();

                prinAdjustmentDTO = PassbookCalculationDTO
                        .builder()
                        .prinPaid(prinPaid)
                        .prinRemainForThisInst(currentInstallmentData.getPrincipal().subtract(prinPaid))
                        .isPrincipalFullyPaid(false)
                        .remainingAmount(BigDecimal.ZERO)
                        .build();
                remainingAmount = BigDecimal.ZERO;
                log.debug("remaining amount after partially paying current installment : {}", remainingAmount);
            }

            InstallmentCalculationDTO currentInstallmentInfo = buildCurrentInstallmentCalculationDTO(lastPassbookEntry, installmentNo, prinAdjustmentDTO, scAdjustmentDTO, paidInstallmentsList, prinPaidTillLastInstallment, scPaidTillLastInstallment, installmentBeginPrinBalance, repaymentScheduleList.get(installmentNo - 1));
            paidInstallmentsList.add(currentInstallmentInfo);

            if (scAdjustmentDTO.getIsSCFullyPaid() && prinAdjustmentDTO.getIsPrincipalFullyPaid()) installmentNo += 1;
            prinPaidTillLastInstallment = prinPaidTillLastInstallment.add(prinAdjustmentDTO.getPrinPaid());
            scPaidTillLastInstallment = scPaidTillLastInstallment.add(scAdjustmentDTO.getScPaid());

            log.debug("remaining amount after loop : {}", remainingAmount);
        }

        return paidInstallmentsList;
    }


    List<InstallmentCalculationDTO> allocatePayment(List<RepaymentScheduleResponseDTO> repaymentScheduleList, Passbook lastPassbookEntry, BigDecimal paidAmount) {

        log.info("repayment schedule list install nos : {}", repaymentScheduleList.stream().map(RepaymentScheduleResponseDTO::getInstallNo).collect(Collectors.toList()));
        int installmentNo = lastPassbookEntry.getInstallNo() == null ? 1 : lastPassbookEntry.getInstallNo();
        BigDecimal remainingAmount = paidAmount;
        log.debug("paid Amount : {}", paidAmount);
        List<InstallmentCalculationDTO> paidInstallmentsList = new ArrayList<>();
        BigDecimal prinPaidTillLastInstallment = lastPassbookEntry.getPrinPaidTillDate() == null ? BigDecimal.ZERO : lastPassbookEntry.getPrinPaidTillDate();
        BigDecimal scPaidTillLastInstallment = lastPassbookEntry.getScPaidTillDate() == null ? BigDecimal.ZERO : lastPassbookEntry.getScPaidTillDate();
        BigDecimal installmentBeginPrinBalance = (lastPassbookEntry.getInstallmentEndPrinBalance() == null || lastPassbookEntry.getInstallmentEndPrinBalance().compareTo(BigDecimal.ZERO) == 0) ? lastPassbookEntry.getLoanAmount() : lastPassbookEntry.getInstallmentEndPrinBalance();

        if (lastPassbookEntry.getInstallNo() == null) {
            installmentNo = 1;
        } else if (lastPassbookEntry.getScRemainForThisInst().add(lastPassbookEntry.getPrinRemainForThisInst()).compareTo(BigDecimal.ZERO) > 0) {
            PassbookCalculationDTO dueAdjustmentDTO = adjustLastInstallmentDue(lastPassbookEntry, remainingAmount);
            paidInstallmentsList.add(buildDueInstallmentCalculationDTO(dueAdjustmentDTO, lastPassbookEntry, lastPassbookEntry.getInstallNo(), prinPaidTillLastInstallment, scPaidTillLastInstallment, installmentBeginPrinBalance, repaymentScheduleList.get(lastPassbookEntry.getInstallNo() - 1)));
            remainingAmount = dueAdjustmentDTO.getRemainingAmount();
            log.debug("remaining amount after adjusting last installment : {}", remainingAmount);
            log.debug("before install no: {}", installmentNo);
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) installmentNo += 1;
            log.debug("after install no : {}", installmentNo);
        } else {
            installmentNo += 1;
        }

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.debug("Remaining Amount: {}", remainingAmount);
            log.debug("working with install no : {}", installmentNo);
            RepaymentScheduleResponseDTO currentInstallmentData = repaymentScheduleList.get(installmentNo - 1);
            log.debug("current installment data installment no : {} | principal : {} | sc : {} | status : {}", currentInstallmentData.getInstallNo(), currentInstallmentData.getPrincipal(), currentInstallmentData.getServiceCharge(), currentInstallmentData.getStatus());
            PassbookCalculationDTO scAdjustmentDTO = doesPaidAmountPaysOffServiceCharge(remainingAmount, currentInstallmentData.getServiceCharge());
            PassbookCalculationDTO prinAdjustmentDTO = doesPaidAmountPaysOffPrincipal(scAdjustmentDTO.getRemainingAmount(), currentInstallmentData.getPrincipal());
            InstallmentCalculationDTO currentInstallmentInfo = buildCurrentInstallmentCalculationDTO(lastPassbookEntry, installmentNo, prinAdjustmentDTO, scAdjustmentDTO, paidInstallmentsList, prinPaidTillLastInstallment, scPaidTillLastInstallment, installmentBeginPrinBalance, repaymentScheduleList.get(installmentNo - 1));
            paidInstallmentsList.add(currentInstallmentInfo);

            log.debug("install no before : {}", installmentNo);
            if (scAdjustmentDTO.getIsSCFullyPaid() && prinAdjustmentDTO.getIsPrincipalFullyPaid()) installmentNo += 1;
            log.debug("install no after : {}", installmentNo);

            remainingAmount = scAdjustmentDTO.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
                    ? prinAdjustmentDTO.getRemainingAmount()
                    : BigDecimal.ZERO;
            prinPaidTillLastInstallment = prinPaidTillLastInstallment.add(prinAdjustmentDTO.getPrinPaid());
            scPaidTillLastInstallment = scPaidTillLastInstallment.add(scAdjustmentDTO.getScPaid());
            log.info("remaining amount after loop : {}", remainingAmount);
            log.debug("principal paid till last installment : {}", prinPaidTillLastInstallment);
            log.debug("service charge paid till last installment : {}", scPaidTillLastInstallment);
        }

        return paidInstallmentsList;
    }

    private static InstallmentCalculationDTO buildCurrentInstallmentCalculationDTO(Passbook lastPassbookEntry, Integer installmentNo, PassbookCalculationDTO prinAdjustmentDTO, PassbookCalculationDTO scAdjustmentDTO, List<InstallmentCalculationDTO> paidInstallmentsList, BigDecimal prinPaidTillLastInstallment, BigDecimal scPaidTillLastInstallment, BigDecimal installmentBeginPrinBalance, RepaymentScheduleResponseDTO repaymentSchedule) {

        return InstallmentCalculationDTO
                .builder()
                .loanAccountId(lastPassbookEntry.getLoanAccountId() == null ? lastPassbookEntry.getDisbursedLoanAccountId() : lastPassbookEntry.getLoanAccountId())
                .installmentNo(installmentNo)
                .installDate(repaymentSchedule.getInstallDate())
                .loanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId())
                .prinPaid(prinAdjustmentDTO.getPrinPaid())
                .prinRemainForThisInst(prinAdjustmentDTO.getPrinRemainForThisInst())
                .serviceChargePaid(scAdjustmentDTO.getScPaid())
                .scRemainForThisInst(scAdjustmentDTO.getScRemainForThisInst())
                .prinPaidTillDate(paidInstallmentsList.isEmpty()
                        ? prinPaidTillLastInstallment.add(prinAdjustmentDTO.getPrinPaid())
                        : paidInstallmentsList.get(paidInstallmentsList.size() - 1).getPrinPaidTillDate().add(prinAdjustmentDTO.getPrinPaid()))
                .scPaidTillDate(paidInstallmentsList.isEmpty()
                        ? scPaidTillLastInstallment.add(scAdjustmentDTO.getScPaid())
                        : paidInstallmentsList.get(paidInstallmentsList.size() - 1).getScPaidTillDate().add(scAdjustmentDTO.getScPaid()))
                .installmentBeginPrinBalance(paidInstallmentsList.isEmpty()
                        ? installmentBeginPrinBalance
                        : paidInstallmentsList.get(paidInstallmentsList.size() - 1).getInstallmentEndPrinBalance())
                .installmentEndPrinBalance(paidInstallmentsList.isEmpty()
                        ? installmentBeginPrinBalance.subtract(prinAdjustmentDTO.getPrinPaid())
                        : paidInstallmentsList.get(paidInstallmentsList.size() - 1).getInstallmentEndPrinBalance().subtract(prinAdjustmentDTO.getPrinPaid()))
                .build();
    }

    private PassbookCalculationDTO adjustLastInstallmentDue(Passbook lastPassbookEntry, BigDecimal paidAmount) {
        PassbookCalculationDTO scAdjustmentDTO = doesPaidAmountPaysOffServiceCharge(paidAmount, lastPassbookEntry.getScRemainForThisInst());
        PassbookCalculationDTO prinAdjustmentDTO = doesPaidAmountPaysOffPrincipal(scAdjustmentDTO.getRemainingAmount(), lastPassbookEntry.getPrinRemainForThisInst());

        return PassbookCalculationDTO
                .builder()
                .scPaid(scAdjustmentDTO.getScPaid())
                .scRemainForThisInst(scAdjustmentDTO.getScRemainForThisInst())
                .isSCFullyPaid(scAdjustmentDTO.getIsSCFullyPaid())
                .prinPaid(prinAdjustmentDTO.getPrinPaid())
                .prinRemainForThisInst(prinAdjustmentDTO.getPrinRemainForThisInst())
                .isPrincipalFullyPaid(prinAdjustmentDTO.getIsPrincipalFullyPaid())
                .remainingAmount(prinAdjustmentDTO.getRemainingAmount())
                .build();
    }

    private static PassbookCalculationDTO adjustLastInstallmentDueFlatRatio(Passbook lastPassbookEntry, BigDecimal paidAmount, BigDecimal effectiveServiceChargeRate, LoanCalculationMetaProperty loanCalculationMetaProperty) {
        log.info("last installment due exists. last installment due prin : {} | sc : {}", lastPassbookEntry.getPrinRemainForThisInst(), lastPassbookEntry.getScRemainForThisInst());
        log.info("effective service charge rate : {}", effectiveServiceChargeRate);

        BigDecimal lastInstallmentDue = lastPassbookEntry.getPrinRemainForThisInst().add(lastPassbookEntry.getScRemainForThisInst());

        if (paidAmount.compareTo(lastInstallmentDue) >= 0) {
            log.info("due paid fully.");
            return PassbookCalculationDTO
                    .builder()
                    .scPaid(lastPassbookEntry.getScRemainForThisInst())
                    .scRemainForThisInst(BigDecimal.ZERO)
                    .isSCFullyPaid(true)
                    .prinPaid(lastPassbookEntry.getPrinRemainForThisInst())
                    .prinRemainForThisInst(BigDecimal.ZERO)
                    .isPrincipalFullyPaid(true)
                    .remainingAmount(paidAmount.subtract(lastInstallmentDue))
                    .build();
        } else {
            log.info("due paid partially");
            BigDecimal prinPaid = paidAmount.divide(effectiveServiceChargeRate.add(BigDecimal.ONE),
                            loanCalculationMetaProperty.getServiceChargeAmountPrecision(),
                            CommonFunctions.getRoundingMode(loanCalculationMetaProperty.getRoundingLogic()));
            BigDecimal scPaid = paidAmount.subtract(prinPaid);

            log.info("prin paid : {} | sc paid : {}", prinPaid, scPaid);
            return PassbookCalculationDTO
                    .builder()
                    .scPaid(scPaid)
                    .scRemainForThisInst(lastPassbookEntry.getScRemainForThisInst().subtract(scPaid))
                    .isSCFullyPaid(false)
                    .prinPaid(prinPaid)
                    .prinRemainForThisInst(lastPassbookEntry.getPrinRemainForThisInst().subtract(prinPaid))
                    .isPrincipalFullyPaid(false)
                    .remainingAmount(BigDecimal.ZERO)
                    .build();
        }
    }

    private static InstallmentCalculationDTO buildDueInstallmentCalculationDTO(PassbookCalculationDTO passbookCalculationDTO, Passbook lastPassbookEntry, Integer installNo, BigDecimal prinPaidTillLastInstallment, BigDecimal scPaidTillLastInstallment, BigDecimal installmentBeginPrinBalance, RepaymentScheduleResponseDTO repaymentSchedule) {
        return InstallmentCalculationDTO
                .builder()
                .loanAccountId(lastPassbookEntry.getLoanAccountId() == null ? lastPassbookEntry.getDisbursedLoanAccountId() : lastPassbookEntry.getLoanAccountId())
                .installmentNo(installNo)
                .installDate(repaymentSchedule.getInstallDate())
                .loanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId())
                .prinPaid(passbookCalculationDTO.getPrinPaid())
                .prinRemainForThisInst(passbookCalculationDTO.getPrinRemainForThisInst())
                .serviceChargePaid(passbookCalculationDTO.getScPaid())
                .scRemainForThisInst(passbookCalculationDTO.getScRemainForThisInst())
                .prinPaidTillDate(prinPaidTillLastInstallment.add(passbookCalculationDTO.getPrinPaid()))
                .scPaidTillDate(scPaidTillLastInstallment.add(passbookCalculationDTO.getScPaid()))
                .installmentBeginPrinBalance(installmentBeginPrinBalance)
                .installmentEndPrinBalance(installmentBeginPrinBalance.subtract(passbookCalculationDTO.getPrinPaid()))
                .build();
    }

    Passbook buildPassbook(InstallmentCalculationDTO installmentCalculationDTO, Passbook lastPassbookEntry, PassbookRequestDTO requestDTO) {
        return Passbook
                .builder()
                .transactionId(requestDTO.getTransactionId())
                .managementProcessId(requestDTO.getManagementProcessId())
                .processId(requestDTO.getProcessId())
                .transactionCode(requestDTO.getTransactionCode())
                .memberId(lastPassbookEntry.getMemberId())
                .loanAccountId(installmentCalculationDTO.getLoanAccountId())
                .loanAccountOid(lastPassbookEntry.getLoanAccountOid())
                .passbookNumber(lastPassbookEntry.getPassbookNumber())
                .loanRepayScheduleId(installmentCalculationDTO.getLoanRepayScheduleId())
                .installNo(installmentCalculationDTO.getInstallmentNo())
                .installDate(installmentCalculationDTO.getInstallDate())
                .transactionDate(requestDTO.getTransactionDate())
                .installmentBeginPrinBalance(installmentCalculationDTO.getInstallmentBeginPrinBalance())
                .prinPaid(installmentCalculationDTO.getPrinPaid())
                .prinPaidTillDate(installmentCalculationDTO.getPrinPaidTillDate())
                .prinRemainForThisInst(installmentCalculationDTO.getPrinRemainForThisInst())
                .serviceChargePaid(installmentCalculationDTO.getServiceChargePaid())
                .scPaidTillDate(installmentCalculationDTO.getScPaidTillDate())
                .scRemainForThisInst(installmentCalculationDTO.getScRemainForThisInst())
                .installmentEndPrinBalance(installmentCalculationDTO.getInstallmentEndPrinBalance())
                .mfiId(requestDTO.getMfiId())
                .officeId(requestDTO.getOfficeId())
                .status(Status.STATUS_ACTIVE.getValue())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDTO.getLoginId())
                .paymentMode(requestDTO.getPaymentMode())
                .source(requestDTO.getSource())
                .samityId(requestDTO.getSamityId())
                .build();
    }

    Mono<List<PassbookResponseDTO>> insertPassbookEntry(List<Passbook> passbookList) {
        return port.insertRecordPassbooksList(passbookList)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class))
                .collectList();
    }

    private PassbookCalculationDTO doesPaidAmountPaysOffServiceCharge(BigDecimal paidAmount, BigDecimal remainingServiceCharge) {
        BigDecimal scPaid;
        boolean isSCFullyPaid;

        if (paidAmount.doubleValue() >= remainingServiceCharge.doubleValue()) {
            scPaid = remainingServiceCharge;
            isSCFullyPaid = true;
        } else {
            scPaid = paidAmount;
            isSCFullyPaid = false;
        }

        BigDecimal remainingAmount = paidAmount.subtract(scPaid);

        return PassbookCalculationDTO
                .builder()
                .scPaid(scPaid)
                .isSCFullyPaid(isSCFullyPaid)
                .scRemainForThisInst(remainingServiceCharge.subtract(scPaid))
                .remainingAmount(remainingAmount)
                .build();
    }

    private PassbookCalculationDTO doesPaidAmountPaysOffPrincipal(BigDecimal paidAmount, BigDecimal remainingPrincipal) {
        BigDecimal prinPaid;
        boolean isPrincipalFullyPaid;

        if (paidAmount.doubleValue() >= remainingPrincipal.doubleValue()) {
            prinPaid = remainingPrincipal;
            isPrincipalFullyPaid = true;
        } else {
            prinPaid = paidAmount;
            isPrincipalFullyPaid = false;
        }
        BigDecimal remainingAmount = paidAmount.subtract(prinPaid);
        return PassbookCalculationDTO
                .builder()
                .isPrincipalFullyPaid(isPrincipalFullyPaid)
                .prinPaid(prinPaid)
                .prinRemainForThisInst(remainingPrincipal.subtract(prinPaid))
                .remainingAmount(remainingAmount)
                .build();
    }

    public Mono<Passbook> getLastPassbookEntry(String loanAccountId) {
        return port.getLastPassbookEntry(loanAccountId)
                .doOnNext(passbook -> log.debug("Last passbook entity install no : {}", passbook.getInstallNo()));
    }

    public Mono<Passbook> getLastPassbookEntryV1(String loanAccountId) {
        return port.getLastPassbookEntry(loanAccountId)
                .doOnNext(passbook -> log.debug("Last passbook entity install no : {}", passbook.getInstallNo()));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccount(String savingsAccountId) {
        return port
                .getLastPassbookEntryBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(savingsAccountUseCase.getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                        .map(SavingsAccountResponseDTO::getOid)
                        .flatMap(port::getLastPassbookEntryBySavingsAccountOid))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Record found for Savings Account : " + savingsAccountId)))
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccountForStagingData(String savingsAccountId) {
        return port
                .getLastPassbookEntryBySavingsAccountId(savingsAccountId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccountOid(String savingsAccountOid) {
        return port.getLastPassbookEntryBySavingsAccountOid(savingsAccountOid)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<List<PassbookResponseDTO>> createPassbookEntryForSavings(PassbookRequestDTO passbookRequestDTO) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(passbookRequestDTO.getSavingsAccountId())
                .doOnRequest(value -> log.debug("Requesting to get savings account details : {}", passbookRequestDTO.getSavingsAccountId()))
                .doOnNext(savingsAccountResponseDTO -> log.debug("received savings account : {}",
                        savingsAccountResponseDTO))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Savings Account found with id : " + passbookRequestDTO.getSavingsAccountId())))
                .flatMap(savingsAccountResponseDTO -> this.createPassbookEntryForSavingsDeposit(buildSavingsDepositCommand(savingsAccountResponseDTO, passbookRequestDTO)))
                .map(List::of)
                .doOnError(throwable -> log.debug("ERROR happened while creating passbook entry for Savings : {}",
                        throwable.getMessage()));

    }

    private SavingsDepositCommand buildSavingsDepositCommand(SavingsAccountResponseDTO savingsAccountResponseDTO, PassbookRequestDTO passbookRequestDTO) {

        return SavingsDepositCommand
                .builder()
                .savingsAccountResponseDTO(savingsAccountResponseDTO)
                .amount(passbookRequestDTO.getAmount())
                .transactionId(passbookRequestDTO.getTransactionId())
                .transactionCode(passbookRequestDTO.getTransactionCode())
                .mfiId(passbookRequestDTO.getMfiId())
                .transactionDate(passbookRequestDTO.getTransactionDate())
                .loginId(passbookRequestDTO.getLoginId())
                .officeId(passbookRequestDTO.getOfficeId())
                .managementProcessId(passbookRequestDTO.getManagementProcessId() != null
                        ? passbookRequestDTO.getManagementProcessId()
                        : null)
                .processId(passbookRequestDTO.getProcessId() != null ? passbookRequestDTO.getProcessId() : null)
                .paymentMode(passbookRequestDTO.getPaymentMode())
                .source(passbookRequestDTO.getSource())
                .samityId(passbookRequestDTO.getSamityId())
                .build();
    }

    @Override
    public Mono<PassbookResponseDTO> createPassbookEntryForSavingsWithdraw(PassbookRequestDTO passbookRequestDTO) {
        String savingsAccountId = passbookRequestDTO.getSavingsAccountId();
        BigDecimal amount = passbookRequestDTO.getAmount();
        String transactionId = passbookRequestDTO.getTransactionId();
        String transactionCode = passbookRequestDTO.getTransactionCode();
        String mfiId = passbookRequestDTO.getMfiId();
        String loginId = passbookRequestDTO.getLoginId();
        LocalDate transactionDate = passbookRequestDTO.getTransactionDate();
        String paymentMode = passbookRequestDTO.getPaymentMode();
        String managementProcessId = passbookRequestDTO.getManagementProcessId();
        String processId = passbookRequestDTO.getProcessId();
        log.debug("passbook request dto : {}", passbookRequestDTO);
        log.debug("savings account id received : {}", savingsAccountId);

        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .doOnRequest(value -> log.debug("Request received to get account details by savings Account id : {}", savingsAccountId))
                .doOnNext(savingsAccountResponseDTO -> log.debug("savings account received : {}", savingsAccountResponseDTO))
                .flatMap(savingsAccountResponseDTO -> this.validateSavingsAccountForWithdraw(savingsAccountResponseDTO, amount))
                .flatMap(tuple3 -> !tuple3.getT1()
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.VALIDATION_FAILED.getValue()))
                        : Mono.just(buildPassbookForSavingsWithdraw(tuple3, transactionDate, transactionId, transactionCode, amount, loginId, paymentMode, mfiId, managementProcessId, processId, passbookRequestDTO))
                        .flatMap(this::insertRecordPassbook)
                        .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class)));
    }


    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn(
            String savingsAccountId, LocalDate transactionDate) {
        return port
                .getPassbookEntriesBySavingsAccountIDAndDate(savingsAccountId, transactionDate)
                .doOnRequest(value -> log.debug("Passbook Service | request Received to get passbook entries"))
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<AccruedInterestResponseDTO> createPassbookEntryForTotalAccruedInterestDeposit(
            PassbookRequestDTO passbookRequestDTO) {
        BigDecimal accruedInterest = passbookRequestDTO.getAmount();
        String accruedInterDepositId = passbookRequestDTO.getAccruedInterDepositId() != null
                ? passbookRequestDTO.getAccruedInterDepositId()
                : null;
        String transactionCode = passbookRequestDTO.getTransactionCode();
        String transactionId = passbookRequestDTO.getTransactionId();
        String savingsAccountId = passbookRequestDTO.getSavingsAccountId();
        LocalDate transactionDate = passbookRequestDTO.getTransactionDate();
        String loginId = passbookRequestDTO.getLoginId();
        String mfiId = passbookRequestDTO.getMfiId();

        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .flatMap(savingsAccountResponseDTO -> port
                        .getLastPassbookEntryBySavingsAccountId(savingsAccountId)
                        .doOnNext(passbook -> log.debug("received passbook entry for savings account : {}, {}",
                                savingsAccountId, passbook))
                        .flatMap(lastPassbookEntry ->
                                commonRepository.getMemberSamityOfficeInfoBySavingsAccountId(savingsAccountId)
                                        .map(memberSamityOfficeEntity -> buildPassbookForAccruedInterest(savingsAccountId, transactionDate, transactionId, transactionCode, mfiId, savingsAccountResponseDTO.getMemberId(), accruedInterDepositId, accruedInterest, lastPassbookEntry, loginId, savingsAccountResponseDTO, memberSamityOfficeEntity))))
                .flatMap(port::insertRecordPassbook)
                .flatMap(passbookEntity -> {
                    Mono<PassbookEntity> passbookEntityMono = Mono.just(passbookEntity);
                    if (passbookEntity.getAccruedInterDepositId() == null) {
                        passbookEntityMono = fdrPersistencePort
                                .updateScheduleStatus(savingsAccountId, transactionDate, Status.STATUS_PAID.getValue())
                                .map(fdrSchedule -> passbookEntity);
                    }
                    return passbookEntityMono;
                })
                .map(passbookEntity -> modelMapper.map(passbookEntity, AccruedInterestResponseDTO.class));
    }

    @Override
    public Mono<List<String>> getRepayScheduleIdListByTransactionList(List<String> transactionIdList) {
        return port.getRepayScheduleIdByTransactionList(transactionIdList)
                .collectList();
    }

    @Override
    public Mono<List<String>> deletePassbookEntryByTransaction(List<String> transactionIdList) {
        return port.deletePassbookEntryByTransaction(transactionIdList);
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntitiesByYearMonthAndSavingsAccountOid(Integer yearValue,
                                                                                        Integer monthValue, String savingsAccountOid) {
        return port.getPassbookEntriesByYearMonthAndSavingsAccountOid(yearValue, monthValue, savingsAccountOid)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndAccountTypeAndPaymentMode(
            String processManagementId, String accountType, String paymentMode) {

        Flux<Passbook> passbookFlux;

        if (paymentMode == null) {
            if (ACCOUNT_TYPE_LOAN.getValue().equalsIgnoreCase(accountType)) {
                passbookFlux = port.getLoanPassbookEntriesByProcessManagementId(processManagementId);
            } else if (ACCOUNT_TYPE_SAVINGS.getValue().equalsIgnoreCase(accountType)) {
                passbookFlux = port.getSavingsPassbookEntriesByProcessManagementId(processManagementId);
            } else {
                return Flux.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid accountType"));
            }
        } else {
            passbookFlux = port.getPassbookEntriesByProcessManagementIdAndPaymentMode(processManagementId, paymentMode);
        }

        return passbookFlux.map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));

    }

    @Override
    public Mono<PassbookResponseDTO> createPassbookEntryForDisbursement(PassbookRequestDTO passbookRequestDTO) {
        return loanAccountUseCase
                .getLoanAccountDetailsByLoanAccountId(passbookRequestDTO.getLoanAccountId())
                .doOnNext(loanAccountResponseDTO -> log.info("loan account details received : {}", loanAccountResponseDTO))
                .map(loanAccountResponseDTO -> buildPassbookForDisbursement(loanAccountResponseDTO, passbookRequestDTO))
                .doOnNext(passbook -> log.info("passbook entity created for disbursement : {}", passbook))
                .flatMap(port::insertRecordPassbook)
                .doOnRequest(l -> log.info("request received to insert passbook entry for disbursement "))
                .doOnSuccess(passbookEntity -> log.info("passbook entry for disbursement saved successfully. {}",
                        passbookEntity))
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));

    }

    private Passbook buildPassbookForDisbursement(LoanAccountResponseDTO loanAccountResponseDTO,
                                                  PassbookRequestDTO requestDTO) {
        return Passbook
                .builder()
                .managementProcessId(requestDTO.getManagementProcessId())
                .transactionId(requestDTO.getTransactionId())
                .transactionCode(requestDTO.getTransactionCode())
                .memberId(requestDTO.getMemberId())
                .passbookNumber(UUID.randomUUID().toString())
                .transactionDate(requestDTO.getTransactionDate())
                .mfiId(requestDTO.getMfiId())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDTO.getLoginId())
                .status(Status.STATUS_ACTIVE.getValue())
                .paymentMode(requestDTO.getPaymentMode())
                .disbursedLoanAccountId(requestDTO.getLoanAccountId())
                .loanAmount(requestDTO.getAmount())
                .calculatedServiceCharge(requestDTO.getCalculatedServiceCharge())
                .disbursementDate(requestDTO.getTransactionDate())
                .loanAccountOid(loanAccountResponseDTO.getOid())
                .processId(requestDTO.getProcessId())
                .officeId(requestDTO.getOfficeId())
                .source(requestDTO.getSource())
                .build();
    }

    private Passbook buildPassbookForAccruedInterest(String savingsAccountId, LocalDate transactionDate, String transactionId, String transactionCode, String mfiId, String memberId, String accruedInterDepositId, BigDecimal accruedInterest, Passbook lastPassbookEntry, String loginId, SavingsAccountResponseDTO savingsAccountResponseDTO, MemberSamityOfficeEntity memberSamityOfficeEntity) {

        return Passbook
                .builder()
                .savingsAccountId(savingsAccountId)
                .transactionDate(transactionDate)
                .transactionId(transactionId)
                .transactionCode(transactionCode)
                .mfiId(mfiId)
                .memberId(savingsAccountResponseDTO.getMemberId())
                .passbookNumber(UUID.randomUUID().toString())
                .accruedInterDepositId(accruedInterDepositId)
                /* .totalAccruedInterDeposit(accruedInterest) */
                .depositAmount(accruedInterest)
                .totalAccruedInterDeposit(lastPassbookEntry.getTotalAccruedInterDeposit() != null
                        ? lastPassbookEntry.getTotalAccruedInterDeposit().add(accruedInterest)
                        : accruedInterest)
                .savgAcctBeginBalance(lastPassbookEntry.getSavgAcctEndingBalance())
                .savgAcctEndingBalance(
                        lastPassbookEntry.getSavgAcctEndingBalance().add(accruedInterest))
                .savingsAvailableBalance(lastPassbookEntry.getSavgAcctEndingBalance()
                        .add(accruedInterest)
                        .subtract(savingsAccountResponseDTO.getMinBalance())
                        .compareTo(BigDecimal.ZERO) >= 0
                        ? lastPassbookEntry.getSavgAcctEndingBalance().add(accruedInterest)
                        .subtract(savingsAccountResponseDTO.getMinBalance())
                        : BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .status(Status.STATUS_ACTIVE.getValue())
                .savingsAccountOid(savingsAccountResponseDTO.getOid())
                .samityId(memberSamityOfficeEntity.getSamityId())
                .build();
    }


    private Passbook buildPassbookForSavingsWithdraw(Tuple3<Boolean, SavingsAccountResponseDTO, Passbook> tuple3, LocalDate transactionDate, String transactionId, String transactionCode, BigDecimal amount, String loginId, String paymentMode, String mfiId, String managementProcessId, String processId, PassbookRequestDTO passbookRequestDTO) {
        return Passbook
                .builder()
                .managementProcessId(managementProcessId)
                .processId(processId)
                .savingsAccountId(tuple3.getT2().getSavingsAccountId())
                .transactionDate(transactionDate)
                .transactionId(transactionId)
                .transactionCode(transactionCode)
                .memberId(tuple3.getT2().getMemberId())
                .passbookNumber(tuple3.getT3().getPassbookNumber())
                .withdrawAmount(amount)
                .savgAcctBeginBalance(tuple3.getT3().getSavgAcctEndingBalance())
                .savgAcctEndingBalance(
                        tuple3.getT3().getSavgAcctEndingBalance().subtract(amount))
                .savingsAvailableBalance(tuple3.getT3().getSavgAcctEndingBalance()
                        .subtract(amount).subtract(tuple3.getT2().getMinBalance()))
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .status(Status.STATUS_ACTIVE.getValue())
                .totalWithdrawAmount(tuple3.getT3().getTotalWithdrawAmount() == null ? amount
                        : tuple3.getT3().getTotalWithdrawAmount().add(amount))
                .totalDepositAmount(tuple3.getT3().getTotalDepositAmount())
                .paymentMode(paymentMode)
                .mfiId(mfiId)
                .savingsAccountOid(tuple3.getT2().getOid())
                .officeId(passbookRequestDTO.getOfficeId())
                .savingsTypeId(tuple3.getT2().getSavingsTypeId())
                .samityId(passbookRequestDTO.getSamityId())
                .build();
    }

    @Override
    public Mono<PassbookResponseDTO> getDisbursementPassbookEntryByDisbursedLoanAccountId(
            String disbursedLoanAccountId) {
        return port.getDisbursementPassbookEntryByDisbursedLoanAccountId(disbursedLoanAccountId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getWithdrawPassbookEntriesByManagementProcessIdAndPaymentMode(
            String managementProcessId, String paymentMode) {
        return port.getWithdrawPassbookEntriesByManagementProcessIdAndPaymentMode(managementProcessId, paymentMode)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<AccruedInterestResponseDTO> createPassbookEntryForInterestDeposit(
            PassbookRequestDTO passbookRequestDTO) {

        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(passbookRequestDTO.getSavingsAccountId())
                .doOnNext(savingsAccountResponseDTO -> log.info("savingsAccountOid : {}", savingsAccountResponseDTO.getOid()))
                .flatMap(savingsAccountResponseDTO ->
                        port
                                .getLastPassbookEntryBySavingsAccountId(passbookRequestDTO.getSavingsAccountId())
                                .switchIfEmpty(port.getLastPassbookEntryBySavingsAccountOid(savingsAccountResponseDTO.getOid()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry Found for : " + passbookRequestDTO.getSavingsAccountId())))
                                .map(lastPassbookEntry -> buildPassbookForInterestDeposit(lastPassbookEntry, passbookRequestDTO, savingsAccountResponseDTO))
                                .flatMap(passbook -> port
                                        .getLastInterestDepositPassbookEntryBySavingsAccountOid(savingsAccountResponseDTO.getOid())
                                        .doOnNext(passbook1 -> log.info("last interest deposit passbook entry : {}", passbook1))
                                        .map(interestDepositPassbookEntry -> {
                                            passbook.setTotalAccruedInterDeposit(interestDepositPassbookEntry.getTotalAccruedInterDeposit().add(passbookRequestDTO.getAmount()));
                                            return passbook;
                                        })
                                        .switchIfEmpty(Mono.just(passbook)))
                                .doOnNext(passbook -> log.info("Passbook to be saved for Interest Deposit : {}", passbook))
                                .flatMap(port::insertRecordPassbook)
                                .doOnSuccess(passbookEntity -> log.info("successfully saved passbook to db : {}", passbookEntity))
                                .map(passbookEntity -> modelMapper.map(passbookEntity, AccruedInterestResponseDTO.class)));
    }

    @Override
    public Mono<List<Passbook>> createPassbookEntryForSavingsAccountForLoanAdjustment(List<Passbook> passbookList) {
        return port
                .createPassbookEntryForLoanAdjustment(passbookList);
    }

    @Override
    public Mono<List<PassbookResponseDTO>> getRepaymentScheduleAndCreatePassbookEntryForLoanV1(PassbookRequestDTO passbookRequestDTO) {
        return null;
    }

    @Override
    public Mono<PassbookResponseDTO> createPassbookEntryForTermDepositClosure(PassbookRequestDTO passbookRequestDTO) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(passbookRequestDTO.getSavingsAccountId())
                .flatMap(savingsAccountResponseDTO ->
                        port
                                .getLastPassbookEntryBySavingsAccountOid(savingsAccountResponseDTO.getOid())
                                .switchIfEmpty(port.getLastPassbookEntryBySavingsAccountOid(savingsAccountResponseDTO.getOid()))
                                .doOnNext(passbook -> log.info("Last passbook received"))
                                .map(passbook -> buildPassbookForTermDepositClosure(passbookRequestDTO, passbook))
                                .doOnNext(passbook -> log.info("Passbook building successful."))
                                .flatMap(passbook -> port
                                        .getLastWithdrawPassbookEntryBySavingsAccountOid(savingsAccountResponseDTO.getOid())
                                        .map(withdrawPassbookEntry -> {
                                            passbook.setTotalWithdrawAmount(withdrawPassbookEntry.getTotalWithdrawAmount().add(passbookRequestDTO.getAmount()));
                                            return passbook;
                                        })
                                        .switchIfEmpty(Mono.just(passbook))))
                .flatMap(port::insertRecordPassbook)
                .doOnNext(passbookEntity -> log.info("save passbook successful"))
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastInterestDepositPassbookEntryBySavingsAccountOid(String savingsAccountOid) {
        return port.getLastInterestDepositPassbookEntryBySavingsAccountOid(savingsAccountOid)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Mono<List<PassbookResponseDTO>> getPassbookEntriesBetweenTransactionDates(String savingsAccountId, LocalDate fromDate, LocalDate toDate) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("request to get savings Account details : {}", savingsAccountId))
                .doOnNext(savingsAccountResponseDTO -> log.info("received savingsAccountDetails : {}", savingsAccountResponseDTO))
                .map(SavingsAccountResponseDTO::getOid)
                .flatMap(savingsAccountOid -> port
                        .getPassbookEntriesBetweenTransactionDates(savingsAccountOid, fromDate, toDate)
                        .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class))
                        .collectList()
                        .doOnNext(passbookResponseDTOS -> log.info("Successfully fetched passbook entries from : {} | to : {} | passbookList : {}", fromDate, toDate, passbookResponseDTOS)));
    }

    @Override
    public Mono<List<String>> deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(String managementProcessId, String passbookProcessId) {
        return port.deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(managementProcessId, passbookProcessId);
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(String processManagementId, String transactionCode, String paymentMode, String savingsTypeId) {
        return port.getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(processManagementId, transactionCode, paymentMode, savingsTypeId);
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryByTransactionCodeAndLoanAccountOid(String transactionCode, String loanAccountOid) {
        return port.getLastPassbookEntryByTransactionCodeAndLoanAccountOid(transactionCode, loanAccountOid);
    }

    @Override
    public Mono<List<PassbookResponseDTO>> getPassbookEntriesByTransactionCodeAndSavingsAccountId(String transactionCode, String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(SavingsAccountResponseDTO::getOid)
                .flatMapMany(savingsAccountOid -> port.getPassbookEntryByTransactionCodeAndSavingsAccountOid(transactionCode, savingsAccountOid))
                .collectList();
    }

    @Override
    public Mono<List<PassbookResponseDTO>> getPassbookEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId) {
        return port
                .getPassbookEntriesByTransactionCodeAndManagementProcessId(transactionCode, managementProcessId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class))
                .collectList()
                ;
    }

    @Override
    public Mono<Passbook> createPassbookEntryForWelfareFund(Passbook passbook) {
        return port.createPassbookEntryForWelfareFund(passbook);
    }

    @Override
    public Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList) {
        if (savingsAccountIdList.isEmpty()) {
            return Mono.just(0);
        }
        return port.deletePostedInterestBySavingsAccountIdList(managementProcessId, savingsAccountIdList);
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesForAdvanceLoanRepaymentDebit(String officeId, LocalDate businessDate) {
        return port.getPassbookEntriesForAdvanceLoanRepaymentDebit(officeId, businessDate)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesForAdvanceLoanRepaymentCredit(String officeId, LocalDate businessDate) {
        return port.getPassbookEntriesForAdvanceLoanRepaymentCredit(officeId, businessDate)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesForPrincipalAndServiceChargeOutstanding(String managementProcessId, LocalDate businessDate) {

        return port.getLoanPassbookEntriesByProcessManagementId(managementProcessId)
                .filter(passbook -> (passbook.getInstallDate().isEqual(businessDate) || passbook.getInstallDate().isBefore(businessDate)) && passbook.getTransactionDate().isEqual(businessDate)) // fix patch for Advance payment credit
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class))
                .doOnComplete(() -> log.info("Passbook entries for Principal and Service Charge Outstanding fetched successfully"))
                .doOnError(throwable -> log.error("Error occurred while fetching passbook entries for Principal and Service Charge Outstanding : {}", throwable.getMessage()))
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesForPrincipalAndServiceChargeOutstandingV2(String managementProcessId) {
        return port.getLoanPassbookEntriesByProcessManagementId(managementProcessId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(String managementProcessId, String transactionCode, String savingsTypeId) {
        return port.getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(managementProcessId, transactionCode, savingsTypeId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> createPassbookEntryForLoanRebateAndWriteOff(PassbookRequestDTO passbookRequestDTO) {
        return this.getRepaymentScheduleAndCreatePassbookEntryForLoan(passbookRequestDTO)
                .flatMapMany(Flux::fromIterable);

    }

    @Override
    public Mono<Tuple2<Map<LocalDate, String>, String>> archivePassbookEntriesByTransactionDateAndLater(String accountId, String accountType, LocalDate transactionDate, String loginId, String managementProcessId) {
        AtomicReference<String> savingsTypeId = new AtomicReference<>();
        return port.getPassbookEntriesByTransactionDateAndLater(accountId, accountType, transactionDate)
                .map(passbook -> {
                    savingsTypeId.set(passbook.getSavingsTypeId() == null ? "" : passbook.getSavingsTypeId());
                    return passbook;
                })
                .collectList()
                .flatMap(passbooks -> {
                    if (passbooks.isEmpty()) {
                        return Mono.just(Tuples.of(Map.of(), savingsTypeId.get() == null ? "" : savingsTypeId.get()));
                    }
                    List<String> passbookOids = passbooks.stream().map(Passbook::getOid).toList();
                    Map<LocalDate, String> laterTransactionDateIdMap = this.getLaterTransactionIds(passbooks, transactionDate);
                    if (accountType.equals(ACCOUNT_TYPE_LOAN.getValue())) {
                        Integer firstInstallmentNo = this.getFirstInstallmentNo(passbooks);
                        return loanRepaymentScheduleUseCase.updateInstallmentStatusFromInstallmentNoToLast(firstInstallmentNo, Status.STATUS_PENDING.getValue(), accountId, managementProcessId)
                                .collectList()
                                .flatMap(list -> passbookHistoryUseCase.archivePassbookHistory(passbooks, loginId))
                                .flatMap(aBoolean -> port.deletePassbookEntriesByOid(passbookOids))
                                .thenReturn(Tuples.of(laterTransactionDateIdMap, savingsTypeId.get()));
                    } else {
                        return this.updateDpsRepaymentSchedule(passbooks, accountType, loginId)
                                .flatMap(passbooks1 -> passbookHistoryUseCase.archivePassbookHistory(passbooks, loginId))
                                .flatMap(aBoolean -> port.deletePassbookEntriesByOid(passbookOids))
                                .thenReturn(Tuples.of(laterTransactionDateIdMap, savingsTypeId.get()));
                    }
                });
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByTransactionId(String transactionId) {
        return port.getPassbookEntriesByTransactionId(transactionId)
                .map(passbook -> modelMapper.map(passbook, PassbookResponseDTO.class));
    }

    private Mono<List<Passbook>> updateDpsRepaymentSchedule(List<Passbook> passbooks, String accountType, String loginId) {
        if (!Constants.ACCOUNT_TYPE_SAVINGS.getValue().equals(accountType)) return Mono.just(passbooks);
        if (passbooks.isEmpty()) return Mono.just(passbooks);
        boolean isDpsAccount = passbooks.get(0).getSavingsTypeId().equals(SAVINGS_TYPE_ID_DPS.getValue());
        if (!isDpsAccount) return Mono.just(passbooks);
        List<String> managementProcessIds = passbooks.stream().map(Passbook::getManagementProcessId).toList();
        if (managementProcessIds.isEmpty()) return Mono.just(passbooks);

        return Flux.fromIterable(managementProcessIds)
            .flatMap(managementProcessId -> dpsRepaymentScheduleUseCase
                    .updateDPSRepaymentScheduleStatusByManagementProcessId(managementProcessId, Status.STATUS_PENDING.getValue(), loginId))
            .collectList()
            .map(booleans -> passbooks)
            .doOnError(throwable -> log.error("Error occurred while updating DPS Repayment Schedule Status : {}", throwable.getMessage()));
    }

    private Integer getFirstInstallmentNo(List<Passbook> passbooks) {
        return passbooks.stream()
                .sorted(Comparator.comparing(Passbook::getInstallNo))
                .map(Passbook::getInstallNo)
                .findFirst()
                .orElse(null);
    }

    private Map<LocalDate, String> getLaterTransactionIds(List<Passbook> passbooks, LocalDate transactionDate) {
        return passbooks.stream()
                .filter(passbook -> passbook.getTransactionDate().isAfter(transactionDate))
                .sorted(Comparator.comparing(Passbook::getTransactionDate))
                .collect(Collectors.toMap(
                        Passbook::getTransactionDate,   // Key: transactionDate
                        Passbook::getTransactionId,      // Value: transactionId
                        (existing, replacement) -> replacement // In case of duplicate dates, keep the replacement entry
                ));
    }



    private Passbook buildPassbookForTermDepositClosure(PassbookRequestDTO passbookRequestDTO, Passbook lastPassbookEntry) {
        return Passbook
                .builder()
                .managementProcessId(passbookRequestDTO.getManagementProcessId())
                .processId(passbookRequestDTO.getProcessId())
                .savingsAccountId(passbookRequestDTO.getSavingsAccountId())
                .transactionDate(passbookRequestDTO.getTransactionDate())
                .transactionId(passbookRequestDTO.getTransactionId())
                .transactionCode(passbookRequestDTO.getTransactionCode())
                .mfiId(passbookRequestDTO.getMfiId())
                .officeId(passbookRequestDTO.getOfficeId())
                .memberId(lastPassbookEntry.getMemberId())
                .passbookNumber(lastPassbookEntry.getPassbookNumber())
                .withdrawAmount(passbookRequestDTO.getAmount())
                .savgAcctBeginBalance(passbookRequestDTO.getSavgAcctBeginBalance())
                .savgAcctEndingBalance(BigDecimal.ZERO)
                .savingsAvailableBalance(BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .createdBy(passbookRequestDTO.getLoginId())
                .status(Status.STATUS_ACTIVE.getValue())
                .totalDepositAmount(passbookRequestDTO.getTotalDepositAmount())
                .totalWithdrawAmount(lastPassbookEntry.getTotalWithdrawAmount() == null
                        ? passbookRequestDTO.getAmount()
                        : lastPassbookEntry.getTotalWithdrawAmount().add(passbookRequestDTO.getAmount()))
                .totalAccruedInterDeposit(passbookRequestDTO.getTotalAccruedInterDeposit())
                .paymentMode(passbookRequestDTO.getPaymentMode())
                .savingsAccountOid(lastPassbookEntry.getSavingsAccountOid())
                .build();
    }

    private Passbook buildPassbookForInterestDeposit(Passbook lastPassbookEntry, PassbookRequestDTO passbookRequestDTO, SavingsAccountResponseDTO savingsAccountResponseDTO) {
        Passbook passbook = Passbook
                .builder()
                .processId(passbookRequestDTO.getProcessId())
                .managementProcessId(passbookRequestDTO.getManagementProcessId())
                .paymentMode(passbookRequestDTO.getPaymentMode())
                .savingsAccountId(passbookRequestDTO.getSavingsAccountId())
                .transactionDate(passbookRequestDTO.getTransactionDate())
                .transactionId(passbookRequestDTO.getTransactionId())
                .transactionCode(passbookRequestDTO.getTransactionCode())
                .mfiId(passbookRequestDTO.getMfiId())
                .memberId(passbookRequestDTO.getMemberId())
                .passbookNumber(lastPassbookEntry.getPassbookNumber() != null ? lastPassbookEntry.getPassbookNumber()
                        : UUID.randomUUID().toString())
                .depositAmount(passbookRequestDTO.getAmount())
                .totalDepositAmount(lastPassbookEntry.getTotalDepositAmount() != null
                        ? lastPassbookEntry.getTotalDepositAmount().add(passbookRequestDTO.getAmount())
                        : passbookRequestDTO.getAmount())
                .totalAccruedInterDeposit(lastPassbookEntry.getTotalAccruedInterDeposit() != null
                        ? lastPassbookEntry.getTotalAccruedInterDeposit().add(passbookRequestDTO.getAmount())
                        : passbookRequestDTO.getAmount())
                .savgAcctBeginBalance(lastPassbookEntry.getSavgAcctEndingBalance())
                .savgAcctEndingBalance(lastPassbookEntry.getSavgAcctEndingBalance().add(passbookRequestDTO.getAmount()))
                .savingsAvailableBalance(lastPassbookEntry.getSavgAcctEndingBalance()
                        .add(passbookRequestDTO.getAmount())
                        .subtract(savingsAccountResponseDTO.getMinBalance())
                        .compareTo(BigDecimal.ZERO) >= 0
                        ? lastPassbookEntry.getSavgAcctEndingBalance().add(passbookRequestDTO.getAmount())
                        .subtract(savingsAccountResponseDTO.getMinBalance())
                        : BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .createdBy(passbookRequestDTO.getLoginId())
                .status(Status.STATUS_ACTIVE.getValue())
                .savingsAccountOid(savingsAccountResponseDTO.getOid())
                .officeId(passbookRequestDTO.getOfficeId())
                .build();

        log.info("Built passbook for FDR to be saved to DB : {}", passbook);
        return passbook;
    }

    private Mono<Tuple3<Boolean, SavingsAccountResponseDTO, Passbook>> validateSavingsAccountForWithdraw(
            SavingsAccountResponseDTO savingsAccountResponseDTO, BigDecimal amount) {
        return savingsAccountResponseDTO.getStatus().equals(Status.STATUS_ACTIVE.getValue())
                ? port.getLastPassbookEntryBySavingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                .doOnRequest(value -> log.info("Request received to get last passbook entry by savings Account id : {}", savingsAccountResponseDTO.getSavingsAccountId()))
                .doOnNext(passbook -> log.info("last passbook entry for savings received : {}", passbook))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        ExceptionMessages.WITHDRAW_NOT_ALLOWED_NO_DEPOSIT_RECORD.getValue())))
                .map(passbook -> {
                    BigDecimal savingsAvailableBalance = passbook.getSavingsAvailableBalance();

                    log.debug("savings available balance : {}", savingsAvailableBalance);
                    log.debug("Requested amount to withdraw : {}", amount);
                    log.debug("after withdrawal savings available balance : {}", savingsAvailableBalance.subtract(amount));

                    return Tuples.of(
                            savingsAvailableBalance.compareTo(amount) >= 0,
                            savingsAccountResponseDTO, passbook);
                })
                : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                ExceptionMessages.SAVINGS_ACCOUNT_STATUS_IS_NOT_ACTIVE.getValue()));

    }

    private Mono<PassbookResponseDTO> createPassbookEntryForSavingsDeposit(
            SavingsDepositCommand savingsDepositCommand) {

        SavingsAccountResponseDTO savingsAccountResponseDTO = savingsDepositCommand.getSavingsAccountResponseDTO();
        log.debug("into create passbook entry for savings deposit");

        return port.getLastPassbookEntryBySavingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                .doOnNext(passbook -> log.debug("received passbook entry for savings account : {}, {}", savingsAccountResponseDTO.getSavingsAccountId(), passbook))
                .flatMap(passbook -> buildPassbookForSavingsDepositEntry(passbook, savingsDepositCommand))
                .switchIfEmpty(buildPassbookForFirstSavingsDepositEntry(savingsDepositCommand))
                .doOnNext(passbook -> log.debug("Passbook to be saved : {}", passbook))
                .flatMap(port::insertRecordPassbook)
                .flatMap(passbookEntity -> processDpsRepaymentSchedule(savingsAccountResponseDTO, savingsDepositCommand, passbookEntity))
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    private Mono<PassbookEntity> processDpsRepaymentSchedule(SavingsAccountResponseDTO savingsAccountResponseDTO, SavingsDepositCommand savingsDepositCommand, PassbookEntity passbookEntity) {
        if (savingsAccountResponseDTO.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue())) {
            return dpsRepaymentScheduleUseCase.getDpsRepaymentScheduleBySavingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                    .doOnRequest(l -> log.info("Request received to get DPS Repayment Schedule"))
                    .flatMap(dpsRepaymentList -> processPendingRepayments(dpsRepaymentList, savingsAccountResponseDTO, savingsDepositCommand, passbookEntity));
        }
        return Mono.just(passbookEntity);
    }


    private Mono<PassbookEntity> processPendingRepayments(List<DpsRepaymentDTO> dpsRepaymentList, SavingsAccountResponseDTO savingsAccountResponseDTO, SavingsDepositCommand savingsDepositCommand, PassbookEntity passbookEntity) {
        List<Integer> dpsRepaymentPendingList = dpsRepaymentList.stream()
                .filter(dpsRepaymentScheduleResponseDTO -> dpsRepaymentScheduleResponseDTO.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
                .map(DpsRepaymentDTO::getRepaymentNo)
                .sorted(Comparator.naturalOrder())
                .toList();

        if (!dpsRepaymentPendingList.isEmpty()) {
            int firstPendingRepaymentNumber = dpsRepaymentPendingList.get(0);
            log.info("First Pending Repayment Number : {}", firstPendingRepaymentNumber);
            return processPaidInstallments(savingsAccountResponseDTO, savingsDepositCommand, passbookEntity, firstPendingRepaymentNumber);
        }
        return Mono.just(passbookEntity);
    }


    private Mono<PassbookEntity> processPaidInstallments(SavingsAccountResponseDTO savingsAccountResponseDTO, SavingsDepositCommand savingsDepositCommand, PassbookEntity passbookEntity, int firstPendingRepaymentNumber) {
        log.info("Savings Deposit Amount : {}", savingsDepositCommand.getAmount());
        log.info("DPS Installment Amount : {}", savingsAccountResponseDTO.getSavingsAmount());
        double noOfPaidInstallments = savingsDepositCommand.getAmount().doubleValue() / savingsAccountResponseDTO.getSavingsAmount().doubleValue();
        log.info("No of Paid Installments : {}", noOfPaidInstallments);
        if (noOfPaidInstallments % 1 != 0) {
            log.error("Paid amount should be equal or multiple of DPS Installment Amount");
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Paid amount should be equal or multiple of DPS Installment Amount"));
        }

        List<Integer> paidInstallmentNos = IntStream.range(0, (int) noOfPaidInstallments)
                .map(i -> firstPendingRepaymentNumber + i)
                .boxed()
                .collect(Collectors.toList());

        log.info("Paid Installment Nos : {}", paidInstallmentNos);

        if (!paidInstallmentNos.isEmpty()) {
            return dpsRepaymentScheduleUseCase.updateDPSRepaymentScheduleStatus(savingsAccountResponseDTO.getSavingsAccountId(), Status.STATUS_PAID.getValue(), paidInstallmentNos, passbookEntity.getManagementProcessId(), passbookEntity.getTransactionDate(), passbookEntity.getCreatedBy())
                    .thenReturn(passbookEntity);
        } else {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Paid amount cannot be less than DPS Installment Amount"));
        }
    }

    private Mono<Passbook> buildPassbookForFirstSavingsDepositEntry(SavingsDepositCommand savingsDepositCommand) {
        Passbook passbook = createPassbook(savingsDepositCommand);
        Mono<Passbook> passbookMono = Mono.just(passbook);

        if (savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue())) {
            return generateDPSRepaymentScheduleAndUpdateMaturityAmount(passbook, savingsDepositCommand);
        }

        if (savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue())) {
            return updateFDRInterestPostingDatesAndMaturityAmount(passbook, savingsDepositCommand)
                    .doOnNext(passbook1 -> log.info("passbook after updating fdr : {}", passbook1));
        }

        return passbookMono;
    }

    private Passbook createPassbook(SavingsDepositCommand savingsDepositCommand) {
        SavingsAccountResponseDTO savingsAccountResponseDTO = savingsDepositCommand.getSavingsAccountResponseDTO();
        return Passbook.builder()
                .managementProcessId(savingsDepositCommand.getManagementProcessId())
                .processId(savingsDepositCommand.getProcessId())
                .savingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                .transactionDate(savingsDepositCommand.getTransactionDate())
                .transactionId(savingsDepositCommand.getTransactionId())
                .transactionCode(savingsDepositCommand.getTransactionCode())
                .mfiId(savingsDepositCommand.getMfiId())
                .officeId(savingsDepositCommand.getOfficeId())
                .memberId(savingsAccountResponseDTO.getMemberId())
                .passbookNumber("Passbook-" + savingsAccountResponseDTO.getMemberId())
                .depositAmount(savingsDepositCommand.getAmount())
                .savgAcctBeginBalance(BigDecimal.ZERO)
                .savgAcctEndingBalance(savingsDepositCommand.getAmount())
                .savingsAvailableBalance(savingsDepositCommand.getAmount().subtract(savingsAccountResponseDTO.getMinBalance()).compareTo(BigDecimal.ZERO) > 0
                        ? savingsDepositCommand.getAmount().subtract(savingsAccountResponseDTO.getMinBalance())
                        : BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .createdBy(savingsDepositCommand.getLoginId())
                .status(Status.STATUS_ACTIVE.getValue())
                .totalDepositAmount(savingsDepositCommand.getAmount())
                .paymentMode(savingsDepositCommand.getPaymentMode())
                .savingsAccountOid(savingsAccountResponseDTO.getOid())
                .createdBy(savingsDepositCommand.getLoginId())
                .savingsTypeId(savingsAccountResponseDTO.getSavingsTypeId())
                .source(savingsDepositCommand.getSource())
                .samityId(savingsDepositCommand.getSamityId())
                .build();
    }

    private Mono<Passbook> generateDPSRepaymentScheduleAndUpdateMaturityAmount(Passbook passbook, SavingsDepositCommand savingsDepositCommand) {
        return dpsRepaymentScheduleUseCase
                .generateDpsRepaymentSchedule(DPSRepaymentCommand
                        .builder()
                        .firstInstallmentDate(passbook.getTransactionDate())
                        .savingsAccountId(passbook.getSavingsAccountId())
                        .loginId(passbook.getCreatedBy())
                        .build())
                .zipWith(savingsAccountUseCase
                        .getDPSAccountDetailsBySavingsAccountId(savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsAccountId()))
                .map(this::calculateDPSMaturityAmountWithoutCompounding)
                .flatMap(maturityAmount -> savingsAccountUseCase
                        .updateFDRDPSAccountMaturityAmount(savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsAccountId(), maturityAmount)
                        .doOnRequest(l -> log.info("Request received to update DPS Account Maturity Amount")))
                .doOnError(throwable -> log.error("Error occurred while updating DPS Account Maturity Amount : {}", throwable.getMessage()))
                .map(dpsRepaymentScheduleResponseDTO -> passbook);
    }


    private Mono<Passbook> updateFDRInterestPostingDatesAndMaturityAmount(Passbook passbook, SavingsDepositCommand savingsDepositCommand) {
        return this.validateFDRCollectionAmount(savingsDepositCommand)
                .doOnNext(aBoolean -> log.info("FDR Collection Amount validated successfully"))
                .flatMap(aBoolean -> getFDRInterestPostingDates(savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsAccountId(), passbook.getTransactionDate()))
                .flatMap(interestPostingDates -> {
                    log.info("interestPostingDates : {}", interestPostingDates);
                    String savingsAccountId = savingsDepositCommand.getSavingsAccountResponseDTO().getSavingsAccountId();
                    LocalDate acctEndDate = interestPostingDates.get(interestPostingDates.size() - 1);

                    return savingsAccountUseCase
                            .getFDRAccountDetailsBySavingsAccountId(savingsAccountId)
                            .map(fdrAccountDTO -> this.calculateFDRMaturityAmount(interestPostingDates, fdrAccountDTO, passbook.getTransactionDate()))
                            .doOnNext(maturityAmount -> log.info("Maturity Amount calculated : {}", maturityAmount))
                            .flatMap(maturityAmount -> savingsAccountUseCase
                                    .updateFDRDPSAccountMaturityAmount(savingsAccountId, maturityAmount))
                            .flatMap(savingsAccountResponseDTO -> savingsAccountUseCase
                                    .updateSavingsAccountInterestPostingDatesAndStartDateEndDate(interestPostingDates, savingsAccountId, passbook.getTransactionDate(), acctEndDate, passbook.getCreatedBy()))
                            .doOnSuccess(savingsAccountResponseDTO1 -> log.info("Successfully updated FDR Account Interest Posting Dates, End Date, Start Date"))
                            .doOnError(throwable -> log.error("Error occurred while updating FDR Account Interest Posting Dates, End Date, Start Date : {}", throwable.getMessage()))
                            .flatMap(savingsAccountResponseDTO1 -> Mono.just(passbook));
                });

    }

    private Mono<Boolean> validateFDRCollectionAmount(SavingsDepositCommand savingsDepositCommand) {
        SavingsAccountResponseDTO savingsAccountResponseDTO = savingsDepositCommand.getSavingsAccountResponseDTO();
        log.info("savings amount : {}", savingsAccountResponseDTO.getSavingsAmount());
        log.info("collection amount : {}", savingsDepositCommand.getAmount());
        return Mono.just(savingsAccountResponseDTO)
                .flatMap(savingsAccount -> savingsAccount.getSavingsAmount().compareTo(savingsDepositCommand.getAmount()) == 0
                        ? Mono.just(true)
                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Collection Amount should be equal to FDR Amount")));
    }

    public BigDecimal calculateFDRMaturityAmount(List<LocalDate> interestPostingDates, FDRAccountDTO fdrAccountDTO, LocalDate firstInstallmentDate) {
        BigDecimal fdrAmount = fdrAccountDTO.getSavingsAmount();
        BigDecimal annualInterestRate = CommonFunctions.getAnnualInterestRate(fdrAccountDTO.getInterestRate(), fdrAccountDTO.getInterestRateFrequency());
        BigDecimal interestRatePerDay = BigDecimal.valueOf(annualInterestRate.doubleValue() / 100 / 365).setScale(8, RoundingMode.HALF_UP);
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal fdrCompoundBalance = BigDecimal.ZERO;
        LinkedList<LocalDate> interestPostingDatesLinked = new LinkedList<>(interestPostingDates);
        interestPostingDatesLinked.add(0, firstInstallmentDate);
        interestPostingDatesLinked.sort(Comparator.naturalOrder());


        for (int i = 0; i < interestPostingDatesLinked.size() - 1; i++) {
            long numberOfDaysInBetween = Math.abs(ChronoUnit.DAYS.between(interestPostingDatesLinked.get(i), interestPostingDatesLinked.get(i + 1)));
            log.info("Number of days in between : {} & {} | {}", interestPostingDatesLinked.get(i), interestPostingDatesLinked.get(i + 1), numberOfDaysInBetween);
            BigDecimal currentBalance = fdrCompoundBalance.compareTo(BigDecimal.ZERO) == 0 ? fdrAmount : fdrCompoundBalance;
            log.info("Current Balance : {}", currentBalance);
            BigDecimal interestForThisPeriod = currentBalance.multiply(interestRatePerDay).multiply(BigDecimal.valueOf(numberOfDaysInBetween)).setScale(2, RoundingMode.HALF_UP);
            log.info("Interest for this period : {}", interestForThisPeriod);
            totalInterest = totalInterest.add(interestForThisPeriod);
            log.info("Total Interest : {}", totalInterest);
            fdrCompoundBalance = currentBalance.add(interestForThisPeriod);
            log.info("FDR Compound Balance : {}", fdrCompoundBalance);
        }
        return fdrAmount.add(totalInterest).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDPSMaturityAmountWithoutCompounding(Tuple2<DpsRepaymentScheduleResponseDTO, DPSAccountDTO> dpsRepaymentScheduleAndDPSAccount) {
        List<DpsRepaymentDTO> dpsRepaymentDTOList = dpsRepaymentScheduleAndDPSAccount.getT1().getRepaymentResponseList();
        DPSAccountDTO dpsAccountDTO = dpsRepaymentScheduleAndDPSAccount.getT2();
        LocalDate acctEndDate = dpsRepaymentScheduleAndDPSAccount.getT1().getAcctEndDate();
        log.info("acctEndDate to be added : {}", acctEndDate);
        BigDecimal totalInterest = BigDecimal.ZERO;

        List<LocalDate> repaymentDates = new ArrayList<>(dpsRepaymentDTOList
                .stream()
                .map(DpsRepaymentDTO::getRepaymentDate)
                .sorted()
                .toList());
        repaymentDates.add(acctEndDate);

        BigDecimal annualInterestRate = CommonFunctions.getAnnualInterestRate(dpsAccountDTO.getInterestRate(), dpsAccountDTO.getInterestRateFrequency());
        BigDecimal interestRatePerDay = BigDecimal.valueOf(annualInterestRate.doubleValue() / 100 / 365).setScale(8, RoundingMode.HALF_UP);
        log.info("interest Rate per Day : {}", interestRatePerDay);

        for (int i = 0; i < repaymentDates.size() - 1; i++) {
            long numberOfDaysInBetween = Math.abs(ChronoUnit.DAYS.between(repaymentDates.get(i), repaymentDates.get(i + 1)));
            double currentBalance = dpsAccountDTO.getSavingsAmount().doubleValue() * (i + 1);
            BigDecimal interestForThisPeriod = BigDecimal.valueOf(currentBalance * interestRatePerDay.doubleValue() * numberOfDaysInBetween).setScale(2, RoundingMode.HALF_UP);
            totalInterest = totalInterest.add(interestForThisPeriod);
               /* log.info("current repayment date : {} | next repayment date : {}", repaymentDates.get(i), repaymentDates.get(i+1));
                log.info("days in between : {}", numberOfDaysInBetween);
                log.info("interest for this period : {}", interestForThisPeriod);
                log.info("total interest amount : {}", totalInterest);
                log.info("ami ekhane asi");*/
        }

        BigDecimal totalDeposit = dpsAccountDTO.getSavingsAmount().multiply(BigDecimal.valueOf(repaymentDates.size() - 1));
        BigDecimal maturityAmount = totalDeposit.add(totalInterest).setScale(0, RoundingMode.HALF_UP);


        log.info("total deposit : {}", totalDeposit);
        log.info("total interest : {}", totalInterest);
        log.info("maturity Amount : {}", maturityAmount);
        return maturityAmount;
    }

    private Mono<List<LocalDate>> getFDRInterestPostingDates(String savingsAccountId, LocalDate firstInstallmentDate) {

        return holidayUseCase
                .getAllHolidaysOfASamityBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("Request received to fetch holidays"))
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                .flatMap(holidays -> savingsAccountUseCase
                        .getFDRAccountDetailsBySavingsAccountId(savingsAccountId)
                        .map(fdrAccountDTO -> this.calculateInterestPostingDates(fdrAccountDTO, firstInstallmentDate, holidays)));

    }


    private List<LocalDate> calculateInterestPostingDates(FDRAccountDTO fdrAccountDTO, LocalDate firstInstallmentDate, List<LocalDate> holidays) {
        LocalDate acct_end_date = null;
        Integer lengthInMonths = getFDRLengthInMonths(fdrAccountDTO);
        acct_end_date = firstInstallmentDate.plusMonths(lengthInMonths);

        while (holidays.contains(acct_end_date)) {
            assert acct_end_date != null;
            acct_end_date = acct_end_date.plusDays(1);
            log.info("Acct End Date : {} is a holiday. Hence shifting to next day.", acct_end_date);
        }

        List<LocalDate> interestPostingDates = new ArrayList<>();

        if (fdrAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("MONTHLY")) {
            interestPostingDates = IntStream.range(0, lengthInMonths)
                    .mapToObj(firstInstallmentDate::plusMonths)
                    .toList();
        } else if (fdrAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("QUARTERLY")) {
            interestPostingDates = IntStream.range(0, lengthInMonths)
                    .filter(i -> i % 3 == 0 && i != 0)
                    .mapToObj(firstInstallmentDate::plusMonths)
                    .toList();
        } else if (fdrAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("HALF_YEARLY")) {
            interestPostingDates = IntStream.range(0, lengthInMonths)
                    .filter(i -> i % 6 == 0 && i != 0)
                    .mapToObj(firstInstallmentDate::plusMonths)
                    .toList();
        } else if (fdrAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("YEARLY")) {
            interestPostingDates = IntStream.range(0, lengthInMonths)
                    .filter(i -> i % 12 == 0 && i != 0)
                    .mapToObj(firstInstallmentDate::plusMonths)
                    .toList();
        }

        List<LocalDate> updatedInterestPostingDates = new ArrayList<>(interestPostingDates);
        updatedInterestPostingDates.add(acct_end_date);

        return updatedInterestPostingDates;
    }


    private Integer getFDRLengthInMonths(FDRAccountDTO fdrAccountDTO) {
        Integer lengthInMonths = 0;
        if (fdrAccountDTO.getDepositTermPeriod().equalsIgnoreCase("MONTH")) {
            lengthInMonths = fdrAccountDTO.getDepositTerm();
        } else if (fdrAccountDTO.getDepositTermPeriod().equalsIgnoreCase("YEAR")) {
            lengthInMonths = fdrAccountDTO.getDepositTerm() * 12;
        }
        return lengthInMonths;
    }


    private Mono<Passbook> buildPassbookForSavingsDepositEntry(Passbook passbook, SavingsDepositCommand savingsDepositCommand) {
        SavingsAccountResponseDTO savingsAccountResponseDTO = savingsDepositCommand.getSavingsAccountResponseDTO();

        return savingsAccountResponseDTO.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue())
                ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Deposit is not allowed for : " + savingsAccountResponseDTO.getSavingsAccountId() + ". Already Deposited."))
                : Mono.just(Passbook
                .builder()
                .managementProcessId(savingsDepositCommand.getManagementProcessId())
                .processId(savingsDepositCommand.getProcessId())
                .savingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                .transactionDate(savingsDepositCommand.getTransactionDate())
                .transactionId(savingsDepositCommand.getTransactionId())
                .transactionCode(savingsDepositCommand.getTransactionCode())
                .mfiId(savingsDepositCommand.getMfiId())
                .officeId(savingsDepositCommand.getOfficeId())
                .memberId(savingsAccountResponseDTO.getMemberId())
                .passbookNumber(passbook.getPassbookNumber())
                .depositAmount(savingsDepositCommand.getAmount())
                .savgAcctBeginBalance(passbook.getSavgAcctEndingBalance())
                .savgAcctEndingBalance(passbook.getSavgAcctEndingBalance().add(savingsDepositCommand.getAmount()))
                .savingsAvailableBalance(
                        passbook
                                .getSavgAcctEndingBalance()
                                .add(savingsDepositCommand.getAmount())
                                .subtract(savingsAccountResponseDTO.getMinBalance())
                                .compareTo(BigDecimal.ZERO) >= 0
                                ? passbook.getSavgAcctEndingBalance().add(savingsDepositCommand.getAmount())
                                .subtract(savingsAccountResponseDTO.getMinBalance())
                                : BigDecimal.ZERO)
                .createdOn(LocalDateTime.now())
                .createdBy(savingsDepositCommand.getLoginId())
                .status(Status.STATUS_ACTIVE.getValue())
                .totalDepositAmount(passbook.getTotalDepositAmount() != null ? passbook.getTotalDepositAmount().add(savingsDepositCommand.getAmount()) : savingsDepositCommand.getAmount())
                .totalWithdrawAmount(passbook.getTotalWithdrawAmount())
                .paymentMode(savingsDepositCommand.getPaymentMode())
                .savingsAccountOid(savingsAccountResponseDTO.getOid())
                .savingsTypeId(savingsAccountResponseDTO.getSavingsTypeId())
                .source(savingsDepositCommand.getSource())
                .samityId(savingsDepositCommand.getSamityId())
                .build());
    }

    private Mono<Boolean> checkIfThisIsTheFirstPassbookEntry(SavingsAccountResponseDTO savingsAccountResponseDTO) {
        return port
                .getLastPassbookEntryBySavingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                .map(passbook -> false)
                .switchIfEmpty(Mono.just(true));

    }

    private Mono<PassbookEntity> insertRecordPassbook(Passbook passbook) {
        log.debug("Inside insertRecordPassbook.");

        return port
                .insertRecordPassbook(passbook)
                .doOnRequest(value -> log.debug("Request for Insertion"))
                .doOnSuccess(entity -> log.debug("insertion success!!!"));

    }

}
