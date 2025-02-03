package net.celloscope.mraims.loanportfolio.features.passbook.application.port.out;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.ResultSet;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.queries.helpers.dto.PassbookGridViewDataDTO;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface PassbookPersistencePort {

    LocalDate getLastCreatedDate(String transactionCode, String loanAccountId);

    Mono<Passbook> getLastPassbookEntry(String loanAccountId);

    Mono<ResultSet> getResultSetFromPassbook(String loanAccountId, String transactionCode);

    Mono<PassbookEntity> insertRecordPassbook(Passbook passbook);

    Flux<Passbook> insertRecordPassbooks(Flux<Passbook> passbooks);
    Flux<Passbook> insertRecordPassbooksList(List<Passbook> passbooks);

    Mono<String> saveRecordsIntoPassBookHistory(List<PassbookResponseDTO> passbookDataList);

    Mono<Passbook> getLastPassbookEntryBySavingsAccountId(String savingsAccountId);

    Flux<PassbookGridViewDataDTO> findPassbookGridViewData(PassbookGridViewQueryDTO queryDTO);

    Flux<Passbook> findPassbookReportData(PassbookReportQueryDTO queryDTO);

    Flux<Passbook> findPassbookReportDataV2(PassbookReportQueryDTO queryDTO);

    Mono<Samity> findSamityDetailsForPassbookReportData(String samityId);

    Flux<Passbook> getPassbookEntriesBySavingsAccountIDAndDate(String savingsAccountId, LocalDate transactionDate);

    Flux<String> getRepayScheduleIdByTransactionList(List<String> transactionIdList);

    Mono<List<String>> deletePassbookEntryByTransaction(List<String> transactionIdList);

    Flux<Passbook> getPassbookEntriesByYearMonthAndSavingsAccountOid(Integer yearValue, Integer monthValue, String savingsAccountId);
    Flux<Passbook> getLoanPassbookEntriesByProcessManagementId(String processManagementId);
    Flux<Passbook> getSavingsPassbookEntriesByProcessManagementId(String processManagementId);
    Flux<Passbook> getPassbookEntriesByProcessManagementIdAndPaymentMode(String processManagementId, String paymentMethod);

    Mono<Passbook> getDisbursementPassbookEntryByDisbursedLoanAccountId(String disbursedLoanAccountId);

    Flux<Passbook> getWithdrawPassbookEntriesByManagementProcessIdAndPaymentMode(String managementProcessId, String paymentMode);

    Mono<List<Passbook>> createPassbookEntryForLoanAdjustment(List<Passbook> passbookList);
    Mono<Passbook> createPassbookEntryForLoanRebate(Passbook passbook);

    Mono<Passbook> getLastPassbookEntryBySavingsAccountOid(String savingsAccountOid);

    Mono<Passbook> getLastInterestDepositPassbookEntryBySavingsAccountOid(String savingsAccountOid);
    Mono<Passbook> getLastWithdrawPassbookEntryBySavingsAccountOid(String savingsAccountOid);

    Flux<Passbook> getPassbookEntriesBetweenTransactionDates(String savingsAccountOid, LocalDate fromDate, LocalDate toDate);

    Mono<List<String>> deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(String managementProcessId, String passbookProcessId);
    Mono<String> deletePassbookEntriesForTransactionCodeByManagementProcessId(String transactionCode, String managementProcessId);

    Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(String processManagementId, String transactionCode, String paymentMode, String savingsTypeId);
    Mono<PassbookResponseDTO> getLastPassbookEntryByLoanAccountOidAndTransactionCodes(String loanAccountOid, List<String> transactionCodes);

    Mono<PassbookResponseDTO> getLastPassbookEntryByTransactionCodeAndLoanAccountOid(String transactionCode, String loanAccountOid);

    Flux<PassbookResponseDTO> getPassbookEntryByTransactionCodeAndSavingsAccountOid(String transactionCode, String savingsAccountOid);

    Mono<Passbook> createPassbookEntryForWelfareFund(Passbook passbook);

    Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList);

    Flux<Passbook> getPassbookEntriesForAdvanceLoanRepaymentDebit(String officeId, LocalDate businessDate);

    Flux<Passbook> getPassbookEntriesForAdvanceLoanRepaymentCredit(String officeId, LocalDate businessDate);
    Flux<Passbook> getPassbookEntriesByInstallDateEqualsBusinessDate(String officeId, LocalDate businessDate);
    Flux<Passbook> getPassbookEntriesByInstallDateBeforeBusinessDateAndTransactionDateEqualsBusinessDate(String officeId, LocalDate businessDate);

    Flux<Passbook> getPassbookEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);

    Flux<Passbook> getPassbookEntriesByTransactionDateAndLater(String accountId, String accountType, LocalDate transactionDate);

    Mono<Boolean> deletePassbookEntriesByOid(List<String> passbookOids);

    Flux<Passbook> getPassbookEntriesByTransactionId(String transactionId);

    Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(String managementProcessId, String transactionCode, String savingsTypeId);
}
