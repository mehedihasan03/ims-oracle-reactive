package net.celloscope.mraims.loanportfolio.features.migration.dtos;


import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import reactor.core.publisher.Flux;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigrationResponseDto {

    private String status;
    private String mfiId;
    private String loginId;
    private String migrationProcessId;
    private List<MigratedComponentsResponseDto> migratedComponents;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
