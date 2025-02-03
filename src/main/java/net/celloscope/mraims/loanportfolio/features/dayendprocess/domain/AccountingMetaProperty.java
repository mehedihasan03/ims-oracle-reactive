package net.celloscope.mraims.loanportfolio.features.dayendprocess.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountingMetaProperty {
    private Boolean allowSCProvision;
    private Boolean allowAdvanceJournal;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
