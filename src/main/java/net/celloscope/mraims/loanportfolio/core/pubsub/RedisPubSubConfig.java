package net.celloscope.mraims.loanportfolio.core.pubsub;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Value("${redis.pubsub.topic}")
    private String topic;

    @Bean
    public ReactiveRedisMessageListenerContainer customRedisContainer(@Qualifier("reactiveRedisConnectionFactory") ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisMessageListenerContainer(connectionFactory);
    }

    @Bean
    public ChannelTopic smsChannelTopic() {
        return new ChannelTopic(topic);
    }
}
