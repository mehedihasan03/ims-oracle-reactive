package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingdata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationStagingDataServiceV3 {
    private final MigrationStagingDataRepositoryV3 repository;
    
    public Flux<StagingDataEntity> getByManagementProcessId(String managementProcessId) {
        return repository.findByManagementProcessIdOrderByMemberIdDesc(managementProcessId)
                .doOnNext(stagingDataEntity -> log.info("StagingDataEntity: {}", stagingDataEntity));
    }
}
