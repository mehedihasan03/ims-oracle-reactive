package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationTabViewSamityDTO {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String totalMember;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private BigDecimal totalAmount;

    private String status;
    private String remarks;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
