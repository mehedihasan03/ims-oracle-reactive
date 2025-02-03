package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccproposal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationSavingsAccProposalService {
    private final MigrationSavingsAccProposalRepository repository;
    public Mono<SavingsAccProposal> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        log.info("Savings Account Proposal saving started for: {}", memberRequestDto);
        return /*repository.findFistByMemberIdAndAcctStartDateAndSavingsTypeIdAndStatusOrderBySavingsApplicationIdDesc(
                    component.getMember().getMemberId(), memberRequestDto.getSavingsInformation().getStartDate(),
                    memberRequestDto.getSavingsInformation().getSavingsTypeId(), MigrationEnums.STATUS_APPROVED.getValue())
                .switchIfEmpty(
                    */savingsApplicationID(memberRequestDto.getMemberId(), memberRequestDto.getSavingsInformation().getSavingsTypeId())
                        .flatMap(id ->  repository.save(buildSavingsApplication(id, memberRequestDto, requestDto, component)))
//                )
                .doOnNext(savingsAccProposal -> {
                    log.info("Savings Account Proposal saved: {}", savingsAccProposal);
                }).doOnError(throwable -> log.error("Error occurred while saving Savings Account Proposal: {}", throwable.getMessage()));
    }

    private Mono<String> savingsApplicationID(String memberId, String savingsTypeId) {
        return MigrationUtils.generateId(
                repository.findFirstBySavingsApplicationIdLikeOrderBySavingsApplicationIdDesc("SAA-" + memberId + "-" + savingsTypeId + "-%"),
                SavingsAccProposal::getSavingsApplicationId,
                "SAA-" + memberId + "-" + savingsTypeId + "-",
                "SAA-" + memberId + "-" + savingsTypeId + "-" + "%04d"
        );
    }
    private SavingsAccProposal buildSavingsApplication(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return SavingsAccProposal.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.SAVINGS_ACCOUNT_PROPOSAL_SHORT_NAME.getValue() + "-" + UUID.randomUUID())       // This is the primary key
                .savingsApplicationId(id)     // *Required
                .savingsProductId(component.getSavingsProduct().getSavingsProductId())     // *Required
                .savingsTypeId(memberRequestDto.getSavingsInformation().getSavingsTypeId())    // *Required
                .savingsProdNameEn("Migration Savings")
                .memberId(component.getMember().getMemberId())                  // *Required
                .interestRateTerms(MigrationEnums.SAVINGS_INTEREST_RATE_TERM.getValue())   // *Required
                .interestRateFrequency(MigrationEnums.SAVINGS_INTEREST_RATE_FREQUENCY.getValue())    // *Required
                .interestPostingPeriod(MigrationEnums.SAVINGS_INTEREST_POSTING_PERIOD.getValue()) // *Required
                .interestCompoundingPeriod(MigrationEnums.SAVINGS_INTEREST_COMPOUNDING_PERIOD.getValue()) // *Required
//                .minOpeningBalance(new BigDecimal("100.00"))
//                .balanceRequiredInterestCalc(new BigDecimal("500.00"))
//                .enforceMinimumBalance(new BigDecimal("0"))
//                .minBalance(new BigDecimal("100.00"))
//                .minDepositAmount(new BigDecimal("0.00"))
                .maxDepositAmount(new BigDecimal("1000000"))
//                .minInstallmentAmount(new BigDecimal("0.00"))
                .maxInstallmentAmount(new BigDecimal("1000000"))
//                .fees(new BigDecimal("0.00"))
                .acctStartDate(memberRequestDto.getSavingsInformation().getStartDate())  // *Required
//                .acctEndDate(LocalDate.parse("2022-12-31"))
//                .vsInstallment(new BigDecimal("10.00"))
                .mfiId(requestDto.getMfiId()) // *Required
//                .loanAppliedOn(LocalDateTime.parse("2022-01-01T00:00:00"))
//                .currentVersion("0")
                .createdBy(requestDto.getLoginId())   // *Required
                .createdOn(LocalDateTime.now())
                .status(MigrationEnums.STATUS_APPROVED.getValue())   // *Required
                .balance(new BigDecimal("0.00"))
//                .gsInstallment(new BigDecimal("10.00"))
//                .accountTerm(new BigDecimal("1"))
//                .savingsAmount(new BigDecimal("0.00"))
//                .prematureClosePenalInter(new BigDecimal("500.00"))
                .build();
    }
}
