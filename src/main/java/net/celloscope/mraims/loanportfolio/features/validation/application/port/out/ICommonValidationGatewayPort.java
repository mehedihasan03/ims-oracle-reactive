package net.celloscope.mraims.loanportfolio.features.validation.application.port.out;

import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.OfficeValidationDTO;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.SamityValidationDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICommonValidationGatewayPort {
    Mono<OfficeValidationDTO> getAndBuildOfficeValidationDTO(String officeId);

    Mono<List<String>> getStagingProcessTrackerSamityListForOffice(String managementProcessId, String officeId);

    Mono<List<SamityValidationDTO>> getAndBuildSamityValidationDTOList(String managementProcessId, List<String> samityIdList);
}
