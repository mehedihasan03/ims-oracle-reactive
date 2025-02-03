package net.celloscope.mraims.loanportfolio.features.archive.adapter.out;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DeleteArchivedHistoryConfiguration {
    @Bean
    public Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> deleteArchivedHistoryMappings(
            IStagingDataHistoryRepositoryDelete stagingDataHistoryRepository,
            IStagingAccountHistoryRepositoryDelete stagingAccountHistoryRepository,
            IStagingProcessTrackerHistoryRepositoryDelete stagingProcessTrackerHistoryRepository,
            ICollectionHistoryRepositoryDelete collectionStagingHistoryRepository,
            IWithdrawHistoryRepositoryDelete withdrawStagingHistoryRepository,
            LoanAdjustmentDataHistoryRepositoryDelete loanAdjustmentDataHistoryRepository,
            ILoanRebateHistoryRepositoryDelete loanRebateHistoryRepository,
            ILoanWaiverHistoryRepositoryDelete loanWaiverHistoryRepository,
            IDayEndProcessTrackerHistoryRepositoryDelete dayEndProcessTrackerHistoryRepository,
            IMonthEndProcessTrackerHistoryRepositoryDelete monthEndProcessTrackerHistoryRepository,
            ILoanWriteOffHistoryRepository loanWriteOffHistoryRepository
    ) {

        Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> deleteArchivedHistoryConfiguration = new LinkedHashMap<>();
        deleteArchivedHistoryConfiguration.put(stagingDataHistoryRepository.getClass(), stagingDataHistoryRepository);
        deleteArchivedHistoryConfiguration.put(stagingAccountHistoryRepository.getClass(), stagingAccountHistoryRepository);
        deleteArchivedHistoryConfiguration.put(stagingProcessTrackerHistoryRepository.getClass(), stagingProcessTrackerHistoryRepository);
        deleteArchivedHistoryConfiguration.put(collectionStagingHistoryRepository.getClass(), collectionStagingHistoryRepository);
        deleteArchivedHistoryConfiguration.put(withdrawStagingHistoryRepository.getClass(), withdrawStagingHistoryRepository);
        deleteArchivedHistoryConfiguration.put(loanAdjustmentDataHistoryRepository.getClass(), loanAdjustmentDataHistoryRepository);
        deleteArchivedHistoryConfiguration.put(loanRebateHistoryRepository.getClass(), loanRebateHistoryRepository);
        deleteArchivedHistoryConfiguration.put(loanWaiverHistoryRepository.getClass(), loanWaiverHistoryRepository);
        deleteArchivedHistoryConfiguration.put(dayEndProcessTrackerHistoryRepository.getClass(), dayEndProcessTrackerHistoryRepository);
        deleteArchivedHistoryConfiguration.put(monthEndProcessTrackerHistoryRepository.getClass(), monthEndProcessTrackerHistoryRepository);
        deleteArchivedHistoryConfiguration.put(loanWriteOffHistoryRepository.getClass(), loanWriteOffHistoryRepository);

        return deleteArchivedHistoryConfiguration;
    }
}


