package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDR;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FDRGridViewDTO {
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String userMessage;
    private List<FDR> data;
    private Integer totalCount;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
