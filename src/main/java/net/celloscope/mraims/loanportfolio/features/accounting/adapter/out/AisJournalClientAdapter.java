package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.AisAPI;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.dto.JournalSnapshotCommand;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisJournalClientPort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class AisJournalClientAdapter implements AisJournalClientPort {
    private final AisAPI aisAPI;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;

    public AisJournalClientAdapter(AisAPI aisAPI, ManagementProcessTrackerUseCase managementProcessTrackerUseCase) {
        this.aisAPI = aisAPI;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
    }

    @Override
    public Mono<AisResponse> postAccounting(JournalRequestDTO journalRequestDTO) {
        return aisAPI
                .postAccountingToIms(journalRequestDTO)
                .doOnRequest(l -> log.info("request received to post accounting : {}", journalRequestDTO))
                .doOnSuccess(aisResponse -> log.debug("Successfully received response : {}", aisResponse));
    }

    @Override
    public Mono<String> createJournalSnapshot(JournalSnapshotCommand journalSnapshotCommand) {
        return aisAPI.createLedgerSnapshot(journalSnapshotCommand)
                .doOnRequest(l -> log.info("Requesting to create Journal Snapshot"))
                .doOnSuccess(s -> log.info("Successfully created Journal Snapshot"))
                .doOnError(throwable -> log.error("Failed to create Journal Snapshot : {}", throwable.getMessage()));
    }


}
