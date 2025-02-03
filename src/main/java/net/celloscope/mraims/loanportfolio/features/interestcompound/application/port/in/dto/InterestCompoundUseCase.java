package net.celloscope.mraims.loanportfolio.features.interestcompound.application.port.in.dto;

import reactor.core.publisher.Mono;

public interface InterestCompoundUseCase {
    Mono<InterestCompoundDTO> saveInterestCompound(InterestCompoundDTO interestCompoundDTO);
    Mono<InterestCompoundDTO> getInterestCompoundBySavingsAccountId(String savingsAccountId);
}
