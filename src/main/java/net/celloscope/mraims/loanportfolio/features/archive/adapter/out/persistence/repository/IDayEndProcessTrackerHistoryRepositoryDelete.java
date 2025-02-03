package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IDayEndProcessTrackerHistoryRepositoryDelete extends ReactiveCrudRepository<DayEndProcessTrackerHistoryEntity, String> , DeleteArchiveDataBusiness<DayEndProcessTrackerHistoryEntity,String> {
}
