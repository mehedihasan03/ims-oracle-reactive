package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportResponseDTO {
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private Integer totalCount;
    private List<Transaction> data;
}
