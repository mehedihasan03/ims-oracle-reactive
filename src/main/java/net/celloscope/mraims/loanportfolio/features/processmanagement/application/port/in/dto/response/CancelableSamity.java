package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelableSamity {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String status;
    private String remarks;
    private String btnCancelEnabled;
    private String btnDeleteEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
