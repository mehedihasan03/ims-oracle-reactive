package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DayEndProcessTrackerHistoryRepository  extends ReactiveCrudRepository<DayEndProcessTrackerHistoryEntity, String> {
}
