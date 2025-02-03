package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in;

import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.AuthorizationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response.AuthorizationGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response.AuthorizationResponseDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response.AuthorizationSummaryViewResponseDTO;
import reactor.core.publisher.Mono;

public interface IAuthorizationUseCase {

    Mono<AuthorizationGridViewResponseDTO> gridViewOfAuthorization(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationSummaryViewResponseDTO> tabViewOfAuthorization(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> lockSamityListForAuthorization(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> unlockSamityListForAuthorization(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> authorizeSamityList(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> authorizeSamityListMigration(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> rejectSamityList(AuthorizationRequestDTO requestDTO);
    Mono<AuthorizationResponseDTO> unauthorizeSamityList(AuthorizationRequestDTO requestDTO);
}
