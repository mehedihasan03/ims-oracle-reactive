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
public class FeeTypeSetting {
    private String oid;
    private String feeTypeSettingId;
    private String feeCollectionCode;
    private String feeTypeNameEn;
    private String feeTypeNameBn;
    private BigDecimal amount;
    private String remarks;
    private String ledgerId;
    private String subledgerId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
}
