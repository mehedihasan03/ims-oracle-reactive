package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionDetailResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionGridResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionStagingRequestDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CollectionStagingDataQueryUseCase {

    //    Regular Collection Grid View
    Mono<CollectionGridViewByOfficeResponseDTO> gridViewOfRegularCollectionByOffice(CollectionDataRequestDTO request);

    Mono<CollectionGridViewByFieldOfficerResponseDTO> gridViewOfRegularCollectionByFieldOfficer(CollectionDataRequestDTO request);

    //    Special Collection Grid View
    Mono<CollectionGridViewByOfficeResponseDTO> gridViewOfSpecialCollectionByOffice(CollectionDataRequestDTO request);

    Mono<CollectionGridViewByFieldOfficerResponseDTO> gridViewOfSpecialCollectionByFieldOfficer(CollectionDataRequestDTO request);

    //    Collection Authorization Grid View
    Mono<AuthorizationGridViewResponseDTO> gridViewOfRegularCollectionAuthorizationByOffice(CollectionDataRequestDTO request);

    Mono<AuthorizationGridViewResponseDTO> gridViewOfSpecialCollectionAuthorizationByOffice(CollectionDataRequestDTO request);


    //  Old Methods, need to refactor
    Mono<CollectionStagingDataDetailViewResponse> getCollectionStagingDataDetailViewBySamityId(CollectionDataRequestDTO request);

    Mono<CollectionStagingDataAccountDetailViewResponse> getCollectionStagingDataDetailViewByAccountId(CollectionDataRequestDTO request);

    Flux<CollectionStagingDataResponseDTO> getCollectionStagingDataForSamityMembers(String stagingDataId);

    Mono<AuthorizationGridViewResponseDTO> gridViewCollectionAuthorizationDataByOfficeId(CollectionDataRequestDTO request);

    Mono<CollectionStagingDataAccountDetailViewResponse> getCollectionStagingDataDetailViewByMemberId(CollectionDataRequestDTO request);


    Mono<CollectionOfficerDetailViewResponseDTO> getCollectionDetailViewByFieldOfficer(CollectionDataRequestDTO request);

    Mono<CollectionMessageResponseDTO> editCommitForCollectionDataBySamity(CollectionDataRequestDTO request);

    Mono<List<CollectionStagingData>> getAllCollectionStagingDataBySamity(String samityId);

//    Mono<AuthorizationGridViewResponseDTO> gridViewOfRegularCollectionAuthorizationByOffice(StagingDataGridViewQueryDto request);

    //    Process Management v2
    Mono<CollectionGridViewOfOfficeResponseDTO> gridViewOfRegularCollectionForOffice(CollectionDataRequestDTO request);

    Mono<CollectionGridViewOfFieldOfficerResponseDTO> gridViewOfRegularCollectionForFieldOfficer(CollectionDataRequestDTO request);

    Mono<CollectionGridViewOfOfficeResponseDTO> gridViewOfSpecialCollectionForOffice(CollectionDataRequestDTO request);

    Mono<CollectionGridViewOfFieldOfficerResponseDTO> gridViewOfSpecialCollectionForFieldOfficer(CollectionDataRequestDTO request);

    Mono<CollectionGridViewOfOfficeResponseDTO> listOfSpecialCollectionSamity(CollectionDataRequestDTO request);

    Mono<CollectionMessageResponseDTO> submitCollectionDataForAuthorizationBySamity(CollectionDataRequestDTO requestDTO);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<Map<String, BigDecimal>> getTotalCollectionAmountForSamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(String managementProcessId, String samityIdList, String loginId);

    Mono<String> validateAndUpdateCollectionStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<List<CollectionStagingData>> getAllCollectionDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String loanAccountId, String managementProcessId);

    Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId, String managementProcessId, String processId, String version);

    Mono<CollectionGridResponse> getCollectionStagingGridView(CollectionStagingRequestDto request);

    Mono<CollectionDetailResponse> getCollectionStagingDetailView(CollectionStagingRequestDto request);

    Mono<CollectionDetailResponse> editSpecialCollectionData(CollectionStagingRequestDto request);
    Mono<CollectionMessageResponseDTO> submitCollectionDataForAuthorizationByOid(CollectionDataRequestDTO requestDTO);

    Mono<CollectionMessageResponseDTO> submitCollectionDataEntity(CollectionEntitySubmitRequestDto requestDTO);

    Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId);

    Mono<CollectionStagingData> getCollectionStagingDataByManagementProcessIdAndProcessId(String loanAccountId, String managementProcessId, String processId);

    Mono<Long> countCollectionStagingData(String managementProcessId, String samityId);
}
