package net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.loan_account")
public class LoanAccountEntity extends BaseToString implements Persistable<String> {

    @Id
    private String oid;
    private String loanApplicationId;
    private String loanAccountId;
    private String mfiId;
    private String status;

//	private String compLoanAccountId;

    private String memberId;

    @Column("loan_product_id")
    private String productCode;
    private String productName;
//    private String loanProductNameEn;
//    private String loanProductNameBn;

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

//    private BigDecimal insurance;
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
    private String loanProductId;

    private LocalDate paidOffDate;

    private BigDecimal serviceChargeRatePerPeriod;
    private BigDecimal origServiceChargeRate;


    @Override
    public String getId() {
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}
