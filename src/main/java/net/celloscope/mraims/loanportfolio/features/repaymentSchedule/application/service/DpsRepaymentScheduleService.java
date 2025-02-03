package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.DpsRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.DPSRepaymentCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.DpsRepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.DpsRepaymentSchedule;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class DpsRepaymentScheduleService implements DpsRepaymentScheduleUseCase {
    private final DpsRepaymentSchedulePersistencePort port;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final HolidayUseCase holidayUseCase;
    private final ModelMapper modelMapper;

    private final TransactionalOperator rxtx;

    public DpsRepaymentScheduleService(DpsRepaymentSchedulePersistencePort port, ISavingsAccountUseCase savingsAccountUseCase, HolidayUseCase holidayUseCase, ModelMapper modelMapper, TransactionalOperator rxtx) {
        this.port = port;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.holidayUseCase = holidayUseCase;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
    }

    @Override
    public Mono<DpsRepaymentScheduleResponseDTO> generateDpsRepaymentSchedule(DPSRepaymentCommand command) {
        AtomicReference<LocalDate> acct_end_date = new AtomicReference<>();
        return port.getDPSRepaymentScheduleBySavingsAccountId(command.getSavingsAccountId())
                .collectList()
                .flatMap(dpsRepaymentScheduleList -> {
                    if (dpsRepaymentScheduleList.isEmpty()) {
                        return savingsAccountUseCase
                                .getDPSAccountDetailsBySavingsAccountId(command.getSavingsAccountId())
                                .flatMap(dpsAccountDTO -> buildDPSRepaymentScheduleAndInterestPostingDates(dpsAccountDTO, command))
                                .flatMap(tuple2 -> {
                                    log.info("DPS Interest Posting Dates : {}", tuple2.getT2());
                                    log.info("acct_end_date : {}", tuple2.getT2().get(tuple2.getT2().size() - 1));
                                    acct_end_date.set(tuple2.getT2().get(tuple2.getT2().size() - 1));
                                    return port
                                            .getDPSRepaymentScheduleBySavingsAccountId(command.getSavingsAccountId())
                                            .collectList()
                                            .filter(List::isEmpty)
                                            /*.flatMap(dpsRepaymentSchedule -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Repayment Schedule already exists for Savings Account ID : " + command.getSavingsAccountId())))*/
                                            .flatMap(dpsRepaymentSchedules ->  port.saveRepaymentSchedule(tuple2.getT1().stream()
                                                    .map(dpsRepaymentDTO -> modelMapper.map(dpsRepaymentDTO, DpsRepaymentSchedule.class)).toList()))
                                            .doOnError(throwable -> log.error("Error occurred while saving DPS Repayment Schedule : {}", throwable.getMessage()))
                                            .flatMap(dpsRepaymentSchedules -> savingsAccountUseCase
                                                    .updateSavingsAccountInterestPostingDatesAndStartDateEndDate(tuple2.getT2(), command.getSavingsAccountId(), command.getFirstInstallmentDate(), tuple2.getT2().get(tuple2.getT2().size() - 1), command.getLoginId())
                                                    .doOnError(throwable -> log.error("Error occurred while updating DPS Interest Posting Dates : {}", throwable.getMessage()))
                                                    .map(savingsAccountResponseDTO -> dpsRepaymentSchedules));
                                })
                                .as(this.rxtx::transactional)
                                .flatMapMany(Flux::fromIterable)
                                .map(dpsRepaymentSchedule -> modelMapper.map(dpsRepaymentSchedule, DpsRepaymentDTO.class))
                                .sort(Comparator.comparing(DpsRepaymentDTO::getRepaymentNo))
                                .collectList()
                                .map(dpsRepaymentScheduleListDpsRepaymentDTOS ->
                                        DpsRepaymentScheduleResponseDTO
                                                .builder()
                                                .repaymentResponseList(dpsRepaymentScheduleListDpsRepaymentDTOS)
                                                .acctEndDate(acct_end_date.get())
                                                .build());
                    }
                    return Mono.just(dpsRepaymentScheduleList.stream().map(dpsRepaymentSchedule -> modelMapper.map(dpsRepaymentSchedule, DpsRepaymentDTO.class)).toList())
                            .map(dpsRepaymentScheduleListDpsRepaymentDTOS ->
                                    DpsRepaymentScheduleResponseDTO
                                            .builder()
                                            .repaymentResponseList(dpsRepaymentScheduleListDpsRepaymentDTOS)
                                            .acctEndDate(dpsRepaymentScheduleListDpsRepaymentDTOS.get(dpsRepaymentScheduleListDpsRepaymentDTOS.size() - 1).getRepaymentDate().plusMonths(1))
                                            .build());
                });

    }

    @Override
    public Mono<List<DpsRepaymentDTO>> getDpsRepaymentScheduleBySavingsAccountId(String savingsAccountId) {
        return port.getDPSRepaymentScheduleBySavingsAccountId(savingsAccountId)
                .map(dpsRepaymentSchedule -> modelMapper.map(dpsRepaymentSchedule, DpsRepaymentDTO.class))
                .collectList();
    }

    @Override
    public Mono<Boolean> updateDPSRepaymentScheduleStatus(String savingsAccountId, String status, List<Integer> paidRepaymentNos, String managementProcessId, LocalDate businessDate, String loginId) {
        return port.updateDPSRepaymentScheduleStatus(savingsAccountId, status, paidRepaymentNos, managementProcessId, businessDate, loginId)
                .doOnRequest(l -> log.info("Request received to update DPS Repayment Schedule Status"))
                .doOnSuccess(aBoolean -> log.info("DPS Repayment Schedule Status updated successfully"))
                .doOnError(throwable -> log.error("Error occurred while updating DPS Repayment Schedule Status : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Integer> getCountOfPendingRepaymentScheduleBySavingsAccountId(String savingsAccountId) {
        return port.countPendingDpsRepaymentScheduleBySavingsAccountId(savingsAccountId)
                .flatMap(noOfInstallment -> noOfInstallment.equals(0) ? getNoOfInstallmentsForDps(savingsAccountId) : Mono.just(noOfInstallment))
                .doOnRequest(l -> log.info("Request received to count pending DPS Repayment Schedule"))
                .doOnSuccess(count -> log.info("Count of pending DPS Repayment Schedule : {}", count))
                .doOnError(throwable -> log.error("Error occurred while counting pending DPS Repayment Schedule : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> updateDPSRepaymentScheduleStatusByManagementProcessId(String managementProcessId, String status, String loginId) {
        return port.updateDPSRepaymentScheduleStatusByManagementProcessId(managementProcessId, status, loginId)
                .collectList()
                .map(dpsRepaymentScheduleEntities -> true)
                .doOnRequest(l -> log.info("Request received to update DPS Repayment Schedule Status by Management Process ID"))
                .doOnSuccess(aBoolean -> log.info("DPS Repayment Schedule Status updated successfully by Management Process ID"))
                .doOnError(throwable -> log.error("Error occurred while updating DPS Repayment Schedule Status by Management Process ID : {}", throwable.getMessage()));
    }


    private Mono<Integer> getNoOfInstallmentsForDps(String savingsAccountId) {
        return savingsAccountUseCase
                .getDPSAccountDetailsBySavingsAccountId(savingsAccountId)
                .doOnNext(dpsAccountDTO -> log.info("DPS Account Details : {}", dpsAccountDTO))
                .map(dpsAccountDTO -> {
                    Integer lengthInMonths = getDPSLengthInMonths(dpsAccountDTO);
                    Integer lengthInWeeks = getDPSLengthInWeeks(dpsAccountDTO);
                    return dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY")
                            ? lengthInWeeks
                            : lengthInMonths;
                })
                .doOnSuccess(count -> log.info("No of Installments for DPS : {}", count));
    }


    private DpsRepaymentSchedule buildDpsRepaymentScheduleToPersist(DpsRepaymentDTO dpsRepaymentDTO) {
        /*return DpsRepaymentSchedule.builder().build();*/
        /*
        *
        *
        * */

        return modelMapper.map(dpsRepaymentDTO, DpsRepaymentSchedule.class);
    }

    private Mono<Tuple2<List<DpsRepaymentDTO>, List<LocalDate>>> buildDPSRepaymentScheduleAndInterestPostingDates(DPSAccountDTO dpsAccountDTO, DPSRepaymentCommand command) {
        Integer lengthInMonths = getDPSLengthInMonths(dpsAccountDTO);
        Integer lengthInWeeks = getDPSLengthInWeeks(dpsAccountDTO);
        Integer noOfInstallments = dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY")
                                    ? lengthInWeeks
                                    : lengthInMonths;

        int monthlyRepayDay = dpsAccountDTO.getMonthlyRepayDay() == null || dpsAccountDTO.getMonthlyRepayDay() == 0 ? 20 : dpsAccountDTO.getMonthlyRepayDay();

        return holidayUseCase
                .getAllHolidaysOfASamityBySavingsAccountId(command.getSavingsAccountId())
                .doOnRequest(l -> log.info("Request received to fetch holidays"))
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                .flatMap(holidays -> Mono.just(this.getRepaymentDates(holidays, command.getFirstInstallmentDate(), DayOfWeek.valueOf(dpsAccountDTO.getSamityDay().toUpperCase()), noOfInstallments, dpsAccountDTO.getDepositEvery(),monthlyRepayDay))
                .flatMap(installmentDates -> Mono.just(buildRepaymentDTOList(installmentDates, dpsAccountDTO))
                    .map(dpsRepaymentDTOS -> {
                    List<LocalDate> interestPostingDates = getDPSInterestPostingDates(dpsRepaymentDTOS, dpsAccountDTO, holidays, command.getFirstInstallmentDate());
                    return Tuples.of(
                            dpsRepaymentDTOS.stream().takeWhile(dpsRepaymentDTO -> dpsRepaymentDTO.getRepaymentNo() <= noOfInstallments).toList(),
                            interestPostingDates);
                })));
    }

    private List<LocalDate> getDPSInterestPostingDates(List<DpsRepaymentDTO> dpsRepaymentDTOS, DPSAccountDTO dpsAccountDTO, List<LocalDate> holidays, LocalDate firstInstallmentDate) {
        List<LocalDate> repaymentDates = dpsRepaymentDTOS.stream().map(DpsRepaymentDTO::getRepaymentDate).toList();
        LocalDate acct_end_date = null;

        Integer lengthInMonths = getDPSLengthInMonths(dpsAccountDTO);
        Integer lengthInWeeks = getDPSLengthInWeeks(dpsAccountDTO);

        if (dpsAccountDTO.getDepositEvery().equalsIgnoreCase("MONTHLY")) {
            acct_end_date = firstInstallmentDate.plusMonths(lengthInMonths);
        } else if (dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY")){
//            acct_end_date = firstInstallmentDate.plusWeeks(lengthInWeeks);
            acct_end_date = repaymentDates.get(repaymentDates.size() - 1).plusWeeks(1);
        }

        log.info("Acct End Date : {}", acct_end_date);

        while (holidays.contains(acct_end_date)) {
            assert acct_end_date != null;
            acct_end_date = acct_end_date.plusDays(1);
            log.info("Acct End Date : {} is a holiday. Hence shifting to next day.", acct_end_date);
        }


        // for monthly dps
        List<LocalDate> interestPostingDates = new ArrayList<>();
        if (dpsAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("MONTHLY")) {
            interestPostingDates = repaymentDates
                    .stream().skip(1).toList();
        } else if (dpsAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("QUARTERLY")) {
            interestPostingDates = IntStream.range(0, repaymentDates.size())
                    .filter(i -> i % 3 == 0 && i != 0)
                    .mapToObj(repaymentDates::get)
                    .collect(Collectors.toList());
        } else if (dpsAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("HALF_YEARLY")) {
            interestPostingDates = IntStream.range(0, repaymentDates.size())
                    .filter(i -> i % 6 == 0 && i != 0)
                    .mapToObj(repaymentDates::get)
                    .collect(Collectors.toList());
        } else if (dpsAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("YEARLY")) {
            interestPostingDates = IntStream.range(0, repaymentDates.size())
                    .filter(i -> i % 12 == 0 && i != 0)
                    .mapToObj(repaymentDates::get)
                    .collect(Collectors.toList());
        }

        List<LocalDate> updatedInterestPostingDates = new ArrayList<>(interestPostingDates);

        // for weekly dps
        if (dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY") && dpsAccountDTO.getInterestPostingPeriod().equalsIgnoreCase("Yearly")) {
            updatedInterestPostingDates = new ArrayList<>();
        }
        updatedInterestPostingDates.add(acct_end_date);

        return updatedInterestPostingDates;
    }

    private List<DpsRepaymentDTO> buildRepaymentDTOList(List<LocalDate> installmentDates, DPSAccountDTO dpsAccountDTO) {
        List<DpsRepaymentDTO> list = new ArrayList<>();
        installmentDates.sort(Comparator.comparing(LocalDate::getYear)
                .thenComparing(LocalDate::getDayOfYear));


        log.info("installment Dates : {}", installmentDates);

        for (int i = 0; i < installmentDates.size(); i++) {
            list.add(DpsRepaymentDTO
                    .builder()
                    .savingsAccountId(dpsAccountDTO.getSavingsAccountId())
                    .savingsAccountOid(dpsAccountDTO.getSavingsAccountOid())
                    .memberId(dpsAccountDTO.getMemberId())
                    .samityId(dpsAccountDTO.getSamityId())
                    .repaymentNo(i+1)
                    .repaymentDate(installmentDates.get(i))
                    .dayOfWeek(String.valueOf(DayOfWeek.from(installmentDates.get(i))))
                    .repaymentAmount(dpsAccountDTO.getSavingsAmount())
                    /*.status(i == 0 ? Status.STATUS_PAID.getValue() : Status.STATUS_PENDING.getValue())*/
                    .status(Status.STATUS_PENDING.getValue())
                    .build());
        }

        return list;

    }

    private Integer getDPSLengthInMonths(DPSAccountDTO dpsAccountDTO) {
        Integer lengthInMonths = 0;
        if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("MONTH")) {
            lengthInMonths = dpsAccountDTO.getDepositTerm();
        } else if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("YEAR")) {
            lengthInMonths = dpsAccountDTO.getDepositTerm() * 12 ;
        }
        log.info("Length in Months : {}", lengthInMonths);
        return lengthInMonths;
    }

    private Integer getDPSLengthInWeeks(DPSAccountDTO dpsAccountDTO) {
        long lenghtInWeeks = dpsAccountDTO.getDepositTerm();
        if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("MONTH")) {
            lenghtInWeeks = Math.round((52.0/12) * dpsAccountDTO.getDepositTerm());
        } else if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("YEAR")) {
            lenghtInWeeks = dpsAccountDTO.getDepositTerm() * 52 ;
        }
        log.info("Length in Weeks : {}", lenghtInWeeks);
        return (int) lenghtInWeeks;
    }


    public List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate firstInstallmentDate, DayOfWeek samityDay, Integer noOfInstallments, String depositEvery, Integer monthlyRepaymentFrequencyDay) {

        List<LocalDate> samityDays = new LinkedList<>();

        int daysToAdd = 0;
        switch (depositEvery.toUpperCase()) {
            case Constants.REPAYMENT_FREQUENCY_WEEKLY -> daysToAdd = 7;
            case Constants.REPAYMENT_FREQUENCY_MONTHLY -> daysToAdd = 30;
        }

        int count = 0;
        log.info("Repayment Frequency : {}", depositEvery);
        log.info("First Installment Date : {}", firstInstallmentDate);
        log.info("No of Installments : {}", noOfInstallments);
        log.info("Samity Day : {}", samityDay);
        log.info("daysToAdd {}", daysToAdd);

        if (depositEvery.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)) {

            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusWeeks(1)) {
                if (date == firstInstallmentDate) {
                    samityDays.add(firstInstallmentDate);
                    count++;
                }
                else if (holidays.contains(date) || date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                    LocalDate providedDate = date;
                    date = date.plusWeeks(1);

                    while (holidays.contains(date)) {
                        date = date.plusWeeks(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }

                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                    log.info("Installment No : {}, Installment Date : {} coincides with Samity Off Day. Hence rescheduled to : {}",count, providedDate, date);
                } else {
                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                }
            }
        } else if (depositEvery.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_MONTHLY)) {
            int incrementFactor = 1;

            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusMonths(incrementFactor)) {
                if (date == firstInstallmentDate) {
                    samityDays.add(firstInstallmentDate);
                    count++;
                }
                else if (holidays.contains(date) || date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                    LocalDate providedDate = date;
                    date = date.plusDays(1);

                    while (holidays.contains(date)) {
                        date = date.plusDays(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }

                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                    log.info("Installment No : {}, Installment Date : {} coincides with Samity Off Day. Hence rescheduled to : {}",count, providedDate, date);
                } else {
                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                }
            }
        }
        log.info("samityDays from Service.getRepaymentDates : {}", samityDays);
        return samityDays;

    }

    private static LocalDate calculateTargetSamityDay(LocalDate probableInstallmentDate, Integer monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate;
        if (monthlyRepaymentFrequencyDay != null) {
             targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);
        } else
            targetDate = probableInstallmentDate;

        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
        }

        return targetDate;
    }
}
