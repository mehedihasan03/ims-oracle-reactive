package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in;

import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionReportResponseDTO;
import reactor.core.publisher.Mono;

public interface TransactionReportUseCase {

    Mono<TransactionReportResponseDTO> getTransactionsReportFromDB(TransactionReportQueryDTO queryDTO);
}
