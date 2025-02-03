package net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.dto.JournalSnapshotCommand;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisJournalClientPort;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.IDataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveRequestDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardGridResponseDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardProcessRequestDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardProcessResponseDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.in.DayForwardProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.out.DayForwardProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.domain.DayForwardProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DayForwardProcessTrackerService implements DayForwardProcessTrackerUseCase {
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final HolidayUseCase holidayUseCase;
    private final CommonRepository commonRepository;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final DayForwardProcessTrackerPersistencePort dayForwardProcessTrackerPersistencePort;
    private final IDataArchiveUseCase dataArchiveUseCase;
    private final ModelMapper modelMapper;
    private final CalendarUseCase calendarUseCase;
    private final Gson gson;
    private final TransactionalOperator rxtx;
    private final AisJournalClientPort aisJournalClientPort;

    public DayForwardProcessTrackerService(ManagementProcessTrackerUseCase managementProcessTrackerUseCase, HolidayUseCase holidayUseCase, CommonRepository commonRepository, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, DayForwardProcessTrackerPersistencePort dayForwardProcessTrackerPersistencePort, IDataArchiveUseCase dataArchiveUseCase, ModelMapper modelMapper, CalendarUseCase calendarUseCase, Gson gson, TransactionalOperator rxtx, AisJournalClientPort aisJournalClientPort) {
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.holidayUseCase = holidayUseCase;
        this.commonRepository = commonRepository;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.dayForwardProcessTrackerPersistencePort = dayForwardProcessTrackerPersistencePort;
        this.dataArchiveUseCase = dataArchiveUseCase;
        this.modelMapper = modelMapper;
        this.calendarUseCase = calendarUseCase;
        this.gson = gson;
        this.rxtx = rxtx;
        this.aisJournalClientPort = aisJournalClientPort;
    }


    @Override
    public Mono<String> resetDayForwardProcessByOfficeId(String officeId) {
        return managementProcessTrackerUseCase
                .getLastManagementProcessIdForOffice(officeId)
                .flatMap(managementProcessId -> dayForwardProcessTrackerPersistencePort.getAllDayForwardTrackerDataByManagementProcessId(managementProcessId)
                    .collectList()
                    .flatMap(dayForwardProcessTrackerEntities -> {
                        boolean allEntriesWithStatusFinished = dayForwardProcessTrackerEntities
                                .stream()
                                .allMatch(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()));
                        return allEntriesWithStatusFinished
                                ? Mono.just("Day Forward process finished successfully. Cannot be Reset.")
                                : dayForwardProcessTrackerPersistencePort.deleteAllDataByManagementProcessId(managementProcessId)
                                .thenReturn("Day Forward Process Reset Successfully");
                    }));
    }

    public Mono<Void> rescheduleSamityDay(List<LocalDate> holidayDates, List<Samity> samityListFlux, String officeId, String loginId, String managementProcessId) {
        log.info("Holiday List for samity to reschedule: {}", holidayDates);
        Map<String, LocalDate> holidayDateMap = new HashMap<>();
        return Flux.fromIterable(holidayDates)
                .doOnRequest(l -> log.info("Rescheduling started for holiday dates: {}", holidayDates))
                .map(holidayDate -> {
                    if (!holidayDateMap.containsKey(holidayDate.getDayOfWeek().toString().toLowerCase())) {
                        holidayDateMap.put(holidayDate.getDayOfWeek().toString().toLowerCase(), holidayDate);
                    }
                    return holidayDate.getDayOfWeek().toString().toLowerCase();
                })
                .distinct()
                .collectList()
                .flatMap(samityHolidayList -> {
                    if(samityListFlux.isEmpty())
                        return commonRepository.getSamityByOfficeId(officeId)
                                .collectList()
                                .zipWith(Mono.just(samityHolidayList));
                    else
                        return Mono.just(samityListFlux).zipWith(Mono.just(samityHolidayList));
                })
                .flatMap(samityListAndSamityDayHolidayList ->
//                        commonRepository
//                        .getSamityByOfficeId(officeId)
                        Flux.fromIterable(samityListAndSamityDayHolidayList.getT1())
                                .collectList()
                                .flatMap(samityList -> {
                                    List<Samity> samityListToReschedule = samityList.stream().filter(samity -> samityListAndSamityDayHolidayList.getT2().contains(samity.getSamityDay().toLowerCase())).toList();
                                    List<Samity> samityListNotToReschedule = samityList.stream().filter(samity -> !samityListToReschedule.contains(samity)).toList();

                                    if (samityListNotToReschedule.isEmpty())
                                        return Mono.just(samityListToReschedule);
                                    else
                                        return updateRescheduleStatusForSamityWithoutHoliday(samityListNotToReschedule, officeId, managementProcessId)
                                                .then(Mono.just(samityListToReschedule));
                                }))
                .flatMapMany(Flux::fromIterable)
                .flatMap(samity -> commonRepository.getLoanAccountIdListBySamityIdAndStatus(samity.getSamityId(), Status.STATUS_ACTIVE.getValue())
                        .collectList()
                        .flatMap(loanAccountIdList -> loanRepaymentScheduleUseCase.rescheduleLoanRepayScheduleOnSamityCancel(loanAccountIdList, loginId, holidayDateMap.get(samity.getSamityDay().toLowerCase()))
                        )
                        .doOnRequest(l -> log.info("holiday day and date map {}", holidayDateMap))
                        .doOnNext(rescheduleResponse -> log.info("Rescheduled loan repay schedule on samity response: {}", rescheduleResponse))
                        .onErrorResume(e -> {
                            log.error("Error while rescheduling loan repay schedule on samity: {}", e.getMessage());
                            return updateDayForwardProcessTrackerAfterRescheduling(Status.STATUS_FAILED.getValue(), managementProcessId, officeId, samity.getSamityId())
                                    .then(Mono.empty());
                        })
                        .flatMap(rescheduleResponse -> updateDayForwardProcessTrackerAfterRescheduling(Status.STATUS_RESCHEDULED.getValue(), managementProcessId, officeId, samity.getSamityId()))
                )
                .doOnComplete(() -> log.info("Rescheduled samity day for office {}", officeId))
                .doOnError(throwable -> log.error("Error while rescheduling samity day for office {}", officeId, throwable))
                /*.subscribeOn(Schedulers.boundedElastic())
                .subscribe();*/
                .then();
    }


    @Override
    public Mono<DayForwardGridResponseDto> dayForwardProcessV2(DayForwardProcessRequestDto requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerRef = new AtomicReference<>();
        return managementProcessTrackerUseCase
                .getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> {
                    log.info("Management Process Id: {}", managementProcessTracker);
                    log.info("Office Id: {}", requestDTO.getOfficeId());
                    managementProcessTrackerRef.set(managementProcessTracker);
                    log.info("Management Process Id Ref: {}", managementProcessTrackerRef.get().getManagementProcessId());
                    return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                            .collectList()
                            .doOnNext(list -> log.debug("Office Event List: {}", list));
                })
                .filter(list -> !list.isEmpty() && list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is not completed for office: " + requestDTO.getOfficeId())))
                .flatMap(officeEventTrackers -> calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(requestDTO.getOfficeId(), managementProcessTrackerRef.get().getBusinessDate())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Last Working Day of Month is not found for office: " + requestDTO.getOfficeId())))
                        .flatMap(lastBusinessDate -> {
                            if (lastBusinessDate.isEqual(managementProcessTrackerRef.get().getBusinessDate())) {
                                return officeEventTrackers
                                        .stream()
                                        .map(OfficeEventTracker::getOfficeEvent)
                                        .anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))
                                            ? Mono.just(officeEventTrackers)
                                            : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process is not completed for office: " + requestDTO.getOfficeId()));
                            } else
                                return Mono.just(officeEventTrackers);
                        }))
                .flatMapMany(officeEventTrackers -> commonRepository.getSamityByOfficeId(requestDTO.getOfficeId()))
                .map(samity -> buildInitialDayForwardProcessTracker(samity, managementProcessTrackerRef.get().getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getLoginId()))
                .collectList()
                .flatMapMany(dayForwardProcessList -> dayForwardProcessTrackerPersistencePort.deleteAllDataByManagementProcessId(managementProcessTrackerRef.get().getManagementProcessId())
                        .thenMany(dayForwardProcessTrackerPersistencePort.saveAllDayForwardProcess(dayForwardProcessList)))
                .collectList()
