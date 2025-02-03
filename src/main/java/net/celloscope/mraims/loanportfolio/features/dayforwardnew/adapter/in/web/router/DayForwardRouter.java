package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.handler.DayForwardHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Slf4j
public class DayForwardRouter {
    @Bean
    public RouterFunction<ServerResponse> ForwardDayRoutes(DayForwardHandler dayForwardHandler) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL_V2,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.POST(
                                                RouteNames.DAY_FORWARD_ROUTINE,
                                                dayForwardHandler::dayForwardRoutineForOffice
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.GET(
                                                RouteNames.DAY_FORWARD_GRID_VIEW,
                                                dayForwardHandler::gridViewOfForwardDayRoutineForOffice
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.PUT(
                                                RouteNames.DAY_FORWARD_CONFIRM,
                                                dayForwardHandler::confirmDayForwardRoutine
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.PUT(
                                                RouteNames.RETRY_DAY_FORWARD,
                                                dayForwardHandler::retryDayForwardRoutineForOffice
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.DELETE(
                                                RouteNames.RESET_DAY_FORWARD_PROCESS,
                                                dayForwardHandler::resetDayForwardProcessForOffice
                                        ))
                )
                .build();
    }
}
