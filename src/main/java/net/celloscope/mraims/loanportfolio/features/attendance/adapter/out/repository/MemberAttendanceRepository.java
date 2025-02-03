package net.celloscope.mraims.loanportfolio.features.attendance.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.attendance.adapter.out.entity.MemberAttendanceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MemberAttendanceRepository extends ReactiveCrudRepository<MemberAttendanceEntity, String> {

    Flux<MemberAttendanceEntity> findAllBySamityIdAndAttendanceDate(String samityId, LocalDate attendanceDate);
    Mono<MemberAttendanceEntity> findFirstByMemberIdAndAttendanceDate(String memberId, LocalDate attendanceDate);
}
