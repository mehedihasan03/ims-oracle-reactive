package net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.entity.PassbookHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassbookHistoryRepository extends ReactiveCrudRepository<PassbookHistoryEntity, String> {
}
