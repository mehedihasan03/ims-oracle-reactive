package net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.persistence.database.entity.SmsLogEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ISmsLogRepository extends ReactiveCrudRepository<SmsLogEntity, String> {
}
