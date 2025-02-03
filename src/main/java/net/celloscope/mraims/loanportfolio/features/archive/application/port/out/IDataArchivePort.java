package net.celloscope.mraims.loanportfolio.features.archive.application.port.out;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.*;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IDataArchivePort {

    Mono<String> saveCollectionIntoHistory(List<CollectionStagingDataHistoryEntity> historyEntityList);

    Mono<String> saveStagingAccountDataIntoHistory(List<StagingAccountDataHistoryEntity> historyEntityList);

    Mono<String> saveStagingDataIntoHistory(List<StagingDataHistoryEntity> historyEntityList);

    Mono<String> saveStagingProcessTrackerIntoHistory(List<StagingProcessTrackerHistoryEntity> historyEntityList);

    Mono<String> saveWithdrawIntoHistory(List<WithdrawStagingDataHistoryEntity> historyEntityList);

    Mono<String> saveLoanAdjustmentIntoHistory(List<LoanAdjustmentDataHistoryEntity> loanAdjustmentDataHistoryEntityList);

    Mono<String> saveMonthEndProcessTrackerIntoHistory(List<MonthEndProcessTrackerHistoryEntity> monthEndProcessTrackerEntityList);
    Mono<String> saveMonthEndProcessDataIntoHistory(List<MonthEndProcessDataHistoryEntity> monthEndProcessDataEntityList);
    Mono<String> saveDayEndProcessTrackerIntoHistory(List<DayEndProcessTrackerHistoryEntity> dayEndProcessTrackerEntityList);
    Mono<String> saveLoanWaiverDataIntoHistory(List<LoanWaiverHistoryEntity> loanWaiverHistoryEntityList);
    Mono<String> saveLoanRebateDataIntoHistory(List<LoanRebateHistoryEntity> loanRebateHistoryEntityList);

    Mono<List<LoanAdjustmentData>> getLoanAdjustmentDataByManagementProcessId(String managementProcessId);

    Mono<String> saveDayForwardProcessTrackerIntoHistory(List<DayForwardProcessTrackerHistoryEntity> historyEntityList);
    Mono<String> saveLoanWriteOffDataIntoHistory(List<LoanWriteOffCollectionHistoryEntity> loanWriteOffHistoryEntityList);
}
