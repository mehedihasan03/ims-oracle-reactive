package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.LoanTypeID;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberSamityOfficeEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.migration.RoundingLogic;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.MigrationRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.MigrationRepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.commands.IRepaymentDatesCommands;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Service
@Slf4j
public class MigrationRepaymentScheduleService implements MigrationRepaymentScheduleUseCase {

    private final HolidayUseCase holidayUseCase;
    private final ModelMapper modelMapper;
    private final RepaymentSchedulePersistencePort port;
    private final CommonRepository commonRepository;
    private final IRepaymentDatesCommands repaymentDatesCommands;
    private final MetaPropertyUseCase metaPropertyUseCase;

    public MigrationRepaymentScheduleService(HolidayUseCase holidayUseCase, ModelMapper modelMapper, RepaymentSchedulePersistencePort port, CommonRepository commonRepository, IRepaymentDatesCommands repaymentDatesCommands, MetaPropertyUseCase metaPropertyUseCase) {
        this.holidayUseCase = holidayUseCase;
        this.modelMapper = modelMapper;
        this.port = port;
        this.commonRepository = commonRepository;
        this.repaymentDatesCommands = repaymentDatesCommands;
        this.metaPropertyUseCase = metaPropertyUseCase;
    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleViewDTO>, BigDecimal>> viewRepaymentScheduleFlat(MigrationRepaymentScheduleCommand command) {
        RoundingMode selectedRoundingMode = getRoundingMode(command.getRoundingMode());

        BigDecimal annualServiceChargeRate = CommonFunctions.getAnnualInterestRate(command.getServiceChargeRate(), command.getServiceChargeRateFrequency());
        annualServiceChargeRate = annualServiceChargeRate.divide(BigDecimal.valueOf(100), command.getServiceChargeRatePrecision(), selectedRoundingMode);

        BigDecimal principalPerInstallment = command.getLoanAmount().divide(BigDecimal.valueOf(command.getNoOfInstallments()), command.getPrincipalAmountPrecision(), selectedRoundingMode);
        BigDecimal totalServiceCharge = command.getLoanAmount().multiply(annualServiceChargeRate);
        BigDecimal totalRepaymentAmount = command.getLoanAmount().add(totalServiceCharge);

        BigDecimal calculatedInstallmentAmount = totalRepaymentAmount.divide(BigDecimal.valueOf(command.getNoOfInstallments()), command.getInstallmentAmountPrecision(), selectedRoundingMode);
        calculatedInstallmentAmount = roundInstallmentAmountIfNecessary(calculatedInstallmentAmount, command.getRoundingInstallmentToNearestInteger(), command.getRoundingInstallmentToNearestIntegerLogic());

        BigDecimal serviceChargePerInstallment = calculatedInstallmentAmount.subtract(principalPerInstallment);
        BigDecimal adjustedAmount = calculatedInstallmentAmount.subtract(totalRepaymentAmount.divide(BigDecimal.valueOf(command.getNoOfInstallments()), 2, selectedRoundingMode));
        BigDecimal totalExtraPayment = BigDecimal.valueOf(Math.abs(adjustedAmount.doubleValue() * (command.getNoOfInstallments()))).setScale(0, selectedRoundingMode);

        log.info("serviceChargeRatePerAnnum : {}", annualServiceChargeRate);
        log.info("principal per installment : {}", principalPerInstallment);
        log.info("totalRepaymentAmount : {}", totalRepaymentAmount);
        log.info("calculatedInstallmentAmount : {}", calculatedInstallmentAmount);
        log.info("adjustedAmount : {}", adjustedAmount);
        log.info("totalExtraPayment : {}", totalExtraPayment);

        BigDecimal finalCalculatedInstallmentAmount = calculatedInstallmentAmount;
        BigDecimal finalCalculatedInstallmentAmount1 = calculatedInstallmentAmount;
        BigDecimal finalAnnualServiceChargeRate = annualServiceChargeRate;
        return /*holidayUseCase
//                .getAllHolidaysOfASamityByOfficeId(command.getOfficeId())
                .getAllHolidaysOfASamityByLoanAccountId(command.getLoanAccountId())
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .map(holidays -> */
                Mono.just(getRepaymentDates(List.of(LocalDate.now()), command.getCutOffDate(), DayOfWeek.valueOf(command.getSamityDay()), command.getNoOfInstallments(), command.getNoOfPastInstallments(), command.getIsMonthly()))
                .map(getRepaymentDates -> this.getRepaymentScheduleForFlatInstallment(command.getLoanAmount(), command.getNoOfInstallments(), finalCalculatedInstallmentAmount, finalCalculatedInstallmentAmount1, getRepaymentDates, principalPerInstallment, serviceChargePerInstallment, adjustedAmount, totalExtraPayment)
                                .stream()
                                .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                                  .toList())
                        .map(list -> Tuples.of(list, finalAnnualServiceChargeRate));

    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlat(MigrationRepaymentScheduleCommand command) {

        log.info("command : {}", command);
        AtomicReference<BigDecimal> annualServiceChargeRate = new AtomicReference<>();
        return this.viewRepaymentScheduleFlat(command)
                .map(tuple2 -> {
                    annualServiceChargeRate.set(tuple2.getT2());
                    return tuple2.getT1()
                            .stream()
                            .map(repaymentScheduleViewDTO -> modelMapper.map(repaymentScheduleViewDTO, RepaymentSchedule.class))
                            .toList();
                })
                .map(repaymentSchedule -> this.createRepaymentScheduleToSaveToDBFlat(repaymentSchedule, command.getLoanAccountId(), command.getMemberId(), command.getMfiId(), command.getStatus(), command.getLoginId()))
                .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                .flatMap(port::saveRepaymentSchedule)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class))
                .collectList()
                .map(list -> Tuples.of(list, annualServiceChargeRate.get()));

    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleViewDTO>, BigDecimal>> viewRepaymentScheduleFlatV2(MigrationRepaymentScheduleCommand command) {
        RoundingMode selectedRoundingMode = getRoundingMode(command.getRoundingMode());

        BigDecimal annualServiceChargeRate = CommonFunctions.getAnnualInterestRate(command.getServiceChargeRate(), command.getServiceChargeRateFrequency());
        annualServiceChargeRate = annualServiceChargeRate.divide(BigDecimal.valueOf(100), command.getServiceChargeRatePrecision(), selectedRoundingMode);

//        BigDecimal principalPerInstallment = command.getLoanAmount().divide(BigDecimal.valueOf(command.getNoOfInstallments()), command.getPrincipalAmountPrecision(), selectedRoundingMode);
        BigDecimal principalPerInstallment = command.getInstallmentAmount().divide(
            (command.getServiceChargeRate().multiply(getLoanTermInYears(command.getLoanTermInMonths()))).divide(
                BigDecimal.valueOf(100), command.getServiceChargeRatePrecision(), RoundingLogic.fromString(command.getRoundingMode())).add(BigDecimal.ONE),
            command.getPrincipalAmountPrecision(), RoundingLogic.fromString(command.getRoundingMode()));

        BigDecimal serviceChargePerInstallment = command.getInstallmentAmount().subtract(principalPerInstallment);

        log.info("serviceChargeRatePerAnnum : {}", annualServiceChargeRate);
        log.info("principal per installment : {}", principalPerInstallment);
        log.info("installmentAmount : {}", command.getInstallmentAmount());
        log.info("isMonthly : {}", command.getIsMonthly());
        log.info("loan Terms in years : {}", getLoanTermInYears(command.getLoanTermInMonths()));

        BigDecimal finalAnnualServiceChargeRate = annualServiceChargeRate;
        return holidayUseCase
                .getAllHolidaysOfAOfficeByOfficeId(command.getOfficeId())
                .collectList()
                .map(holidays -> getRepaymentDates(holidays, command.getCutOffDate(), DayOfWeek.valueOf(command.getSamityDay()), command.getNoOfInstallments(), command.getNoOfPastInstallments(), command.getIsMonthly()))
                .map(getRepaymentDates -> this.generateRepaymentScheduleForFlatInstallmentV2(command.getLoanAmount(), command.getNoOfInstallments(), command.getInstallmentAmount(), getRepaymentDates, principalPerInstallment, serviceChargePerInstallment, command.getDisbursedLoanAmount())
                        .stream()
                        .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                        .toList())
                .map(repaymentScheduleViewDTOS -> Tuples.of(repaymentScheduleViewDTOS, finalAnnualServiceChargeRate));
    }

    private BigDecimal getLoanTermInYears(Integer  loanTermInMonth) {
        if (loanTermInMonth == null)
            return BigDecimal.ONE;
        return BigDecimal.valueOf(loanTermInMonth).divide(BigDecimal.valueOf(12));
    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlatV2(MigrationRepaymentScheduleCommand command) {
        log.info("command : {}", command);

        AtomicReference<BigDecimal> annualServiceChargeRate = new AtomicReference<>();
        return this.viewRepaymentScheduleFlatV2(command)
                .map(tuple2 -> {
                    annualServiceChargeRate.set(tuple2.getT2());
                    return tuple2.getT1()
                            .stream()
                            .map(repaymentScheduleViewDTO -> modelMapper.map(repaymentScheduleViewDTO, RepaymentSchedule.class))
                            .toList();
                })
                .map(repaymentSchedule -> this.createRepaymentScheduleToSaveToDBFlat(repaymentSchedule, command.getLoanAccountId(), command.getMemberId(), command.getMfiId(), command.getStatus(), command.getLoginId()))
                .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                .flatMap(port::saveRepaymentSchedule)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class))
                .collectList()
                .map(list -> Tuples.of(list, annualServiceChargeRate.get()));
    }

