package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.in.web.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.in.web.handler.ProcessManagementHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static net.celloscope.mraims.loanportfolio.core.routes.RouteNames.*;

@Configuration
@RequiredArgsConstructor
public class ProcessManagementRouter {
	
	private final ProcessManagementHandler handler;

	@Bean
	public RouterFunction<ServerResponse> processManagementRouterConfigV2() {
		return RouterFunctions.route()
				.path(MRA_API_BASE_URL_V2.concat(PROCESS_MANAGEMENT_BASE_URL), this::processManagementRoutesV2)
				.build();
	}

	private RouterFunction<ServerResponse> processManagementRoutesV2() {
		return RouterFunctions.route()
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(FORWARD_DAY_ROUTINE.concat(BY_OFFICE).concat(GRID_VIEW), handler::gridViewOfForwardDayRoutineForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(FORWARD_DAY_ROUTINE.concat(BY_OFFICE).concat(START), handler::runForwardDayRoutineForOfficeV2))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.DELETE(FORWARD_DAY_ROUTINE.concat(BY_OFFICE).concat(DELETE), handler::revertForwardDayRoutineForOfficeV2))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(CANCEL_SAMITY.concat(BY_OFFICE).concat(GRID_VIEW), handler::gridViewOfSamityCancelForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(CANCEL_SAMITY.concat(BY_OFFICE).concat(CANCEL), handler::cancelRegularSamityListForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.DELETE(CANCEL_SAMITY.concat(BY_OFFICE).concat(DELETE), handler::deleteRegularSamityListCancellationForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(BY_OFFICE.concat(BTN_CREATE_TRANSACTION).concat(STATUS), handler::getCreateTransactionButtonStatusForOffice))
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> processManagementRouterConfig() {
		return RouterFunctions.route()
				.path(MRA_API_BASE_URL.concat(PROCESS_MANAGEMENT_BASE_URL), this::processManagementRoutes)
				.build();
	}
	
	private RouterFunction<ServerResponse> processManagementRoutes() {
		return RouterFunctions.route()
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(PROCESS_MANAGEMENT_RUN_DAY_END_PROCESS, handler::runDayEndProcessForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(PROCESS_MANAGEMENT_RUN_MONTH_END_PROCESS, handler::runMonthEndProcessForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(PROCESS_MANAGEMENT_RUN_FORWARD_DAY_ROUTINE, handler::runForwardDayRoutineForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(PROCESS_MANAGEMENT_GRID_VIEW_PROCESS_DASHBOARD_BY_MFI, handler::gridViewOfOfficeProcessDashboardForMfi))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(PROCESS_MANAGEMENT_GRID_VIEW_PROCESS_DASHBOARD_BY_OFFICE, handler::gridViewOfSamityProcessDashboardForOffice))
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.GET(GET_PROCESS_MANAGEMENT_FOR_OFFICE, handler::getProcessManagementByOffice))
				.build();
	}
}
