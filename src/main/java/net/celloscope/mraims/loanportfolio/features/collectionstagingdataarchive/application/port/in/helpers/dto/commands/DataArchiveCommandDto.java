package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataArchiveCommandDto extends BaseToString {
    private String officeId;
    private String mfiId;
}
