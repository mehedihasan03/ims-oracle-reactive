package net.celloscope.mraims.loanportfolio.features.authorization.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.authorization.adapter.in.handler.AuthorizationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
@RequiredArgsConstructor
public class AuthorizationRouter {
    private final AuthorizationHandler handler;

    @Bean
    public RouterFunction<ServerResponse> authorizationRouterConfig(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(AUTHORIZATION).concat(BY_OFFICE), this::authorizationRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> authorizationRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_GRID_VIEW, handler::gridViewOfAuthorization)
                        .GET(GET_TAB_VIEW, handler::tabViewOfAuthorization)
                        .POST(LOCK, handler::lockSamityListForAuthorization)
                        .POST(UNLOCK, handler::unlockSamityListForAuthorization)
                        .POST(AUTHORIZE, handler::authorizeSamityList)
                        .POST(REJECT, handler::rejectSamityList)
                        .POST(UNAUTHORIZE, handler::unauthorizeSamityList)
                )
                .build();
    }
}
