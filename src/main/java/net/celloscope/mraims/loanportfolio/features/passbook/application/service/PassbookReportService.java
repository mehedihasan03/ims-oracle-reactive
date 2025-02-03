package net.celloscope.mraims.loanportfolio.features.passbook.application.service;

import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookReportUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookReportResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionReportResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class PassbookReportService implements PassbookReportUseCase {

    private final ModelMapper modelMapper;
    private final PassbookPersistencePort port;

    public PassbookReportService(PassbookPersistencePort port) {
        this.port = port;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public Mono<PassbookReportResponseDTO> getPassbookReportFromDB(PassbookReportQueryDTO queryDTO) {
        return port.findPassbookReportData(queryDTO)
                .collectList()
                .flatMap(data -> {
                    PassbookReportResponseDTO responseDTO = PassbookReportResponseDTO.builder()
                            .data(data)
                            .samityId(queryDTO.getSamityId())
                            .totalCount(data.size())
                            .build();
                    return port.findSamityDetailsForPassbookReportData(queryDTO.getSamityId())
                            .mapNotNull(samity -> {
                                responseDTO.setSamityNameEn(samity.getSamityNameEn());
                                responseDTO.setSamityNameBn(samity.getSamityNameBn());
                                return responseDTO;
                            });
                });
    }

    @Override
    public Mono<PassbookReportResponseDTO> getPassbookReportFromDBV2(PassbookReportQueryDTO queryDTO) {
        return port.findPassbookReportDataV2(queryDTO)
                .sort(Comparator.comparing(Passbook::getTransactionDate))
                .collectList()
                .flatMap(data -> {
                    PassbookReportResponseDTO responseDTO = PassbookReportResponseDTO.builder()
                            .data(data)
                            .samityId(queryDTO.getSamityId())
                            .totalCount(data.size())
                            .build();
                    return port.findSamityDetailsForPassbookReportData(queryDTO.getSamityId())
                            .mapNotNull(samity -> {
                                responseDTO.setSamityNameEn(samity.getSamityNameEn());
                                responseDTO.setSamityNameBn(samity.getSamityNameBn());
                                return responseDTO;
                            });
                });
    }
}
