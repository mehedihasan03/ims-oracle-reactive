package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.SavingsAccountProductEntity;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSRepaymentCommand {
    private String savingsAccountId;
    private LocalDate firstInstallmentDate;
    private String loginId;
}
