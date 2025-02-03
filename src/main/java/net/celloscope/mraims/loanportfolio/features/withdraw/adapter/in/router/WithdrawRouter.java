package net.celloscope.mraims.loanportfolio.features.withdraw.adapter.in.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.in.handler.AuthorizeWithdrawHandler;
import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.in.handler.StageWithdrawHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class WithdrawRouter {

    private final StageWithdrawHandler handler;

    public WithdrawRouter(StageWithdrawHandler handler) {
        this.handler = handler;
    }


    @Bean
    public RouterFunction<ServerResponse> WithdrawRoutes(StageWithdrawHandler stageWithdrawHandler,
                                                         AuthorizeWithdrawHandler authorizeWithdrawHandler) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.STAGE_WITHDRAW, stageWithdrawHandler::stageWithdraw)
                                )
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.AUTHORIZE_WITHDRAW, authorizeWithdrawHandler::authorizeWithdraw)
                                )
                )
                .build();
    }



    @Bean
    public RouterFunction<ServerResponse> withdrawRouteConfigV2(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(WITHDRAW_STAGING_DATA_BASE_URL), this::withdrawRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> withdrawRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(WITHDRAW.concat(EDIT), handler::updateWithdrawData))
                .build();
    }
}
