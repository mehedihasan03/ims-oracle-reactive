package net.celloscope.mraims.loanportfolio.features.schedulers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GarbageCollectorScheduler {

    @Value("${garbage-collector-scheduler.enabled}")
    private String isClearMemorySchedulerEnabled;

    @Scheduled(fixedRate = 3600000)
    public void clearMemory() {
        if (!Boolean.parseBoolean(isClearMemorySchedulerEnabled)) {
            return;
        }
        System.gc();
        log.info("System.gc() was called to suggest JVM to run garbage collector");
    }

}
