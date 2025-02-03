package net.celloscope.mraims.loanportfolio.features.passbook.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookGridViewUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassbookGridViewService implements PassbookGridViewUseCase {

    private final PassbookPersistencePort port;

    private final ModelMapper modelMapper;

    @Override
    public Mono<PassbookGridViewResponseDTO> passbookGridViewData(PassbookGridViewQueryDTO queryDTO) {
        return port.findPassbookGridViewData(queryDTO)
                .doOnNext(d -> log.info("1. Data received from database: {}", d.toString()))
                .collectList()
                .map(d -> PassbookGridViewResponseDTO.builder()
                        .data(d)
                        .officeId(queryDTO.getOfficeId())
                        .fieldOfficerId(d.get(0).getFieldOfficerId())
                        .fieldOfficerNameEn(d.get(0).getFieldOfficerNameEn())
                        .fieldOfficerNameBn(d.get(0).getFieldOfficerNameBn())
                        .build());
    }
}
