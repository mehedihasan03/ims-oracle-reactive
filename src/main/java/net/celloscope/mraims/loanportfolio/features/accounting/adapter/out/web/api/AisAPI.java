package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.exception.WebClientExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.dto.JournalSnapshotCommand;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class AisAPI {
    private final WebClient webClient;
    private static final String IMS_URL_TEMPLATE = "/mra-ims/mfi/api/v1/journal/save";
    private static final String IMS_URL_JOURNAL_SNAPSHOT = "/mra-ims/mfi/api/v1/chart-of-accounts/ledger-subledger/history/save";


    public AisAPI(@Qualifier("mraGatewayWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<AisResponse> postAccountingToIms(JournalRequestDTO request) {
        /*log.info("JournalRequestDTO from postAccountingToIms: {}", request);*/
        return CurrentTenantIdHolder.getId()
                .flatMap(instituteOid -> webClient
                        .post()
                        .uri(uriBuilder -> uriBuilder
                                .path(IMS_URL_TEMPLATE)
                                .queryParam(QueryParams.INSTITUTE_OID.getValue(),instituteOid)
                                .build())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(Mono.just(request), JournalRequestDTO.class)
                        .retrieve()
                        .onStatus(HttpStatus::isError, response -> processError(response, IMS_URL_TEMPLATE))
                        .bodyToMono(AisResponse.class)
                        .doOnError(throwable ->  log.error("Failed to get Ais Response from mra gateway : {}", throwable.getMessage()))
                        .doOnSuccess(response -> log.info("Successfully got response from mra gateway"))
                        /*.onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.UNAUTHORIZED, throwable.getMessage()))*/
                        );

    }

    private Mono<Throwable> processError(ClientResponse response, String url) {
        return response.bodyToMono(Map.class)
                .doOnSuccess(stringStringMap ->
                        log.error("""
                                        Client returned error response for {}
                                         Response Status : {}
                                         Response Body : {}
                                        """,
                                url,
                                response.statusCode(),
                                stringStringMap)
                )
                .switchIfEmpty(Mono.error(new WebClientExceptionHandlerUtil(response.statusCode(), "Service Sent Empty Error Response Body", null)))
                .flatMap(stringStringMap -> {
                    log.error("Error Body : {}", stringStringMap);
                    return Mono.error(new ExceptionHandlerUtil(response.statusCode(), stringStringMap.get("error").toString()));
                });
    }

    public Mono<String> createLedgerSnapshot(JournalSnapshotCommand journalSnapshotCommand) {

        log.info("Request Body to create Ledger Snapshot : {}", journalSnapshotCommand);
        return CurrentTenantIdHolder.getId()
                .flatMap(instituteOid -> webClient
                                .post()
                                .uri(uriBuilder -> uriBuilder
                                        .path(IMS_URL_JOURNAL_SNAPSHOT)
                                        .queryParam(QueryParams.INSTITUTE_OID.getValue(),instituteOid)
                                        .queryParam(QueryParams.OFFICE_ID.getValue(), journalSnapshotCommand.getOfficeId())
                                        .queryParam(QueryParams.MFI_ID.getValue(), journalSnapshotCommand.getMfiId())
                                        .queryParam(QueryParams.LOGIN_ID.getValue(), journalSnapshotCommand.getLoginId())
                                        .build())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .body(Mono.just(journalSnapshotCommand), JournalSnapshotCommand.class)
                                .retrieve()
                                .onStatus(HttpStatus::isError, response -> processError(response, IMS_URL_JOURNAL_SNAPSHOT))
                                .bodyToMono(String.class)
                                .doOnError(throwable ->  log.error("Failed to save Journal Snapshot from mra gateway : {}", throwable.getMessage()))
                                .doOnSuccess(response -> log.info("Successfully saved Journal Snapshot"))
                        /*.onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.UNAUTHORIZED, throwable.getMessage()))*/
                );
    }
}

