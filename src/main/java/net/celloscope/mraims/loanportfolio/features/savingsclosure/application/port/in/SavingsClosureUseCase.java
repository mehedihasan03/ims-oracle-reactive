package net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in;

import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto.SavingsClosureCommand;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto.SavingsClosureDto;
import reactor.core.publisher.Mono;

public interface SavingsClosureUseCase {

    Mono<SavingsClosureDto> closeSavingsAccount(SavingsClosureCommand command);

    Mono<SavingsClosureDto> authorizeSavingsClosure(SavingsClosureCommand command);

    Mono<SavingsClosureDto> rejectSavingsClosure(SavingsClosureCommand command);
}
