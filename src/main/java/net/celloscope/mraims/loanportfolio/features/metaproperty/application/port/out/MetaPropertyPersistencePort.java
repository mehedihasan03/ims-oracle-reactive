package net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.out;

import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity.MetaPropertyEntity;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

public interface MetaPropertyPersistencePort {

    Mono<MetaProperty> getEqualInstallmentParams();
    Mono<MetaPropertyEntity> getMetaPropertyByDescriptionAndStatus(String description, String status);
    Mono<MetaPropertyEntity> getMetaPropertyByPropertyIdAndStatus(String description, String status);


}
