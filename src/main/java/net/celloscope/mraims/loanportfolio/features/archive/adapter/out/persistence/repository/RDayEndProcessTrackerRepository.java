package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RDayEndProcessTrackerRepository extends RestoreArchivedDataBusiness<DayEndProcessTrackerEntity, String> {
}
