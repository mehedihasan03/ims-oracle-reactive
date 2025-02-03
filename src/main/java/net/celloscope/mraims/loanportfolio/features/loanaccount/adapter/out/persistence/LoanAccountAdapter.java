package net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.entity.LoanAccountEntity;
import net.celloscope.mraims.loanportfolio.features.loanaccount.adapter.out.persistence.database.repository.LoanAccountRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.out.ILoanAccountPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanaccount.domain.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoanAccountAdapter implements ILoanAccountPersistencePort {

    private final CommonRepository commonRepository;
    private final LoanAccountRepository repository;
    private final ModelMapper mapper;

    @Override
    public Flux<StagingAccountData> getLoanAccountListForStagingAccountData(String memberId, String status, String loanType) {
        return repository.getLoanAccountListForStagingAccountData(memberId, status, loanType);
    }

    @Override
    public Mono<LoanAccount> getLoanAccountDetailsByLoanAccountId(String loanAccountId) {
        return repository.getLoanAccountInfo(loanAccountId)
                .map(loanAccountProductEntity -> mapper.map(loanAccountProductEntity, LoanAccount.class));
    }

    @Override
    public Mono<LoanAccount> updateLoanAccount(String loanAccountId, LocalDate disbursementDate, BigDecimal serviceChargeRatePerPeriod, BigDecimal annualServiceChargeRate, LocalDate expectedEndDate) {
        BigDecimal finalServiceChargeRatePerPeriod = serviceChargeRatePerPeriod == null ? BigDecimal.ZERO : serviceChargeRatePerPeriod;
        log.info("Updating loan account with loanAccountId: {}, disbursementDate: {}, serviceChargeRatePerPeriod: {}", loanAccountId, disbursementDate, finalServiceChargeRatePerPeriod);

        return repository
                .findByLoanAccountId(loanAccountId)
                .map(loanAccountEntity -> {
                    loanAccountEntity.setStatus(Status.STATUS_ACTIVE.getValue());
                    loanAccountEntity.setActualDisburseDt(disbursementDate);
                    loanAccountEntity.setServiceChargeRatePerPeriod(finalServiceChargeRatePerPeriod);
                    loanAccountEntity.setOrigServiceChargeRate(annualServiceChargeRate.multiply(BigDecimal.valueOf(100)));
                    loanAccountEntity.setPlannedEndDate(expectedEndDate);
                    return loanAccountEntity;
                })
                .flatMap(repository::save)
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccount.class));
    }

    @Override
    public Mono<String> getProductIdByLoanAccountId(String loanAccountId) {
        return repository
                .findByLoanAccountId(loanAccountId)
                .map(LoanAccountEntity::getLoanProductId);
    }

    @Override
    public Mono<LoanAccount> updateLoanAccountStatusForPaidOff(String loanAccountId, String status, LocalDate businessDate) {
        return repository
                .findByLoanAccountId(loanAccountId)
                .map(loanAccountEntity -> {
                    loanAccountEntity.setStatus(status);
                    loanAccountEntity.setPaidOffDate(businessDate);
                    return loanAccountEntity;
                })
                .flatMap(repository::save)
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccount.class));
    }

    @Override
    public Mono<LoanAccount> updateLoanAccountStatus(String loanAccountId, String status) {
        return repository
                .findByLoanAccountId(loanAccountId)
                .map(loanAccountEntity -> {
                    loanAccountEntity.setStatus(status);
                    return loanAccountEntity;
                })
                .flatMap(repository::save)
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccount.class));
    }

    @Override
    public Mono<LoanAccountResponseDTO> getLoanAccountById(String oid) {
        return repository.findById(oid)
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccountResponseDTO.class));
    }

    @Override
    public Flux<LoanAccount> getAllLoanAccountsByMemberIdAndStatus(List<String> memberId, String status) {
        return repository.findAllByMemberIdInAndStatus(memberId, status)
                .doOnComplete(() -> log.info("Successfully fetched all loan accounts for member id : {} and status : {}", memberId, status))
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccount.class));
    }

    @Override
    public Flux<LoanAccount> getAllLoanAccountsForAMemberByStatus(String memberId, String status) {
        return repository.findAllByMemberIdAndStatus(memberId, status)
                .doOnComplete(() -> log.info("Successfully fetched all loan accounts for member id : {} and status : {}", memberId, status))
                .map(loanAccountEntity -> mapper.map(loanAccountEntity, LoanAccount.class));
    }
}
