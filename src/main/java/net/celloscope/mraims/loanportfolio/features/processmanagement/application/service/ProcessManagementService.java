package net.celloscope.mraims.loanportfolio.features.processmanagement.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.AccountingUseCase;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.IDataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveRequestDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.DayEndProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.DayEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.*;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.request.ProcessTrackerRequestDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import org.modelmapper.ModelMapper;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProcessManagementService implements ProcessManagementUseCase {
	
	private final ManagementProcessTrackerUseCase managementProcessUseCase;
	private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
	private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final AccountingUseCase accountingUseCase;
	private final CommonRepository commonRepository;
	private final IStagingDataUseCase stagingDataUseCase;
	private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
	private final IWithdrawStagingDataUseCase withdrawStagingDataUseCase;
	private final CalendarUseCase calendarUseCase;
	private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
	private final IDataArchiveUseCase dataArchiveUseCase;
	private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
    private final ModelMapper modelMapper;
	private final Gson gson;
	private final TransactionalOperator rxtx;
	
	public ProcessManagementService(ManagementProcessTrackerUseCase managementProcessTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, DayEndProcessTrackerUseCase dayEndProcessTrackerUseCase, AccountingUseCase accountingUseCase, CommonRepository commonRepository, IStagingDataUseCase stagingDataUseCase1, CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase, IWithdrawStagingDataUseCase withdrawStagingDataUseCase, CalendarUseCase calendarUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, IDataArchiveUseCase dataArchiveUseCase, IStagingDataUseCase stagingDataUseCase, DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort, ModelMapper modelMapper, TransactionalOperator rxtx) {
		this.managementProcessUseCase = managementProcessTrackerUseCase;
		this.officeEventTrackerUseCase = officeEventTrackerUseCase;
		this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.accountingUseCase = accountingUseCase;
		this.commonRepository = commonRepository;
        this.stagingDataUseCase = stagingDataUseCase1;
        this.collectionStagingDataQueryUseCase = collectionStagingDataQueryUseCase;
		this.withdrawStagingDataUseCase = withdrawStagingDataUseCase;
		this.calendarUseCase = calendarUseCase;
		this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
		this.dataArchiveUseCase = dataArchiveUseCase;
        this.dayEndProcessTrackerPersistencePort = dayEndProcessTrackerPersistencePort;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
	}
	
	@Override
	public Mono<DayEndProcessResponseDTO> runDayEndProcessForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.map(managementProcessTracker -> DayEndProcessResponseDTO.builder()
						.managementProcessId(managementProcessTracker.getManagementProcessId())
						.mfiId(requestDTO.getMfiId())
						.officeId(managementProcessTracker.getOfficeId())
						.businessDate(managementProcessTracker.getBusinessDate())
						.businessDay(managementProcessTracker.getBusinessDay())
						.dayEndProcessRunBy(requestDTO.getLoginId())
						.build())
				.flatMap(this::validateIfForwardDayRoutineIsRunnableAndUpdateDB)
				.flatMap(this::checkAndGetAisJournalForCollection)
				.flatMap(this::checkAndGetAisJournalForWithdraw)
				.flatMap(responseDTO -> officeEventTrackerUseCase.insertOfficeEvent(responseDTO.getManagementProcessId(), responseDTO.getOfficeId(), OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue(), responseDTO.getDayEndProcessRunBy(), UUID.randomUUID().toString())
						.map(officeEventTracker -> responseDTO))
				.map(responseDTO -> {
					responseDTO.setUserMessage("Day End Process Completed Successfully for office: " + responseDTO.getOfficeId());
					return responseDTO;
				});
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> runMonthEndProcessForOffice(ProcessTrackerRequestDTO requestDTO) {
		return null;
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> runForwardDayRoutineForOffice(ProcessTrackerRequestDTO requestDTO) {
//		@TODO: check for first time forward day routine run
		log.info("Forward Day Routine Process Started");
		return managementProcessUseCase.getLastManagementProcessIdForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcessId -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessId, requestDTO.getOfficeId())
						.collectList()
						.doOnNext(list -> log.debug("Office Event List: {}", list)))
				.filter(list -> list.size() == 3 && list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is not completed for office: " + requestDTO.getOfficeId())))
				.flatMap(list -> dataArchiveUseCase.archiveAndDeleteStagingDataForOffice(DataArchiveRequestDTO.builder()
								.managementProcessId(list.get(0).getManagementProcessId())
								.mfiId(requestDTO.getMfiId())
								.loginId(requestDTO.getLoginId())
								.officeId(requestDTO.getOfficeId())
								.build()))
				.flatMap(responseDTO -> this.createNewManagementProcessForOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId(), responseDTO.getArchivedBy()))
				.flatMap(newManagementProcessId -> managementProcessUseCase.getCurrentBusinessDateForOffice(newManagementProcessId, requestDTO.getOfficeId())
						.map(localDate -> ProcessTrackerResponseDTO.builder()
								.managementProcessId(newManagementProcessId)
								.officeId(requestDTO.getOfficeId())
								.businessDate(localDate)
								.businessDay(localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
								.userMessage("Forward Day Routine Successfully Run For Office: " + requestDTO.getOfficeId() + " with new Management Process Id: " + newManagementProcessId)
								.build())
				)
//				.map(newManagementProcessId -> ProcessTrackerResponseDTO.builder()
//						.userMessage("Forward Day Routine Successfully Run For Office: " + requestDTO.getOfficeId() + " with new Management Process Id: " + newManagementProcessId)
//						.build())
				.doOnError(throwable -> log.error("Failed to Run Forward Day Routine: {}", throwable.getMessage()))
				.doOnSuccess(processTrackerResponseDTO -> log.info("Forward Day routine Completed: {}", processTrackerResponseDTO));
	}
	
	@Override
	public Mono<ProcessDashboardOfOfficeResponseDTO> gridViewOfOfficeProcessDashboardForMfi(ProcessTrackerRequestDTO requestDTO) {
		return commonRepository.getOfficeListByMfi(requestDTO.getMfiId(), requestDTO.getLimit(), requestDTO.getLimit() * requestDTO.getOffset())
				.map(officeEntity -> modelMapper.map(officeEntity, OfficeProcessDTO.class))
				.flatMap(officeProcessDTO -> managementProcessUseCase.getLastManagementProcessForOffice(officeProcessDTO.getOfficeId())
						.map(managementProcessTracker -> {
							officeProcessDTO.setManagementProcessId(managementProcessTracker.getManagementProcessId());
							officeProcessDTO.setBusinessDate(managementProcessTracker.getBusinessDate());
							officeProcessDTO.setBusinessDay(managementProcessTracker.getBusinessDay());
							return officeProcessDTO;
						}))
				.flatMap(this::getOfficeEventsForProcessDashBoardByOffice)
				.flatMap(officeProcessDTO -> this.getDaysLaggingOfOfficeFromBusinessDay(officeProcessDTO.getOfficeId(), officeProcessDTO.getBusinessDate())
						.map(daysLagging -> {
							officeProcessDTO.setDaysLagging(daysLagging);
							return officeProcessDTO;
						}))
				.collectList()
				.map(list -> {
					list.sort(Comparator.comparing(OfficeProcessDTO::getOfficeId));
					return list;
				})
				.flatMap(officeProcessDTOList -> commonRepository.getTotalCountOfOfficeByMfi(requestDTO.getMfiId())
						.map(totalCount -> ProcessDashboardOfOfficeResponseDTO.builder()
								.mfiId(requestDTO.getMfiId())
								.data(officeProcessDTOList)
								.totalCount(totalCount)
								.build()))
				.doOnNext(processDashboardOfOfficeResponseDTO -> log.info("ProcessDashboardOfOfficeResponseDTO: {}", processDashboardOfOfficeResponseDTO));
	}
	
	@Override
	public Mono<ProcessDashboardOfSamityResponseDTO> gridViewOfSamityProcessDashboardForOffice(ProcessTrackerRequestDTO requestDTO) {
		AtomicLong totalCount = new AtomicLong(0);
		AtomicReference<String> isAllSamityCompletedOrCanceled = new AtomicReference<>();
		
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnNext(managementProcessTracker -> log.debug("managementProcessTracker: {}", managementProcessTracker))
				.flatMap(managementProcessTracker -> this.getSamityProcessListForProcessDashBoard(managementProcessTracker)
//						.skip((long) requestDTO.getLimit() * requestDTO.getOffset())
//						.take(requestDTO.getLimit())
						.collectList()
						.map(list -> {
							totalCount.set(list.size());
							isAllSamityCompletedOrCanceled.set(checkIfAllSamityIsCompletedOrCanceledForOffice(list));
							return list.stream().skip((long) requestDTO.getLimit() * requestDTO.getOffset()).limit(requestDTO.getLimit()).collect(Collectors.toList());
						})
						.doOnNext(samityProcessDTOList -> log.debug("samityProcessDTOList: {}", samityProcessDTOList))
						.map(samityProcessDTOList -> {
							samityProcessDTOList.sort(Comparator.comparing(SamityProcessDTO::getSamityId));
							return samityProcessDTOList;
						})
						.map(samityProcessDTOList -> Tuples.of(managementProcessTracker, samityProcessDTOList)))
				.map(tuple -> ProcessDashboardOfSamityResponseDTO.builder()
						.managementProcessId(tuple.getT1().getManagementProcessId())
						.businessDate(tuple.getT1().getBusinessDate())
						.businessDay(tuple.getT1().getBusinessDay())
						.officeId(requestDTO.getOfficeId())
						.data(tuple.getT2())
						.totalCount(totalCount.get())
						.build())
				.flatMap(dto -> this.getDaysLaggingOfOfficeFromBusinessDay(dto.getOfficeId(), dto.getBusinessDate())
						.map(daysLagging -> {
							dto.setDaysLagging(daysLagging);
							return dto;
						}))
				.flatMap(dto -> this.getOfficeEventsForProcessDashBoardByOffice(gson.fromJson(dto.toString(), OfficeProcessDTO.class))
						.map(officeProcessDTO -> {
							dto.setIsDayStarted(officeProcessDTO.getIsDayStarted());
							dto.setIsStagingDataGenerated(officeProcessDTO.getIsStagingDataGenerated());
							dto.setIsDayEndProcessCompleted(officeProcessDTO.getIsDayEndProcessCompleted());
							return dto;
						}))
				.flatMap(dto -> commonRepository.getOfficeEntityByOfficeId(dto.getOfficeId())
						.map(officeEntity -> {
							dto.setOfficeNameEn(officeEntity.getOfficeNameEn());
							dto.setOfficeNameBn(officeEntity.getOfficeNameBn());
							return dto;
						}))
				.map(dto -> {
					dto.setIsDayEndProcessRunnable(isAllSamityCompletedOrCanceled.get().equals("Yes") && dto.getIsDayEndProcessCompleted().equals("No") ? "Yes" : "No");
					dto.setIsForwardDayRoutineRunnable(dto.getIsDayEndProcessCompleted().equals("Yes") && dto.getIsDayEndProcessRunnable().equals("No") ? "Yes" : "No");
					return dto;
				})
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Office Id not found")))
				.doOnNext(processDashboardOfSamityResponseDTO -> log.info("processDashboardOfSamityResponseDTO: {}", processDashboardOfSamityResponseDTO));
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> getCurrentProcessManagementForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.map(tracker -> ProcessTrackerResponseDTO.builder()
						.managementProcessId(tracker.getManagementProcessId())
						.officeId(tracker.getOfficeId())
						.businessDate(tracker.getBusinessDate())
						.businessDay(tracker.getBusinessDay())
						.userMessage("Process Created By: " + tracker.getCreatedBy())
						.build());
	}

	@Override
	public Mono<ForwardDayGridViewResponseDTO> gridViewOfForwardDayRoutineForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getAllManagementProcessForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcessTrackerList -> {
					List<ManagementProcessTracker> paginatedList = managementProcessTrackerList.stream()
							.skip((long) requestDTO.getLimit() * requestDTO.getOffset())
							.limit(requestDTO.getLimit())
							.toList();
					return this.buildForwardDayRoutineResponse(paginatedList, managementProcessTrackerList.size());
				})
