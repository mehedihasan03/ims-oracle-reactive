package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.*;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanWaiverUseCase {

    Mono<LoanWaiverGridViewResponseDTO> getLoanWaiverList(LoanWaiverGridViewRequestDTO requestDto);

    Mono<LoanWaiverDetailViewResponseDTO> getLoanWaiverDetailView(LoanWaiverDetailViewRequestDTO requestDto);

    Mono<LoanWaiverMemberDetailViewResponseDTO> getLoanWaiverMemberDetailView(LoanWaiverMemberDetailViewRequestDTO requestDto);

    Mono<LoanWaiverResponseDTO> createLoanWaiver(LoanWaiverCreateUpdateRequestDTO requestDto);

    Mono<LoanWaiverResponseDTO> updateLoanWaiver(LoanWaiverCreateUpdateRequestDTO requestDto);

    Mono<LoanWaiverResponseDTO> submitLoanWaiver(LoanWaiverSubmitRequestDTO requestDto);

    Mono<List<LoanWaiverDTO>> getLoanWaiverDataBySamityId(String samityId, String managementProcessId);

    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<LoanWaiverDTO>> getAllLoanWaiverDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateLoanWaiverDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);
}
