package net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementResponseDTO {
    private String userMessage;
    private List<RepaymentScheduleResponseDTO> repaymentScheduleResponseDTOList;
}
