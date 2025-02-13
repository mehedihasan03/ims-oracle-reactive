package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Table("template.savings_account_interest_deposit_history")
public class SavingsAccountInterestDepositHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String accruedInterestId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private String managementProcessId;
    private String memberId;
    private String officeId;
    private String samityId;
    private String transactionId;
    private String productId;
    private String savingsTypeId;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private BigDecimal accruedInterestAmount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String remarks;
    private LocalDateTime archivedOn;
    private String archivedBy;

    @Override
    public String getId() {
        return this.oid;
    }

    public void setId(String id) {
        this.oid = id;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
