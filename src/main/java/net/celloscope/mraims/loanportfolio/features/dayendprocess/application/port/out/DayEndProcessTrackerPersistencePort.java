package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out;

import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.DayEndProcessTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DayEndProcessTrackerPersistencePort {

    Flux<DayEndProcessTracker> getDayEndProcessTrackerEntriesForOffice(String managementProcessId, String officeId);

    Mono<List<DayEndProcessTracker>> saveDayEndProcessTrackerEntryList(List<DayEndProcessTracker> dayEndProcessTrackerList);

    Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryStatus(DayEndProcessTracker dayEndProcessTracker, String status);
    Mono<String> saveAISDayEndProcessTrackerIntoHistory(DayEndProcessTracker dayEndProcessTracker);
    Mono<String> saveMISDayEndProcessTrackerIntoHistory(DayEndProcessTracker dayEndProcessTracker);

    Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryAisRequest(DayEndProcessTracker dayEndProcessTracker, JournalRequestDTO aisRequest);
    Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryAisResponse(DayEndProcessTracker dayEndProcessTracker, String aisResponse);

    Mono<List<DayEndProcessTracker>> updateDayEndProcessEntryListForRetry(List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId);

    Mono<String> deleteDayEndProcessTrackerEntryListForOffice(String managementProcessId, String officeId);
    Mono<String> deleteAllByManagementProcessId(String managementProcessId);

    Mono<List<DayEndProcessTrackerEntity>> getAllDayEndProcessTrackerDataByManagementProcessId(String managementProcessId);

}
