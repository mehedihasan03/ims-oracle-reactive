package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.dto.StagingDataTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataDetailViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataMemberInfoDetailViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStagingDataPersistencePort {
	
	Flux<StagingData> getStagingDataMemberInfoBySamityId(String samityId);
	
	Flux<StagingData> save(List<StagingData> list);
	
	Mono<StagingDataDetailViewResponseDTO> getSamityInfoForStagingDataDetailView(String samityId);
	
	Flux<MemberInfoDTO> getMemberInfoListForStagingDataDetailViewBySamityId(String samityId);
	
	Mono<StagingDataMemberInfoDetailViewResponseDTO> getSamityInfoForStagingDataDetailViewByAccountId(String accountId);
	
	Mono<MemberInfoDTO> getMemberInfoForStagingDataDetailViewByAccountId(String accountId);
	
	Flux<StagingDataTransactionDTO> getStagingDataBySamityId(String samityId);
	
	Mono<StagingDataMemberInfoDetailViewResponseDTO> getSamityInfoForStagingDataDetailViewByMemberId(String memberId);
	
	Mono<MemberInfoDTO> getMemberInfoForStagingDataDetailViewByMemberId(String memberId);
	
	Flux<StagingData> getStagingDataByFieldOfficer(String fieldOfficerId);
	
	Flux<String> findSamityIdListByFieldOfficerIdList(List<String> fieldOfficerIdList, Integer limit, Integer offset);
	
	Mono<Integer> getTotalCountOfStagingDataByFieldOfficerList(List<String> fieldOfficerIdList);
	
	Flux<StagingData> getStagingDataBySamity(String samityId);
	
	Flux<String> findSamityIdListByFieldOfficerIdListForSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset);
	
	Flux<String> findSamityIdListByFieldOfficerIdListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset);
	
	
	Mono<Integer> getTotalCountByFieldOfficerListForSamityDay(List<String> fieldOfficerIdList, String samityDay);
	
	Mono<Integer> getTotalCountByFieldOfficerListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay);
	
	Flux<String> getSamityIdListByFieldOfficer(String fieldOfficerId);

	Mono<String> deleteStagingDataByManagementProcessId(String managementProcessId);

	Mono<List<StagingDataEntity>> getAllStagingDataByManagementProcessId(String managementProcessId);


	Mono<String> deleteAllStagingDataByManagementProcessId(String managementProcessId);

//	Process Management v2
	Mono<List<String>> saveAllStagingData(List<StagingData> stagingDataList);

	Mono<List<String>> editUpdateAndDeleteStagingDataOfASamity(String processId, String samityId);

    Mono<StagingData> getStagingDataByMemberId(String memberId);

    Mono<List<String>> updateStagingDataToEditHistoryTable(String managementProcessId, String processId, List<String> samityIdList);

    Flux<StagingData> getAllStagingDataBySamity(String managementProcessId, String samityId);

	Mono<List<String>> getMemberIdListFromSatgingDataBySamityIdList(List<String> samityIdList);

	Mono<StagingData> getStagingDataByStagingDataId(String stagingDataId);
}
