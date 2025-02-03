package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository.StagingAccountDataArchiveRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.StagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.StagingAccountData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Qualifier("StagingAccountDataArchiveAdapter")
public class StagingAccountDataArchiveAdapter implements StagingAccountDataPersistencePort {

    private final ModelMapper modelMapper;

    private final StagingAccountDataArchiveRepository repository;

    public StagingAccountDataArchiveAdapter(ModelMapper modelMapper, StagingAccountDataArchiveRepository repository) {
        this.modelMapper = modelMapper;
        this.repository = repository;
    }


    @Override
    public Flux<StagingAccountData> getStagingAccountDataByOfficeId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, StagingAccountData.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteStagingAccountDataById(String id) {
        return this.repository.deleteByStagingAccountDataId(id)
                .mapNotNull(voidResult -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }
}
