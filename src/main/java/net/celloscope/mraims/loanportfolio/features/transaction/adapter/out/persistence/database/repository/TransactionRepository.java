package net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.queries.helpers.dto.TransactionGridViewDataDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, String> {
	
	@Query("""
			select X.*, Y.transaction_count, Y.transaction_amount from
			    (select s.samity_id, count(msopm.*) as total_member
			    from template.mem_smt_off_pri_map msopm
				join template.samity s
				on s.samity_id = msopm.samity_id
			    group by s.samity_id
			    where msopm.status = 'Active') X
			left join
			    (select s.samity_id, count(t.*) as transaction_count, sum(t.amount) as transaction_amount
			    from template."transaction" t
				join template.mem_smt_off_pri_map msopm
				on t.member_id = msopm.member_id
				join template.samity s
				on s.samity_id = msopm.samity_id
			    where t.transaction_date between :FROM_DATE and :TO_DATE
			    and msopm.status = 'Active'
			    group by s.samity_id) Y
			on X.samity_id = Y.samity_id;
			""")
	Flux<TransactionGridViewDataDTO> getTransactionCountBySamityId(@Param("FROM_DATE") LocalDateTime fromDate, @Param("TO_DATE") LocalDateTime toDate);
	
	
	@Query("""
			select *
			from template."transaction" t
			join template.mem_smt_off_pri_map msopm
			on t.member_id = msopm.member_id
			join template.samity s
			on s.samity_id = msopm.samity_id
			where s.samity_id = :SAMITY_ID
			and t.transaction_date between :FROM_DATE and :TO_DATE
			and msopm.status = 'Active'
			and
				case
					when (:ACCOUNT_NO is not null and :ACCOUNT_NO != '') then (t.loan_account_id = :ACCOUNT_NO or t.savings_account_id = :ACCOUNT_NO)
					else 1 = 1
				end
			and
				case
					when (:SEARCH_TEXT is not null and :SEARCH_TEXT != '') then (t.transaction_code ilike :SEARCH_TEXT)
					else 1 = 1
				end;
			""")
	Flux<TransactionEntity> getTransactionReportDataBySamityIdAndTransactionDateBetweenFromDateAndToDate(@Param("FROM_DATE") LocalDateTime fromDate,
	                                                                                                     @Param("TO_DATE") LocalDateTime toDate,
	                                                                                                     @Param("SAMITY_ID") String samityId,
	                                                                                                     @Param("ACCOUNT_NO") String accountNo,
	                                                                                                     @Param("SEARCH_TEXT") String searchText);
	
	@Query("""
			select *
			from template.samity s
			where s.samity_id = :SAMITY_ID;
			""")
	Mono<Samity> getSamityForTransactionReport(@Param("SAMITY_ID") String samityId);
	
	@Query("""
			    SELECT t.transaction_id FROM template."transaction" t WHERE t.staging_data_id IN (:stagingDataIdList);
			""")
	Flux<String> findTransactionIdListByStagingDataIdList(List<String> stagingDataIdList);
	
	
	@Query("""
			    SELECT * FROM template."transaction" t WHERE t.staging_data_id IN (:stagingDataIdList);
			""")
	Flux<TransactionEntity> findAllByStagingDataIdList(List<String> stagingDataIdList);

	Flux<TransactionEntity> findAllByMemberIdAndTransactionCodeAndLoanAccountIdNotNull(String memberId, String transactionCode);

	Flux<TransactionEntity> findAllBySavingsAccountIdNotNullAndLoanAdjustmentProcessIdIn(List<String> loanAdjustmentProcessIdList);

	Flux<TransactionEntity> findAllBySamityIdInAndTransactionDateGreaterThanEqual(List<String> samityIdList, LocalDate transactionDate);

//	Flux<TransactionEntity> findAllByManagementProcessId(String managementProcessId, LocalDate transactionDate);
	Flux<TransactionEntity> findAllByManagementProcessId(String managementProcessId);

	Mono<Boolean> existsTransactionEntityBySavingsAccountIdAndTransactionCode(String savingsAccountId, String transactionCode);

	Mono<Boolean> existsTransactionEntityByManagementProcessIdAndSavingsAccountIdAndTransactionCode(String managementProcessId, String savingsAccountId, String transactionCode);

	Flux<TransactionEntity> findAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);
	Flux<TransactionEntity> findAllByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode);
	Flux<TransactionEntity> findAllByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode);
	Flux<TransactionEntity> findAllByManagementProcessIdAndTransactionCodeAndSavingsAccountIdIn(String managementProcessId, String transactionCode, List<String> savingsAccountIdList);

	Flux<TransactionEntity> findAllByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);
	Mono<TransactionEntity> findByTransactionId(String transactionId);
}


