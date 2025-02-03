package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStagingRequestDto {

    private String id;
    private BigDecimal amount;
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;
    private String loanAccountId;
    private String fieldOfficerId;
    private String transactionDate;
    private Integer limit;
    private Integer offset;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String status;
    private String searchText;

    private String memberId;
    private String samityId;
    private String collectionType;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
