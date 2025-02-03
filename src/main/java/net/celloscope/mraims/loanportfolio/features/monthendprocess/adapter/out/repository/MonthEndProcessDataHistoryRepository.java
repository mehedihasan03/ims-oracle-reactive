package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MonthEndProcessDataHistoryRepository extends ReactiveCrudRepository<MonthEndProcessDataHistoryEntity, String> {
}
