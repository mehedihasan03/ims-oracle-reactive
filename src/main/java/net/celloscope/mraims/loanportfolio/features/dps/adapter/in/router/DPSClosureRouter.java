package net.celloscope.mraims.loanportfolio.features.dps.adapter.in.router;

import net.celloscope.mraims.loanportfolio.features.dps.adapter.in.handler.DPSClosureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
public class DPSClosureRouter {
    private final DPSClosureHandler handler;

    public DPSClosureRouter(DPSClosureHandler handler) {
        this.handler = handler;
    }

    @Bean
    public RouterFunction<ServerResponse> dpsClosureRouteConfig() {
        return RouterFunctions
                .route()
                .path(MRA_API_BASE_URL_V2, this::dpsClosureRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> dpsClosureRoutes() {
        return RouterFunctions
                .route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(DPS_GRID_VIEW, handler::getDPSGridViewByOffice)
                        .GET(DPS_CLOSURE_GRID_VIEW, handler::getDPSAuthorizationGridViewByOffice)
                        .GET(DPS_DETAIL_VIEW, handler::getDPSDetailViewBySavingsAccountId)
                        .GET(DPS_CLOSURE_DETAIL_VIEW, handler::getDPSClosureDetailViewBySavingsAccountId)
                        .GET(DPS_CLOSING_INFO, handler::getDPSAccountClosingInfo)
                        .POST(DPS_CLOSURE, handler::closeDPSAccount)
                        .POST(DPS_AUTHORIZATION, handler::authorizeDPSAccountClosure)
                        .POST(DPS_REJECTION, handler::rejectDPSAccountClosure)
                )
                .build();
    }
}
