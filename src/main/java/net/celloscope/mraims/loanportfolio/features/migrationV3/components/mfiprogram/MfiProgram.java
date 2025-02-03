package net.celloscope.mraims.loanportfolio.features.migrationV3.components.mfiprogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("mfi_program")
public class MfiProgram {
    private String oid;
    private String mfiProgramId;
    private String mfiProgramNameEn;
    private String mfiProgramNameBn;
    private String mfiProgramShortName;
    private String noPrimaryLoanProduct;
    private String fundingCategory;
    private List<String> loanFundIds;
    private String noOtherLoanProduct;
    private String mfiId;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private Boolean isApproverRemarks;
    private String approverRemarks;
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
