package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundGridViewResponseDto {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanAccountData> data;
    private int totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
