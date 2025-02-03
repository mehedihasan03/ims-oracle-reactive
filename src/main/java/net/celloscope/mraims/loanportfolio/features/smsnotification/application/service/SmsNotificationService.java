package net.celloscope.mraims.loanportfolio.features.smsnotification.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.pubsub.ISubscriberHandler;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.persistence.ISmsLogPersistencePort;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.pubsub.PubsubPort;
import net.celloscope.mraims.loanportfolio.features.smsnotification.domain.SmsLog;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;
import org.testng.util.Strings;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SmsNotificationService implements ISmsNotificationUseCase, ISubscriberHandler {

    private final ISmsLogPersistencePort smsLogPersistencePort;
    private final PubsubPort pubsubPort;
    private final Gson gson;

    public SmsNotificationService(ISmsLogPersistencePort smsLogPersistencePort, PubsubPort pubsubPort) {
        this.smsLogPersistencePort = smsLogPersistencePort;
        this.pubsubPort = pubsubPort;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<SmsLog> saveSmsLog(SmsNotificationRequestDTO requestDTO) {
        return Mono.just(requestDTO)
                .filter(request -> Strings.isNotNullAndNotEmpty(request.getMobileNumber()))
                .mapNotNull(this::buildSmsLog)
                .flatMap(smsLogEntity ->
                        smsLogPersistencePort.save(smsLogEntity)
                                .doOnSuccess(smsLog -> log.info("SMS Log saved: " + smsLog)));
    }

    @Override
    public Mono<Boolean> publishSmsRequest(SmsNotificationRequestDTO requestDTO) {
        return pubsubPort.publishSmsRequest(CommonFunctions.buildGsonBuilder(requestDTO));
    }

    private SmsLog buildSmsLog(SmsNotificationRequestDTO requestDTO) {
        Map<String, String> values = new HashMap<>();
        values.put("type", requestDTO.getType());
        values.put("id", requestDTO.getId());
        values.put("amount", requestDTO.getAmount());
        values.put("datetime", requestDTO.getDatetime());
        values.put("mfiId", requestDTO.getMfiId());
        values.put("accountId", requestDTO.getAccountId());
        values.put("memberId", requestDTO.getMemberId());

        StringSubstitutor sub = new StringSubstitutor(values);
        String sms = sub.replace(requestDTO.getTemplate());

        return SmsLog.builder()
                .mobileNo(requestDTO.getMobileNumber())
                .sms(sms)
                .queuedBy(requestDTO.getLoginId())
                .build();
    }

    @Override
    public Mono<Void> handleMessage(String message) {
        /*
         * 1. convert string to SMSRequestDTO OBJECT
         * 2. convert sms request dto to smslog
         * 3. ceeate sms body from template
         * 4. save sms to database */
        SmsNotificationRequestDTO requestDTO = gson.fromJson(message, SmsNotificationRequestDTO.class);
        return saveSmsLog(requestDTO).flatMap(smsLog -> Mono.empty());
    }

}
