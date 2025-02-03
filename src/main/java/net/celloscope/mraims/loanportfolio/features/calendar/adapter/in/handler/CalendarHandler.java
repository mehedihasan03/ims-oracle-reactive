package net.celloscope.mraims.loanportfolio.features.calendar.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.request.CalendarRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class CalendarHandler {
	
	private final CalendarUseCase useCase;
	
	public Mono<ServerResponse> getNextBusinessDateForOffice(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(CalendarRequestDTO.class)
				.flatMap(requestDTO -> useCase.getNextBusinessDateForOffice(requestDTO.getOfficeId(), requestDTO.getCurrentBusinessDate()))
				.flatMap(businessDate -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(businessDate))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
}
