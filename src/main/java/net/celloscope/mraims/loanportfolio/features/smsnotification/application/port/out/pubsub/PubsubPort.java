package net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.out.pubsub;

import reactor.core.publisher.Mono;

public interface PubsubPort {
    Mono<Boolean> publishSmsRequest(String message);
}
