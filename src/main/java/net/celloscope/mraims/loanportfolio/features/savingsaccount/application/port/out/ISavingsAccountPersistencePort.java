package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.out;

import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountDto;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ISavingsAccountPersistencePort {

    Flux<SavingsAccountResponseDTO> getSavingsAccountFluxByMemberId(String memberId);

    Mono<SavingsAccountResponseDTO> getSavingsAccountBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, LocalDate transactionDate, String loginId);

    Mono<String> getProductIdBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountStatus(String savingsAccountId, String status, LocalDate activationDate, LocalDate closingDate);

    Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountOnMaturity(String savingsAccountId);

    Flux<SavingsAccountResponseDTO> getAllFDRAccountsEligibleForInterestPosting(LocalDate lastBusinessDate, LocalDate currentBusinessDate, String savingsTypeId);

    Flux<SavingsAccountResponseDTO> getSavingAccountByMemberIdList(List<String> memberIdList);

    Flux<SavingsAccountResponseDTO> getSavingsAccountsByOfficeIdAndStatus(String officeId, String value);

    Flux<FDRAccountDTO> getFDRSavingsAccountsByOfficeIdAndStatus(String officeId, List<String> statusList);

    Mono<FDRAccountDTO> getFDRAccountDetailsBySavingsAccountId(String savingsAccountId);

    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeId(String officeId, List<String> statusList);

    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndSearchText(String officeId, String searchText, List<String> statusList);

    Mono<DPSAccountDTO> getDPSAccountDetailsBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountDto> getSavingsAccountInfoBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, String loginId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatusForSavingsClosure(String savingsAccountId, LocalDate closingDate, String status, String loginId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountInterestPostingDates(List<LocalDate> interestPostingDates, String savingsAccountId, LocalDate acctStartDate, LocalDate acctEndDate, String loginId);

    Mono<SavingsAccountResponseDTO> updateFDRDPSAccountMaturityAmount(String savingsAccountId, BigDecimal maturityAmount);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountBalance(String savingsAccountId, BigDecimal balance, String status);
}
