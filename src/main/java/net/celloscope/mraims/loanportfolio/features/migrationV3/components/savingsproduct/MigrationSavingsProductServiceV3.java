package net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsproduct;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationUtilsV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationSavingsProductServiceV3 {

    private final MigrationSavingsProductRepositoryV3 repository;

    public Mono<SavingsProduct> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        log.info("Savings Product saving started for: {}", memberRequestDto);
        return repository.findFirstBySavingsProdNameEnOrderBySavingsProductId(memberRequestDto.getSavingsInformation().getSavingsProductName())
            .switchIfEmpty(
                    savingsProductID()
                    .flatMap(id ->  repository.save(buildLoanProduct(id, memberRequestDto, requestDto, component)))
            ).doOnNext(savingsProduct -> {
                log.info("Savings Product saved: {}", savingsProduct);
            }).doOnError(throwable -> log.error("Error occurred while saving Savings Product: {}", throwable.getMessage()));
    }

    private Mono<String> savingsProductID() {
        return MigrationUtilsV3.generateId(
                repository.findFirstByOrderBySavingsProductIdDesc(),
                SavingsProduct::getSavingsProductId,
                "SP-",
                "SP-%04d"
        );
    }

    private SavingsProduct buildLoanProduct(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return SavingsProduct.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.LOAN_PRODUCT_SHORT_NAME.getValue() + "-" + UUID.randomUUID())              // This is the primary key
                .savingsProductId(id)   // *Required
                .savingsTypeId(memberRequestDto.getSavingsInformation().getSavingsTypeId())    // *Required
                .shortNameDep("Migration Savings Product") // *Required
                .savingsProdNameEn(memberRequestDto.getSavingsInformation().getSavingsProductName()) // *Required
                .savingsProdNameBn(memberRequestDto.getSavingsInformation().getSavingsProductName()) // *Required
                .displayName(memberRequestDto.getSavingsInformation().getSavingsProductName()) // *Required
//                .descSavingsProd("General Savings")
                .interestRateTerms(MigrationEnums.SAVINGS_INTEREST_RATE_TERM.getValue())
                .interestRateFrequency(MigrationEnums.SAVINGS_INTEREST_RATE_FREQUENCY.getValue()) // *Required
                .interestCalculatedUsing(MigrationEnums.SAVINGS_INTEREST_CALCULATE_USING.getValue()) // *Required
                .interestPostingPeriod(MigrationEnums.SAVINGS_INTEREST_POSTING_PERIOD.getValue()) // *Required
                .interestCompoundingPeriod(MigrationEnums.SAVINGS_INTEREST_COMPOUNDING_PERIOD.getValue()) // *Required
//                .minOpeningBalance(0.00)
//                .balanceRequiredInterestCalc(0.00)
//                .enforceMinimumBalance(false)
//                .minBalance(0.00)
//                .minDepositAmount(0.00)
                .maxDepositAmount(BigDecimal.valueOf(1000000))
//                .minInstallmentAmount(0.00)
                .maxInstallmentAmount(BigDecimal.valueOf(1000000))
                .mfiId(requestDto.getMfiId()) // *Required
                .status(MigrationEnums.STATUS_ACTIVE.getValue())   // *Required
//                .currentVersion("0")
//                .isNewRecord(true)
                .createdBy(requestDto.getLoginId())   // *Required
                .createdOn(LocalDateTime.now())  // *Required
                .build();
    }

}
