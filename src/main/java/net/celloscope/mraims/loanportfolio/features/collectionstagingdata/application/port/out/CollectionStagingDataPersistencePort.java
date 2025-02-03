package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionDataVerifyDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.RejectionCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.UnauthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface CollectionStagingDataPersistencePort {
	Mono<List<CollectionStagingData>> saveAllCollectionData(List<CollectionStagingData> dataList, String isUploaded);
	
	Mono<Integer> updateAllCollectionDataBySamityForAuthorization(String samityId, String collectionType, String approvedBy);
	
	Flux<CollectionStagingDataEntity> getAllCollectionDataBy(String samityId, String collectionType);
	
	Flux<CollectionStagingDataEntity> getAllCollectionData(String samityId, String collectionType);
	
	Flux<CollectionStagingData> getCollectionStagingDataByStagingDataId(String stagingDataId);
	
	Flux<CollectionStagingData> getCollectionStagingDataByLocanOrSavingsAccount(String account);
	
	Mono<Long> getCountOfCollectionStagingDataByAccountIdList(List<String> account);
	
	Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId);
	
	Mono<CollectionStagingData> getCollectionStagingDataBySavingsAccountId(String savingsAccountId);
	
	Mono<CollectionDataVerifyDTO> getCollectionDataToVerifyPayment(CollectionDataVerifyDTO command);
	
	Mono<BigDecimal> getTotalCollectionBySamity(String samityId);
	
	Mono<List<CollectionStagingData>> editUpdateAllCollectionData(List<CollectionStagingData> dataList, String loginId);

	Mono<List<CollectionStagingData>> editUpdateCollectionDataByManagementProcessId(List<CollectionStagingData> dataList, String managementProcessId, String processId, String loginId);

    Mono<List<CollectionStagingData>> removeCollectionData(String managementProcessId, String processId);

    Mono<String> updateStatusToSubmitCollectionDataForAuthorizationByManagementProcessId(String managementProcessId, String processId, String loginId);

	Mono<Integer> lockCollectionBySamity(String samityId, String lockedBy);
	
	Mono<Integer> unlockCollectionBySamity(String samityId);
	
	Mono<Integer> rejectCollectionBySamity(RejectionCollectionCommand command);
	
	Flux<CollectionStagingDataEntity> editCommitForCollectionDataBySamity(CollectionDataRequestDTO command);

	Mono<List<String>> getStagingDataIdListBySamity(String samityId);
	
	Mono<UnauthorizeCollectionCommand> unauthorizeBySamity(UnauthorizeCollectionCommand command);

	Mono<List<CollectionStagingData>> getAllCollectionDataBySamityId(String samityId);

	Mono<List<CollectionStagingDataEntity>> getAllCollectionDataByManagementProcessId(String managementProcessId);

	Mono<String> deleteAllCollectionDataByManagementProcessId(String managementProcessId);

    Flux<CollectionStagingData> getAllCollectionDataBySamity(String samityId);

	Mono<CollectionStagingData> getOneCollectionBySamity(String samityId);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

	Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

	Mono<List<CollectionStagingData>> saveAllCollectionDataToDatabase(List<CollectionStagingData> collectionStagingDataList);

	Mono<List<CollectionStagingData>> getCollectionStagingDataBySamityIdList(List<String> samityIdList);

	Mono<String> validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId);
	Mono<String> validateAndUpdateCollectionStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Flux<CollectionStagingData> getAllCollectionDataBySamityIdList(List<String> samityIdList);

	Mono<String> validateAndUpdateCollectionDataForSubmission(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

	Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId, String processId);

	Flux<CollectionStagingData> getAllCollectionStagingDataByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

	Mono<List<CollectionStagingData>> saveAllCollectionStagingDataIntoEditHistory(List<CollectionStagingData> data, String loginId);

	Mono<Void> deleteAllCollectionStagingData(List<CollectionStagingData> data);

	Flux<CollectionStagingData> getAllCollectionStagingDataByManagementProcessIdAndLoginId(String managementProcessId, String collectionType, String loginId, Integer limit, Integer offset);

	Mono<CollectionStagingData> getCollectionStagingDataByOid(String oid);
	Mono<Void> deleteSpecialCollectionStagingDataByOid(String oid);

	Mono<CollectionStagingData> saveCollectionStagingData(CollectionStagingData collectionStagingData);
	public Mono<String> validateAndUpdateCollectionDataForSubmissionByOid(String loginId, String oid);

	Flux<CollectionStagingData> getAllCollectionDataByOidList(List<String> oid);

	Mono<Long> collectionStagingDataCount(String managementProcessId, String collectionType, String loginId);

	Mono<String> deleteCollectionDataByManagementProcessIdAndProcessId(String managementProcessId, String processId);

	Mono<Long> collectionStagingDataCount(String managementProcessId, String samityid);
}

