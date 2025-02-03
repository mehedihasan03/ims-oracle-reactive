package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class DataArchiveResponseDto extends BaseToString {
    private String userMessage;
}
