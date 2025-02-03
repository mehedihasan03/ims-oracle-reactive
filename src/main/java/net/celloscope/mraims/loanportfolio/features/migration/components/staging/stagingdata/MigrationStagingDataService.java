package net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingdata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationStagingDataService {
    private final MigrationStagingDataRepository repository;
    
    public Flux<StagingDataEntity> getByManagementProcessId(String managementProcessId) {
        return repository.findByManagementProcessIdOrderByMemberIdDesc(managementProcessId)
                .doOnNext(stagingDataEntity -> log.info("StagingDataEntity: {}", stagingDataEntity));
    }
}
