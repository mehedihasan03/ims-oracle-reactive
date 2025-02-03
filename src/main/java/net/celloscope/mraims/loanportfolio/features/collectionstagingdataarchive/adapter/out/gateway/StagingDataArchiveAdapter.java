package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository.StagingDataArchiveRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.StagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.StagingData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Qualifier("StagingDataArchiveAdapter")
public class StagingDataArchiveAdapter implements StagingDataPersistencePort {

    private final ModelMapper modelMapper;

    private final StagingDataArchiveRepository repository;

    public StagingDataArchiveAdapter(ModelMapper modelMapper, StagingDataArchiveRepository repository) {
        this.modelMapper = modelMapper;
        this.repository = repository;
    }

    @Override
    public Flux<StagingData> getStagingDataByOfficeId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, StagingData.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteStagingDataById(String id) {
        return this.repository.deleteByStagingDataId(id)
                .mapNotNull(voidResult -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }
}
