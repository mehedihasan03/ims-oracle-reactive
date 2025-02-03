package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.LoanCalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.migration.components.member.Member;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.SettleRebateRequestDto;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto.*;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto.*;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.in.SeasonalLoanUseCase;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.domain.SeasonalLoan;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import org.apache.commons.lang3.StringUtils;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.domain.ServiceChargeChart;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.REBATE;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.ACCOUNT_TYPE_LOAN;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.PAYMENT_MODE_CASH;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Slf4j
public class SeasonalLoanService implements SeasonalLoanUseCase {
    private final CommonRepository commonRepository;
    private final LoanAccountUseCase loanAccountUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final ModelMapper modelMapper;
    private final PassbookUseCase passbookUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final IStagingDataUseCase iStagingDataUseCase;
    private final MetaPropertyUseCase metaPropertyUseCase;

    public SeasonalLoanService(CommonRepository commonRepository, LoanAccountUseCase loanAccountUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, ServiceChargeChartUseCase serviceChargeChartUseCase, CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase, PaymentCollectionUseCase paymentCollectionUseCase, ModelMapper modelMapper, PassbookUseCase passbookUseCase, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, IStagingDataUseCase iStagingDataUseCase, MetaPropertyUseCase metaPropertyUseCase) {
        this.commonRepository = commonRepository;
        this.loanAccountUseCase = loanAccountUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.serviceChargeChartUseCase = serviceChargeChartUseCase;
        this.collectionStagingDataQueryUseCase = collectionStagingDataQueryUseCase;
        this.paymentCollectionUseCase = paymentCollectionUseCase;
        this.modelMapper = modelMapper;
        this.passbookUseCase = passbookUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.iStagingDataUseCase = iStagingDataUseCase;
    }

