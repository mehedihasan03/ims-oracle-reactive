package net.celloscope.mraims.loanportfolio.features.accounting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AisMetaData {
    private String oid;
    private String processName;
    private String ledgerKey;
    private String journalEntryType;
    private String hasSubledger;
    private String isAggregated;
    private String tableName;
    private List<String> fieldNames;
    private String productType;
    private String savingsTypeId;
    private String paymentMode;
    private String transactionCode;
    private String status;
    private String description;
}
