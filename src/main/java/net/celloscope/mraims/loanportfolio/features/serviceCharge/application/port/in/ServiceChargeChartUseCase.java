package net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in;

import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.ServiceChargeChartResponseDTO;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.domain.ServiceChargeChart;
import reactor.core.publisher.Mono;

public interface ServiceChargeChartUseCase {
    Mono<ServiceChargeChartResponseDTO> getServiceChargeDetailsByLoanAccountId(String loanAccountId);
    Mono<ServiceChargeChart> getServiceChargeDetailsByLoanProductId(String loanProductId);
}
