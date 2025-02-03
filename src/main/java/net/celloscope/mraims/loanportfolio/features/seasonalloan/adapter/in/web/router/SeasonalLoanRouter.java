package net.celloscope.mraims.loanportfolio.features.seasonalloan.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.adapter.in.web.handler.SeasonalLoanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class SeasonalLoanRouter {
    @Bean
    public RouterFunction<ServerResponse> SeasonalLoanRoutes(
            SeasonalLoanHandler seasonalLoanHandler
    ) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL_V2.concat(RouteNames.SEASONAL_LOAN_BASE_URL),
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.DETAIL_VIEW, seasonalLoanHandler::getSeasonalLoanDetailView)
                                )
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW, seasonalLoanHandler::getSeasonalLoanGridView)
                                )
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.COLLECTION, seasonalLoanHandler::collectSeasonalLoan)
                                )
                )
                .build();
    }
}
