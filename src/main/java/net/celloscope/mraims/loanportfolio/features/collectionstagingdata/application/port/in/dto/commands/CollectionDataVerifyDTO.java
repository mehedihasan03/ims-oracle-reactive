package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDataVerifyDTO {
    private String officeId;
    private String samityId;
    private String samityDay;
    private List<String> stagingDataIdForLoanList;
    private List<String> loanAccountIdList;
    private List<String> stagingDataIdForSavingsList;
    private List<String> savingsAccountIdList;
    private String collectionType;
    private Integer totalCount;
    private String status;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
