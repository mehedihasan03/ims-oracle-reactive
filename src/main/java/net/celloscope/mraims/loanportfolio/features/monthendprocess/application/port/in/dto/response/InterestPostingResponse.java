package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessProductTransaction;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestPostingResponse {

    private String transactionCode;
    private List<InterestProductResponse> data;
    private BigDecimal totalAmount;
    private String status;
    private String btnRetryEnabled;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