//                .doOnNext(localDates -> archiveDataAndUpdateTracker(requestDTO, managementProcessTrackerRef.get().getManagementProcessId()))
                .flatMap(dayForwardProcessTrackerEntities -> Mono.deferContextual(contextView -> Mono.fromRunnable(() -> {
                            Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                            this.archiveDataAndUpdateTracker(requestDTO, managementProcessTrackerRef.get().getManagementProcessId())
                                    .contextWrite(context)
                                    .subscribeOn(Schedulers.immediate())
                                    .subscribe();
                        })
                        .thenReturn(dayForwardProcessTrackerEntities)))
                .flatMap(savedDayForwardTrackerList -> holidayUseCase.getAllHolidaysOfAnOfficeByManagementProcessId(managementProcessTrackerRef.get().getManagementProcessId(), requestDTO.getOfficeId())
                                .collectList()
                                .filter(holidayList -> !holidayList.isEmpty())
//                        .doOnNext(holidayList -> rescheduleSamityDay(holidayList, new ArrayList<>(), requestDTO.getOfficeId(), requestDTO.getLoginId(), managementProcessTrackerRef.get().getManagementProcessId()))
                                .flatMap(holidayList -> Mono.deferContextual(contextView -> Mono.fromRunnable(() -> {
                                            Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                            this.rescheduleSamityDay(holidayList, new ArrayList<>(), requestDTO.getOfficeId(), requestDTO.getLoginId(), managementProcessTrackerRef.get().getManagementProcessId())
                                                    .contextWrite(context)
                                                    .subscribeOn(Schedulers.immediate())
                                                    .subscribe();
                                        })
                                        .thenReturn(holidayList)))
                                .switchIfEmpty(Mono.defer(() -> updateRescheduleStatusForSamityWithoutHoliday(requestDTO.getOfficeId(), managementProcessTrackerRef.get().getManagementProcessId())
                                        .map(dayForwardProcessTrackerEntityList -> new ArrayList<>())))
                )
                .flatMapMany(holidayList -> dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessTrackerRef.get().getManagementProcessId(), requestDTO.getOfficeId()))
                .map(dayForwardTracker -> modelMapper.map(dayForwardTracker, DayForwardProcessTracker.class))
                .flatMap(dayForwardTracker -> commonRepository.getSamityBySamityId(dayForwardTracker.getSamityId())
                        .map(samity -> {
                            dayForwardTracker.setSamityNameEn(samity.getSamityNameEn());
                            dayForwardTracker.setSamityNameBn(samity.getSamityNameBn());
                            dayForwardTracker.setSamityDay(samity.getSamityDay());
                            return dayForwardTracker;
                        })
                )
                .collectList()
                .map(dayForwardTrackerList -> buildDayForwardGridViewResponse(dayForwardTrackerList, managementProcessTrackerRef.get(), dayForwardTrackerList.size()))
                .flatMap(this::setBtnStatusForForwardDayRoutineAndRevert)
                .flatMap(this::setRefershRetryAndConfirmBtnStatus)
                .doOnError(throwable -> log.error("Error while processing day forward for office {}", requestDTO.getOfficeId(), throwable))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while processing day forward for office")));
    }

    private JournalSnapshotCommand buildJournalSnapshotCommand(DayForwardProcessRequestDto dayForwardProcessRequestDto) {
        return JournalSnapshotCommand.builder()
                .managementProcessId(dayForwardProcessRequestDto.getManagementProcessId())
                .officeId(dayForwardProcessRequestDto.getOfficeId())
                .loginId(dayForwardProcessRequestDto.getLoginId())
                .businessDate(dayForwardProcessRequestDto.getBusinessDate())
                .mfiId(dayForwardProcessRequestDto.getMfiId())
                .build();
    }

    @Override
    public Mono<DayForwardGridResponseDto> refreshDayForwardProcess(DayForwardProcessRequestDto requestDto) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerRef = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .flatMapMany(managementProcessTracker -> {
                    managementProcessTrackerRef.set(managementProcessTracker);
                    return dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessTracker.getManagementProcessId(), requestDto.getOfficeId())
                            .flatMap(this::updateStatusDependingOnArchivingAndRescheduling);
                })
                .switchIfEmpty(Mono.just(DayForwardProcessTrackerEntity.builder().build()))
                .collectList()
                .flatMap(dayForwardProcessTrackerEntities ->
                        Flux.fromIterable(dayForwardProcessTrackerEntities)
                                .skip((long) requestDto.getLimit() * requestDto.getOffset())
                                .take(requestDto.getLimit())
                                .map(dayForwardTracker -> modelMapper.map(dayForwardTracker, DayForwardProcessTracker.class))
                                .flatMap(dayForwardTracker -> commonRepository.getSamityBySamityId(dayForwardTracker.getSamityId())
                                        .map(samity -> {
                                            dayForwardTracker.setSamityNameEn(samity.getSamityNameEn());
                                            dayForwardTracker.setSamityNameBn(samity.getSamityNameBn());
                                            dayForwardTracker.setSamityDay(samity.getSamityDay());
                                            return dayForwardTracker;
                                        }))
                                .collectList()
                                .map(dayForwardTrackerList -> buildDayForwardGridViewResponse(dayForwardTrackerList, managementProcessTrackerRef.get(), dayForwardProcessTrackerEntities.size())))
                .flatMap(this::setBtnStatusForForwardDayRoutineAndRevert)
                .flatMap(this::setRefershRetryAndConfirmBtnStatus);

    }

    @Override
    public Mono<DayForwardProcessResponseDto> confirmDayForwardProcess(DayForwardProcessRequestDto requestDto) {
        return rxtx.transactional(
                managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .flatMapMany(managementProcessTracker -> {
                    requestDto.setBusinessDate(managementProcessTracker.getBusinessDate().toString());
                    requestDto.setManagementProcessId(managementProcessTracker.getManagementProcessId());
                    return dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessTracker.getManagementProcessId(), requestDto.getOfficeId());
                })
                .collectList()
                .filter(dayForwardProcessTrackerEntityList -> dayForwardProcessTrackerEntityList.stream().allMatch(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day Forward Process is not completed for office: " + requestDto.getOfficeId())))
                .flatMap(dataList -> archiveDayForwardProcessTrackerData(requestDto.getManagementProcessId(), requestDto.getLoginId()))
                .flatMap(archiveResponse -> createNewManagementProcessForOfficeV2(requestDto.getManagementProcessId(), requestDto.getMfiId(), requestDto.getOfficeId(), requestDto.getLoginId()))
                .flatMap(dayForwardProcessTrackers -> aisJournalClientPort
                        .createJournalSnapshot(buildJournalSnapshotCommand(requestDto))
                        .doOnNext(journalSnapshotResponse -> log.info("Journal Snapshot Response : {}", journalSnapshotResponse))
                        .map(journalSnapshotResponse -> dayForwardProcessTrackers)
                        .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error while processing Journal Snapshot")))
                .doOnError(throwable -> log.error("Error while processing Journal Snapshot {}", throwable.getMessage()))
                .flatMap(s -> Mono.just(DayForwardProcessResponseDto.builder().userMessage("Day Forwarded Successfully.").build())));
    }

    @Override
    public Mono<DayForwardGridResponseDto> retryDayForwardProcess(DayForwardProcessRequestDto requestDto) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerRef = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .flatMapMany(managementProcessTracker -> {
                    managementProcessTrackerRef.set(managementProcessTracker);
                    return dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessTracker.getManagementProcessId(), requestDto.getOfficeId());
                })
                .filter(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getStatus().equals(Status.STATUS_FAILED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No failed day forward data found to retry" + requestDto.getOfficeId())))
                .collectList()
                .flatMap(failedTrackerEntity -> decideWhetherToRetryArchivingOrRescheduling(failedTrackerEntity, requestDto, managementProcessTrackerRef.get().getManagementProcessId()))
                .flatMapMany(retriedTrackerList -> dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessTrackerRef.get().getManagementProcessId(), requestDto.getOfficeId()))
                .map(dayForwardTracker -> modelMapper.map(dayForwardTracker, DayForwardProcessTracker.class))
                .flatMap(dayForwardTracker -> commonRepository.getSamityBySamityId(dayForwardTracker.getSamityId())
                        .map(samity -> {
                            dayForwardTracker.setSamityNameEn(samity.getSamityNameEn());
                            dayForwardTracker.setSamityNameBn(samity.getSamityNameBn());
                            dayForwardTracker.setSamityDay(samity.getSamityDay());
                            return dayForwardTracker;
                        })
                )
                .collectList()
                .map(dayForwardTrackerList -> buildDayForwardGridViewResponse(dayForwardTrackerList, managementProcessTrackerRef.get(), dayForwardTrackerList.size()))
                .flatMap(this::setBtnStatusForForwardDayRoutineAndRevert)
                .flatMap(this::setRefershRetryAndConfirmBtnStatus)
                .doOnError(throwable -> log.error("Error while retrying day forward for office {}", requestDto.getOfficeId(), throwable))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while retrying day forward for office")));
    }


    private Mono<List<DayForwardProcessTrackerEntity>> decideWhetherToRetryArchivingOrRescheduling(List<DayForwardProcessTrackerEntity> dayForwardProcessTrackerEntityList, DayForwardProcessRequestDto requestDto, String managementProcessId) {
        if(dayForwardProcessTrackerEntityList.stream().anyMatch(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getArchivingStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue()))){
            return Flux.fromIterable(dayForwardProcessTrackerEntityList)
//                    .map(dayForwardData -> {
//                        dayForwardData.setRetriedOn(LocalDateTime.now());
//                        dayForwardData.setRetriedBy(requestDto.getLoginId());
//                        dayForwardData.setStatus(Status.STATUS_RETRYING.getValue());
//                        dayForwardData.setArchivingStatus(Status.STATUS_RETRYING.getValue());
//                        return dayForwardData;
//                    })
//                    .collectList()
//                    .flatMapMany(dayForwardProcessTrackerPersistencePort::saveAllDayForwardProcess)
                    .flatMap(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerPersistencePort.updateDayForwardProcessByManagementProcessIdAndSamityId(dayForwardProcessTrackerEntity, Status.STATUS_RETRYING.getValue(), "", Status.STATUS_RETRYING.getValue(), requestDto.getLoginId(), LocalDateTime.now()))
                    .collectList()
//                    .doOnNext(dataList -> archiveDataAndUpdateTracker(requestDto, managementProcessId));
                    .flatMap(dayForwardProcessTrackerEntities -> Mono.deferContextual(contextView -> Mono.fromRunnable(() -> {
                                Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                this.archiveDataAndUpdateTracker(requestDto, managementProcessId)
                                        .contextWrite(context)
                                        .subscribeOn(Schedulers.immediate())
                                        .subscribe();
                            })
                            .thenReturn(dayForwardProcessTrackerEntities)));
        }

        if(dayForwardProcessTrackerEntityList.stream().anyMatch(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getReschedulingStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue()))){
            return Flux.fromIterable(dayForwardProcessTrackerEntityList)
                    .filter(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerEntity.getReschedulingStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue()))
