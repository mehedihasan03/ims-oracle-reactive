package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in;

import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookReportResponseDTO;
import reactor.core.publisher.Mono;

public interface PassbookReportUseCase {

    Mono<PassbookReportResponseDTO> getPassbookReportFromDB(PassbookReportQueryDTO queryDTO);

    Mono<PassbookReportResponseDTO> getPassbookReportFromDBV2(PassbookReportQueryDTO queryDTO);
}
