package net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoVoucherJournalRequestDTO {
    private String officeId;
    private String mfiId;
    private String loginId;
    private String managementProcessId;
    private String processId;
    private LocalDate businessDate;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }
}
