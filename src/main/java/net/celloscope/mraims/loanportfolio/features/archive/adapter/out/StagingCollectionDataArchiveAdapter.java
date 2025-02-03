package net.celloscope.mraims.loanportfolio.features.archive.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.*;
import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository.*;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.out.IDataArchivePort;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository.DayEndProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.repository.DayForwardProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository.LoanWaiverHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository.LoanWriteOffCollectionHistoryRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
//@RequiredArgsConstructor
public class StagingCollectionDataArchiveAdapter implements IDataArchivePort {

    private final ICollectionHistoryRepositoryDelete collectionHistoryRepository;
    private final IWithdrawHistoryRepositoryDelete withdrawHistoryRepository;
    private final LoanAdjustmentDataHistoryRepositoryDelete loanAdjustmentDataHistoryRepository;
    private final IStagingAccountHistoryRepositoryDelete stagingAccountHistoryRepository;
    private final IStagingDataHistoryRepositoryDelete stagingDataHistoryRepository;
    private final IStagingProcessTrackerHistoryRepositoryDelete stagingProcessTrackerHistoryRepository;
    private final MonthEndProcessTrackerHistoryRepository monthEndProcessTrackerHistoryRepository;
    private final MonthEndProcessDataHistoryRepository monthEndProcessDataHistoryRepository;
    private final DayEndProcessTrackerHistoryRepository dayEndProcessTrackerHistoryRepository;
    private final DayForwardProcessTrackerHistoryRepository dayForwardProcessTrackerHistoryRepository;
    private final LoanWaiverHistoryRepository loanWaiverHistoryRepository;
    private final LoanRebateHistoryRepository loanRebateHistoryRepository;
    private final LoanWriteOffCollectionHistoryRepository loanWriteOffCollectionHistoryRepository;
    private final Gson gson;

    public StagingCollectionDataArchiveAdapter(ICollectionHistoryRepositoryDelete collectionHistoryRepository, IWithdrawHistoryRepositoryDelete withdrawHistoryRepository, LoanAdjustmentDataHistoryRepositoryDelete loanAdjustmentDataHistoryRepository, IStagingAccountHistoryRepositoryDelete stagingAccountHistoryRepository, IStagingDataHistoryRepositoryDelete stagingDataHistoryRepository, IStagingProcessTrackerHistoryRepositoryDelete stagingProcessTrackerHistoryRepository, Gson gson, MonthEndProcessTrackerHistoryRepository monthEndProcessTrackerHistoryRepository, MonthEndProcessDataHistoryRepository monthEndProcessDataHistoryRepository, DayForwardProcessTrackerHistoryRepository dayForwardProcessTrackerHistoryRepository, LoanWaiverHistoryRepository loanWaiverHistoryRepository, DayEndProcessTrackerHistoryRepository dayEndProcessTrackerHistoryRepository, LoanRebateHistoryRepository loanRebateHistoryRepository, LoanWriteOffCollectionHistoryRepository loanWriteOffCollectionHistoryRepository) {
        this.collectionHistoryRepository = collectionHistoryRepository;
        this.withdrawHistoryRepository = withdrawHistoryRepository;
        this.loanAdjustmentDataHistoryRepository = loanAdjustmentDataHistoryRepository;
        this.stagingAccountHistoryRepository = stagingAccountHistoryRepository;
        this.stagingDataHistoryRepository = stagingDataHistoryRepository;
        this.stagingProcessTrackerHistoryRepository = stagingProcessTrackerHistoryRepository;
        this.monthEndProcessTrackerHistoryRepository = monthEndProcessTrackerHistoryRepository;
        this.monthEndProcessDataHistoryRepository = monthEndProcessDataHistoryRepository;
        this.dayForwardProcessTrackerHistoryRepository = dayForwardProcessTrackerHistoryRepository;
        this.loanWaiverHistoryRepository = loanWaiverHistoryRepository;
        this.loanRebateHistoryRepository = loanRebateHistoryRepository;
        this.loanWriteOffCollectionHistoryRepository = loanWriteOffCollectionHistoryRepository;
        this.gson = CommonFunctions.buildGson(this);
        this.dayEndProcessTrackerHistoryRepository = dayEndProcessTrackerHistoryRepository;
    }


