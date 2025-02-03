package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.enums.UserRoles;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataDetailViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataGenerationStatusResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataMemberInfoDetailViewResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;


@Slf4j
@Component
@RequiredArgsConstructor
public class StagingDataHandler {

    private final IStagingDataUseCase stagingDataUseCase;

    public Mono<ServerResponse> generateStagingDataAndStagingAccountData(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(StagingDataRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getMfiId()) && !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()) && !HelperUtil.checkIfNullOrEmpty(requestDTO.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "mfiId, officeId and loginId are required")))
                .doOnNext(requestDTO -> log.debug("Staging Data generation requestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::generateStagingDataAndStagingAccountData)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getStagingDataDetailViewResponse(ServerRequest request) {
        StagingDataRequestDTO requestDTO = buildRequestDTOFromQueryParams(request);
        Mono<StagingDataDetailViewResponseDTO> stagingDataGridViewResponseDTO = stagingDataUseCase.getStagingDataDetailViewResponseBySamityId(requestDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stagingDataGridViewResponseDTO, StagingDataDetailViewResponseDTO.class);
    }

    public Mono<ServerResponse> getStagingDataDetailViewResponseByAccountId(ServerRequest request) {
        StagingDataRequestDTO requestDTO = buildRequestDTOFromQueryParams(request);
        Mono<StagingDataMemberInfoDetailViewResponseDTO> stagingDataMemberDetailViewResponse = stagingDataUseCase.getStagingDataDetailViewResponseByAccountId(requestDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stagingDataMemberDetailViewResponse, StagingDataMemberInfoDetailViewResponseDTO.class);
    }

    public Mono<ServerResponse> getStagingDataGenerationStatusResponse(ServerRequest request) {
        StagingDataRequestDTO requestDTO = buildRequestDTOFromQueryParams(request);
        Mono<StagingDataGenerationStatusResponseDTO> stagingDataGenerationStatusResponse = stagingDataUseCase.getStagingDataGenerationStatusResponse(requestDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stagingDataGenerationStatusResponse, StagingDataGenerationStatusResponseDTO.class);
    }

    public Mono<ServerResponse> resetStagingProcessTrackerEntriesByOfficeId(ServerRequest request) {
        String officeId = request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        return stagingDataUseCase.resetStagingProcessTrackerEntriesByOfficeId(officeId)
                .flatMap(s -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(s))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> getStagingDataDetailViewResponseByMemberId(ServerRequest request) {
        StagingDataRequestDTO requestDTO = buildRequestDTOFromQueryParams(request);
        Mono<StagingDataMemberInfoDetailViewResponseDTO> stagingDataDetailViewResponse = stagingDataUseCase.getStagingDataDetailViewResponseByMemberId(requestDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stagingDataDetailViewResponse, StagingDataMemberInfoDetailViewResponseDTO.class);
    }

    private static StagingDataRequestDTO buildRequestDTOFromQueryParams(ServerRequest request) {
        return StagingDataRequestDTO.builder()
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""))
                .mfiId(request.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .memberId(request.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                .accountId(request.queryParam(QueryParams.ACCOUNT_ID.getValue()).orElse(""))
                .employeeId(request.queryParam(QueryParams.EMPLOYEE_ID.getValue()).orElse(""))
                .build();
    }

//    Staging Data: Process Management V2
    public Mono<ServerResponse> gridViewOfStagingDataStatusByOffice(ServerRequest serverRequest) {

//        Required Query Parameters
        final List<String> queryParamsList = List.of(
                QueryParams.OFFICE_ID.getValue()
        );

//        User Accessibility Roles
        final List<String> userAccessRolesList =  List.of(
                UserRoles.MFI_BRANCH_MANAGER.getValue()
        );

        return Mono.fromSupplier(() -> buildRequestDTOFromContextApi(serverRequest))
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
//                @TODO: Added mock data: delete after proper header set from api-gateway
                .map(requestDTO -> {
//                    requestDTO.setMfiId("M1001");
//                    requestDTO.setLoginId("branch-manager");
                    requestDTO.setUserRole(UserRoles.MFI_BRANCH_MANAGER.getValue());
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
//                .flatMap(requestDTO -> CommonFunctions.checkAccessibilityAndValidateRequestDTO(requestDTO, queryParamsList, userAccessRolesList))
                .flatMap(stagingDataUseCase::gridViewOfStagingDataStatusByOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfStagingDataStatusByOfficeFilteredByFieldOfficer(ServerRequest serverRequest) {

        return Mono.just(StagingDataRequestDTO.builder().build())
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setFieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::gridViewOfStagingDataStatusByOfficeFilteredByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfStagingDataStatusByFieldOfficer(ServerRequest serverRequest) {

//        Required Query Parameters
        final List<String> queryParamsList = List.of(
                QueryParams.EMPLOYEE_ID.getValue()
        );

//        User Accessibility Roles
        final List<String> userAccessRolesList =  List.of(
                UserRoles.MFI_FIELD_OFFICER.getValue()
        );

        return Mono.fromSupplier(() -> buildRequestDTOFromContextApi(serverRequest))
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    requestDTO.setEmployeeId(serverRequest.queryParam(QueryParams.EMPLOYEE_ID.getValue()).orElse(""));
                    return requestDTO;
                })
//                @TODO: Added mock data: delete after proper header set from api-gateway
                .map(requestDTO -> {
//                    requestDTO.setMfiId("M1001");
//                    requestDTO.setLoginId("field-officer");
                    requestDTO.setUserRole(UserRoles.MFI_FIELD_OFFICER.getValue());
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
//                .flatMap(requestDTO -> CommonFunctions.checkAccessibilityAndValidateRequestDTO(requestDTO, queryParamsList, userAccessRolesList))
                .flatMap(stagingDataUseCase::gridViewOfStagingDataStatusByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> generateStagingDataByOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(StagingDataRequestDTO.class)
            .flatMap(requestDTO -> Mono.deferContextual(context -> {
                StagingDataRequestDTO stagingDataRequestDTO = buildRequestDTOFromContextApi(serverRequest);
                stagingDataRequestDTO.setOfficeId(requestDTO.getOfficeId());
                context.stream().forEach(entry -> log.info("Context key: {}, value: {}", entry.getKey(), entry.getValue()));
                return Mono.just(stagingDataRequestDTO);
            }))
                .map(requestDTO -> {
                    StagingDataRequestDTO stagingDataRequestDTO = buildRequestDTOFromContextApi(serverRequest);
                    stagingDataRequestDTO.setOfficeId(requestDTO.getOfficeId());
                    return stagingDataRequestDTO;
                })
                .map(requestDTO -> {
                    requestDTO.setUserRole(UserRoles.MFI_BRANCH_MANAGER.getValue());
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::generateStagingDataByOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> invalidateStagingDataBySamityList(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(StagingDataRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .doOnNext(stagingDataRequestDTO -> log.debug("Invalidate RequestDTO: {}", stagingDataRequestDTO))
                .flatMap(stagingDataUseCase::invalidateStagingDataBySamityList)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
//                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> regenerateStagingDataBySamityList(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(StagingDataRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::regenerateStagingDataBySamityList)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> deleteStagingDataByOffice(ServerRequest serverRequest) {
        return Mono.just(StagingDataRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::deleteStagingDataByOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> downloadStagingDataByFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(StagingDataRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::downloadStagingDataByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> deleteStagingDataByFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(StagingDataRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::deleteStagingDataByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> stagedSamityListByFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(StagingDataRequestDTO.builder()
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .build())
                .doOnNext(requestDTO -> log.info("Staging Data RequestDTO: {}", requestDTO))
                .flatMap(stagingDataUseCase::getStagedSamityListByFieldOfficerId)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private static StagingDataRequestDTO buildRequestDTOFromContextApi(ServerRequest serverRequest) {
        return StagingDataRequestDTO.builder()
                .mfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                .userRole(serverRequest.queryParam(QueryParams.USER_ROLE.getValue()).orElse(""))
//                .mfiId(serverRequest.headers().firstHeader(QueryParams.MFI_ID.getValue()))
//                .loginId(serverRequest.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
//                .officeId(serverRequest.headers().firstHeader(QueryParams.OFFICE_ID.getValue()))
//                .userRole(serverRequest.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .build();
    }

}
