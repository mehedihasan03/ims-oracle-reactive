package net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ILoanAccountPersistencePort {

    Flux<StagingAccountData> getLoanAccountListForStagingAccountData(String memberId, String status, String loanType);
    Mono<LoanAccount> getLoanAccountDetailsByLoanAccountId(String loanAccountId);
    Mono<LoanAccount> updateLoanAccount(String loanAccountId, LocalDate disbursementDate, BigDecimal serviceChargeRatePerPeriod, BigDecimal annualServiceChargeRate, LocalDate expectedEndDate);
    Mono<String> getProductIdByLoanAccountId(String loanAccountId);

    Mono<LoanAccount> updateLoanAccountStatus(String loanAccountId, String status);

    Mono<LoanAccount> updateLoanAccountStatusForPaidOff(String loanAccountId, String status, LocalDate businessDate);

    Mono<LoanAccountResponseDTO> getLoanAccountById(String oid);
    Flux<LoanAccount> getAllLoanAccountsByMemberIdAndStatus(List<String> memberId, String status);
    Flux<LoanAccount> getAllLoanAccountsForAMemberByStatus(String memberId, String status);
}
