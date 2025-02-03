package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.in.web.router;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.in.web.handler.SavingsAccountHandler;
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
public class SavingsAccountRouter {

    @Bean
    public RouterFunction<ServerResponse> savingsAccountRoutes(SavingsAccountHandler savingsAccountHandler) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL_V2,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON),
                                        nestedBuilder -> nestedBuilder.POST(
                                                RouteNames.ACTIVATE_SAVINGS_ACCOUNT,
                                                savingsAccountHandler::activateSavingsAccount
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
