package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.DepositSchemeDetailDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingSavingsAccountInfoDTO extends BaseToString {

    private String oid;
    private String savingsAccountId;
    private String savingsProductCode;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private String savingsProductType;
    private BigDecimal targetAmount;
    private BigDecimal balance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;
    
    private BigDecimal lastDepositAmount;
    private LocalDateTime lastDepositDate;
    private String lastDepositType;
    
    private BigDecimal lastWithdrawAmount;
    private LocalDateTime lastWithdrawDate;
    private String lastWithdrawType;
    
    private BigDecimal accruedInterestAmount;
    private List<DepositSchemeDetailDTO> depositSchemeDetail;
    private Integer dpsPendingInstallmentNo;
}
