package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstallmentInfo {
    private Integer minInstallmentNo;
    private Integer maxInstallmentNo;
    private Integer defaultInstallmentNo;
}
