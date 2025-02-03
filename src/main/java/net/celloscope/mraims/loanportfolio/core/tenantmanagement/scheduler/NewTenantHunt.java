package net.celloscope.mraims.loanportfolio.core.tenantmanagement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.config.TenantConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewTenantHunt {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final TenantConfig tenantConfig;

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        if (tenantConfig.newInstituteFound()) {
            log.info("new MFI found! {}", dateFormat.format(new Date()));
            tenantConfig.updateConnectionFactories();
        }
    }
}
