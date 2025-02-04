package net.celloscope.mraims.loanportfolio.auth;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.UNAUTHORIZED, "Access Denied : Missing Authorization token in Request Headers"));
        }

        String token = authHeader.substring(7);
        Authentication authToken = new UsernamePasswordAuthenticationToken(token, token);

        return authenticationManager.authenticate(authToken)
                .map(SecurityContextImpl::new);
    }
}

