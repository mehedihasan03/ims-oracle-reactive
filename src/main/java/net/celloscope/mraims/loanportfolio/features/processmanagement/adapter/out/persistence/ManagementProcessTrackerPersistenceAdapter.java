package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.ManagementProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.ManagementProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@Slf4j
public class ManagementProcessTrackerPersistenceAdapter implements ManagementProcessTrackerPersistencePort {

	private final ManagementProcessTrackerRepository repository;

	private final ModelMapper mapper;
	private final Gson gson;

	public ManagementProcessTrackerPersistenceAdapter(ManagementProcessTrackerRepository repository, ModelMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
		this.gson = CommonFunctions.buildGson(this);
	}

	@Override
	public Mono<ManagementProcessTracker> insertManagementProcess(ManagementProcessTracker managementProcessTracker) {
		return repository.save(gson.fromJson(managementProcessTracker.toString(), ManagementProcessTrackerEntity.class))
				.doOnNext(managementProcessTrackerEntity -> log.debug("Management Process Tracker Entity: {}", managementProcessTrackerEntity))
				.map(entity -> gson.fromJson(entity.toString(), ManagementProcessTracker.class))
				.doOnError(throwable -> log.error("Error saving management process tracker: {}", throwable.getMessage()));
	}

	@Override
	public Mono<String> deleteManagementProcessByManagementProcessIdAndOfficeId(String managementProcessId, String officeId) {
		return repository
				.findByManagementProcessIdAndOfficeId(managementProcessId, officeId)
				.flatMap(repository::delete)
                .then(Mono.just("Management Process Tracker deleted successfully for Process Id: " + managementProcessId))
				;
	}

	@Override
	public Mono<String> getLastManagementProcessIdForOffice(String officeId) {
		return repository.findFirstByOfficeIdOrderByBusinessDateDesc(officeId)
				.map(ManagementProcessTrackerEntity::getManagementProcessId)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Process Id found for Office: " + officeId)));
	}

	@Override
	public Mono<LocalDate> getCurrentBusinessDateForOffice(String managementProcessId, String officeId) {
		return repository.findByManagementProcessIdAndOfficeId(managementProcessId, officeId)
				.map(ManagementProcessTrackerEntity::getBusinessDate);
	}
	
	@Override
	public Mono<LocalDate> getCurrentBusinessDateByManagementProcessId(String managementProcessId) {
		return repository.findFirstByManagementProcessId(managementProcessId)
				.map(ManagementProcessTrackerEntity::getBusinessDate);
	}

	@Override
	public Mono<ManagementProcessTracker> getLastManagementProcessForOffice(String officeId) {
		return repository.findFirstByOfficeIdOrderByBusinessDateDesc(officeId)
				.switchIfEmpty(Mono.just(ManagementProcessTrackerEntity.builder().build()))
				.map(entity -> gson.fromJson(entity.toString(), ManagementProcessTracker.class));
	}

	@Override
	public Flux<ManagementProcessTracker> getAllManagementProcessForOffice(String officeId) {
		return repository.findAllByOfficeIdOrderByBusinessDateDesc(officeId)
				.map(entity -> gson.fromJson(entity.toString(), ManagementProcessTracker.class));
	}
}
