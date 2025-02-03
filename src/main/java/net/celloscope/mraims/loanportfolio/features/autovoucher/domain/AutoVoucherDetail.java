package net.celloscope.mraims.loanportfolio.features.autovoucher.domain;

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
public class AutoVoucherDetail {
    private String oid;
    private String autoVoucherDetailOid;
    private String voucherId;
    private LocalDate transactionDate;
    private BigDecimal debitedAmount;
    private BigDecimal creditedAmount;
    private String ledgerId;
    private String subledgerId;
    private String remarks;
    private String officeId;
    private String mfiId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String archivedBy;
    private LocalDateTime archivedOn;
}
