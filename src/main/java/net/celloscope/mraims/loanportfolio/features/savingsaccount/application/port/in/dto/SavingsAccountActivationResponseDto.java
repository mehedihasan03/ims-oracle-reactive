package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccountActivationResponseDto {
    private String userMessage;
    private String savingsAccountId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
