package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity.WelfareFundDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface WelfareFundDataRepository extends ReactiveCrudRepository<WelfareFundDataEntity, String> {

    @Query("""
            SELECT loan_account_id, office_id, transaction_date, amount, status FROM welfare_fund_data WHERE office_id = :officeId AND status != :status ORDER BY transaction_date DESC LIMIT :limit OFFSET :offset;
            """)
    Flux<WelfareFundDataEntity> getWelfareFundDataEntitiesByOfficeIdFilterByStatus(String officeId, long limit, long offset, String status);

    Mono<WelfareFundDataEntity> findFirstByLoanAccountIdAndTransactionDate(String loanAccountId, LocalDate transactionDate);

    Mono<WelfareFundDataEntity> findFirstByLoanAccountIdAndOfficeIdAndTransactionDate(String loanAccountId, String officeId, LocalDate transactionDate);

    Flux<WelfareFundDataEntity> findAllByLoanAccountIdOrderByTransactionDate(String loanAccountId);
    Flux<WelfareFundDataEntity> findAllByOfficeIdAndStatusOrderByTransactionDate(String officeId, String status);

    Flux<WelfareFundDataEntity> findAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);
}
