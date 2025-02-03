package net.celloscope.mraims.loanportfolio.features.loanaccount.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.out.ILoanAccountPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanAccountService implements LoanAccountUseCase {

    private final ILoanAccountPersistencePort port;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final ModelMapper mapper;

    @Override
    public Flux<StagingAccountData> getLoanAccountListForStagingAccountData(String memberId, String status, String loanType) {
        return port.getLoanAccountListForStagingAccountData(memberId, status, loanType);
    }

    @Override
    public Mono<LoanAccountResponseDTO> getLoanAccountDetailsByLoanAccountId(String loanAccountId) {
        return port.getLoanAccountDetailsByLoanAccountId(loanAccountId)
                .map(loanAccount -> mapper.map(loanAccount, LoanAccountResponseDTO.class));
    }

    @Override
    public Mono<String> getProductIdByLoanAccountId(String loanAccountId) {
        return port.getProductIdByLoanAccountId(loanAccountId);
    }

    @Override
    public Mono<LoanAccountResponseDTO> updateLoanAccountStatusForPaidOff(String loanAccountId, String managementProcessId, String status) {
        return managementProcessTrackerUseCase.getCurrentBusinessDateForManagementProcessId(managementProcessId)
                .flatMap(businessDate -> port.updateLoanAccountStatusForPaidOff(loanAccountId, status, businessDate))
                .doOnSuccess(loanAccount -> log.info("Loan account : {} status updated to : {} successfully", loanAccountId, status))
                .map(loanAccount -> mapper.map(loanAccount, LoanAccountResponseDTO.class));
    }

    @Override
    public Mono<LoanAccountResponseDTO> updateLoanAccountStatus(String loanAccountId, String status) {
        return port
                .updateLoanAccountStatus(loanAccountId, status)
                .doOnSuccess(loanAccount -> log.info("Loan account : {} status updated to : {} successfully", loanAccountId, status))
                .map(loanAccount -> mapper.map(loanAccount, LoanAccountResponseDTO.class));
    }

    @Override
    public Mono<LoanAccountResponseDTO> getLoanAccountById(String oid) {
        return port.getLoanAccountById(oid);
    }

    @Override
    public Flux<LoanAccount> getAllLoanAccountsByMemberIdListAndStatus(List<String> memberId, String status) {
        return port.getAllLoanAccountsByMemberIdAndStatus(memberId, status)
                .doOnRequest(loanAccount -> log.info("Requesting to retrieve loan accounts by member id list: {}", memberId))
                .doOnNext(loanAccount -> log.info("Loan account retrieved successfully: {}", loanAccount.getLoanAccountId()));
    }

    @Override
    public Flux<LoanAccount> getAllLoanAccountsByMemberIdAndStatus(String memberId, String status) {
        return port.getAllLoanAccountsForAMemberByStatus(memberId, status)
                .doOnRequest(loanAccount -> log.info("Requesting to retrieve loan accounts by member id: {}", memberId))
                .doOnNext(loanAccount -> log.info("Loan account retrieved successfully: {}", loanAccount.getLoanAccountId()));
    }
}