//				.flatMap(this::buildForwardDayRoutineResponse)
				.flatMap(this::setBtnStatusForForwardDayRoutineGridView)
				.doOnSuccess(response -> log.info("Grid View Of Forward Day Routine Response: {}", response))
				.doOnError(throwable -> log.error("Error in Grid View Of Forward Day Routine Response: {}", throwable.getMessage()));
	}

	@Override
	public Mono<SamityCancelGridViewResponseDTO> gridViewOfSamityCancelForOffice(ProcessTrackerRequestDTO requestDTO) {
		final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>(null);
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnNext(managementProcess::set)
				.flatMap(this::getCancelableSamityList)
				.map(cancelableSamityList -> SamityCancelGridViewResponseDTO.builder()
						.officeId(managementProcess.get().getOfficeId())
						.officeNameEn(managementProcess.get().getOfficeNameEn())
						.officeNameBn(managementProcess.get().getOfficeNameBn())
						.businessDate(managementProcess.get().getBusinessDate())
						.businessDay(managementProcess.get().getBusinessDay())
						.data(cancelableSamityList)
						.totalCount(cancelableSamityList.size())
						.build())
				.flatMap(samityCancelGridViewResponseDTO -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcess.get().getManagementProcessId(), managementProcess.get().getOfficeId())
						.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
						.map(OfficeEventTracker::getOfficeEvent)
						.collectList()
						.map(officeEventList -> {
							if(officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))){
								samityCancelGridViewResponseDTO.getData().clear();
								samityCancelGridViewResponseDTO.setTotalCount(0);
							}
							return samityCancelGridViewResponseDTO;
						}))
				.doOnSuccess(response -> log.info("Grid View Of Cancel Samity Response: {}", response))
				.doOnError(throwable -> log.error("Error in Grid View Of Cancel Samity Response: {}", throwable.getMessage()));
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> cancelRegularSamityListForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcess -> this.validateAndCancelSamityListForOffice(managementProcess, requestDTO))
				.map(managementProcess -> ProcessTrackerResponseDTO.builder()
						.officeId(managementProcess.getOfficeId())
						.officeNameEn(managementProcess.getOfficeNameEn())
						.officeNameBn(managementProcess.getOfficeNameBn())
						.businessDate(managementProcess.getBusinessDate())
						.businessDay(managementProcess.getBusinessDay())
						.samityIdList(requestDTO.getSamityIdList())
						.userMessage("Samity Cancellation Process Is Successful ")
						.build())
				.doOnSuccess(response -> log.info("Cancel Samity Response: {}", response))
				.doOnError(throwable -> log.error("Error in Cancel Samity Response: {}", throwable.getMessage()));
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> deleteRegularSamityListCancellationForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcess -> this.validateIfSamityListIsAlreadyCanceledForOfficeAndDeleteCancellation(managementProcess, requestDTO))
				.map(managementProcess -> ProcessTrackerResponseDTO.builder()
						.officeId(managementProcess.getOfficeId())
						.officeNameEn(managementProcess.getOfficeNameEn())
						.officeNameBn(managementProcess.getOfficeNameBn())
						.businessDate(managementProcess.getBusinessDate())
						.businessDay(managementProcess.getBusinessDay())
						.samityIdList(requestDTO.getSamityIdList())
						.userMessage("Samity Cancellation Process is Deleted Successfully ")
						.build())
				.doOnSuccess(response -> log.info("Delete Samity Cancellation Response: {}", response))
				.doOnError(throwable -> log.error("Error in Delete Samity Cancellation Response: {}", throwable.getMessage()));
	}

	private Mono<ManagementProcessTracker> validateIfSamityListIsAlreadyCanceledForOfficeAndDeleteCancellation(ManagementProcessTracker managementProcessTracker, ProcessTrackerRequestDTO requestDTO) {
		return this.validateOfficeEventsForSamityCancellation(managementProcessTracker)
				.flatMapIterable(managementProcessId -> requestDTO.getSamityIdList())
				.flatMap(samityId -> this.validateSamityForDeleteOfCancellationProcessAndDeleteSamityEvent(managementProcessTracker.getManagementProcessId(), samityId))
				.collectList()
				.map(response -> managementProcessTracker);
	}

	private Mono<String> validateSamityForDeleteOfCancellationProcessAndDeleteSamityEvent(String managementProcessId, String samityId) {
		return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
				.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
				.map(SamityEventTracker::getSamityEvent)
				.collectList()
				.filter(samityEventlist -> !samityEventlist.isEmpty() && samityEventlist.contains(SamityEvents.CANCELED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id: " + samityId + " is not Canceled, Delete of Samity Cancellation Request Failed")))
				.flatMap(samityEventTracker -> samityEventTrackerUseCase.deleteSamityEventTrackerByEventList(managementProcessId, samityId, List.of(SamityEvents.CANCELED.getValue())))
				.map(deletedSamityEventList -> samityId);
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> runForwardDayRoutineForOfficeV2(ProcessTrackerRequestDTO requestDTO) {
//		@TODO: check for first time forward day routine run
		log.info("Forward Day Routine Process Started");
		AtomicReference<String> mfiId = new AtomicReference<>();
		AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
		return managementProcessUseCase
				.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnSuccess(managementProcessTracker -> log.info("Last ManagementProcessTracker Found"))
				.doOnError(throwable -> log.error("Failed to get Last ManagementProcessTracker: {}", throwable.getMessage()))
				.doOnNext(managementProcessTrackerAtomicReference::set)
				.doOnNext(managementProcessTracker -> mfiId.set(managementProcessTracker.getMfiId()))
				.map(ManagementProcessTracker::getManagementProcessId)
				.flatMap(managementProcessId -> officeEventTrackerUseCase
						.getAllOfficeEventsForOffice(managementProcessId, requestDTO.getOfficeId())
						.collectList()
						.doOnNext(list -> log.debug("Office Event List: {}", list)))
				.filter(list -> !list.isEmpty() && list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is not completed for office: " + requestDTO.getOfficeId())))
				.flatMap(list -> dataArchiveUseCase.archiveAndDeleteStagingDataForOffice(DataArchiveRequestDTO.builder()
						.managementProcessId(managementProcessTrackerAtomicReference.get().getManagementProcessId())
						.mfiId(mfiId.get())
						.loginId(requestDTO.getLoginId())
						.officeId(requestDTO.getOfficeId())
						.build()))
				.flatMap(responseDTO -> this.createNewManagementProcessForOfficeV2(managementProcessTrackerAtomicReference.get().getManagementProcessId(), mfiId.get(), requestDTO.getOfficeId(), requestDTO.getLoginId()))
				.flatMap(newManagementProcessId -> managementProcessUseCase.getCurrentBusinessDateForOffice(newManagementProcessId, requestDTO.getOfficeId())
						.map(localDate -> ProcessTrackerResponseDTO.builder()
								.managementProcessId(newManagementProcessId)
								.officeId(requestDTO.getOfficeId())
								.businessDate(localDate)
								.businessDay(localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
								.userMessage("Forward Day Routine Successfully Run For Office: " + requestDTO.getOfficeId())
								.build())
				)
//				.map(newManagementProcessId -> ProcessTrackerResponseDTO.builder()
//						.userMessage("Forward Day Routine Successfully Run For Office: " + requestDTO.getOfficeId() + " with new Management Process Id: " + newManagementProcessId)
//						.build())
				.doOnError(throwable -> log.error("Failed to Run Forward Day Routine: {}", throwable.getMessage()))
				.doOnSuccess(processTrackerResponseDTO -> log.info("Forward Day routine Completed: {}", processTrackerResponseDTO));
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> revertForwardDayRoutineForOfficeV2(ProcessTrackerRequestDTO requestDTO) {
		return this
				.getLastManagementProcessIdForDeletingDayForwardRoutine(requestDTO.getOfficeId())
				.flatMap(dataArchiveUseCase::revertArchiveDataAndDeleteHistoryForDayForwardRoutine)
				.as(this.rxtx::transactional)
				.doOnRequest(l -> log.info("Request for Delete Forward Day Routine Process to Data Archive Use Case"))
				.doOnSuccess(s -> log.info("Delete Forward Day Routine Process Completed Successfully"))
				.doOnError(throwable -> log.error("Error in Delete Forward Day Routine Process: {}", throwable.getMessage()))
				.thenReturn(ProcessTrackerResponseDTO.builder()
						.userMessage("Delete Forward Day Routine Process Completed Successfully")
						.build())
				;

	}

	private Mono<String> getLastManagementProcessIdForDeletingDayForwardRoutine(String officeId) {
		return managementProcessUseCase
				.getLastManagementProcessIdForOffice(officeId)
				.flatMap(managementProcessId ->
						managementProcessUseCase
						.deleteManagementProcessForOfficeByManagementProcessId(managementProcessId, officeId)
								.thenReturn(managementProcessId)
				)
				.flatMap(managementProcessId ->
						officeEventTrackerUseCase
								.getLastOfficeEventForOffice(managementProcessId, officeId)
								.flatMap(officeEventTrackerUseCase::deleteOfficeEventTracker)
				)
				.flatMap(s -> managementProcessUseCase
						.getLastManagementProcessIdForOffice(officeId)
				)
				.doOnSuccess(s -> log.info("Last Management Process Id for Deleting Day Forward Routine"))
				.doOnError(throwable -> log.error("Error in getting Last Management Process Id for Deleting Day Forward Routine: {}", throwable.getMessage()))
				;
	}

	@Override
	public Mono<ProcessTrackerResponseDTO> getCreateTransactionButtonStatusForOffice(ProcessTrackerRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.map(managementProcessTracker -> ProcessTrackerResponseDTO.builder()
						.managementProcessId(managementProcessTracker.getManagementProcessId())
						.officeId(managementProcessTracker.getOfficeId())
						.officeNameEn(managementProcessTracker.getOfficeNameEn())
						.officeNameBn(managementProcessTracker.getOfficeNameBn())
						.businessDate(managementProcessTracker.getBusinessDate())
						.businessDay(managementProcessTracker.getBusinessDay())
						.build())
				.flatMap(this::setCreateTransactionBtnStatusForOffice)
				.doOnError(throwable -> log.error("Failed to get Create Transaction Button Status: {}", throwable.getMessage()))
				.doOnSuccess(processTrackerResponseDTO -> log.info("Create Transaction Button Status response: {}", processTrackerResponseDTO));
	}

	private Mono<ProcessTrackerResponseDTO> setCreateTransactionBtnStatusForOffice(ProcessTrackerResponseDTO responseDTO) {
		return officeEventTrackerUseCase.getAllOfficeEventsForOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
				.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
				.map(OfficeEventTracker::getOfficeEvent)
				.collectList()
				.flatMap(officeEventList -> dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
						.filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
						.collectList()
						.map(dayEndProcessTrackerList -> Tuples.of(officeEventList, dayEndProcessTrackerList)))
				.map(tuples -> {
					List<String> officeEventList = tuples.getT1();
					List<DayEndProcessTracker> dayEndProcessTrackerList = tuples.getT2();
					if(officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())){
						responseDTO.setBtnCreateTransactionEnabled("No");
						responseDTO.setUserMessage("Day End Process (AIS) is Already Completed for Office, Transaction Service is Not Available");
					} else if(officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())){
						responseDTO.setBtnCreateTransactionEnabled("No");
						responseDTO.setUserMessage("Day End Process (MIS) is Already Completed for Office, Transaction Service is Not Available");
					} else if(!dayEndProcessTrackerList.isEmpty()){
						responseDTO.setBtnCreateTransactionEnabled("No");
						responseDTO.setUserMessage("Day End Process (MIS) is Running for Office, Transaction Service is Not Available");
					} else if(officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())){
						responseDTO.setBtnCreateTransactionEnabled("Yes");
						responseDTO.setUserMessage("Staging Data is Generated for Office, Transaction Service is Available");
					} else {
						responseDTO.setBtnCreateTransactionEnabled("No");
						responseDTO.setUserMessage("Staging Data is Not Generated yet for Office, Transaction Service is Not Available");
					}
					responseDTO.setManagementProcessId(null);
					return responseDTO;
				});
	}

	private Mono<String> createNewManagementProcessForOfficeV2(String managementProcessId, String mfiId, String officeId, String loginId) {
		final String newManagementProcessId = UUID.randomUUID().toString();
		log.info("Office Id : {}, Management Process Id: {}", officeId, managementProcessId);
		return managementProcessUseCase.getCurrentBusinessDateForOffice(managementProcessId, officeId)
				.flatMap(currentBusinessDate -> calendarUseCase.getNextBusinessDateForOffice(officeId, currentBusinessDate))
				.flatMap(nextBusinessDate -> commonRepository.getOfficeEntityByOfficeId(officeId)
						.flatMap(officeEntity -> managementProcessUseCase.insertManagementProcessV2(newManagementProcessId, mfiId, officeId, officeEntity.getOfficeNameEn(), officeEntity.getOfficeNameBn(), nextBusinessDate, loginId)))
				.flatMap(processTracker -> officeEventTrackerUseCase.insertOfficeEvent(processTracker.getManagementProcessId(), processTracker.getOfficeId(), OfficeEvents.DAY_STARTED.getValue(), processTracker.getCreatedBy(), UUID.randomUUID().toString()))
				.map(officeEventTracker -> newManagementProcessId);
	}

	private Mono<ManagementProcessTracker> validateAndCancelSamityListForOffice(ManagementProcessTracker managementProcess, ProcessTrackerRequestDTO requestDTO) {
		return this.validateOfficeEventsForSamityCancellation(managementProcess)
				.flatMap(managementProcessId -> this.validateSamityCancelListForStagingProcess(managementProcess.getManagementProcessId(), requestDTO.getSamityIdList(), managementProcess.getBusinessDay()))
				.flatMap(aBoolean -> this.validateSamityCancelListForSamityEvent(managementProcess.getManagementProcessId(), requestDTO.getSamityIdList()))
				.flatMap(aBoolean -> samityEventTrackerUseCase.insertSamityListForAnEvent(managementProcess.getManagementProcessId(), managementProcess.getOfficeId(), requestDTO.getSamityIdList(), requestDTO.getLoginId(), SamityEvents.CANCELED.getValue(), requestDTO.getRemarks()))
				.map(samityEventTrackerList -> managementProcess);
	}

	private Mono<String> validateOfficeEventsForSamityCancellation(ManagementProcessTracker managementProcess) {
		return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcess.getManagementProcessId(), managementProcess.getOfficeId())
				.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
				.map(OfficeEventTracker::getOfficeEvent)
				.collectList()
				.doOnNext(officeEventList -> log.info("Office Id: {}, Office Event List: {}", managementProcess.getOfficeId(), officeEventList))
				.filter(officeEventList -> officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Generation is Not Completed for Office")))
				.filter(officeEventList -> officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed for Office")))
				.flatMap(officeEventList -> this.checkIfDayEndProcessIsRunningForOffice(managementProcess.getManagementProcessId(), managementProcess.getOfficeId()));
	}

	private Mono<String> checkIfDayEndProcessIsRunningForOffice(String managementProcessId, String officeId) {
		return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessId, officeId)
				.filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
				.collectList()
				.filter(List::isEmpty)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Running for Office")))
				.map(list -> managementProcessId);
	}

	private Mono<Boolean> validateSamityCancelListForStagingProcess(String managementProcessId, List<String> samityIdList, String businessDay){
		return stagingDataUseCase.getStagingProcessTrackerListBySamityIdList(managementProcessId, samityIdList)
				.doOnNext(stagingProcessTrackerList -> log.info("Samity Id List Requested: {}, Samity Id List From Staging Process Tracker: {}", samityIdList, stagingProcessTrackerList.stream().map(StagingProcessTrackerEntity::getSamityId).toList()))
				.filter(stagingProcessTrackerList -> !stagingProcessTrackerList.isEmpty() && new HashSet<>(stagingProcessTrackerList.stream().map(StagingProcessTrackerEntity::getSamityId).toList()).containsAll(samityIdList))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Cancellation Process Request is Not Valid")))
				.filter(stagingProcessTrackerList -> stagingProcessTrackerList.stream().allMatch(stagingProcessTracker -> stagingProcessTracker.getSamityDay().equals(businessDay)))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Cancellation Process Request is Not Valid, Only Regular Samity Can be Canceled")))
				.filter(stagingProcessTrackerList -> stagingProcessTrackerList.stream().noneMatch(stagingProcessTracker -> stagingProcessTracker.getIsDownloaded().equals("Yes")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Cancellation Process Request is Not Valid, Samity Data is Already Downloaded")))
				.doOnNext(string -> log.info("Samity Cancellation Validation Successful From Staging Process"))
				.map((stagingProcessTrackerEntityList -> true));
	}

	private Mono<Boolean> validateSamityCancelListForSamityEvent(String managementProcessId, List<String> samityIdList) {
		return Flux.fromIterable(samityIdList)
				.flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
						.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
						.map(SamityEventTracker::getSamityEvent)
						.collectList()
						.map(samityEventList -> Tuples.of(samityId, samityEventList)))
				.doOnNext(tuple -> log.info("Samity Id: {}, Samity Event List: {}", tuple.getT1(), tuple.getT2()) )
				.filter(tuple -> tuple.getT2().isEmpty())
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Cancellation Request Is Not Valid, Samity Transaction Already Happened")))
				.collectList()
				.doOnNext(string -> log.info("Samity Cancellation Validation Successful From SamityEvent Tracker"))
				.map(samityId -> true);
	}

	private Mono<List<CancelableSamity>> getCancelableSamityList(ManagementProcessTracker managementProcessTracker) {
		return stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
				.collectList()
				.map(stagingProcessTrackerEntityList -> {
					if(!stagingProcessTrackerEntityList.isEmpty()){
						return stagingProcessTrackerEntityList.stream()
								.map(stagingProcessTrackerEntity -> gson.fromJson(stagingProcessTrackerEntity.toString(), CancelableSamity.class))
								.filter(cancelableSamity -> cancelableSamity.getSamityDay().equals(managementProcessTracker.getBusinessDay()))
								.toList();
					}
					return (List<CancelableSamity>) new ArrayList<CancelableSamity>();
				})
				.flatMap(samityList -> {
					if(!samityList.isEmpty()){
						return samityEventTrackerUseCase.getAllSamityEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
								.map(samityEventTrackerList -> this.buildCancelableSamityListForGridView(samityList, samityEventTrackerList, managementProcessTracker.getBusinessDay()));
					}
					return Mono.just(samityList);
				})
				.flatMap(cancelableSamityList -> this.setBtnStatusForSamityCancellationForDayEndProcess(managementProcessTracker, cancelableSamityList));
	}


	private Mono<List<CancelableSamity>> setBtnStatusForSamityCancellationForDayEndProcess(ManagementProcessTracker managementProcessTracker, List<CancelableSamity> samityList) {
		if(samityList.isEmpty()){
			return Mono.just(samityList);
		}
		return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
				.filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
				.collectList()
				.map(dayEndProcessTrackerList -> {
					if(!dayEndProcessTrackerList.isEmpty()){
						samityList.forEach(cancelableSamity -> {
							cancelableSamity.setBtnCancelEnabled("No");
							cancelableSamity.setBtnDeleteEnabled("No");
						});
					}
					return samityList;
				});
	}

	private List<CancelableSamity> buildCancelableSamityListForGridView(List<CancelableSamity> samityList, List<SamityEventTracker> samityEventTrackerList, String businessDay) {
		return  samityList.stream()
				.peek(cancelableSamity -> {
					AtomicReference<String> samityCancelRemarks = new AtomicReference<>();
					List<String> samityEvents = samityEventTrackerList.stream()
							.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) && !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityId()) && samityEventTracker.getSamityId().equals(cancelableSamity.getSamityId()))
							.peek(samityEventTracker -> {
								if(samityEventTracker.getSamityEvent().equals(SamityEvents.CANCELED.getValue())){
									samityCancelRemarks.set(samityEventTracker.getRemarks());
								}
							})
							.map(SamityEventTracker::getSamityEvent)
							.toList();
					this.setSamityAndBtnStatusForCancelableSamity(cancelableSamity, samityEvents, businessDay, samityCancelRemarks.get());
				})
				.filter(cancelableSamity -> !HelperUtil.checkIfNullOrEmpty(cancelableSamity.getSamityId()))
				.sorted(Comparator.comparing(CancelableSamity::getSamityId))
				.peek(cancelableSamity -> log.info("Samity Id: {}, Status: {}, Cancelable: {}", cancelableSamity.getSamityId(), cancelableSamity.getStatus(), cancelableSamity.getBtnCancelEnabled()))
				.toList();
	}

	private void setSamityAndBtnStatusForCancelableSamity(CancelableSamity cancelableSamity, List<String> samityEvents, String businessDay, String samityCancelRemarks) {
		log.info("Samity Id: {}, Samity Day: {}, Business Day: {}, Samity Event List: {}", cancelableSamity.getSamityId(), cancelableSamity.getSamityDay(), businessDay, samityEvents);
		if(!samityEvents.isEmpty()){
			if(samityEvents.contains(SamityEvents.AUTHORIZED.getValue())){
				cancelableSamity.setStatus("Samity Authorized");
				cancelableSamity.setBtnDeleteEnabled("No");
			} else if(samityEvents.contains(SamityEvents.CANCELED.getValue())){
				cancelableSamity.setStatus("Samity Canceled");
				cancelableSamity.setBtnDeleteEnabled("Yes");
				cancelableSamity.setRemarks(samityCancelRemarks);
			} else if(samityEvents.contains(SamityEvents.COLLECTED.getValue()) || samityEvents.contains(SamityEvents.WITHDRAWN.getValue()) || samityEvents.contains(SamityEvents.LOAN_ADJUSTED.getValue())){
				cancelableSamity.setStatus("Samity Authorization Pending");
				cancelableSamity.setBtnDeleteEnabled("No");
			}
			cancelableSamity.setBtnCancelEnabled("No");
		} else {
			if(cancelableSamity.getSamityDay().equals(businessDay)){
				cancelableSamity.setStatus("Samity Staging Data Generated");
				cancelableSamity.setBtnCancelEnabled("Yes");
				cancelableSamity.setBtnDeleteEnabled("No");
			} else {
				cancelableSamity.setSamityId(null);
			}
		}
	}

	private Mono<ForwardDayGridViewResponseDTO> setBtnStatusForForwardDayRoutineGridView(ForwardDayGridViewResponseDTO responseDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
				.flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
						.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
						.map(OfficeEventTracker::getOfficeEvent)
						.collectList())
				.doOnNext(officeEventList -> log.info("Office Id: {}, Office Event List: {}", responseDTO.getOfficeId(), officeEventList))
				.map(officeEventList -> {
					if(officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))){
						responseDTO.setBtnRunForwardDayEnabled("Yes");
					} else {
						responseDTO.setBtnRunForwardDayEnabled("No");
					}
					if(officeEventList.get(officeEventList.size() - 1).equals(OfficeEvents.DAY_STARTED.getValue())) {
						responseDTO.setBtnDeleteEnabled("Yes");
					} else {
						responseDTO.setBtnDeleteEnabled("No");
					}
					return responseDTO;
				})
				.flatMap(this::checkIfBusinessDayIsLastWorkingDayAndSetBtnStatusOnMonthEndProcessCompletion);
	}


	private Mono<ForwardDayGridViewResponseDTO> checkIfBusinessDayIsLastWorkingDayAndSetBtnStatusOnMonthEndProcessCompletion(ForwardDayGridViewResponseDTO responseDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
				.flatMap(managementProcessTracker -> calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(responseDTO.getOfficeId(), managementProcessTracker.getBusinessDate())
						.flatMap(lastWorkingDay -> {
							if(lastWorkingDay.equals(managementProcessTracker.getBusinessDate())){
								return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
										.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
										.map(OfficeEventTracker::getOfficeEvent)
										.collectList()
										.map(officeEventList -> {
											if(officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))){
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

	private Mono<ForwardDayGridViewResponseDTO> buildForwardDayRoutineResponse(List<ManagementProcessTracker> managementProcessTrackerList, int size) {
		ManagementProcessTracker latestManagementProcessTracker = managementProcessTrackerList.stream().findFirst().get();
//		List<BusinessDayResponseDTO> businessDayResponseDTOList = managementProcessTrackerList.stream()
//				.map(managementProcessTracker -> BusinessDayResponseDTO.builder()
//						.businessDate(managementProcessTracker.getBusinessDate())
//						.businessDay(managementProcessTracker.getBusinessDay())
//						.dayStartedOn(managementProcessTracker.getCreatedOn())
//						.btnDeleteEnabled("No")
//						.build())
//				.toList();

//		return ForwardDayGridViewResponseDTO.builder()
//				.mfiId(latestManagementProcessTracker.getMfiId())
//				.officeId(latestManagementProcessTracker.getOfficeId())
//				.officeNameEn(latestManagementProcessTracker.getOfficeNameEn())
//				.officeNameBn(latestManagementProcessTracker.getOfficeNameBn())
//				.businessDate(latestManagementProcessTracker.getBusinessDate())
//				.businessDay(latestManagementProcessTracker.getBusinessDay())
//				.data(businessDayResponseDTOList)
//				.totalCount(businessDayResponseDTOList.size())
//				.build();
		return this.getTransactionAmountForBusinessDayResponse(managementProcessTrackerList)
				.map(businessDayResponseDTOList -> ForwardDayGridViewResponseDTO.builder()
						.mfiId(latestManagementProcessTracker.getMfiId())
						.officeId(latestManagementProcessTracker.getOfficeId())
						.officeNameEn(latestManagementProcessTracker.getOfficeNameEn())
						.officeNameBn(latestManagementProcessTracker.getOfficeNameBn())
						.businessDate(latestManagementProcessTracker.getBusinessDate())
						.businessDay(latestManagementProcessTracker.getBusinessDay())
						.data(businessDayResponseDTOList)
						.totalCount(size)
						.build());
	}

	private Mono<List<BusinessDayResponseDTO>> getTransactionAmountForBusinessDayResponse(List<ManagementProcessTracker> managementProcessTrackerList){
		Map<String, List<BigDecimal>> managementProcessTotalAmountMap = new HashMap<>();
		List<BigDecimal> totalAmount = new ArrayList<>();
		return Flux.fromIterable(managementProcessTrackerList)
				.doOnNext(managementProcessTracker -> managementProcessTotalAmountMap.put(managementProcessTracker.getManagementProcessId(), new ArrayList<>()))
				.doOnNext(managementProcessTracker -> log.info("Current Management Process Id: {}", managementProcessTracker.getManagementProcessId()))
				.flatMap(managementProcessTracker -> commonRepository.getTotalAmountByTransactionCodeForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), TransactionCodes.LOAN_REPAY.getValue())
						.switchIfEmpty(Mono.just(0.00))
						.flatMap(totalLoanCollection -> commonRepository.getTotalAmountByTransactionCodeForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), TransactionCodes.SAVINGS_DEPOSIT.getValue())
								.switchIfEmpty(Mono.just(0.00))
								.map(totalSavingsCollection -> totalLoanCollection + totalSavingsCollection))
						.map(totalCollection -> {
							managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).add(BigDecimal.valueOf(totalCollection));
							totalAmount.add(BigDecimal.valueOf(totalCollection));
							return managementProcessTracker;
						}))
				.flatMap(managementProcessTracker -> commonRepository.getTotalAmountByTransactionCodeForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(),TransactionCodes.SAVINGS_WITHDRAW.getValue())
						.switchIfEmpty(Mono.just(0.00))
						.map(totalWithdraw -> {
							managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).add(BigDecimal.valueOf(totalWithdraw));
							totalAmount.add(BigDecimal.valueOf(totalWithdraw));
							return managementProcessTracker;
						}))
				.flatMap(managementProcessTracker -> commonRepository.getTotalAmountByTransactionCodeForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), TransactionCodes.LOAN_DISBURSEMENT.getValue())
						.switchIfEmpty(Mono.just(0.00))
						.map(totalDisbursement -> {
							managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).add(BigDecimal.valueOf(totalDisbursement));
							totalAmount.add(BigDecimal.valueOf(totalDisbursement));
							return managementProcessTracker;
						}))
