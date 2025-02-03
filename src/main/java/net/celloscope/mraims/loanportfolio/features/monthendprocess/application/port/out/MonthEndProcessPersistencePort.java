package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessTracker;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface MonthEndProcessPersistencePort {

    Flux<MonthEndProcessTracker> getMonthEndProcessTrackerEntriesForOffice(String officeId, Integer limit, Integer offset);
    Flux<MonthEndProcessTracker> getMonthEndProcessTrackerEntriesByManagementProcessForOffice(String managementProcessId, String officeId);

    Mono<List<MonthEndProcessTracker>> insertMonthEndProcessTrackerEntryList(List<MonthEndProcessTracker> monthEndProcessTrackerList);
    Mono<String> saveMonthEndProcessTrackerEntriesIntoHistory(List<MonthEndProcessTracker> monthEndProcessTrackerList);
    Mono<String> deleteFromMonthEndProcessTrackerByManagementProcessId(String managementProcessId);

    Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerStatus(String managementProcessId, String officeId, String transactionCode, String status);
    Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerStatusForRetry(String managementProcessId, String officeId, String transactionCode, String status, String loginId);

    Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerAisRequestData(String managementProcessId, String officeId, String transactionCode, String aisRequest);
    Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerAisResponseData(String managementProcessId, String officeId, String transactionCode, String aisResponse);

    Flux<MonthEndProcessData> getMonthEndProcessDataEntriesForOffice(String managementProcessId, String officeId);

    Mono<List<MonthEndProcessTracker>> getMonthEndProcessTrackerForManagementProcessId(String managementProcessId);

    Mono<List<MonthEndProcessData>> getMonthEndProcessDataForManagementProcessId(String managementProcessId);

    Mono<List<MonthEndProcessData>> insertMonthEndProcessDataList(List<MonthEndProcessData> monthEndProcessDataList);

    Mono<MonthEndProcessData> updateMonthEndProcessDataForProcessing(MonthEndProcessData monthEndProcessData);

    Mono<MonthEndProcessData> updateMonthEndProcessDataForTotalAccruedAndPostingAmount(MonthEndProcessData monthEndProcessData, BigDecimal totalAccruedAmount, BigDecimal totalPostingAmount);

    Mono<MonthEndProcessData> updateMonthEndProcessDataForTotalPostingAmount(MonthEndProcessData monthEndProcessData, BigDecimal totalPostingAmount);
    Mono<MonthEndProcessData> updateMonthEndProcessDataForFailed(MonthEndProcessData monthEndProcessData, String remarks);

    Mono<MonthEndProcessData> updateMonthEndProcessDataForFinished(MonthEndProcessData monthEndProcessData);

    Mono<String> getMonthEndProcessIdForOffice(String managementProcessId, String officeId);

    Mono<MonthEndProcessData> updateMonthEndProcessDataForRetry(String managementProcessId, String samityId, String loginId);

    Mono<String> deleteAllByManagementProcessId(String managementProcessId);
}
