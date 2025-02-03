package net.celloscope.mraims.loanportfolio.features.migrationV3.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
@RequiredArgsConstructor
public class DeleteOfficeDataRouterV3 {
    private final DeleteOfficeDataHandlerV3 handler;

    @Bean
    public RouterFunction<ServerResponse> deleteOfficeDataRouterConfigV3() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(MRA_API_BASE_URL_V3.concat(DELETE_ALL_OFFICE_DATA), handler::deleteOfficeData)
                ).build();
    }
}
