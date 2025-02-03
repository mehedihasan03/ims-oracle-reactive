package net.celloscope.mraims.loanportfolio.features.accounting.application.port.out;

import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.dto.JournalSnapshotCommand;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import reactor.core.publisher.Mono;

public interface AisJournalClientPort {
    Mono<AisResponse> postAccounting(JournalRequestDTO journalRequestDTO);
    Mono<String> createJournalSnapshot(JournalSnapshotCommand journalSnapshotCommand);
}
