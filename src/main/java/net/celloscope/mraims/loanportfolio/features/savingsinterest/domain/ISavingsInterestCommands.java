package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
public interface ISavingsInterestCommands {
	BigDecimal calculateDailyAccruedInterest(BigDecimal principalBalance, BigDecimal interestRate, String interestRateFrequency, Integer interestRatePrecision, Integer accruedInterestPrecision, RoundingMode roundingMode, String daysInYear, LocalDate interestCalculationDate);
}
