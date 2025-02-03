package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out;

import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity.WithdrawEntity;
import net.celloscope.mraims.loanportfolio.features.withdraw.domain.Withdraw;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WithdrawPersistencePort {
    Flux<Withdraw> saveWithdrawStagingData(List<Withdraw> withdrawList, String managementProcessId, String samityEventTrackerId);
    Mono<Integer> updateAllWithdrawStagedDataBy(String samityId, String withdrawType, String approvedBy);
    Flux<Withdraw> getWithdrawStagedDataByStagingDataId(String stagingDataId);

    Mono<List<WithdrawEntity>> getAllWithdrawStagingDataByManagementProcessId(String managementProcessId);
    Mono<String> deleteAllWithdrawStagingDataByManagementProcessId(String managementProcessId);

    Mono<Withdraw> getWithdrawData(String oid);
    Mono<Withdraw> updateWithdrawAmount(Withdraw withdrawData);
}
