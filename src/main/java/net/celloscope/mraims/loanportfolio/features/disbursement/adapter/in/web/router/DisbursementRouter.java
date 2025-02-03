package net.celloscope.mraims.loanportfolio.features.disbursement.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.disbursement.adapter.in.web.handler.DisbursementHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Component
@RequiredArgsConstructor
public class DisbursementRouter {

    private final DisbursementHandler disbursementHandler;

    @Bean
    public RouterFunction<ServerResponse> disbursementRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL, this::disbursementRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> disbursementRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(DISBURSE, disbursementHandler::disburseLoan)
                        .POST(DISBURSE_MIGRATION, disbursementHandler::disburseLoanMigration))
                .build();
    }
}
