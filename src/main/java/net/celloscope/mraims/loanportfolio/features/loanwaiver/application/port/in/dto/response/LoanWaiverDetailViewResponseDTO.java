package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverDetailViewResponseDTO {

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
    private String waiverPaymentMethod;
    private LocalDate waiverDate;
    private BigDecimal payableAmount;
    private BigDecimal waivedAmount;
    private String status;
    private String remarks;

    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private String submittedBy;
    private LocalDateTime submittedOn;
    private String approvedBy;
    private LocalDateTime approvedOn;

    private String officeId;
    private String samityId;
    private BigDecimal serviceChargeRate;
    private int loanTerm;
    private BigDecimal installmentAmount;
    private int noOfInstallment;
    private String disbursementDate;
    private BigDecimal advancePaid;

    private CollectionDetailView collection;

    private List<AdjustedLoanAccount> adjustedLoanAccountList;
    private List<SavingsAccountDetails> savingsAccountList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
