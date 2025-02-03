package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OfficeEventTrackerHistoryRepository extends ReactiveCrudRepository<OfficeEventTrackerHistoryEntity, String> {
}
