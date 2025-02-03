package net.celloscope.mraims.loanportfolio.features.disbursement.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.DisbursementUseCase;
import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.helpers.dto.DisbursementResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.out.ILoanAccountPersistencePort;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.MigrationRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.MigrationRepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.ServiceChargeChartResponseDTO;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_FLAT;

@Service
@Slf4j
public class DisbursementService implements DisbursementUseCase {

    private final LoanAccountUseCase loanAccountUseCase;
    private final ILoanAccountPersistencePort loanAccountPersistencePort;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final CommonRepository commonRepository;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final TransactionalOperator rxtx;
    private final Gson gson;

    private final ISmsNotificationUseCase smsNotificationUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final MigrationRepaymentScheduleUseCase migrationRepaymentScheduleUseCase;

    public DisbursementService(
            TransactionalOperator rxtx, LoanAccountUseCase loanAccountUseCase,
            ILoanAccountPersistencePort loanAccountPersistencePort,
            LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase,
            ServiceChargeChartUseCase serviceChargeChartUseCase, TransactionUseCase transactionUseCase,
            PassbookUseCase passbookUseCase, CommonRepository commonRepository, MetaPropertyUseCase metaPropertyUseCase,
            Gson gson, ISmsNotificationUseCase smsNotificationUseCase, ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
            MigrationRepaymentScheduleUseCase migrationRepaymentScheduleUseCase) {
        this.loanAccountUseCase = loanAccountUseCase;
        this.loanAccountPersistencePort = loanAccountPersistencePort;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.serviceChargeChartUseCase = serviceChargeChartUseCase;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.commonRepository = commonRepository;
        this.rxtx = rxtx;
        this.gson = gson;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.smsNotificationUseCase = smsNotificationUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.migrationRepaymentScheduleUseCase = migrationRepaymentScheduleUseCase;
    }

