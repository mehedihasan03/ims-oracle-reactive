package net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class PassbookRouter {
    @Bean
    public RouterFunction<ServerResponse> PassbookRoutes(
            PassbookHandler passbookHandler

    ) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.CREATE_PASSBOOK_ENTRY_LOAN, passbookHandler::createPassbookEntryForLoan)
                                        .POST(RouteNames.CREATE_PASSBOOK_ENTRY_SAVINGS, passbookHandler:: createPassbookEntryforSavings)
                                        .POST(RouteNames.CREATE_PASSBOOK_ENTRY_SAVINGS_WITHDRAW, passbookHandler::createPassbookEntryForSavingsWithdraw)
                                        .POST(RouteNames.CREATE_PASSBOOK_ENTRY_ACCRUED_INTEREST_DEPOSIT, passbookHandler::createPassbookEntryForTotalAccruedInterestDeposit)
                                        .GET(PASSBOOK.concat(GRID_VIEW), passbookHandler::passbookGridView)
                                        .GET(PASSBOOK.concat(REPORT_BY_SAMITY), passbookHandler::passbookReportViewV1))
                )
                .path(RouteNames.MRA_API_BASE_URL_V2,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(PASSBOOK.concat(REPORT_BY_SAMITY), passbookHandler::passbookReportViewV2)
                                        .GET(PASSBOOK.concat("/test"), passbookHandler::getPassbookEntriesForPrincipalAndServiceChargeOutstanding))
                )
                .build();
    }
}
