package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AisResponse {

    private String status;
    private String code;
    private String message;
    private String messageCode;
    private AisJournalData data;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }

}

