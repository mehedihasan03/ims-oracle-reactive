package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessProductTransaction {

    private String productId;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal amount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
