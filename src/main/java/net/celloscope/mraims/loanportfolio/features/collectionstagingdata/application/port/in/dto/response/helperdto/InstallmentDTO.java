package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDTO {

    private String loanRepayScheduleId;
    private Integer installmentNo;
    private LocalDate installmentDate;
    private BigDecimal installmentAmount;
    private BigDecimal advance;
    private BigDecimal due;
    private BigDecimal penalty;
    private BigDecimal fees;
    private BigDecimal insurance;
    private String isCurrent;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
