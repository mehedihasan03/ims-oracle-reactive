package net.celloscope.mraims.loanportfolio.features.migration.components.loanadjustment;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeleteLoanAdjustmentRepository extends ReactiveCrudRepository<LoanAdjustmentDataEntity, String>, DeleteDataByManagementProcessIdRepository<LoanAdjustmentDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
