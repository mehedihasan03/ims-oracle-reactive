package net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.AccountingMetaProperty;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingRequestDTO {
    private String officeId;
    private String mfiId;
    private String loginId;
    private String processName;
    private String managementProcessId;
    private String businessDate;
    private String disbursedLoanAccountId;

    private AccountingMetaProperty accountingMetaProperty;
}
