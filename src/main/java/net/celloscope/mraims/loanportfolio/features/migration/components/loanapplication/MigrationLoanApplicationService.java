package net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.migration.components.mfiprogram.MigrationMfiProgramService;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationLoanApplicationService {
    private final MigrationLoanApplicationRepository repository;
    private final MigrationMfiProgramService mfiProgramService;

    public Mono<LoanApplication> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository.findFirstByMemberIdAndAppliedLoanAmountAndExpectedDisburseDtAndStatusOrderByLoanApplicationIdDesc(
                    component.getMember().getMemberId(), getPrincipalAmount(requestDto, memberRequestDto), memberRequestDto.getLoanInformation().getLoanDisbursementDate(),
                    MigrationEnums.STATUS_APPROVED.getValue())
                .switchIfEmpty(
                        loanApplicationID(component.getMember().getMemberId(), memberRequestDto.getLoanInformation().getMfiProgramId(), requestDto.getMfiId())
                                .flatMap(id ->  repository.save(buildLoanApplication(id, memberRequestDto, requestDto, component)))
                ).doOnError(throwable -> log.error("Error occurred while saving LoanApplication: {}", throwable.getMessage()));
    }

    private BigDecimal getPrincipalAmount(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto) {
        return MigrationUtils.calculatePrincipal(memberRequestDto.getMemberId(), memberRequestDto.getLoanInformation().getDisbursedLoanAmount(),
                requestDto.getConfigurations().getPrincipalAmountPrecision(), memberRequestDto.getLoanInformation().getServiceChargeRate(),
                memberRequestDto.getLoanInformation().getLoanTerm(), requestDto.getConfigurations().getServiceChargeRatePrecision(), requestDto.getConfigurations().getRoundingMode());
    }
    private BigDecimal getInstallmentAmount(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto) {
        if (memberRequestDto.getLoanInformation().getInstallmentAmount() != null && memberRequestDto.getLoanInformation().getInstallmentAmount().compareTo(BigDecimal.ZERO) > 0)
            return memberRequestDto.getLoanInformation().getInstallmentAmount();
        return MigrationUtils.calculateInstallmentAmount(memberRequestDto.getMemberId(), memberRequestDto.getLoanInformation().getDisbursedLoanAmount(),
                memberRequestDto.getLoanInformation().getNoInstallment(), requestDto.getConfigurations().getInstallmentAmountPrecision(),
                requestDto.getConfigurations().getRoundingMode());
    }


    private Mono<String> loanApplicationID(String memberId, String mfiProgramId, String mfiId) {
        return mfiProgramService.getMfiProgramByIdAndMfiIdAndStatus(mfiProgramId, mfiId, MigrationEnums.STATUS_ACTIVE.getValue())
                .map(mfiProgram -> mfiProgram.getMfiProgramShortName() + "-" + memberId + "-")
                .doOnNext(loanApplicationIdPrefix -> log.info("Loan Application ID Prefix: {}", loanApplicationIdPrefix))
                .flatMap(loanApplicationIdPrefix ->
                        MigrationUtils.generateId(
                                repository.findFirstByLoanApplicationIdLikeOrderByLoanApplicationIdDesc(loanApplicationIdPrefix + "%", MigrationEnums.STATUS_APPROVED.getValue())
                                        .doOnNext(loanApplication -> log.info("Last Loan Application ID: {}", loanApplication.getLoanApplicationId())),
                                LoanApplication::getLoanApplicationId,
                                loanApplicationIdPrefix,
                                loanApplicationIdPrefix + "-%02d"
                        )
                ).doOnError(throwable -> log.error("Error occurred while generating LoanApplication ID: {}", throwable.getMessage()));
    }
    private LoanApplication buildLoanApplication(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return LoanApplication.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.LOAN_ACCOUNT_APPLICATION_SHORT_NAME.getValue() + "-" + UUID.randomUUID())       // This is the primary key
                .loanApplicationId(id)     // *Required
                .memberId(component.getMember().getMemberId())                  // *Required
                .loanProductId(component.getLoanProduct().getLoanProductId())             // *Required
                .econPurposeMraCode(memberRequestDto.getLoanInformation().getEconPurposeMraCode())    // *Required
                .lendingCategoryId(memberRequestDto.getLoanInformation().getLendingCategoryId())        // *Required
                .appliedLoanAmount(getPrincipalAmount(requestDto, memberRequestDto))     // *Required
                .approvedLoanAmount(getPrincipalAmount(requestDto, memberRequestDto))
                .loanTerm(memberRequestDto.getLoanInformation().getLoanTerm())               // *Required
                .noInstallment(memberRequestDto.getLoanInformation().getNoInstallment())          // *Required
                .graceDays(memberRequestDto.getLoanInformation().getDefaultGraceDays())              // *Required
                .isGraceOverwritten(memberRequestDto.getLoanInformation().getIsGraceOverwritten())               // *Required
                .installmentAmount(getInstallmentAmount(requestDto, memberRequestDto))      // *Required
//                .payMethodId("1")
//                .insurance("Insurance")
//                .loanInsurancePremium(BigDecimal.ONE)
//                .loanInsurBorrowDeath(BigDecimal.ONE)
                .expectedDisburseDt(memberRequestDto.getLoanInformation().getLoanDisbursementDate())
//                .loanContractPhaseId("1")
//                .fundSource("Fund Source")
//                .isSubsidizedCredit("No")
//                .isWelfareFund("No")
//                .folioNo("1")
//                .guarantorListJson("Guarantor List")
                .mfiId(requestDto.getMfiId())                         // *Required
//                .loanAppliedOn(LocalDateTime.now())
//                .loanAppliedBy("User")
//                .verifiedBy("User")
//                .verifiedOn(LocalDateTime.now())
//                .rejectedOn(LocalDateTime.now())
//                .rejectedBy("User")
                .status(MigrationEnums.STATUS_APPROVED.getValue())               // *Required
//                .currentVersion("1.0")
//                .isNewRecord("Yes")
//                .lockedBy("User")
//                .lockedOn(LocalDateTime.now())
//                .approvedBy("User")
//                .approvedOn(LocalDateTime.now())
//                .remarkedBy("User")
//                .remarkedOn(LocalDateTime.now())
//                .isApproverRemarks("Yes")
//                .approverRemarks("Approver Remarks")
                .createdBy(requestDto.getLoginId())              // *Required
                .createdOn(LocalDateTime.now())        // *Required
//                .updatedBy("User")
//                .updatedOn(LocalDateTime.now())
                .build();
    }
}
