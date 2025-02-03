package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.commands;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface IRepaymentDatesCommands {
    List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate disburseDate, DayOfWeek samityDay, Integer graceDays, Integer noOfInstallments, String repaymentFrequency, Integer monthlyRepaymentFrequencyDay, Integer loanTerm);
}
