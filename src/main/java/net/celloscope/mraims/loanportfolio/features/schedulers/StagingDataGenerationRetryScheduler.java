package net.celloscope.mraims.loanportfolio.features.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.UserRoles;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataStatusByOfficeResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class StagingDataGenerationRetryScheduler {

    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final IStagingProcessTrackerRepository stagingProcessTrackerRepository;
    private final SchedulerHelperUtils schedulerHelperUtils;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;

    @Value("${retry.staging-data-generation-process-scheduler.enabled}")
    private String isRetryStagingDataGenerationProcessSchedulerEnabled;

    @Scheduled(fixedRate = 360000)
    public void retryStagingDataGenerationProcess() {
        if (!Boolean.parseBoolean(isRetryStagingDataGenerationProcessSchedulerEnabled)) {
            return;
        }
        schedulerHelperUtils.getActiveOfficeIdsForInstitute()
//            .delayElements(Duration.ofMinutes(1))
            .flatMap(officeId -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .flatMap(managementProcessTracker -> isReInitiateRequired(officeId, managementProcessTracker.getManagementProcessId())
                    .flatMap(isRequired -> {
                        if (isRequired) {
                            return deleteStagingDataGenerationProcess(officeId, managementProcessTracker.getManagementProcessId())
                                .then(runStagingDataGenerationForOffice(officeId));
                        } else {
                            return Mono.empty();
                        }
                    })
                )
                .onErrorContinue((throwable, o) -> log.error("Error processing office {}", officeId, throwable))
            )
//            .collectList()
            .subscribe();
    }

    private Mono<StagingDataStatusByOfficeResponseDTO> runStagingDataGenerationForOffice(String officeId) {
        return stagingDataUseCase.generateStagingDataByOffice(StagingDataRequestDTO
                .builder()
                .isScheduledRequest(true)
                .officeId(officeId)
                .loginId("scheduler")
                .userRole(UserRoles.MFI_BRANCH_MANAGER.getValue())
                .build())
            .doOnRequest(l -> log.info("Staging Data generation process restarted for office: {}", officeId))
            .doOnNext(response -> log.info("Staging Data generation process initiated for office: {}", officeId))
            .onErrorMap(throwable -> {
                log.error("Error initiating Staging Data generation process for office: {} error: {}", officeId, throwable.getMessage());
                return throwable;
            })
            ;
    }

    private Mono<StagingDataStatusByOfficeResponseDTO> deleteStagingDataGenerationProcess(String officeId, String managementProcessId) {
        return stagingDataUseCase.deleteStagingDataByOffice(StagingDataRequestDTO
                .builder()
                .isScheduledRequest(true)
                .officeId(officeId)
                .loginId("scheduler")
                .build())
            .doOnRequest(l -> log.info("Staging Data generation process deletion started for office: {} management process: {}", officeId, managementProcessId))
            .doOnNext(response -> log.info("Staging Data generation process deleted for office: {} management process: {}", officeId, managementProcessId))
            .onErrorMap(throwable -> {
                log.error("Error deleting Staging Data generation process for office: {} management process: {}  error: {}", officeId, managementProcessId, throwable.getMessage());
                return throwable;
            });
    }

    private Mono<Boolean> isReInitiateRequired(String officeId, String managementProcessId) {
        return officeEventTrackerUseCase.getLastOfficeEventForOffice(managementProcessId, officeId)
            .flatMap(officeEventTracker -> {
                if (!officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_STARTED.getValue())) {
                    return Mono.just(false);
                }
                return stagingProcessTrackerRepository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                    .collectList()
                    .switchIfEmpty(Mono.just(new ArrayList<>()))
                    .flatMap(stagingProcessTrackerEntities -> {
                        if (stagingProcessTrackerEntities.isEmpty()) {
                            return Mono.just(false);
                        }
                        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
                        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

                        boolean allFinished = stagingProcessTrackerEntities.stream()
                            .allMatch(tracker -> tracker.getStatus().equals(Status.STATUS_FINISHED.getValue()));
                        boolean anyActivityInLastOneMinute = stagingProcessTrackerEntities.stream()
                            .anyMatch(tracker -> tracker.getStatus().equals(Status.STATUS_FINISHED.getValue()) && tracker.getProcessEndTime().isAfter(oneMinuteAgo));
                        boolean allActivityIsWaitingForLastFiveMinutes = stagingProcessTrackerEntities.stream()
                            .allMatch(tracker -> tracker.getStatus().equals(Status.STATUS_WAITING.getValue()) && tracker.getCreatedOn().isBefore(fiveMinutesAgo));
                        boolean anyHaltedActivityInLastFiveMinutes = stagingProcessTrackerEntities.stream()
                            .anyMatch(tracker -> !tracker.getStatus().equals(Status.STATUS_WAITING.getValue()) && tracker.getProcessStartTime().isBefore(fiveMinutesAgo) && tracker.getProcessEndTime() == null);

                        if (allFinished) {
                            log.info("office id {} management process id {} || All activities are finished", officeId, managementProcessId);
                            return Mono.just(false);
                        } else if (anyActivityInLastOneMinute) {
                            log.info("office id {} management process id {} || Activity found in last one minute", officeId, managementProcessId);
                            return Mono.just(false);
                        } else return Mono.just(allActivityIsWaitingForLastFiveMinutes || anyHaltedActivityInLastFiveMinutes);
                    });
            })
            .doOnRequest(l -> log.info("Checking re-initiate requirement for Staging Data generation for office: {} mangementprocessId : {} ", officeId, managementProcessId))
            .doOnNext(isRequired -> log.info(" office id {} management process id {} || Re-initiate of Staging Data generation is required: {}", officeId, managementProcessId, isRequired))
            .onErrorResume(throwable -> {
                log.error("office id {}, management process id {} ||Error checking re-initiate requirement for Staging Data generation: {}", officeId, managementProcessId, throwable.getMessage());
                return Mono.just(false);
            });
    }
}
