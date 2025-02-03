package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.handler.SavingsInterestHandler;
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
public class SavingsInterestRouter {

    private final SavingsInterestHandler savingsInterestHandler;

    @Bean
    public RouterFunction<ServerResponse> savingsInterestRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL, this::savingsInterestRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> savingsInterestRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(SAVINGS_ACCRUED_INTEREST_BY_DATE, savingsInterestHandler::calculateDailyAccruedInterest)
                        .GET(SAVINGS_ACCRUED_INTEREST_BY_MONTH, savingsInterestHandler::calculateMonthlySavingsAccruedInterest)
                        .GET(CALCULATE_ACCRUED_INTEREST_BY_MONTH, savingsInterestHandler::calculateMonthlyAccruedInterest)
                        .POST(CALCULATE_AND_SAVE_ACCRUED_INTEREST_BY_MONTH, savingsInterestHandler::calculateAndSaveMonthlyAccruedInterest)
                        .GET(GET_ACCRUED_INTEREST_ENTITIES, savingsInterestHandler::getAccruedInterestEntities)
                        .GET(GET_DPS_MATURITY_AMOUNT, savingsInterestHandler::calculateDPSMaturityAmount)
                        .POST(POST_SAVINGS_INTEREST, savingsInterestHandler::postSavingsInterest)
                )
                .build();
    }
}
