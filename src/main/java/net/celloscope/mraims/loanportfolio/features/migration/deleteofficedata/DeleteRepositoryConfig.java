package net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata;

import net.celloscope.mraims.loanportfolio.features.migration.components.autovoucher.DeleteAutoVoucherDetailPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.autovoucher.DeleteAutoVoucherPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.dayend.DeleteDayEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanadjustment.DeleteLoanAdjustmentRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanrebate.DeleteLoanRebateRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanwaiver.DeleteLoanWaiverRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanwriteoff.DeleteLoanWriteOffCollectionRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.managementprocesstracker.DeleteManagementProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.monthend.DeleteMonthEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.office.DeleteOfficeEventTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.passbook.DeletePassbookRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.samity.DeleteSamityEventTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount.interestdeposit.DeleteSavingsAccountInterestDepositRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.collectionstagingdata.DeleteCollectionStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.processtracker.DeleteStagingProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingaccountdata.DeleteStagingAccountDataRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingdata.DeleteStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.withdrawstagingdata.DeleteWithdrawStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.migration.components.transaction.DeleteTransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DeleteRepositoryConfig {
    @Bean
    public Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> repositoryMap(
            DeleteSamityEventTrackerRepository samityEventTrackerRepository,
            DeleteOfficeEventTrackerRepository officeEventTrackerRepository,
            DeleteStagingDataRepository stagingDataRepository,
            DeleteStagingAccountDataRepository stagingAccountDataRepository,
            DeleteCollectionStagingDataRepository collectionStagingDataRepository,
            DeleteLoanAdjustmentRepository loanAdjustmentRepository,
            DeleteLoanRebateRepository loanRebateRepository,
            DeleteLoanWriteOffCollectionRepository loanWriteOffCollectionRepository,
            DeleteLoanWaiverRepository loanWaiverRepository,
            DeleteTransactionRepository transactionRepository,
            DeletePassbookRepository passbookRepository,
            DeleteWithdrawStagingDataRepository deleteWithdrawStagingDataRepository,
            DeleteStagingProcessTrackerRepository stagingProcessTrackerRepository,
            DeleteAutoVoucherDetailPersistenceRepository autoVoucherDetailPersistenceRepository,
            DeleteAutoVoucherPersistenceRepository autoVoucherPersistenceRepository,
            DeleteSavingsAccountInterestDepositRepository savingsAccountInterestDepositRepository,
            DeleteDayEndProcessTrackerRepository dayEndProcessTrackerRepository,
            DeleteMonthEndProcessTrackerRepository monthEndProcessTrackerRepository
    ) {

        Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> repositoryMap = new LinkedHashMap<>();
        repositoryMap.put(deleteWithdrawStagingDataRepository.getClass(), deleteWithdrawStagingDataRepository);
        repositoryMap.put(collectionStagingDataRepository.getClass(), collectionStagingDataRepository);
        repositoryMap.put(stagingDataRepository.getClass(), stagingDataRepository);
        repositoryMap.put(stagingAccountDataRepository.getClass(), stagingAccountDataRepository);
        repositoryMap.put(loanAdjustmentRepository.getClass(), loanAdjustmentRepository);
        repositoryMap.put(loanRebateRepository.getClass(), loanRebateRepository);
        repositoryMap.put(loanWriteOffCollectionRepository.getClass(), loanWriteOffCollectionRepository);
        repositoryMap.put(loanWaiverRepository.getClass(), loanWaiverRepository);
        repositoryMap.put(autoVoucherDetailPersistenceRepository.getClass(), autoVoucherDetailPersistenceRepository);
        repositoryMap.put(autoVoucherPersistenceRepository.getClass(), autoVoucherPersistenceRepository);
        repositoryMap.put(passbookRepository.getClass(), passbookRepository);
        repositoryMap.put(transactionRepository.getClass(), transactionRepository);
        repositoryMap.put(savingsAccountInterestDepositRepository.getClass(), savingsAccountInterestDepositRepository);
        repositoryMap.put(samityEventTrackerRepository.getClass(), samityEventTrackerRepository);
        repositoryMap.put(stagingProcessTrackerRepository.getClass(), stagingProcessTrackerRepository);
        repositoryMap.put(dayEndProcessTrackerRepository.getClass(), dayEndProcessTrackerRepository);
        repositoryMap.put(monthEndProcessTrackerRepository.getClass(), monthEndProcessTrackerRepository);
        repositoryMap.put(officeEventTrackerRepository.getClass(), officeEventTrackerRepository);

        return repositoryMap;
    }
}
