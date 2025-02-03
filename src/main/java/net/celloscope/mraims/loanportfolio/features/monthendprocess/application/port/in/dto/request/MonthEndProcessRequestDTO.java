package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessRequestDTO {

    private String officeId;
    private String loginId;
    private Integer month;
    private Integer year;
    private Integer limit;
    private Integer offset;
    private List<String> samityIdList;
    private List<String> transactionCodeList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
