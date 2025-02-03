package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IStagingAccountDataEditHistoryRepository extends ReactiveCrudRepository<StagingAccountDataEditHistoryEntity, String> {
}
