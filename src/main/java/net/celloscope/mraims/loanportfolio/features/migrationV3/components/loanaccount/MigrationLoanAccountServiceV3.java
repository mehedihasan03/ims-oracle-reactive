package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanaccount;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationUtilsV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.mfiprogram.MigrationMfiProgramServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationLoanAccountServiceV3 {
    private final MigrationLoanAccountRepositoryV3 repository;
    private final MigrationMfiProgramServiceV3 mfiProgramService;

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
                    MigrationUtilsV3.generateId(
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
                .loanAccountId(memberRequestDto.getLoanInformation().getLoanAccountId() != null ? memberRequestDto.getLoanInformation().getLoanAccountId() : id)         // *Required
//                .loanAccountId(id)         // *Required
                .companyLoanAccountId(memberRequestDto.getLoanInformation().getCompanyLoanAccountId())
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
                .payMethod(component.getLoanApplication().getPayMethodId())
                .insurance(component.getLoanApplication().getInsurance())
//                .loanInsurancePremium(BigDecimal.ONE)
//                .loanInsurBorrowDeath(BigDecimal.ONE)
                .expectedDisburseDt(component.getLoanApplication().getExpectedDisburseDt())
                .actualDisburseDt(component.getLoanApplication().getExpectedDisburseDt())
//                .plannedEndDate(LocalDate.now())
//                .actualEndDate(LocalDate.now())
                .loanContractPhaseId(component.getLoanApplication().getLoanContractPhaseId())
//                .loanWriteOffId("1")
//                .fundSource("Fund Source")
                .isSubsidizedCredit(component.getLoanApplication().getIsSubsidizedCredit())
                .isLawSuit(memberRequestDto.getLoanInformation().getLawSuit())
                .isWelfareFund(component.getLoanApplication().getIsWelfareFund())
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
                .businessDate(requestDto.getBusinessDate())
                .managementProcessId(requestDto.getManagementProcessId())
                .build();
    }

    public Flux<LoanAccount> getLoanAccountsByMemberId(String memberId) {
        return repository.findAllByMemberIdAndStatus(memberId, MigrationEnums.STATUS_ACTIVE.getValue());
    }
}
