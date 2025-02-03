package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataDetailViewResponseDTO extends BaseToString {

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

    private List<LoanAccountSummeryDTO> loanAccountSummery;
    private BigDecimal loanAccountTotalDue;

    private List<SavingsAccountSummeryDTO> savingsAccountSummery;
    private BigDecimal savingsAccountTotalTarget;
}
