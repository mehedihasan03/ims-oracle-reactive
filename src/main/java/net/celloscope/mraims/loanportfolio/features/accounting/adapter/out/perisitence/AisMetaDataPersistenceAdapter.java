package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.helper.FieldName;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.repository.AisMetaDataRepository;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisMetaDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

@Component
@Slf4j
public class AisMetaDataPersistenceAdapter implements AisMetaDataPersistencePort {
    private final AisMetaDataRepository repository;
    private final ModelMapper mapper;
    private final Gson gson;

    public AisMetaDataPersistenceAdapter(AisMetaDataRepository repository, ModelMapper mapper, Gson gson) {
        this.repository = repository;
        this.mapper = mapper;
        this.gson = gson;
    }

    @Override
    public Flux<AisMetaData> getAisMetaDataByProcessName(String processName) {
        return repository
                .findAisMetaDataEntityByProcessName(processName)
                .doOnRequest(l -> log.info("requesting to fetch aisMetaData by process name : {}", processName))
                .map(aisMetaDataEntity -> {
                    AisMetaData aisMetaData = mapper.map(aisMetaDataEntity, AisMetaData.class);
                    aisMetaData.setFieldNames(gson.fromJson(aisMetaDataEntity.getFieldName(), ArrayList.class));
                    return aisMetaData;
                })
                .doOnNext(aisMetaData -> log.info("aisMetaData: {}", aisMetaData))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }


}
