package net.celloscope.mraims.loanportfolio.features.migration.components.monthend;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteMonthEndProcessTrackerRepository extends ReactiveCrudRepository<MonthEndProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepository<MonthEndProcessTrackerEntity, String> {
}
