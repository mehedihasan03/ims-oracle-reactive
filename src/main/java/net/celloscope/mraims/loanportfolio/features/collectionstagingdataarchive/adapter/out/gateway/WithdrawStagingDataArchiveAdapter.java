package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository.WithdrawStagingDataArchiveRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.WithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Qualifier("WithdrawStagingDataArchiveAdapter")
public class WithdrawStagingDataArchiveAdapter implements WithdrawStagingDataPersistencePort {

    private final ModelMapper modelMapper;

    private final WithdrawStagingDataArchiveRepository repository;

    public WithdrawStagingDataArchiveAdapter(ModelMapper modelMapper, WithdrawStagingDataArchiveRepository repository) {
        this.modelMapper = modelMapper;
        this.repository = repository;
    }

    @Override
    public Flux<WithdrawStagingData> getWithdrawStagingDataByOfficeId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, WithdrawStagingData.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteWithdrawStagingDataById(String id) {
        return this.repository.deleteByWithdrawStagingDataId(id)
                .mapNotNull(voidResult -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }
}
