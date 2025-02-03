package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionCollectionCommand {

    private String loginId;
    private String samityId;
    private String remarks;
}
