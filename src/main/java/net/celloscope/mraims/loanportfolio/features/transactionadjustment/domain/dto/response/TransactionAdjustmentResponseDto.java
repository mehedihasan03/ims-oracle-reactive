package net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAdjustmentResponseDto {
    private String userMessage;
    private TransactionAdjustment data;
}
