package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.DPSRepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.DPSRepaymentCommand;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface DpsRepaymentScheduleUseCase {
    Mono<DpsRepaymentScheduleResponseDTO> generateDpsRepaymentSchedule(DPSRepaymentCommand command);
    Mono<List<DpsRepaymentDTO>> getDpsRepaymentScheduleBySavingsAccountId(String savingsAccountId);
    Mono<Boolean> updateDPSRepaymentScheduleStatus(String savingsAccountId, String status, List<Integer> paidRepaymentNos, String managementProcessId, LocalDate businessDate, String loginId);
    Mono<Integer> getCountOfPendingRepaymentScheduleBySavingsAccountId(String savingsAccountId);
    Mono<Boolean> updateDPSRepaymentScheduleStatusByManagementProcessId(String managementProcessId, String status, String loginId);
}
