package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.in.handler.LoanAdjustmentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_LOGINID;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.EMPTY_MFIID;

@Configuration
@RequiredArgsConstructor
public class LoanAdjustmentRouter {

        private final LoanAdjustmentHandler handler;

        @Bean
        public RouterFunction<ServerResponse> loanAdjustmentRouterConfig() {
                return RouterFunctions.route()
                                .path(MRA_API_BASE_URL_V2.concat(LOAN_ADJUSTMENT), this::loanAdjustmentRoutes)
                                .build();
        }

        private RouterFunction<ServerResponse> loanAdjustmentRoutes() {
                return RouterFunctions.route()
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(BY_MEMBER.concat(ADJUST_LOAN), handler::createLoanAdjustmentForMember))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(BY_SAMITY.concat(AUTHORIZE), handler::authorizeLoanAdjustment))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .POST(BY_SAMITY.concat(SUBMIT), handler::submitLoanAdjustment))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_OFFICE.concat(GRID_VIEW), handler::gridViewOfLoanAdjustmentForOffice))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_FIELD_OFFICER.concat(GRID_VIEW), handler::gridViewOfLoanAdjustmentForFieldOfficer))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_SAMITY.concat(DETAIL_VIEW), handler::detailViewOfLoanAdjustmentForSamity))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_MEMBER.concat(DETAIL_VIEW), handler::detailViewOfLoanAdjustmentForMember))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(BY_MEMBER.concat(DETAILS), handler::detailsOfLoanAdjustmentCreationForAMember))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(GRID_VIEW, handler::loanAdjustmentGridView))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET(DETAIL_VIEW, handler::loanAdjustmentDetailView))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .PUT(EDIT, handler::updateAdjustmentAmount))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .PUT(BY_ENTITY.concat(SUBMIT), handler::submitLoanAdjustmentEntity))
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .DELETE(BY_ENTITY.concat(DELETE), handler::deleteAdjustmentData))
                        .build();
        }
}
