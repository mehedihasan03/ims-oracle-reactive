package net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
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
@Table("transaction")
public class TransactionEntity implements Persistable<String> {
    @Id
    private String oid;
    private String managementProcessId;
    private String stagingDataId;
    private String accountType;
    private String transactionId;
    private String processId;

    private String officeId;
    private String samityId;
    private String memberId;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String collectionType;
    private String withdrawType;
    private String transactionCode;
    private String paymentMode;
    private String digitalPayCompId;
    private String digitalWalletNumber;
    private String mfiId;
    private LocalDate transactionDate;
    private String transactedBy;
    private LocalDateTime postedOn;
    private String postedBy;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String deleted;
    private String loanAdjustmentProcessId;

    @Builder.Default
    private String source = Constants.SOURCE_APPLICATION.getValue();

    public void setSource(String source) {
        this.source = (source == null) ? Constants.SOURCE_APPLICATION.getValue() : source;
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
