package net.celloscope.mraims.loanportfolio.features.cancel.application.port.in;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.request.CancelSamityRequestDTO;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.response.CancelSamityResponseDTO;
import reactor.core.publisher.Mono;

public interface CancelledSamityUseCase {
    Mono<CancelSamityResponseDTO> cancelSamityBySamityId(CancelSamityRequestDTO requestDTO);
    Mono<CancelSamityResponseDTO> restoreCancelledSamityBySamityId(CancelSamityRequestDTO requestDTO);
}
