package net.celloscope.mraims.loanportfolio.core.filters;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.TracerUtil;
import net.celloscope.mraims.loanportfolio.core.util.enums.DateTimeFormatterPattern;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class IWebFilter implements WebFilter {

    private DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
    private final TracerUtil tracerUtil;

    public IWebFilter(TracerUtil tracerUtil) {
        this.tracerUtil = tracerUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        setRequestHeaders(serverWebExchange);
        setMdcAttributeForLogBack(serverWebExchange);
        logRequest(serverWebExchange.getRequest());
        setResponseHeader(serverWebExchange);
        logResponse(serverWebExchange);

//        return webFilterChain.filter(serverWebExchange);

        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap(); // copy the current MDC context

        return webFilterChain.filter(serverWebExchange).contextWrite(ctx -> ctx.put("mdcContextMap", mdcContextMap)); // put the MDC context into the subscriber context
    }

    private void logRequest(ServerHttpRequest request) {
        log.info("""
                        Request Received From {}
                         Uri : {}
                         Method : {}
                         Headers : {}
                         Path : {}
                         Query Params : {}
                         Content type : {}
                         Acceptable Media Type {}
                        """,
                request.getLocalAddress(),
                request.getURI(),
                request.getMethod(),
                request.getHeaders(),
                request.getPath(),
                request.getQueryParams(),
                request.getHeaders().getContentType(),
                request.getHeaders().getAccept());
    }

    private void logResponse(ServerWebExchange serverWebExchange) {
        serverWebExchange.getResponse().beforeCommit(() -> {
            log.info("""
                            Response Sending To {}
                             Uri : {}
                             Path : {}
                             Headers : {}
                             Response Status : {}
                             Content type : {}
                            """,
                    serverWebExchange.getRequest().getLocalAddress(),
                    serverWebExchange.getRequest().getURI(),
                    serverWebExchange.getRequest().getPath(),
                    serverWebExchange.getResponse().getHeaders(),
                    serverWebExchange.getResponse().getStatusCode(),
                    serverWebExchange.getResponse().getHeaders().getContentType()
            );
            log.info("Response processing time for {} is {} ms", serverWebExchange.getRequest().getPath(),
                    Objects.requireNonNull(serverWebExchange.getResponse().getHeaders().
                            get(HeaderNames.RESPONSE_PROCESSING_TIME_IN_MS.getValue())).stream().findFirst().orElse("0"));
            return Mono.empty();
        });
    }


    private void setResponseHeader(ServerWebExchange serverWebExchange) {
        serverWebExchange.getResponse().beforeCommit(() -> {
            Objects.requireNonNull(serverWebExchange.getRequest().getHeaders().get(HeaderNames.REQUEST_RECEIVED_TIME_IN_MS.getValue()))
                    .stream()
                    .findFirst()
                    .ifPresent(s -> {
                        // Adjust received time if it has less than 3 fractional digits
//                        String adjustedReceivedTime = adjustFractionalSeconds(s);
                        LocalDateTime receivedTime = LocalDateTime.parse(s, formater);

                        long processingTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                - receivedTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        serverWebExchange.getResponse().getHeaders().set(HeaderNames.RESPONSE_PROCESSING_TIME_IN_MS.getValue(), String.valueOf(processingTime));
                    });

            serverWebExchange.getResponse().getHeaders().set(HeaderNames.RESPONSE_SENT_TIME_IN_MS.getValue(), LocalDateTime.now().format(formater));
            serverWebExchange.getResponse().getHeaders().set(HeaderNames.TRACE_ID.getValue(),
                    Objects.requireNonNull(serverWebExchange.getRequest().getHeaders().get(HeaderNames.TRACE_ID.getValue()))
                            .stream()
                            .findAny()
                            .orElse(""));
            return Mono.empty();
        });
    }

    private void setRequestHeaders(ServerWebExchange serverWebExchange) {
        serverWebExchange.mutate().request(originalRequest -> originalRequest
                .headers(headers -> {
                    headers.set(HeaderNames.REQUEST_RECEIVED_TIME_IN_MS.getValue(), LocalDateTime.now().format(formater));
                    if (Objects.isNull(headers.get(HeaderNames.TRACE_ID.getValue()))
                            || Objects.requireNonNull(headers.get(HeaderNames.TRACE_ID.getValue()))
                            .stream()
                            .allMatch(String::isEmpty))
                        headers.set(HeaderNames.TRACE_ID.getValue(), tracerUtil.getCurrentTraceId());
                })).build();
    }

    private void setMdcAttributeForLogBack(ServerWebExchange serverWebExchange) {
        MDC.put("Method", Objects.requireNonNull(serverWebExchange.getRequest().getMethod()).name());
        MDC.put("Uri", serverWebExchange.getRequest().getPath().value());
        MDC.put("Trace-Id", tracerUtil.getCurrentTraceId());
        MDC.put("Request-Trace-Id", serverWebExchange.getRequest().getHeaders().get(HeaderNames.TRACE_ID.getValue()).stream().findFirst().orElse("DEFAULT"));
    }

}
