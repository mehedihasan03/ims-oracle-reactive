package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.ais_meta_data")
public class AisMetaDataEntity {
    @Id
    private String oid;
    private String processName;
    private String ledgerKey;
    private String journalEntryType;
    private String hasSubledger;
    private String isAggregated;
    private String tableName;
    private String fieldName;
    private String productType;
    private String savingsTypeId;
    private String paymentMode;
    private String transactionCode;
    private String status;
    private String description;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
