package net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher.DeleteAutoVoucherDetailPersistenceRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher.DeleteAutoVoucherPersistenceRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.office.DeleteOfficeEventTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.OfficeDataDeleteRequestDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteOfficeDataServiceV3 {
    private final TransactionalOperator rxtx;
    private final ManagementProcessTrackerUseCase managementProcessUseCase;
    private final Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> repositoryMap;


    public Mono<String> deleteAllOfficeDataForManagementProcessId(OfficeDataDeleteRequestDTO request) {
        return managementProcessUseCase
                .getLastManagementProcessIdForOffice(request.getOfficeId())
                .doOnSuccess(managementProcessId -> log.info("Deleting data for management process id: {}", managementProcessId))
                .flatMap(managementProcessId ->
                        deleteDataForManagementProcessId(managementProcessId, request.getOfficeId())
                                .thenReturn(managementProcessId)
                )
                .as(rxtx::transactional)
                .doOnSuccess(success -> log.info("Deleted all office data for management process id"))
                .thenReturn("Success")
                ;
    }

    private Mono<Void> deleteDataForManagementProcessId(String managementProcessId, String officeId) {
        return Flux
                .fromIterable(repositoryMap.values())
                .doOnNext(repository -> log.warn("Deleting data from repository: {}", repository.getClass().getSimpleName()))
                .flatMap(repository -> deleteDataFromRepository(repository, managementProcessId, officeId))
                .then();
    }

    private <T, ID> Mono<Void> deleteDataFromRepository(ReactiveCrudRepository<T, ID> repository, String managementProcessId, String officeId) {
        if (repository instanceof DeleteAutoVoucherDetailPersistenceRepositoryV3) {
            return ((DeleteAutoVoucherDetailPersistenceRepositoryV3) repository)
                    .deleteAllByOfficeId(officeId)
                    .doOnSuccess(success -> log.info("Deleted data from repository: {}", repository.getClass().getSimpleName()));
        } else if (repository instanceof DeleteAutoVoucherPersistenceRepositoryV3) {
            return ((DeleteAutoVoucherPersistenceRepositoryV3) repository)
                    .deleteAllByOfficeId(officeId)
                    .doOnSuccess(success -> log.info("Deleted data from repository: {}", repository.getClass().getSimpleName()));
        } else if (repository instanceof DeleteOfficeEventTrackerRepositoryV3) {
            return ((DeleteOfficeEventTrackerRepositoryV3) repository)
                    .deleteAllByManagementProcessIdAndOfficeEventNot(managementProcessId, OfficeEvents.DAY_STARTED.getValue())
                    .doOnSuccess(success -> log.info("Deleted data from repository: {}", repository.getClass().getSimpleName()));
        } else if (repository instanceof DeleteDataByManagementProcessIdRepositoryV3) {
            return ((DeleteDataByManagementProcessIdRepositoryV3<T, ID>) repository)
                    .deleteAllByManagementProcessId(managementProcessId)
                    .doOnSuccess(success -> log.info("Deleted data from repository: {}", repository.getClass().getSimpleName()));
        } else {
            log.warn("Repository {} does not support deleteAllByManagementProcessId method.", repository.getClass().getSimpleName());
            return Mono.empty();
        }
    }
}
