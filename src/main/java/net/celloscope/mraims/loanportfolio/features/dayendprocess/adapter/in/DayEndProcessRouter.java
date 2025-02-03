package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.in;

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
public class DayEndProcessRouter {

    private final DayEndProcessHandler handler;

    @Bean
    public RouterFunction<ServerResponse> dayEndProcessRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(DAY_END_PROCESS), this::dayEndProcessRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> dayEndProcessRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GRID_VIEW), handler::gridViewOfDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(DETAIL_VIEW), handler::detailViewOfDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(START), handler::runDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(RETRY), handler::retryDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(RETRY_ALL), handler::retryAllDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(AUTO_VOUCHER).concat(GENERATE), handler::generateAutoVoucherForDayEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(AUTO_VOUCHER).concat(RETRY), handler::retryAutoVoucherGenerationForDayEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(AUTO_VOUCHER).concat(DELETE), handler::deleteAutoVoucherGenerationForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(DELETE), handler::deleteDayEndProcessForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(DAY_END_PROCESS_REVERT_AIS), handler::revertDayEndProcessForAIS))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(DAY_END_PROCESS_REVERT_MIS), handler::revertDayEndProcessForMIS))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(STATUS), handler::getStatusOfDayEndProcessForOffice))
                .build();
    }
}
