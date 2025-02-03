package net.celloscope.mraims.loanportfolio.features.accounting.domain.commands;

import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IAccountingCommands {
    Mono<Journal> buildJournal(String description, BigDecimal debitedAmount, BigDecimal creditedAmount, String ledgerId, String subLedgerId);
    Mono<JournalRequestDTO> buildJournalResponseBody(AccountingRequestDTO requestDTO, List<Journal> journalList, LocalDate businessDate);
}
