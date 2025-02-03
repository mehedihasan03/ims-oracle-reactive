package net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto;

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
public class SamityValidationDTO {

    private String samityId;
    private String fieldOfficerId;
    private String isDownloaded;
    private String isUploaded;
    private String status;
    private List<String> samityEvents;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
