package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.EmployeeEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends R2dbcRepository<EmployeeEntity, String> {
    Mono<EmployeeEntity> findByEmployeeId(String employeeId);
}
