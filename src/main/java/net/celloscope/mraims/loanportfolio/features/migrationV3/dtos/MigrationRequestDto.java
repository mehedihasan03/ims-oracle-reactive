package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationRequestDto {
    private String officeId;
    private String loginId;
    private String mfiId;
    private String managementProcessId;
    private LocalDate businessDate;
    private MigrationConfigurationRequestDto configurations;
    private List<MigrationMemberRequestDto> members;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
