package net.celloscope.mraims.loanportfolio.features.attendance.application.port.in;

import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceRequestDTO;
import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface MemberAttendanceUseCase {

    Mono<List<MemberAttendance>> getMemberAttendanceListForSamityByAttendanceDate(String samityId, LocalDate attendanceDate);
    Mono<List<MemberAttendance>> saveMemberAttendanceListForSamity(MemberAttendanceRequestDTO requestDTO);
    Mono<List<MemberAttendance>> updateMemberAttendanceListForSamity(MemberAttendanceRequestDTO requestDTO);
}
