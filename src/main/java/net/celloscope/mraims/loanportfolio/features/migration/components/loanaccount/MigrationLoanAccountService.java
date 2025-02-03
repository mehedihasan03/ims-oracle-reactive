package net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication.LoanApplication;
import net.celloscope.mraims.loanportfolio.features.migration.components.mfiprogram.MigrationMfiProgramService;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationLoanAccountService {
    private final MigrationLoanAccountRepository repository;
    private final MigrationMfiProgramService mfiProgramService;

    public Mono<LoanAccount> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository.findFirstByLoanApplicationIdAndStatus(component.getLoanApplication().getLoanApplicationId(), MigrationEnums.STATUS_ACTIVE.getValue())
                .switchIfEmpty(
                        loanAccountID(component.getMember().getMemberId(), memberRequestDto.getLoanInformation().getMfiProgramId(), requestDto.getMfiId())
                                .flatMap(id ->  repository.save(buildLoanAccount(id, memberRequestDto, requestDto, component)))
                ).doOnError(throwable -> log.error("Error occurred while saving LoanAccount: {}", throwable.getMessage()));
    }

    private Mono<String> loanAccountID(String memberId, String mfiProgramId, String mfiId) {
        return mfiProgramService.getMfiProgramByIdAndMfiIdAndStatus(mfiProgramId, mfiId, MigrationEnums.STATUS_ACTIVE.getValue())
                .map(mfiProgram -> mfiProgram.getMfiProgramShortName() + "-" + memberId + "-")
                .flatMap(loanAccountIdPrefix ->
                    MigrationUtils.generateId(
                        repository.findFirstByLoanAccountIdLikeOrderByLoanAccountIdDesc(loanAccountIdPrefix + "%"),
                        LoanAccount::getLoanAccountId,
                        loanAccountIdPrefix,
                        loanAccountIdPrefix + "-%02d"
                    )
                );
    }

    private LoanAccount buildLoanAccount(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return LoanAccount.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.LOAN_ACCOUNT_APPLICATION_SHORT_NAME.getValue() + "-" + UUID.randomUUID())                   // This is the primary key
                .loanApplicationId(component.getLoanApplication().getLoanApplicationId())     // *Required
                .loanAccountId(id)         // *Required
                .memberId(component.getMember().getMemberId())
                .loanProductId(component.getLoanProduct().getLoanProductId())         // *Required
//                .origServiceChargeRate(BigDecimal.ONE)
                .productName(component.getLoanProduct().getLoanProductNameEn())            // *Required
                .econPurposeMraCode(component.getLoanApplication().getEconPurposeMraCode())        // *Required
                .lendingCategoryId(component.getLoanApplication().getLendingCategoryId())                 // *Required
                .loanAmount(component.getLoanApplication().getAppliedLoanAmount())             // *Required
                .loanTerm(component.getLoanApplication().getLoanTerm())               // *Required
                .noInstallment(component.getLoanApplication().getNoInstallment())          // *Required
                .isGraceOverwritten(component.getLoanApplication().getIsGraceOverwritten())              // *Required
                .graceDays(component.getLoanApplication().getGraceDays())              // *Required
                .installmentAmount(component.getLoanApplication().getInstallmentAmount())      // *Required
                .scheduleCreationList("[]")      // *Required
                .noRescheduled(BigDecimal.ZERO)          // *Required
//                .payMethod("Pay Method")
//                .insurance("Insurance")
//                .loanInsurancePremium(BigDecimal.ONE)
//                .loanInsurBorrowDeath(BigDecimal.ONE)
                .expectedDisburseDt(component.getLoanApplication().getExpectedDisburseDt())
                .actualDisburseDt(component.getLoanApplication().getExpectedDisburseDt())
//                .plannedEndDate(LocalDate.now())
//                .actualEndDate(LocalDate.now())
//                .loanContractPhaseId("1")
//                .loanWriteOffId("1")
//                .fundSource("Fund Source")
//                .isSubsidizedCredit("Yes")
//                .isLawSuit("No")
//                .isWelfareFund("Yes")
//                .folioNo("1")
//                .guarantorListJson("Guarantor List")
//                .currentVersion("1.0")
//                .isNewRecord("Yes")
//                .approvedBy("Approver")
//                .approvedOn(LocalDateTime.now())
//                .remarkedBy("User")
//                .remarkedOn(LocalDateTime.now())
//                .isApproverRemarks("Approver Remarks")
//                .approverRemarks("Approver Remarks")
                .mfiId(requestDto.getMfiId())                 // *Required
//                .loanClassStatus("Active")
//                .loanClassStatusDate(LocalDateTime.now())
                .status(MigrationEnums.STATUS_APPROVED.getValue())           // *Required
//                .loanAppliedOn(LocalDateTime.now())
//                .loanAppliedBy("User")
                .disbursedOn(LocalDateTime.now())
                .disbursedBy(requestDto.getLoginId())
//                .closedOn(LocalDateTime.now())
//                .closedBy("User")
                .createdBy(requestDto.getLoginId())      // *Required
                .createdOn(LocalDateTime.now())    // *Required
//                .updatedBy("User")
//                .updatedOn(LocalDateTime.now())
//                .serviceChargeRatePerPeriod(BigDecimal.ONE)
                .build();
    }

    public Flux<LoanAccount> getLoanAccountsByMemberId(String memberId) {
        return repository.findAllByMemberIdAndStatus(memberId, MigrationEnums.STATUS_ACTIVE.getValue());
    }
}
