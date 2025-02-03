package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.commands;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
@Component
@Slf4j
public class RepaymentDatesCommands implements IRepaymentDatesCommands{

    @Override
    public List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate disburseDate, DayOfWeek samityDay, Integer graceDays, Integer noOfInstallments, String repaymentFrequency, Integer monthlyRepaymentFrequencyDay, Integer loanTermInMonths) {


        log.info("holidays : {}", holidays);
        log.info("disburse date  : {}", disburseDate);
        log.info("samityday  : {}", samityDay);
        log.info("grace days  : {}", graceDays);
        log.info("no of installments : {}", noOfInstallments);
        log.info("payment period : {}", repaymentFrequency);


        List<LocalDate> samityDays = new LinkedList<>();
        monthlyRepaymentFrequencyDay =  monthlyRepaymentFrequencyDay > 1 ? monthlyRepaymentFrequencyDay : 1;

        LocalDate firstInstallmentDate = this.getFirstInstallmentDate(holidays, disburseDate, graceDays, repaymentFrequency, samityDay);

        log.info("Selected First installment considering Holiday : {}", firstInstallmentDate);

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)
            || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_FORTNIGHTLY)) {
            int incrementFactor = 0;
            switch (repaymentFrequency.toUpperCase()) {
                case Constants.REPAYMENT_FREQUENCY_WEEKLY -> incrementFactor = 7;
                case Constants.REPAYMENT_FREQUENCY_FORTNIGHTLY -> incrementFactor = 14;
            }
            this.getSamityDaysForWeeklyPayments(samityDays, holidays, firstInstallmentDate, noOfInstallments, samityDay, incrementFactor);

        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_MONTHLY)
                || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_BIMONTHLY)
                || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_QUARTERLY)
                || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_FOUR_MONTHLY)
                || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_HALF_YEARLY)) {
            int incrementFactor = getIncrementFactor(repaymentFrequency);

            LocalDate targetFirstInstallmentDate = calculateTargetSamityDayForFirstInstallment(firstInstallmentDate, monthlyRepaymentFrequencyDay, samityDay);
            this.getSamityDaysForMonthlyRepayments(samityDays, holidays, targetFirstInstallmentDate, noOfInstallments, samityDay, incrementFactor, monthlyRepaymentFrequencyDay);

        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            this.getSamityDaysForYearlyPayments(holidays, firstInstallmentDate, noOfInstallments, samityDay, samityDays);
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_SINGLE)) {
            LocalDate gracePeriodEndDate = disburseDate.plusDays(graceDays);
            LocalDate singleInstallmentDate = gracePeriodEndDate.plusMonths(loanTermInMonths);
            samityDays.add(singleInstallmentDate);
            return samityDays;
        }

        log.info("samityDays from RepaymentDatesCommands.getRepaymentDates : {}", samityDays);
        return samityDays;

    }

    private static int getIncrementFactor(String repaymentFrequency) {
        int incrementFactor = 0;
        switch (repaymentFrequency.toUpperCase()) {
            case Constants.REPAYMENT_FREQUENCY_MONTHLY -> incrementFactor = 1;
            case Constants.REPAYMENT_FREQUENCY_BIMONTHLY -> incrementFactor = 2;
            case Constants.REPAYMENT_FREQUENCY_QUARTERLY -> incrementFactor = 3;
            case Constants.REPAYMENT_FREQUENCY_FOUR_MONTHLY -> incrementFactor = 4;
            case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> incrementFactor = 6;
        }
        return incrementFactor;
    }


    private LocalDate getFirstInstallmentDate(List<LocalDate> holidays, LocalDate disburseDate, Integer graceDays, String repaymentFrequency, DayOfWeek samityDay) {
        LocalDate gracePeriodEndingDate = disburseDate.plusDays(graceDays);
        log.info("gracePeriodEndingDate : {}", gracePeriodEndingDate);
        LocalDate firstInstallmentDate;

        DayOfWeek expectedFirstInstallmentDay = gracePeriodEndingDate.getDayOfWeek();
        log.info("expectedFirstInstallmentDay : {}", expectedFirstInstallmentDay);
        log.info("samityDay : {}", samityDay);

        if (expectedFirstInstallmentDay.equals(samityDay)) {
            firstInstallmentDate = gracePeriodEndingDate;
        } else {
            firstInstallmentDate = gracePeriodEndingDate.with(samityDay);
            while (firstInstallmentDate.isBefore(gracePeriodEndingDate) || !firstInstallmentDate.getDayOfWeek().equals(samityDay)) {
                firstInstallmentDate = firstInstallmentDate.plusDays(1);
                if (firstInstallmentDate.getDayOfWeek().equals(samityDay)) {
                    break;
                }
            }
            log.info("firstInstallmentDate after while loop : {}", firstInstallmentDate);
        }

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            firstInstallmentDate = gracePeriodEndingDate.plusYears(1);
        }

        while (holidays.contains(firstInstallmentDate) || firstInstallmentDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            firstInstallmentDate = firstInstallmentDate.plusDays(7);
            if (!holidays.contains(firstInstallmentDate))
                break;
        }

        log.info("firstInstallmentDate FINAL : {}", firstInstallmentDate);

        return firstInstallmentDate;
    }


    private List<LocalDate> getSamityDaysForYearlyPayments(List<LocalDate> holidays, LocalDate firstInstallmentDate, Integer noOfInstallments, DayOfWeek samityDay, List<LocalDate> samityDays) {
        int count = 0;
        log.info("first installment date ");
        for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusYears(1)) {
            if (holidays.contains(date)) {
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
            } else {
                count++;
                samityDays.add(date);
            }
            count++;
        }
        return samityDays;
    }


    private List<LocalDate> getSamityDaysForWeeklyPayments(List<LocalDate> samityDays, List<LocalDate> holidays, LocalDate firstInstallmentDate, Integer noOfInstallments, DayOfWeek samityDay, Integer daysToAdd) {
        int count = 0;

        for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusDays(daysToAdd)) {

            if (date.getDayOfWeek().equals(samityDay) && !holidays.contains(date)) {
                count++;
                samityDays.add(date);
            } else if (holidays.contains(date)) {
                LocalDate dateCoincidedWithHoliday = date;
                log.info("dateCoIncided with holiday : {}", dateCoincidedWithHoliday);
                while (holidays.contains(date)) {
                    log.info("holidays contain : {}", date);
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

        log.info("samityDays for weekly payments : {}", samityDays);

        return samityDays;
    }


    private List<LocalDate> getSamityDaysForMonthlyRepayments(List<LocalDate> samityDays, List<LocalDate> holidays, LocalDate firstInstallmentDate, Integer noOfInstallments, DayOfWeek samityDay, Integer incrementFactor, Integer monthlyRepaymentFrequencyDay) {

        samityDays.add(firstInstallmentDate);
        int count = 1;
        for (LocalDate date = firstInstallmentDate.plusMonths(incrementFactor); count < noOfInstallments; date = date.plusMonths(incrementFactor)) {

            if (holidays.contains(date) || date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
                LocalDate providedDate = date;
                date = date.plusDays(1);

                while (holidays.contains(date)) {
                    date = date.plusDays(1);
                }

                LocalDate samityDate = calculateTargetSamityDayForRestInstallments(date, monthlyRepaymentFrequencyDay, samityDay);
                samityDays.add(samityDate);
                count++;
                log.info("Installment No : {}, Installment Date : {} coincides with Samity Off Day. Hence rescheduled to : {}", count, providedDate, date);
            } else {
                samityDays.add(calculateTargetSamityDayForRestInstallments(date, monthlyRepaymentFrequencyDay, samityDay));
                count++;
            }
        }

        return samityDays;
    }

    private static LocalDate calculateTargetSamityDayForFirstInstallment(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);
        if (targetDate.isBefore(probableInstallmentDate)) {
            targetDate = targetDate.plusMonths(1);
        }

        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
        }

        return targetDate;
    }

    private static LocalDate calculateTargetSamityDayForRestInstallments(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);

        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
        }

        return targetDate;
    }


}
