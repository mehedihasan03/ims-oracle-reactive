package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.in.route;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.in.handler.WithdrawStagingDataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_LOGINID;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_MFIID;

@Component
@RequiredArgsConstructor
public class WithdrawStagingDataRouteConfig {

    private final WithdrawStagingDataHandler handler;

    @Bean
    public RouterFunction<ServerResponse> withdrawRouterConfig() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(STAGING_WITHDRAW_DATA_BASE_URL), this::withdrawRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> withdrawRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(BY_SAMITY.concat(PAYMENT), handler::withdrawPayment)
                                .PUT(BY_SAMITY.concat(PAYMENT), handler::updateWithdrawPayment)
                                .POST(BY_SAMITY.concat(SUBMIT), handler::submitWithdrawPayment)
                                .GET(BY_OFFICE.concat(GRID_VIEW), handler::gridViewOfWithdrawStagingDataByOffice)
                                .GET(GRID_VIEW.concat(WITHDRAW_COLLECTION), handler::gridViewOfWithdrawCollectionData)
                                .GET(DETAIL_VIEW.concat(WITHDRAW_COLLECTION), handler::DetailViewOfWithdrawCollectionData)
                                .GET(BY_FIELD_OFFICER.concat(GRID_VIEW), handler::gridViewOfWithdrawStagingDataByFieldOfficer)
                                .DELETE(BY_ENTITY.concat(DELETE), handler::deleteWithdrawData)
                )

                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(BY_ENTITY.concat(SUBMIT), handler::submitWithdrawData))
                .build();
    }

    private ServerRequest headersSanityCheck(ServerRequest request) {
        CommonFunctions.validateHeaders(request, HeaderNames.LOGIN_ID.getValue(), EMPTY_LOGINID.getValue());
        CommonFunctions.validateHeaders(request, HeaderNames.MFI_ID.getValue(), EMPTY_MFIID.getValue());
        return request;
    }

    @Bean
    public RouterFunction<ServerResponse> withdrawStagingDataRouterConfigV1() {
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL, this::withdrawStagingDataRoutesV1)
                .build();
    }

    private RouterFunction<ServerResponse> withdrawStagingDataRoutesV1() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GRID_VIEW_WITHDRAW_STAGING_DATA_AUTHORIZATION, handler::gridViewOfWithdrawStagingDataForAuthorizationByOffice)
                        .GET(GRID_VIEW_WITHDRAW_STAGING_DATA_BY_OFFICE, handler::gridViewWithdrawStagingDataByOfficeV1)
                        .GET(GRID_VIEW_WITHDRAW_STAGING_DATA, handler::gridViewWithdrawStagingDataByFieldOfficerV1)
                        .GET(DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_SAMITY_ID, handler::detailViewOfWithdrawStagingDataBySamityId)
                        .GET(DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_MEMBER_ID, handler::detailViewOfWithdrawStagingDataByMemberId)
                        .GET(DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_ACCOUNT_ID, handler::detailViewOfWithdrawStagingDataBySavingsAccountId)
                )
                .build();
    }
}
