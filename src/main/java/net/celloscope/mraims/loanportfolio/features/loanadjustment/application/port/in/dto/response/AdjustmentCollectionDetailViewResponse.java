package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MemberInfoDTO;

import java.util.List;

@Setter
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentCollectionDetailViewResponse {

    private String userMessage;
    private AdjustmentDetailViewResponse data;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
