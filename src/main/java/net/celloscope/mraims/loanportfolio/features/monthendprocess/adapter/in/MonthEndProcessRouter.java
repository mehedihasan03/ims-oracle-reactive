package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MonthEndProcessRouter {

    private final MonthEndProcessHandler handler;

    @Bean
    public RouterFunction<ServerResponse> monthEndProcessRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(MONTH_END_PROCESS), this::monthEndProcessRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> monthEndProcessRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GRID_VIEW), handler::gridViewOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(STATUS), handler::gridViewOfStatusOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(START), handler::runSamityStatusOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(RETRY), handler::retrySamityStatusOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(RETRY_ALL), handler::retryAllSamityStatusOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(ACCOUNTING).concat(GRID_VIEW), handler::gridViewOfAccountingOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(ACCOUNTING).concat(START), handler::runAccountingOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(ACCOUNTING).concat(RETRY), handler::retryAccountingByTransactionCodeListOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OFFICE.concat(ACCOUNTING).concat(RETRY_ALL), handler::retryAllAccountingOfMonthEndProcess))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OFFICE.concat(MONTH_END_PROCESS_REVERT), handler::revertMonthEndProcess))
                .build();
    }
}
