package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.repository.DayForwardProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.repository.DayForwardProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.out.DayForwardProcessTrackerPersistencePort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DayForwardProcessTrackerPersistenceAdapter implements DayForwardProcessTrackerPersistencePort {
    private final DayForwardProcessTrackerRepository dayForwardProcessTrackerRepository;
    private final DayForwardProcessTrackerHistoryRepository dayForwardProcessTrackerHistoryRepository;

    @Override
    public Mono<DayForwardProcessTrackerEntity> saveDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity) {
        return dayForwardProcessTrackerRepository.save(dayForwardProcessTrackerEntity)
                .doOnRequest(req -> log.info("Saving day forward process tracker {}", dayForwardProcessTrackerEntity))
                .doOnSuccess(s -> log.info("Saved day forward process tracker {}", dayForwardProcessTrackerEntity))
                .doOnError(e -> log.error("Error while saving day forward process tracker {}", dayForwardProcessTrackerEntity, e))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving day forward process tracker")));
    }

    @Override
    public Flux<DayForwardProcessTrackerEntity> saveAllDayForwardProcess(List<DayForwardProcessTrackerEntity> dayForwardProcessTrackerEntityList) {
        return dayForwardProcessTrackerRepository.saveAll(dayForwardProcessTrackerEntityList)
                .doOnRequest(req -> log.info("Saving day forward process tracker list {}", dayForwardProcessTrackerEntityList))
                .doOnComplete(() -> log.info("Saved day forward process tracker list {}", dayForwardProcessTrackerEntityList))
                .doOnError(e -> log.error("Error while saving day forward process tracker list {}", dayForwardProcessTrackerEntityList, e))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving day forward process tracker list")));
    }

    @Override
    public Flux<DayForwardProcessTrackerEntity> getAllDayForwardTrackerDataByManagementProcessId(String managementProcessId) {
        return dayForwardProcessTrackerRepository.findAllByManagementProcessId(managementProcessId)
                .doOnRequest(req -> log.info("Getting day forward process tracker by id {}", managementProcessId))
                .doOnComplete(() -> log.info("Got day forward process tracker by id {}", managementProcessId))
                .doOnError(e -> log.error("Error while getting day forward process tracker by id {}", managementProcessId, e))
                .onErrorResume(e -> Flux.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting day forward process tracker by id")));
    }

    @Override
    public Flux<DayForwardProcessTrackerEntity> getAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId) {
        return dayForwardProcessTrackerRepository.findByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .doOnRequest(req -> log.info("Getting day forward process tracker by management process id {} and office id {}", managementProcessId, officeId))
                .doOnComplete(() -> log.info("Got day forward process tracker by management process id {} and office id {}", managementProcessId, officeId))
                .doOnError(e -> log.error("Error while getting day forward process tracker by management process id {} and office id {}", managementProcessId, officeId, e))
                .onErrorResume(e -> Flux.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting day forward process tracker by management process id and office id")));
    }

    @Override
    public Mono<DayForwardProcessTrackerEntity> getDayForwardProcessByManagementProcessIdAndOfficeIdAndSamityId(String managementProcessId, String officeId, String samityId) {
        return dayForwardProcessTrackerRepository.findByManagementProcessIdAndOfficeIdAndSamityId(managementProcessId, officeId, samityId)
                .doOnRequest(req -> log.info("Getting day forward process tracker by management process id {}, office id {} and samity id {}", managementProcessId, officeId, samityId))
                .doOnSuccess(s -> log.info("Got day forward process tracker by management process id {}, office id {} and samity id {}", managementProcessId, officeId, samityId))
                .doOnError(e -> log.error("Error while getting day forward process tracker by management process id {}, office id {} and samity id {}", managementProcessId, officeId, samityId, e))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while getting day forward process tracker by management process id, office id and samity id")));
    }

    @Override
    public Mono<Void> deleteAllDataByManagementProcessId(String managementProcessId) {
        return dayForwardProcessTrackerRepository.deleteByManagementProcessId(managementProcessId)
                .doOnRequest(req -> log.info("Deleting day forward process tracker by management process id {}", managementProcessId))
                .doOnSuccess(s -> log.info("Deleted day forward process tracker by management process id {}", managementProcessId))
                .doOnError(e -> log.error("Error while deleting day forward process tracker by management process id {}", managementProcessId, e))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while deleting day forward process tracker by management process id")));
    }

    @Override
    public Mono<String> saveDayForwardProcessTrackerIntoHistory(List<DayForwardProcessTrackerHistoryEntity> historyEntityList) {
        return dayForwardProcessTrackerHistoryRepository
                .saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Day Forward Process Tracker history: {}", throwable.getMessage()))
                .map(list -> "Day Forward Process Tracker History Save Successful")
                ;
    }

    @Override
    public Mono<DayForwardProcessTrackerEntity> updateRescheduleStatusOfDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String rescheduleStatus) {
        dayForwardProcessTrackerEntity.setReschedulingStatus(rescheduleStatus);
        return dayForwardProcessTrackerRepository.updateRescheduleStatusBySamityIdAndManagementProcessId(dayForwardProcessTrackerEntity.getManagementProcessId(), dayForwardProcessTrackerEntity.getSamityId(), rescheduleStatus)
                .doOnRequest(req -> log.info("Updating reschedule status of day forward process tracker with status: {}", rescheduleStatus))
                .doOnSuccess(s -> log.info("Updated reschedule status of day forward process tracker {}", dayForwardProcessTrackerEntity))
                .thenReturn(dayForwardProcessTrackerEntity)
                .doOnError(e -> log.error("Error while updating reschedule status of day forward process tracker {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating reschedule status of day forward process tracker")));
    }

    @Override
    public Mono<Void> updateArchivingStatusOfDayForwardProcess(String officeId, String managementProcessId, String archivingStatus) {
        return dayForwardProcessTrackerRepository.updateArchivingStatusByOfficeIdAndManagementProcessId(managementProcessId, officeId, archivingStatus)
                .doOnRequest(req -> log.info("Updating archiving status of day forward process tracker with status: {}", archivingStatus))
                .doOnSuccess(s -> log.info("Updated archiving status of day forward process tracker with status: {}", archivingStatus))
                .doOnError(e -> log.error("Error while updating archiving status of day forward process tracker: {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating archiving status of day forward process tracker")));
    }

    @Override
    public Mono<DayForwardProcessTrackerEntity> updateStatusAndProcessEndTimeOfDayForwardProcess(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String status, LocalDateTime processEndTime) {
        dayForwardProcessTrackerEntity.setStatus(status);
        dayForwardProcessTrackerEntity.setProcessEndTime(processEndTime);
        return dayForwardProcessTrackerRepository.updateStatusAndProcessEndTimeBySamityIdAndManagementProcessId(dayForwardProcessTrackerEntity.getManagementProcessId(), dayForwardProcessTrackerEntity.getSamityId(), status, processEndTime)
                .doOnRequest(req -> log.info("Updating status of day forward process tracker with status: {}", status))
                .doOnSuccess(s -> log.info("Updated status of day forward process tracker {}", dayForwardProcessTrackerEntity))
                .thenReturn(dayForwardProcessTrackerEntity)
                .doOnError(e -> log.error("Error while updating status of day forward process tracker {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating status of day forward process tracker")));
    }

    @Override
    public Mono<DayForwardProcessTrackerEntity> updateDayForwardProcessByManagementProcessIdAndSamityId(DayForwardProcessTrackerEntity dayForwardProcessTrackerEntity, String status, String rescheduleStatus, String archiveStatus, String loginId, LocalDateTime retriedOn) {
        return dayForwardProcessTrackerRepository.updateDayForwardProcessTrackerByManagementProcessIdAndSamityId(dayForwardProcessTrackerEntity.getManagementProcessId(), dayForwardProcessTrackerEntity.getSamityId(), status, rescheduleStatus, archiveStatus, loginId, retriedOn)
                .doOnRequest(req -> log.info("Updating day forward process tracker with status: {}, reschedule status: {}, archive status: {}, login id: {}, retried on: {}", status, rescheduleStatus, archiveStatus, loginId, retriedOn))
                .doOnSuccess(s -> log.info("Successfully Updated day forward process tracker"))
                .thenReturn(dayForwardProcessTrackerEntity)
                .doOnError(e -> log.error("Error while updating day forward process tracker {}", e.getMessage()))
                .onErrorResume(e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while updating day forward process tracker")));
    }

}
