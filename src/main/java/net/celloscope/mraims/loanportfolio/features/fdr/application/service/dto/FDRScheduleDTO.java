package net.celloscope.mraims.loanportfolio.features.fdr.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FDRScheduleDTO {

    private String savingsAccountId;
    private String savingsProductId;
    private String savingsAmount;
    private Double interestRate;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private Integer depositTermInMonths;

}
