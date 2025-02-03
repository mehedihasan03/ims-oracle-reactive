package net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAttendanceRequestDTO {

    private String mfiId;
    private String samityId;
    private LocalDate attendanceDate;
    private String loginId;
    private List<MemberAttendanceInfo> data;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
