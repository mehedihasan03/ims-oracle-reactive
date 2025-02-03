package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDataInfo {

    private String btnOpenEnabled;
    private String btnViewEnabled;
    private String btnEditEnabled;
    private String btnCommitEnabled;
    private String btnSubmitEnabled;
    private String oid;
    private String collectionStagingDataId;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String stagingDataId;
    private String accountType;
    private String loanAccountId;
    private String savingsAccountId;
    private String accountId;
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private String status;
    private String createdBy;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
