package net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata;

import net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher.DeleteAutoVoucherDetailPersistenceRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher.DeleteAutoVoucherPersistenceRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.dayend.DeleteDayEndProcessTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanadjustment.DeleteLoanAdjustmentRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanrebate.DeleteLoanRebateRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanwaiver.DeleteLoanWaiverRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanwriteoff.DeleteLoanWriteOffCollectionRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.monthend.DeleteMonthEndProcessTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.office.DeleteOfficeEventTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.passbook.DeletePassbookRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity.DeleteSamityEventTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccount.interestdeposit.DeleteSavingsAccountInterestDepositRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.collectionstagingdata.DeleteCollectionStagingDataRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.processtracker.DeleteStagingProcessTrackerRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingaccountdata.DeleteStagingAccountDataRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingdata.DeleteStagingDataRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.withdrawstagingdata.DeleteWithdrawStagingDataRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.transaction.DeleteTransactionRepositoryV3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DeleteRepositoryConfigV3 {
    @Bean
    public Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> repositoryMapV3(
            DeleteSamityEventTrackerRepositoryV3 samityEventTrackerRepository,
            DeleteOfficeEventTrackerRepositoryV3 officeEventTrackerRepository,
            DeleteStagingDataRepositoryV3 stagingDataRepository,
            DeleteStagingAccountDataRepositoryV3 stagingAccountDataRepository,
            DeleteCollectionStagingDataRepositoryV3 collectionStagingDataRepository,
            DeleteLoanAdjustmentRepositoryV3 loanAdjustmentRepository,
            DeleteLoanRebateRepositoryV3 loanRebateRepository,
            DeleteLoanWriteOffCollectionRepositoryV3 loanWriteOffCollectionRepository,
            DeleteLoanWaiverRepositoryV3 loanWaiverRepository,
            DeleteTransactionRepositoryV3 transactionRepository,
            DeletePassbookRepositoryV3 passbookRepository,
            DeleteWithdrawStagingDataRepositoryV3 deleteWithdrawStagingDataRepository,
            DeleteStagingProcessTrackerRepositoryV3 stagingProcessTrackerRepository,
            DeleteAutoVoucherDetailPersistenceRepositoryV3 autoVoucherDetailPersistenceRepository,
            DeleteAutoVoucherPersistenceRepositoryV3 autoVoucherPersistenceRepository,
            DeleteSavingsAccountInterestDepositRepositoryV3 savingsAccountInterestDepositRepository,
            DeleteDayEndProcessTrackerRepositoryV3 dayEndProcessTrackerRepository,
            DeleteMonthEndProcessTrackerRepositoryV3 monthEndProcessTrackerRepository
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
