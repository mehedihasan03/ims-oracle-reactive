package net.celloscope.mraims.loanportfolio.core.pubsub;

import reactor.core.publisher.Mono;

public interface ISubscriberHandler {
    Mono<Void> handleMessage(String message);
}
