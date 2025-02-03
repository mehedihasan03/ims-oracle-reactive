package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.CalculateInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.PostSavingsInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SavingsInterestUseCase {
    Mono<SavingsInterestResponseDTO> calculateDailyAccruedInterest(CalculateInterestCommand command);

    Mono<SavingsAccruedInterestResponseDTO> calculateMonthlyAccruedInterest(CalculateInterestCommand command);

    Mono<BigDecimal> calculateInterestBetweenDates(LocalDate fromDate, LocalDate toDate, String savingsAccountId, LocalDate businessDate);

    Mono<BigDecimal> calculateDPSMaturityAmountWithoutCompounding(String savingsAccountId);
    Mono<List<SavingsAccountInterestDeposit>> getAllSavingsAccountInterestDepositsForManagementProcessId(String managementProcessId);

    Mono<String> postSavingsInterest(PostSavingsInterestCommand command);
}
