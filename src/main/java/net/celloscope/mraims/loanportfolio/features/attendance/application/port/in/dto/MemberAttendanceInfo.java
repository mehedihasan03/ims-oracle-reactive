package net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAttendanceInfo {
    private String memberId;
    private String status;
}
