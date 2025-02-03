package net.celloscope.mraims.loanportfolio.features.transaction.application.port.out;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.queries.helpers.dto.TransactionGridViewDataDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface TransactionPersistencePort {
	Mono<Boolean> saveTransactionsToDb(List<Transaction> transactionList);
	Mono<String> saveTransactionsIntoTransactionHistory(List<Transaction> transactionList);
	
	Flux<TransactionGridViewDataDTO> findTransactionGridViewData(TransactionGridViewQueryDTO queryDTO);
	
	Flux<Transaction> findTransactionReportData(TransactionReportQueryDTO queryDTO);
	
	Mono<Samity> findSamityDetailsForTransactionReportData(String samityId);
	
	Mono<List<String>> getTransactionIdListForSamityByStagingDataId(List<String> stagingDataIdList);
	
	Mono<List<String>> deleteTransactionForSamityByStagingDataIDList(List<String> stagingDataIdList);

	Mono<SingleTransactionResponseDTO> saveSingleTransactionToDB(Transaction transaction);

	Mono<List<Transaction>> saveTransactionListToDB(List<Transaction> transactionList);

    Flux<Transaction> getLoanAdjustedTransactionsForLoanAccountsOfMember(String memberId);

	Flux<Transaction> getSavingsAccountsForLoanAdjustedTransactions(List<String> loanAdjustmentProcessIdList);

    Mono<List<Transaction>> getAllLoanDisbursementTransactionDataForSamityIdList(List<String> samityIdList);

    Flux<Transaction> getAllTransactionsOnABusinessDayForOffice(String managementProcessId, LocalDate businessDate);

	Mono<Boolean> checkIfTransactionAlreadyExistsBySavingsAccountIdAndTransactionCode(String savingsAccountId, String transactionCode);

	Mono<Boolean> checkIfTransactionAlreadyExistsByManagementProcessIdAndSavingsAccountIdAndTransactionCode(String managementProcessId, String savingsAccountId, String transactionCode);

	Mono<String> deleteTransactionsForSamityUnauthorization(String managementProcessId, String transactionProcessId);
	Mono<String> deleteTransactionsForTransactionCodeByManagementProcessId(String transactionCode, String managementProcessId);

    Flux<Transaction> getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode);

    Mono<Transaction> createTransactionForWelfareFund(Transaction transaction);

    Flux<Transaction> getWelfareFundTransactionForOfficeByManagementProcessId(String managementProcessId);

    Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList);

	Mono<List<Transaction>> getTransactionEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);
	Mono<Transaction> getTransactionByTransactionId(String transactionId);
}
