package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookReportResponseDTO {
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private Integer totalCount;
    private List<Passbook> data;
}
