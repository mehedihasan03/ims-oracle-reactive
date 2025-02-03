package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsDepositCommand {

    private SavingsAccountResponseDTO savingsAccountResponseDTO;
    private BigDecimal amount;
    private String transactionId;
    private String transactionCode;
    private String mfiId;
    private String officeId;
    private LocalDate transactionDate;
    private String loginId;
    private String managementProcessId;
    private String processId;
    private String paymentMode;
    private String source;
    private String samityId;

}
