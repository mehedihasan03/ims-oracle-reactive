package net.celloscope.mraims.loanportfolio.features.offline.adapter.in;

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
public class StagingDataOfflineRouter {

    private final StagingDataOfflineHandler handler;

    @Bean
    public RouterFunction<ServerResponse> stagingDataOfflineRouterConfig(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(STAGING_DATA_BASE_URL).concat(OFFLINE), this::stagingDataOfflineRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> stagingDataOfflineRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(DOWNLOAD), handler::downloadStagingDataByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_FIELD_OFFICER.concat(DELETE), handler::deleteDownloadedStagingDataByFieldOfficer))
                .build();
    }
}
