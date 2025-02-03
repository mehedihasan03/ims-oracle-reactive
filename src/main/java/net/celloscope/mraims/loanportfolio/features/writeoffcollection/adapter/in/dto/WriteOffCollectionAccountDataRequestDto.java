package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedAccount;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffCollectionAccountDataRequestDto {
    private String id;
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;
    private String loanAccountId;
    private String transactionDate;
    private Integer limit;
    private Integer offset;

    private String status;
    private String searchText;

    private String memberId;
    private String samityId;
    private BigDecimal writeOffCollectionAmount;
    private String collectionType;
    private BigDecimal collectedAmountByCash;
    private List<AdjustedAccount> adjustedAccountList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
