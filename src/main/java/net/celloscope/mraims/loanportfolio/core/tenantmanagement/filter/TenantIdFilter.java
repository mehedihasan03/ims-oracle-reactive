package net.celloscope.mraims.loanportfolio.core.tenantmanagement.filter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.TenantIdEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TenantIdFilter implements WebFilter {

    @Value("${spring.r2dbc.default.schema}")
    private String defaultSchema;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var value = exchange.getRequest().getQueryParams().getFirst(TenantIdEnum.TENANT_ID.getValue());
        if (StringUtils.hasText(value)) {
            log.info("value from query param: " + value);
        } else {
            value = "MRA-IMS-MFI-Oid-MRA";
        }
        return chain.filter(exchange)
            .contextWrite(CurrentTenantIdHolder.withId(value));
    }
}
