package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.in.handler.WelFareFundHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Component
@RequiredArgsConstructor
public class WelFareFundRouteConfig {

    private final WelFareFundHandler handler;

    @Bean
    public RouterFunction<ServerResponse> welFareFundRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(GET_WELFARE_FUND_DATA), this::welfareFundRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> welfareFundRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GET_GRID_VIEW), handler::gridViewWelfareFundByOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_ACCOUNT.concat(DETAIL_VIEW), handler::welfareFundDetailView))
//                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
//                        .GET(BY_ACCOUNT.concat(DETAILS), handler::loanAccountForWelfareByAccountId))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_ACCOUNT.concat(COLLECT), handler::saveCollectedWelfareFund))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(BY_ACCOUNT.concat(UPDATE), handler::updateWelfareFundCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_ACCOUNT.concat(DETAILS), handler::getWelfareFundDetailsForLoanAccount))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_ACCOUNT.concat(AUTHORIZE), handler::authorizeWelfareFundDataByLoanAccountId))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_ACCOUNT.concat(REJECT), handler::rejectWelfareFundDataByLoanAccountId))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(AUTHORIZATION).concat(GRID_VIEW), handler::gridViewOfWelfareFundDataByOfficeForAuthorization))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_ACCOUNT.concat(AUTHORIZATION).concat(DETAIL_VIEW), handler::detailViewOfWelfareFundDataByLoanAccountForAuthorization))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_ACCOUNT.concat(RESET_WELFARE_FUND_COLLECTION), handler::resetWelfareFundData))
                .build();
    }
}
