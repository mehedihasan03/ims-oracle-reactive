package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.authorization.adapter.in.handler.AuthorizationHandler;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.in.hadler.AutoVoucherHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.UNAUTHORIZE;

@Configuration
@RequiredArgsConstructor
public class AutoVoucherRouter {
    private final AutoVoucherHandler handler;

    @Bean
    public RouterFunction<ServerResponse> autoVoucherRouterConfig(){
        return RouterFunctions.route()
                .path("auto-voucher", this::autoVoucherRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> autoVoucherRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET("create", handler::createAndSaveAutoVoucherFromAISRequest)
                        .GET("get", handler::getAutoVoucherListByManagementProcessIdAndProcessId)
                        .DELETE("delete", handler::deleteAutoVoucherListByManagementProcessIdAndProcessId)
                        .GET("get-detail", handler::getAutoVoucherDetailListByVoucherId)
                )
                .build();
    }
}
