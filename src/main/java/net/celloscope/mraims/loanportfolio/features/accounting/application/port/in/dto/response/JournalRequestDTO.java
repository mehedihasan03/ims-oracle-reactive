package net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalRequestDTO {
    private String journalType;
    private String description;
    private BigDecimal amount;
    private String referenceNo;
    private String journalProcess;
    private String officeId;
    private String mfiId;
    private String createdBy;
    private List<Journal> journalList;
    private String processId;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }
}
