package net.celloscope.mraims.loanportfolio.features.migration.components.mfiprogram;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationMfiProgramRepository extends R2dbcRepository<MfiProgram, String> {
    Mono<MfiProgram> findFirstByMfiProgramIdAndMfiIdAndStatusOrderByCreatedOnDesc(String mfiProgramId, String mfiId, String status);
    Mono<MfiProgram> findFirstByOrderByOidDesc();
}
