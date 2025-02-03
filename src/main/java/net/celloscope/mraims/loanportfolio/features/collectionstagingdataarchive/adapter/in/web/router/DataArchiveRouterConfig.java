package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.in.web.handler.DataArchiveHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class DataArchiveRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> DataArchiveRoutes(DataArchiveHandler handler) {
        return route()
                .path(RouteNames.COLLECTION_STAGING_DATA_ARCHIVE_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.ARCHIVE_COLLECTION, handler::archiveData)
                                )
                )
                .build();
    }
}
