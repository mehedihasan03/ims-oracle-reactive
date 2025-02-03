package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionGridResponse;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEditRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustmentEditResponseDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface LoanAdjustmentUseCase {

    Mono<LoanAdjustmentResponseDTO> createLoanAdjustmentForMember(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> createLoanAdjustmentForRebate(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> updateLoanAdjustmentForMember(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> deleteLoanAdjustmentAndSaveToHistoryForMember(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> authorizeLoanAdjustmentForSamity(LoanAdjustmentRequestDTO requestDTO);
    Mono<LoanAdjustmentOfficeResponseDTO> gridViewOfLoanAdjustmentForOffice(LoanAdjustmentRequestDTO requestDTO);
    Mono<LoanAdjustmentFieldOfficerResponseDTO> gridViewOfLoanAdjustmentForFieldOfficer(LoanAdjustmentRequestDTO requestDTO);
    Mono<LoanAdjustmentSamityGridViewResponseDTO> detailViewOfLoanAdjustmentForSamity(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentMemberGridViewResponseDTO> detailViewOfLoanAdjustmentForAMember(LoanAdjustmentRequestDTO requestDTO);
    Mono<LoanAdjustmentDetailViewResponseDTO> detailsOfLoanAdjustmentCreationForAMember(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> submitLoanAdjustmentDataForAuthorization(LoanAdjustmentRequestDTO requestDTO);

    Mono<LoanAdjustmentResponseDTO> submitLoanAdjustmentDataForAuthorization(String managementProcessId, String processId, String loginId);

    Mono<List<LoanAdjustmentData>> getLoanAdjustmentDataBySamity(String samityId);

    Mono<Map<String, BigDecimal>> getTotalLoanAdjustmentAmountForSamityIdList(List<String> samityIdList);

    Mono<Map<String, BigDecimal>> getTotalSavingsAdjustmentAmountForSamityIdList(List<String> samityIdList);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<LoanAdjustmentMemberGridViewResponseDTO> getAdjustedLoanAccountListByManagementProcessId(LoanAdjustmentRequestDTO requestDTO);

    Mono<CollectionGridResponse> AdjustmentCollectionGridView(LoanAdjustmentRequestDTO loanAdjustmentRequestDTO);

    Mono<AdjustmentCollectionDetailViewResponse> AdjustmentCollectionDetailView(LoanAdjustmentRequestDTO loanAdjustmentRequestDTO);

    Mono<AdjustmentEditResponseDto> updateAdjustmentAmount(AdjustmentEditRequestDto requestDto);

    Mono<LoanAdjustmentResponseDTO> submitAdjustmentDataEntity(AdjustmentEntitySubmitRequestDto requestDto);

    Mono<LoanAdjustmentResponseDTO> resetLoanAdjustmentDataByEntity(LoanAdjustmentRequestDTO requestDTO);

    Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<LoanAdjustmentData> loanAdjustmentCollectionByLoanAccountId(String loanAccountId);

    Mono<Long> countLoanAdjustmentData(String managementProcessId, String samityId);
}
