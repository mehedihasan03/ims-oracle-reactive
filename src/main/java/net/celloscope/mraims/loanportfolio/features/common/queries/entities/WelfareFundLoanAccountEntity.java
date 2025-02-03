package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("loan_account")
public class WelfareFundLoanAccountEntity {

    private String loanAccountId;
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String status;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
