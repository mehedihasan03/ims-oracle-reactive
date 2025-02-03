package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessHistory {

    private String month;
    private Integer year;
    private LocalDate monthEndDate;
    private BigDecimal totalAccruedAmount;
    private BigDecimal totalPostingAmount;
    private String status;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
