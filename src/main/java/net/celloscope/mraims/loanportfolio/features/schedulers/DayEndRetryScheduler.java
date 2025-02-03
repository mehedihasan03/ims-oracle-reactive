package net.celloscope.mraims.loanportfolio.features.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository.DayEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.DayEndProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request.DayEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.DayEndProcessResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DayEndRetryScheduler {

//    /mra-ims/mfi/process/api/v2/day-end-process/by-office/detail-view
//    /mra-ims/mfi/process/api/v2/day-end-process/by-office/auto-voucher/generate
//    /mra-ims/mfi/process/api/v2/day-end-process/by-office/auto-voucher/delete

    private final DayEndProcessTrackerRepository dayEndProcessTrackerRepository;
    private final CommonRepository commonRepository;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final DayEndProcessTrackerUseCase dayEndProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SchedulerHelperUtils schedulerHelperUtils;

    @Value("${retry.day-end-process-scheduler.enabled}")
    private String isRetryAutoVoucherGenerationProcessSchedulerEnabled;

    @Scheduled(fixedRate = 360000)
    public void retryAutoVoucherGenerationProcess() {
        if (!Boolean.parseBoolean(isRetryAutoVoucherGenerationProcessSchedulerEnabled)) {
            return;
        }
        schedulerHelperUtils.getActiveOfficeIdsForInstitute()
            .flatMap(officeId -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .flatMap(managementProcessTracker -> isReInitiateRequired(officeId, managementProcessTracker.getManagementProcessId())
                    .flatMap(isRequired -> {
                        if (isRequired) {
                            return deleteAutoVoucherGenerationProcess(officeId, managementProcessTracker.getManagementProcessId())
                                .then(runAutoVoucherGenerationForOffice(officeId));
                        } else {
                            return Mono.empty();
                        }
                    })
                )
                .onErrorContinue((throwable, o) -> log.error("Error processing office {}", officeId, throwable))
            )
            .subscribe();
    }

    private Mono<DayEndProcessResponseDTO> runAutoVoucherGenerationForOffice(String officeId) {
        return dayEndProcessTrackerUseCase.generateAutoVoucherForOffice(DayEndProcessRequestDTO
            .builder()
                .isScheduledRequest(true)
                .officeId(officeId)
            .build())
            .doOnRequest(l -> log.info("Auto voucher generation process restarted for office: {}", officeId))
            .doOnNext(response -> log.info("Auto voucher generation process initiated for office: {}", officeId))
            .onErrorMap(throwable -> {
                log.error("Error initiating auto voucher generation process for office: {} error: {}", officeId, throwable.getMessage());
                return throwable;
            })
            ;
    }

    private Mono<DayEndProcessResponseDTO> deleteAutoVoucherGenerationProcess(String officeId, String managementProcessId) {
        return dayEndProcessTrackerUseCase.deleteAutoVoucherGenerationForOffice(DayEndProcessRequestDTO
                .builder()
                    .isScheduledRequest(true)
                    .officeId(officeId)
                .build())
            .doOnRequest(l -> log.info("Auto voucher generation process deletion started for office: {} management process: {}", officeId, managementProcessId))
            .doOnNext(response -> log.info("Auto voucher generation process deleted for office: {} management process: {}", officeId, managementProcessId))
            .onErrorMap(throwable -> {
                log.error("Error deleting auto voucher generation process for office: {} management process: {}  error: {}", officeId, managementProcessId, throwable.getMessage());
                return throwable;
            });
    }

    private Mono<Boolean> isReInitiateRequired(String officeId, String managementProcessId) {
        return officeEventTrackerUseCase.getLastOfficeEventForOffice(managementProcessId, officeId)
            .flatMap(officeEventTracker -> {
                if (!officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())) {
                    return Mono.just(false);
                }
                return dayEndProcessTrackerRepository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                    .collectList()
                    .switchIfEmpty(Mono.just(new ArrayList<>()))
                    .flatMap(dayEndProcessTrackerEntities -> {
                        if (dayEndProcessTrackerEntities.isEmpty()) {
                            return Mono.just(false);
                        }
                        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
                        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

                        boolean allFinished = dayEndProcessTrackerEntities.stream().allMatch(tracker -> tracker.getStatus().equals(Status.STATUS_FINISHED.getValue()));
                        boolean anyActivityInLastOneMinute = dayEndProcessTrackerEntities.stream().anyMatch(tracker -> tracker.getStatus().equals(Status.STATUS_FINISHED.getValue()) && tracker.getProcessEndTime().isAfter(oneMinuteAgo));
                        boolean allActivityIsWaitingForLastFiveMinutes = dayEndProcessTrackerEntities.stream()
                            .allMatch(tracker -> tracker.getStatus().equals(Status.STATUS_WAITING.getValue()) && tracker.getCreatedOn().isBefore(fiveMinutesAgo));
                        boolean anyHaltedActivityInLastFiveMinutes = dayEndProcessTrackerEntities.stream().anyMatch(tracker -> tracker.getProcessStartTime().isBefore(fiveMinutesAgo) && tracker.getProcessEndTime() == null);

                        if (allFinished) {
                            log.info("office id {} management process id {} || All activities are finished", officeId, managementProcessId);
                            return Mono.just(false);
                        } else if (anyActivityInLastOneMinute) {
                            log.info("office id {} management process id {} || Activity found in last one minute", officeId, managementProcessId);
                            return Mono.just(false);
                        } else return Mono.just(allActivityIsWaitingForLastFiveMinutes || anyHaltedActivityInLastFiveMinutes);
                    });
            })
            .doOnRequest(l -> log.info("Checking re-initiate requirement for auto voucher generation for office: {} mangementprocessId : {} ", officeId, managementProcessId))
            .doOnNext(isRequired -> log.info(" office id {} management process id {} || Re-initiate of auto voucher generation is required: {}", officeId, managementProcessId, isRequired))
            .onErrorResume(throwable -> {
                log.error("office id {}, management process id {} ||Error checking re-initiate requirement for auto voucher generation: {}", officeId, managementProcessId, throwable.getMessage());
                return Mono.just(false);
            });
    }

}
