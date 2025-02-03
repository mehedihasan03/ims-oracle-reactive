package net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.out;

import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.CombinedDTO;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.domain.ServiceChargeChart;
import reactor.core.publisher.Mono;

public interface ServiceChargeChartPort {
    Mono<ServiceChargeChart> getServiceChargeChartByLoanProductId(String loanProductId);
    Mono<CombinedDTO> getCombinedDto(String loanAccountId);

}
