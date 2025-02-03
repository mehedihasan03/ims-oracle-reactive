package net.celloscope.mraims.loanportfolio.features.migration.components.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationMemberRepository extends R2dbcRepository<Member, String>{
    Mono<Member> findFirstByMemberIdOrderByMemberIdDesc(String memberId);

}
