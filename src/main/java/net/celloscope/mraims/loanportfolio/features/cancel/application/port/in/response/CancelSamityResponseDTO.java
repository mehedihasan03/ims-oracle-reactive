package net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelSamityResponseDTO {
    private String userMessage;
}
