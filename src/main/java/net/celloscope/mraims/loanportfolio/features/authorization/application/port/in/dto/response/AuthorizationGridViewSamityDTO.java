package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationGridViewSamityDTO {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String totalMember;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private BigDecimal totalCollectionAmount;
    private BigDecimal totalWithdrawAmount;
    private BigDecimal totalLoanAdjustmentAmount;
    private BigDecimal totalLoanRebateAmount;
    private BigDecimal totalLoanWaivedAmount;
    private BigDecimal totalLoanWriteOffCollectionAmount;

    private String status;
    private String remarks;

    private String btnLockEnabled;

    private String lockedBy;
    private String authorizedBy;
    private String unauthorizedBy;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
