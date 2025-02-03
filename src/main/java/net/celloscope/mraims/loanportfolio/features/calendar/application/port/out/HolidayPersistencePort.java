package net.celloscope.mraims.loanportfolio.features.calendar.application.port.out;

import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.domain.Holiday;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface HolidayPersistencePort {
	
	Mono<Holiday> getNextHolidayEntryForOffice(String officeId, LocalDate currentBusinessDate);

	Flux<Holiday> getAllHolidaysOfASamityByLoanAccountId(String loanAccountId);
	Flux<Holiday> getAllHolidaysOfASamityBySavingsAccountId(String savingsAccountId);
	Flux<Holiday> getAllHolidaysOfASamityByOfficeId(String officeId);
	Flux<Holiday> getAllHolidaysOfAnOfficeByManagementProcessId(String managementProcessId, String officeId);
}
