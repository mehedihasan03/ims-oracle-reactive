package net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookGridViewUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookReportUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.PassbookReportResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class PassbookHandler {
    private final PassbookUseCase passbookUseCase;

    private final PassbookGridViewUseCase passbookGridViewUseCase;

    private final PassbookReportUseCase passbookReportUseCase;

    public Mono<ServerResponse> createPassbookEntryForLoan(ServerRequest serverRequest) {

        return passbookUseCase.getRepaymentScheduleAndCreatePassbookEntryForLoan(buildPassbookRequestDTO(serverRequest))
                .flatMap(passbookResponseDTOList ->
                        ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(passbookResponseDTOList)
                )
                .onErrorMap(this::buildResponseStatusException);
    }


    public Mono<ServerResponse> createPassbookEntryforSavings(ServerRequest serverRequest) {

        return passbookUseCase.createPassbookEntryForSavings(buildPassbookRequestDTO(serverRequest))
                .flatMap(passbookResponseDTOList -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponseDTOList))
                .onErrorMap(this::buildResponseStatusException);
    }

    public Mono<ServerResponse> createPassbookEntryForTotalAccruedInterestDeposit(ServerRequest serverRequest) {

        return passbookUseCase.createPassbookEntryForTotalAccruedInterestDeposit(buildPassbookRequestDTO(serverRequest))
                .flatMap(passbookResponseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponseDTO))
                .onErrorMap(this::buildResponseStatusException);
    }


    public Mono<ServerResponse> createPassbookEntryForSavingsWithdraw(ServerRequest serverRequest) {
        return passbookUseCase.createPassbookEntryForSavingsWithdraw(buildPassbookRequestDTO(serverRequest))
                .flatMap(aBoolean -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(aBoolean))
                .onErrorMap(this::buildResponseStatusException);
    }

    private PassbookRequestDTO buildPassbookRequestDTO(ServerRequest serverRequest) {
        BigDecimal amount = new BigDecimal(serverRequest.queryParam(QueryParams.AMOUNT.getValue()).orElse(""));
        String loanAccountId = serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("");
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        String transactionId = serverRequest.queryParam(QueryParams.TRANSACTION_ID.getValue()).orElse("");
        String transactionCode = serverRequest.queryParam(QueryParams.TRANSACTION_CODE.getValue()).orElse("");
        String mfiId = serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse("");
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        LocalDate transactionDate = LocalDate.parse(serverRequest.queryParam(QueryParams.TRANSACTION_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));

        log.info("Build passbook request dto : {}", PassbookRequestDTO
                .builder()
                .amount(amount)
                .loanAccountId(loanAccountId)
                .savingsAccountId(savingsAccountId)
                .transactionId(transactionId)
                .transactionCode(transactionCode)
                .mfiId(mfiId)
                .loginId(loginId)
                .transactionDate(transactionDate)
                .build());

        return PassbookRequestDTO
                .builder()
                .amount(amount)
                .loanAccountId(loanAccountId)
                .savingsAccountId(savingsAccountId)
                .transactionId(transactionId)
                .transactionCode(transactionCode)
                .mfiId(mfiId)
                .loginId(loginId)
                .transactionDate(transactionDate)
                .build();
    }


    public Mono<ServerResponse> passbookGridView(ServerRequest request) {
        if (request.queryParam(QueryParams.OFFICE_ID.getValue()).isEmpty()) {
            return ServerResponse.badRequest().bodyValue("OfficeId is mandatory.");
        }
        PassbookGridViewQueryDTO queryDTO = PassbookGridViewQueryDTO.builder()
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).get())
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(null))
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(null))
                .fromDate(LocalDate.parse(
                        request.queryParam(QueryParams.FROM_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.now())/* + "T00:00:00.000"*/)))
                .toDate(LocalDate.parse(
                        request.queryParam(QueryParams.TO_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.now().plusDays(1))/* + "T23:59:59.999"*/)))
                .build();
        Mono<PassbookGridViewResponseDTO> passbookResponseDTOMono = passbookGridViewUseCase.passbookGridViewData(queryDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(passbookResponseDTOMono, PassbookGridViewResponseDTO.class);
    }


    public ResponseStatusException buildResponseStatusException(Throwable throwable) {
        if (throwable instanceof ExceptionHandlerUtil) {
            return new ResponseStatusException(((ExceptionHandlerUtil) throwable).getCode(), throwable.getMessage());
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
    }

    public Mono<ServerResponse> passbookReportViewV1(ServerRequest request) {
        if (request.queryParam(QueryParams.SAMITY_ID.getValue()).isEmpty()) {
            return ServerResponse.badRequest().bodyValue("SamityID is mandatory.");
        }
        PassbookReportQueryDTO queryDTO = PassbookReportQueryDTO.builder()
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).get())
                .fromDate(LocalDate.parse(
                        request.queryParam(QueryParams.FROM_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.now())/* + "T00:00:00.000"*/)))
                .toDate(LocalDate.parse(
                        request.queryParam(QueryParams.TO_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.now().plusDays(1))/* + "T23:59:59.999"*/)))
                .accountNo(request.queryParam(QueryParams.ACCOUNT_NO.getValue()).orElse(null))
                .searchText(request.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse(null))
                .build();
        Mono<PassbookReportResponseDTO> passbookResponseDTOMono = passbookReportUseCase.getPassbookReportFromDB(queryDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(passbookResponseDTOMono, PassbookReportResponseDTO.class);
    }

    public Mono<ServerResponse> passbookReportViewV2(ServerRequest request) {
        if (request.queryParam(QueryParams.SAMITY_ID.getValue()).isEmpty()) {
            return ServerResponse.badRequest().bodyValue("SamityID is mandatory.");
        }
        PassbookReportQueryDTO queryDTO = PassbookReportQueryDTO.builder()
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).get())
                .fromDate(LocalDate.parse(
                        request.queryParam(QueryParams.FROM_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.ofEpochDay(0))/* + "T00:00:00.000"*/)))
                .toDate(LocalDate.parse(
                        request.queryParam(QueryParams.TO_DATE.getValue())
                                .orElse(String.valueOf(LocalDate.now().plusDays(1))/* + "T23:59:59.999"*/)))
                .accountNo(request.queryParam(QueryParams.ACCOUNT_NO.getValue()).orElse(null))
                .searchText(request.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse(null))
                .build();
        Mono<PassbookReportResponseDTO> passbookResponseDTOMono = passbookReportUseCase.getPassbookReportFromDBV2(queryDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(passbookResponseDTOMono, PassbookReportResponseDTO.class);
    }

    public Mono<ServerResponse> getPassbookEntitiesBetweenTransactionDates(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam("savingsAccountId").orElse("");
        LocalDate fromDate = LocalDate.parse(serverRequest.queryParam(QueryParams.FROM_DATE.getValue()).orElse(String.valueOf(LocalDate.now())));
        LocalDate toDate = LocalDate.parse(serverRequest.queryParam(QueryParams.TO_DATE.getValue()).orElse(String.valueOf(LocalDate.now())));

        return passbookUseCase
                .getPassbookEntriesBetweenTransactionDates(savingsAccountId, fromDate, toDate)
                .flatMap(passbookResponseDTOS -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passbookResponseDTOS));
    }

    public Mono<ServerResponse> getPassbookEntitiesByManagementProcessIdAndTransactionCodeAndPaymentMode(ServerRequest serverRequest) {
        String managementProcessId = serverRequest.queryParam("managementProcessId").orElse("");
        String transactionCode = serverRequest.queryParam("transactionCode").orElse(null);
        String paymentMode = serverRequest.queryParam("paymentMode").orElse(null);
        String savingsTypeId = serverRequest.queryParam("savingsTypeId").orElse(null);

        return passbookUseCase
                .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(managementProcessId, transactionCode, paymentMode, savingsTypeId)
                .collectList()
                .flatMap(passbookResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponse));
    }


    public Mono<ServerResponse> getLastPassbookEntryByTransactionCodeAndLoanAccountOid(ServerRequest serverRequest) {
        String loanAccountOid = serverRequest.queryParam("loanAccountOid").orElse("");
        String transactionCode = serverRequest.queryParam("transactionCode").orElse(null);

        return passbookUseCase
                .getLastPassbookEntryByTransactionCodeAndLoanAccountOid(transactionCode, loanAccountOid)
                .flatMap(passbookResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponse));
    }


    public Mono<ServerResponse> getPassbookEntriesForAdvanceLoanRepaymentDebit(ServerRequest serverRequest) {
        String officeId = serverRequest.queryParam("officeId").orElse("");
        LocalDate businessDate = LocalDate.parse(serverRequest.queryParam("businessDate").orElse(String.valueOf(LocalDate.now())));

        return passbookUseCase
                .getPassbookEntriesForAdvanceLoanRepaymentDebit(officeId, businessDate)
                .collectList()
                .flatMap(passbookResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponse));
    }

    public Mono<ServerResponse> getPassbookEntriesForAdvanceLoanRepaymentCredit(ServerRequest serverRequest) {
        String officeId = serverRequest.queryParam("officeId").orElse("");
        LocalDate businessDate = LocalDate.parse(serverRequest.queryParam("businessDate").orElse(String.valueOf(LocalDate.now())));

        return passbookUseCase
                .getPassbookEntriesForAdvanceLoanRepaymentCredit(officeId, businessDate)
                .collectList()
                .flatMap(passbookResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponse));
    }


    public Mono<ServerResponse> getPassbookEntriesForPrincipalAndServiceChargeOutstanding(ServerRequest serverRequest) {
        String officeId = serverRequest.queryParam("officeId").orElse("");
        LocalDate businessDate = LocalDate.parse(serverRequest.queryParam("businessDate").orElse(String.valueOf(LocalDate.now())));

        return passbookUseCase
                .getPassbookEntriesForPrincipalAndServiceChargeOutstanding(officeId, businessDate)
                .collectList()
                .flatMap(passbookResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(passbookResponse));
    }

}
