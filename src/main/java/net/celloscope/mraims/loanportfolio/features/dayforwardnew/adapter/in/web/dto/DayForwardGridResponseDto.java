package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.domain.DayForwardProcessTracker;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayForwardGridResponseDto {
    private String btnRunForwardDayEnabled;
    private String btnConfirmEnabled;
    private String btnRefreshEnabled;
    private String btnRetryEnabled;
    private String btnRevertEnabled;
    private String mfiId;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;

    private List<DayForwardProcessTracker> data;
    private long totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
