package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookReportQueryDTO extends BaseToString {
    private String samityId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String accountNo;
    private String searchText;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