    @Override
    public Mono<List<RepaymentSchedule>> viewRepaymentScheduleFlatInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command) {
        log.info("command : {}", command);
        return holidayUseCase.getAllHolidaysOfASamityByLoanAccountId(command.getLoanAccountId())
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(holidayList -> log.info("Holiday List: {}", holidayList))
                .flatMap(holidayList -> {

                    log.info("accumulated loan amount : {} , total outstanding amount: {}", command.getAccumulatedLoanAmount(),command.getTotalOutstandingAmount());
                    double paidAmount = command.getAccumulatedLoanAmount().subtract(command.getTotalOutstandingAmount()).doubleValue();

                    int beforeCutOffInstallments = (int) Math.ceil((paidAmount + command.getOverdueAmount().doubleValue()) / command.getInstallmentAmount().doubleValue());
                    int afterCutOffInstallments = command.getNoOfInstallments() - beforeCutOffInstallments;

                    log.info("before CutOff installments : {}", beforeCutOffInstallments);
                    log.info("after CutOff installments : {}", afterCutOffInstallments);
                    log.info("total installments : {}", command.getNoOfInstallments());

                    return commonRepository.getMemberSamityOfficeInfoByLoanAccountId(command.getLoanAccountId())
                            .map(memberSamityOfficeEntity -> {
                                LocalDate cutOffDate = command.getOverdueAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                                        || (command.getTotalOutstandingAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                                        && command.getOverdueAmount().compareTo(BigDecimal.ZERO) == 0)
                                        ? command.getDisbursementDate()
                                        : command.getCutOffDate();
                                if (memberSamityOfficeEntity.getLoanTypeId().equalsIgnoreCase(LoanTypeID.LOAN_TYPE_SS.getValue())) {
                                    cutOffDate = command.getDisbursementDate();
                                }
                                return Tuples.of(memberSamityOfficeEntity, cutOffDate);
                            })
                            .flatMap(memberSamityOfficeEntityAndCutOffDateTuple -> {
                                MemberSamityOfficeEntity memberSamityOfficeEntity = memberSamityOfficeEntityAndCutOffDateTuple.getT1();
                                LocalDate cutOffDate = memberSamityOfficeEntityAndCutOffDateTuple.getT2();
                                return cutOffDate.equals(command.getCutOffDate())
                                        ? getRepaymentScheduleWithCutOffDate(memberSamityOfficeEntity, command, holidayList, afterCutOffInstallments, beforeCutOffInstallments)
                                        : getRepaymentScheduleWithDisbursementDate(memberSamityOfficeEntity, command, holidayList);
                            });
                    });
    }

    private Mono<List<RepaymentSchedule>> getRepaymentScheduleWithCutOffDate(MemberSamityOfficeEntity memberSamityOfficeEntity, RepaymentScheduleCommand command, List<LocalDate> holidayList, int afterCutOffInstallments, int beforeCutOffInstallments) {

        return Mono.just(this.getRepaymentDates(holidayList, command.getCutOffDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), 1, afterCutOffInstallments, command.getRepaymentFrequency(), memberSamityOfficeEntity.getMonthlyRepayDay(),  command.getLoanTermInMonths()))
                .doOnNext(repaymentDates -> log.info("After cut off repayment schedule Dates: {}", repaymentDates))
                .map(repaymentDates -> {
                    Collections.sort(repaymentDates);
                    log.info("After Cut off repay dates : {} | size : {}", repaymentDates, repaymentDates.size());
                    List<LocalDate> paidRepayDates = this.getRepaymentDatesReverse(holidayList, beforeCutOffInstallments, command.getRepaymentFrequency(), command.getCutOffDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), command.getMonthlyRepaymentFrequencyDay());
                    log.info("paid repay dates : {} | size : {}", paidRepayDates, paidRepayDates.size());
                    repaymentDates.addAll(paidRepayDates);
                    Collections.sort(repaymentDates);
                    log.info("repaymentDates : {}", repaymentDates);
                    log.info("repaymentDates size : {}", repaymentDates.size());
                    List<RepaymentSchedule> pendingRepaymentSchedule = this.getPendingRepaymentSchedules(repaymentDates, command);

                    log.info("final repayment schedule : {}", pendingRepaymentSchedule);

                    log.info("total principal : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add));
                    log.info("total service charge : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add));
                    return pendingRepaymentSchedule;
                });
    }

    private Mono<List<RepaymentSchedule>> getRepaymentScheduleWithDisbursementDate(MemberSamityOfficeEntity memberSamityOfficeEntity, RepaymentScheduleCommand command, List<LocalDate> holidayList) {
        return Mono.just(this.getRepaymentDates(holidayList, command.getDisbursementDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), command.getGraceDays(), command.getNoOfInstallments(), command.getRepaymentFrequency(), memberSamityOfficeEntity.getMonthlyRepayDay(), command.getLoanTermInMonths()))
                .doOnNext(repaymentDates -> log.info("After cut off repayment schedule Dates: {}", repaymentDates))
                .map(repaymentDates -> {
                    Collections.sort(repaymentDates);

                    List<RepaymentSchedule> pendingRepaymentSchedule = this.getPendingRepaymentSchedules(repaymentDates, command);

                    log.info("final repayment schedule : {}", pendingRepaymentSchedule);

                    log.info("total principal : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add));
                    log.info("total service charge : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add));
                    return pendingRepaymentSchedule;
                });
    }


    private List<RepaymentSchedule> createRepaymentScheduleToSaveToDBFlat(List<RepaymentSchedule> repaymentScheduleList, String loanAccountId, String memberId, String mfiId, String status, String loginId) {

        // TODO: 6/15/23  pass these infos as argument
        /*String loanAccountId = "JAGO-W-1006-107-1001-1";
        String memberId = "1006-107-1001";
        String mfiId = "M1000";
        String statusFromLoanAccount = "pending";*/


        return repaymentScheduleList
                .stream()
                .map(repaymentSchedule -> {
                    repaymentSchedule.setLoanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId());
                    repaymentSchedule.setLoanAccountId(loanAccountId);
                    repaymentSchedule.setMemberId(memberId);
                    repaymentSchedule.setInstallNo(repaymentSchedule.getInstallNo());
                    repaymentSchedule.setInstallDate(repaymentSchedule.getInstallDate());
                    repaymentSchedule.setBeginPrinBalance(repaymentSchedule.getBeginPrinBalance());
                    repaymentSchedule.setScheduledPayment(repaymentSchedule.getScheduledPayment());
                    repaymentSchedule.setExtraPayment(repaymentSchedule.getExtraPayment());
                    repaymentSchedule.setTotalPayment(repaymentSchedule.getTotalPayment());
                    repaymentSchedule.setPrincipal(repaymentSchedule.getPrincipal());
                    repaymentSchedule.setServiceCharge(repaymentSchedule.getServiceCharge());
                    repaymentSchedule.setEndPrinBalance(repaymentSchedule.getEndPrinBalance());
                    repaymentSchedule.setMfiId(mfiId);
                    repaymentSchedule.setCreatedOn(LocalDateTime.now());
                    repaymentSchedule.setCreatedBy(loginId);
                    repaymentSchedule.setStatus(status);
                    return repaymentSchedule;
                })
//                .skip(1)
                .toList();
    }

    @Override
    public Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleFlatInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command) {
        return this.viewRepaymentScheduleFlatInstallmentAmountProvidedForMigration(command)
                            .map(repaymentSchedule -> this.createRepaymentScheduleToSaveToDBFlat(repaymentSchedule, command.getLoanAccountId(), command.getMemberId(), command.getMfiId(), Status.STATUS_PENDING.getValue(), command.getLoginId()))
                            .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                            .flatMap(port::saveRepaymentSchedule)
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                            .flatMapMany(Flux::fromIterable)
                            .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class))
                            .collectList();
    }



    @Override
    public Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleFlatInstallmentAmountProvidedForMigrationV2(RepaymentScheduleCommand command) {

        return holidayUseCase.getAllHolidaysOfASamityByLoanAccountId(command.getLoanAccountId())
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(holidayList -> log.info("Holiday List: {}", holidayList))
                .flatMap(holidayList -> {
                    BigDecimal totalPaidAmount = command.getLoanAmount().add(command.getTotalServiceCharge()).subtract(command.getTotalOutstandingAmount());
                    Integer fullyPaidInstallments = totalPaidAmount.divide(command.getInstallmentAmount(), 0, RoundingMode.FLOOR).intValue();
                    log.info("fully paid installments : {}", fullyPaidInstallments);
                    BigDecimal paidTotalAmount = command.getLoanAmount()
                            .add(command.getTotalServiceCharge())
                            .subtract(command.getTotalOutstandingAmount());
                    log.info("paid total amount : {}", paidTotalAmount);
                    BigDecimal partialPaidAmount = paidTotalAmount
                            .subtract(command.getInstallmentAmount().multiply(BigDecimal.valueOf(fullyPaidInstallments)));
                    log.info("partial paid amount : {}", partialPaidAmount);
                    Integer partialPaidInstallmentNo = partialPaidAmount.compareTo(BigDecimal.ZERO) > 0 ? fullyPaidInstallments + 1 : 0;
                    log.info("partialPaidInstallmentNo : {}", partialPaidInstallmentNo);
                    Integer remainingInstallments = command.getNoOfInstallments() - fullyPaidInstallments;
                    log.info("remaining installments : {}", remainingInstallments);
                    AtomicReference<MemberSamityOfficeEntity> memberSamityOfficeEntityAtomicReference = new AtomicReference<>();

                    int graceDays = command.getOverdueAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                            || (command.getTotalOutstandingAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                            && command.getOverdueAmount().compareTo(BigDecimal.ZERO) == 0)
                                ? command.getGraceDays()
                                : 0;

                    LocalDate cutOffDate = command.getOverdueAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                            || (command.getTotalOutstandingAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                            && command.getOverdueAmount().compareTo(BigDecimal.ZERO) == 0)
                            ? command.getDisbursementDate()
                            : command.getCutOffDate();




                    return commonRepository.getMemberSamityOfficeInfoByLoanAccountId(command.getLoanAccountId())
                            .doOnNext(memberSamityOfficeEntityAtomicReference::set)
                            .flatMap(loanAccountResponseDTO -> Mono.just(this.getRepaymentDates(holidayList, command.getCutOffDate(), DayOfWeek.valueOf(loanAccountResponseDTO.getSamityDay().toUpperCase()), graceDays, remainingInstallments, command.getRepaymentFrequency(), loanAccountResponseDTO.getMonthlyRepayDay(), command.getLoanTermInMonths()))
                            .doOnNext(repaymentDates -> log.info("Repayment schedule Dates: {}", repaymentDates))
                            .map(repaymentDates -> {
                                List<RepaymentSchedule> pendingRepaymentSchedule = this.getPendingRepaymentSchedulesV2(repaymentDates, command, fullyPaidInstallments, remainingInstallments, partialPaidInstallmentNo, partialPaidAmount);
                                List<LocalDate> paidRepaymentDates = this.getRepaymentDatesReverse(holidayList, fullyPaidInstallments, command.getRepaymentFrequency(), pendingRepaymentSchedule.get(0).getInstallDate(), DayOfWeek.valueOf(loanAccountResponseDTO.getSamityDay().toUpperCase()), command.getMonthlyRepaymentFrequencyDay());
                                List<RepaymentSchedule> paidRepaymentSchedules = this.getPaidRepaymentSchedules(fullyPaidInstallments, paidRepaymentDates, command);
                                pendingRepaymentSchedule.addAll(paidRepaymentSchedules);
                                pendingRepaymentSchedule.sort(Comparator.comparing(RepaymentSchedule::getInstallNo));

                                log.info("final repayment schedule : {}", pendingRepaymentSchedule);

                                log.info("total principal : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add));
                                log.info("total service charge : {}", pendingRepaymentSchedule.stream().map(RepaymentSchedule::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add));
                                return pendingRepaymentSchedule;
                            }))
                            .flatMapMany(Flux::fromIterable)
                            .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class))
                            .collectList();

                });
    }

    @Override
    public Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleDecliningInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        log.info("command : {}", command);
        double paidAmount = command.getAccumulatedLoanAmount().subtract(command.getTotalOutstandingAmount()).doubleValue(); // 20000
        /*int paidInstallments = (int) (paidAmount / command.getInstallmentAmount().doubleValue()); // 20
        int overdueInstallments = (int) Math.ceil(command.getOverdueAmount().doubleValue() / command.getInstallmentAmount().doubleValue()); // 10
        int beforeCutOffInstallments = paidInstallments + overdueInstallments; // 30*/
        int beforeCutOffInstallments = (int) Math.ceil((paidAmount + command.getOverdueAmount().doubleValue()) / command.getInstallmentAmount().doubleValue()); // 30
        int afterCutOffInstallments = command.getNoOfInstallments() - beforeCutOffInstallments; // 15

        /*log.info("paid installments : {}", paidInstallments);
        log.info("overdue installments : {}", overdueInstallments);*/
        log.info("before CutOff installments : {}", beforeCutOffInstallments);
        log.info("after CutOff installments : {}", afterCutOffInstallments);
        log.info("total installments : {}", command.getNoOfInstallments());

        LocalDate cutOffDate = command.getOverdueAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                || (command.getTotalOutstandingAmount().compareTo(command.getAccumulatedLoanAmount()) == 0
                && command.getOverdueAmount().compareTo(BigDecimal.ZERO) == 0)
                ? command.getDisbursementDate()
                : command.getCutOffDate();


        return holidayUseCase.getAllHolidaysOfAOfficeByOfficeId(command.getOfficeId())
                .collectList()
                .flatMap(holidays -> commonRepository.getMemberSamityOfficeInfoByLoanAccountId(command.getLoanAccountId())
                        .map(memberSamityOfficeEntity -> cutOffDate.equals(command.getCutOffDate())
                            ? getRepaymentDatesWithCutOffDate(memberSamityOfficeEntity, command, holidays, afterCutOffInstallments, beforeCutOffInstallments)
                            : getRepaymentDatesWithDisbursementDate(memberSamityOfficeEntity, command, holidays)))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                            .map(metaProperty -> {
                                RoundingMode roundingMode = CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic());
                                BigDecimal serviceChargeRatePerPeriod = round(metaProperty.getServiceChargeRatePrecision(), BigDecimal.valueOf(command.getAnnualServiceChargeRate().doubleValue() / command.getNoOfInstallments()), roundingMode);
                                BigDecimal initialServiceCharge = round(8, serviceChargeRatePerPeriod.multiply(command.getLoanAmount()), roundingMode);

                                return this.getRepaymentSchedulesList(command.getLoanAmount(), command.getNoOfInstallments(), command.getInstallmentAmount(), repaymentDates, repaymentScheduleList, CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic()), serviceChargeRatePerPeriod, initialServiceCharge, command.getInstallmentAmount(), BigDecimal.ZERO, metaProperty.getServiceChargePrecision())
                                            .stream()
