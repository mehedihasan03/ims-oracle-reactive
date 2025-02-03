package net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.handler.AccrueInterestHandler;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.in.web.handler.TransactionAdjustmentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class TransactionAdjustmentRouter {

    private final TransactionAdjustmentHandler transactionAdjustmentHandler;

    @Bean
    public RouterFunction<ServerResponse> transactionAdjustmentRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2, this::transactionAdjustmentRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> transactionAdjustmentRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(TRANSACTION_ADJUSTMENT, transactionAdjustmentHandler::adjustTransaction)
                )
                .build();
    }
}
