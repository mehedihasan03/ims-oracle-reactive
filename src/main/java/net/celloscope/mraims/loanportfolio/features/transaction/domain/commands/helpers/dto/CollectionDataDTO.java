package net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDataDTO {
    private String stagingDataId;
    private String accountType;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String currentVersion;
    private String status;

}
