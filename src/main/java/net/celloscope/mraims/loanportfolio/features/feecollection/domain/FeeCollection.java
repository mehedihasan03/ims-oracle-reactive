package net.celloscope.mraims.loanportfolio.features.feecollection.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeeCollection {
    private String oid;
    private String feeCollectionId;
    private String managementProcessId;
    private String referenceNo;
    private LocalDate collectionDate;
    private BigDecimal amount;
    private String receiveMode;
    private String debitLedgerId;
    private String debitSubledgerId;
    private String creditLedgerId;
    private String creditSubledgerId;
    private String remarks;
    private String memberId;
    private String feeCollectionCode;
    private String feeTypeSettingId;
    private String collectedBy;
    private String officeId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
}
