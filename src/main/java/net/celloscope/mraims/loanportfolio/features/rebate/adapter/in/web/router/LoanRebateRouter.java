package net.celloscope.mraims.loanportfolio.features.rebate.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.in.web.handler.LoanRebateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_LOGINID;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_MFIID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class LoanRebateRouter {
    @Bean
    public RouterFunction<ServerResponse> LoanRebateRoutes(LoanRebateHandler loanRebateHandler) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL_V2,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.POST(
                                                RouteNames.RESET_LOAN_REBATE,
                                                loanRebateHandler::resetLoanRebate
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.GET(
                                                RouteNames.LOAN_REBATE,
                                                loanRebateHandler::getLoanRebate
                                        ))
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.GET(
                                                RouteNames.GET_LOAN_AND_SAVINGS_ACCOUNT_DETAILS_BY_MEMBER,
                                                loanRebateHandler::getAccountDetailsForRebate
                                        )).before(this::headersSanityCheck)
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.POST(
                                                RouteNames.SETTLE_REBATED_AMOUNT,
                                                loanRebateHandler::settleRebate
                                        )).before(this::headersSanityCheck)
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.GET(
                                                RouteNames.LOAN_REBATE_GRID_VIEW_BY_OFFICE,
                                                loanRebateHandler::loanRebateGridViewByOffice
                                        )).before(this::headersSanityCheck)
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.POST(
                                                RouteNames.SUBMIT_LOAN_REBATE,
                                                loanRebateHandler::submitLoanRebate
                                        )).before(this::headersSanityCheck)
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.GET(
                                                RouteNames.LOAN_REBATE_DETAIL_VIEW_BY_ID,
                                                loanRebateHandler::getLoanRebateDetail
                                        )).before(this::headersSanityCheck)
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.PUT(
                                                RouteNames.UPDATE_LOAN_REBATE,
                                                loanRebateHandler::updateLoanRebate
                                        )).before(this::headersSanityCheck)
                )
                .build();
    }

    private ServerRequest headersSanityCheck(ServerRequest request) {
        CommonFunctions.validateHeaders(request, HeaderNames.LOGIN_ID.getValue(), EMPTY_LOGINID.getValue());
        CommonFunctions.validateHeaders(request, HeaderNames.MFI_ID.getValue(), EMPTY_MFIID.getValue());
        return request;
    }
}
