package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.ResetCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.ResetCollectionResponseDto;
import reactor.core.publisher.Mono;

public interface ResetCollectionUseCase {

    Mono<ResetCollectionResponseDto> resetCollection(ResetCollectionCommand command);
    Mono<ResetCollectionResponseDto> resetSpecialCollection(String oid);

}
