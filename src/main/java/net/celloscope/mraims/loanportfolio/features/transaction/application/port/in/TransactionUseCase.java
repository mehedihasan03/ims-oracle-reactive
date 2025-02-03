package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in;

import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionUseCase {
	Mono<TransactionResponseDTO> createTransactionForOneSamity(String samityId, String managementProcessId, String transactionProcessId, String officeId, String source);
	
	Mono<TransactionResponseDTO> createTransactionForWithdrawBySamityId(String managementProcessId, String transactionProcessId, String samityId, String officeId);
	
	Mono<List<String>> getTransactionIdListForSamityByStagingIDList(List<String> stagingDataIdList);
	
	Mono<List<String>> deleteTransactionForSamityByStagingDataIDList(List<String> stagingDataIdList);

	Mono<Transaction> createTransactionForSavingsInterestDeposit(String loginId, String savingsAccountId, LocalDate interestPostingDate, BigDecimal calculatedInterest, String officeId);

	Mono<TransactionResponseDTO> createTransactionForHalfYearlyInterestPosting(String savingsAccountId, Integer interestPostingYear, String closingType, String loginId);

	Mono<SingleTransactionResponseDTO> createTransactionForLoanDisbursement(String disbursementLoanAccountId, BigDecimal loanAmount, LocalDate disbursementDate, String memberId, String mfiId, String loginId, String managementProcessId, String officeId, String source);

	Mono<TransactionResponseDTO> createTransactionForMultipleFDRInterestPosting(String loginId, String savingsAccountId, LocalDate interestPostingDate);

	Mono<SingleTransactionResponseDTO> createTransactionForSingleFDRInterest(String loginId, String savingsAccountId, LocalDate interestPostingDate, BigDecimal calculatedInterest, String officeId);
	Mono<SingleTransactionResponseDTO> createTransactionForFDRActivation(String savingsAccountId, BigDecimal fdrAmount, LocalDate activationDate, String loginId);

	Mono<List<Transaction>> createTransactionEntryForLoanAdjustmentForSamity(List<Transaction> transactionList);

    Mono<List<Transaction>> getLoanAdjustedTransactionsForLoanAccountsOfMember(String memberId);

	Mono<List<Transaction>> getSavingsAccountsForLoanAdjustedTransactions(List<String> loanAdjustmentProcessIdList);

    Mono<Map<String, BigDecimal>> getTotalLoanDisbursementAmountForSamityResponse(List<String> samityIdList);

    Mono<List<Transaction>> getAllTransactionsOnABusinessDayForOffice(String managementProcessId, LocalDate businessDate);

	Mono<TransactionResponseDTO> createTransactionForOneSamityV1(String samityId, String managementProcessId, String transactionProcessId);

	Mono<SingleTransactionResponseDTO> createTransactionForFDRClosure(Transaction transaction);
	Mono<SingleTransactionResponseDTO> createTransactionForDPSClosure(Transaction transaction);

	Mono<SingleTransactionResponseDTO> createTransactionForSavingsClosure(Transaction transaction);

    Mono<String> deleteTransactionsForSamityUnauthorization(String managementProcessId, String transactionProcessId);

	Flux<Transaction> getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode);

	Mono<Transaction> createTransactionForWelfareFund(Transaction transaction);

    Mono<List<Transaction>> getWelfareFundTransactionForOfficeByManagementProcessId(String managementProcessId);

    Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList);

	Mono<List<Transaction>> getTransactionsByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);
	Mono<Transaction> getTransactionByTransactionId(String transactionId);
	Mono<Transaction> saveTransaction(Transaction transaction);
}
