package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingaccountdata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationStagingAccountDataServiceV3 {
    private final MigrationStagingAccountDataRepositoryV3 repository;
    
    public Flux<StagingAccountDataEntity> getByManagementProcessId(String managementProcessId) {
        return repository.findByManagementProcessIdOrderByMemberIdDesc(managementProcessId)
                .doOnNext(stagingAccountDataEntity -> log.info("StagingAccountDataEntity: {}", stagingAccountDataEntity));
    }
}