//                    .flatMap(dayForwardEntity -> {
//                        dayForwardEntity.setRetriedOn(LocalDateTime.now());
//                        dayForwardEntity.setRetriedBy(requestDto.getLoginId());
//                        dayForwardEntity.setStatus(Status.STATUS_RETRYING.getValue());
//                        dayForwardEntity.setReschedulingStatus(Status.STATUS_RETRYING.getValue());
//
//                        return dayForwardProcessTrackerPersistencePort.saveDayForwardProcess(dayForwardEntity);
//                    })
                    .flatMap(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerPersistencePort.updateDayForwardProcessByManagementProcessIdAndSamityId(dayForwardProcessTrackerEntity, Status.STATUS_RETRYING.getValue(), Status.STATUS_RETRYING.getValue(), "", requestDto.getLoginId(), LocalDateTime.now()))
                    .map(DayForwardProcessTrackerEntity::getSamityId)
                    .flatMap(commonRepository::getSamityBySamityId)
                    .collectList()
                    .flatMap(samityList -> holidayUseCase.getAllHolidaysOfAnOfficeByManagementProcessId(managementProcessId, requestDto.getOfficeId())
                                    .collectList()
                                    .filter(holidayList -> !holidayList.isEmpty())
//                            .doOnNext(holidayList -> rescheduleSamityDay(holidayList, samityList, requestDto.getOfficeId(), requestDto.getLoginId(), managementProcessId))
                                    .flatMap(holidayList -> Mono.deferContextual(contextView -> Mono.fromRunnable(() -> {
                                                Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                                this.rescheduleSamityDay(holidayList, samityList, requestDto.getOfficeId(), requestDto.getLoginId(), managementProcessId)
                                                        .contextWrite(context)
                                                        .subscribeOn(Schedulers.immediate())
                                                        .subscribe();
                                            })
                                            .thenReturn(holidayList)))
                                    .switchIfEmpty(Mono.defer(() -> updateRescheduleStatusForSamityWithoutHoliday(requestDto.getOfficeId(), managementProcessId)
                                            .map(updatedTrackerEntityList -> new ArrayList<>())))
                                    .then(Mono.just(dayForwardProcessTrackerEntityList))
                    );
        }

        return Mono.just(dayForwardProcessTrackerEntityList);
    }


    private Mono<String> createNewManagementProcessForOfficeV2(String managementProcessId, String mfiId, String officeId, String loginId) {
        final String newManagementProcessId = UUID.randomUUID().toString();
        log.info("Office Id : {}, Management Process Id: {}", officeId, managementProcessId);
        return managementProcessTrackerUseCase.getCurrentBusinessDateForOffice(managementProcessId, officeId)
                .flatMap(currentBusinessDate -> calendarUseCase.getNextBusinessDateForOffice(officeId, currentBusinessDate))
                .flatMap(nextBusinessDate -> commonRepository.getOfficeEntityByOfficeId(officeId)
                        .flatMap(officeEntity -> managementProcessTrackerUseCase.insertManagementProcessV2(newManagementProcessId, mfiId, officeId, officeEntity.getOfficeNameEn(), officeEntity.getOfficeNameBn(), nextBusinessDate, loginId)))
                .flatMap(processTracker -> officeEventTrackerUseCase.insertOfficeEvent(processTracker.getManagementProcessId(), processTracker.getOfficeId(), OfficeEvents.DAY_STARTED.getValue(), processTracker.getCreatedBy(), UUID.randomUUID().toString()))
                .map(officeEventTracker -> newManagementProcessId);
    }


    private Mono<DayForwardProcessTrackerEntity> updateStatusDependingOnArchivingAndRescheduling(DayForwardProcessTrackerEntity dayForwardProcessTrackerData) {
        if (StringUtils.isNotBlank(dayForwardProcessTrackerData.getStatus()) && (dayForwardProcessTrackerData.getStatus().equalsIgnoreCase(Status.STATUS_PROCESSING.getValue()) || dayForwardProcessTrackerData.getStatus().equalsIgnoreCase(Status.STATUS_RETRYING.getValue()))) {
            if (dayForwardProcessTrackerData.getArchivingStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue()) || dayForwardProcessTrackerData.getReschedulingStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue())) {
//                dayForwardProcessTrackerData.setStatus(Status.STATUS_FAILED.getValue());
//                dayForwardProcessTrackerData.setProcessEndTime(LocalDateTime.now());
                return dayForwardProcessTrackerPersistencePort.updateStatusAndProcessEndTimeOfDayForwardProcess(dayForwardProcessTrackerData, Status.STATUS_FAILED.getValue(), LocalDateTime.now());
            } else if (dayForwardProcessTrackerData.getArchivingStatus().equalsIgnoreCase(Status.STATUS_ARCHIVED.getValue()) && dayForwardProcessTrackerData.getReschedulingStatus().equalsIgnoreCase(Status.STATUS_RESCHEDULED.getValue())) {
//                dayForwardProcessTrackerData.setStatus(Status.STATUS_FINISHED.getValue());
//                dayForwardProcessTrackerData.setProcessEndTime(LocalDateTime.now());
                return dayForwardProcessTrackerPersistencePort.updateStatusAndProcessEndTimeOfDayForwardProcess(dayForwardProcessTrackerData, Status.STATUS_FINISHED.getValue(), LocalDateTime.now());
            } else
                return Mono.just(dayForwardProcessTrackerData);
        } else
            return Mono.just(dayForwardProcessTrackerData);
    }

    private DayForwardGridResponseDto buildDayForwardGridViewResponse(List<DayForwardProcessTracker> dayForwardTrackerList, ManagementProcessTracker managementProcessTracker, int size) {
        return DayForwardGridResponseDto.builder()
                .mfiId(managementProcessTracker.getMfiId())
                .officeId(managementProcessTracker.getOfficeId())
                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                .businessDate(managementProcessTracker.getBusinessDate())
                .businessDay(managementProcessTracker.getBusinessDay())
                .data(dayForwardTrackerList)
                .totalCount(size)
                .build();
    }


    private Mono<DayForwardGridResponseDto> setRefershRetryAndConfirmBtnStatus(DayForwardGridResponseDto responseDTO) {
        return Mono.just(responseDTO)
                .flatMap(gridResponse -> {
                    if (!gridResponse.getData().isEmpty()) {
                        if (gridResponse.getData().stream().anyMatch(dayForwardProcessTracker -> dayForwardProcessTracker.getStatus().equals(Status.STATUS_PROCESSING.getValue()) || dayForwardProcessTracker.getStatus().equals(Status.STATUS_RETRYING.getValue()))) {
                            gridResponse.setBtnRefreshEnabled("Yes");
                            gridResponse.setBtnRetryEnabled("No");
                            gridResponse.setBtnConfirmEnabled("No");
                            gridResponse.setBtnRunForwardDayEnabled("No");
                        } else {
                            if (gridResponse.getData().stream().anyMatch(dayForwardProcessTracker -> dayForwardProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()))) {
                                gridResponse.setBtnRefreshEnabled("No");
                                gridResponse.setBtnRetryEnabled("Yes");
                                gridResponse.setBtnConfirmEnabled("No");
                                gridResponse.setBtnRunForwardDayEnabled("No");
                            } else {
                                gridResponse.setBtnRefreshEnabled("No");
                                gridResponse.setBtnRetryEnabled("No");
                                gridResponse.setBtnConfirmEnabled("Yes");
                                gridResponse.setBtnRunForwardDayEnabled("No");
                            }
                        }
                    } else {
                        gridResponse.setBtnRefreshEnabled("No");
                        gridResponse.setBtnRetryEnabled("No");
                        gridResponse.setBtnConfirmEnabled("No");
                    }

                    return Mono.just(gridResponse);
                });
    }


    private Mono<DayForwardGridResponseDto> setBtnStatusForForwardDayRoutineAndRevert(DayForwardGridResponseDto responseDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .doOnNext(officeEventList -> log.info("Office Id: {}, Office Event List: {}", responseDTO.getOfficeId(), officeEventList))
                .map(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        responseDTO.setBtnRunForwardDayEnabled("Yes");
                    } else {
                        responseDTO.setBtnRunForwardDayEnabled("No");
                    }
                    if (officeEventList.get(officeEventList.size() - 1).equals(OfficeEvents.DAY_STARTED.getValue())) {
                        responseDTO.setBtnRevertEnabled("Yes");
                    } else {
                        responseDTO.setBtnRevertEnabled("No");
                    }
                    return responseDTO;
                })
                .flatMap(this::checkIfBusinessDayIsLastWorkingDayAndSetBtnStatusOnMonthEndProcessCompletion);
    }


    private Mono<DayForwardGridResponseDto> checkIfBusinessDayIsLastWorkingDayAndSetBtnStatusOnMonthEndProcessCompletion(DayForwardGridResponseDto responseDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .flatMap(managementProcessTracker -> calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(responseDTO.getOfficeId(), managementProcessTracker.getBusinessDate())
                        .flatMap(lastWorkingDay -> {
                            if (lastWorkingDay.equals(managementProcessTracker.getBusinessDate())) {
                                return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
                                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                                        .map(OfficeEventTracker::getOfficeEvent)
                                        .collectList()
                                        .map(officeEventList -> {
                                            if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))) {
                                                responseDTO.setBtnRunForwardDayEnabled("Yes");
                                            } else {
                                                responseDTO.setBtnRunForwardDayEnabled("No");
                                            }
                                            return responseDTO;
                                        });
                            }
                            responseDTO.setBtnRunForwardDayEnabled(responseDTO.getBtnRunForwardDayEnabled());
                            return Mono.just(responseDTO);
                        }));
    }


    private Mono<List<Samity>> updateRescheduleStatusForSamityWithoutHoliday(List<Samity> samityList, String officeId, String managementProcessId) {
        return Flux.fromIterable(samityList)
                .flatMap(samity -> updateDayForwardProcessTrackerAfterRescheduling(Status.STATUS_RESCHEDULED.getValue(), managementProcessId, officeId, samity.getSamityId())
                        .thenReturn(samity))
                .collectList();
    }


    private Mono<List<DayForwardProcessTrackerEntity>> updateRescheduleStatusForSamityWithoutHoliday(String officeId, String managementProcessId) {
        return dayForwardProcessTrackerPersistencePort
                .getAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .flatMap(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerPersistencePort.updateRescheduleStatusOfDayForwardProcess(dayForwardProcessTrackerEntity, Status.STATUS_RESCHEDULED.getValue()))
                .collectList();
    }


    private Mono<Void> archiveDataAndUpdateTracker(DayForwardProcessRequestDto requestDTO, String ManagementProcessId) {
        return dataArchiveUseCase.archiveAndDeleteStagingDataForOffice(DataArchiveRequestDTO.builder()
                        .managementProcessId(ManagementProcessId)
                        .mfiId(requestDTO.getMfiId())
                        .loginId(requestDTO.getLoginId())
                        .officeId(requestDTO.getOfficeId())
                        .build())
                .onErrorResume(e -> {
                    log.error("Error while archiving data: {}", e.getMessage());
                    return updateDayForwardProcessTrackerAfterArchiving(Status.STATUS_FAILED.getValue(), ManagementProcessId, requestDTO.getOfficeId())
                            .then(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while archiving data")));
                })
                .doOnSuccess(res -> log.info("Data Archived Successfully: {}", res))
                .then(updateDayForwardProcessTrackerAfterArchiving(Status.STATUS_ARCHIVED.getValue(), ManagementProcessId, requestDTO.getOfficeId()))
                .then();
    }

    private Mono<Void> updateDayForwardProcessTrackerAfterArchiving(String archivingStatus, String managementProcessId, String officeId) {
//        return dayForwardProcessTrackerPersistencePort.getAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
//                .map(dayForwardProcessTrackerEntity -> {
//                    dayForwardProcessTrackerEntity.setArchivingStatus(archivingStatus);
//                    return dayForwardProcessTrackerEntity;
//                })
//                .flatMap(dayForwardProcessTrackerPersistencePort::saveDayForwardProcess)
//                .then();
        return dayForwardProcessTrackerPersistencePort.updateArchivingStatusOfDayForwardProcess(officeId, managementProcessId, archivingStatus)
                .doOnSuccess(res -> log.info("Day Forward Process Tracker Successfully Updated After Archiving: {}", res));
    }


    private Mono<Void> updateDayForwardProcessTrackerAfterRescheduling(String reschedulingStatus, String managementProcessId, String officeId, String samityId) {
        return dayForwardProcessTrackerPersistencePort.getDayForwardProcessByManagementProcessIdAndOfficeIdAndSamityId(managementProcessId, officeId, samityId)
//                .map(dayForwardProcessTrackerEntity -> {
//                    dayForwardProcessTrackerEntity.setReschedulingStatus(reschedulingStatus);
//                    return dayForwardProcessTrackerEntity;
//                })
                .flatMap(dayForwardProcessTrackerEntity -> dayForwardProcessTrackerPersistencePort.updateRescheduleStatusOfDayForwardProcess(dayForwardProcessTrackerEntity, reschedulingStatus))
                .then();
    }

    private DayForwardProcessTrackerEntity buildInitialDayForwardProcessTracker(Samity samity, String managementProcessId, String officeId, String loginId) {
        return DayForwardProcessTrackerEntity.builder()
                .managementProcessId(managementProcessId)
                .dayForwardProcessTrackerId(UUID.randomUUID().toString())
                .officeId(officeId)
                .samityId(samity.getSamityId())
                .status(Status.STATUS_PROCESSING.getValue())
                .archivingStatus(Status.STATUS_PROCESSING.getValue())
                .reschedulingStatus(Status.STATUS_PROCESSING.getValue())
//                .remarks("Day Forward Process Started")
                .processStartTime(LocalDateTime.now())
                .createdBy(loginId)
                .createdOn(LocalDateTime.now())
                .build();
    }


    private Mono<String> archiveDayForwardProcessTrackerData(String managementProcessId, String loginId) {
        return dayForwardProcessTrackerPersistencePort
                .getAllDayForwardTrackerDataByManagementProcessId(managementProcessId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No day forward tracking data found for archiving")))
                .collectList()
                .doOnNext(list -> log.info("Day Forward Process Tracker List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
//                            DayForwardProcessTrackerHistoryEntity historyEntity = gson.fromJson(item.toString(), DayForwardProcessTrackerHistoryEntity.class);
                            DayForwardProcessTrackerHistoryEntity historyEntity = modelMapper.map(item, DayForwardProcessTrackerHistoryEntity.class);
                            historyEntity.setArchivedOn(LocalDateTime.now());
                            historyEntity.setArchivedBy(loginId);
                            return historyEntity;
                        })
                        .toList())
                .flatMap(dayForwardProcessTrackerPersistencePort::saveDayForwardProcessTrackerIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .flatMap(responseDTO -> dayForwardProcessTrackerPersistencePort.deleteAllDataByManagementProcessId(managementProcessId).then(Mono.just("Day Forward Process Data Deleted")))
                .as(rxtx::transactional)
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .doOnError(e -> log.error("Error while archiving day forward process tracker data: {}", e.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while archiving day forward process tracker data")));
    }
}
