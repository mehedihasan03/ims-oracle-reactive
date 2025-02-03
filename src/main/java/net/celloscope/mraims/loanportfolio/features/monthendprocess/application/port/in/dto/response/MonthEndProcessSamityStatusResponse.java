package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessSamityStatusResponse {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private BigDecimal totalAccruedAmount;
    private BigDecimal totalPostingAmount;

    private String status;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    private String btnRetryEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
