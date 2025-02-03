package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.out;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterestDTODomain;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AccruedInterestPort {
    Mono<AccruedInterestDTODomain> saveAccruedInterest(SavingsAccruedInterestResponseDTO savingsAccruedInterestResponseDTO, AccruedInterestCommand command);

    Mono<String> saveAllSavingsAccountInterestDepositsIntoHistory(List<SavingsAccountInterestDeposit> savingsAccountInterestDepositList);

    Mono<Boolean> checkIfExistsByYearMonthAndSavingsAccountId(Integer year, Integer month, String savingsAccountId);

    Flux<SavingsAccruedInterestResponseDTO> getAccruedInterestEntriesBySavingsAccountIdYearAndMonthListAndStatus(String savingsAccountId, Integer year, List<String> monthList, String status);

    Mono<Boolean> updateTransactionIdAndStatusByAccruedInterestIdList(List<String> accruedInterestIdList, String transactionId, String status);

    Mono<SavingsAccountInterestDeposit> saveAccruedInterestV2(SavingsAccountInterestDeposit savingsAccountInterestDeposit);

    Flux<AccruedInterest> getAccruedInterestEntriesByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

    Mono<Integer> deleteIfAlreadyAccrued(String managementProcessId, String samityId);

    Mono<String> deleteAllSavingsAccountInterestDepositByManagementProcessId(String managementProcessId);

    Flux<SavingsAccountInterestDeposit>findAllSavingsAccountInterestDepositsForManagementProcessId(String managementProcessId);
}
