package net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity.MetaPropertyEntity;
import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.repository.MetaPropertyRepository;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.out.MetaPropertyPersistencePort;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyParam;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Status.STATUS_ACTIVE;
import static net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum.EQUAL_INSTALLMENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetaPropertyAdapter implements MetaPropertyPersistencePort {
    private final MetaPropertyRepository repository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<MetaProperty> getEqualInstallmentParams() {
        return repository.findMetaPropertyByDescriptionAndStatus(EQUAL_INSTALLMENT.getValue(), STATUS_ACTIVE.getValue())
                .mapNotNull(metaPropertyEntity -> {
                    MetaProperty metaProperty = modelMapper.map(metaPropertyEntity, MetaProperty.class);
                    try {
                        metaProperty.setParams(objectMapper.readValue(
                                metaPropertyEntity.getParameters(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, MetaPropertyParam.class)
                        ));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return metaProperty;
                });
    }

    @Override
    public Mono<MetaPropertyEntity> getMetaPropertyByDescriptionAndStatus(String description, String status) {
        return repository
                .findMetaPropertyByDescriptionAndStatus(description, status)
                .doOnRequest(l -> log.info("Request Received to fetch Meta Property entity by description : {}", description))
                .doOnNext(metaPropertyEntity -> log.info("Meta Property entity fetched by description : {}", metaPropertyEntity));
    }

    @Override
    public Mono<MetaPropertyEntity> getMetaPropertyByPropertyIdAndStatus(String propertyId, String status) {
        return repository
                .findMetaPropertyEntityByPropertyIdAndStatus(propertyId, status)
                .doOnRequest(l -> log.info("Request Received to fetch Meta Property entity by propertyId : {}", propertyId))
                .doOnNext(metaPropertyEntity -> log.info("Meta Property entity fetched by propertyId : {}", metaPropertyEntity));
    }

}
