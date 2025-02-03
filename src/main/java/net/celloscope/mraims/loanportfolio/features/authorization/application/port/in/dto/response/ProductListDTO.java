package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDTO {

    private List<ProductSummaryDTO> productSummaryList;
    private BigDecimal totalAmount;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
