package net.celloscope.mraims.loanportfolio.features.accounting.domain.commands;

import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class AccountingCommands implements IAccountingCommands{
    @Override
    public Mono<Journal> buildJournal(String description, BigDecimal debitedAmount, BigDecimal creditedAmount, String ledgerId, String subLedgerId) {
        return Mono.just(
                Journal
                    .builder()
                    .description(description)
                    .debitedAmount(debitedAmount)
                    .creditedAmount(creditedAmount)
                    .ledgerId(ledgerId)
                    .subledgerId(subLedgerId.isEmpty() ? null : subLedgerId)
                    .build());
    }

    @Override
    public Mono<JournalRequestDTO> buildJournalResponseBody(AccountingRequestDTO requestDTO, List<Journal> journalList, LocalDate businessDate) {
        return Mono.just(
                JournalRequestDTO
                        .builder()
                        .journalType(requestDTO.getProcessName())
                        .description(requestDTO.getProcessName() + ", Date: " + businessDate)
                        .amount(BigDecimal.ZERO)
                        .referenceNo(businessDate.toString())
                        .journalProcess("Auto")
                        .officeId(requestDTO.getOfficeId())
                        .mfiId(requestDTO.getMfiId())
                        .createdBy(requestDTO.getLoginId())
                        .journalList(journalList)
                        .build()
        );
    }
}
