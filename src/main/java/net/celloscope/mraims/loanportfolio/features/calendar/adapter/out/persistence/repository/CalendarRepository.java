package net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.entity.CalendarEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface CalendarRepository extends ReactiveCrudRepository<CalendarEntity, String> {
	
	Mono<CalendarEntity> findFirstByOfficeIdAndCalendarDateAfter(String officeId, LocalDate calendarDate);
	
	Mono<CalendarEntity> findFirstByOfficeIdAndIsWorkingDayAndCalendarDateAfterOrderByCalendarDate(String officeId, String isWorkingDay, LocalDate calendarDate);

	@Query("""
	select distinct (calendar_date) from template.calendar c
	where calendar_date < :currentDate
	and is_working_day = 'Yes'
	and office_id = :officeId
	order by calendar_date desc
	FETCH FIRST 1 ROWS ONLY;
	""")
	Mono<LocalDate> getLastBusinessDateForOffice(String officeId, LocalDate currentBusinessDate);

	Mono<CalendarEntity> findFirstByOfficeIdAndMonthOfYearAndCalendarYearAndIsWorkingDayOrderByCalendarDateDesc(String officeId, Integer monthOfYear, Integer CalendarYear, String isWorkingDay);
}
