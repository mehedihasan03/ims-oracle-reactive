package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository.EmployeeRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository.StagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.EmployeePersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.Employee;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmployeePersistenceAdapter implements EmployeePersistencePort {
    private final ModelMapper mapper;
    private final StagingDataRepository repository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Mono<Employee> getEmployeeDetailByEmployeeId(String employeeId) {
        return repository
                .getEmployeeDetailByEmployeeId(employeeId)
                .map(stagingDataEntity -> {
                    Employee employee = mapper.map(stagingDataEntity, Employee.class);
                    employee.setEmpNameEn(stagingDataEntity.getFieldOfficerNameEn());
                    employee.setEmpNameBn(stagingDataEntity.getFieldOfficerNameBn());
                    return employee;
                })
                .doOnSuccess(success -> log.info("EmployeeEntity fetched from Db for stagingDataId - {}", employeeId))
                .doOnError(throwable -> log.error("Error while fetching EmployeeEntity from Db\nReason - {}", throwable.getMessage()))
                ;
    }

    @Override
    public Mono<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .map(employeeEntity -> mapper.map(employeeEntity, Employee.class))
                .doOnSuccess(success -> log.info("EmployeeEntity fetched from Db for employeeId - {}", employeeId))
                .doOnError(throwable -> log.error("Error while fetching EmployeeEntity from DB - {}", throwable.getMessage()));
    }
}
