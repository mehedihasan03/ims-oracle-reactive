package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in;

import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookGridViewResponseDTO;
import reactor.core.publisher.Mono;

public interface PassbookGridViewUseCase {

    Mono<PassbookGridViewResponseDTO> passbookGridViewData(PassbookGridViewQueryDTO queryDTO);
}
