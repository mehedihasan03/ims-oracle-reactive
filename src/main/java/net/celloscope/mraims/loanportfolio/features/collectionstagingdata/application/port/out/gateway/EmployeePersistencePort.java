package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.Employee;
import reactor.core.publisher.Mono;

public interface EmployeePersistencePort {
    Mono<Employee> getEmployeeDetailByEmployeeId(String employeeId);

    Mono<Employee> getEmployeeByEmployeeId(String employeeId);
}
