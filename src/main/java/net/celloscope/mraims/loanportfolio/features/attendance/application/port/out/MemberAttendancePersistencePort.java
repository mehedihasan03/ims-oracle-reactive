package net.celloscope.mraims.loanportfolio.features.attendance.application.port.out;

import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface MemberAttendancePersistencePort {
    Flux<MemberAttendance> getMemberAttendanceListForSamityByAttendanceDate(String samityId, LocalDate attendanceDate);
    Mono<List<MemberAttendance>> saveMemberAttendanceListForSamity(List<MemberAttendance> memberAttendanceList);

    Mono<List<MemberAttendance>> updateMemberAttendanceListForSamity(List<MemberAttendance> memberAttendanceList);
}
