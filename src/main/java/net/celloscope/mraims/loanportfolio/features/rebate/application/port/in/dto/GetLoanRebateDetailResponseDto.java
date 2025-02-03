package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionDetailView;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetLoanRebateDetailResponseDto {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private BigDecimal totalLoanAmount;
    private BigDecimal principalPaid;
    private BigDecimal serviceChargePaid;
    private BigDecimal totalPaid;
    private BigDecimal principalRemaining;
    private BigDecimal serviceChargeRemaining;
    private BigDecimal totalDue;
    private String rebatePaymentMethod;
    private LocalDate rebateDate;
    private BigDecimal rebateableAmount;
    private BigDecimal rebatedAmount;
    private BigDecimal payableAmountAfterRebate;
    private String status;
    private String remarks;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private String submittedBy;
    private LocalDateTime submittedOn;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String samityId;
    private String officeId;
    private BigDecimal serviceChargeRate;
    private int loanTerm;
    private BigDecimal installmentAmount;
    private int noOfInstallment;
    private String disbursementDate;
    private BigDecimal advancePaid;
    private String loanRebateOid;


    private CollectionDetailView collection;

    private List<AdjustedLoanAccount> adjustedLoanAccountList;
    private List<SavingsAccountForRebateDto> savingsAccountList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
