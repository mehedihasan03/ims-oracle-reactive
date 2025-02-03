package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanRebateGridData {
    private String oid;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private LocalDate earlySettlementDate;
    private BigDecimal rebatedAmount;
    private BigDecimal payableAmountAfterRebate;
    private String status;
    private String btnUpdateEnabled;
    private String btnSubmitEnabled;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
