package net.celloscope.mraims.loanportfolio.features.transaction.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionGridViewUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionReportUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionReportResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHandler {
    private final TransactionUseCase transactionUseCase;

    private final TransactionGridViewUseCase transactionGridViewUseCase;

    private final TransactionReportUseCase transactionReportUseCase;

    public Mono<ServerResponse> createTransaction(ServerRequest request) {
        String samityId = request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse("");
        String officeId = request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        final String transactionProcessId = UUID.randomUUID().toString();
        final String managementProcessId = UUID.randomUUID().toString();
        String source = request.queryParam(QueryParams.SOURCE.getValue()).orElse("Application");
        Mono<TransactionResponseDTO> transactionResponseDTOMono = transactionUseCase.createTransactionForOneSamity(samityId, managementProcessId, transactionProcessId, officeId, source);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(transactionResponseDTOMono, TransactionResponseDTO.class);
    }

    public Mono<ServerResponse> transactionGridView(ServerRequest request) {
        if (request.queryParam(QueryParams.OFFICE_ID.getValue()).isEmpty()) {
            return ServerResponse.badRequest().bodyValue("OfficeId is mandatory.");
        }
        TransactionGridViewQueryDTO queryDTO = TransactionGridViewQueryDTO.builder()
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).get())
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(null))
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(null))
                .fromDate(LocalDateTime.parse(
                        request.queryParam(QueryParams.FROM_DATE.getValue())
                                .orElse(LocalDate.now() + "T00:00:00.000")))
                .toDate(LocalDateTime.parse(
                        request.queryParam(QueryParams.TO_DATE.getValue())
                                .orElse(LocalDate.now() + "T23:59:59.999")))
                .build();
        Mono<TransactionGridViewResponseDTO> transactionResponseDTOMono = transactionGridViewUseCase.transactionGridViewData(queryDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(transactionResponseDTOMono, TransactionGridViewResponseDTO.class);
    }

    public Mono<ServerResponse> transactionReportView(ServerRequest request) {
        if (request.queryParam(QueryParams.SAMITY_ID.getValue()).isEmpty()) {
            return ServerResponse.badRequest().bodyValue("SamityID is mandatory.");
        }
        TransactionReportQueryDTO queryDTO = TransactionReportQueryDTO.builder()
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).get())
                .fromDate(LocalDateTime.parse(
                        request.queryParam(QueryParams.FROM_DATE.getValue())
                                .orElse(LocalDate.now() + "T00:00:00.000")))
                .toDate(LocalDateTime.parse(
                        request.queryParam(QueryParams.TO_DATE.getValue())
                                .orElse(LocalDate.now() + "T23:59:59.999")))
                .accountNo(request.queryParam(QueryParams.ACCOUNT_NO.getValue()).orElse(null))
                .searchText(request.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse(null))
                .build();
        Mono<TransactionReportResponseDTO> transactionResponseDTOMono = transactionReportUseCase.getTransactionsReportFromDB(queryDTO);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(transactionResponseDTOMono, TransactionReportResponseDTO.class);
    }

    public Mono<ServerResponse> createTransactionForAccruedInterest(ServerRequest request) {
        String loginId = request.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String savingsAccountId = request.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        LocalDate interestCalculationDate = LocalDate.parse(request.queryParam(QueryParams.INTEREST_CALCULATION_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        BigDecimal accruedInterestAmount = BigDecimal.valueOf(Long.parseLong(request.queryParam(QueryParams.ACCRUED_INTEREST_AMOUNT.getValue()).orElse("")));
        String officeId = request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new IllegalArgumentException("officeId is required."));
        return transactionUseCase
                .createTransactionForSavingsInterestDeposit(loginId, savingsAccountId, interestCalculationDate, accruedInterestAmount, officeId)
                .doOnRequest(l -> log.info("Request received to create transaction for accrued interest with loginId : {}, savingsAccountId : {}, interestCalculationDate : {}, accruedInterestAmount : {}", loginId, savingsAccountId, interestCalculationDate, accruedInterestAmount))
                .doOnSuccess(transactionResponseDTO -> log.info("Successfully created transaction & ready to send response : {}", transactionResponseDTO))
                        .flatMap(transactionResponseDTO -> ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(transactionResponseDTO));
    }

    /*public Mono<ServerResponse> createTransactionForHalfYearlyInterestPosting(ServerRequest request) {
        String loginId = request.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String savingsAccountId = request.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        Integer interestPostingYear = Integer.valueOf(request.queryParam(QueryParams.INTEREST_CALCULATION_YEAR.getValue()).orElse(""));
        String closingType = request.queryParam(QueryParams.CLOSING_TYPE.getValue()).orElse("");

        return transactionUseCase
                .createTransactionForHalfYearlyInterestPosting(savingsAccountId, interestPostingYear, closingType, loginId)
                .doOnRequest(l -> log.info("Request received to create transaction for accrued interest with savingsAccountId : {}, interestPostingYear : {}, closingType : {}, loginId : {}", savingsAccountId, interestPostingYear, closingType, loginId))
                .doOnSuccess(transactionResponseDTO -> log.info("Successfully created transaction & ready to send response : {}", transactionResponseDTO))
                .flatMap(transactionResponseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(transactionResponseDTO));
    }*/

}
