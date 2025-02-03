package net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.out;

import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface DayForwardProcessTrackerPersistencePort {
    Mono<DayForwardProcessTrackerEntity> saveDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity);

    Flux<DayForwardProcessTrackerEntity> saveAllDayForwardProcess(List<DayForwardProcessTrackerEntity> dayForwardProcessTrackerEntity);

    Flux<DayForwardProcessTrackerEntity> getAllDayForwardTrackerDataByManagementProcessId(String managementProcessId);

    Flux<DayForwardProcessTrackerEntity> getAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

    Mono<DayForwardProcessTrackerEntity> getDayForwardProcessByManagementProcessIdAndOfficeIdAndSamityId(String managementProcessId, String officeId, String samityId);

    Mono<Void> deleteAllDataByManagementProcessId(String managementProcessId);

    Mono<String> saveDayForwardProcessTrackerIntoHistory(List<DayForwardProcessTrackerHistoryEntity> historyEntityList);

    Mono<DayForwardProcessTrackerEntity> updateRescheduleStatusOfDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String rescheduleStatus);

    Mono<Void> updateArchivingStatusOfDayForwardProcess(String officeId, String managementProcessId, String archivingStatus);

    Mono<DayForwardProcessTrackerEntity> updateStatusAndProcessEndTimeOfDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String status, LocalDateTime processEndTime);

    Mono<DayForwardProcessTrackerEntity> updateDayForwardProcessByManagementProcessIdAndSamityId(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String status, String rescheduleStatus, String archiveStatus, String loginId, LocalDateTime retriedOn);
}
