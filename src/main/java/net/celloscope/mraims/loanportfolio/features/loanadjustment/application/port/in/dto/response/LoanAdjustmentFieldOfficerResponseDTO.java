package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAdjustmentFieldOfficerResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;

    private LocalDate businessDate;
    private String businessDay;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private List<LoanAdjustmentSamityGridViewResponseDTO> data;
    private Integer totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
