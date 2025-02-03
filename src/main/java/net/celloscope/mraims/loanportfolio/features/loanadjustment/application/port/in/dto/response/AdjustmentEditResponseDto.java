package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentEditResponseDto {
    private String userMessage;
}
