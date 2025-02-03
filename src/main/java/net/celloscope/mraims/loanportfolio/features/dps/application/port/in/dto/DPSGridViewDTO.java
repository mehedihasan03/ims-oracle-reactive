package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPS;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDR;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSGridViewDTO {
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String userMessage;
    private List<DPS> data;
    private Integer totalCount;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
