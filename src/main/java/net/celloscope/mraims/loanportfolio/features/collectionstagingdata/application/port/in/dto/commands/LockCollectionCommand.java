package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockCollectionCommand {
    private String instituteOid;
    private String mfiId;
    private String loginId;
    private String officeId;
    private String fieldOfficerId;
    private String samityId;
}
