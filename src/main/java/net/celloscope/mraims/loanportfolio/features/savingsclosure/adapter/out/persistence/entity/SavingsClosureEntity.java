package net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("savings_account_closure")
public class SavingsClosureEntity implements Persistable<String> {

    @Id
    private String oid;
    private String savingsAccountId;
    private String savingsApplicationId;
    private String savingsProductId;
    private String savingsProdNameEn;
    private String memberId;
    private String officeId;
    private String memberNameEn;
    private String memberNameBn;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private LocalDate acctCloseDate;
    private BigDecimal savingsAmount;
    private BigDecimal actualInterestRate;
    private BigDecimal effectiveInterestRate;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private BigDecimal totalInterest;
    private BigDecimal closingInterest;
    private BigDecimal closingAmount;
    private String paymentMode;
    private String referenceAccountId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private LocalDateTime rejectedOn;
    private String rejectedBy;
    private String status;
    private String remarks;

    private String managementProcessId;
    private String savingsTypeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

    @Override
    public String getId() {
        return this.oid;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}
