package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in;

import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PassbookUseCase {
    Mono<List<PassbookResponseDTO>> getRepaymentScheduleAndCreatePassbookEntryForLoan(PassbookRequestDTO passbookRequestDTO);

    Mono<Passbook> getLastPassbookEntry(String loanAccountId);

    Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccount(String savingsAccountId);
    Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccountForStagingData(String savingsAccountId);
    Mono<PassbookResponseDTO> getLastPassbookEntryBySavingsAccountOid(String savingsAccountOid);

    Mono<List<PassbookResponseDTO>> createPassbookEntryForSavings(PassbookRequestDTO passbookRequestDTO);

    Mono<PassbookResponseDTO> createPassbookEntryForSavingsWithdraw(PassbookRequestDTO passbookRequestDTO);

    Flux<PassbookResponseDTO> getPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn(String savingsAccountId, LocalDate transactionDate);

    Mono<AccruedInterestResponseDTO> createPassbookEntryForTotalAccruedInterestDeposit(PassbookRequestDTO passbookRequestDTO);

    Mono<List<String>> getRepayScheduleIdListByTransactionList(List<String> transactionIdList);

    Mono<List<String>> deletePassbookEntryByTransaction(List<String> transactionIdList);
    Flux<PassbookResponseDTO> getPassbookEntitiesByYearMonthAndSavingsAccountOid(Integer yearValue, Integer monthValue, String savingsAccountId);

    Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndAccountTypeAndPaymentMode(String processManagementId, String accountType, String paymentMode);

    Mono<PassbookResponseDTO> createPassbookEntryForDisbursement(PassbookRequestDTO passbookRequestDTO);
    Mono<PassbookResponseDTO> getDisbursementPassbookEntryByDisbursedLoanAccountId(String disbursedLoanAccountId);

    Flux<PassbookResponseDTO> getWithdrawPassbookEntriesByManagementProcessIdAndPaymentMode(String managementProcessId, String paymentMode);

    Mono<AccruedInterestResponseDTO> createPassbookEntryForInterestDeposit(PassbookRequestDTO passbookRequestDTO);

    Mono<List<Passbook>> createPassbookEntryForSavingsAccountForLoanAdjustment(List<Passbook> passbookList);

    Mono<List<PassbookResponseDTO>> getRepaymentScheduleAndCreatePassbookEntryForLoanV1(PassbookRequestDTO passbookRequestDTO);

    Mono<PassbookResponseDTO> createPassbookEntryForTermDepositClosure(PassbookRequestDTO passbookRequestDTO);

    Mono<PassbookResponseDTO> getLastInterestDepositPassbookEntryBySavingsAccountOid(String savingsAccountOid);

    Mono<List<PassbookResponseDTO>> getPassbookEntriesBetweenTransactionDates(String savingsAccountId, LocalDate fromDate, LocalDate toDate);

    Mono<List<String>> deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(String managementProcessId, String passbookProcessId);

    Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(String processManagementId, String transactionCode, String paymentMode, String savingsTypeId);

    Mono<PassbookResponseDTO> getLastPassbookEntryByTransactionCodeAndLoanAccountOid(String transactionCode, String loanAccountOid);

    Mono<List<PassbookResponseDTO>> getPassbookEntriesByTransactionCodeAndSavingsAccountId(String transactionCode, String savingsAccountId);
    Mono<List<PassbookResponseDTO>> getPassbookEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);

    Mono<Passbook> createPassbookEntryForWelfareFund(Passbook passbook);

    Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId,  List<String> savingsAccountIdList);

    Flux<PassbookResponseDTO> getPassbookEntriesForAdvanceLoanRepaymentDebit(String officeId, LocalDate businessDate);
    Flux<PassbookResponseDTO> getPassbookEntriesForAdvanceLoanRepaymentCredit(String officeId, LocalDate businessDate);
    Flux<PassbookResponseDTO> getPassbookEntriesForPrincipalAndServiceChargeOutstanding(String officeId, LocalDate businessDate);
    Flux<PassbookResponseDTO> getPassbookEntriesForPrincipalAndServiceChargeOutstandingV2(String managementProcessId);
    Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(String managementProcessId, String transactionCode, String savingsTypeId);

    Flux<PassbookResponseDTO> createPassbookEntryForLoanRebateAndWriteOff(PassbookRequestDTO passbookRequestDTO);
    Mono<Tuple2<Map<LocalDate, String>, String>> archivePassbookEntriesByTransactionDateAndLater(String accountId, String accountType, LocalDate transactionDate, String loginId, String managementProcessId);
    Flux<PassbookResponseDTO> getPassbookEntriesByTransactionId(String transactionId);
}
