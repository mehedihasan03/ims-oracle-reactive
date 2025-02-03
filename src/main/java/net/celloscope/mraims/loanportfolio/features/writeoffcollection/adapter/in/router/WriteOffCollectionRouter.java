package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.handler.WriteOffCollectionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.*;

@Configuration
@RequiredArgsConstructor
public class WriteOffCollectionRouter {

    private final WriteOffCollectionHandler handler;

    @Bean
    public RouterFunction<ServerResponse> writeOffCollectionRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(WRITE_OFF_COLLECTION), this::writeOffCollectionRoute)
                .build();
    }

    private RouterFunction<ServerResponse> writeOffCollectionRoute() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(LOAN_ACCOUNT_DETAILS_BY_ID, handler::getWriteOffCollectionData))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_ACCOUNT.concat(COLLECT), handler::createWriteOffCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(BY_ACCOUNT.concat(UPDATE), handler::updateWriteOffCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(SUBMIT, handler::submitLoanWriteOffCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GRID_VIEW), handler::getCollectedWriteOffLoanData))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_ID.concat(DETAILS), handler::getDetailWriteOffCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_ENTITY.concat(DELETE), handler::deleteLoanWriteOffData))
                .before(this::headersSanityCheck)
                .build();
    }

    private ServerRequest headersSanityCheck(ServerRequest request) {
        CommonFunctions.validateHeaders(request, HeaderNames.LOGIN_ID.getValue(), EMPTY_LOGINID.getValue());
        CommonFunctions.validateHeaders(request, HeaderNames.MFI_ID.getValue(), EMPTY_MFIID.getValue());
        return request;
    }
}

