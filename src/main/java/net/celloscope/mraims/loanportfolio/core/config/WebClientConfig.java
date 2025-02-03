package net.celloscope.mraims.loanportfolio.core.config;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.TracerUtil;
import net.celloscope.mraims.loanportfolio.core.util.enums.DateTimeFormatterPattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Configuration
@Slf4j
public class WebClientConfig {

    private DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());

    private final HttpClient httpClient;
    private final HttpClient httpClientWithCert;
    private final TracerUtil tracerUtil;


    @Value("${mfi.base-url}")
    private String MFI_BASE_URL;
/*
    @Value("${cbs-middleware.base-url}")
    private String CBS_MIDDLEWARE_BASE_URL;*/


    public WebClientConfig(@Qualifier("reactiveHttpClientWithTimeout") HttpClient httpClient, @Qualifier("secureHttpClient") HttpClient httpClientWithCert, TracerUtil tracerUtil) {
        this.httpClient = httpClient;
        this.httpClientWithCert = httpClientWithCert;
        this.tracerUtil = tracerUtil;
    }

    @Bean
    public WebClient cctWebClient() {
        return getWebClient(MFI_BASE_URL, httpClientWithCert);
    }

    @Bean
    public WebClient mraGatewayWebClient() {
        return getWebClient(MFI_BASE_URL, httpClient);
    }

    private WebClient getWebClient(String baseUrl, HttpClient httpClient) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((ClientRequest request, ExchangeFunction next) -> {
                    ClientRequest updatedRequest = setRequestHeaders(request);
                    logRequest(updatedRequest);
                    return next.exchange(updatedRequest)
                            .doOnNext((ClientResponse response) -> {
                                setResponseHeaders(updatedRequest, response);
                                logResponse(response, baseUrl);
                            });
                })
                .build();
    }


    private ClientRequest setRequestHeaders(ClientRequest request) {
        String traceId = tracerUtil.getCurrentTraceId();
        if (Optional.ofNullable(request.headers().get(HeaderNames.TRACE_ID.getValue())).isPresent()
                && !Objects.requireNonNull(request.headers().get(HeaderNames.TRACE_ID.getValue()))
                .stream()
                .allMatch(String::isEmpty))
            traceId = Objects.requireNonNull(request.headers().get(HeaderNames.TRACE_ID.getValue()))
                    .stream()
                    .findFirst()
                    .orElse(traceId);
        return ClientRequest.from(request)
                .header(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue(), LocalDateTime.now().format(formater))
                .header(HeaderNames.TRACE_ID.getValue(), traceId)
                .build();
    }

    private void setResponseHeaders(ClientRequest request, ClientResponse response) {
        response.mutate().header(HeaderNames.RESPONSE_RECEIVED_TIME_IN_MS.getValue(), LocalDateTime.now().format(formater));
        if (!Objects.requireNonNull(request.headers().get(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue()))
                .stream()
                .allMatch(String::isEmpty)) {
            String s = Objects.requireNonNull(request.headers().get(HeaderNames.REQUEST_SENT_TIME_IN_MS.getValue()))
                    .stream()
                    .findAny().orElse(LocalDateTime.now().format(formater));
            response.mutate().header(HeaderNames.RESPONSE_TRANSMISSION_TIME_IN_MS.getValue(),
                    String.valueOf(
                            LocalDateTime.now()
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                    - LocalDateTime
                                    .parse(s, formater)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                    ));
        }
    }

    private void logRequest(ClientRequest request) {
        try {
            log.info("""
                            Request Sending From {}
                             Uri : {}
                             Method : {}
                             Headers : {}
                             Content type : {}
                             Acceptable Media Type {}
                            """,
                    InetAddress.getLocalHost().getHostAddress(),
                    request.url(),
                    request.method(),
                    request.headers(),
                    request.headers().getContentType(),
                    request.headers().getAccept());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void logResponse(ClientResponse response, String baseUrl) {
        log.info("""
                        Response Receiving from {}
                         Headers : {}
                         Response Status : {}
                         Content type : {}
                        """,
                baseUrl,
                response.headers().asHttpHeaders(),
                response.statusCode(),
                response.headers().contentType()
        );
        log.info("Response receiving time from {} is {} ms", baseUrl,
                Objects.requireNonNull(response.headers()
                        .header(HeaderNames.RESPONSE_TRANSMISSION_TIME_IN_MS.getValue())
                        .stream()
                        .findFirst()
                        .orElse("0")
                ));
    }

}

