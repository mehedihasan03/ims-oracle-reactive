package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private List<Transaction> transactionList;
    private Integer transactionCount;
}
