package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RMonthEndProcessTrackerRepository extends RestoreArchivedDataBusiness<MonthEndProcessTrackerEntity, String> {
}
