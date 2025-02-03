package net.celloscope.mraims.loanportfolio.features.welfarefund.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFund {

    private String oid;
    private String welfareFundDataId;
    private String managementProcessId;
    private String officeId;
    private String loanAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNo;
    private String isNew;
    private int currentVersion;
    private String status;
    private LocalDate transactionDate;
    private String samityId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private LocalDateTime rejectedOn;
    private String rejectedBy;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
