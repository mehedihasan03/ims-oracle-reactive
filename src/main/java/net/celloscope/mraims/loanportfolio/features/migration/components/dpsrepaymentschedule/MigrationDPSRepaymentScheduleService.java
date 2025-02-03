package net.celloscope.mraims.loanportfolio.features.migration.components.dpsrepaymentschedule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationDPSRepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.DpsRepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.DpsRepaymentSchedule;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationDPSRepaymentScheduleService {

    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final HolidayUseCase holidayUseCase;
    private final DpsRepaymentSchedulePersistencePort port;
    private final ModelMapper modelMapper;
    private final TransactionalOperator rxtx;


    public Mono<DpsRepaymentScheduleResponseDTO> generateDpsRepaymentScheduleMigration(MigrationDPSRepaymentScheduleCommand command) {
        AtomicReference<LocalDate> acct_end_date = new AtomicReference<>();
        return validateCommand(command)
                .flatMap(migrationDPSRepaymentScheduleCommand -> savingsAccountUseCase
                        .getDPSAccountDetailsBySavingsAccountId(command.getSavingsAccountId()))
                .flatMap(dpsAccountDTO -> buildDPSRepaymentScheduleAndInterestPostingDates(dpsAccountDTO, command))
                .flatMap(tuple2 -> {
                    log.info("DPS Interest Posting Dates : {}", tuple2.getT2());
                    log.info("acct_end_date : {}", tuple2.getT2().get(tuple2.getT2().size() - 1));
                    acct_end_date.set(tuple2.getT2().get(tuple2.getT2().size() - 1));
                    return port
                            .getDPSRepaymentScheduleBySavingsAccountId(command.getSavingsAccountId())
                            .collectList()
                            .filter(List::isEmpty)
                            .flatMap(dpsRepaymentScheduleList ->  port.saveRepaymentSchedule(tuple2.getT1().stream()
                                    .map(dpsRepaymentDTO -> modelMapper.map(dpsRepaymentDTO, DpsRepaymentSchedule.class)).toList()))
                            .doOnError(throwable -> log.error("Error occurred while saving DPS Repayment Schedule : {}", throwable.getMessage()))
                            .flatMap(dpsRepaymentSchedules -> savingsAccountUseCase
                                    .updateSavingsAccountInterestPostingDatesAndStartDateEndDate(tuple2.getT2(), command.getSavingsAccountId(), getAccountStartDate(tuple2), tuple2.getT2().get(tuple2.getT2().size() - 1), command.getLoginId())
                                    .doOnError(throwable -> log.error("Error occurred while updating DPS Interest Posting Dates : {}", throwable.getMessage()))
                                    .map(savingsAccountResponseDTO -> dpsRepaymentSchedules));
                })
                .as(this.rxtx::transactional)
                .flatMapMany(Flux::fromIterable)
                .map(dpsRepaymentSchedule -> modelMapper.map(dpsRepaymentSchedule, DpsRepaymentDTO.class))
                .sort(Comparator.comparing(DpsRepaymentDTO::getRepaymentNo))
                .collectList()
                .map(dpsRepaymentScheduleList ->
                        DpsRepaymentScheduleResponseDTO
                                .builder()
                                .repaymentResponseList(dpsRepaymentScheduleList)
                                .acctEndDate(acct_end_date.get())
                                .build());
    }

    private LocalDate getAccountStartDate(Tuple2<List<DpsRepaymentDTO>, List<LocalDate>> tuple2) {
        return tuple2.getT1().stream()
            .filter(dpsRepaymentDTO -> dpsRepaymentDTO.getRepaymentNo().equals(1))
            .findFirst()
            .orElseThrow()
            .getRepaymentDate();
    }

    public Mono<Tuple2<List<DpsRepaymentDTO>, List<LocalDate>>> buildDPSRepaymentScheduleAndInterestPostingDates(DPSAccountDTO dpsAccountDTO, MigrationDPSRepaymentScheduleCommand command) {
        dpsAccountDTO.setDepositEvery(dpsAccountDTO.getDepositEvery() == null ? "Monthly" : dpsAccountDTO.getDepositEvery());
        dpsAccountDTO.setDepositTerm(dpsAccountDTO.getDepositTerm() == null ? 12 : dpsAccountDTO.getDepositTerm());
        dpsAccountDTO.setDepositTermPeriod(dpsAccountDTO.getDepositTermPeriod() == null ? "Month" : dpsAccountDTO.getDepositTermPeriod());
        dpsAccountDTO.setInterestPostingPeriod(dpsAccountDTO.getInterestPostingPeriod() == null ? "Yearly" : dpsAccountDTO.getInterestPostingPeriod());
        command.setMonthlyRepaymentFrequencyDay(command.getMonthlyRepaymentFrequencyDay() == null ? 1 : command.getMonthlyRepaymentFrequencyDay());

        Integer lengthInMonths = getDPSLengthInMonths(dpsAccountDTO);
        Integer lengthInWeeks = getDPSLengthInWeeks(dpsAccountDTO);
        Integer noOfInstallments = dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY")
                ? lengthInWeeks
                : lengthInMonths;

        return holidayUseCase
                .getAllHolidaysOfASamityBySavingsAccountId(command.getSavingsAccountId())
                .switchIfEmpty(Flux.just(HolidayResponseDTO.builder().holidayDate(LocalDate.of(2023, 12,16)).build()))
                .doOnRequest(l -> log.info("Request received to fetch holidays"))
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                .flatMap(holidays -> Mono.just(this.getRepaymentDatesForMonthlyDPSMigration(holidays, command.getCutOffDate(), DayOfWeek.valueOf(dpsAccountDTO.getSamityDay().toUpperCase()), noOfInstallments, command.getNoOfPaidInstallments(), command.getMonthlyRepaymentFrequencyDay()))
                        .flatMap(installmentDates -> Mono.just(buildRepaymentDTOList(installmentDates, dpsAccountDTO))
                                .map(dpsRepaymentDTOS -> {
                                    List<LocalDate> interestPostingDates = getDPSInterestPostingDates(dpsRepaymentDTOS, dpsAccountDTO, holidays);
                                    return Tuples.of(
                                            dpsRepaymentDTOS.stream().takeWhile(dpsRepaymentDTO -> dpsRepaymentDTO.getRepaymentNo() <= noOfInstallments).toList(),
                                            interestPostingDates);
                                })));
    }


    private Mono<MigrationDPSRepaymentScheduleCommand> validateCommand(MigrationDPSRepaymentScheduleCommand migrationDPSRepaymentScheduleCommand) {
        if (migrationDPSRepaymentScheduleCommand.getCutOffDate() == null) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Cut Off Date is required"));
        } else if (migrationDPSRepaymentScheduleCommand.getNoOfPaidInstallments() == null) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No of Paid Installments is required"));
        } else if (migrationDPSRepaymentScheduleCommand.getSavingsAccountId() == null) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account Id is required"));
        } else
            return Mono.just(migrationDPSRepaymentScheduleCommand);
    }

    private Integer getDPSLengthInMonths(DPSAccountDTO dpsAccountDTO) {
        Integer lengthInMonths = 0;
        if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("MONTH")) {
            lengthInMonths = dpsAccountDTO.getDepositTerm();
        } else if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("YEAR")) {
            lengthInMonths = dpsAccountDTO.getDepositTerm() * 12 ;
        }
        return lengthInMonths;
    }

    private Integer getDPSLengthInWeeks(DPSAccountDTO dpsAccountDTO) {
        long lenghtInWeeks = dpsAccountDTO.getDepositTerm();
        if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("MONTH")) {
            lenghtInWeeks = Math.round((52.0/12) * dpsAccountDTO.getDepositTerm());
        } else if (dpsAccountDTO.getDepositTermPeriod().equalsIgnoreCase("YEAR")) {
            lenghtInWeeks = dpsAccountDTO.getDepositTerm() * 52 ;
        }
        return (int) lenghtInWeeks;
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

    public List<LocalDate> getRepaymentDatesForMonthlyDPSMigration(List<LocalDate> holidays, LocalDate cutOffDate, DayOfWeek samityDay, int totalInstallments, int paidNoOfInstallments, Integer monthlyRepaymentFrequencyDay) {
        List<LocalDate> repaymentDates = new ArrayList<>();

        // Generate dates for paid installments (reversed order)
        for (int i = paidNoOfInstallments; i >= 1; i--) {
            LocalDate pastInstallmentDate = cutOffDate.with(samityDay).minusMonths(i);
            repaymentDates.add(pastInstallmentDate);
        }

        // Generate dates for remaining installments (starting from next month)
        LocalDate nextInstallmentDate = cutOffDate.plusDays(1);
        while (repaymentDates.size() < totalInstallments) {
            nextInstallmentDate = calculateTargetSamityDay(nextInstallmentDate, monthlyRepaymentFrequencyDay, samityDay);
            if (!holidays.contains(nextInstallmentDate )) {
                repaymentDates.add(nextInstallmentDate);
                nextInstallmentDate = nextInstallmentDate.plusMonths(1);
            } else {
                nextInstallmentDate = nextInstallmentDate.with(samityDay);
                while (holidays.contains(nextInstallmentDate)) {
                    nextInstallmentDate = nextInstallmentDate.plusWeeks(1);
                }
                repaymentDates.add(nextInstallmentDate);
                nextInstallmentDate = nextInstallmentDate.plusMonths(1);
            }

        }

        return repaymentDates;
    }

    private static LocalDate calculateTargetSamityDay(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        monthlyRepaymentFrequencyDay = monthlyRepaymentFrequencyDay <= 0 ? 1 : monthlyRepaymentFrequencyDay;
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);
        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
        }
        return targetDate;
    }


    private List<LocalDate> getDPSInterestPostingDates(List<DpsRepaymentDTO> dpsRepaymentDTOS, DPSAccountDTO dpsAccountDTO, List<LocalDate> holidays) {
        List<LocalDate> repaymentDates = dpsRepaymentDTOS.stream().map(DpsRepaymentDTO::getRepaymentDate).toList();
        LocalDate acct_end_date = null;

        /*dpsAccountDTO.setDepositEvery(dpsAccountDTO.getDepositEvery() == null ? "Monthly" : dpsAccountDTO.getDepositEvery());
        dpsAccountDTO.setDepositTerm(dpsAccountDTO.getDepositTerm() == null ? 12 : dpsAccountDTO.getDepositTerm());
        dpsAccountDTO.setDepositTermPeriod(dpsAccountDTO.getDepositTermPeriod() == null ? "Month" : dpsAccountDTO.getDepositTermPeriod());
        dpsAccountDTO.setInterestPostingPeriod(dpsAccountDTO.getInterestPostingPeriod() == null ? "Yearly" : dpsAccountDTO.getInterestPostingPeriod());*/

        if (dpsAccountDTO.getDepositEvery().equalsIgnoreCase("MONTHLY")) {
//            acct_end_date = firstInstallmentDate.plusMonths(lengthInMonths);
            acct_end_date = repaymentDates.get(repaymentDates.size() - 1).plusMonths(1);
        } else if (dpsAccountDTO.getDepositEvery().equalsIgnoreCase("WEEKLY")){
//            acct_end_date = firstInstallmentDate.plusWeeks(lengthInWeeks);
            acct_end_date = repaymentDates.get(repaymentDates.size() - 1).plusWeeks(1);
        }

        log.info("Acct End Date : {}", acct_end_date);

        while (holidays.contains(acct_end_date) && !acct_end_date.getDayOfWeek().equals(dpsAccountDTO.getSamityDay().toUpperCase())) {
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

}
