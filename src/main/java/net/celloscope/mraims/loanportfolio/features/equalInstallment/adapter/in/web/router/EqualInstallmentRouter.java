package net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler.EqualInstallmentHandler;
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
public class EqualInstallmentRouter {
    private final EqualInstallmentHandler equalInstallmentHandler;

    public EqualInstallmentRouter(EqualInstallmentHandler equalInstallmentHandler) {
        this.equalInstallmentHandler = equalInstallmentHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> EqualInstallmentRoutes() {
        return route()
                .path(RouteNames.MRA_API_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GET_EQUAL_INSTALLMENT, equalInstallmentHandler::getEqualInstallmentAmount)))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> equalInstallmentRouteConfigV2(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2, this::equalInstallmentRoutesV2)
                .build();
    }

    private RouterFunction<ServerResponse> equalInstallmentRoutesV2() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_EQUAL_INSTALLMENT, equalInstallmentHandler::getEqualInstallmentAmountAccordingToInterestCalcMethod))
                .build();
    }
}
