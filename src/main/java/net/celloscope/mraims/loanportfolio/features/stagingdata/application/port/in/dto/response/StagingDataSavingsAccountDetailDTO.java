package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagingDataSavingsAccountDetailDTO {
    private String stagingAccountDataId;
    private String memberId;
    private String savingsAccountId;
    private String savingsProductCode;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private String savingsProductType;
    private BigDecimal targetAmount;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal balance;
    private BigDecimal lastDepositAmount;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;
    private BigDecimal accruedInterestAmount;
    private String depositSchemeDetail;
    private LocalDateTime lastDepositDate;
    private String depositCollectionType;
    private BigDecimal lastWithdrawAmount;
    private LocalDateTime lastWithdrawDate;
    private String withdrawType;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
