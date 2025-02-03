package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in;

import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ISavingsAccountUseCase {

    Flux<SavingsAccountResponseDTO> getSavingsAccountFluxByMemberId(String memberId);
    Mono<SavingsAccountResponseDTO> getSavingsAccountDetailsBySavingsAccountId(String savingsAccountId);
    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, LocalDate transactionDate, String loginId);
    Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountStatus(String savingsAccountId, String status, LocalDate activationDate, LocalDate closingDate);
    Mono<String> getProductIdBySavingsAccountId(String savingsAccountId);
    Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountOnMaturity(String savingsAccountId);

    Flux<SavingsAccountResponseDTO> getAllFDRAccountsEligibleForInterestPosting(LocalDate lastBusinessDate, LocalDate currentBusinessDate);

    Flux<SavingsAccountResponseDTO> getSavingAccountForStagingAccountDataByMemberIdList(List<String> memberIdList);

    Flux<SavingsAccountResponseDTO> getSavingsAccountsByOfficeId(String officeId);

    Flux<FDRAccountDTO> getFDRSavingsAccountsByOfficeId(String officeId);
    Mono<FDRAccountDTO> getFDRAccountDetailsBySavingsAccountId(String savingsAccountId);

    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeId(String officeId);

    Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndSearchText(String officeId, String searchText);

    Mono<DPSAccountDTO> getDPSAccountDetailsBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountDto> getSavingsAccountInfoBySavingsAccountId(String savingsAccountId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, String loginId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountStatusForSavingsClosure(String savingsAccountId, LocalDate closeDate, String status, String loginId);

    Mono<SavingsAccountResponseDTO> updateSavingsAccountInterestPostingDatesAndStartDateEndDate(List<LocalDate> interestPostingDates, String savingsAccountId, LocalDate acctStartDate, LocalDate acctEndDate, String loginId);

    Mono<SavingsAccountResponseDTO> updateFDRDPSAccountMaturityAmount(String savingsAccountId, BigDecimal maturityAmount);
    Mono<SavingsAccountResponseDTO> updateSavingsAccountBalance(String savingsAccountId, BigDecimal balance, String status);

    Mono<SavingsAccountActivationResponseDto> activateSavingsAccountWIthOpeningBalance(SavingsAccountActivationRequestDto requestDto);
}
