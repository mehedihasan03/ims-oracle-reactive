package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.WithdrawStagingDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository.WithdrawStagingDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.WithdrawStagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.WithdrawStagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawStagingDataHistoryAdapter implements WithdrawStagingDataHistoryPersistencePort {

    private final ModelMapper modelMapper;

    private final WithdrawStagingDataHistoryRepository repository;

    @Override
    public Mono<Boolean> saveWithdrawStagingDataHistory(List<WithdrawStagingDataHistory> historyData) {
        return Mono.just(historyData.stream()
                        .map(archiveData -> this.modelMapper.map(archiveData, WithdrawStagingDataHistoryEntity.class))
                        .toList())
                .map(this.repository::saveAll)
                .mapNotNull(entity -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Flux<WithdrawStagingDataHistory> getLiveWithdrawStagingDataId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, WithdrawStagingDataHistory.class))
                .doOnError(error -> log.error(error.getMessage()));
    }
}
