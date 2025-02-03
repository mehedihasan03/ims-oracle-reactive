package net.celloscope.mraims.loanportfolio.features.migrationV3.components.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationMemberRepositoryV3 extends R2dbcRepository<Member, String>{
    Mono<Member> findFirstByMemberIdOrderByMemberIdDesc(String memberId);

}