    @Override
    public Mono<SeasonalLoanCollectionResponseDto> collectSeasonalLoan(SeasonalLoanCollectionRequestDto command) {
        return loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(command.getLoanAccountId())
                .filter(loanAccountResponseDTO -> loanAccountResponseDTO.getStatus().equalsIgnoreCase(Status.STATUS_ACTIVE.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account is not active.")))
                .flatMap(loanAccountResponseDTO -> calculateTotalPayableTillBusinessDate(loanAccountResponseDTO.getOid(), command.getOfficeId()))
                .doOnNext(seasonalLoan -> log.info("installment amount : {} | total Payable calculated : {} | paid amount : {}", seasonalLoan.getInstallmentAmount(), seasonalLoan.getTotalAmountPayable(), command.getCollectionAmount()))
                .filter(seasonalLoan -> seasonalLoan.getTotalAmountPayable().compareTo(command.getCollectionAmount()) == 0
                                        || seasonalLoan.getInstallmentAmount().compareTo(command.getCollectionAmount()) == 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection amount is not equal to total payable amount or Installment amount.")))
                .flatMap(seasonalLoan -> Mono.zip(managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId()), commonRepository.getSamityByMemberId(seasonalLoan.getMemberId()), iStagingDataUseCase.getStagingDataByMemberId(seasonalLoan.getMemberId()))
                        .map(tupleOfManagementProcessAndSamityAndStagingData -> {
                            seasonalLoan.setSamityId(tupleOfManagementProcessAndSamityAndStagingData.getT2().getSamityId());
                            seasonalLoan.setManagementProcessId(tupleOfManagementProcessAndSamityAndStagingData.getT1().getManagementProcessId());
                            seasonalLoan.setStagingDataId(tupleOfManagementProcessAndSamityAndStagingData.getT3().getStagingDataId());
                            seasonalLoan.setProcessId(UUID.randomUUID().toString());
                            return seasonalLoan;
                        })
                )
                .flatMap(seasonalLoan -> paymentCollectionUseCase.collectSeasonalLoanPaymentBySamity(buildPaymentCollectionRequestDto(command, seasonalLoan))
                        .thenReturn(SeasonalLoanCollectionResponseDto.builder()
                                .userMessage("Seasonal Loan collection saved successfully.")
                                .build())
                )
                .doOnError(throwable -> log.error("Error occurred while collecting seasonal loan. Error: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    @Override
    public Mono<SeasonalLoanGridResponseDto> getSeasonalLoanGridView(SeasonalLoanGridRequestDto command) {
        return commonRepository.getAllLoanProductByLoanTypeId(LoanTypeID.LOAN_TYPE_SS.getValue())
                .collectList()
                .flatMap(loanProducts -> commonRepository.getAllMemberEntityByOfficeId(command.getOfficeId())
                        .map(MemberEntity::getMemberId)
                        .collectList()
                        .flatMapMany(memberEntityList -> loanAccountUseCase.getAllLoanAccountsByMemberIdListAndStatus(memberEntityList, Status.STATUS_ACTIVE.getValue()))
                        .flatMap(loanAccount ->
                            iStagingDataUseCase.getStagingAccountDataByLoanAccountId(loanAccount.getLoanAccountId())
                                    .switchIfEmpty(Mono.just(StagingAccountData.builder().build()))
                                    .flatMap(stagingAccountData -> stagingAccountData.getLoanAccountId() != null
                                                ? Mono.just(loanAccount)
                                                : Mono.empty()))
                        .filter(loanAccount -> loanProducts.stream().anyMatch(loanProduct -> loanProduct.getLoanProductId().equals(loanAccount.getLoanProductId())))
                        .switchIfEmpty(Mono.empty())
                        .map(loanAccount -> modelMapper.map(loanAccount, SeasonalLoanData.class))
                        .flatMap(seasonalLoan -> commonRepository.getSamityByMemberId(seasonalLoan.getMemberId())
                                .map(samity -> {
                                    seasonalLoan.setSamityId(samity.getSamityId());
                                    seasonalLoan.setSamityDay(samity.getSamityDay());
                                    seasonalLoan.setFieldOfficerId(samity.getFieldOfficerId());
                                    return seasonalLoan;
                                })
                        )
                        .filter(seasonalLoanData -> StringUtils.isEmpty(command.getMemberId()) || seasonalLoanData.getMemberId().equals(command.getMemberId()))
                        .filter(seasonalLoanData -> (StringUtils.isEmpty(command.getLoanAccountId()) || seasonalLoanData.getLoanAccountId().equals(command.getLoanAccountId()))
                                && (StringUtils.isEmpty(command.getSamityId()) || seasonalLoanData.getSamityId().equals(command.getSamityId()))
                                && (StringUtils.isEmpty(command.getFieldOfficerId()) || seasonalLoanData.getFieldOfficerId().equals(command.getFieldOfficerId())))
                        .collectList()
                        .flatMap(seasonalLoanList ->
                            Flux.fromIterable(seasonalLoanList)
                                    .skip((long) command.getOffset() * command.getLimit())
                                    .take(command.getLimit())
                                    .flatMap(seasonalLoanData -> collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(seasonalLoanData.getLoanAccountId())
                                            .filter(collectionStagingData -> collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                                            .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()))
                                            .map(collectionStagingData -> {
                                                seasonalLoanData.setCollectionStagingDataId(StringUtils.isNotBlank(collectionStagingData.getCollectionStagingDataId())
                                                        ? collectionStagingData.getCollectionStagingDataId()
                                                        : null);
                                                seasonalLoanData.setBtnCloseEnabled(Strings.isNotNullAndNotEmpty(collectionStagingData.getCollectionStagingDataId())
                                                        ? Status.STATUS_NO.getValue()
                                                        : Status.STATUS_YES.getValue());

                                                seasonalLoanData.setBtnSubmitEnabled(Strings.isNotNullAndNotEmpty(collectionStagingData.getCollectionStagingDataId())
                                                        ? Status.STATUS_YES.getValue()
                                                        : Status.STATUS_NO.getValue());
                                                return seasonalLoanData;
                                            }))
                                    .flatMap(seasonalLoanData -> commonRepository
                                            .getMemberEntityByMemberId(seasonalLoanData.getMemberId())
                                            .doOnNext(memberEntity -> log.info("Member information fetched successfully : {}", memberEntity))
                                            .doOnError(throwable -> log.error("Error occurred while fetching member information. Error: {}", throwable.getMessage()))
                                            .map(memberEntity -> {
                                                memberEntity.setMobile(this.extractMobileNumberFromMobileDetails(memberEntity.getMobile()));
                                                seasonalLoanData.setMemberInformation(memberEntity);
                                                return seasonalLoanData;
                                            }))
                                    .collectList()
                        )
                )
                .map(seasonalLoanData -> {
                    seasonalLoanData.sort(Comparator.comparing(SeasonalLoanData::getActualDisburseDt).reversed());
                    return seasonalLoanData;
                })
                .doOnNext(seasonalLoanDatalist -> log.info("Seasonal Loan Data sorted : {}", seasonalLoanDatalist.stream().map(SeasonalLoanData::getActualDisburseDt).toList()))
                .flatMap(SeasonalLoanDatalist -> {
                    return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
                            .map(managementProcessTracker -> SeasonalLoanGridResponseDto.builder()
                                    .officeId(managementProcessTracker.getOfficeId())
                                    .officeNameEn(managementProcessTracker.getOfficeNameEn())
                                    .officeNameBn(managementProcessTracker.getOfficeNameBn())
                                    .businessDate(managementProcessTracker.getBusinessDate())
                                    .businessDay(managementProcessTracker.getBusinessDay())
                                    .data(SeasonalLoanDatalist)
                                    .totalCount(SeasonalLoanDatalist.size())
                                    .build());
                })
                .doOnError(throwable -> log.error("Error occurred while fetching seasonal loan grid. Error: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())));
    }

    private String extractMobileNumberFromMobileDetails(String phone) {
        Gson gson = new Gson();
        ArrayList mobileList = gson.fromJson(phone, ArrayList.class);
        String contactNo = "";
        if (!mobileList.isEmpty()) {
            MobileInfoDTO mobileInfoDTO;
            try {
                mobileInfoDTO = gson.fromJson(mobileList.get(0).toString(), MobileInfoDTO.class);
            } catch (Exception e) {
                log.error("Error in parsing mobile info: {}", e.getMessage());
                mobileInfoDTO = new MobileInfoDTO();
            }
           contactNo = mobileInfoDTO.getContactNo();
        }
        return contactNo;
    }


    private PaymentCollectionBySamityCommand buildPaymentCollectionRequestDto(SeasonalLoanCollectionRequestDto requestDto, SeasonalLoan seasonalLoan) {
        return PaymentCollectionBySamityCommand.builder()
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(seasonalLoan.getSamityId())
                .collectionType(CollectionType.SINGLE.getValue())
                .processId(seasonalLoan.getProcessId())
                .managementProcessId(seasonalLoan.getManagementProcessId())
                .data(Arrays.asList(CollectionData.builder()
                        .stagingDataId(seasonalLoan.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(requestDto.getLoanAccountId())
                        .amount(requestDto.getCollectionAmount())
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .currentVersion(1)
                        .build()))
                .build();
    }




    @Override
    public Mono<SeasonalLoanDetailViewDto> getSeasonalLoanDetailView(String oid, String officeId) {
        /*
        *  Get loan account by oid -> Get loan account id, get product id
        *  Get Current Business Date by officeId
        *  Get Service charge chart by product id
        *  Calculate daily interest rate by meta-property
        *  Calculate number of daysPassed between disbursement date and current business date
        *  Calculate interest amount by multiplying daily interest rate and number of daysPassed
        *  Get Repayment Schedule
        *  Build SeasonalLoanDetailViewDto
        * */

        return this.calculateTotalPayableTillBusinessDate(oid, officeId)
                .flatMap(this::buildSeasonalLoanDetailViewDto);

    }


    private BigDecimal calculateServiceChargeRatePerDay(ServiceChargeChart serviceChargeChart, LoanCalculationMetaProperty loanCalculationMetaProperty) {
        BigDecimal annualServiceChargeRate = CommonFunctions.getAnnualInterestRate(serviceChargeChart.getServiceChargeRate(), serviceChargeChart.getServiceChargeRateFreq());
        BigDecimal daysInYear= BigDecimal.valueOf(Long.parseLong(loanCalculationMetaProperty.getDaysInYear()));
        RoundingMode roundingMode = CommonFunctions.getRoundingMode(loanCalculationMetaProperty.getRoundingLogic());
        return annualServiceChargeRate.divide(daysInYear.multiply(BigDecimal.valueOf(100)), loanCalculationMetaProperty.getServiceChargeRatePrecision(), roundingMode);
    }


    private Mono<SeasonalLoan> calculateTotalPayableTillBusinessDate(String oid, String officeId) {
        return loanAccountUseCase.getLoanAccountById(oid)
                .doOnNext(loanAccountResponseDTO -> log.info("Loan Account fetched successfully."))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account not found.")))
                .flatMap(loanAccountResponseDTO -> {
                    String loanAccountId = loanAccountResponseDTO.getLoanAccountId();
                    String productId = loanAccountResponseDTO.getProductCode();
                    return Mono.zip(
                            Mono.just(loanAccountResponseDTO),
                            managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Business Date not found.")))
                                    .map(ManagementProcessTracker::getBusinessDate),
                            serviceChargeChartUseCase.getServiceChargeDetailsByLoanProductId(productId)
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Service Charge Chart not found."))),
                            passbookUseCase.getDisbursementPassbookEntryByDisbursedLoanAccountId(loanAccountId).map(PassbookResponseDTO::getTransactionDate)
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Disbursement Passbook Entry not found."))),
                            loanRepaymentScheduleUseCase.getRepaymentScheduleByLoanAccountId(loanAccountId)
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Repayment Schedule not found.")))
                    );
                })
                .flatMap(tuple5 -> metaPropertyUseCase.getLoanCalculationMetaProperty(tuple5.getT2())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Calculation Meta Property not found.")))
                        .map(loanCalculationMetaProperty -> Tuples.of(tuple5.getT1(), tuple5.getT2(), tuple5.getT3(), tuple5.getT4(), tuple5.getT5(), loanCalculationMetaProperty)))
                .map(tuple -> {
                    LoanAccountResponseDTO loanAccountResponseDTO = tuple.getT1();
                    LocalDate currentBusinessDate = tuple.getT2();
                    ServiceChargeChart serviceChargeChart = tuple.getT3();
                    LocalDate disbursementDate = tuple.getT4();
                    List<RepaymentScheduleResponseDTO> repaymentScheduleResponseDtoList = tuple.getT5();
                    LoanCalculationMetaProperty loanCalculationMetaProperty = tuple.getT6();

                    BigDecimal scheduledInstallmentAmount = repaymentScheduleResponseDtoList.stream().map(RepaymentScheduleResponseDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal serviceChargeRatePerDay = this.calculateServiceChargeRatePerDay(serviceChargeChart, loanCalculationMetaProperty);
                    int daysPassed = (int) ChronoUnit.DAYS.between(disbursementDate, currentBusinessDate);
                    BigDecimal totalServiceChargeTillBusinessDate = loanAccountResponseDTO.getLoanAmount()
                                                                        .multiply(serviceChargeRatePerDay)
                                                                        .multiply(BigDecimal.valueOf(daysPassed));
                    BigDecimal roundedTotalServiceChargeTillBusinessDate = CommonFunctions.round(totalServiceChargeTillBusinessDate, loanCalculationMetaProperty.getServiceChargeAmountPrecision(), CommonFunctions.getRoundingMode(loanCalculationMetaProperty.getRoundingLogic()));
                    BigDecimal totalPayableTillBusinessDate = loanAccountResponseDTO.getLoanAmount().add(roundedTotalServiceChargeTillBusinessDate);

                    SeasonalLoan seasonalLoan = SeasonalLoan
                            .builder()
                            .oid(loanAccountResponseDTO.getOid())
                            .loanApplicationId(loanAccountResponseDTO.getLoanApplicationId())
                            .loanAccountId(loanAccountResponseDTO.getLoanAccountId())
                            .status(loanAccountResponseDTO.getStatus())
                            .memberId(loanAccountResponseDTO.getMemberId())
                            .loanProductId(loanAccountResponseDTO.getProductCode())
                            .productName(loanAccountResponseDTO.getProductName())
                            .loanAmount(loanAccountResponseDTO.getLoanAmount())
                            .loanTerm(loanAccountResponseDTO.getLoanTerm())
                            .noInstallment(loanAccountResponseDTO.getNoInstallment())
                            .graceDays(loanAccountResponseDTO.getGraceDays())
                            .installmentAmount(scheduledInstallmentAmount)
                            .actualDisburseDt(disbursementDate)
                            .actualEndDate(currentBusinessDate)
                            .businessDate(currentBusinessDate)
                            .serviceChargeRate(serviceChargeChart.getServiceChargeRate())
                            .serviceChargeRateFreq(serviceChargeChart.getServiceChargeRateFreq())
                            .daysInYear(Integer.valueOf(loanCalculationMetaProperty.getDaysInYear()))
                            .daysPassed(daysPassed)
                            .serviceChargeRatePerDay(serviceChargeRatePerDay)
                            .totalServiceCharge(roundedTotalServiceChargeTillBusinessDate)
                            .totalAmountPayable(totalPayableTillBusinessDate)
                            .repaymentSchedule(repaymentScheduleResponseDtoList)
                            .plannedEndDate(repaymentScheduleResponseDtoList.get(repaymentScheduleResponseDtoList.size() - 1).getInstallDate())
                            .build();
                    log.info("seasonal loan calculated successfully. {}", seasonalLoan);
                    return seasonalLoan;
                });
    }

    private Mono<SeasonalLoanDetailViewDto> buildSeasonalLoanDetailViewDto(SeasonalLoan seasonalLoan) {
        return commonRepository.getMemberEntityByMemberId(seasonalLoan.getMemberId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Member Information not found for member id: " + seasonalLoan.getMemberId())))
                .map(memberEntity -> {
                    memberEntity.setMobile(this.extractMobileNumberFromMobileDetails(memberEntity.getMobile()));
                    return memberEntity;
                })
                .doOnError(throwable -> log.error("Error occurred while fetching member information. Error: {}", throwable.getMessage()))
                    .map(memberEntity ->
                            SeasonalLoanDetailViewDto
                            .builder()
                            .userMessage("Seasonal Loan Data Fetched Successfully.")
                            .data(seasonalLoan)
                            .memberInformation(memberEntity)
                            .build());

    }




}
