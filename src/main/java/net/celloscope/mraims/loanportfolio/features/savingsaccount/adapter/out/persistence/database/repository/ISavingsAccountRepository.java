package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.entity.SavingsAccountEntity;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountDto;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.domain.SavingsAccount;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface ISavingsAccountRepository extends ReactiveCrudRepository<SavingsAccountEntity, String> {

    @Query("""
        SELECT
        	sp.savings_prod_name_en AS savings_product_name_en,
        	sp.savings_prod_name_bn AS savings_product_name_bn,
        	*
        FROM
        	template.savings_account sa
        INNER JOIN template.savings_product sp ON
        	sa.savings_product_id = sp.savings_product_id
        WHERE sa.member_id = :memberId;
    """)
    Flux<SavingsAccount> findAllByMemberId(String memberId);

    @Query("""
    SELECT sp.MIN_DEPOSIT_AMOUNT, sp.MAX_DEPOSIT_AMOUNT, sa.* FROM template.SAVINGS_ACCOUNT sa
    INNER JOIN template.SAVINGS_PRODUCT sp
    ON sa.SAVINGS_PRODUCT_ID = sp.SAVINGS_PRODUCT_ID
    WHERE sa.SAVINGS_ACCOUNT_ID = :savingsAccountId;
    """)
    Mono<SavingsAccountEntity> findBySavingsAccountId(String savingsAccountId);

    @Query("""
    SELECT *
    FROM template.savings_account sa
    WHERE
    sa.savings_type_id = :savingsTypeId
    AND EXTRACT(DAY FROM sa.acct_start_date ::date) BETWEEN EXTRACT(DAY FROM :startDate::date) AND EXTRACT(DAY FROM :endDate::date)
    AND sa.acct_end_date >= :endDate;
    """)
    Flux<SavingsAccountEntity> findAllValidFDRAccountsInBetweenDaysOfMonths(LocalDate startDate, LocalDate endDate, String savingsTypeId);

    @Query("""
        SELECT
        	sp.savings_prod_name_en AS savings_product_name_en,
        	sp.savings_prod_name_bn AS savings_product_name_bn,
        	*
        FROM
        	template.savings_account sa
        INNER JOIN template.savings_product sp ON
        	sa.savings_product_id = sp.savings_product_id
        WHERE
        	sa.member_id in (:memberIdList);
    """)
    Flux<SavingsAccount> findAllByMemberIdList(List<String> memberIdList);

    @Query("""
   SELECT *
   FROM template.savings_account
   WHERE member_id LIKE :officeId || '-%'
   and status = :status;
   """)
    Flux<SavingsAccount> getSavingsAccountByOfficeIdAndStatus(String officeId, String status);

    @Query("""

    SELECT sa.savings_account_id , sa.savings_product_id , sp.savings_prod_name_en, sp.savings_prod_name_bn, sa.member_id , m.member_name_en , m.member_name_bn, sa.acct_start_date , sa.savings_amount , ic.interest AS interest_rate, sa.acct_end_date , sa.status,
    sa.deposit_term, sa.deposit_term_period, sa.deposit_every
    FROM template.savings_account sa
    INNER JOIN template.interest_chart ic
    ON sa.savings_product_id = ic.savings_product_id
    INNER JOIN template.savings_product sp
    ON sp.savings_product_id = sa.savings_product_id
    INNER JOIN template."member" m
    ON sa.member_id = m.member_id
    WHERE sa.member_id LIKE :officeId || '-%'
    AND sa.savings_type_id = :savingsTypeId
    AND sa.status IN (:statusList);
    """)
    Flux<FDRAccountDTO> getFDRSavingsAccountsByOfficeIdAndStatus(String officeId, List<String> statusList, String savingsTypeId);


    @Query("""
    SELECT
    sa.savings_account_id, sa.savings_application_id,
    sa.savings_product_id,  sp.savings_prod_name_en, sp.savings_prod_name_bn,
    sa.member_id, m.member_name_en, m.member_name_bn,
    sa.acct_start_date, sa.savings_amount, sa.maturity_amount,
    ic.interest AS interest_rate, sa.interest_rate_frequency,
    sa.interest_posting_period, sa.acct_end_date,
    sa.status, sa.oid AS savings_account_oid,
    sa.deposit_term, sa.deposit_term_period, sa.deposit_every,
    sa.savings_type_id
    FROM template.savings_account sa
    INNER JOIN template.interest_chart ic
    ON sa.savings_product_id = ic.savings_product_id
    INNER JOIN template.savings_product sp
    ON sp.savings_product_id = sa.savings_product_id
    INNER JOIN template."member" m
    ON sa.member_id = m.member_id
    WHERE sa.savings_account_id = :savingsAccountId
    AND sa.savings_type_id = :savingsTypeId;
    """)
    Mono<FDRAccountDTO> getFDRAccountDetailsBySavingsAccountId(String savingsAccountId, String savingsTypeId);

    @Query("""

    SELECT sa.savings_account_id , sa.savings_product_id , sp.savings_prod_name_en, sp.savings_prod_name_bn, sa.member_id , m.member_name_en , m.member_name_bn, sa.acct_start_date , sa.savings_amount , sa.balance, ic.interest AS interest_rate, sa.acct_end_date , sa.status,
    sa.deposit_term, sa.deposit_term_period, sa.deposit_every
    FROM template.savings_account sa
    INNER JOIN template.interest_chart ic
    ON sa.savings_product_id = ic.savings_product_id
    INNER JOIN template.savings_product sp
    ON sp.savings_product_id = sa.savings_product_id
    INNER JOIN template."member" m
    ON sa.member_id = m.member_id
    WHERE sa.member_id LIKE :officeId || '-%'
    AND sa.savings_type_id = :savingsTypeId
    AND sa.status IN (:statusList);
    """)
    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndStatus(String officeId, List<String> statusList, String savingsTypeId);

    @Query("""
            SELECT sa.savings_account_id , sa.savings_product_id , sp.savings_prod_name_en, sp.savings_prod_name_bn, sa.member_id , m.member_name_en , m.member_name_bn, sa.acct_start_date , sa.savings_amount , sa.balance, ic.interest AS interest_rate, sa.acct_end_date , sa.status,
            sa.deposit_term, sa.deposit_term_period, sa.deposit_every
            FROM template.savings_account sa
            INNER JOIN template.interest_chart ic
            ON sa.savings_product_id = ic.savings_product_id
            INNER JOIN template.savings_product sp
            ON sp.savings_product_id = sa.savings_product_id
            INNER JOIN template."member" m
            ON sa.member_id = m.member_id
            WHERE sa.member_id LIKE :officeId || '-%'
            AND sa.savings_type_id = :savingsTypeId
            AND sa.status IN (:statusList)
            AND
                case
                    when (:searchText is not null and :searchText != '')
                    then (sa.savings_account_id ilike '%' || :searchText || '%'
                    or sa.member_id ilike '%' || :searchText || '%' 
                    or m.member_name_en ilike '%' || :searchText || '%')
                    else 1 = 1
                end;
            """)
    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndStatusAndSearchText(String officeId, String searchText, List<String> statusList, String savingsTypeId);

    @Query("""
    SELECT
    sa.savings_account_id, sa.savings_application_id,
    sa.savings_product_id, sa.savings_prod_name_en, sp.savings_prod_name_en, sp.savings_prod_name_bn,
    sa.member_id, m.member_name_en, m.member_name_bn,
    sa.acct_start_date, sa.savings_amount, sa.maturity_amount,
    ic.interest AS interest_rate, sa.interest_rate_frequency,
    sa.interest_posting_period, sa.acct_end_date,
    sa.status, sa.oid AS savings_account_oid,
    sa.deposit_term, sa.deposit_term_period, sa.deposit_every,
    s.samity_id, s.samity_day, sa.monthly_repay_day,
    sa.savings_type_id
    FROM template.savings_account sa
    INNER JOIN template.interest_chart ic
    ON sa.savings_product_id = ic.savings_product_id
    INNER JOIN template.savings_product sp
    ON sp.savings_product_id = sa.savings_product_id
    INNER JOIN template."member" m
    ON sa.member_id = m.member_id
    INNER JOIN template.mem_smt_off_pri_map msopm
    ON m.member_id = msopm.member_id
    INNER JOIN template.samity s
    ON msopm.samity_id = s.samity_id
    WHERE sa.savings_account_id = :savingsAccountId
    AND sa.savings_type_id = :savingsTypeId
    AND msopm.status = 'Active'
    FETCH FIRST 1 ROWS ONLY;
    """)
    Mono<DPSAccountDTO> getDPSAccountDetailsBySavingsAccountId(String savingsAccountId, String savingsTypeId);

    @Query("""
    SELECT
    sa.savings_account_id, sa.savings_application_id,
    sa.savings_product_id, sa.savings_prod_name_en, sp.savings_prod_name_en, sp.savings_prod_name_bn,
    sa.member_id, m.member_name_en, m.member_name_bn,
    sa.acct_start_date, sa.savings_amount, sa.maturity_amount,
    ic.interest AS interest_rate, sa.interest_rate_frequency,
    sa.interest_posting_period, sa.acct_end_date,
    sa.status, sa.oid AS savings_account_oid,
    sa.deposit_term, sa.deposit_term_period, sa.deposit_every,
    s.samity_id, s.samity_day, sa.monthly_repay_day,
    sa.savings_type_id
    FROM template.savings_account sa
    INNER JOIN template.interest_chart ic
    ON sa.savings_product_id = ic.savings_product_id
    INNER JOIN template.savings_product sp
    ON sp.savings_product_id = sa.savings_product_id
    INNER JOIN template."member" m
    ON sa.member_id = m.member_id
    INNER JOIN template.mem_smt_off_pri_map msopm
    ON m.member_id = msopm.member_id
    INNER JOIN template.samity s
    ON msopm.samity_id = s.samity_id
    WHERE sa.savings_account_id = :savingsAccountId
    FETCH FIRST 1 ROWS ONLY;
    """)
    Mono<SavingsAccountDto> getSavingsAccountInfoBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountEntity> getSavingsAccountEntityBySavingsAccountId(String savingsAccountId);

}
