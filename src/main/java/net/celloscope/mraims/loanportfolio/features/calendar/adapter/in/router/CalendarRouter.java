package net.celloscope.mraims.loanportfolio.features.calendar.adapter.in.router;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.calendar.adapter.in.handler.CalendarHandler;
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
public class CalendarRouter {
	
	private final CalendarHandler handler;
	
	@Bean
	public RouterFunction<ServerResponse> calendarRouterConfig() {
		return RouterFunctions.route()
				.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
						.POST(MRA_API_BASE_URL.concat(GET_NEXT_BUSINESS_DAY_FROM_CALENDAR), handler::getNextBusinessDateForOffice))
				.build();
	}
}
