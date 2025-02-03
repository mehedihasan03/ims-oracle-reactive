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
public class StagingDataStatusByFieldOfficerResponseDTO {

    private String officeId;
    private String OfficeNameEn;
    private String OfficeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private String isDownloaded;
    private String btnDownloadEnabled;
    private String btnDeleteEnabled;
    private String status;
    private String userMessage;
    private List<StagingDataSamityStatusForFieldOfficerDTO> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
