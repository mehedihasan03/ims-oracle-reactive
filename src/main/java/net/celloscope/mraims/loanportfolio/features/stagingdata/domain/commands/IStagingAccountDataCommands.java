package net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands;

import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface IStagingAccountDataCommands {
	Mono<StagingAccountData> generateStagingAccountDataForOneActiveLoanAccount(Mono<StagingAccountData> stagingAccountData, Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate);
}
