package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAdjustmentSamityGridViewResponseDTO {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private Integer totalMember;
    private String status;
    private String remarks;
    private BigDecimal totalLoanAdjustment;
    private String btnOpenEnabled;
    private String btnViewEnabled;
    private String btnUpdateEnabled;
    private String btnEditEnabled;
    private String btnSubmitEnabled;
    private List<LoanAdjustmentMemberGridViewResponseDTO> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
