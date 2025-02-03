package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.PassbookTransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IPassbookTransactionGatewayRepository extends ReactiveCrudRepository<PassbookTransactionEntity, String> {
	
	@Query("""
		SELECT
	    p.deposit_amount AS last_deposit_amount,
	    p.transaction_date AS last_deposit_date,
	    t.collection_type AS last_deposit_type
	  FROM
	    passbook p
	  INNER JOIN "transaction" t ON
	    p.transaction_id = t.transaction_id
	  WHERE
	    p.savings_account_id = :savingsAccountId
	    AND
	    p.deposit_amount IS NOT NULL
	    AND
	    p.deposit_amount::int != 0::int
	  ORDER BY p.created_on DESC
	  LIMIT 1;
	""")
	Mono<PassbookTransactionEntity> getLastPassbookEntryForDepositAmountWithSavingsAccount(String savingsAccountId);
	
	@Query("""
		SELECT
	    p.withdraw_amount AS last_withdraw_amount,
	    p.transaction_date AS last_withdraw_date,
	    t.withdraw_type AS last_withdraw_type
	  FROM
	    passbook p
	  INNER JOIN "transaction" t ON
	    p.transaction_id = t.transaction_id
	  WHERE
	    p.savings_account_id = :savingsAccountId
	    AND
	    p.withdraw_amount IS NOT NULL
	    AND
	    p.withdraw_amount::int != 0::int
	  ORDER BY p.created_on DESC
	  LIMIT 1;
	""")
	Mono<PassbookTransactionEntity> getLastPassbookEntryForWithdrawAmountWithSavingsAccount(String savingsAccountId);
}
