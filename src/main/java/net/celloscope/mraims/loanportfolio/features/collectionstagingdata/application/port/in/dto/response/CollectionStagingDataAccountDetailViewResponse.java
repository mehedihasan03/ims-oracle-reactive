package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStagingDataAccountDetailViewResponse {

    private String officeId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private Integer totalMember;
    private String mfiId;

    private BigDecimal totalLoanRecoverable;
    private BigDecimal totalLoanCollection;
    private BigDecimal serviceChargeRecoverable;
    private BigDecimal serviceChargeCollection;
    private BigDecimal totalRecoverable;
    private BigDecimal totalCollection;
    private List<MemberInfoDTO> memberList;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
