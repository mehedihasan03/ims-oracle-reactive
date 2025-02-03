package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.api;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.WebClientExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.dto.WriteOffAccountResponse;
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
public class WriteOffAccountApi {

    private final WebClient webClient;
    private static final String IMS_URL_TEMPLATE = "/mra-ims/mfi/api/v1/loan-provision/writing-off-account/list";


    public WriteOffAccountApi(@Qualifier("mraGatewayWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<WriteOffAccountResponse> getWriteOffAccountList(WriteOffCollectionAccountDataRequestDto dataRequestDto) {
//        log.info("calling getWriteOffCollectionList 3rd party api: {}", dataRequestDto);
        return CurrentTenantIdHolder.getId()
                .flatMap(instituteOid -> webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(IMS_URL_TEMPLATE)
                                .queryParam(QueryParams.OFFSET.getValue(), 0)
                                .queryParam(QueryParams.LIMIT.getValue(), 100000)
                                .queryParam(QueryParams.INSTITUTE_OID.getValue(), dataRequestDto.getInstituteOid())
                                .queryParam(QueryParams.OFFICE_ID.getValue(), dataRequestDto.getOfficeId())
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .onStatus(HttpStatus::isError, response -> processError(response, IMS_URL_TEMPLATE))
                        .bodyToMono(WriteOffAccountResponse.class)
                        .doOnRequest(l -> log.info("request received to get write off collection list from MFI gateway"))
                        .doOnSuccess(writeOffAccountResponse -> log.info("Successfully got write off list Response from mra gateway {}", writeOffAccountResponse))
                        .doOnError(throwable -> log.error("Failed to get write off list Response from mra gateway : {}", throwable.getMessage()))
                        .doOnSuccess(response -> log.info("Successfully got response from mra gateway"))
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
                    return Mono.error(new WebClientExceptionHandlerUtil(response.statusCode(), stringStringMap.toString(), stringStringMap));
                });
    }

}
