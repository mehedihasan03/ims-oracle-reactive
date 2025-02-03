package net.celloscope.mraims.loanportfolio.features.calendar.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.out.HolidayPersistencePort;
import net.celloscope.mraims.loanportfolio.features.calendar.domain.Holiday;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class HolidayService implements HolidayUseCase {
    private final HolidayPersistencePort port;
    private final ModelMapper modelMapper;
    public HolidayService(HolidayPersistencePort port, ModelMapper modelMapper) {
        this.port = port;
        this.modelMapper = modelMapper;
    }

    @Override
    public Flux<HolidayResponseDTO> getAllHolidaysOfASamityByLoanAccountId(String loanAccountId) {
        return port.getAllHolidaysOfASamityByLoanAccountId(loanAccountId)
                .map(holiday -> modelMapper.map(holiday, HolidayResponseDTO.class));
    }

    @Override
    public Flux<HolidayResponseDTO> getAllHolidaysOfASamityBySavingsAccountId(String savingsAccountId) {
        return port.getAllHolidaysOfASamityBySavingsAccountId(savingsAccountId)
                .map(holiday -> modelMapper.map(holiday, HolidayResponseDTO.class));

//        return Flux.fromIterable(List.of(HolidayResponseDTO.builder().holidayDate(LocalDate.of(2024,12,16)).build()));
    }

    @Override
    public Flux<LocalDate> getAllHolidaysOfAOfficeByOfficeId(String officeId) {
        return port.getAllHolidaysOfASamityByOfficeId(officeId)
                .map(Holiday::getHolidayDate)
                .distinct();

//        return Flux.fromIterable(List.of(LocalDate.of(2023,12,16)));
    }

    @Override
    public Flux<LocalDate> getAllHolidaysOfAnOfficeByManagementProcessId(String managementProcessId, String officeId) {
        return port.getAllHolidaysOfAnOfficeByManagementProcessId(managementProcessId, officeId)
                .filter(holiday -> holiday.getStatus().equalsIgnoreCase("Active"))
                .map(Holiday::getHolidayDate);
    }

}
