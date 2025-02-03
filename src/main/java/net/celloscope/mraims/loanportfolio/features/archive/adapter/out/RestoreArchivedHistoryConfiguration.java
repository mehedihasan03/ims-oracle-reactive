package net.celloscope.mraims.loanportfolio.features.archive.adapter.out;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository.*;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity.WithdrawStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class RestoreArchivedHistoryConfiguration {

    @Bean
    public Map<Class<?>, ReactiveCrudRepository<?, String>> restoreArchivedHistoryMappings(
            RStagingDataRepositoryArchivedData stagingDataRepository,
            RStagingAccountDataRepositoryArchivedData stagingAccountDataRepository,
            RStagingWithdrawDataRepositoryArchivedData stagingWithdrawDataRepository,
            RCollectionStagingDataRepositoryArchivedData collectionStagingDataRepository,
            RStagingProcessTrackerRepository stagingProcessTrackerRepository,
            RLoanAdjustmentRepository loanAdjustmentRepository,
            RLoanWaiverRepository loanWaiverRepository,
            RLoanRebateRepository loanRebateRepository,
            RDayEndProcessTrackerRepository dayEndProcessTrackerRepository,
            RMonthEndProcessTrackerRepository monthEndProcessTrackerRepository,
            RLoanWriteOffRepository loanWriteOffRepository
    ) {
        Map<Class<?>, ReactiveCrudRepository<?, String>> restoreArchivedHistoryConfiguration = new LinkedHashMap<>();

        restoreArchivedHistoryConfiguration.put(StagingDataEntity.class, stagingDataRepository);
        restoreArchivedHistoryConfiguration.put(StagingAccountDataEntity.class, stagingAccountDataRepository);
        restoreArchivedHistoryConfiguration.put(StagingProcessTrackerEntity.class, stagingProcessTrackerRepository);
        restoreArchivedHistoryConfiguration.put(CollectionStagingDataEntity.class, collectionStagingDataRepository);
        restoreArchivedHistoryConfiguration.put(StagingWithdrawDataEntity.class, stagingWithdrawDataRepository);
        restoreArchivedHistoryConfiguration.put(LoanAdjustmentDataEntity.class, loanAdjustmentRepository);
        restoreArchivedHistoryConfiguration.put(LoanRebateEntity.class, loanRebateRepository);
        restoreArchivedHistoryConfiguration.put(LoanWaiverEntity.class, loanWaiverRepository);
        restoreArchivedHistoryConfiguration.put(DayEndProcessTrackerEntity.class, dayEndProcessTrackerRepository);
        restoreArchivedHistoryConfiguration.put(MonthEndProcessTrackerEntity.class, monthEndProcessTrackerRepository);
        restoreArchivedHistoryConfiguration.put(LoanWriteOffCollectionEntity.class, loanWriteOffRepository);

        return restoreArchivedHistoryConfiguration;
    }
}
