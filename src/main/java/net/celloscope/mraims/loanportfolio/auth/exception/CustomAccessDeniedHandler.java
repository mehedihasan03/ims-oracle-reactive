//package net.celloscope.mraims.loanportfolio.auth.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {
//
//    @Override
//    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException deniedException) {
//        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> errorDetails = new HashMap<>();
//        errorDetails.put("code", 403);
//        errorDetails.put("message", "Forbidden: You do not have permission to access this resource");
//        errorDetails.put("error", deniedException.getMessage());
//
//        try {
//            String json = new ObjectMapper().writeValueAsString(errorDetails);
//            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
//                    .bufferFactory().wrap(json.getBytes())));
//        } catch (Exception e) {
//            log.error("Error writing JSON response", e);
//            return Mono.error(e);
//        }
//    }
//}
//
//