//				.flatMap(managementProcessTracker -> commonRepository.getTotalAmountByTransactionCodeForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), TransactionCodes.LOAN_ADJUSTMENT.getValue())
//						.switchIfEmpty(Mono.just(0.00))
//						.map(totalLoanAdjustment -> {
//		managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).add(BigDecimal.valueOf(totalCollection));
//							totalAmount.add(BigDecimal.valueOf(totalLoanAdjustment));
//							return managementProcessTracker;
//						}))
				.map(managementProcessTracker -> BusinessDayResponseDTO.builder()
						.businessDate(managementProcessTracker.getBusinessDate())
						.businessDay(managementProcessTracker.getBusinessDay())
						.dayStartedOn(managementProcessTracker.getCreatedOn())
						.totalCollection(managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).get(0))
						.totalWithdraw(managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).get(1))
						.totalDisbursement(managementProcessTotalAmountMap.get(managementProcessTracker.getManagementProcessId()).get(2))
//						.totalLoanAdjustment(totalAmount.get(3))
						.btnDeleteEnabled("No")
						.build())
				.sort(Comparator.comparing(BusinessDayResponseDTO::getBusinessDate))
				.collectList()
				.doOnNext(businessDayResponseDTOList -> Collections.reverse(businessDayResponseDTOList));
	}

	//	Process Management V2
	private Mono<Integer> getDaysLaggingOfOfficeFromBusinessDay(String officeId, LocalDate businessDate){
		LocalDate currentDate = LocalDate.now();
		if(businessDate != null && businessDate.isBefore(currentDate)){
			return commonRepository.getTotalDaysBetweenBusinessDateAndCurrentDateByOffice(officeId, businessDate, currentDate)
					.flatMap(totalDays -> commonRepository.getTotalHolidaysBetweenBusinessDateAndCurrentDateByOffice(officeId, businessDate, currentDate)
							.map(holidays -> -(totalDays - holidays - 1)));
		} else if(businessDate != null &&  businessDate.isAfter(currentDate)){
			return Mono.just(1);
		}
		return Mono.just(0);
	}

	private Mono<DayEndProcessResponseDTO> validateIfForwardDayRoutineIsRunnableAndUpdateDB(DayEndProcessResponseDTO responseDTO){
		return Mono.just(responseDTO)
				.flatMap(this::validateIfDayEndProcessIsAlreadyCompleted)
				.flatMap(this::validateRegularSamityForDayEndProcess)
				.flatMap(this::validateSpecialSamityForDayEndProcess)
				.flatMap(this::rescheduleLoanRepayScheduleOnSamityCancel);
	}

	private Mono<DayEndProcessResponseDTO> checkAndGetAisJournalForCollection(DayEndProcessResponseDTO responseDTO){
		return commonRepository.getTotalCollectionAmountByOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
				.switchIfEmpty(Mono.just(0.0))
				.flatMap(totalAmount -> {
					if(totalAmount > 0.0){
						return accountingUseCase.getAccountingJournalBody(AccountingRequestDTO
								.builder()
								.managementProcessId(responseDTO.getManagementProcessId())
								.loginId(responseDTO.getDayEndProcessRunBy())
								.officeId(responseDTO.getOfficeId())
								.mfiId(responseDTO.getMfiId())
								.processName(AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION.getValue())
								.build())
								.map(aisResponse -> responseDTO);
					}
					return Mono.just(responseDTO);
				});
	}

	private Mono<DayEndProcessResponseDTO> checkAndGetAisJournalForWithdraw(DayEndProcessResponseDTO responseDTO){
		return commonRepository.getTotalWithdrawAmountByOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
				.switchIfEmpty(Mono.just(0.0))
				.flatMap(totalAmount -> {
					if(totalAmount > 0.0){
						return accountingUseCase.getAccountingJournalBody(AccountingRequestDTO
										.builder()
										.managementProcessId(responseDTO.getManagementProcessId())
										.loginId(responseDTO.getDayEndProcessRunBy())
										.officeId(responseDTO.getOfficeId())
										.mfiId(responseDTO.getMfiId())
										.processName(AisMetaDataEnum.PROCESS_NAME_WITHDRAW.getValue())
										.build())
								.map(aisResponse -> responseDTO);
					}
					return Mono.just(responseDTO);
				});
	}

	private String checkIfAllSamityIsCompletedOrCanceledForOffice(List<SamityProcessDTO> samityProcessList){
		AtomicInteger count = new AtomicInteger();

		samityProcessList.forEach(item -> {
			if(item.getIsCanceled().equals("Yes")){
				count.getAndIncrement();
			} else {
				if (item.getIsCollectionCompleted().equals("Yes") && item.getIsCollectionPassbookCompleted().equals("Yes")){
					count.getAndIncrement();
				}
				if(item.getIsWithdrawCompleted().equals("Yes") && item.getIsWithdrawPassbookCompleted().equals("Yes")){
					count.getAndIncrement();
				}
			}
		});
		return samityProcessList.size() == count.get() ? "Yes" : "No";
	}

	private Mono<String> createNewManagementProcessForOffice(String managementProcessId, String officeId, String loginId) {
		final String newManagementProcessId = UUID.randomUUID().toString();
		
		return managementProcessUseCase.getCurrentBusinessDateForOffice(managementProcessId, officeId)
				.flatMap(currentBusinessDate -> calendarUseCase.getNextBusinessDateForOffice(officeId, currentBusinessDate))
				.flatMap(nextBusinessDate -> managementProcessUseCase.insertManagementProcess(newManagementProcessId, officeId, nextBusinessDate, loginId))
				.flatMap(processTracker -> officeEventTrackerUseCase.insertOfficeEvent(processTracker.getManagementProcessId(), processTracker.getOfficeId(), OfficeEvents.DAY_STARTED.getValue(), processTracker.getCreatedBy(), UUID.randomUUID().toString()))
				.map(officeEventTracker -> newManagementProcessId);
	}
	
	private Mono<OfficeProcessDTO> getOfficeEventsForProcessDashBoardByOffice(OfficeProcessDTO officeProcessDTO) {
		if (officeProcessDTO.getManagementProcessId() == null) {
			return Mono.just(officeProcessDTO);
		} else {
			return officeEventTrackerUseCase.getAllOfficeEventsForOffice(officeProcessDTO.getManagementProcessId(), officeProcessDTO.getOfficeId())
					.collectList()
					.map(list -> {
						officeProcessDTO.setIsDayStarted(
								list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_STARTED.getValue())) ? "Yes" : "No"
						);
						officeProcessDTO.setIsStagingDataGenerated(
								list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())) ? "Yes" : "No"
						);
						officeProcessDTO.setIsDayEndProcessCompleted(
								list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())) ? "Yes" : "No"
						);
						return officeProcessDTO;
					});
		}
		
		
	}
	
	private Flux<SamityProcessDTO> getSamityProcessListForProcessDashBoard(ManagementProcessTracker managementProcessTracker) {
		return commonRepository.getSamityEntityListByOffice(managementProcessTracker.getOfficeId())
				.map(samity -> modelMapper.map(samity, SamityProcessDTO.class))
				.doOnNext(samityProcessDTO -> log.debug("samityProcessDTO: {}", samityProcessDTO))
				.flatMap(samityProcessDTO -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(), samityProcessDTO.getSamityId())
						.collectList()
						.doOnNext(samityEventTrackerList -> log.debug("samityEventTrackerList: {}", samityEventTrackerList))
						.map(samityEventTrackerList -> Tuples.of(samityProcessDTO, samityEventTrackerList)))
				.doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
				.filter(tuple -> tuple.getT1().getSamityDay().equals(managementProcessTracker.getBusinessDay()) || tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getManagementProcessId())))
				.map(tuple -> {
//					Set Collection Parameters
					tuple.getT1().setIsCollectionCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.COLLECTED.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsCollectionAuthorizationCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsCollectionTransactionCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.TRANSACTION_COMPLETED.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsCollectionPassbookCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.PASSBOOK_COMPLETED.getValue())) ? "Yes" : "No"
					);
