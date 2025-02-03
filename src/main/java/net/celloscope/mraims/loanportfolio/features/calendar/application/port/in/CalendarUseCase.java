package net.celloscope.mraims.loanportfolio.features.calendar.application.port.in;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CalendarUseCase {
	
	Mono<LocalDate> getNextBusinessDateForOffice(String officeId, LocalDate currentBusinessDate);
	Mono<LocalDate> getLastBusinessDateForOffice(String officeId, LocalDate currentBusinessDate);
	Mono<LocalDate> getNextWorkingDayOfAMonthForOffice(String officeId, LocalDate currentBusinessDate);
	Mono<LocalDate> getLastWorkingDayOfAMonthOfCurrentYearForOffice(String officeId, LocalDate currentBusinessDate);
}
