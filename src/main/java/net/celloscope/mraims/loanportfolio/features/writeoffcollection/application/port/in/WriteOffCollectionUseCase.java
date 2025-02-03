package net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in;

import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateAuthorizeCommand;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.dto.LoanWriteOffCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.LoanWriteOffCollection;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WriteOffCollectionUseCase {

    Mono<WriteOffCollectionAccountDataResponseDto> getWriteOffCollectionAccountData(WriteOffCollectionAccountDataRequestDto request);

    Mono<LoanWriteOffDetailsResponseDto> getWriteOffCollectionAccountDataV2(WriteOffCollectionAccountDataRequestDto request);

    Mono<LoanWriteOffMsgCommonResponseDto> createWriteOffCollection(WriteOffCollectionAccountDataRequestDto request);

    Mono<LoanWriteOffMsgCommonResponseDto> updateWriteOffCollection(WriteOffCollectionAccountDataRequestDto request);

    Mono<LoanWriteOffMsgCommonResponseDto> submitLoanWriteOffCollectionData(WriteOffCollectionAccountDataRequestDto request);
    Mono<LoanWriteOffGridViewByOfficeResponseDto> getCollectedWriteOffAccountData(LoanWriteOffGridViewByOfficeRequestDto request);

    Mono<LoanWriteOffDetailsResponseDto> getDetailsCollectedWriteOffData(WriteOffCollectionAccountDataRequestDto request);

    Mono<List<LoanWriteOffCollectionDTO>> getLoanWriteOffDataBySamityId(String samityId, String managementProcessId);

    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);

    Mono<List<LoanWriteOffCollectionDTO>> getAllLoanWriteOffCollectionDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateLoanWriteOffCOllectionDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> authorizeSamityForLoanWriteOff(LoanWriteOffAuthorizationCommand loanWriteOffAuthorizationCommand);
    Mono<LoanWriteOffCollection> updateLoanWriteOffDataOnUnAuthorization(LoanWriteOffCollectionDTO writeOffCollection);

    Mono<LoanWriteOffGridViewByOfficeResponseDto> getWriteOffEligibleAccountList(LoanWriteOffGridViewByOfficeRequestDto request);
    Mono<LoanWriteOffMsgCommonResponseDto> deleteWriteOffData(WriteOffCollectionAccountDataRequestDto requestDto);
}