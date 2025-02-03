package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.dto.PassbookTransactionDTO;
import reactor.core.publisher.Mono;

public interface IPassbookTransactionGateway {
	
	Mono<PassbookTransactionDTO> getLastPassbookEntryForDepositAmountWithSavingsAccount(String savingsAccountId);
	
	Mono<PassbookTransactionDTO> getLastPassbookEntryForWithdrawAmountWithSavingsAccount(String savingsAccountId);
}
