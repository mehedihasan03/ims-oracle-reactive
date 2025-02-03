package net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in;

import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanAccountUseCase {

    Flux<StagingAccountData> getLoanAccountListForStagingAccountData(String memberId, String status, String loanType);
    Mono<LoanAccountResponseDTO> getLoanAccountDetailsByLoanAccountId(String loanAccountId);
    Mono<String> getProductIdByLoanAccountId(String loanAccountId);
    Mono<LoanAccountResponseDTO> updateLoanAccountStatusForPaidOff(String loanAccountId, String managementProcessId, String status);
    Mono<LoanAccountResponseDTO> updateLoanAccountStatus(String loanAccountId, String status);
    Mono<LoanAccountResponseDTO> getLoanAccountById(String oid);
    Flux<LoanAccount> getAllLoanAccountsByMemberIdListAndStatus(List<String> memberId, String status);
    Flux<LoanAccount> getAllLoanAccountsByMemberIdAndStatus(String memberId, String status);
}
