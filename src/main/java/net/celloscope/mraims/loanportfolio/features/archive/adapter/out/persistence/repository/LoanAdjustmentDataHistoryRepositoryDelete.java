package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.LoanAdjustmentDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanAdjustmentDataHistoryRepositoryDelete extends ReactiveCrudRepository<LoanAdjustmentDataHistoryEntity, String> , DeleteArchiveDataBusiness<LoanAdjustmentDataHistoryEntity,String> {

    Flux<LoanAdjustmentDataHistoryEntity> findAllByManagementProcessId(String managementProcessId);
}
