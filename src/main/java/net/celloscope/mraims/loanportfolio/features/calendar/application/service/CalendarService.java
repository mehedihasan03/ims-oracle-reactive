package net.celloscope.mraims.loanportfolio.features.calendar.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.out.CalendarPersistencePort;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.out.HolidayPersistencePort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class CalendarService implements CalendarUseCase {
	
	private final CalendarPersistencePort calendarPort;
	private final HolidayPersistencePort holidayPort;
	
	public CalendarService(CalendarPersistencePort calendarPort, HolidayPersistencePort holidayPort) {
		this.calendarPort = calendarPort;
		this.holidayPort = holidayPort;
	}
	
	@Override
	public Mono<LocalDate> getNextBusinessDateForOffice(String officeId, LocalDate currentBusinessDate) {
		return this.getNextBusinessDateFromHolidayAndCalendar(officeId, currentBusinessDate);
	}

	@Override
	public Mono<LocalDate> getLastBusinessDateForOffice(String officeId, LocalDate currentBusinessDate) {
		return calendarPort.getLastBusinessDateForOffice(officeId, currentBusinessDate);
	}

	@Override
	public Mono<LocalDate> getNextWorkingDayOfAMonthForOffice(String officeId, LocalDate currentBusinessDate) {
		return calendarPort.getNextBusinessCalendarDataForOffice(officeId, currentBusinessDate)
				.map(calendar -> {
					if(calendar.getMonthOfYear() == currentBusinessDate.getMonthValue()){
						return calendar.getCalendarDate();
					}
					return currentBusinessDate;
				});
	}

	@Override
	public Mono<LocalDate> getLastWorkingDayOfAMonthOfCurrentYearForOffice(String officeId, LocalDate currentBusinessDate) {
		return calendarPort.getLastWorkingDayOfAMonthOfCurrentYearForOffice(officeId, currentBusinessDate)
				.doOnNext(lastWorkingDay -> log.info("Last Working Day of Month {} is: {}", currentBusinessDate.getMonth(), lastWorkingDay));
	}

	private Mono<LocalDate> getNextBusinessDateFromHolidayAndCalendar(String officeId, LocalDate currentBusinessDate) {
		AtomicReference<LocalDate> businessDate = new AtomicReference<>();
		return calendarPort.getNextBusinessCalendarDataForOffice(officeId, currentBusinessDate)
				.doOnNext(calendar -> log.debug("Calendar: {}", calendar))
				.flatMap(calendar -> {
					businessDate.set(calendar.getCalendarDate());
					return holidayPort.getNextHolidayEntryForOffice(officeId, currentBusinessDate);
				})
				.doOnNext(holiday -> log.debug("Holiday: {}", holiday))
				.flatMap(holiday -> {
					if (!holiday.getHolidayDate().isEqual(businessDate.get())) {
						return Mono.just(businessDate.get());
					} else {
						return this.getNextBusinessDateFromHolidayAndCalendar(officeId, businessDate.get());
					}
				})
				.doOnNext(nextBusinessDate -> log.info("Next Business Date: {}", nextBusinessDate));
	}
}
