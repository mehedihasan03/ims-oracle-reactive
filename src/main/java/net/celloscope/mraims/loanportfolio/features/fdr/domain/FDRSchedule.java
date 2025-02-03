package net.celloscope.mraims.loanportfolio.features.fdr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FDRSchedule {
    private String savingsAccountId;
    private Integer postingNo;
    private LocalDate interestPostingDate;
    private BigDecimal calculatedInterest;
    private String status;
    private LocalDateTime createdOn;
    private String createdBy;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
