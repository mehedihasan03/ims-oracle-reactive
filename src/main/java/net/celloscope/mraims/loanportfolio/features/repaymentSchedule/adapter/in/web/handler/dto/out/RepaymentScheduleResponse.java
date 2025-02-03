package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepaymentScheduleResponse {
    private String message;
    private List<?> data;
}
