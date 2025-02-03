package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepaymentScheduleGenerationDTO {
    private List<RepaymentScheduleResponseDTO> repaymentScheduleResponseDTOList;
    private BigDecimal annualServiceChargeRate;

}
