package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.SamityMemberCount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataGenerationStatusDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingProcessTrackerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStagingProcessTrackerPersistencePort {

    Flux<StagingDataGenerationStatusDTO> getStagingDataGenerationStatusFlux(String officeId);

    Mono<Integer> getTotalMemberForOneSamity(String samityId);

    Flux<StagingDataGenerationStatusDTO> saveProcessTrackerWithWaitingStatus(List<StagingDataGenerationStatusDTO> statusList);

    Mono<StagingDataGenerationStatusDTO> updateProcessTrackerStatusToProcessing(StagingDataGenerationStatusDTO statusDTO);

    Mono<StagingDataGenerationStatusDTO> updateProcessTrackerStatusToFinishedBySamity(StagingDataGenerationStatusDTO statusDTO);

    Flux<StagingDataGenerationStatusDTO> getStagingDataGenerationStatusResponse(String officeId);

    Mono<StagingProcessTrackerDTO> getStagingProcessTrackerEntityBySamityId(String samityId);

    Mono<Integer> getTotalMemberOfSamityToGenerateStagingData(String samityId);

    Flux<SamityMemberCount> getTotalMemberListOfSamityByOfficeId(String officeId);

    Mono<String> deleteStagingProcessTrackerByManagementProcessId(String managementProcessId);

    Mono<List<String>> getAllSamityIdListByOfficeId(String officeId);

    Mono<List<StagingProcessTrackerEntity>> getAllStagingProcessTrackerEntityByManagementProcessId(String managementProcessId);
    Mono<String> deleteAllStagingProcessTrackerEntityByManagementProcessId(String managementProcessId);

//    Process Management v2
    Flux<StagingProcessTrackerEntity> getStagingProcessTrackerEntityBySamityIdList(List<String> samityIdList);

    Flux<String> getSamityIdListByOfficeId(String managementProcessId, String officeId);

    Mono<List<StagingProcessTrackerEntity>> saveStagingProcessTrackerEntityList(List<StagingProcessTrackerEntity> stagingProcessTrackerEntityList);

    Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityToProcessing(String samityId, String status);

    Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityToFinished(String samityId, String status, Integer totalMemberStaged, Integer totalAccountStaged);

    Mono<StagingProcessTrackerEntity> editUpdateAndDeleteProcessTracker(StagingProcessTrackerEntity trackerEntity, String loginId, String remarks);

    Mono<StagingProcessTrackerEntity> updateProcessTrackerEntityForRegeneration(StagingProcessTrackerEntity trackerEntity, String loginId);

    Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityListForOffice(String managementProcessId, String officeId);

    Flux<StagingProcessTrackerEntity> getStagingProcessEntityListBySamityIdList(String managementProcessId, List<String> samityIdList);

    Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityListForFieldOfficer(String managementProcessId, String fieldOfficerId);

    Mono<StagingProcessTrackerEntity> getStagingProcessEntityForSamity(String managementProcessId, String samityId);

    Flux<StagingProcessTrackerEntity> getStagingProcessEntityByOffice(String managementProcessId, String officeId);

    Mono<List<String>> updateStagingProcessTrackerDataToEditHistoryTable(String managementProcessId, String processId, String officeId);

    Mono<String> updateProcessTrackerForDownloadedStagingDataDeletionByFieldOfficer(String managementProcessId, String fieldOfficerId);

    Mono<List<StagingProcessTrackerEntity>> UpdateStagingProcessEntityListForDownloadByFieldOfficer(String managementProcessId, String fieldOfficerId, String loginId);

    Mono<String> resetStagingProcessTrackerEntriesByManagementProcessId(String managementProcessId);
}
