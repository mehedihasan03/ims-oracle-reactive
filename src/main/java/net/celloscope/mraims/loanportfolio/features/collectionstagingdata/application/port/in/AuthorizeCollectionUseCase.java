package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.AuthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.RejectionCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.UnauthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import reactor.core.publisher.Mono;

public interface AuthorizeCollectionUseCase {
    Mono<CollectionMessageResponseDTO> authorize(AuthorizeCollectionCommand command);

    Mono<CollectionMessageResponseDTO> reject(RejectionCollectionCommand command);

    Mono<CollectionMessageResponseDTO> unauthorize(UnauthorizeCollectionCommand command);
}
