package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionByFieldOfficerCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCollectionHandler {
	
	private final PaymentCollectionUseCase paymentCollectionUseCase;
	
	public Mono<ServerResponse> collectPaymentBySamityV1(ServerRequest serverRequest) {
		return this.buildRequestCommandForCollectionBySamity(serverRequest)
				.flatMap(paymentCollectionUseCase::collectPaymentBySamityV1)
				.flatMap(response -> ServerResponse
						.status(HttpStatus.CREATED)
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(response))
				.doOnSuccess(res -> log.info("Response for payment collection: {}", res.statusCode()))
				.doOnError(err -> log.error("Error occurred while processing payment collection request: {}", err.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
	
	public Mono<ServerResponse> collectPaymentByFieldOfficer(ServerRequest serverRequest) {
		return this.buildRequestCommandForCollectionByFieldOfficer(serverRequest)
				.flatMap(paymentCollectionUseCase::collectPaymentByFieldOfficer)
				.flatMap(response -> ServerResponse
						.status(HttpStatus.CREATED)
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(response))
				.doOnSuccess(res -> log.info("Response for payment collection: {}", res.statusCode()))
				.doOnError(err -> log.error("Error occurred while processing payment collection request: {}", err.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
	}
	
	public Mono<ServerResponse> updateCollectionPaymentBySamity(ServerRequest serverRequest) {
		return this.buildRequestCommandForCollectionBySamity(serverRequest)
				.flatMap(paymentCollectionUseCase::editAndUpdatePaymentBySamity)
				.flatMap(response -> ServerResponse
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(response))
				.doOnSuccess(res -> log.info("Response for payment collection: {}", res.statusCode()))
				.doOnError(err -> log.error("Error occurred while updating payment collection: {}", err.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
		
	}
	
	private Mono<PaymentCollectionBySamityCommand> buildRequestCommandForCollectionBySamity(ServerRequest serverRequest) {
		 return this.validateRequestForCollection(serverRequest)
				 .flatMap(req -> req.bodyToMono(PaymentCollectionBySamityCommand.class))
				 .map(command -> {
					command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
					command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
					command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return command;
				});
	}

	private Mono<PaymentCollectionByFieldOfficerCommand> buildRequestCommandForCollectionByFieldOfficer(ServerRequest serverRequest) {
		return this.validateRequestForCollection(serverRequest)
				.flatMap(req -> req.bodyToMono(PaymentCollectionByFieldOfficerCommand.class))
				.map(command -> {
					command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
					command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
					command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
					return command;
				});
	}

	private Mono<ServerRequest> validateRequestForCollection(ServerRequest serverRequest){
		return Mono.just(serverRequest)
				.filter(request -> !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
						&& !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
						&& !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "mfiId, officeId and loginId are required in query params")));
	}
}