//					Set Withdraw Parameters
					tuple.getT1().setIsWithdrawCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsWithdrawAuthorizationCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsWithdrawTransactionCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.TRANSACTION_COMPLETED.getValue())) ? "Yes" : "No"
					);
					tuple.getT1().setIsWithdrawPassbookCompleted(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.PASSBOOK_COMPLETED.getValue())) ? "Yes" : "No"
					);
//					Set Cancellation Parameters
					tuple.getT1().setIsCanceled(
							tuple.getT2().stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.CANCELED.getValue())) ? "Yes" : "No"
					);
					tuple.getT2().forEach(item -> {
						if (!HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.CANCELED.getValue())) {
							tuple.getT1().setRemarks(item.getRemarks());
						}
					});
					return tuple.getT1();
				})
				.flatMap(samityProcessDTO -> {
					if(!HelperUtil.checkIfNullOrEmpty(samityProcessDTO.getFieldOfficerId())){
						return commonRepository.getFieldOfficerByFieldOfficerId(samityProcessDTO.getFieldOfficerId())
								.map(fieldOfficerEntity -> {
									samityProcessDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
									samityProcessDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
									return samityProcessDTO;
								});
					}
					return Mono.just(samityProcessDTO);

				})
				.flatMap(this::getCollectionTypeForSamity)
				.flatMap(this::getWithdrawTypeForSamity);
	}

	private Mono<SamityProcessDTO> getWithdrawTypeForSamity(SamityProcessDTO samityProcessDTO) {
		if(!HelperUtil.checkIfNullOrEmpty(samityProcessDTO.getIsWithdrawCompleted()) && samityProcessDTO.getIsWithdrawCompleted().equals("Yes")){
			return withdrawStagingDataUseCase.getAllWithdrawStagingDataBySamity(samityProcessDTO.getSamityId())
					.map(list -> {
						if(list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getIsUploaded()) && item.getIsUploaded().equals("Yes"))){
							samityProcessDTO.setWithdrawType("Offline");
							samityProcessDTO.setIsWithdrawDownloaded("Yes");
							samityProcessDTO.setIsWithdrawUploaded("Yes");
						} else{
							samityProcessDTO.setWithdrawType("Online");
						}
						return samityProcessDTO;
					});
		}
		return Mono.just(samityProcessDTO);
	}

	private Mono<SamityProcessDTO> getCollectionTypeForSamity(SamityProcessDTO samityProcessDTO) {
		if(!HelperUtil.checkIfNullOrEmpty(samityProcessDTO.getIsCollectionCompleted()) && samityProcessDTO.getIsCollectionCompleted().equals("Yes")){
			return collectionStagingDataQueryUseCase.getAllCollectionStagingDataBySamity(samityProcessDTO.getSamityId())
					.map(list -> {
						if(list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getIsUploaded()) && item.getIsUploaded().equals("Yes"))){
							samityProcessDTO.setCollectionType("Offline");
							samityProcessDTO.setIsCollectionDownloaded("Yes");
							samityProcessDTO.setIsCollectionUploaded("Yes");
						} else{
							samityProcessDTO.setCollectionType("Online");
						}
						return samityProcessDTO;
					});
		}
		return Mono.just(samityProcessDTO);
	}

	private Mono<DayEndProcessResponseDTO> validateIfDayEndProcessIsAlreadyCompleted(DayEndProcessResponseDTO responseDTO){
		return officeEventTrackerUseCase.getAllOfficeEventsForOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
				.collectList()
				.filter(officeEventTrackerList -> officeEventTrackerList.stream().noneMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Already Completed For office: " + responseDTO.getOfficeId())))
				.map(officeEventTrackerList -> responseDTO);
	}

	private Mono<DayEndProcessResponseDTO> validateRegularSamityForDayEndProcess(DayEndProcessResponseDTO responseDTO){
		return commonRepository.getSamityByOfficeId(responseDTO.getOfficeId())
				.filter(samity -> !HelperUtil.checkIfNullOrEmpty(samity.getSamityDay()) && samity.getSamityDay().equalsIgnoreCase(responseDTO.getBusinessDay()))
				.map(Samity::getSamityId)
				.flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(responseDTO.getManagementProcessId(), samityId)
						.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Regular Samity with id " + samityId + " is not Completed or canceled")))
						.collectList()
						.doOnNext(samityEventTrackerList -> log.debug("Regular Samity Event Tracker List with samity id {}: {}", samityId, samityEventTrackerList))
//						.filter(samityEventTrackerList -> samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.CANCELED.getValue()) || item.getSamityEvent().equals(SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue())))
						.filter(this::checkIfRegularSamityIsCanceledOrCompleted)
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Regular Samity with id " + samityId + " is not Completed or canceled")))
						.map(samityEventTrackerList -> samityId))
				.collectList()
				.doOnNext(samityIdList -> log.info("Regular samity list is validated for day end process: {}", samityIdList))
				.map(list -> responseDTO);
	}

	private Boolean checkIfRegularSamityIsCanceledOrCompleted(List<SamityEventTracker> samityEventTrackerList){
		AtomicReference<Boolean> isCanceled = new AtomicReference<>(false);
		AtomicReference<Boolean> isCompleted = new AtomicReference<>(false);
		samityEventTrackerList.forEach(eventTracker -> {
			if (eventTracker.getSamityEvent().equals(SamityEvents.CANCELED.getValue())) {
				isCanceled.set(true);
			}
		});

		if(isCanceled.get()){
			return true;
		} else{
			isCompleted.set(samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue())));
			if(!isCompleted.get()){
				return false;
			} else{
				samityEventTrackerList.forEach(eventTracker -> {
					if(eventTracker.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue())){
						isCompleted.set(samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.WITHDRAW_PASSBOOK_COMPLETED.getValue())));
					}
				});
			}
		}
		return isCompleted.get();
	}


	private Mono<DayEndProcessResponseDTO> validateSpecialSamityForDayEndProcess(DayEndProcessResponseDTO responseDTO){
		return commonRepository.getSpecialSamityIdListForManagementProcessByOffice(responseDTO.getManagementProcessId(), responseDTO.getOfficeId())
				.doOnNext(samityId -> log.info("Special Samity Id: {}", samityId))
				.flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(responseDTO.getManagementProcessId(), samityId)
						.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Samity Event Not Found for Samity Id " + samityId)))
						.collectList()
						.doOnNext(samityEventTrackerList -> log.debug("Special Samity Event Tracker List with samity id {}: {}", samityId, samityEventTrackerList))
//						.filter(samityEventTrackerList -> samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue())))
						.filter(this::checkIfSpecialSamityIsCompletedOrNot)
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Special Samity with id " + samityId + " is not Completed yet")))
						.map(samityEventTrackerList -> samityId))
				.collectList()
				.doOnNext(samityIdList -> log.info("Special samity list is validated for day end process: {}", samityIdList))
				.map(list -> responseDTO);
	}

	private Boolean checkIfSpecialSamityIsCompletedOrNot(List<SamityEventTracker> samityEventTrackerList){
		AtomicReference<Boolean> isCompleted = new AtomicReference<>(true);

		samityEventTrackerList.forEach(samityEventTracker -> {
			if(samityEventTracker.getSamityEvent().equals(SamityEvents.COLLECTED.getValue())){
				isCompleted.set(samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue())));
			} else {
				isCompleted.set(true);
			}
		});
		if(!isCompleted.get()){
			return false;
		} else{
			samityEventTrackerList.forEach(samityEventTracker -> {
				if(samityEventTracker.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue())){
					isCompleted.set(samityEventTrackerList.stream().anyMatch(item -> item.getSamityEvent().equals(SamityEvents.WITHDRAW_PASSBOOK_COMPLETED.getValue())));
				} else {
					isCompleted.set(true);
				}
			});
		}
		return isCompleted.get();
	}

	private Mono<DayEndProcessResponseDTO> rescheduleLoanRepayScheduleOnSamityCancel(DayEndProcessResponseDTO responseDTO){
		return commonRepository.getSamityByOfficeId(responseDTO.getOfficeId())
				.filter(samity -> !HelperUtil.checkIfNullOrEmpty(samity.getSamityDay()) && samity.getSamityDay().equalsIgnoreCase(responseDTO.getBusinessDay()))
				.map(Samity::getSamityId)
				.flatMap(samityId -> commonRepository.getLoanAccountIdListBySamityIdAndStatus(samityId, Status.STATUS_ACTIVE.getValue())
						.collectList())
				.doOnNext(loanAccountIdList -> log.info("Loan Account Id List to Reschedule: {}", loanAccountIdList))
				.flatMap(loanAccountIdList -> loanRepaymentScheduleUseCase.rescheduleLoanRepayScheduleOnSamityCancel(loanAccountIdList, responseDTO.getDayEndProcessRunBy(), responseDTO.getBusinessDate()))
				.collectList()
				.map(strings -> responseDTO);
	}
}
