package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawSavingsAccountInfoDTO {

    private String savingsAccountId;
    private String savingsProductCode;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private String savingsProductType;
    private BigDecimal targetAmount;
    private BigDecimal balance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal minBalance;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;

    private BigDecimal accruedInterestAmount;
    private List<WithdrawSavingsAccountDepositSchemeDetail> depositSchemeDetails;


    private BigDecimal lastDepositAmount;
    private LocalDateTime lastDepositDate;
    private String lastDepositType;

    private BigDecimal lastWithdrawAmount;
    private LocalDateTime lastWithdrawDate;
    private String lastWithdrawType;

    //	from withdraw staging data
    private BigDecimal amount;
    private String paymentMode;
    private String uploadedBy;
    private LocalDateTime uploadedOn;
    private String status;
    private String withdrawType;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
