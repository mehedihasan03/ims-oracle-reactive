package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.commands.DataArchiveCommandDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.response.DataArchiveResponseDto;
import reactor.core.publisher.Mono;

public interface DataArchiveUseCase {

    Mono<DataArchiveResponseDto> archive(DataArchiveCommandDto command);
}
