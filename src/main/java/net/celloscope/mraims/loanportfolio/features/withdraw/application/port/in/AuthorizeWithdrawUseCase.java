package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in;

import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.AuthorizeWithdrawCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.AuthorizeWithdrawResponseDTO;
import reactor.core.publisher.Mono;

public interface AuthorizeWithdrawUseCase {
    Mono<AuthorizeWithdrawResponseDTO> authorizeWithdraw(AuthorizeWithdrawCommand command);
}
