package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.in.web.handler.StagingDataHandler;
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
public class StagingDataRouter {

    private final StagingDataHandler handler;

    @Bean
    public RouterFunction<ServerResponse> stagingDataRouterConfigV2(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(STAGING_DATA_BASE_URL), this::stagingDataRoutesV2)
                .build();
    }

    private RouterFunction<ServerResponse> stagingDataRoutesV2() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GET_STAGING_DATA_STATUS), handler::gridViewOfStagingDataStatusByOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GET_STAGING_DATA_STATUS).concat(FILTER).concat(BY_FIELD_OFFICER), handler::gridViewOfStagingDataStatusByOfficeFilteredByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(GET_STAGING_DATA_STATUS), handler::gridViewOfStagingDataStatusByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(POST_GENERATE_STAGING_DATA), handler::generateStagingDataByOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_SAMITY.concat(POST_EXCEPTION_STAGING_DATA_BY_SAMITY), handler::invalidateStagingDataBySamityList))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_SAMITY.concat(POST_REGENERATE_STAGING_DATA_BY_SAMITY), handler::regenerateStagingDataBySamityList))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(DELETE), handler::deleteStagingDataByOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(DOWNLOAD), handler::downloadStagingDataByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_FIELD_OFFICER.concat(DELETE), handler::deleteStagingDataByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(SAMITY_LIST), handler::stagedSamityListByFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(RESET_STAGING_PROCESS), handler::resetStagingProcessTrackerEntriesByOfficeId))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> stagingDataRouterConfig() {
        return RouterFunctions.route()

//				@BaseURL: /mra-ims/mfi/process/api/v1/staging-data
//				@access: private, auth required
//				@desc: handle staging data and staging account data
                .path(MRA_API_BASE_URL.concat(STAGING_DATA_BASE_URL), this::stagingDataRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> stagingDataRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(POST_GENERATE_STAGING_DATA, handler::generateStagingDataAndStagingAccountData))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_STAGING_DATA_STATUS, handler::getStagingDataGenerationStatusResponse))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_STAGING_DATA_DETAIL_VIEW_BY_SAMITY_ID, handler::getStagingDataDetailViewResponse))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_STAGING_DATA_DETAIL_VIEW_BY_ACCOUNT_ID, handler::getStagingDataDetailViewResponseByAccountId))
                .build();
    }
}
