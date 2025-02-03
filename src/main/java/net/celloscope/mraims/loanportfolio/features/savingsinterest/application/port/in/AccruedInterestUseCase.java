package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface AccruedInterestUseCase {
    Mono<SavingsAccruedInterestResponseDTO> saveAccruedInterest(AccruedInterestCommand command);
    Flux<SavingsAccruedInterestResponseDTO> getAccruedInterestEntriesBySavingsAccountIdYearAndClosingType(String savingsAccountId, Integer year, String closingType);
    Mono<Boolean> updateTransactionIdAndStatusByAccruedInterestIdList(List<String> accruedInterestIdList, String transactionId, String status);
    Mono<SavingsAccruedInterestResponseDTO> saveFDRAccruedInterest(AccruedInterestCommand command);
    Mono<String> calculateMonthlySavingsInterestAndAccrue(String officeId, Integer interestCalculationMonth, Integer interestCalculationYear, String loginId);

    Mono<AccruedInterestResponseDTO> getAccruedInterestEntriesByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

    Mono<AccruedInterestDTO> accrueAndSaveMonthlyInterest(List<String> savingsAccountIdList, Integer month, Integer year, String loginId, String managementProcessId, String processId, String samityId, String officeId, LocalDate businessDate);
    Mono<List<AccruedInterestDTO>> calculateMonthlyAccruedInterest(String savingsAccountId, Integer month, Integer year, LocalDate businessDate);


}
