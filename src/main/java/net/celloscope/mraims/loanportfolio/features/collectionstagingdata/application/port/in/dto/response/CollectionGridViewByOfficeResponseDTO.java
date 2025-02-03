package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionGridViewByOfficeResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;

    private List<GridViewDataObject> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
