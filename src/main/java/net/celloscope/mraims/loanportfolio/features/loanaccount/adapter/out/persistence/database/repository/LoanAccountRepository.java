package net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.entity.LoanAccountEntity;
import net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.entity.LoanAccountProductEntity;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface LoanAccountRepository extends ReactiveCrudRepository<LoanAccountEntity, String> {

    @Query("""
        SELECT
        	lp.loan_product_name_en AS product_name_en,
        	lp.loan_product_name_bn AS product_name_bn,
        	la.loan_product_id AS product_code,
        	*
        FROM
        	loan_account la
        INNER JOIN loan_product lp ON
        	la.loan_product_id = lp.loan_product_id
        WHERE
        	la.member_id = :memberId
        	AND
        	la.status = :status
        	AND
        	lp.loan_type_id = :loanType;
    """)
    Flux<StagingAccountData> getLoanAccountListForStagingAccountData(@Param("memberId") String memberId, @Param("status") String status, String loanType);

    Mono<LoanAccountEntity> findByLoanAccountId(String loanAccountId);

    @Query("""
    select lp.monthly_repay_day, * from loan_account la
    inner join loan_product lp
    on la.loan_product_id = lp.loan_product_id
    where la.loan_account_id = :loanAccountId;
    """)
    Mono<LoanAccountProductEntity> getLoanAccountInfo(String loanAccountId);

    Flux<LoanAccountEntity> findAllByMemberIdInAndStatus(List<String> memberId, String status);

    Flux<LoanAccountEntity> findAllByMemberIdAndStatus(String memberId, String status);
}
