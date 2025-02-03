package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.in;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LoanWaiverRouter {

        private final LoanWaiverHandler handler;

        @Bean
        public RouterFunction<ServerResponse> loanWaiverRouterConfig() {
                return RouterFunctions.route()
                                .path(MRA_API_BASE_URL_V2.concat(LOAN_WAIVER), this::loanWaiverRoutes)
                                .build();
        }

        private RouterFunction<ServerResponse> loanWaiverRoutes() {
                return RouterFunctions.route()
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_OFFICE.concat(GET_GRID_VIEW), handler::getLoanWaiverList))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_ID.concat(DETAILS), handler::getLoanWaiverInformationById))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(MEMBER_DETAILS_BY_ID, handler::getMemberInformationForLoanWaiver))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(BY_ACCOUNT.concat(COLLECT), handler::createLoanWaiverByLoanAccountId))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .PUT(BY_ACCOUNT.concat(UPDATE), handler::updateLoanWaiverByLoanAccountId))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(SUBMIT, handler::submitLoanWaiverByLoanAccountId))
                        .build();
        }
}
