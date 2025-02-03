package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationSavingsAccountService {
    private final MigrationSavingsAccountRepository repository;

    public Mono<SavingsAccount> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        log.info("Savings Account saving started for: {}", memberRequestDto);
        return repository.findFirstBySavingsApplicationIdAndStatus(component.getSavingsAccProposal().getSavingsApplicationId(), MigrationEnums.STATUS_ACTIVE.getValue())
            .switchIfEmpty(
                savingsAccountID(component.getMember().getMemberId(), memberRequestDto.getSavingsInformation().getSavingsTypeId())
                .flatMap(id -> repository.save(buildSavingsAccount(id, memberRequestDto, requestDto, component)))
            ).doOnNext(savingsAccount -> {
                log.info("Savings Account saved: {}", savingsAccount);
            }).doOnError(throwable -> log.error("Error occurred while saving Savings Account: {}", throwable.getMessage()));
    }

    private Mono<String> savingsAccountID(String memberId, String savingsTypeId) {
//        return Mono.just(savingsTypeId + "-" + memberId);
        return  MigrationUtils.generateId(
                repository.findFirstBySavingsAccountIdLikeOrderBySavingsAccountIdDesc(savingsTypeId + "-" + memberId + "-%"),
                SavingsAccount::getSavingsAccountId,
                savingsTypeId + "-" + memberId + "-",
                savingsTypeId + "-" + memberId + "-%02d"
        );
    }

    private SavingsAccount buildSavingsAccount(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return SavingsAccount.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.SAVINGS_ACCOUNT_SHORT_NAME.getValue() + "-" + UUID.randomUUID())               // This is the primary key
                .savingsApplicationId(component.getSavingsAccProposal().getSavingsApplicationId())    // *Required
                .savingsAccountId(id)      // *Required
                .savingsProductId(component.getSavingsProduct().getSavingsProductId())     // *Required
                .savingsTypeId(memberRequestDto.getSavingsInformation().getSavingsTypeId())    // *Required
                .savingsProdNameEn("Migration Savings")   // *Required
                .memberId(component.getMember().getMemberId())            // *Required
                .interestRateTerms(MigrationEnums.SAVINGS_INTEREST_RATE_TERM.getValue())   // *Required
                .interestRateFrequency(MigrationEnums.SAVINGS_INTEREST_RATE_FREQUENCY.getValue())    // *Required
//                .minOpeningBalance(10.00)
//                .balanceRequiredInterestCalc(500.00)
//                .enforceMinimumBalance(0)
//                .minBalance(100.00)
//                .minDepositAmount(0.00)
                .maxDepositAmount(BigDecimal.valueOf(1000000))
//                .minInstallmentAmount(0.00)
                .maxInstallmentAmount(BigDecimal.valueOf(1000000))
//                .fees(0.00)
                .acctStartDate(memberRequestDto.getSavingsInformation().getStartDate())  // *Required
//                .acctEndDate(LocalDateTime.parse("2024-02-26T00:00:00"))
//                .vsInstallment(10.00)
                .mfiId(requestDto.getMfiId()) // *Required
//                .currentVersion("0")
                .createdBy(requestDto.getLoginId())   // *Required
                .createdOn(LocalDateTime.now()) // *Required
                .status(MigrationEnums.STATUS_ACTIVE.getValue())   // *Required
                .depositTerm(memberRequestDto.getSavingsInformation().getDepositTerm())  // *Required for DPS
                .depositEvery(MigrationEnums.SAVINGS_DEPOSIT_EVERY.getValue())  // *Required for DPS
                .depositTermPeriod(MigrationEnums.SAVINGS_DEPOSIT_PERIOD.getValue())  // *Required for DPS
//                .balance(0.00)  // *Required
//                .gsInstallment(10.00)
//                .accountTerm(1)
                .savingsAmount(memberRequestDto.getSavingsInformation().getSavingsAmount())  // *Required for DPS
//                .maturityAmount(0.00)
//                .prematureClosePenalInter(500.00)
                .build();
    }
}
