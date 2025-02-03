package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthEndProcessTrackerHistoryRepository extends ReactiveCrudRepository<MonthEndProcessTrackerHistoryEntity, String>{
}
