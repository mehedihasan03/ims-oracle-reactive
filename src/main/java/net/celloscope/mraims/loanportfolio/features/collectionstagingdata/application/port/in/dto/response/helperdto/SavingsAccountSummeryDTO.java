package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccountSummeryDTO {

    private String productCode;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal totalTarget;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
