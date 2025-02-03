package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount.interestdeposit;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsAccountInterestDepositEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DeleteSavingsAccountInterestDepositRepository extends ReactiveCrudRepository<SavingsAccountInterestDepositEntity, String>, DeleteDataByManagementProcessIdRepository<SavingsAccountInterestDepositEntity, String> {
}
