package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in;

import java.util.List;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IStagingDataUseCase {
        Mono<StagingDataGenerationResponseDTO> generateStagingDataAndStagingAccountData(StagingDataRequestDTO request);

        Mono<StagingDataDetailViewResponseDTO> getStagingDataDetailViewResponseBySamityId(StagingDataRequestDTO request);

        Mono<StagingDataMemberInfoDetailViewResponseDTO> getStagingDataDetailViewResponseByAccountId(StagingDataRequestDTO request);

        Mono<StagingDataGenerationStatusResponseDTO> getStagingDataGenerationStatusResponse(StagingDataRequestDTO requestDTO);

        Flux<StagingDataResponseDTO> getStagingDataBySamityId(String samityId);

        Mono<StagingDataMemberInfoDetailViewResponseDTO> getStagingDataDetailViewResponseByMemberId(StagingDataRequestDTO request);

        Flux<StagingData> getStagingDataByFieldOfficer(String fieldOfficerId);

        Mono<StagingDataSavingsAccountDetailDTO> getStagingDataSavingsAccountDetailBySavingsAccountId(String savingsAccountId);

        Flux<String> findSamityIdListByFieldOfficerIdList(List<String> fieldOfficerIdList, Integer limit, Integer offset);

        Mono<Integer> getTotalCountByFieldOfficerList(List<String> fieldOfficerIdList);

        Flux<StagingData> getStagingDataBySamity(String samityId);

        Flux<String> getSamityIdListByFieldOfficer(String fieldOfficerId);

        // Staging Data: Process Management v2
        Mono<StagingDataStatusByOfficeResponseDTO> gridViewOfStagingDataStatusByOffice(StagingDataRequestDTO requestDTO);

        Mono<StagingDataStatusByOfficeResponseDTO> gridViewOfStagingDataStatusByOfficeFilteredByFieldOfficer(StagingDataRequestDTO requestDTO);

        Mono<StagingDataStatusByFieldOfficerResponseDTO> gridViewOfStagingDataStatusByFieldOfficer(StagingDataRequestDTO requestDTO);

        Mono<StagingDataStatusByOfficeResponseDTO> generateStagingDataByOffice(StagingDataRequestDTO requestDTO);

        Mono<InvalidateSamityResponseDTO> invalidateStagingDataBySamityList(StagingDataRequestDTO requestDTO);

        Mono<StagingDataStatusByOfficeResponseDTO> regenerateStagingDataBySamityList(StagingDataRequestDTO requestDTO);

        Mono<StagingDataStatusByOfficeResponseDTO> deleteStagingDataByOffice(StagingDataRequestDTO requestDTO);

        Mono<List<StagingProcessTrackerEntity>> getStagingProcessTrackerListBySamityIdList(String managementProcessId, List<String> samityIdList);

        Mono<List<String>> getRegularSamityIdListByOfficeIdAndSamityDay(String managementProcessId, String officeId, String businessDay);

        Mono<List<String>> getRegularSamityIdListByFieldOfficerIdAndSamityDay(String managementProcessId, String fieldOfficerId, String businessDay);

        Mono<List<String>> getSpecialSamityIdListByOfficeIdAndSamityDay(String managementProcessId, String officeId,
                                                                        String businessDay);
        Mono<List<String>> getSpecialSamityIdListByFieldOfficerIdAndSamityDay(String managementProcessId, String officeId, String businessDay);

        Mono<StagingAccountData> getStagingAccountDataBySavingsAccountId(String savingsAccountId);

        Mono<StagingData> getStagingDataByMemberId(String memberId);

        Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityForFieldOfficer(String managementProcessId, String fieldOfficerId);

        Mono<StagingProcessTrackerEntity> getStagingProcessEntityForSamity(String managementProcessId, String samityId);

        Flux<StagingProcessTrackerEntity> getStagingProcessEntityByOffice(String managementProcessId, String officeId);

        Flux<StagingData> getAllStagingDataBySamity(String managementProcessId, String samityId);

        Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(String managementProcessId, List<String> memberIdList);

        Flux<StagingAccountData> getStagingAccountDataListByMemberId(String memberId);

        Flux<StagingAccountData> getStagingAccountDataBySavingsAccountIdList(List<String> savingsAccountIdList);

        Mono<List<StagingAccountData>> getAllStagingAccountDataBySamityIdList(List<String> samityIdList);

        Flux<String> getSamityIdListByOfficeId(String managementProcessId, String officeId);

        Mono<StagingDataDownloadByFieldOfficerResponseDTO> downloadStagingDataByFieldOfficer(StagingDataRequestDTO requestDTO);

        Mono<StagingDataResponseDTO> deleteStagingDataByFieldOfficer(StagingDataRequestDTO requestDTO);

        Mono<StagingData> getStagingDataByAccountId(String accountId);

        Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String loanAccountId);

        Mono<StagingData> getStagingDataByStagingDataId(String stagingDataId);

        Mono<SamityListResponseDTO> getStagedSamityListByFieldOfficerId(StagingDataRequestDTO stagingDataRequestDTO);

        Mono<String> resetStagingProcessTrackerEntriesByOfficeId(String officeId);
}
