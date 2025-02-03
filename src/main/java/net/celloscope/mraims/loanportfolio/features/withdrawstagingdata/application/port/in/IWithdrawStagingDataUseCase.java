package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionDetailResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionGridResponse;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StageWithdrawResponseDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawPaymentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawStagingDataQueryDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IWithdrawStagingDataUseCase {

    Mono<WithdrawGridViewByOfficeResponseDTO> gridViewOfWithdrawStagingDataByOfficeV1(WithdrawStagingDataQueryDTO queryDTO);

    Mono<WithdrawGridViewByFieldOfficerResponseDTO> gridViewOfWithdrawStagingDataByFieldOfficerV1(WithdrawStagingDataQueryDTO queryDTO);

    Mono<AuthorizationWithdrawGridViewResponseDTO> gridViewOfWithdrawStagingDataForAuthorizationByOffice(WithdrawStagingDataQueryDTO queryDTO);

    Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataBySamityId(WithdrawStagingDataQueryDTO queryDTO);

    Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataByMemberId(WithdrawStagingDataQueryDTO queryDTO);

    Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataByAccountId(WithdrawStagingDataQueryDTO queryDTO);

    Mono<List<StagingWithdrawData>> getAllWithdrawStagingDataBySamity(String samityId);

//    Process Management v2
    Mono<WithdrawPaymentResponseDTO> withdrawPayment(WithdrawPaymentRequestDTO requestDTO);
    Mono<WithdrawPaymentResponseDTO> updateWithdrawPayment(WithdrawPaymentRequestDTO requestDTO);
    Mono<WithdrawPaymentResponseDTO> submitWithdrawPayment(WithdrawStagingDataQueryDTO requestDTO);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<Map<String, BigDecimal>> getTotalWithdrawAmountForSamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateWithdrawStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<List<StagingWithdrawData>> getAllWithdrawDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<WithdrawGridViewByOfficeResponseDTO> gridViewOfWithdrawStagingDataByOffice(WithdrawStagingDataQueryDTO queryDTO);

    Mono<WithdrawGridViewByFieldOfficerResponseDTO> gridViewOfWithdrawStagingDataByFieldOfficer(WithdrawStagingDataQueryDTO queryDTO);

    Mono<CollectionGridResponse> withdrawCollectionGridView(WithdrawStagingDataQueryDTO requestDTO);

    Mono<CollectionDetailResponse> withdrawCollectionDetailView(WithdrawStagingDataQueryDTO requestDTO);

    Mono<WithdrawPaymentResponseDTO> submitWithdrawDataEntity(WithdrawEntitySubmitRequestDto requestDto);

    Mono<WithdrawPaymentResponseDTO> deleteWithdrawData(WithdrawStagingDataQueryDTO requestDTO);

    Mono<StagingWithdrawData> getWithdrawStagingDataBySavingsAccountId(String savingsAccountId, String managementProcessId);
}
