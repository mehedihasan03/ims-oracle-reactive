package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanadjustment;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteLoanAdjustmentRepositoryV3 extends ReactiveCrudRepository<LoanAdjustmentDataEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<LoanAdjustmentDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
