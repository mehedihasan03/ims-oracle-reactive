package net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.entity.PassbookHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.repository.PassbookHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.out.PassbookHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.domain.PassbookHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class PassbookHistoryPersistenceAdapter implements PassbookHistoryPersistencePort {
    private final PassbookHistoryRepository repository;
    private final ModelMapper modelMapper;

    public PassbookHistoryPersistenceAdapter(PassbookHistoryRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<Boolean> archivePassbookHistory(List<PassbookHistory> passbookHistories) {
        return Flux.fromIterable(passbookHistories)
                .map(passbookHistory -> {
                    PassbookHistoryEntity entity = modelMapper.map(passbookHistory, PassbookHistoryEntity.class);
                    entity.setOid(null);
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .then(Mono.just(true));
    }
}
