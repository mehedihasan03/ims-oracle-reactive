package net.celloscope.mraims.loanportfolio.features.smsnotification.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.persistence.database.entity.SmsLogEntity;
import net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.persistence.database.repository.ISmsLogRepository;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.persistence.ISmsLogPersistencePort;
import net.celloscope.mraims.loanportfolio.features.smsnotification.domain.SmsLog;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsLogAdapter implements ISmsLogPersistencePort {

    private final ISmsLogRepository repository;
    private final ModelMapper mapper;

    @Override
    public Mono<SmsLog> save(SmsLog data) {
        SmsLogEntity entity = mapper.map(data, SmsLogEntity.class);
        return repository.save(entity)
                .map(smsLogEntity -> mapper.map(smsLogEntity, SmsLog.class));
    }
}
