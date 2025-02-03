package net.celloscope.mraims.loanportfolio.features.transaction.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionGridViewUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionGridViewService implements TransactionGridViewUseCase {

    private final TransactionPersistencePort port;

    private final ModelMapper modelMapper;


    @Override
    public Mono<TransactionGridViewResponseDTO> transactionGridViewData(TransactionGridViewQueryDTO queryDTO) {

        return port.findTransactionGridViewData(queryDTO)
                .doOnNext(d -> log.info("1. Data received from database: {}", d.toString()))
                .collectList()
                .map(d -> TransactionGridViewResponseDTO.builder()
                        .data(d)
                        .officeId(queryDTO.getOfficeId())
                        .fieldOfficerId(d.get(0).getFieldOfficerId())
                        .fieldOfficerNameEn(d.get(0).getFieldOfficerNameEn())
                        .fieldOfficerNameBn(d.get(0).getFieldOfficerNameBn())
                        .build());
    }
}
