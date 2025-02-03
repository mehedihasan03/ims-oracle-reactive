package net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity;

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
@Table("transaction_history")
public class TransactionHistoryEntity implements Persistable<String> {
    @Id
    private String oid;
    private String transactionId;
    private String managementProcessId;
    private String processId;
    private String memberId;
    private String stagingDataId;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String transactionCode;
    private String accountType;
    private String collectionType;
    private String withdrawType;
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
    private String officeId;
    private String samityId;
    private String loanAdjustmentProcessId;
    private LocalDateTime archivedOn;
    private String archivedBy;

    @Override
    public String getId() {
        return this.oid;
    }

//    public void setId(String id) {
//        this.oid = id;
//    }

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
