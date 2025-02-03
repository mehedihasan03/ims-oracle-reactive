package net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetaPropertyHandler {
    private final MetaPropertyUseCase metaPropertyUseCase;

    public Mono<ServerResponse> getMetaPropertyByPropertyId(ServerRequest serverRequest) {

        String id = serverRequest.pathVariable("id");
        return metaPropertyUseCase.getMetaPropertyByPropertyId(id)
                .flatMap(metaPropertyResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(metaPropertyResponseDTO)
                );
    }
}
