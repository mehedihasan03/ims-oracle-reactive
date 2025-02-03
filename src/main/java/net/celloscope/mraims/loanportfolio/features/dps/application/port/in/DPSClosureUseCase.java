package net.celloscope.mraims.loanportfolio.features.dps.application.port.in;

import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import reactor.core.publisher.Mono;

public interface DPSClosureUseCase {
    Mono<DPSGridViewDTO> getDPSGridViewByOffice(DPSGridViewCommand command);
    Mono<DPSDetailViewDTO> getDPSDetailViewBySavingsAccountId(String savingsAccountId);

    Mono<DPSClosureDTO> closeDPSAccount(DPSClosureCommand command);
    Mono<DPSClosureDTO> authorizeDPSClosure(DPSAuthorizeCommand command);

    Mono<DPSClosureDTO> rejectDPSClosure(DPSAuthorizeCommand command);

    Mono<DPSClosureGridViewResponse> getDPSClosureGridViewByOffice(DPSGridViewCommand command);
    Mono<DPSClosureDetailViewResponse> getDPSClosureDetailViewBySavingsAccountId(String savingsAccountId);

    Mono<DPSClosureDTO> getDPSClosingInfoBySavingsAccountId(DPSClosureCommand command);

}
