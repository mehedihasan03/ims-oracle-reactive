package net.celloscope.mraims.loanportfolio.features.migration.components.memsmtoffprimap;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.mem_smt_off_pri_map")
public class MemSmtOffPriMap {
    private String oid;
    private String memSmtOffPriMapId;
    private String memberId;
    private String lendingCategoryId;
    private String officeId;
    private String samityId;
    private String samityGroupCode;
    private String primarySavingsProdId;
    private String primaryLoanProdId;
    private String mfiProgramId;
    private String mfiId;
    private LocalDate effectiveDate;
    private LocalDate tillDate;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
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
