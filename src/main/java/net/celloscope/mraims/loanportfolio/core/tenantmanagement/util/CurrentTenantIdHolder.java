package net.celloscope.mraims.loanportfolio.core.tenantmanagement.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

@Slf4j
public class CurrentTenantIdHolder {

    public static final String INSTITUTE_OID = CurrentTenantIdHolder.class.getName() + ".INSTITUTE_OID";

    public static Context withId(String id) {
        return Context.of(INSTITUTE_OID, id);
    }

    public static Mono<String> getId() {
        return Mono.deferContextual(contextView -> {
            if (contextView.hasKey(INSTITUTE_OID)) {
                String tenantId = contextView.get(INSTITUTE_OID);
                log.debug("Retrieved tenant ID from context: {}", tenantId);
                return Mono.just(tenantId);
            }
            return Mono.empty();
        });
    }

    public static Mono<Context> buildContext(String id) {
        return Mono.just(id).map(CurrentTenantIdHolder::withId);
    }

    public static Function<Context, Context> clearContext() {
        return (context) -> context.delete(INSTITUTE_OID);
    }
}
