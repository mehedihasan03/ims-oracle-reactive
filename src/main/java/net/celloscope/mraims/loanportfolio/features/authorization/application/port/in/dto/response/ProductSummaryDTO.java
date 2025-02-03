package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {

    private String productId;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal amount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
