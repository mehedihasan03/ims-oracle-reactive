package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawPaymentRequestDTO {

    private String loginId;
    private String officeId;
    private String samityId;
    private String withdrawType;
    private List<WithdrawPaymentDataObject> data;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
