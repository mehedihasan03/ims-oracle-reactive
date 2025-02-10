package net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.entity.FeeTypeSettingEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface FeeTypeSettingRepository extends ReactiveCrudRepository<FeeTypeSettingEntity, String> {

    @Query("""
            SELECT l.ledger_id, s.subledger_id FROM template.fee_type_setting fts
            left join template.ledger l
            on l.parent_ledger_id = fts.ledger_id
            and l.office_id = :officeId
            left join template.subledger s
            on s.parent_subledger_id = fts.subledger_id
            and s.office_id = :officeId
            where fts.fee_type_setting_id = :feeSettingId;
            """)
    Mono<FeeTypeSettingEntity> findByFeeTypeSettingId(String feeSettingId, String officeId);
    Mono<FeeTypeSettingEntity> findByFeeTypeSettingId(String feeSettingId);
}
