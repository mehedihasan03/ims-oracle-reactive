package net.celloscope.mraims.loanportfolio.core.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReactiveRedisPublisher {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ChannelTopic topic;

    public ReactiveRedisPublisher(@Qualifier("customReactiveRedisTemplate") ReactiveRedisTemplate<String, String> reactiveRedisTemplate, @Qualifier("smsChannelTopic") ChannelTopic topic) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.topic = topic;
    }

    public Mono<Long> publish(String message) {
        log.info("On Topic {} Publishing message : {}", topic.getTopic(), message);
        return reactiveRedisTemplate.convertAndSend(topic.getTopic(), message)
//            .log()
            ;
    }

}
