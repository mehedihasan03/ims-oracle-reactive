package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawStagingData extends BaseToString {

    private String oid;
    private String id;
    private String managementProcessId;

    private String withdrawStagingDataId;
    private String stagingDataId;
    private String samityId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String withdrawType;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String currentVersion;
    private String status;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
}
