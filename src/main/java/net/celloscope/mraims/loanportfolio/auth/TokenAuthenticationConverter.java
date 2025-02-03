//package net.celloscope.mraims.loanportfolio.auth;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//@RequiredArgsConstructor
//public class TokenAuthenticationConverter implements ServerAuthenticationConverter {
//
//    private final TokenValidationService tokenValidationService;
//
//    @Override
//    public Mono<Authentication> convert(ServerWebExchange exchange) {
//        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
//                .filter(authHeader -> authHeader.startsWith("Bearer "))
//                .map(authHeader -> authHeader.substring(7)) // Extract token
//                .flatMap(token -> Mono.fromCallable(() -> tokenValidationService.validateTokenAndExtractCredentialsByApiUrl(token)))
//                .onErrorResume(e -> Mono.empty())
//                .map(authUser -> new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities()));
//    }
//}
//
