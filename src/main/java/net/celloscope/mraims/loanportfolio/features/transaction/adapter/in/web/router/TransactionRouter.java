package net.celloscope.mraims.loanportfolio.features.transaction.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.in.web.handler.TransactionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
@RequiredArgsConstructor
public class TransactionRouter {
    private final TransactionHandler transactionHandler;

    @Bean
    public RouterFunction<ServerResponse> transactionRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL.concat(TRANSACTION), this::transactionRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> transactionRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(CREATE_TRANSACTION, transactionHandler::createTransaction)
                        .POST(CREATE_TRANSACTION_FOR_ACCRUED_INTEREST, transactionHandler::createTransactionForAccruedInterest)
                        /*.POST(CREATE_TRANSACTION_FOR_HALF_YEARLY_ACCRUED_INTEREST, transactionHandler::createTransactionForHalfYearlyInterestPosting)*/
                        .GET(GRID_VIEW, transactionHandler::transactionGridView)
                        .GET(REPORT_BY_SAMITY, transactionHandler::transactionReportView))
                .build();
    }

}
