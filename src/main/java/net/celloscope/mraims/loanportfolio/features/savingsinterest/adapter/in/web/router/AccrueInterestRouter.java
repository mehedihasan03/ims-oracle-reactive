package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.handler.AccrueInterestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
@RequiredArgsConstructor
public class AccrueInterestRouter {

    private final AccrueInterestHandler accrueInterestHandler;

    @Bean
    public RouterFunction<ServerResponse> accrueInterestRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2, this::accrueInterestRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> accrueInterestRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(SAVINGS_ACCRUED_INTERESTS_BY_MANAGEMENT_PROCESS_ID_AND_OFFICE_ID, accrueInterestHandler::getAccruedInterestEntities)
                )
                .build();
    }
}
