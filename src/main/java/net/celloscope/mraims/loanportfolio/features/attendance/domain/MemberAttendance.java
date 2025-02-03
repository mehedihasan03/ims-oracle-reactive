package net.celloscope.mraims.loanportfolio.features.attendance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAttendance {

    private String memberAttendanceId;
    private String samityId;
    private String memberId;

    private LocalDate attendanceDate;
    private String samityDay;

    private String status;
    private String mfiId;

    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
//    private LocalDateTime uploadedOn;
//    private String uploadedBy;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
