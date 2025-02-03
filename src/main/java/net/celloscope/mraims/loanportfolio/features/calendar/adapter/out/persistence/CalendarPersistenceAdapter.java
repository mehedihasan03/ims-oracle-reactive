package net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.entity.CalendarEntity;
import net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.repository.CalendarRepository;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.out.CalendarPersistencePort;
import net.celloscope.mraims.loanportfolio.features.calendar.domain.Calendar;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@Slf4j
public class CalendarPersistenceAdapter implements CalendarPersistencePort {
	
	private final CalendarRepository repository;
	
	private final ModelMapper modelMapper;
	private final Gson gson;
	
	public CalendarPersistenceAdapter(CalendarRepository repository, ModelMapper modelMapper) {
		this.repository = repository;
		this.modelMapper = modelMapper;
		this.gson = CommonFunctions.buildGson(this);
	}
	
	@Override
	public Mono<Calendar> getNextBusinessCalendarDataForOffice(String officeId, LocalDate currentBusinessDate) {
		return repository.findFirstByOfficeIdAndIsWorkingDayAndCalendarDateAfterOrderByCalendarDate(officeId, "Yes", currentBusinessDate)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Next Working Day Entry is Not Found In Calendar For Office")))
				.map(entity -> gson.fromJson(entity.toString(), Calendar.class))
				.doOnNext(calendar -> log.info("Next Working Day Calendar Data: {}", calendar));
	}
	
	@Override
	public Mono<LocalDate> getNextBusinessDateForOffice(String officeId, LocalDate currentBusinessDate) {
		return Mono.just(LocalDate.now());
	}

	@Override
	public Mono<LocalDate> getLastBusinessDateForOffice(String officeId, LocalDate currentBusinessDate) {
		return repository
				.getLastBusinessDateForOffice(officeId, currentBusinessDate)
				.doOnNext(date -> log.info("last business date for office : {} -> {}", officeId, date));
	}

	@Override
	public Mono<LocalDate> getLastWorkingDayOfAMonthOfCurrentYearForOffice(String officeId, LocalDate currentBusinessDate) {
		return repository.findFirstByOfficeIdAndMonthOfYearAndCalendarYearAndIsWorkingDayOrderByCalendarDateDesc(officeId, currentBusinessDate.getMonthValue(), currentBusinessDate.getYear(), "Yes")
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Month End Date is Not Found In Calendar For Office : " + officeId + " and Month : " + currentBusinessDate.getMonth() + " and Year : " + currentBusinessDate.getYear())))
				.map(CalendarEntity::getCalendarDate);
	}
}
