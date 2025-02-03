package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataStatusByOfficeResponseDTO {

    private String btnStagingDataGenerateEnabled;
    private String btnStartProcessEnabled;
    private String btnRefreshEnabled;
    private String btnDeleteEnabled;
    private String mfiId;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String status;
    private String userMessage;
    private List<StagingDataSamityStatusForOfficeDTO> data;
    private Integer totalCount;

    @Override
    public String toString(){
       return CommonFunctions.buildGsonBuilder(this);
    }
}
