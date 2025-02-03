package net.celloscope.mraims.loanportfolio.features.cancel.adapter.out;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.entity.SamityEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.repository.ICancelSamityRepository;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.out.persistence.ICancelSamityPersistencePort;
import net.celloscope.mraims.loanportfolio.features.cancel.domain.CancelSamity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CancelSamityAdapter implements ICancelSamityPersistencePort {
    private final ICancelSamityRepository repository;
    private final ModelMapper modelMapper;


    public CancelSamityAdapter(ICancelSamityRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<CancelSamity> saveSamityEventTracker(CancelSamity cancelSamity) {
        log.info("cancel samity to be saved to db : {}", cancelSamity);
        return repository
                .save(modelMapper.map(cancelSamity, SamityEventTrackerEntity.class))
                .map(samityEventTrackerEntity -> modelMapper.map(samityEventTrackerEntity, CancelSamity.class));
    }
}
