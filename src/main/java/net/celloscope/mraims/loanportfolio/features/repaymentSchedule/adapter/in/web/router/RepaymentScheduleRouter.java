package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
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
public class RepaymentScheduleRouter {
    @Bean
    public RouterFunction<ServerResponse> RepaymentScheduleRoutes(
            RepaymentScheduleHandler repaymentScheduleHandler
    ) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GET_REPAYMENT_SCHEDULE_DECLINING_BALANCE_EQUAL_INSTALLMENT, repaymentScheduleHandler::getRepaymentSchedule)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.VIEW_REPAYMENT_SCHEDULE_DECLINING_BALANCE_EQUAL_INSTALLMENT, repaymentScheduleHandler::viewRepaymentSchedule)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.VIEW_REPAYMENT_SCHEDULE_FLAT, repaymentScheduleHandler::viewRepaymentScheduleFlat)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.VIEW_REPAYMENT_SCHEDULE_FLAT_MIGRATION, repaymentScheduleHandler::viewRepaymentScheduleFlatMigration)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .GET(RouteNames.GET_REPAYMENT_SCHEDULE_DECLINING_BALANCE, repaymentScheduleHandler::getRepaymentScheduleWithFlatPrincipal)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .GET(RouteNames.GET_REPAYMENT_INFO, repaymentScheduleHandler::getRepaymentInfo)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .GET(RouteNames.GET_REPAYMENT_SCHEDULE_BY_LOAN_ACCOUNT_ID, repaymentScheduleHandler::getRepaymentScheduleByLoanAccountId)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .GET(RouteNames.GENERATE_DPS_REPAYMENT_SCHEDULE, repaymentScheduleHandler::generateRepaymentScheduleForDps)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .GET(RouteNames.GENERATE_REPAYMENT_SCHEDULE_FLAT_INSTALLMENT_INFO, repaymentScheduleHandler::getRepaymentScheduleWithInstallmentInfoProvided)
                                ).nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .POST(RouteNames.RESCHEDULE_REPAY_SCHEDULE_ON_SAMITY_CANCEL, repaymentScheduleHandler::rescheduleLoanRepayScheduleOnSamityCancel)
                                ))
                .build();
    }
}
