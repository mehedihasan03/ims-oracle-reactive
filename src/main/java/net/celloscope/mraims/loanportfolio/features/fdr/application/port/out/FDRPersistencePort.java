package net.celloscope.mraims.loanportfolio.features.fdr.application.port.out;

import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface FDRPersistencePort {
    Mono<List<FDRSchedule>> saveInterestPostingSchedule(List<FDRSchedule> fdrScheduleList);
    Flux<FDRSchedule> getFDRInterestPostingSchedulesByDateAndStatus(LocalDate interestPostingDate, String status);
    Flux<FDRSchedule> getSchedule(String savingsAccountId);
    Mono<FDRSchedule> updateScheduleStatus(String savingsAccountId, LocalDate interestPostingDate, String updatedStatus);
    Mono<Boolean> checkIfScheduleExistsBySavingsAccountId(String savingsAccountId);
    Mono<Boolean> checkIfLastInterestPosting(String savingsAccountId, Integer postingNo);
}
