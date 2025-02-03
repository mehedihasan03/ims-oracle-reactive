package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AisJournalData {
    private String oid;
    private String status;
    private String createdBy;
    private String referenceJournalSummaryId;
    private String parentFinancialPeriodId;
    private List<AisJournal> journalList;
    private String journalSummaryId;
    private String journalDate;
    private String journalType;
    private String journalProcess;
    private String description;
    private String amount;
    private String referenceNo;
    private String financialPeriodId;
    private String officeId;
    private String mfiId;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }
}
