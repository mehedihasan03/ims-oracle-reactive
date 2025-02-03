package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessDataArchivePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessPersistencePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.OfficeEventTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.out.AccruedInterestPort;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class MonthEndProcessDataArchiveAdapter implements MonthEndProcessDataArchivePort {
    private final PassbookPersistencePort passbookPersistencePort;
    private final TransactionPersistencePort transactionPersistencePort;
    private final OfficeEventTrackerPersistencePort officeEventTrackerPersistencePort;
    private final AccruedInterestPort savingsInterestPort;
    private final MonthEndProcessPersistencePort monthEndProcessPersistencePort;
    private final MonthEndProcessDataHistoryRepository monthEndProcessDataHistoryRepository;
    private final ModelMapper modelMapper;

    public MonthEndProcessDataArchiveAdapter(PassbookPersistencePort passbookPersistencePort, TransactionPersistencePort transactionPersistencePort, OfficeEventTrackerPersistencePort officeEventTrackerPersistencePort, AccruedInterestPort savingsInterestPort, MonthEndProcessPersistencePort monthEndProcessPersistencePort, MonthEndProcessDataHistoryRepository monthEndProcessDataHistoryRepository, ModelMapper modelMapper) {
        this.passbookPersistencePort = passbookPersistencePort;
        this.transactionPersistencePort = transactionPersistencePort;
        this.officeEventTrackerPersistencePort = officeEventTrackerPersistencePort;
        this.savingsInterestPort = savingsInterestPort;
        this.monthEndProcessPersistencePort = monthEndProcessPersistencePort;
        this.monthEndProcessDataHistoryRepository = monthEndProcessDataHistoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<String> saveIntoPassBookHistory(List<PassbookResponseDTO> passbookDataList) {
        return passbookPersistencePort
                .saveRecordsIntoPassBookHistory(passbookDataList)
                .doOnRequest(value -> log.info("Request received for saving into passbook history"))
                .doOnSuccess(value -> log.info("Successfully saved into passbook history"))
                .doOnError(throwable -> log.error("Error occurred while saving into passbook history", throwable))
                ;
    }

    @Override
    public Mono<String> deleteFromPassBook(String managementProcessId) {
        return passbookPersistencePort
                .deletePassbookEntriesForTransactionCodeByManagementProcessId(TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessId)
                .doOnRequest(value -> log.info("Request received for deleting from passbook for management process id: {}", managementProcessId))
                .doOnSuccess(value -> log.info("Successfully deleted from passbook for management process id: {}", managementProcessId))
                .doOnError(throwable -> log.error("Error occurred while deleting from passbook for management process id: {}", managementProcessId, throwable))
                ;
    }

    @Override
    public Mono<String> saveIntoTransactionHistory(List<Transaction> transactionList) {
        return transactionPersistencePort
                .saveTransactionsIntoTransactionHistory(transactionList)
                .doOnRequest(value -> log.info("Request received for saving into transaction history"))
                .doOnSuccess(value -> log.info("Successfully saved into transaction history"))
                .doOnError(throwable -> log.error("Error occurred while saving into transaction history", throwable));
    }

    @Override
    public Mono<String> deleteFromTransaction(String managementProcessId) {
        return transactionPersistencePort
                .deleteTransactionsForTransactionCodeByManagementProcessId(TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessId)
                .doOnRequest(value -> log.info("Request received for deleting from transaction for management process id: {}", managementProcessId))
                .doOnSuccess(value -> log.info("Successfully deleted from transaction for management process id: {}", managementProcessId))
                .doOnError(throwable -> log.error("Error occurred while deleting from transaction for management process id: {}", managementProcessId, throwable))
                ;
    }

    @Override
    public Mono<String> saveIntoMonthEndProcessTrackerHistory(List<MonthEndProcessTracker> monthEndProcessTrackerList) {
        return monthEndProcessPersistencePort
                .saveMonthEndProcessTrackerEntriesIntoHistory(monthEndProcessTrackerList)
                .doOnRequest(value -> log.info("Request received for saving into month end process tracker history"))
                .doOnSuccess(value -> log.info("Successfully saved into month end process tracker history"))
                .doOnError(throwable -> log.error("Error occurred while saving into month end process tracker history", throwable))
                .then(Mono.just("Successfully saved into month end process tracker history"));
    }

    @Override
    public Mono<String> deleteFromMonthEndProcessTracker(String managementProcessId) {
        return monthEndProcessPersistencePort
                .deleteFromMonthEndProcessTrackerByManagementProcessId(managementProcessId)
                .doOnRequest(value -> log.info("Request received for deleting from month end process tracker"))
                .doOnSuccess(value -> log.info("Successfully deleted from month end process tracker"))
                .doOnError(throwable -> log.error("Error occurred while deleting from month end process tracker", throwable));
    }

    @Override
    public Mono<String> saveIntoOfficeEventTrackerHistory(OfficeEventTracker officeEventTracker) {
        return officeEventTrackerPersistencePort
                .saveOfficeEventTrackerIntoHistory(officeEventTracker)
                .doOnRequest(value -> log.info("Request received for saving into office event tracker history"))
                .doOnSuccess(value -> log.info("Successfully saved into office event tracker history"))
                .doOnError(throwable -> log.error("Error occurred while saving into office event tracker history", throwable))
                .then(Mono.just("Successfully saved into office event tracker history"));
    }

    @Override
    public Mono<String> deleteFromOfficeEventTracker(OfficeEventTracker officeEventTracker) {
        return officeEventTrackerPersistencePort
                .deleteOfficeEventTrackerForDayEndProcess(officeEventTracker)
                .doOnRequest(value -> log.info("Request received for deleting from office event tracker"))
                .doOnSuccess(value -> log.info("Successfully deleted from office event tracker"))
                .doOnError(throwable -> log.error("Error occurred while deleting from office event tracker", throwable))
                .then(Mono.just("Successfully deleted from office event tracker"));
    }

    @Override
    public Mono<String> saveIntoSavingsAccountInterestDepositHistory(List<SavingsAccountInterestDeposit> savingsAccountInterestDepositList) {
        return savingsInterestPort
                .saveAllSavingsAccountInterestDepositsIntoHistory(savingsAccountInterestDepositList)
                .doOnRequest(value -> log.info("Request received for saving into savings account interest deposit history"))
                .doOnSuccess(value -> log.info("Successfully saved into savings account interest deposit history"))
                .doOnError(throwable -> log.error("Error occurred while saving into savings account interest deposit history", throwable))
                .then(Mono.just("Successfully saved into savings account interest deposit history"));
    }

    @Override
    public Mono<String> deleteFromSavingsAccountInterestDeposit(String managementProcessId) {
        return savingsInterestPort
                .deleteAllSavingsAccountInterestDepositByManagementProcessId(managementProcessId)
                .doOnRequest(value -> log.info("Request received for deleting from savings account interest deposit"))
                .doOnSuccess(value -> log.info("Successfully deleted from savings account interest deposit"))
                .doOnError(throwable -> log.error("Error occurred while deleting from savings account interest deposit", throwable))
                .then(Mono.just("Successfully deleted from savings account interest deposit"));
    }

    @Override
    public Mono<String> saveIntoMonthEndProcessDataHistory(List<MonthEndProcessData> monthEndProcessDataList) {
        return Flux.fromIterable(monthEndProcessDataList)
                .map(monthEndData -> {
                    MonthEndProcessDataHistoryEntity historyEntity = modelMapper.map(monthEndData, MonthEndProcessDataHistoryEntity.class);
                    historyEntity.setArchivedBy("System");
                    historyEntity.setArchivedOn(LocalDateTime.now());
                    return historyEntity;
                })
                .collectList()
                .flatMapMany(monthEndProcessDataHistoryRepository::saveAll)
                .doOnRequest(value -> log.info("Request received for saving into month end process data history"))
                .doOnComplete(() -> log.info("Successfully saved into month end process data history"))
                .doOnError(throwable -> log.error("Error occurred while saving into month end process data history", throwable))
                .then(Mono.just("Successfully saved into month end process data history"));
    }

    @Override
    public Mono<String> deleteFromMonthEndProcessData(String managementProcessId) {
        return monthEndProcessPersistencePort
                .deleteAllByManagementProcessId(managementProcessId)
                .doOnRequest(value -> log.info("Request received for deleting from month end process data"))
                .doOnSuccess(value -> log.info("Successfully deleted from month end process data"))
                .doOnError(throwable -> log.error("Error occurred while deleting from month end process data", throwable));
    }
}
