package net.celloscope.mraims.loanportfolio.features.processmanagement.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.ManagementProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ManagementProcessTrackerService implements ManagementProcessTrackerUseCase {
	
	private final ManagementProcessTrackerPersistencePort port;
	
	private final ModelMapper modelMapper;
	private final Gson gson;
	
	public ManagementProcessTrackerService(ManagementProcessTrackerPersistencePort port, ModelMapper modelMapper) {
		this.port = port;
		this.modelMapper = modelMapper;
		this.gson = CommonFunctions.buildGson(this);
	}
	
	@Override
	public Mono<ManagementProcessTracker> insertManagementProcess(String managementProcessId, String officeId, LocalDate businessDate, String loginId) {
		return Mono.fromSupplier(() -> ManagementProcessTracker.builder()
						.managementProcessId(managementProcessId)
						.officeId(officeId)
						.businessDate(businessDate)
						.businessDay(businessDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
						.createdOn(LocalDateTime.now())
						.createdBy(loginId)
						.build())
				.doOnNext(managementProcessTracker -> log.info("Management Process Tracker: {}", managementProcessTracker))
				.flatMap(port::insertManagementProcess);
	}
	
	@Override
	public Mono<ManagementProcessTracker> updateManagementProcess(String managementProcessId, String officeId, String loginId) {
		return null;
	}

	@Override
	public Mono<String> deleteManagementProcessForOfficeByManagementProcessId(String managementProcessId, String officeId) {
		return port
				.deleteManagementProcessByManagementProcessIdAndOfficeId(managementProcessId, officeId)
				.doOnSuccess(s -> log.info("Management Process Tracker deleted successfully for Process Id: {}", managementProcessId))
				.doOnError(throwable -> log.error("Error deleting management process tracker: {}", throwable.getMessage()))
				;
	}

	@Override
	public Mono<String> getLastManagementProcessIdForOffice(String officeId) {
		return port.getLastManagementProcessIdForOffice(officeId)
				.doOnNext(s -> log.info("Management Process Id for office {} is {}", officeId, s));
	}
	
	@Override
	public Mono<LocalDate> getCurrentBusinessDateForOffice(String managementProcessId, String officeId) {
		return port.getCurrentBusinessDateForOffice(managementProcessId, officeId)
				.doOnNext(businessDate -> log.info("Current Business Date: {}", businessDate))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Business Date found for office: " + officeId)));
	}

	@Override
	public Mono<LocalDate> getCurrentBusinessDateForManagementProcessId(String managementProcessId) {
		return port.getCurrentBusinessDateByManagementProcessId(managementProcessId)
				.doOnNext(businessDate -> log.info("Current Business Date of management process id : {}", businessDate))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Business Date found for management process id : " + managementProcessId)));
	}
	
	@Override
	public Mono<ManagementProcessTracker> getLastManagementProcessForOffice(String officeId) {
		return port.getLastManagementProcessForOffice(officeId)
				.doOnNext(managementProcessTracker -> log.debug("Management Process for office {} is {}", officeId, managementProcessTracker));
	}

	@Override
	public Mono<List<ManagementProcessTracker>> getAllManagementProcessForOffice(String officeId) {
		return port.getAllManagementProcessForOffice(officeId)
				.collectList();
	}

	@Override
	public Mono<ManagementProcessTracker> insertManagementProcessV2(String managementProcessId, String mfiId, String officeId, String officeNameEn, String officeNameBn, LocalDate businessDate, String loginId) {
		return Mono.fromSupplier(() -> ManagementProcessTracker.builder()
						.managementProcessId(managementProcessId)
						.officeId(officeId)
						.officeNameEn(officeNameEn)
						.officeNameBn(officeNameBn)
						.businessDate(businessDate)
						.mfiId(mfiId)
						.businessDay(businessDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
						.createdOn(LocalDateTime.now())
						.createdBy(loginId)
						.build())
				.doOnNext(managementProcessTracker -> log.info("Management Process Tracker: {}", managementProcessTracker))
				.flatMap(port::insertManagementProcess);
	}

}
