package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffCashCollection {

    private String oid;
    private String collectionStagingDataId;
    private String managementProcessId;
    private String processId;
    private String stagingDataId;
    private String samityId;
    private String accountType;
    private String loanAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private String status;

}
