package net.celloscope.mraims.loanportfolio.features.loancalculator.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.loancalculator.adapter.in.web.handler.LoanCalculatorHandler;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.RepaymentScheduleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class LoanCalculatorRouter {
    @Bean
    public RouterFunction<ServerResponse> LoanCalculatorRoutes(
            LoanCalculatorHandler loanCalculatorHandler
    ) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL_V2.concat(RouteNames.LOAN_CALCULATOR_BASE_URL),
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.LOAN_CALCULATOR_LOAN_PRODUCTS, loanCalculatorHandler::getLoanProductsByMfi)
                                        .GET(RouteNames.LOAN_CALCULATOR_LOAN_PRODUCT_INFO, loanCalculatorHandler::getLoanProductInfoByLoanProductId)
                                        .GET(RouteNames.LOAN_CALCULATOR_REPAYMENT_SCHEDULE.concat(RouteNames.GENERATE), loanCalculatorHandler::getLoanRepaySchedule)
                                ))
                .build();
    }
}
