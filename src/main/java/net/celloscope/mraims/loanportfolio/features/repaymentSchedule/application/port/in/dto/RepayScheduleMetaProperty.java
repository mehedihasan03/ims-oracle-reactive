package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepayScheduleMetaProperty {

    private String name;
    private String rescheduleOnSamityCancel;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
