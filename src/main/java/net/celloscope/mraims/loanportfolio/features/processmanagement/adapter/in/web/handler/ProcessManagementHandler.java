package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ProcessManagementUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.request.ProcessTrackerRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessManagementHandler {
	
	private final ProcessManagementUseCase useCase;
	
	public Mono<ServerResponse> runDayEndProcessForOffice(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::runDayEndProcessForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> runMonthEndProcessForOffice(ServerRequest serverRequest){
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::runMonthEndProcessForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
	
	public Mono<ServerResponse> runForwardDayRoutineForOffice(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::runForwardDayRoutineForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
	
	public Mono<ServerResponse> gridViewOfOfficeProcessDashboardForMfi(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.mfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
						.limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
						.offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
						.build())
				.flatMap(useCase::gridViewOfOfficeProcessDashboardForMfi)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
	
	public Mono<ServerResponse> gridViewOfSamityProcessDashboardForOffice(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						.limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
						.offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
						.build())
				.flatMap(useCase::gridViewOfSamityProcessDashboardForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> getProcessManagementByOffice(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
				.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
				.build())
				.filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
				.flatMap(useCase::getCurrentProcessManagementForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

//	process management v2


	public Mono<ServerResponse> gridViewOfForwardDayRoutineForOffice(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						.limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
						.offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
						.build())
				.filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
				.flatMap(useCase::gridViewOfForwardDayRoutineForOffice)
				.flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(processTrackerResponseDTOMono))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}



	public Mono<ServerResponse> gridViewOfSamityCancelForOffice(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						.limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
						.offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
						.build())
				.filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
				.flatMap(useCase::gridViewOfSamityCancelForOffice)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> cancelRegularSamityListForOffice(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::cancelRegularSamityListForOffice)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> deleteRegularSamityListCancellationForOffice(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::deleteRegularSamityListCancellationForOffice)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> runForwardDayRoutineForOfficeV2(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(ProcessTrackerRequestDTO.class)
				.map(requestDTO -> {
					requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return requestDTO;
				})
				.flatMap(useCase::runForwardDayRoutineForOfficeV2)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> revertForwardDayRoutineForOfficeV2(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						.build())
				.flatMap(useCase::revertForwardDayRoutineForOfficeV2)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}

	public Mono<ServerResponse> getCreateTransactionButtonStatusForOffice(ServerRequest serverRequest) {
		return Mono.just(ProcessTrackerRequestDTO.builder()
						.officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						.build())
				.flatMap(useCase::getCreateTransactionButtonStatusForOffice)
				.flatMap(responseDTO -> ServerResponse.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(responseDTO))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
}
