package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface DayForwardProcessTrackerRepository extends R2dbcRepository<DayForwardProcessTrackerEntity, String> {
    Flux<DayForwardProcessTrackerEntity> findAllByManagementProcessId(String managementProcessId);

    Flux<DayForwardProcessTrackerEntity> findByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

    Mono<DayForwardProcessTrackerEntity> findByManagementProcessIdAndOfficeIdAndSamityId(String managementProcessId, String officeId, String samityId);

    Mono<Void> deleteByManagementProcessId(String managementProcessId);

    @Query("update template.day_forward_process_tracker set rescheduling_status = :rescheduleStatus where management_process_id = :managementProcessId and samity_id = :samityId")
    Mono<Void> updateRescheduleStatusBySamityIdAndManagementProcessId(String managementProcessId, String samityId, String rescheduleStatus);

    @Query("update template.day_forward_process_tracker set archiving_status = :archivingStatus where management_process_id = :managementProcessId and office_id = :officeId")
    Mono<Void> updateArchivingStatusByOfficeIdAndManagementProcessId(String managementProcessId, String officeId, String archivingStatus);

    @Query("update template.day_forward_process_tracker set status = :status, process_end_time = :processEndTime where management_process_id = :managementProcessId and samity_id = :samityId")
    Mono<Void> updateStatusAndProcessEndTimeBySamityIdAndManagementProcessId(String managementProcessId, String samityId, String rescheduleStatus, LocalDateTime processEndTime);

    @Query("update template.day_forward_process_tracker set status = case when :status is not null and :status != '' then :status else status end, rescheduling_status = case when :rescheduleStatus is not null and :rescheduleStatus != '' then :rescheduleStatus else rescheduling_status end, archiving_status = case when :archiveStatus is not null and :archiveStatus != '' then :archiveStatus else archiving_status end, retried_by = case when :loginId is not null and :loginId != '' then :loginId else retried_by end, retried_on = case when :retriedOn is not null then :retriedOn else retried_on end where management_process_id = :managementProcessId and samity_id = :samityId;")
    Mono<Void> updateDayForwardProcessTrackerByManagementProcessIdAndSamityId(String managementProcessId, String samityId, String status, String rescheduleStatus, String archiveStatus, String loginId, LocalDateTime retriedOn);
}
