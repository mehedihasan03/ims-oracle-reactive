package net.celloscope.mraims.loanportfolio.features.loanaccount.application.service.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountResponseDTO extends BaseToString {

//	private String oid;

    private String loanApplicationId;
    private String loanAccountId;
    private String mfiId;
    private String status;

//	private String compLoanAccountId;

    private String memberId;
    private String productCode;
    private String productName;

    private String econPurposeMraCode;
    //	private String loanPurpose;
    private String lendingCategoryId;

    private BigDecimal loanAmount;
    private Integer loanTerm;
    private Integer noInstallment;

    private String isGraceOverwritten;
    private Integer graceDays;

    private BigDecimal installmentAmount;
    private String scheduleCreationList;
    private Integer noRescheduled;

    private String payMethod;

    private BigDecimal insurance;
    private BigDecimal loanInsurancePremium;
    private BigDecimal loanInsurBorrowDeath;

    private LocalDate expectedDisburseDt;
    private LocalDate actualDisburseDt;

    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;

    private String loanContractPhaseId;
    private String loanWriteOffId;
    private String fundSource;
    private String isSubsidizedCredit;
    private String isLawSuit;
    private String isWelfareFund;
    private String folioNo;
    private String guarantorListJson;
    private String currentVersion;
    private String isNewRecord;

    private String approvedBy;
    private LocalDateTime approvedOn;

    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;

    private LocalDateTime loanAppliedOn;
    private String loanAppliedBy;

    private LocalDateTime disbursedOn;
    private String disbursedBy;

    private LocalDateTime closedOn;
    private String closedBy;

    private LocalDateTime createdOn;
    private String createdBy;

    private LocalDateTime updatedOn;
    private String updatedBy;
}
