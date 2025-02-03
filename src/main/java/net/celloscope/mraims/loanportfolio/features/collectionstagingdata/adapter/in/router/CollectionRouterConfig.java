package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.routes.RouteNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_LOGINID;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_MFIID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CollectionRouterConfig {
    private final StagingCollectionDataHandler handler;
    private final ResetCollectionHandler resetCollectionHandler;

    @Bean
    public RouterFunction<ServerResponse> stagingCollectionRouterConfig(){
        return RouterFunctions.route()
                .path(MRA_API_BASE_URL_V2.concat(STAGING_COLLECTION_DATA_BASE_URL), this::stagingCollectionDataRoutes)
                .build();
    }

    private RouterFunction<ServerResponse> stagingCollectionDataRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GET_GRID_VIEW).concat(REGULAR_SAMITY), handler::gridViewOfRegularCollectionForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(GET_GRID_VIEW).concat(REGULAR_SAMITY), handler::gridViewOfRegularCollectionForFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_OFFICE.concat(GET_GRID_VIEW).concat(SPECIAL_SAMITY), handler::gridViewOfSpecialCollectionForOffice))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GET_LIST.concat(SPECIAL_SAMITY), handler::listOfSpecialCollectionSamity))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(BY_FIELD_OFFICER.concat(GET_GRID_VIEW).concat(SPECIAL_SAMITY), handler::gridViewOfSpecialCollectionForFieldOfficer))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_SAMITY.concat(PAYMENT), handler::collectPaymentBySamity))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(BY_SAMITY.concat(UPDATE), handler::updateCollectionPaymentBySamity))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_SAMITY.concat(SUBMIT), handler::submitCollectionDataForAuthorizationBySamity))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_SAMITY.concat(RESET_COLLECTION), resetCollectionHandler::resetCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(GRID_VIEW.concat(SPECIAL_COLLECTION), handler::getCollectionDataGridView))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(DETAIL_VIEW.concat(SPECIAL_COLLECTION), handler::getCollectionDataDetailView))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .DELETE(BY_OID.concat(RESET_SPECIAL_COLLECTION), resetCollectionHandler::resetSpecialCollection))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(SPECIAL_COLLECTION.concat(EDIT), handler::editSpecialCollectionData))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(BY_OID.concat(SUBMIT), handler::submitCollectionDataForAuthorizationByOid))
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .PUT(BY_ENTITY.concat(SUBMIT), handler::submitCollectionDataEntityByOidList))

                .build();
    }

    private ServerRequest headersSanityCheck(ServerRequest request) {
        CommonFunctions.validateHeaders(request, HeaderNames.LOGIN_ID.getValue(), EMPTY_LOGINID.getValue());
        CommonFunctions.validateHeaders(request, HeaderNames.MFI_ID.getValue(), EMPTY_MFIID.getValue());
        return request;
    }

    @Bean
    public RouterFunction<ServerResponse> CollectionDataRoutes(PaymentCollectionHandler paymentCollectionHandler,
                                                               AuthorizeCollectionHandler authorizeCollectionHandler,
                                                               CollectionStagingDataHandler handler,
                                                               LockCollectionHandler lockCollectionHandler) {
        return route()
                .path(RouteNames.MRA_API_BASE_URL,
                        builder -> builder
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.PAYMENT_COLLECTION, paymentCollectionHandler::collectPaymentBySamityV1))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.PAYMENT_COLLECTION_BY_FIELD_OFFICER, paymentCollectionHandler::collectPaymentByFieldOfficer))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .POST(RouteNames.AUTHORIZE_COLLECTION, authorizeCollectionHandler::authorizeCollection))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_DATA_STAGING_COLLECTION_BY_OFFICE, handler::gridViewOfRegularCollectionByOffice))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_DATA_STAGING_COLLECTION, handler::gridViewOfRegularCollectionByFieldOfficer))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_SAMITY, handler::getCollectionStagingDataDetailViewBySamity))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_ACCOUNT, handler::getCollectionStagingDataDetailViewByAccount))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_AUTHORIZATION, handler::gridViewOfRegularCollectionAuthorizationByOffice))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_MEMBER, handler::getCollectionStagingDataDetailViewByMemberId))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_DATA_STAGING_SPECIAL_COLLECTION_BY_OFFICE, handler::gridViewOfSpecialCollectionByOffice))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_DATA_STAGING_SPECIAL_COLLECTION, handler::gridViewOfSpecialCollectionByFieldOfficer))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.GRID_VIEW_SPECIAL_COLLECTION_AUTHORIZATION, handler::gridViewOfSpecialCollectionAuthorizationByOffice))
                                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .GET(RouteNames.DETAIL_VIEW_COLLECTION_OF_FIELD_OFFICER, handler::getCollectionDetailViewByFieldOfficer))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .PUT(RouteNames.UPDATE_PAYMENT_COLLECTION, paymentCollectionHandler::updateCollectionPaymentBySamity))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.LOCK_COLLECTION, lockCollectionHandler::lockCollection))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.UNLOCK_COLLECTION, lockCollectionHandler::unlockCollection))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.REJECT_COLLECTION, authorizeCollectionHandler::rejectCollection))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestedBuilder -> nestedBuilder
                                        .POST(RouteNames.COMMIT_COLLECTION, handler::editCommitForCollectionDataBySamity))
                                .nest(RequestPredicates.accept(APPLICATION_JSON), nestBuilder -> nestBuilder
                                        .POST(RouteNames.UNAUTHORIZE_COLLECTION, authorizeCollectionHandler::unauthorizeCollection))
                )
                .build();
    }

}
