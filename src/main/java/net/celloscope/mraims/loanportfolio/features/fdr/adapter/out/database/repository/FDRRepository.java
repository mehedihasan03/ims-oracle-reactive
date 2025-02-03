package net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRScheduleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface FDRRepository extends ReactiveCrudRepository<FDRScheduleEntity, String> {
    Flux<FDRScheduleEntity> findAllByInterestPostingDateAndStatus(LocalDate interestPostingDate, String status);
    Mono<FDRScheduleEntity> findBySavingsAccountIdAndInterestPostingDate(String savingsAccountId, LocalDate interestPostingDate);
    Mono<Boolean> existsDistinctBySavingsAccountId(String savingsAccountId);
    Flux<FDRScheduleEntity> findFDRScheduleEntityBySavingsAccountIdOrderByPostingNo(String savingsAccountId);
    Mono<FDRScheduleEntity> findFDRScheduleEntityBySavingsAccountIdOrderByPostingNoDesc(String savingsAccountId);
    Mono<FDRScheduleEntity> findFirstBySavingsAccountIdOrderByPostingNoDesc(String savingsAccountId);
}
