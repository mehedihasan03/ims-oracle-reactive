package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.StagingAccountDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IStagingAccountHistoryRepositoryDelete extends ReactiveCrudRepository<StagingAccountDataHistoryEntity, String> , DeleteArchiveDataBusiness<StagingAccountDataHistoryEntity,String> {
}
