package net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("transaction_adjustment")
public class TransactionAdjustmentEntity implements Persistable<String> {
    @Id
    private String oid;
    private String managementProcessId;
    private LocalDate businessDate;
    private String memberId;
    private String officeId;
    private String loanAccountId;
    private String loanProductId;
    private String savingsAccountId;
    private String savingsProductId;
    private String transactionId;
    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private String reverseTransactionId;
    private LocalDate reverseTransactionDate;
    private BigDecimal reverseTransactionAmount;
    private String adjustmentTransactionId;
    private LocalDate adjustmentTransactionDate;
    private BigDecimal adjustmentTransactionAmount;
    private BigDecimal netAmount;
    private String paymentMode;
    private String journalEntryType;
    private String remarks;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private BigDecimal transactionPrincipal;
    private BigDecimal transactionServiceCharge;
    private String transactionCode;
    private String savingsTypeId;

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

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
