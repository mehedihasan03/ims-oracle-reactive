package net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in;

import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.smsnotification.domain.SmsLog;
import reactor.core.publisher.Mono;

public interface ISmsNotificationUseCase {
    Mono<SmsLog> saveSmsLog(SmsNotificationRequestDTO requestDTO);
    Mono<Boolean> publishSmsRequest(SmsNotificationRequestDTO requestDTO);
}
