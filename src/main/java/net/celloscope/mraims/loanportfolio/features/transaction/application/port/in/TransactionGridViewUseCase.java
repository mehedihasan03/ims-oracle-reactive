package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in;

import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionGridViewResponseDTO;
import reactor.core.publisher.Mono;

public interface TransactionGridViewUseCase {

    Mono<TransactionGridViewResponseDTO> transactionGridViewData(TransactionGridViewQueryDTO queryDTO);
}
