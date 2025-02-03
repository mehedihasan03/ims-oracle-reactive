package net.celloscope.mraims.loanportfolio.features.serviceCharge.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.ServiceChargeChartResponseDTO;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.out.ServiceChargeChartPort;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.domain.ServiceChargeChart;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
@Slf4j
public class SericeChargeChartService implements ServiceChargeChartUseCase {

    private final ServiceChargeChartPort serviceChargeChartPort;
    private final ModelMapper modelMapper;

    public SericeChargeChartService(ServiceChargeChartPort serviceChargeChartPort, ModelMapper modelMapper) {
        this.serviceChargeChartPort = serviceChargeChartPort;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<ServiceChargeChartResponseDTO> getServiceChargeDetailsByLoanAccountId(String loanAccountId) {
        return serviceChargeChartPort
                .getCombinedDto(loanAccountId)
                .map(serviceChargeChart -> modelMapper.map(serviceChargeChart, ServiceChargeChartResponseDTO.class));
    }

    @Override
    public Mono<ServiceChargeChart> getServiceChargeDetailsByLoanProductId(String loanProductId) {
        return serviceChargeChartPort.getServiceChargeChartByLoanProductId(loanProductId);
    }
}
