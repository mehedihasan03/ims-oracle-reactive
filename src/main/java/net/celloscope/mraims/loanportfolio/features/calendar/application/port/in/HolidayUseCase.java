package net.celloscope.mraims.loanportfolio.features.calendar.application.port.in;

import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface HolidayUseCase {
    Flux<HolidayResponseDTO> getAllHolidaysOfASamityByLoanAccountId(String loanAccountId);
    Flux<HolidayResponseDTO> getAllHolidaysOfASamityBySavingsAccountId(String savingsAccountId);
    Flux<LocalDate> getAllHolidaysOfAOfficeByOfficeId(String officeId);
    Flux<LocalDate> getAllHolidaysOfAnOfficeByManagementProcessId(String managementProcessId, String officeId);
}
