package net.celloscope.mraims.loanportfolio.features.migration.components.lendingcategory;

import com.google.gson.GsonBuilder;
import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("template.lending_category")
public class LendingCategory {

    private String oid;
    private String lendingCategoryId;
    private String lendingCategoryShortName;
    private String lendingCategoryNameEn;
    private String lendingCategoryNameBn;
    private String noPrimaryLoanProduct;
    private String noOtherLoanProduct;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String mfiId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