    @Override
    public Mono<String> saveCollectionIntoHistory(List<CollectionStagingDataHistoryEntity> historyEntityList) {
        return collectionHistoryRepository.saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("failed to save Collection history: {}", throwable.getMessage()))
                .map(list -> "Collection History Save Successful");
    }

    @Override
    public Mono<String> saveStagingAccountDataIntoHistory(List<StagingAccountDataHistoryEntity> historyEntityList) {
        return stagingAccountHistoryRepository.saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("failed to save staging account history: {}", throwable.getMessage()))
                .map(list -> "Staging account History Save Successful");
    }

    @Override
    public Mono<String> saveStagingDataIntoHistory(List<StagingDataHistoryEntity> historyEntityList) {
        return stagingDataHistoryRepository.saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("failed to save staging data history: {}", throwable.getMessage()))
                .map(list -> "Staging Data History Save Successful");
    }

    @Override
    public Mono<String> saveStagingProcessTrackerIntoHistory(List<StagingProcessTrackerHistoryEntity> historyEntityList) {
        return stagingProcessTrackerHistoryRepository.saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("failed to save staging process tracker history: {}", throwable.getMessage()))
                .map(list -> "Staging Process Tracker History Save Successful");
    }

    @Override
    public Mono<String> saveWithdrawIntoHistory(List<WithdrawStagingDataHistoryEntity> historyEntityList) {
        return withdrawHistoryRepository.saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("failed to save staging process tracker history: {}", throwable.getMessage()))
                .map(list -> "Withdraw Staging Data History Save Successful");
    }

    @Override
    public Mono<String> saveLoanAdjustmentIntoHistory(List<LoanAdjustmentDataHistoryEntity> loanAdjustmentDataHistoryEntities) {
        return loanAdjustmentDataHistoryRepository.saveAll(loanAdjustmentDataHistoryEntities)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Loan Adjustment history: {}", throwable.getMessage()))
                .map(list -> "Loan Adjustment History Save Successful");
    }

    @Override
    public Mono<String> saveMonthEndProcessTrackerIntoHistory(List<MonthEndProcessTrackerHistoryEntity> monthEndProcessTrackerEntityList) {
        return monthEndProcessTrackerHistoryRepository
                .saveAll(monthEndProcessTrackerEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Month End Process Tracker history: {}", throwable.getMessage()))
                .map(list -> "Month End Process Tracker History Save Successful");
    }

    @Override
    public Mono<String> saveMonthEndProcessDataIntoHistory(List<MonthEndProcessDataHistoryEntity> monthEndProcessDataEntityList) {
        return monthEndProcessDataHistoryRepository
                .saveAll(monthEndProcessDataEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Month End Process Data history: {}", throwable.getMessage()))
                .map(list -> "Month End Process Data History Save Successful");
    }

    @Override
    public Mono<String> saveDayEndProcessTrackerIntoHistory(List<DayEndProcessTrackerHistoryEntity> dayEndProcessTrackerEntityList) {
        return dayEndProcessTrackerHistoryRepository
                .saveAll(dayEndProcessTrackerEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Day End Process Tracker history: {}", throwable.getMessage()))
                .map(list -> "Day End Process Tracker History Save Successful")
                ;
    }

    @Override
    public Mono<String> saveLoanWaiverDataIntoHistory(List<LoanWaiverHistoryEntity> loanWaiverHistoryEntityList) {
        return loanWaiverHistoryRepository
                .saveAll(loanWaiverHistoryEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Loan Waiver history: {}", throwable.getMessage()))
                .map(list -> "Loan Waiver History Save Successful");
    }

    @Override
    public Mono<String> saveLoanRebateDataIntoHistory(List<LoanRebateHistoryEntity> loanRebateHistoryEntityList) {
        return loanRebateHistoryRepository
                .saveAll(loanRebateHistoryEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Loan Rebate history: {}", throwable.getMessage()))
                .map(list -> "Loan Rebate History Save Successful");
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getLoanAdjustmentDataByManagementProcessId(String managementProcessId) {
        return loanAdjustmentDataHistoryRepository.findAllByManagementProcessId(managementProcessId)
                .doOnNext(dto -> log.info("Loan Adjustment Data 1: {}", dto))
                .map(entity -> gson.fromJson(entity.toString(),
                        LoanAdjustmentData.class))
                .doOnNext(dto -> log.info("Loan Adjustment Data 2: {}", dto))
                .collectList();
    }

    @Override
    public Mono<String> saveDayForwardProcessTrackerIntoHistory(List<DayForwardProcessTrackerHistoryEntity> historyEntityList) {
        return dayForwardProcessTrackerHistoryRepository
                .saveAll(historyEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Day Forward Process Tracker history: {}", throwable.getMessage()))
                .map(list -> "Day Forward Process Tracker History Save Successful")
                ;
    }

    @Override
    public Mono<String> saveLoanWriteOffDataIntoHistory(List<LoanWriteOffCollectionHistoryEntity> loanWriteOffHistoryEntityList) {
        return loanWriteOffCollectionHistoryRepository.saveAll(loanWriteOffHistoryEntityList)
                .collectList()
                .doOnError(throwable -> log.error("Failed to save Loan Write Off history: {}", throwable.getMessage()))
                .map(list -> "Loan Write Off History Save Successful");
    }
}
