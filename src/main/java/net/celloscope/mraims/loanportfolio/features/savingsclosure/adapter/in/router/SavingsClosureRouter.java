package net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.in.router;

import net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.in.handler.SavingsClosureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.MRA_API_BASE_URL_V2;
import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
public class SavingsClosureRouter {

    private final SavingsClosureHandler handler;

    public SavingsClosureRouter(SavingsClosureHandler handler) {
        this.handler = handler;
    }

    @Bean
    public RouterFunction<ServerResponse> savingsClosureRouterConfig() {
        return RouterFunctions
                .route()
                .path(MRA_API_BASE_URL_V2, this::savingsClosureRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> savingsClosureRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(SAVINGS_ACCOUNT.concat(CREATE_SAVINGS_CLOSURE), handler::closeSavingsAccount)
                        .POST(SAVINGS_ACCOUNT.concat(REJECT_SAVINGS_CLOSURE), handler::rejectSavingsAccountClosure)
                        .POST(SAVINGS_ACCOUNT.concat(AUTHORIZE_SAVINGS_CLOSURE), handler::authorizeSavingsAccountClosure)
                )
                .build();
    }
}
