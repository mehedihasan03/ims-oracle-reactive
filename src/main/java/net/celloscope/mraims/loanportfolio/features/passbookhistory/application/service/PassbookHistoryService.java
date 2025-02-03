package net.celloscope.mraims.loanportfolio.features.passbookhistory.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.in.PassbookHistoryUseCase;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.out.PassbookHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.domain.PassbookHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PassbookHistoryService implements PassbookHistoryUseCase {
    private final PassbookHistoryPersistencePort port;
    private final ModelMapper modelMapper;

    public PassbookHistoryService(PassbookHistoryPersistencePort port, ModelMapper modelMapper) {
        this.port = port;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<Boolean> archivePassbookHistory(List<Passbook> passbookList, String loginId) {
        return Flux.fromIterable(passbookList)
                .map(passbook -> modelMapper.map(passbook, PassbookHistory.class))
                .map(passbookHistory -> {
                    passbookHistory.setArchivedBy(loginId);
                    passbookHistory.setArchivedOn(LocalDateTime.now());
                    return passbookHistory;
                })
                .collectList()
                .flatMap(port::archivePassbookHistory);
    }
}
