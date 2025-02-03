package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.in;

import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto.*;
import reactor.core.publisher.Mono;

public interface SeasonalLoanUseCase {
    Mono<SeasonalLoanCollectionResponseDto> collectSeasonalLoan(SeasonalLoanCollectionRequestDto command);
    Mono<SeasonalLoanGridResponseDto> getSeasonalLoanGridView(SeasonalLoanGridRequestDto command);
    Mono<SeasonalLoanDetailViewDto> getSeasonalLoanDetailView(String oid, String officeId);
}
