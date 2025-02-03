package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsAccountInterestDepositEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AccruedInterestRepository extends ReactiveCrudRepository<SavingsAccountInterestDepositEntity, String> {

    @Query("""
    SELECT EXISTS (
    SELECT 1
    FROM savings_account_interest_deposit said
    WHERE interest_calculation_month = :month
    and interest_calculation_year = :year
    and savings_account_id = :savingsAccountId
    );
    """)
    Mono<Boolean> checkIfExistsByYearMonthAndSavingsAccountId(Integer year, Integer month, String savingsAccountId);

    /*Flux<SavingsAccountInterestDepositEntity> findAllBySavingsAccountIdAndInterestCalculationYearAndInterestCalculationMonthInAndStatus(String savingsAccountId, Integer year, List<String> monthList, String status);*/

    Flux<SavingsAccountInterestDepositEntity> findAllByAccruedInterestIdIn(List<String> accruedInterestIdList);
    Flux<SavingsAccountInterestDepositEntity> findAllByManagementProcessId (String managementProcessId);

    Flux<SavingsAccountInterestDepositEntity> getAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);
    Flux<SavingsAccountInterestDepositEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
}
