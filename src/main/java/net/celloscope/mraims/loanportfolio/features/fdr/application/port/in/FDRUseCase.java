package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in;

import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface FDRUseCase {
    Mono<FDRResponseDTO> getFDRSchedule(String savingsAccountId);
    Flux<FDRSchedule> getFDRInterestPostingSchedulesByDateAndStatus(LocalDate interestPostingDate, String status);
    Mono<FDRSchedule> updateScheduleStatus(String savingsAccountId, LocalDate interestPostingDate, String updatedStatus);
    Flux<FDRSchedule> postFDRInterestToAccount(LocalDate interestPostingDate, String loginId, String officeId);
    Mono<FDRResponseDTO> activateFDRAccount(FDRRequestDTO requestDTO);
    Mono<String> activateFDRAccount2(FDRRequestDTO requestDTO);
    Mono<String> accrueFDRInterest(LocalDate interestCalculationDate, String officeId, String loginId);




    Mono<FDRGridViewDTO> getFDRGridViewByOffice(FDRGridViewCommand command);
    Mono<FDRDetailViewDTO> getFDRDetailViewByAccountId(String savingsAccountId);
    Mono<FDRClosureDTO> closeFDRAccount(FDRClosureCommand command);
    Mono<FDRClosureDTO> authorizeFDRClosure(FDRAuthorizeCommand command);
    Mono<FDRClosureDTO> rejectFDRClosure(FDRAuthorizeCommand command);

    Mono<FDRClosureGridViewResponse> getFDRClosureGridViewByOffice(FDRGridViewCommand command);
    Mono<FDRClosureDetailViewResponse> getFDRClosureDetailViewBySavingsAccountId(String savingsAccountId);

    Mono<FDRClosureDTO> getFDRClosingInfoBySavingsAccountId(FDRClosureCommand command);
}
