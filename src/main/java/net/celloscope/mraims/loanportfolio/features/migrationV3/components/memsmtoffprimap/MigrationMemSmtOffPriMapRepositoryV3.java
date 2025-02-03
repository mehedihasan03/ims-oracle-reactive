package net.celloscope.mraims.loanportfolio.features.migrationV3.components.memsmtoffprimap;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationMemSmtOffPriMapRepositoryV3 extends R2dbcRepository<MemSmtOffPriMap, String> {
    Mono<MemSmtOffPriMap> findFirstByOrderByMemSmtOffPriMapIdDesc();
    Mono<MemSmtOffPriMap> findFirstByMemberIdAndOfficeIdAndSamityIdAndStatusOrderByMemSmtOffPriMapIdDesc(String memberId, String officeId, String samityId, String status);

}
