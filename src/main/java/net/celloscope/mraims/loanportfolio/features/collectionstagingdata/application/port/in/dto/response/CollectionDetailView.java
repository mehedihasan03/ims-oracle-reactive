package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailView {
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
