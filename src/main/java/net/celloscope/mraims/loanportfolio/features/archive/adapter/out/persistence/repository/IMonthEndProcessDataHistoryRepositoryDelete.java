package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IMonthEndProcessDataHistoryRepositoryDelete extends ReactiveCrudRepository<MonthEndProcessDataHistoryEntity, String>, DeleteArchiveDataBusiness<MonthEndProcessDataHistoryEntity,String>{
}
