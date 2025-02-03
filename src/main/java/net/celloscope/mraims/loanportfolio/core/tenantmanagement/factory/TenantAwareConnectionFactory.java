package net.celloscope.mraims.loanportfolio.core.tenantmanagement.factory;

import io.r2dbc.pool.ConnectionPool;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TenantAwareConnectionFactory extends AbstractRoutingConnectionFactory {

    private final Map<Object, ConnectionPool> connectionFactories = new ConcurrentHashMap<>();

    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return CurrentTenantIdHolder.getId()
            .switchIfEmpty(Mono.just("MRA-IMS-MFI-Oid-MRA"))
            .doOnNext(id -> log.debug("Current lookup key (tenant ID): {}", id))
            .doOnNext(id -> {
                ConnectionPool connectionPool = connectionFactories.get(id);
                if (connectionPool != null) {
                    log.debug("Using connection factory for tenant ID: {}", id);
                } else {
                    log.warn("No connection factory found for tenant ID: {}", id);
                }
            })
            .cast(Object.class);
    }

//    @Override
    public void setTargetConnectionFactories(Map<?, ?> targetConnectionFactories) {
        super.setTargetConnectionFactories(targetConnectionFactories);
        targetConnectionFactories.forEach((key, value) -> {
            if (value instanceof ConnectionPool) {
                connectionFactories.put(key, (ConnectionPool) value);
            }
        });
    }

    public void updateTargetConnectionFactories(Map<String, ConnectionPool> updatedConnectionFactories) {
        setTargetConnectionFactories(updatedConnectionFactories);
        afterPropertiesSet();
    }
}