    @Override
    public Mono<DisbursementResponseDTO> disburseLoan(String loanAccountId, LocalDate disbursementDate,
                                                      String loginId, String officeId, String serviceChargeCalculationMethod) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                        .map(managementProcessTracker -> {
                            managementProcessTrackerAtomicReference.set(managementProcessTracker);
                            return managementProcessTracker;
                        })
                .flatMap(managementProcessTracker -> loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanAccountId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Not Found")))
                .flatMap(loanAccountResponseDTO -> validate(loanAccountResponseDTO, loanAccountId))
                .flatMap(tuples -> !tuples.getT1()
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.LOAN_REPAYMENT_SCHEDULE_ALREADY_GENERATED.getValue()))
                        : serviceChargeChartUseCase
                            .getServiceChargeDetailsByLoanAccountId(loanAccountId)
                            .doOnNext(serviceChargeChartResponseDTO -> log.info("service charge chart : {}", serviceChargeChartResponseDTO))
                            .flatMap(serviceChargeChartResponseDTO ->
                                (serviceChargeCalculationMethod != null && serviceChargeCalculationMethod.equalsIgnoreCase(SERVICE_CHARGE_CALCULATION_METHOD_FLAT.getValue()))
                                        || (serviceChargeChartResponseDTO.getInterestCalcMethod() != null && serviceChargeChartResponseDTO.getInterestCalcMethod().equalsIgnoreCase(SERVICE_CHARGE_CALCULATION_METHOD_FLAT.getValue()))
                                            ? this.buildLoanRepayScheduleRequestDTOFlat(serviceChargeChartResponseDTO, tuples.getT2(), managementProcessTrackerAtomicReference.get().getBusinessDate(), loginId)
                                                .flatMap(loanRepaymentScheduleUseCase::getRepaymentScheduleForLoanFlat)
                                            : this.buildLoanRepayScheduleRequestDTO(serviceChargeChartResponseDTO, tuples.getT2(), managementProcessTrackerAtomicReference.get().getBusinessDate(), loginId)
                                                .flatMap(loanRepaymentScheduleUseCase::getRepaymentScheduleForLoanDecliningBalance)
                                                .doOnRequest(l -> log.info("Requesting to get repayment schedule : {}", loanAccountId)))
                        .flatMap(tuple2 -> {
                            List<RepaymentScheduleResponseDTO> repaymentScheduleList = tuple2.getT1();
                            BigDecimal annualServiceChargeRate = tuple2.getT2();
                            RepaymentScheduleResponseDTO lastInstallment = repaymentScheduleList.get(repaymentScheduleList.size() - 1);
                            return loanAccountPersistencePort
                                    .updateLoanAccount(loanAccountId, managementProcessTrackerAtomicReference.get().getBusinessDate(), lastInstallment.getServiceChargeRatePerPeriod(), annualServiceChargeRate, lastInstallment.getInstallDate())
                                    .thenReturn(Tuples.of(repaymentScheduleList, tuples.getT2()));
                        }))
                .flatMap(tuple2 -> createTransactionPassbook(tuple2.getT2(), managementProcessTrackerAtomicReference.get().getBusinessDate(), loginId, officeId, Constants.SOURCE_APPLICATION.getValue())
                        .thenReturn(DisbursementResponseDTO
                                .builder()
                                .userMessage("Loan Disbursement Successful.")
                                .repaymentScheduleResponseDTOList(tuple2.getT1())
                                .build()))
                .as(rxtx::transactional);
        }

    @Override
    public Mono<DisbursementResponseDTO> disburseLoanMigration(String loanAccountId, LocalDate disbursementDate, String loginId, String officeId, String serviceChargeCalculationMethod, LocalDate cutOffDate, Integer noOfPastInstallments, BigDecimal installmentAmount, BigDecimal disbursedLoanAmount, Boolean isMonthly, Integer loanTermInMonths) {
        log.info("isMonthly disburseLoanMigration : {}", isMonthly);
        return loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Not Found")))
                .flatMap(loanAccountResponseDTO -> validate(loanAccountResponseDTO, loanAccountId))
                .flatMap(tuples -> !tuples.getT1()
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        ExceptionMessages.LOAN_REPAYMENT_SCHEDULE_ALREADY_GENERATED
                                .getValue()))
                        : serviceChargeChartUseCase
                        .getServiceChargeDetailsByLoanAccountId(loanAccountId)
                        .doOnNext(serviceChargeChartResponseDTO -> log.info(
                                "service charge chart : {}",
                                serviceChargeChartResponseDTO))
                        .flatMap(serviceChargeChartResponseDTO -> {
                            MigrationRepaymentScheduleCommand migrationRepaymentScheduleCommand = buildMigrationRepaymentScheduleCommandFlat(serviceChargeChartResponseDTO,
                                    tuples.getT2(),
                                    disbursementDate,
                                    loginId, cutOffDate, noOfPastInstallments, installmentAmount, disbursedLoanAmount, isMonthly, loanTermInMonths);
                            return installmentAmount != null && installmentAmount.compareTo(BigDecimal.ZERO) > 0
                                    ? migrationRepaymentScheduleUseCase.getRepaymentScheduleForLoanFlatV2(migrationRepaymentScheduleCommand)
                                    : migrationRepaymentScheduleUseCase.getRepaymentScheduleForLoanFlat(migrationRepaymentScheduleCommand);
                        })
                        .doOnRequest(l -> log.info(
                                "Requesting to get repayment schedule : {}",
                                loanAccountId))
                        .flatMap(tuple2 -> {
                            List<RepaymentScheduleResponseDTO> repaymentSchedule = tuple2.getT1();
                            RepaymentScheduleResponseDTO lastInstallment = repaymentSchedule.get(repaymentSchedule.size() - 1);
                            return loanAccountPersistencePort
                                    .updateLoanAccount(loanAccountId,
                                            disbursementDate, lastInstallment.getServiceChargeRatePerPeriod(), tuple2.getT2(), lastInstallment.getInstallDate())
                                    .thenReturn(Tuples.of(
                                            repaymentSchedule,
                                            tuples.getT2()));
                        }))
                .flatMap(tuple2 -> createTransactionPassbook(tuple2.getT2(), disbursementDate,
                        loginId, officeId, Constants.SOURCE_MIGRATION.getValue())
                        .thenReturn(DisbursementResponseDTO
                                .builder()
                                .userMessage("Loan Disbursement Successful.")
                                .repaymentScheduleResponseDTOList(tuple2.getT1())
                                .build()))
                .as(rxtx::transactional);
    }

    @Override
    public Mono<DisbursementResponseDTO> disburseLoanMigrationV3(MigrationMemberRequestDto requestDto, String loanAccountId, LocalDate disbursementDate, String loginId, String officeId, String serviceChargeCalculationMethod, LocalDate cutOffDate, Integer noOfPastInstallments, BigDecimal installmentAmount, BigDecimal disbursedLoanAmount, Boolean isMonthly, Integer loanTermInMonths) {
        log.info("isMonthly disburseLoanMigration : {}", isMonthly);
        return loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Not Found")))
                .flatMap(loanAccountResponseDTO -> validate(loanAccountResponseDTO, loanAccountId))
                .flatMap(tuples -> !tuples.getT1()
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        ExceptionMessages.LOAN_REPAYMENT_SCHEDULE_ALREADY_GENERATED
                                .getValue()))
                        : serviceChargeChartUseCase
                        .getServiceChargeDetailsByLoanAccountId(loanAccountId)
                        .doOnNext(serviceChargeChartResponseDTO -> log.info(
                                "service charge chart : {}",
                                serviceChargeChartResponseDTO))
                        .flatMap(serviceChargeChartResponseDTO -> {
                            RepaymentScheduleCommand migrationRepaymentScheduleCommand = buildRepaymentScheduleCommandMigration(requestDto,
                                    serviceChargeChartResponseDTO,
                                    tuples.getT2(),
                                    loginId, cutOffDate, installmentAmount, officeId);
                            return  serviceChargeCalculationMethod.equalsIgnoreCase(SERVICE_CHARGE_CALCULATION_METHOD_FLAT.getValue())
                                    && (installmentAmount != null && installmentAmount.compareTo(BigDecimal.ZERO) > 0)
                                        ? migrationRepaymentScheduleUseCase
                                            .generateRepaymentScheduleFlatInstallmentAmountProvidedForMigration(migrationRepaymentScheduleCommand)
                                            .map(list -> Tuples.of(list, migrationRepaymentScheduleCommand.getAnnualServiceChargeRate()))
                                        : migrationRepaymentScheduleUseCase
                                            .generateRepaymentScheduleDecliningInstallmentAmountProvidedForMigration(migrationRepaymentScheduleCommand)
                                            .map(list -> Tuples.of(list, migrationRepaymentScheduleCommand.getAnnualServiceChargeRate()));
                        })
                        .doOnRequest(l -> log.info(
                                "Requesting to get repayment schedule : {}",
                                loanAccountId))
                        .flatMap(tuple2 -> {
                            List<RepaymentScheduleResponseDTO> repaymentSchedule = tuple2.getT1();
                            RepaymentScheduleResponseDTO lastInstallment = repaymentSchedule.get(repaymentSchedule.size() - 1);
                            return loanAccountPersistencePort
                                    .updateLoanAccount(loanAccountId,
                                            disbursementDate, lastInstallment.getServiceChargeRatePerPeriod(), tuple2.getT2(), lastInstallment.getInstallDate())
                                    .thenReturn(Tuples.of(
                                            repaymentSchedule,
                                            tuples.getT2()));
                        }))
                .flatMap(tuple2 -> createTransactionPassbook(tuple2.getT2(), disbursementDate,
                        loginId, officeId, Constants.SOURCE_MIGRATION.getValue())
                        .thenReturn(DisbursementResponseDTO
                                .builder()
                                .userMessage("Loan Disbursement Successful.")
                                .repaymentScheduleResponseDTOList(tuple2.getT1())
                                .build()))
                .as(rxtx::transactional);
    }

    private Mono<LoanRepaymentScheduleRequestDTO> buildLoanRepayScheduleRequestDTO(
            ServiceChargeChartResponseDTO serviceChargeChartResponseDTO,
            LoanAccountResponseDTO loanAccountResponseDTO, LocalDate disbursementDate, String loginId) {

        return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                .map(metaProperty -> LoanRepaymentScheduleRequestDTO
                    .builder()
                    .loanAmount(loanAccountResponseDTO.getLoanAmount())
                    .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate()
                            .divide(BigDecimal.valueOf(100), metaProperty.getServiceChargeRatePrecision(), CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic())))
                    .serviceChargeRateFrequency(serviceChargeChartResponseDTO.getServiceChargeRateFreq())
                    .noOfInstallments(loanAccountResponseDTO.getNoInstallment())
                    .installmentAmount(loanAccountResponseDTO.getInstallmentAmount())
                    .graceDays(loanAccountResponseDTO.getGraceDays())
                    .disburseDate(disbursementDate)
                    .samityDay(serviceChargeChartResponseDTO.getSamityDay().toUpperCase())
                    .loanTerm(loanAccountResponseDTO.getLoanTerm())
                    .repaymentFrequency(serviceChargeChartResponseDTO.getRepaymentFrequency())
                    .roundingLogic(metaProperty.getRoundingLogic())
                    .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                    .memberId(loanAccountResponseDTO.getMemberId())
                    .mfiId(loanAccountResponseDTO.getMfiId())
                    .status(Status.STATUS_PENDING.getValue())
                    .loginId(loginId)
                    .monthlyRepaymentFrequencyDay(loanAccountResponseDTO.getMonthlyRepayDay())
                    .roundingToNearest(metaProperty.getRoundingToNearestInteger())
                    .serviceChargeRatePrecision(metaProperty.getServiceChargeRatePrecision())
                    .serviceChargeAmountPrecision(metaProperty.getServiceChargePrecision())
                    .installmentPrecision(metaProperty.getInstallmentPrecision())
                    .build());
    }


    private Mono<LoanRepaymentScheduleRequestDTO> buildLoanRepayScheduleRequestDTOFlat(
            ServiceChargeChartResponseDTO serviceChargeChartResponseDTO,
            LoanAccountResponseDTO loanAccountResponseDTO, LocalDate disbursementDate, String loginId) {

        return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                .map(metaProperty ->
                        LoanRepaymentScheduleRequestDTO
                            .builder()
                                .loanAmount(loanAccountResponseDTO.getLoanAmount())
                                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate()
                                        .divide(BigDecimal.valueOf(100), metaProperty.getServiceChargeRatePrecision(), CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic())))
                                .serviceChargeRateFrequency(serviceChargeChartResponseDTO.getServiceChargeRateFreq())
                                .noOfInstallments(loanAccountResponseDTO.getNoInstallment())
                                .installmentAmount(loanAccountResponseDTO.getInstallmentAmount())
                                .graceDays(loanAccountResponseDTO.getGraceDays())
                                .disburseDate(disbursementDate)
                                .samityDay(serviceChargeChartResponseDTO.getSamityDay().toUpperCase())
                                .loanTerm(loanAccountResponseDTO.getLoanTerm())
                                .repaymentFrequency(serviceChargeChartResponseDTO.getRepaymentFrequency())
                                .roundingLogic(metaProperty.getRoundingLogic())
                                .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                                .memberId(loanAccountResponseDTO.getMemberId())
                                .mfiId(loanAccountResponseDTO.getMfiId())
                                .status(Status.STATUS_PENDING.getValue())
                                .loginId(loginId)
                                .monthlyRepaymentFrequencyDay(loanAccountResponseDTO.getMonthlyRepayDay())
                                .roundingToNearest(metaProperty.getRoundingToNearestInteger())
                                .serviceChargeRatePrecision(metaProperty.getServiceChargeRatePrecision())
                                .serviceChargeAmountPrecision(metaProperty.getServiceChargePrecision())
                                .installmentPrecision(metaProperty.getInstallmentPrecision())
                            .build());
    }

    private MigrationRepaymentScheduleCommand buildMigrationRepaymentScheduleCommandFlat(
            ServiceChargeChartResponseDTO serviceChargeChartResponseDTO,
            LoanAccountResponseDTO loanAccountResponseDTO, LocalDate disbursementDate, String loginId, LocalDate cutOffDate, Integer noOfPastInstallments, BigDecimal installmentAmount, BigDecimal disbursedLoanAmount, Boolean isMonthly, Integer loanTermInMonths) {

        return MigrationRepaymentScheduleCommand
                .builder()
                .loanAmount(loanAccountResponseDTO.getLoanAmount())
                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate())
                .serviceChargeRateFrequency(serviceChargeChartResponseDTO.getServiceChargeRateFreq())
                .noOfInstallments(loanAccountResponseDTO.getNoInstallment())
                .graceDays(loanAccountResponseDTO.getGraceDays())
                .disburseDate(disbursementDate)
                .samityDay(serviceChargeChartResponseDTO.getSamityDay().toUpperCase())
                .repaymentFrequency(serviceChargeChartResponseDTO.getRepaymentFrequency())
                .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                .memberId(loanAccountResponseDTO.getMemberId())
                .mfiId(loanAccountResponseDTO.getMfiId())
                .status(Status.STATUS_PENDING.getValue())
                .loginId(loginId)
                .monthlyRepaymentFrequencyDay(loanAccountResponseDTO.getMonthlyRepayDay())
                .isMonthly(isMonthly)
                .roundingMode(RepaymentScheduleEnum.NO_ROUNDING.toString())
                .roundingInstallmentToNearestIntegerLogic(RepaymentScheduleEnum.NO_ROUNDING_TO_INTEGER.toString())
                .roundingInstallmentToNearestInteger(0)
                .serviceChargeRatePrecision(8)
                .principalAmountPrecision(2)
                .installmentAmountPrecision(0)
                .cutOffDate(cutOffDate)
                .noOfPastInstallments(noOfPastInstallments)
                .installmentAmount(installmentAmount)
                .disbursedLoanAmount(disbursedLoanAmount)
                .loanTermInMonths(loanTermInMonths)
                .build();
    }


    private RepaymentScheduleCommand buildRepaymentScheduleCommandMigration(
            MigrationMemberRequestDto requestDto,
            ServiceChargeChartResponseDTO serviceChargeChartResponseDTO,
            LoanAccountResponseDTO loanAccountResponseDTO, String loginId, LocalDate cutOffDate,
            BigDecimal installmentAmount, String officeId) {

        return RepaymentScheduleCommand
                .builder()
                .loanAmount(requestDto.getLoanInformation().getDisbursedLoanAmount())
                .noOfInstallments(loanAccountResponseDTO.getNoInstallment())
                .graceDays(loanAccountResponseDTO.getGraceDays())
                .repaymentFrequency(serviceChargeChartResponseDTO.getRepaymentFrequency())
                .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                .memberId(loanAccountResponseDTO.getMemberId())
                .mfiId(loanAccountResponseDTO.getMfiId())
                .loginId(loginId)
                .cutOffDate(cutOffDate)
                .disbursementDate(requestDto.getLoanInformation().getLoanDisbursementDate())
                .installmentAmount(installmentAmount)
                .loanTermInMonths(loanAccountResponseDTO.getLoanTerm())
                .totalOutstandingAmount(requestDto.getLoanInformation().getLoanOutstanding())
                .accumulatedLoanAmount(requestDto.getLoanInformation().getDisbursedLoanAmount().add(requestDto.getLoanInformation().getTotalServiceCharge()))
                .installmentAmount(loanAccountResponseDTO.getInstallmentAmount())
                .installmentAmount(requestDto.getLoanInformation().getInstallmentAmount())
                .installmentServiceCharge(requestDto.getLoanInformation().getInstallmentServiceCharge())
                .installmentPrincipal(requestDto.getLoanInformation().getInstallmentPrincipleAmount())
                .totalServiceCharge(requestDto.getLoanInformation().getTotalServiceCharge())
                .outstandingPrincipal(requestDto.getLoanInformation().getPrincipleOutstanding())
                .outstandingServiceCharge(requestDto.getLoanInformation().getServiceChargeOutstanding())
                .overdueAmount(requestDto.getLoanInformation().getOverDueAmount())
                .monthlyRepaymentFrequencyDay(requestDto.getLoanInformation().getMonthlyRepayDay().intValue())
                .officeId(officeId)
                .annualServiceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRateFreq().equalsIgnoreCase(RepaymentScheduleEnum.FREQUENCY_MONTHLY.getValue())
                        ? (serviceChargeChartResponseDTO.getServiceChargeRate().multiply(BigDecimal.valueOf(loanAccountResponseDTO.getLoanTerm()))).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                        : (serviceChargeChartResponseDTO.getServiceChargeRate().multiply(BigDecimal.valueOf(loanAccountResponseDTO.getLoanTerm()))).divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP))
                .build();
    }

    private Mono<AisResponse> createTransactionPassbook(LoanAccountResponseDTO loanAccountResponseDTO,
                                                        LocalDate disbursementDate, String loginId, String officeId, String source) {
        AtomicReference<List<SMSNotificationMetaProperty>> smsNotificationMetaPropertyList = new AtomicReference<>();
        return this.getSMSNotificationMetaProperty()
                .doOnNext(smsNotificationMetaPropertyList::set)
                .doOnNext(metaPropertyList -> log.info("SMS Notification Meta Property List: {}", metaPropertyList))
                .flatMap(list -> managementProcessTrackerUseCase
                        .getLastManagementProcessIdForOffice(officeId)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Process Tracker found for office : " + officeId)))
                        .flatMap(managementProcessId -> transactionUseCase
                                .createTransactionForLoanDisbursement(loanAccountResponseDTO.getLoanAccountId(), loanAccountResponseDTO.getLoanAmount(), disbursementDate, loanAccountResponseDTO.getMemberId(), loanAccountResponseDTO.getMfiId(), loginId, managementProcessId, officeId, source)
                                .doOnRequest(l -> log.info("requesting to create transaction for disbursement with loanAccountId : {}, disbursementDate : {}, loginId : {}", loanAccountResponseDTO.getLoanAccountId(), disbursementDate, loginId))
                                .doOnSuccess(savedTransactionResponseDTO -> log.info("disbursement transaction saved successfully. savedTransaction : {}", savedTransactionResponseDTO)))
                        .doOnSuccess(transactionResponseDTO -> this.createSMSNotificationRequestForTransaction(transactionResponseDTO, smsNotificationMetaPropertyList.get(), loginId).subscribeOn(Schedulers.immediate()).subscribe())
                        .flatMap(transactionResponseDTO -> loanRepaymentScheduleUseCase
                                .getTotalLoanPay(loanAccountResponseDTO.getLoanAccountId())
                                .doOnNext(totalPayment -> log.info("total loan pay amount of loan : {}", totalPayment))
                                .map(totalPayment -> totalPayment.subtract(loanAccountResponseDTO.getLoanAmount()))
                                .doOnNext(totalServiceCharge -> log.info("total service charge after subtracting loan amount from totalLoanPay amount : {}", totalServiceCharge))
                                .flatMap(totalServiceCharge -> passbookUseCase
                                        .createPassbookEntryForDisbursement(
                                                buildPassbookRequestDto(
                                                        loanAccountResponseDTO,
                                                        transactionResponseDTO,
                                                        totalServiceCharge,
                                                        loginId, source))
                                        .doOnRequest(l -> log.info("requesting to create disbursement passbook entry"))
                                        .doOnSuccess(passbookResponseDTO -> log.info("disbursement passbook entry saved successfully. saved Passbook Response Dto : {}", passbookResponseDTO))))
                        .map(passbookResponseDTO -> AisResponse.builder().build()));
    }


    private PassbookRequestDTO buildPassbookRequestDto(LoanAccountResponseDTO loanAccountResponseDTO,
                                                       SingleTransactionResponseDTO transactionResponseDTO, BigDecimal calculatedServiceCharge,
                                                       String loginId, String source) {
        return PassbookRequestDTO
                .builder()
                .amount(loanAccountResponseDTO.getLoanAmount())
                .loanAccountOid(loanAccountResponseDTO.getOid())
                .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                .transactionId(transactionResponseDTO.getTransactionId())
                .transactionCode(transactionResponseDTO.getTransactionCode())
                .mfiId(loanAccountResponseDTO.getMfiId())
                .loginId(loginId)
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .paymentMode(transactionResponseDTO.getPaymentMode())
                .calculatedServiceCharge(calculatedServiceCharge)
                .memberId(loanAccountResponseDTO.getMemberId())
                .managementProcessId(transactionResponseDTO.getManagementProcessId())
                .processId(transactionResponseDTO.getProcessId())
                .officeId(transactionResponseDTO.getOfficeId())
                .source(source)
                .samityId(transactionResponseDTO.getSamityId())
                .build();
    }

    private Mono<Tuple2<Boolean, LoanAccountResponseDTO>> validate(LoanAccountResponseDTO loanAccountResponseDTO,
                                                                   String loanAccountId) {
        if (loanAccountResponseDTO.getStatus().equals(Status.STATUS_APPROVED.getValue())) {
            return Mono.zip(loanRepaymentScheduleUseCase
                            .getRepaymentScheduleByLoanAccountId(loanAccountId)
                            .doOnNext(repaymentScheduleResponseDTOS -> log.info(
                                    "Repayment schedule received : {}",
                                    repaymentScheduleResponseDTOS))
                            .map(repaymentScheduleResponseDTOS -> repaymentScheduleResponseDTOS.size() <= 1)
                            .switchIfEmpty(Mono.just(true)), Mono.just(loanAccountResponseDTO))
                    .doOnNext(objects -> log.info("Repayment schedule not found : {}",
                            objects.getT1()));
        } else if (loanAccountResponseDTO.getStatus().equals(Status.STATUS_ACTIVE.getValue())) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                    ExceptionMessages.LOAN_REPAYMENT_SCHEDULE_ALREADY_GENERATED.getValue()));
        } else {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                    ExceptionMessages.LOAN_ACCOUNT_STATUS_NOT_APPROVED.getValue()));
        }
    }

    private Mono<SingleTransactionResponseDTO> createSMSNotificationRequestForTransaction(SingleTransactionResponseDTO transactionResponseDTO, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList, String loginId) {
        return Mono.fromSupplier(() -> smsNotificationMetaPropertyList.stream()
                        .filter(metaProperty -> metaProperty.getType().equalsIgnoreCase(transactionResponseDTO.getTransactionCode()))
                        .findFirst()
                        .get())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SMS Notification Meta Property Not Found for Transaction Code " + transactionResponseDTO.getTransactionCode())))
                .flatMap(smsNotificationMetaProperty -> {
                    if (smsNotificationMetaProperty.getIsSMSNotificationEnabled().equals("Yes")) {
                        return this.createAndSaveSMSNotificationRequest(transactionResponseDTO, smsNotificationMetaProperty, loginId);
                    }
                    return Mono.just(transactionResponseDTO);
                }).onErrorResume(e -> Mono.just(transactionResponseDTO));
    }

    public Mono<SingleTransactionResponseDTO> createAndSaveSMSNotificationRequest(SingleTransactionResponseDTO transactionResponseDTO, SMSNotificationMetaProperty smsNotificationMetaProperty, String loginId) {
        AtomicReference<String> instituteOid = new AtomicReference<>();
        return commonRepository.getInstituteOidByMFIId(transactionResponseDTO.getMfiId())
                .doOnNext(instituteOid::set)
                .flatMap(institute ->
                        commonRepository.getMemberEntityByMemberIdList(List.of(transactionResponseDTO.getMemberId()))
                                .last()
                                .map(memberEntity -> {
                                    MobileInfoDTO mobileInfoDTO = gson.fromJson(gson.fromJson(memberEntity.getMobile(), ArrayList.class)
                                            .get(0)
                                            .toString(), MobileInfoDTO.class);
                                    return SmsNotificationRequestDTO.builder()
                                            .type(transactionResponseDTO.getTransactionCode())
                                            .id(transactionResponseDTO.getTransactionId())
                                            .amount(String.valueOf(transactionResponseDTO.getAmount()))
                                            .datetime(String.valueOf(transactionResponseDTO.getTransactionDate()))
                                            .accountId(!HelperUtil.checkIfNullOrEmpty(transactionResponseDTO.getLoanAccountId()) ? transactionResponseDTO.getLoanAccountId() : transactionResponseDTO.getSavingsAccountId())
                                            .memberId(transactionResponseDTO.getMemberId())
                                            .mobileNumber(mobileInfoDTO.getContactNo())
                                            .template(smsNotificationMetaProperty.getTemplate())
                                            .mfiId(transactionResponseDTO.getMfiId())
                                            .instituteOid(instituteOid.get())
                                            .loginId(loginId)
                                            .build();
                                }))
                .doOnNext(smsNotificationRequestDTO -> {
                    log.info("SMS Notification Request DTO: {}", smsNotificationRequestDTO);
                    smsNotificationUseCase.publishSmsRequest(smsNotificationRequestDTO)
                            .subscribeOn(Schedulers.immediate())
                            .subscribe();
                })
                .map(smsLog -> transactionResponseDTO)
            .onErrorResume(e -> Mono.just(transactionResponseDTO));
    }

    private Mono<List<SMSNotificationMetaProperty>> getSMSNotificationMetaProperty() {
        return metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SMS_NOTIFICATION_META_PROPERTY_ID.getValue())
                .filter(metaPropertyResponseDTO -> !HelperUtil.checkIfNullOrEmpty(metaPropertyResponseDTO.getPropertyId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Meta Property Found For SMS Notification Service")))
                .handle((metaPropertyResponseDTO, sink) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList;
                    try {
                        smsNotificationMetaPropertyList = new ArrayList<>(objectMapper.readValue(
                                metaPropertyResponseDTO.getParameters(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, SMSNotificationMetaProperty.class)
                        ));
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                        return;
                    }
                    sink.next(smsNotificationMetaPropertyList);
                });
    }
}
