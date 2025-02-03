package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverDetailViewRequestDTO extends GenericLoanWaiverRequestDTO {
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String id;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
