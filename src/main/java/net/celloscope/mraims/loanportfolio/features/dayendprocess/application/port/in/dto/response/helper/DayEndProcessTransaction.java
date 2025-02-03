package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessTransaction {

    private List<DayEndProcessProductTransaction> data;
    private String transactionCode;
    private BigDecimal totalAmount;
    private String status;
    private String btnRetryEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
