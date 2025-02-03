package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request.WelfareFundRequestDto;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.welfarefund.domain.WelfareFund;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WelfareFundUseCase {
    Mono<WelfareFundGridViewResponseDto> gridViewOfWelfareFundByOffice(WelfareFundRequestDto requestDto);

    Mono<WelfareFundDetailsViewResponseDto> getWelfareFundDetailView(WelfareFundRequestDto requestDto);

    Mono<LoanAccountDetailsResponseDto> loanAccountDetailsByLoanAccountId(WelfareFundRequestDto requestDto);

    Mono<WelfareFundSaveResponseDto> saveCollectedWelfareFund(WelfareFundRequestDto requestDto);

    Mono<WelfareFundSaveResponseDto> updateCollectedWelfareFund(WelfareFundRequestDto requestDto);
    Mono<WelfareFundLoanAccountDetailViewResponseDTO> getWelfareFundDetailsForLoanAccount(WelfareFundRequestDto requestDto);
    Mono<WelfareFundSaveResponseDto> authorizeWelfareFundDataByLoanAccountId(WelfareFundRequestDto requestDto);
    Mono<WelfareFundSaveResponseDto> rejectWelfareFundDataByLoanAccountId(WelfareFundRequestDto requestDto);
    Mono<WelfareFundGridViewResponseDto> gridViewOfWelfareFundDataByOfficeForAuthorization(WelfareFundRequestDto requestDto);
    Mono<WelfareFundDetailsViewResponseDto> detailViewOfWelfareFundDataByLoanAccountForAuthorization(WelfareFundRequestDto requestDto);

    Flux<WelfareFund> getAllWelfareFundTransactionForOfficeOnABusinessDay(String managementProcessId, String officeId);

    Mono<WelfareFundSaveResponseDto> resetWelfareFundData(WelfareFundRequestDto requestDto);
}
