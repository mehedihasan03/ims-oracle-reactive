package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountDetailsEntity {

    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private String status;
    private BigDecimal loanAmount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
