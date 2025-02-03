package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthorizeCollectionCommand {
    public String instituteOid;
    private String officeId;
    public String fieldOfficerId;
    public String mfiId;
    public String loginId;
    public String samityId;
    public String collectionType;
}

