package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverGridViewResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanWaiverDTO> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
