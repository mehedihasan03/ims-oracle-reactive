package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AisJournal {
    private String oid;
    private String entryNo;
    private String journalType;
    private String description;
    private String debitedAmount;
    private String creditedAmount;
    private String ledgerId;
    private String parentLedgerId;
    private String ledgerType;
    private String ledgerNameEn;
    private String ledgerNameBn;
    private String parentLedgerBalance;
    private String ledgerBalance;
    private String parentSubledgerBalance;
    private String subledgerBalance;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }
}
