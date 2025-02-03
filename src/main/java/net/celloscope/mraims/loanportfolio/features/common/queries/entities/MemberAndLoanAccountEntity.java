package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAndLoanAccountEntity {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String loanAccountId;
    private String loanAccountOid;
    private BigDecimal loanAmount;
}
