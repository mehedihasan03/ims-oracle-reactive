package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DayForwardProcessTrackerHistoryRepository extends R2dbcRepository<DayForwardProcessTrackerHistoryEntity, String> {
}
