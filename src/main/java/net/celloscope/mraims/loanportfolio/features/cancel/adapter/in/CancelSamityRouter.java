package net.celloscope.mraims.loanportfolio.features.cancel.adapter.in;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
public class CancelSamityRouter {
    private final CancelSamityHandler cancelSamityHandler;
    public CancelSamityRouter(CancelSamityHandler cancelHandler) {
        this.cancelSamityHandler = cancelHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> cancelSamityRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL, this::cancelSamityRoutes)
                .build();
    }
    private RouterFunction<ServerResponse> cancelSamityRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(CANCEL_SAMITY, cancelSamityHandler::cancelSamity))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(RESTORE_CANCELLED_SAMITY, cancelSamityHandler::restoreCancelledSamity))
                .build();
    }
}
