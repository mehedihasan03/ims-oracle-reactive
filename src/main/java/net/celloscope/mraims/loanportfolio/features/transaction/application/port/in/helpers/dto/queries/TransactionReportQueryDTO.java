package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportQueryDTO extends BaseToString {
    private String samityId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String accountNo;
    private String searchText;
}
