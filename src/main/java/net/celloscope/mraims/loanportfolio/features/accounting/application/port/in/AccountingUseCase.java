package net.celloscope.mraims.loanportfolio.features.accounting.application.port.in;

import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.AutoVoucherJournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AccountingUseCase {
    Mono<AisResponse> getAccountingJournalBody(AccountingRequestDTO requestDTO);
    Mono<JournalRequestDTO> getAccountingJournalRequestBody(AccountingRequestDTO requestDTO);
    Mono<AisResponse> saveAccountingJournal(JournalRequestDTO journalRequestDTO);
    Mono<List<AisResponse>> buildAndSaveAccountingJournalFromAutoVoucherList(AutoVoucherJournalRequestDTO autoVoucherJournalRequestDTO);
}
