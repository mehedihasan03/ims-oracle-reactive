package net.celloscope.mraims.loanportfolio.core.pubsub;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class ReactiveRedisSubscriber {

    private final ReactiveRedisMessageListenerContainer container;
    private final ChannelTopic topic;
    private final ISubscriberHandler subscriberHandler;

    @Value("${spring.r2dbc.default.schema}")
    private String defaultSchema;

    public ReactiveRedisSubscriber(@Qualifier("customRedisContainer") ReactiveRedisMessageListenerContainer container, @Qualifier("smsChannelTopic") ChannelTopic topic, ISubscriberHandler subscriberHandler) {
        this.container = container;
        this.topic = topic;
        this.subscriberHandler = subscriberHandler;
    }

    public Flux<Void> subscribe() {
        return container.receiveLater(topic)
            .flatMapMany(messageFlux -> messageFlux.map(ReactiveSubscription.Message::getMessage))
            .map(String::new)
            .flatMap(this::handleMessage)
//            .doOnError(e -> log.error("Error receiving message", e))
            .doOnRequest(l -> log.info("Requesting messages"))
            .onErrorResume(throwable -> {
                return Mono.empty();
            });
    }

    private Mono<Void> handleMessage(String msg) {
        log.info("Received message : " + msg);
        return Mono.just(msg)
            .contextWrite(context -> {
                String instituteOid = parseInstituteOidFromMessage(msg);
                if (StringUtils.hasText(instituteOid)) {
                    log.info("instituteOid from msg: " + instituteOid);
                } else {
                    instituteOid = this.defaultSchema;
                }

                return context.put(CurrentTenantIdHolder.INSTITUTE_OID, instituteOid);
            })
            .flatMap(subscriberHandler::handleMessage);
    }

    private String parseInstituteOidFromMessage(String msg) {
        JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
        return jsonObject.get("instituteOid").getAsString();
//        return Mono.just(jsonObject.get("instituteOid").getAsString());
    }

    @PostConstruct
    public void listen() {
        subscribe().subscribe();
    }
}
