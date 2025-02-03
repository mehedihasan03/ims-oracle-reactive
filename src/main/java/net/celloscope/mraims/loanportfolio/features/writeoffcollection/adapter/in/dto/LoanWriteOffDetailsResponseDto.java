package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffDetailsResponseDto {
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
    private String paymentMethod;
    private LocalDate writeOffCollectionDate;
    private BigDecimal writeOffCollectionAmount;
    private String status;
    private LoanWriteOffCashCollection collection;



    private BigDecimal payableAmountAfterWriteOffCollection;
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
    private String loanWriteOffOid;



    private List<SavingsAccountForLoanWriteOffDto> savingsAccountList;
    private List<AdjustedLoanAccount> adjustedLoanAccountList;

    private String btnUpdateEnabled;
    private String btnSubmitEnabled;


}