//                                            .skip(1)
                                        .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentSchedule.class))
                                            .toList();
                            });
                })
                .map(repaymentSchedule -> this.createRepaymentScheduleToSaveToDB(repaymentSchedule, command.getLoanAccountId(), command.getMemberId(), command.getMfiId(), Status.STATUS_PENDING.getValue(), command.getLoginId()))
                .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                .flatMap(port::saveRepaymentSchedule)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> modelMapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class))
                .collectList();
    }

    private List<RepaymentSchedule> createRepaymentScheduleToSaveToDB(List<RepaymentSchedule> repaymentScheduleList, String loanAccountId, String memberId, String mfiId, String status, String createdBy) {

        // TODO: 6/15/23  pass these infos as argument
        /*String loanAccountId = "JAGO-W-1006-107-1001-1";
        String memberId = "1006-107-1001";
        String mfiId = "M1000";
        String statusFromLoanAccount = "pending";*/


        return repaymentScheduleList
                .stream()
                .map(repaymentSchedule -> {
                    repaymentSchedule.setLoanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId());
                    repaymentSchedule.setLoanAccountId(loanAccountId);
                    repaymentSchedule.setMemberId(memberId);
                    repaymentSchedule.setInstallNo(repaymentSchedule.getInstallNo());
                    repaymentSchedule.setInstallDate(repaymentSchedule.getInstallDate());
                    repaymentSchedule.setBeginPrinBalance(repaymentSchedule.getBeginPrinBalance());
                    repaymentSchedule.setScheduledPayment(repaymentSchedule.getScheduledPayment());
                    repaymentSchedule.setExtraPayment(repaymentSchedule.getExtraPayment());
                    repaymentSchedule.setTotalPayment(repaymentSchedule.getTotalPayment());
                    repaymentSchedule.setPrincipal(repaymentSchedule.getPrincipal());
                    repaymentSchedule.setServiceCharge(repaymentSchedule.getServiceCharge());
                    repaymentSchedule.setEndPrinBalance(repaymentSchedule.getEndPrinBalance());
                    repaymentSchedule.setMfiId(mfiId);
                    repaymentSchedule.setCreatedOn(LocalDateTime.now());
                    repaymentSchedule.setCreatedBy(createdBy);
                    repaymentSchedule.setStatus(status);
                    return repaymentSchedule;
                })
                .skip(1)
                .toList();
    }

    private List<LocalDate> getRepaymentDatesWithCutOffDate(MemberSamityOfficeEntity memberSamityOfficeEntity, RepaymentScheduleCommand command, List<LocalDate> holidays, int afterCutOffInstallments, int beforeCutOffInstallments) {

            List<LocalDate> afterCutOffRepaymentDates = this.getRepaymentDates(holidays, command.getCutOffDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), 1, afterCutOffInstallments, command.getRepaymentFrequency(), command.getMonthlyRepaymentFrequencyDay(), command.getLoanTermInMonths());

            List<LocalDate> beforeCutOffRepaymentDates = this.getRepaymentDatesReverse(holidays, beforeCutOffInstallments, command.getRepaymentFrequency(), command.getCutOffDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), command.getMonthlyRepaymentFrequencyDay());

            afterCutOffRepaymentDates.addAll(beforeCutOffRepaymentDates); // this should be equal to noOfInstallments
            Collections.sort(afterCutOffRepaymentDates);
            log.info("noOfInstallments from repaymentDates : {}", afterCutOffRepaymentDates.size());
            return afterCutOffRepaymentDates;
    };

    private List<LocalDate> getRepaymentDatesWithDisbursementDate(MemberSamityOfficeEntity memberSamityOfficeEntity, RepaymentScheduleCommand command, List<LocalDate> holidayList) {
        return this.getRepaymentDates(holidayList, command.getDisbursementDate(), DayOfWeek.valueOf(memberSamityOfficeEntity.getSamityDay().toUpperCase()), command.getGraceDays(), command.getNoOfInstallments(), command.getRepaymentFrequency(), memberSamityOfficeEntity.getMonthlyRepayDay(), command.getLoanTermInMonths());
    }


    private List<RepaymentSchedule> getRepaymentSchedulesList(BigDecimal loanAmount, Integer noOfInstallments, BigDecimal installmentAmount, List<LocalDate> samityDays, List<RepaymentSchedule> repaymentScheduleList, RoundingMode roundingMode, BigDecimal serviceChargeRatePerPeriod, BigDecimal initialServiceCharge, BigDecimal probableEI, BigDecimal adjustedAmount, Integer serviceChargePrecision) {
        initialServiceCharge = round(serviceChargePrecision, initialServiceCharge, roundingMode);
        log.info("samity days : {}", samityDays);
        int count = 0;
        for (int j = 0; j <= noOfInstallments; j++) {
            RepaymentSchedule table = new RepaymentSchedule();
            // First Row , Installment 0
            if (count == 0) {
                table.setBeginPrinBalance(loanAmount);
                table.setEndPrinBalance(loanAmount);
                table.setPrincipal(BigDecimal.ZERO);
                table.setServiceCharge(BigDecimal.ZERO);
                table.setTotalPayment(BigDecimal.ZERO);
                table.setInstallNo(0);
                table.setExtraPayment(BigDecimal.ZERO);
                table.setScheduledPayment(BigDecimal.ZERO);
                table.setInstallDate(samityDays.get(0));
                table.setLoanRepayScheduleId("LoanRepayScheduleId");
                repaymentScheduleList.add(table);
                count++;
            }
            // Last Installment
            else if (count == noOfInstallments) {
                BigDecimal lastPrincipal = loanAmount.subtract(repaymentScheduleList.stream().skip(1).map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add));
                BigDecimal previousBalance = round(2, repaymentScheduleList.get(j - 1).getEndPrinBalance(), roundingMode);
                BigDecimal lastInterest = round(serviceChargePrecision, serviceChargeRatePerPeriod.multiply(previousBalance), roundingMode);
                BigDecimal paymentDue = lastPrincipal.add(lastInterest);
                BigDecimal updatedBalance = previousBalance.subtract(lastPrincipal).setScale(0, RoundingMode.FLOOR);
                table.setInstallNo(repaymentScheduleList.get(j - 1).getInstallNo() + 1);
                table.setPrincipal(lastPrincipal);
                table.setServiceCharge(lastInterest);
                table.setTotalPayment(paymentDue);
                table.setScheduledPayment(probableEI);
                table.setExtraPayment(table.getTotalPayment().subtract(table.getScheduledPayment()));
                table.setEndPrinBalance(updatedBalance);
                table.setBeginPrinBalance(previousBalance);
                table.setInstallDate(samityDays.get(samityDays.size() - 1));
                table.setLoanRepayScheduleId(UUID.randomUUID() + "-repay-" + table.getInstallNo());
                repaymentScheduleList.add(table);
                count++;
            }
            // Installment No (1 -> n-1)
            else {
                BigDecimal previousBalance = round(2, repaymentScheduleList.get(j - 1).getEndPrinBalance(), roundingMode);
                // installment No 1
                if (repaymentScheduleList.get(j - 1).getServiceCharge() == null) {
                    createTableData(installmentAmount, repaymentScheduleList, j, table, previousBalance, initialServiceCharge, adjustedAmount, probableEI, samityDays, count - 1);
                    count++;
                }
                // installment No (2 -> n-1)
                else {
                    BigDecimal updatedInterest = round(serviceChargePrecision, serviceChargeRatePerPeriod.multiply(previousBalance), roundingMode);
                    createTableData(installmentAmount, repaymentScheduleList, j, table, previousBalance, updatedInterest, adjustedAmount, probableEI, samityDays, count - 1);
                    count++;
                }
            }
        }
        return repaymentScheduleList;
    }

    private BigDecimal round(int scale, BigDecimal amount, RoundingMode roundingMode) {
        return amount.setScale(scale, roundingMode);
    }

    private void createTableData(BigDecimal installmentAmount, List<RepaymentSchedule> list, Integer j, RepaymentSchedule table, BigDecimal previousBalance, BigDecimal updatedInterest, BigDecimal adjustedAmount, BigDecimal scheduledAmount, List<LocalDate> repaymentDates, Integer count) {

        BigDecimal updatedPrincipal = installmentAmount.subtract(updatedInterest);
        table.setPrincipal(updatedPrincipal);
        BigDecimal updatedBalance = previousBalance.subtract(updatedPrincipal);
        table.setEndPrinBalance(updatedBalance);
        table.setExtraPayment(adjustedAmount);
        table.setScheduledPayment(scheduledAmount);
        table.setServiceCharge(updatedInterest);
        table.setInstallNo(list.get(j - 1).getInstallNo() + 1);
        table.setTotalPayment(installmentAmount);
        table.setBeginPrinBalance(previousBalance);
        table.setInstallDate(repaymentDates.get(count));
        table.setLoanRepayScheduleId(UUID.randomUUID() + "-repay-" + table.getInstallNo());
        list.add(table);
    }

    public List<LocalDate> getRepaymentDatesReverse(List<LocalDate> holidays, Integer noOfPaidInstallments, String repaymentFrequency, LocalDate cutOffDate, DayOfWeek samityDay, int monthlyRepaymentFrequencyDay) {

        List<LocalDate> samityDays = new LinkedList<>();


        int daysToSubtract = 0;
        switch (repaymentFrequency.toUpperCase()) {
            case Constants.REPAYMENT_FREQUENCY_WEEKLY -> daysToSubtract = 7;
            case Constants.REPAYMENT_FREQUENCY_FORTNIGHTLY -> daysToSubtract = 14;
            case Constants.REPAYMENT_FREQUENCY_MONTHLY -> daysToSubtract = 30;
            case Constants.REPAYMENT_FREQUENCY_BIMONTHLY -> daysToSubtract = 60;
            case Constants.REPAYMENT_FREQUENCY_QUARTERLY -> daysToSubtract = 90;
            case Constants.REPAYMENT_FREQUENCY_FOUR_MONTHLY -> daysToSubtract = 120;
            case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> daysToSubtract = 180;
            case Constants.REPAYMENT_FREQUENCY_YEARLY -> daysToSubtract = 365;
        }

        LocalDate firstDateBeforeCutOff = cutOffDate;

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)) {
            while (firstDateBeforeCutOff.getDayOfWeek() != samityDay) {
                firstDateBeforeCutOff = firstDateBeforeCutOff.minusDays(1);
            }
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_MONTHLY)) {
            firstDateBeforeCutOff =  calculateTargetSamityDayReverse(cutOffDate, monthlyRepaymentFrequencyDay, samityDay);
        }

        if (holidays.contains(firstDateBeforeCutOff)) {
            firstDateBeforeCutOff = repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)
                    ? firstDateBeforeCutOff.minusDays(7)
                    : firstDateBeforeCutOff.minusDays(1);
        }

        LocalDate installDate = firstDateBeforeCutOff;
        for (int i = 0 ; i < noOfPaidInstallments ; i++) {
            int daysToSubtractInCaseOfHoliday = repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY) ? 7 : 1;
            if (holidays.contains(installDate)) {
                installDate = installDate.minusDays(daysToSubtractInCaseOfHoliday);
                samityDays.add(installDate);
            } else {
                samityDays.add(installDate);
            }
            installDate = installDate.minusDays(daysToSubtract);
        }

        Collections.sort(samityDays);

        log.info("samityDays from Service.getRepaymentDatesReverse : {}", samityDays);
        return samityDays;

    }

    private List<RepaymentSchedule> getPendingRepaymentSchedules(List<LocalDate> repaymentDates, RepaymentScheduleCommand command) {
        int outstandingInstallments = repaymentDates.size();
        log.info("Repaymenct Schedule command : {}", command);
        log.info("outstandingInstallments : {}", outstandingInstallments);
        BigDecimal totalPrincipalBeforeLastInstallment = command.getInstallmentPrincipal().multiply(BigDecimal.valueOf(outstandingInstallments - 1));
        log.info("totalPrincipalBeforeLastInstallment : {}", totalPrincipalBeforeLastInstallment);
        BigDecimal totalServiceChargeBeforeLastInstallment = command.getInstallmentServiceCharge().multiply(BigDecimal.valueOf(outstandingInstallments - 1));
        log.info("totalServiceChargeBeforeLastInstallment : {}", totalServiceChargeBeforeLastInstallment);
        BigDecimal lastInstallmentPrincipal = command.getLoanAmount().subtract(totalPrincipalBeforeLastInstallment);
        log.info("lastInstallmentPrincipal : {}", lastInstallmentPrincipal);
        BigDecimal lastInstallmentServiceCharge = command.getTotalServiceCharge().subtract(totalServiceChargeBeforeLastInstallment);
        log.info("lastInstallmentServiceCharge : {}", lastInstallmentServiceCharge);
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        Collections.sort(repaymentDates);

        log.info("testng.css repaymentDates : {}", repaymentDates);
        for (int i = 0; i < outstandingInstallments - 1 ; i++) {  //command.getNoOfInstallments()

            repaymentScheduleList.add(
                    RepaymentSchedule
                            .builder()
                            .installNo(i+1)
                            .installDate(repaymentDates.get(i))
                            .scheduledPayment(command.getInstallmentAmount())
                            .beginPrinBalance(repaymentScheduleList.isEmpty()
                                    ? command.getLoanAmount()
                                    : repaymentScheduleList.get(i-1).getEndPrinBalance())
                            .principal(command.getInstallmentPrincipal())
                            .serviceCharge(command.getInstallmentServiceCharge())
                            .endPrinBalance(repaymentScheduleList.isEmpty()
                                    ? command.getLoanAmount().subtract(command.getInstallmentPrincipal())
                                    : repaymentScheduleList.get(i-1).getEndPrinBalance().subtract(command.getInstallmentPrincipal()))
                            .totalPayment(command.getInstallmentAmount())
                            .loanRepayScheduleId(UUID.randomUUID().toString())
                            .build());
        }
        log.info("testng.css repaymentScheduleList : {}", repaymentScheduleList);

        // last installment
        if(!repaymentScheduleList.isEmpty()){
            repaymentScheduleList.add(
                    RepaymentSchedule
                            .builder()
                            .installNo(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getInstallNo() + 1)
                            .installDate(repaymentDates.get(repaymentDates.size() - 1))
                            .scheduledPayment(command.getInstallmentAmount())
                            .beginPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance())
                            .principal(lastInstallmentPrincipal)
                            .serviceCharge(lastInstallmentServiceCharge)
                            .totalPayment(lastInstallmentPrincipal.add(lastInstallmentServiceCharge))
                            .endPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance().subtract(lastInstallmentPrincipal))
                            .loanRepayScheduleId(UUID.randomUUID().toString())
                            .build());
        } else{
            repaymentScheduleList.add(
                    RepaymentSchedule
                            .builder()
                            .installNo(command.getNoOfInstallments())
                            .installDate(repaymentDates.get(repaymentDates.size() - 1))
                            .scheduledPayment(command.getInstallmentAmount())
                            .beginPrinBalance(lastInstallmentPrincipal)
                            .principal(lastInstallmentPrincipal)
                            .serviceCharge(lastInstallmentServiceCharge)
                            .totalPayment(lastInstallmentPrincipal.add(lastInstallmentServiceCharge))
                            .endPrinBalance(lastInstallmentPrincipal.subtract(lastInstallmentPrincipal))
                            .loanRepayScheduleId(UUID.randomUUID().toString())
                            .build());
        }


        log.info("final repayment schedule list : {}", repaymentScheduleList);

        return repaymentScheduleList;
    }

    private List<RepaymentSchedule> getPendingRepaymentSchedulesV2(List<LocalDate> repaymentDates, RepaymentScheduleCommand command, Integer paidInstallments, Integer remainingInstallments, Integer partialPaidInstallmentNo, BigDecimal partialPaidAmount) {
        int outstandingInstallments = repaymentDates.size();
        BigDecimal totalPrincipalBeforeLastInstallment = command.getInstallmentPrincipal().multiply(BigDecimal.valueOf(outstandingInstallments - 1));
        BigDecimal totalServiceChargeBeforeLastInstallment = command.getInstallmentServiceCharge().multiply(BigDecimal.valueOf(outstandingInstallments - 1));
        BigDecimal lastInstallmentPrincipal = command.getOutstandingPrincipal().subtract(totalPrincipalBeforeLastInstallment);
        BigDecimal lastInstallmentServiceCharge = command.getOutstandingServiceCharge().subtract(totalServiceChargeBeforeLastInstallment);
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        Collections.sort(repaymentDates);

        /*if(partialPaidInstallmentNo > 0) {
            BigDecimal partialInstallmentServiceCharge = partialPaidAmount.subtract(command.getInstallmentServiceCharge()).compareTo(BigDecimal.ZERO) > 0
                    ? command.getInstallmentServiceCharge()
                    : partialPaidAmount;

            BigDecimal partialInstallmentPrincipal = partialPaidAmount.subtract(partialInstallmentServiceCharge).compareTo(BigDecimal.ZERO) > 0
                    ?
                    :

        }*/


        for (int i = 0; i < remainingInstallments - 1 ; i++) {

            repaymentScheduleList.add(
                    RepaymentSchedule
                            .builder()
                            .installNo(i+paidInstallments+1)
                            .installDate(repaymentDates.get(i))
                            .scheduledPayment(command.getInstallmentAmount())
                            .beginPrinBalance(repaymentScheduleList.isEmpty()
                                    ? command.getOutstandingPrincipal()
                                    : repaymentScheduleList.get(i-1).getEndPrinBalance())
                            .principal(command.getInstallmentPrincipal())
                            .serviceCharge(command.getInstallmentServiceCharge())
                            .endPrinBalance(repaymentScheduleList.isEmpty()
                                    ? command.getOutstandingPrincipal().subtract(command.getInstallmentPrincipal())
                                    : repaymentScheduleList.get(i-1).getEndPrinBalance().subtract(command.getInstallmentPrincipal()))
                            .totalPayment(command.getInstallmentAmount())
                            .build());
        }

        // last installment

        repaymentScheduleList.add(
                RepaymentSchedule
                        .builder()
                        .installNo(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getInstallNo() + 1)
                        .installDate(repaymentDates.get(repaymentDates.size() - 1))
                        .scheduledPayment(command.getInstallmentAmount())
                        .beginPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance())
                        .principal(lastInstallmentPrincipal)
                        .serviceCharge(lastInstallmentServiceCharge)
                        .totalPayment(lastInstallmentPrincipal.add(lastInstallmentServiceCharge))
                        .endPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance().subtract(lastInstallmentPrincipal))
                        .build());


        return repaymentScheduleList;
    }


    private List<RepaymentSchedule> getPaidRepaymentSchedules(Integer paidInstallments, List<LocalDate> repaymentDates, RepaymentScheduleCommand command) {
        // 1 - paid installments
        List<RepaymentSchedule> paidInstallmentsList = new ArrayList<>();
        for (int i = 0 ; i < paidInstallments ; i++) {
            paidInstallmentsList.add(
                    RepaymentSchedule
                            .builder()
                            .installNo(i+1)
                            .installDate(repaymentDates.get(i))
                            .scheduledPayment(command.getInstallmentAmount())
                            .beginPrinBalance(paidInstallmentsList.isEmpty()
                                    ? command.getLoanAmount()
                                    : paidInstallmentsList.get(i-1).getEndPrinBalance())
                            .principal(command.getInstallmentPrincipal())
                            .serviceCharge(command.getInstallmentServiceCharge())
                            .endPrinBalance(paidInstallmentsList.isEmpty()
                                    ? command.getLoanAmount().subtract(command.getInstallmentPrincipal())
                                    : paidInstallmentsList.get(i-1).getEndPrinBalance().subtract(command.getInstallmentPrincipal()))
                            .totalPayment(command.getInstallmentAmount())
                            .build());
        }

        log.info("paid Installment list : {}", paidInstallmentsList);
        return paidInstallmentsList;
    }

    public RoundingMode getRoundingMode(String roundingMode) {
        // By default natural rounding is selected
        RoundingMode roundingModeSelected = RoundingMode.HALF_UP;
        RepaymentScheduleEnum roundingModeEnum = RepaymentScheduleEnum.valueOf(roundingMode);

        switch (roundingModeEnum) {
            case ROUNDING_UP -> roundingModeSelected = RoundingMode.UP;
            case ROUNDING_DOWN, NO_ROUNDING -> roundingModeSelected = RoundingMode.DOWN;
        }
        return roundingModeSelected;
    }

    public BigDecimal roundInstallmentAmountIfNecessary(BigDecimal installmentAmount, Integer roundingToNearest, String roundingToNearestIntegerLogic) {
        BigDecimal roundedEI;
        double installmentAmountDouble = installmentAmount.doubleValue();
        RepaymentScheduleEnum roundingLogicEnum = RepaymentScheduleEnum.valueOf(roundingToNearestIntegerLogic);

        switch (roundingLogicEnum) {
            case ROUNDING_UP:
                if (roundingToNearest == 1)
                    roundedEI = BigDecimal.valueOf(Math.ceil(installmentAmountDouble));
                else if (roundingToNearest == 5)
                    roundedEI = BigDecimal.valueOf(5 * Math.ceil(installmentAmountDouble / 5));
                else if (roundingToNearest == 10)
                    roundedEI = BigDecimal.valueOf(10 * Math.ceil(installmentAmountDouble / 10));
                else
                    roundedEI = BigDecimal.valueOf(Math.ceil(installmentAmountDouble));
                break;
            case ROUNDING_DOWN:
                if (roundingToNearest == 1)
                    roundedEI = BigDecimal.valueOf(Math.floor(installmentAmountDouble));
                else if (roundingToNearest == 5)
                    roundedEI = BigDecimal.valueOf(5 * Math.floor(installmentAmountDouble / 5));
                else if (roundingToNearest == 10)
                    roundedEI = BigDecimal.valueOf(10 * Math.floor(installmentAmountDouble / 10));
                else
                    roundedEI = BigDecimal.valueOf(Math.floor(installmentAmountDouble));
                break;
            case NO_ROUNDING_TO_INTEGER:
                roundedEI = BigDecimal.valueOf(installmentAmountDouble);
                break;
            default:
                roundedEI = BigDecimal.valueOf(Math.ceil(installmentAmountDouble));
        }
        return roundedEI;
    }
    public List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate cutOffDate, DayOfWeek samityDay, int totalInstallments, int paidNoOfInstallments, Boolean isMonthlyInstallment) {
        if (isMonthlyInstallment != null && isMonthlyInstallment)
            return getRepaymentDatesForMonthly(holidays, cutOffDate, samityDay, totalInstallments, paidNoOfInstallments);
        else return getRepaymentDatesForWeekly(holidays, cutOffDate, samityDay, totalInstallments, paidNoOfInstallments);
    }

        public List<LocalDate> getRepaymentDatesForWeekly(List<LocalDate> holidays, LocalDate cutOffDate, DayOfWeek samityDay, int totalInstallments, int paidNoOfInstallments) {
        List<LocalDate> repaymentDates = new LinkedList<>();
        // Generate dates for paid installments
        LocalDate lastPastInstallmentDate = cutOffDate.with(samityDay);
        if (lastPastInstallmentDate.isAfter(cutOffDate))
            lastPastInstallmentDate = lastPastInstallmentDate.minusWeeks(1);
        while (repaymentDates.size() < paidNoOfInstallments) {
            if (!holidays.contains(lastPastInstallmentDate)) {
                repaymentDates.add(0, lastPastInstallmentDate);
            }
            lastPastInstallmentDate = lastPastInstallmentDate.minusWeeks(1);
        }

        // Generate dates for remaining installments after the cut-off date
        LocalDate nextInstallmentDate = cutOffDate.plusDays(1).with(samityDay);
        while (repaymentDates.size() < totalInstallments) {
            if (!holidays.contains(nextInstallmentDate) && !repaymentDates.contains(nextInstallmentDate)) {
                /*if (nextInstallmentDate.getDayOfWeek() == samityDay) {
                    repaymentDates.add(nextInstallmentDate);
                } else {
                    LocalDate tempDate = nextInstallmentDate;
                    while (tempDate.getDayOfWeek() != samityDay) {
                        tempDate = tempDate.plusDays(1);
                    }
                    log.info("TEST | Accepted Temp Date : {}", tempDate);
                    repaymentDates.add(tempDate);
                }*/
                repaymentDates.add(nextInstallmentDate);
            }
            // Move to the next installment date
            nextInstallmentDate = nextInstallmentDate.plusWeeks(1);
        }
        log.info("TEST | Repayment Dates : {}", repaymentDates);
        return repaymentDates;
    }

    private LocalDate getDateForPast(LocalDate lastDate, DayOfWeek samityDay) {
        LocalDate currentDay =  lastDate.minusMonths(1).withDayOfMonth(1);
        while (currentDay.getDayOfWeek() != samityDay) {
            currentDay = currentDay.plusDays(1);
        }
        return currentDay;
    }

    private LocalDate getDateForNextMonth(LocalDate lastDate, DayOfWeek samityDay) {
        LocalDate currentDay =  lastDate.plusMonths(1).withDayOfMonth(1);
        while (currentDay.getDayOfWeek() != samityDay) {
            currentDay = currentDay.plusDays(1);
        }
        return currentDay;
    }

    public List<LocalDate> getRepaymentDatesForMonthly(List<LocalDate> holidays, LocalDate cutOffDate, DayOfWeek samityDay, int totalInstallments, int paidNoOfInstallments) {
        List<LocalDate> repaymentDates = new LinkedList<>();
        // Generate dates for paid installments
        LocalDate lastPastInstallmentDate = cutOffDate.withDayOfMonth(1).with(samityDay);
        log.info("TEST | lastPastInstallmentDate (past) : {}", lastPastInstallmentDate);
        while (repaymentDates.size() < paidNoOfInstallments) {
            if (repaymentDates.contains(lastPastInstallmentDate)) {
                lastPastInstallmentDate = getDateForPast(lastPastInstallmentDate, samityDay);
                log.info("TEST SKIPPED MONTH ALREADY EXIST | lastPastInstallmentDate (past) : {}", lastPastInstallmentDate);
            } else if (!holidays.contains(lastPastInstallmentDate)) {
                repaymentDates.add(0,lastPastInstallmentDate);
                log.info("TEST | Added lastPastInstallmentDate : {}", lastPastInstallmentDate);
                log.info("TEST | Checking lastPastInstallmentDate : {} {}",  lastPastInstallmentDate.minusMonths(1), lastPastInstallmentDate.minusMonths(1).with(samityDay));
                lastPastInstallmentDate = getDateForPast(lastPastInstallmentDate, samityDay);
                log.info("TEST | AGAIN CHECKING lastPastInstallmentDate : {}", lastPastInstallmentDate);
            } else {
                log.info("TEST | Holiday lastPastInstallmentDate : {}", lastPastInstallmentDate);
                lastPastInstallmentDate = lastPastInstallmentDate.plusWeeks(1).with(samityDay);
            }
        }

        // Generate dates for remaining installments after the cut-off date
        LocalDate nextInstallmentDate = cutOffDate.plusDays(1).withDayOfMonth(1).with(samityDay);
        while (repaymentDates.size() < totalInstallments) {
            if (repaymentDates.contains(nextInstallmentDate)) {
                nextInstallmentDate = getDateForNextMonth(nextInstallmentDate, samityDay);
                continue;
            }
            if (!holidays.contains(nextInstallmentDate)) {
                repaymentDates.add(nextInstallmentDate);
                nextInstallmentDate = getDateForNextMonth(nextInstallmentDate, samityDay);
            } else {
                nextInstallmentDate = nextInstallmentDate.plusWeeks(1).with(samityDay);
            }
        }
        return repaymentDates;
    }


    public List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate disburseDate, DayOfWeek samityDay, Integer graceDays, Integer noOfInstallments, String repaymentFrequency, Integer monthlyRepaymentFrequencyDay, Integer loanTermInMonths) {


        log.info("holidays : {}", holidays);
        log.info("disburse date  : {}", disburseDate);
        log.info("samityday  : {}", samityDay);
        log.info("grace days  : {}", graceDays);
        log.info("no of installments : {}", noOfInstallments);
        log.info("payment period : {}", repaymentFrequency);

        monthlyRepaymentFrequencyDay=(monthlyRepaymentFrequencyDay == null || monthlyRepaymentFrequencyDay==0) ? 20 : monthlyRepaymentFrequencyDay;
        /*Set<DayOfWeek> weekends = new HashSet<>();
        Set<LocalDate> customHolidays = new HashSet<>();*/
        /*customHolidays.add(LocalDate.of(2023, 8, 20));
        customHolidays.add(LocalDate.of(2023, 9, 3));*/
        /*weekends.add(DayOfWeek.SATURDAY);*/
        /*weekends.add(DayOfWeek.FRIDAY);*/

        List<LocalDate> samityDays = new LinkedList<>();
        int customHolidayShift = 7;

        LocalDate gracePeriodEndingDate = disburseDate.plusDays(graceDays);
        log.info("gracePeriodEndingDate : {}", gracePeriodEndingDate);
        LocalDate firstInstallmentDate;

        int daysToAdd = 0;
        switch (repaymentFrequency.toUpperCase()) {
            case Constants.REPAYMENT_FREQUENCY_WEEKLY -> daysToAdd = 7;
            case Constants.REPAYMENT_FREQUENCY_MONTHLY -> daysToAdd = 30;
            case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> daysToAdd = 180;
            case Constants.REPAYMENT_FREQUENCY_YEARLY -> daysToAdd = 365;
        }

        DayOfWeek expectedFirstInstallmentDay = gracePeriodEndingDate.getDayOfWeek();

        if (expectedFirstInstallmentDay.equals(samityDay)) {
            firstInstallmentDate = gracePeriodEndingDate;
        } else {
            firstInstallmentDate = gracePeriodEndingDate.with(samityDay);
            if (firstInstallmentDate.isBefore(gracePeriodEndingDate)) {
                firstInstallmentDate = firstInstallmentDate.plusDays(daysToAdd);
            }
        }

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            firstInstallmentDate = gracePeriodEndingDate.plusDays(daysToAdd);
        }



        log.info("Expected First installment Date: {}", firstInstallmentDate);

        /*if (holidays.contains(firstInstallmentDate)) firstInstallmentDate = firstInstallmentDate.plusDays(daysToAdd);*/

        while (holidays.contains(firstInstallmentDate) || firstInstallmentDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            firstInstallmentDate = firstInstallmentDate.plusDays(1);
        }

        log.info("Selected First installment considering Holiday : {}", firstInstallmentDate);

        int count = 0;

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)) {
            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusDays(daysToAdd)) {

                if (date.getDayOfWeek() == samityDay && !holidays.contains(date)) {
                    count++;
                    samityDays.add(date);
                } else if (holidays.contains(date)) {
                    LocalDate dateCoincidedWithHoliday = date;
                    while (holidays.contains(date)) {
                        date = date.plusDays(7);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }
                    count++;
                    samityDays.add(date);
                    log.info("InstallNo : {}, InstallDate : {} coincided with holiday. Hence shifted to : {}", count, dateCoincidedWithHoliday, date);
                }
            }
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_MONTHLY) || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_HALF_YEARLY)) {
            int incrementFactor = 0;
            switch (repaymentFrequency.toUpperCase()) {
                case Constants.REPAYMENT_FREQUENCY_MONTHLY -> incrementFactor = 1;
                case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> incrementFactor = 6;
            }

//            LocalDate probableFirstInstallmentDate = firstInstallmentDate.plusMonths(incrementFactor);
            LocalDate probableFirstInstallmentDate = firstInstallmentDate;
            LocalDate targetFirstInstallmentDate = calculateTargetSamityDay(probableFirstInstallmentDate, monthlyRepaymentFrequencyDay, samityDay);

            log.info("into else if : increment factor : {}", incrementFactor);
            for (LocalDate date = targetFirstInstallmentDate; count < noOfInstallments; date = date.plusMonths(incrementFactor)) {

                if (holidays.contains(date) || date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {

                    LocalDate providedDate = date;
                    date = date.plusDays(1);

                    while (holidays.contains(date)) {
                        date = date.plusDays(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }

                    LocalDate samityDate = calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay);
                    samityDays.add(samityDate);
                    count++;
                    log.info("Installment No : {}, Installment Date : {} coincides with Samity Off Day. Hence rescheduled to : {}", count, providedDate, date);
                } else {
                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                }

            }
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusDays(daysToAdd)) {

                if (date.getDayOfWeek() == samityDay && !holidays.contains(date)) {
                    count++;
                    samityDays.add(date);
                } else if (holidays.contains(date)) {
                    LocalDate dateCoincidedWithHoliday = date;
                    while (holidays.contains(date)) {
                        date = date.plusDays(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }
                    count++;
                    samityDays.add(date);
                    log.info("InstallNo : {}, InstallDate : {} coincided with holiday. Hence shifted to : {}", count, dateCoincidedWithHoliday, date);
                }
            }
        }else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_SINGLE)) {
            LocalDate gracePeriodEndDate = disburseDate.plusDays(graceDays);
            LocalDate singleInstallmentDate = gracePeriodEndDate.plusMonths(loanTermInMonths);
            samityDays.add(singleInstallmentDate);
            return samityDays;
        }

        log.info("samityDays from Service.getRepaymentDates : {}", samityDays);
        return samityDays;

    }

    private static LocalDate calculateTargetSamityDay(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);

        /*if (probableInstallmentDate.getDayOfMonth() <= monthlyRepaymentFrequencyDay) {
            // If probable installment date is on or before the repayment day
            targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);
            log.info("probable install date < frequency day || targetDate = {}", targetDate);
        } else {
            // If probable installment date is after the repayment day
            targetDate = probableInstallmentDate.plusMonths(1).withDayOfMonth(monthlyRepaymentFrequencyDay);
            log.info("probable install date > frequency day || targetDate = {}", targetDate);
        }*/

        // Find the nearest samity day (Sunday) before the target date
        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
            /*if (targetDate.isBefore(probableInstallmentDate))
                targetDate = targetDate.plusMonths(1).withDayOfMonth(monthlyRepaymentFrequencyDay);*/

        }

        return targetDate;
    }

    private static LocalDate calculateTargetSamityDayReverse(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);

        // Find the nearest samity day (Sunday) before the target date
        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.minusDays(1);
        }

        return targetDate;
    }


    public List<RepaymentSchedule> getRepaymentScheduleForFlatInstallment(BigDecimal loanAmount, Integer noOfInstallments, BigDecimal calculatedInstallmentAmount, BigDecimal installmentAmount, List<LocalDate> repaymentDates, BigDecimal principalPerInstallment, BigDecimal serviceChargePerInstallment, BigDecimal adjustedAmount, BigDecimal totalExtraPayment) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        BigDecimal lastInstallmentAmount;
        if (adjustedAmount.doubleValue() > 0) {
            lastInstallmentAmount = installmentAmount.subtract(totalExtraPayment);
        } else if (adjustedAmount.doubleValue() < 0) {
            lastInstallmentAmount = installmentAmount.add(totalExtraPayment);
        } else
            lastInstallmentAmount = installmentAmount;

        // build installment 1 to n-1
        for (int i = 0; i < repaymentDates.size()-1; i++) {
            RepaymentSchedule repayment = RepaymentSchedule
                    .builder()
                    .installNo(i+1)
                    .installDate(repaymentDates.get(i))
                    .dayOfWeek(repaymentDates.get(i).getDayOfWeek().toString())
                    .beginPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                            ? loanAmount
                            : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i))))
                    .scheduledPayment(calculatedInstallmentAmount)
                    .extraPayment(adjustedAmount)
                    .totalPayment(installmentAmount)
                    .principal(principalPerInstallment)
                    .serviceCharge(serviceChargePerInstallment)
                    .endPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                            ? loanAmount.subtract(principalPerInstallment)
                            : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i+1))))
                    .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + (i+1))
                    .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                    .build();
            repaymentScheduleList.add(repayment);
        }

        // build last installment

        repaymentScheduleList
                .add(RepaymentSchedule
                        .builder()
                        .installNo(repaymentDates.size())
                        .installDate(repaymentDates.get(repaymentDates.size() - 1))
                        .dayOfWeek(repaymentDates.get(repaymentDates.size() - 1).getDayOfWeek().toString())
                        .beginPrinBalance(BigDecimal.valueOf(loanAmount.doubleValue() - principalPerInstallment.doubleValue() * (noOfInstallments - 1)))
                        .scheduledPayment(calculatedInstallmentAmount)
                        .extraPayment(adjustedAmount.doubleValue() > 0 ? BigDecimal.valueOf(totalExtraPayment.doubleValue() * (-1)) : totalExtraPayment)
                        .totalPayment(lastInstallmentAmount)
                        .principal(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance())
                        .serviceCharge(lastInstallmentAmount.subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance()))
                        .endPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance().subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance()))
                        .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + repaymentDates.size())
                        .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                        .build());
        return repaymentScheduleList;
    }



    public List<RepaymentSchedule> generateRepaymentScheduleForFlatInstallmentV2(BigDecimal loanAmount, Integer noOfInstallments, BigDecimal installmentAmount, List<LocalDate> repaymentDates, BigDecimal principalPerInstallment, BigDecimal serviceChargePerInstallment, BigDecimal totalRepaymentAmount) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();

        // build installment 1 to n-1
        for (int i = 0; i < repaymentDates.size()-1; i++) {
            RepaymentSchedule repayment = RepaymentSchedule
                    .builder()
                    .installNo(i+1)
                    .installDate(repaymentDates.get(i))
                    .dayOfWeek(repaymentDates.get(i).getDayOfWeek().toString())
                    .beginPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                            ? loanAmount
                            : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i))))
                    .scheduledPayment(installmentAmount)
                    .extraPayment(BigDecimal.ZERO)
                    .totalPayment(installmentAmount)
                    .principal(principalPerInstallment)
                    .serviceCharge(serviceChargePerInstallment)
                    .endPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                            ? loanAmount.subtract(principalPerInstallment)
                            : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i+1))))
                    .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + (i+1))
                    .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                    .build();
            repaymentScheduleList.add(repayment);
        }

        // build last installment

        BigDecimal adjustedInstallmentAmount = totalRepaymentAmount.subtract(installmentAmount.multiply(BigDecimal.valueOf(noOfInstallments)));
        BigDecimal lastInstallmentAmount = installmentAmount.add(adjustedInstallmentAmount);


        repaymentScheduleList
                .add(RepaymentSchedule
                        .builder()
                        .installNo(repaymentDates.size())
                        .installDate(repaymentDates.get(repaymentDates.size() - 1))
                        .dayOfWeek(repaymentDates.get(repaymentDates.size() - 1).getDayOfWeek().toString())
                        .beginPrinBalance(BigDecimal.valueOf(loanAmount.doubleValue() - principalPerInstallment.doubleValue() * (noOfInstallments - 1)))
                        .scheduledPayment(installmentAmount)
                        .extraPayment(lastInstallmentAmount.subtract(installmentAmount))
                        .totalPayment(lastInstallmentAmount)
                        .principal(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance())
                        .serviceCharge(lastInstallmentAmount.subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance()))
                        .endPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance().subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance()))
                        .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + repaymentDates.size())
                        .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                        .build());
        return repaymentScheduleList;
    }

}
