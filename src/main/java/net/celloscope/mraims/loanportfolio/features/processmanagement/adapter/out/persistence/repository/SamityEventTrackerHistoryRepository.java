package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SamityEventTrackerHistoryRepository extends ReactiveCrudRepository<SamityEventTrackerHistoryEntity, String> {
}
