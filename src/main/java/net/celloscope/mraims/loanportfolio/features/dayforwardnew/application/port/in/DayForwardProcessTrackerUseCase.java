package net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.in;

import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardGridResponseDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardProcessRequestDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardProcessResponseDto;
import reactor.core.publisher.Mono;

public interface DayForwardProcessTrackerUseCase {
    Mono<String> resetDayForwardProcessByOfficeId(String officeId);

    Mono<DayForwardGridResponseDto> dayForwardProcessV2(DayForwardProcessRequestDto requestDto);

    Mono<DayForwardGridResponseDto> refreshDayForwardProcess(DayForwardProcessRequestDto requestDto);

    Mono<DayForwardProcessResponseDto> confirmDayForwardProcess(DayForwardProcessRequestDto requestDto);

    Mono<DayForwardGridResponseDto> retryDayForwardProcess(DayForwardProcessRequestDto requestDto);
}
