package net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.domain.SavingsClosure;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsClosureDto {

    private String userMessage;
    private SavingsClosure data;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
