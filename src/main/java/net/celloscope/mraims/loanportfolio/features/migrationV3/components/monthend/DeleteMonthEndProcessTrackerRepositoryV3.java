package net.celloscope.mraims.loanportfolio.features.migrationV3.components.monthend;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DeleteMonthEndProcessTrackerRepositoryV3 extends ReactiveCrudRepository<MonthEndProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<MonthEndProcessTrackerEntity, String> {
}
