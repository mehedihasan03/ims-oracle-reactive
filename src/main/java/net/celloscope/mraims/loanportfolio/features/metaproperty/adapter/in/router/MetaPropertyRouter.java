package net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.in.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.in.handler.MetaPropertyHandler;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.PASSBOOK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class MetaPropertyRouter {
    @Bean
    public RouterFunction<ServerResponse> metaPropertyRoutes(
            MetaPropertyHandler metaPropertyHandler

    ) {
        return route()
                .path(MRA_API_BASE_URL_V2,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(META_PROPERTY.concat("/{id}"), metaPropertyHandler::getMetaPropertyByPropertyId))
                )
                .build();
    }
}
