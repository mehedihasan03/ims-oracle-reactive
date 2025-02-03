package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import java.util.List;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.LoanAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.SavingsAccountSummeryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IStagingAccountDataRepository extends ReactiveCrudRepository<StagingAccountDataEntity, String> {

        @Query("""
                        SELECT
                        	*
                        FROM
                        	staging_account_data sad
                        WHERE
                        	sad.member_id = :memberId
                        	AND sad.loan_account_id IS NOT NULL;
                        """)
        Flux<StagingAccountDataEntity> getStagingLoanAccountDataListByMemberId(@Param("memberId") String memberId);

        @Query("""
                        SELECT
                        	*
                        FROM
                        	staging_account_data sad
                        WHERE
                        	sad.member_id = :memberId
                        	AND sad.savings_account_id IS NOT NULL;
                        """)
        Flux<StagingAccountDataEntity> getStagingSavingsAccountDataListByMemberId(@Param("memberId") String memberId);

        @Query("""
                            SELECT
                            	sad.product_code,
                            	sad.product_name_en,
                            	sad.product_name_bn,
                            	sum(sad.total_due) AS total_due
                            FROM
                            	staging_account_data sad
                            INNER JOIN staging_data sd ON
                            	sad.member_id = sd.member_id
                            WHERE
                            	sad.loan_account_id IS NOT NULL
                            	AND
                                sd.samity_id = :samityId
                            GROUP BY
                            	sad.product_code,
                            	sad.product_name_en,
                            	sad.product_name_bn,
                            	sad.product_code;
                        """)
        Flux<LoanAccountSummeryDTO> getLoanAccountSummeryByProductCode(String samityId);

        @Query("""
                        SELECT
                        	sad.savings_product_code AS product_code,
                        	sad.savings_product_name_en AS product_name_en,
                        	sad.savings_product_name_bn AS product_name_bn,
                        	sum(sad.target_amount) AS total_target
                        FROM
                        	staging_account_data sad
                        INNER JOIN staging_data sd ON
                        	sad.member_id = sd.member_id
                        WHERE
                        	sad.savings_account_id IS NOT NULL
                        	AND
                        	sd.samity_id = :samityId
                        GROUP BY
                        	sad.savings_product_code,
                        	sad.savings_product_name_en,
                        	sad.savings_product_name_bn,
                        	sad.savings_product_code;
                        """)
        Flux<SavingsAccountSummeryDTO> getSavingsAccountSummeryByProductCode(String samityId);

        @Query("""
                        SELECT DISTINCT
                        	*
                        FROM
                        	staging_account_data sad
                        WHERE
                        	sad.loan_account_id = :accountId
                        	OR sad.savings_account_id = :accountId
                        LIMIT 1;
                        """)
        Mono<StagingAccountDataEntity> getStagingLoanOrSavingsAccountByAccountId(String accountId);

//        Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

        Flux<StagingAccountDataEntity> findAllByManagementProcessId(String managementProcessId);

        Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

        Flux<StagingAccountDataEntity> findAllByProcessIdAndMemberIdIn(String processId, List<String> memberIdList);

        Mono<Void> deleteAllByProcessIdAndMemberIdIn(String processId, List<String> memberIdList);

        Mono<StagingAccountDataEntity> findFirstBySavingsAccountIdNotNullAndSavingsAccountId(String savingsAccountId);

        Flux<StagingAccountDataEntity> findAllByManagementProcessIdAndProcessIdAndMemberIdIn(String managementProcessId,
                        String processId, List<String> memberIdList);

        Flux<StagingAccountDataEntity> findAllByManagementProcessIdAndMemberIdIn(String managementProcessId,
                        List<String> memberIdList);

        Flux<StagingAccountDataEntity> getStagingAccountDataEntitiesByMemberId(String memberId);

        Flux<StagingAccountDataEntity> findAllBySavingsAccountIdIn(List<String> savingAccountIdList);
        Flux<StagingAccountDataEntity> findAllByMemberIdIn(List<String> memberIdList);

        Mono<StagingAccountDataEntity> findFirstByLoanAccountIdNotNullAndLoanAccountId(String loanAccountId);

}
