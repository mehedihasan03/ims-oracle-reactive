package net.celloscope.mraims.loanportfolio.features.migration.components.dayend;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteDayEndProcessTrackerRepository extends ReactiveCrudRepository<DayEndProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepository<DayEndProcessTrackerEntity, String> {
}
