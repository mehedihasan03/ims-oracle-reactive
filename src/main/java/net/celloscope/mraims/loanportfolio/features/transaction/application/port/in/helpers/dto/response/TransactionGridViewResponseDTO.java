package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.queries.helpers.dto.TransactionGridViewDataDTO;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionGridViewResponseDTO extends BaseToString {
    private String officeId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private List<TransactionGridViewDataDTO> data;
}

