package net.celloscope.mraims.loanportfolio.features.fdr.adapter.in.router;

import net.celloscope.mraims.loanportfolio.features.fdr.adapter.in.handler.FDRHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
public class FDRRouter {
    private final FDRHandler handler;

    public FDRRouter(FDRHandler handler) {
        this.handler = handler;
    }

    @Bean
    public RouterFunction<ServerResponse> fdrInterestPostingRouterConfig() {
        return RouterFunctions
                .route()
                .path(MRA_API_BASE_URL_V2, this::fdrInterestPostingRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> fdrInterestPostingRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(FDR_INTEREST_POSTING, handler::postFDRInterest)
                        .GET(GET_SCHEDULE, handler::getFDRInterestPostingSchedule)
                        .POST(ACTIVATE_FDR_ACCOUNT, handler::activateFDRAccount)
                        .GET(FDR_GRID_VIEW, handler::getFDRGridViewByOffice)
                        .GET(FDR_DETAIL_VIEW, handler::getFDRDetailViewBySavingsAccountId)
                        .GET(FDR_CLOSURE_GRID_VIEW, handler::getFDRAuthorizationGridViewByOffice)
                        .GET(FDR_CLOSURE_DETAIL_VIEW, handler::getFDRClosureDetailViewBySavingsAccountId)
                        .GET(FDR_CLOSING_INFO, handler::getFDRAccountClosingInfo)
                        .POST(FDR_CLOSURE, handler::closeFDRAccount)
                        .POST(FDR_AUTHORIZATION, handler::authorizeFDRAccountClosure)
                        .POST(FDR_REJECTION, handler::rejectFDRAccountClosure)
                )
                .build();
    }
}
