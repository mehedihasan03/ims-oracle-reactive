package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMonthEndProcessTrackerHistoryRepositoryDelete extends ReactiveCrudRepository<MonthEndProcessTrackerHistoryEntity, String> , DeleteArchiveDataBusiness<MonthEndProcessTrackerHistoryEntity,String> {
}
