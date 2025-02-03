package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.LockCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.LockCollectionResponseDto;
import reactor.core.publisher.Mono;

public interface LockCollectionUseCase {

    Mono<LockCollectionResponseDto> lockCollectionBySamity(LockCollectionCommand command);

    Mono<LockCollectionResponseDto> unlockCollectionBySamity(LockCollectionCommand command);
}
