package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverGridViewRequestDTO extends GenericLoanWaiverRequestDTO{

    private String samityId;
    private String status;
    private Integer limit;
    private Integer offset;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
