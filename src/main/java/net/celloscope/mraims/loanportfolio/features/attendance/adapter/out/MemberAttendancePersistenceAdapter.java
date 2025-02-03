package net.celloscope.mraims.loanportfolio.features.attendance.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.attendance.adapter.out.entity.MemberAttendanceEntity;
import net.celloscope.mraims.loanportfolio.features.attendance.adapter.out.repository.MemberAttendanceRepository;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.out.MemberAttendancePersistencePort;
import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class MemberAttendancePersistenceAdapter implements MemberAttendancePersistencePort {

    private final MemberAttendanceRepository repository;
    private final Gson gson;

    public MemberAttendancePersistenceAdapter(MemberAttendanceRepository repository) {
        this.repository = repository;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Flux<MemberAttendance> getMemberAttendanceListForSamityByAttendanceDate(String samityId, LocalDate attendanceDate) {
        return repository.findAllBySamityIdAndAttendanceDate(samityId, attendanceDate)
                .map(entity -> gson.fromJson(entity.toString(), MemberAttendance.class));
    }

    @Override
    public Mono<List<MemberAttendance>> saveMemberAttendanceListForSamity(List<MemberAttendance> memberAttendanceList) {
        return Flux.fromIterable(memberAttendanceList)
                .map(memberAttendance -> gson.fromJson(memberAttendance.toString(), MemberAttendanceEntity.class))
                .flatMap(repository::save)
                .collectList()
                .map(entityList -> memberAttendanceList);
    }

    @Override
    public Mono<List<MemberAttendance>> updateMemberAttendanceListForSamity(List<MemberAttendance> memberAttendanceList) {
        return Flux.fromIterable(memberAttendanceList)
                .flatMap(memberAttendance -> repository.findFirstByMemberIdAndAttendanceDate(memberAttendance.getMemberId(), memberAttendance.getAttendanceDate())
                        .switchIfEmpty(Mono.just(gson.fromJson(memberAttendance.toString(), MemberAttendanceEntity.class)))
                        .map(entity -> {
                            entity.setStatus(memberAttendance.getStatus());
                            return entity;
                        }))
                .flatMap(repository::save)
                .collectList()
                .map(entityList -> memberAttendanceList);
    }
}
