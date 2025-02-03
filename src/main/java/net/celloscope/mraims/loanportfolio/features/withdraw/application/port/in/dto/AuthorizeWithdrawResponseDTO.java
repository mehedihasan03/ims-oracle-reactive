package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeWithdrawResponseDTO {
    private String userMessage;
}
