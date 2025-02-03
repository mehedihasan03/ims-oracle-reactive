package net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.repository.HolidayRepository;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.out.HolidayPersistencePort;
import net.celloscope.mraims.loanportfolio.features.calendar.domain.Holiday;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@Slf4j
public class HolidayPersistenceAdapter implements HolidayPersistencePort {

	private final HolidayRepository repository;

	private final ModelMapper modelMapper;
	private final Gson gson;

	public HolidayPersistenceAdapter(HolidayRepository repository, ModelMapper modelMapper) {
		this.repository = repository;
		this.modelMapper = modelMapper;
		this.gson = CommonFunctions.buildGson(this);
	}

	@Override
	public Mono<Holiday> getNextHolidayEntryForOffice(String officeId, LocalDate currentBusinessDate) {
		return repository.findFirstByOfficeIdAndHolidayDateAfterOrderByHolidayDate(officeId, currentBusinessDate)
				.map(holidayEntity -> gson.fromJson(holidayEntity.toString(), Holiday.class));
	}

	@Override
	public Flux<Holiday> getAllHolidaysOfASamityByLoanAccountId(String loanAccountId) {
		return repository.getAllHolidaysOfASamityByLoanAccountId(loanAccountId)
				.map(holidayEntity -> modelMapper.map(holidayEntity, Holiday.class));
	}

	@Override
	public Flux<Holiday> getAllHolidaysOfASamityBySavingsAccountId(String savingsAccountId) {
		return repository.getAllHolidaysOfASamityBySavingsAccountId(savingsAccountId)
				.map(holidayEntity -> modelMapper.map(holidayEntity, Holiday.class));
	}

	@Override
	public Flux<Holiday> getAllHolidaysOfASamityByOfficeId(String officeId) {
		return repository
				.findAllByOfficeIdOrderByHolidayDate(officeId)
				.doOnRequest(l -> log.info("Requested to fetch holidays for office {}", officeId))
//				.doOnNext(holidayEntity -> log.info("Fetched holiday {}", holidayEntity))
				.map(holidayEntity -> modelMapper.map(holidayEntity, Holiday.class));
	}

	@Override
	public Flux<Holiday> getAllHolidaysOfAnOfficeByManagementProcessId(String managementProcessId, String officeId) {
		return repository.findAllByManagementProcessIdAndOfficeIdOrderByHolidayDate(managementProcessId, officeId)
				.doOnRequest(l -> log.info("Requested to fetch holidays for office {} and management process {}", officeId, managementProcessId))
				.map(holidayEntity -> modelMapper.map(holidayEntity, Holiday.class))
				.doOnComplete(() -> log.info("Completed fetching holidays for office {} and management process {}", officeId, managementProcessId))
				.doOnError(throwable -> log.error("Error fetching holidays for officeId and managementProcessId: {}", throwable.getMessage()));
	}
}
