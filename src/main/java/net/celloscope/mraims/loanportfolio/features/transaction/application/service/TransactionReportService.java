package net.celloscope.mraims.loanportfolio.features.transaction.application.service;

import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionReportUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionReportResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransactionReportService implements TransactionReportUseCase {

    private final ModelMapper modelMapper;
    private final TransactionPersistencePort port;

    public TransactionReportService(TransactionPersistencePort port) {
        this.port = port;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public Mono<TransactionReportResponseDTO> getTransactionsReportFromDB(TransactionReportQueryDTO queryDTO) {
        return port.findTransactionReportData(queryDTO)
                .collectList()
                .flatMap(data -> {
                    TransactionReportResponseDTO responseDTO = TransactionReportResponseDTO.builder()
                            .data(data)
                            .samityId(queryDTO.getSamityId())
                            .totalCount(data.size())
                            .build();
                    return port.findSamityDetailsForTransactionReportData(queryDTO.getSamityId())
                            .mapNotNull(samity -> {
                                responseDTO.setSamityNameEn(samity.getSamityNameEn());
                                responseDTO.setSamityNameBn(samity.getSamityNameBn());
                                return responseDTO;
                            });
                });
    }
}
