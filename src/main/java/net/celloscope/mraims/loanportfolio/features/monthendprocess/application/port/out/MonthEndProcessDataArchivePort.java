package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MonthEndProcessDataArchivePort {
    Mono<String> saveIntoPassBookHistory(List<PassbookResponseDTO> passbookDataList);
    Mono<String> deleteFromPassBook(String managementProcessId);
    Mono<String> saveIntoTransactionHistory(List<Transaction> transactionList);
    Mono<String> deleteFromTransaction(String managementProcessId);
    Mono<String> saveIntoMonthEndProcessTrackerHistory(List<MonthEndProcessTracker> monthEndProcessTrackerList);
    Mono<String> deleteFromMonthEndProcessTracker(String managementProcessId);
    Mono<String> saveIntoOfficeEventTrackerHistory(OfficeEventTracker officeEventTracker);
    Mono<String> deleteFromOfficeEventTracker(OfficeEventTracker officeEventTracker);
    Mono<String> saveIntoSavingsAccountInterestDepositHistory(List<SavingsAccountInterestDeposit> savingsAccountInterestDepositList);
    Mono<String> deleteFromSavingsAccountInterestDeposit(String managementProcessId);
    Mono<String> saveIntoMonthEndProcessDataHistory(List<MonthEndProcessData> monthEndProcessDataList);
    Mono<String> deleteFromMonthEndProcessData(String managementProcessId);
}
