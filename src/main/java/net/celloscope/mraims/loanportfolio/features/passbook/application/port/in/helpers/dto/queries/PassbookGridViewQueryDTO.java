package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookGridViewQueryDTO extends BaseToString {
    private String officeId;
    private String fieldOfficerId;
    private String samityId;
    private LocalDate fromDate;
    private LocalDate toDate;
}
