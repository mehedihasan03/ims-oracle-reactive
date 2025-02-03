package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.MigrationRepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.util.List;

public interface MigrationRepaymentScheduleUseCase {
    Mono<Tuple2<List<RepaymentScheduleViewDTO>, BigDecimal>> viewRepaymentScheduleFlat(MigrationRepaymentScheduleCommand command);
    Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlat(MigrationRepaymentScheduleCommand command);

    Mono<Tuple2<List<RepaymentScheduleViewDTO>, BigDecimal>> viewRepaymentScheduleFlatV2(MigrationRepaymentScheduleCommand command);
    Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlatV2(MigrationRepaymentScheduleCommand command);

    Mono<List<RepaymentSchedule>> viewRepaymentScheduleFlatInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command);
    Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleFlatInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command);
    Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleFlatInstallmentAmountProvidedForMigrationV2(RepaymentScheduleCommand command);
    Mono<List<RepaymentScheduleResponseDTO>> generateRepaymentScheduleDecliningInstallmentAmountProvidedForMigration(RepaymentScheduleCommand command);
}
