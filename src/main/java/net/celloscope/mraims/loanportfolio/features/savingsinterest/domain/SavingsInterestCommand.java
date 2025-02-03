package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums.DaysInYear;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
@Slf4j
@Component
public class SavingsInterestCommand implements ISavingsInterestCommands {
	@Override
	public BigDecimal calculateDailyAccruedInterest(BigDecimal principalBalance, BigDecimal interestRate, String interestRateFrequency, Integer interestRatePrecision, Integer accruedInterestPrecision, RoundingMode roundingMode, String daysInYear, LocalDate localDate) {
		log.info("interest rate frequency : {}", interestRateFrequency);
		log.info("interest calculation date : {}", localDate);
		Integer daysInCurrentMonth = YearMonth.from(localDate).lengthOfMonth();
		log.info("daysInCurrentMonth: {}", daysInCurrentMonth);
		BigDecimal calculatedDailyInterest = this.calculateDailyAccruedInterestFromDaysInYear(
				principalBalance,
				this.calculateDailyInterestRate(interestRate, interestRateFrequency, interestRatePrecision, roundingMode, daysInYear, localDate),
				accruedInterestPrecision,
				roundingMode,
				daysInCurrentMonth,
				daysInYear
		);
		log.info("calculatedDailyAccruedInterest : {}", calculatedDailyInterest);
		return calculatedDailyInterest;
	}
	
	private Double calculateDailyInterestRate(BigDecimal interestRate, String interestRateFrequency, Integer interestRatePrecision, RoundingMode roundingMode, String daysInYear, LocalDate localDate) {
		BigDecimal actualInterestRate = interestRate.divide(BigDecimal.valueOf(100), 2, roundingMode);
		BigDecimal dailyInterestRate;

		log.info("interest rate : {}", interestRate);
		log.info("actual interest rate : {}", actualInterestRate);
		switch (interestRateFrequency.toUpperCase()) {
			case "DAILY" -> dailyInterestRate = actualInterestRate;
			case "WEEKLY" -> dailyInterestRate = actualInterestRate.divide(BigDecimal.valueOf(7.0), interestRatePrecision, roundingMode);
			case "MONTHLY" -> dailyInterestRate = actualInterestRate.multiply(BigDecimal.valueOf(12.0))
					.divide(BigDecimal.valueOf(getActualDaysFromDaysInYear(daysInYear, localDate)), interestRatePrecision, roundingMode);
			default ->
					dailyInterestRate = actualInterestRate.divide(BigDecimal.valueOf(getActualDaysFromDaysInYear(daysInYear, localDate)), interestRatePrecision, roundingMode);
		};

		log.info("dailyInterestRate : {}", dailyInterestRate);
		return dailyInterestRate.doubleValue();
	}
	
	private Integer getActualDaysFromDaysInYear(String daysInYear, LocalDate localDate) {
		if (daysInYear.equalsIgnoreCase(DaysInYear._365_FIXED.getValue())) {
			return 365;
		} else if (daysInYear.equalsIgnoreCase(DaysInYear._360_DAYS.getValue()) || daysInYear.equalsIgnoreCase(DaysInYear._30_360_GERMAN.getValue())) {
			return 360;
		} else {
			return localDate.getYear() % 4 == 0 && (localDate.getYear() % 100 != 0 || localDate.getYear() % 400 == 0) ? 366 : 365;
		}
	}
	
	private BigDecimal calculateDailyAccruedInterestFromDaysInYear(BigDecimal principalBalance, Double dailyInterestRate, Integer accruedInterestPrecision, RoundingMode roundingMode, Integer daysInCurrentMonth, String daysInYear) {
		
		BigDecimal interest = round(accruedInterestPrecision, principalBalance.multiply(BigDecimal.valueOf(dailyInterestRate)), roundingMode);
		log.info("DailyAccruedInterestFromDaysInYear (rounded) : {}", interest);
		return !daysInYear.equalsIgnoreCase(DaysInYear._30_360_GERMAN.getValue())
				? interest
				: interest.multiply(BigDecimal.valueOf(30.0)).divide(BigDecimal.valueOf(daysInCurrentMonth), accruedInterestPrecision, roundingMode);
	}

	private BigDecimal round(int scale, BigDecimal amount, RoundingMode roundingMode) {
		return amount.setScale(scale, roundingMode);
	}
}
