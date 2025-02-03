package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.queries.helpers.dto.PassbookGridViewDataDTO;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookGridViewResponseDTO extends BaseToString {
    private String officeId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private List<PassbookGridViewDataDTO> data;
}