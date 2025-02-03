package net.celloscope.mraims.loanportfolio.features.serviceCharge.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.adapter.out.persistence.database.repository.ServiceChargeChartRepository;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.CombinedDTO;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.out.ServiceChargeChartPort;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.domain.ServiceChargeChart;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Component
@Slf4j
public class ServiceChargeAdapter implements ServiceChargeChartPort {
    private final ServiceChargeChartRepository serviceChargeChartRepository;
    private final ModelMapper modelMapper;

    public ServiceChargeAdapter(ServiceChargeChartRepository serviceChargeChartRepository, ModelMapper modelMapper) {
        this.serviceChargeChartRepository = serviceChargeChartRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<ServiceChargeChart> getServiceChargeChartByLoanProductId(String loanProductId) {
        return serviceChargeChartRepository
                .findByLoanProductId(loanProductId)
                .map(serviceChargeChartEntity -> modelMapper.map(serviceChargeChartEntity, ServiceChargeChart.class));
    }

    @Override
    public Mono<CombinedDTO> getCombinedDto(String loanAccountId) {
        return serviceChargeChartRepository
                .getCombinedDTO(loanAccountId);
    }
}
