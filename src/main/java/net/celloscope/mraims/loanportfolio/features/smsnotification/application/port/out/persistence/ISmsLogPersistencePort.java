package net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.persistence;

import net.celloscope.mraims.loanportfolio.features.smsnotification.domain.SmsLog;
import reactor.core.publisher.Mono;

public interface ISmsLogPersistencePort {
    Mono<SmsLog> save(SmsLog data);
}
