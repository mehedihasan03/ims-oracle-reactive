package net.celloscope.mraims.loanportfolio.features.attendance.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.MemberAttendanceUseCase;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceRequestDTO;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.out.MemberAttendancePersistencePort;
import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class MemberAttendanceService implements MemberAttendanceUseCase {

    private final MemberAttendancePersistencePort port;
    private final Gson gson;

    public MemberAttendanceService(MemberAttendancePersistencePort port) {
        this.port = port;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<List<MemberAttendance>> getMemberAttendanceListForSamityByAttendanceDate(String samityId, LocalDate attendanceDate) {
        return port.getMemberAttendanceListForSamityByAttendanceDate(samityId, attendanceDate)
                .collectList();
    }

    @Override
    public Mono<List<MemberAttendance>> saveMemberAttendanceListForSamity(MemberAttendanceRequestDTO requestDTO) {
        return port.saveMemberAttendanceListForSamity(this.buildMemberAttendanceList(requestDTO));
    }

    @Override
    public Mono<List<MemberAttendance>> updateMemberAttendanceListForSamity(MemberAttendanceRequestDTO requestDTO) {
        return port.updateMemberAttendanceListForSamity(this.buildMemberAttendanceList(requestDTO));
    }

    private List<MemberAttendance> buildMemberAttendanceList(MemberAttendanceRequestDTO requestDTO) {
        return requestDTO.getData().stream()
                .map(memberAttendanceInfo -> MemberAttendance.builder()
                        .memberAttendanceId(UUID.randomUUID().toString())
                        .mfiId(requestDTO.getMfiId())
                        .samityId(requestDTO.getSamityId())
                        .memberId(memberAttendanceInfo.getMemberId())
                        .status(memberAttendanceInfo.getStatus())
                        .attendanceDate(requestDTO.getAttendanceDate())
                        .samityDay(requestDTO.getAttendanceDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .createdBy(requestDTO.getLoginId())
                        .createdOn(LocalDateTime.now())
                        .build())
                .toList();
    }
}
