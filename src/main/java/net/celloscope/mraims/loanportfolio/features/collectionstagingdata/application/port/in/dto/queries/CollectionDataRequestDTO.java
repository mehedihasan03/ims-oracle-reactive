package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDataRequestDTO {
    private String officeId;
    private String fieldOfficerId;
    private String mfiId;
    private String loginId;
    private String samityId;
    private String accountId;
    private String memberId;
    private Integer limit;
    private Integer offset;
    private String oid;
}
