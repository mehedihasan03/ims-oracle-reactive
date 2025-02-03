package net.celloscope.mraims.loanportfolio.features.calendar.application.port.out;

import net.celloscope.mraims.loanportfolio.features.calendar.domain.Calendar;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CalendarPersistencePort {
	Mono<Calendar> getNextBusinessCalendarDataForOffice(String officeId, LocalDate currentBusinessDate);
	
	Mono<LocalDate> getNextBusinessDateForOffice(String officeId, LocalDate currentBusinessDate);
	Mono<LocalDate> getLastBusinessDateForOffice(String officeId, LocalDate currentBusinessDate);

	Mono<LocalDate> getLastWorkingDayOfAMonthOfCurrentYearForOffice(String officeId, LocalDate currentBusinessDate);
}
