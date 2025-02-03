package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in;

import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.*;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanRebateUseCase {
    Mono<LoanRebateResponseDTO> getLoanRebateInfoByLoanAccountId(ServerRequest serverRequest);
    Mono<GetDetailsByMemberResponseDto> collectAccountDetailsByMemberId(GetDetailsByMemberRequestDto requestDto);
    Mono<SettleRebateResponseDto> settleRebate(SettleRebateRequestDto requestDto);
    Mono<LoanRebateGridViewByOfficeResponseDto> getLoanRebateGridViewByOfficeId(LoanRebateGridViewByOfficeRequestDto requestDto);
    Mono<GetLoanRebateDetailResponseDto> getLoanRebateDetail(GetLoanRebateDetailRequestDto requestDto);
    Mono<SettleRebateResponseDto> submitLoanRebate(GetLoanRebateDetailRequestDto requestDto);
    Mono<SettleRebateResponseDto> updateLoanRebate(SettleRebateRequestDto requestDto);
    Mono<List<LoanRebateDTO>> getLoanRebateDataBySamityId(String samityId, String managementProcessId);
    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);
    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);
    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);
    Mono<List<LoanRebateDTO>> getAllLoanRebateDataBySamityIdList(List<String> samityIdList);
    Mono<String> validateAndUpdateLoanRebateDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);
    Mono<String> authorizeSamityForLoanRebate(LoanRebateAuthorizeCommand loanRebateAuthorizeCommand);
    Mono<LoanRebateDTO> updateLoanRebateDataOnUnAuthorization(LoanRebateDTO loanRebateDTO);
    Mono<SettleRebateResponseDto> resetLoanRebate(GetLoanRebateDetailRequestDto requestDto);
}
