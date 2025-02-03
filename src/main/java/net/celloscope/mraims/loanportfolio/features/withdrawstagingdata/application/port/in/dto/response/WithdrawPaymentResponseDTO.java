package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawPaymentResponseDTO {
    private String userMessage;
}
