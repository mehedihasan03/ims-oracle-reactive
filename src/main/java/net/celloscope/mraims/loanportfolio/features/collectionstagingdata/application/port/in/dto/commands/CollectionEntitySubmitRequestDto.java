package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionEntitySubmitRequestDto {

    private List<String> id;
    public String mfiId;
    public String loginId;
    public String officeId;
    public String userRole;
    public String instituteOid;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
