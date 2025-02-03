package net.celloscope.mraims.loanportfolio.features.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
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
    private String source;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
