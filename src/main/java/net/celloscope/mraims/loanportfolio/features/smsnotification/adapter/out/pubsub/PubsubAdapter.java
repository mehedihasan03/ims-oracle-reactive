package net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.pubsub.ReactiveRedisPublisher;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.pubsub.PubsubPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class PubsubAdapter implements PubsubPort {

    private final ReactiveRedisPublisher reactiveRedisPublisher;
    @Override
    public Mono<Boolean> publishSmsRequest(String message) {
        return reactiveRedisPublisher.publish(message)
                .map(result -> result > 0)
                .doOnSuccess(success -> log.info("Message published successfully: {}", success));
    }
}
