package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationUtilsV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationLoanProductServiceV3 {
    private final MigrationLoanProductRepositoryV3 repository;

    public Mono<LoanProduct> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository.findByLoanProductNameEn(memberRequestDto.getLoanInformation().getLoanProductName())
                .switchIfEmpty(
                    loanProductID()
                        .flatMap(id ->  repository.save(buildLoanProduct(id, memberRequestDto, requestDto, component)))
                            .doOnError(throwable -> log.error("Error occurred while saving LoanProduct: {}", throwable.getMessage()))
                ).doOnError(throwable -> log.error("Error occurred while saving LoanProduct: {}", throwable.getMessage()));
    }

    private Mono<String> loanProductID() {
        return MigrationUtilsV3.generateId(
                repository.findFirstByOrderByLoanProductIdDesc(),
                LoanProduct::getLoanProductId,
//                "L-",
                "L",
//                "L-%04d"
                "L-%04d"
        );
    }

    private LoanProduct buildLoanProduct(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return LoanProduct.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.LOAN_PRODUCT_SHORT_NAME.getValue() + "-" + UUID.randomUUID())               // This is the primary key
                .loanProductId(id)     // *Required
//                .compLoanProductId("1")
                .loanProductNameEn(memberRequestDto.getLoanInformation().getLoanProductName())  // *Required
                .loanProductNameBn(memberRequestDto.getLoanInformation().getLoanProductName())  // *Required
                .loanProductDisplayName(memberRequestDto.getLoanInformation().getLoanProductName())
//                .descProduct("Description")
                .mfiProgramId(memberRequestDto.getLoanInformation().getMfiProgramId())                  // *Required
                .productNature("Product Nature")
                .loanTypeId(memberRequestDto.getLoanInformation().getLoanTypeId())                      // *Required for staging data
//                .minLoanAmount(BigDecimal.ONE)
//                .maxLoanAmount(BigDecimal.TEN)
                .repaymentFrequency(memberRequestDto.getLoanInformation().getRepaymentFrequency())
                .monthlyRepayDay(memberRequestDto.getLoanInformation().getMonthlyRepayDay())
//                .interestCalcMethod("Simple")
                .defaultGraceDays(memberRequestDto.getLoanInformation().getDefaultGraceDays())
//                .fundSource("Fund Source")
//                .loanProductStartDate(LocalDate.now())
//                .loanProductCloseDate(LocalDate.now())
//                .currentVersion("1.0")
//                .isNewRecord("Yes")
//                .approvedBy("Approver")
//                .approvedOn(LocalDateTime.now())
//                .remarkedBy("User")
//                .remarkedOn(LocalDateTime.now())
//                .isApproverRemarks("Approver Remarks")
//                .approverRemarks("Approver Remarks")
                .mfiId(requestDto.getMfiId())             // *Required
                .status(MigrationEnums.STATUS_ACTIVE.getValue())
                .migratedBy(requestDto.getLoginId())
                .migratedOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())      // *Required
                .createdOn(LocalDateTime.now())        // *Required
//                .updatedBy("User")
//                .updatedOn(LocalDateTime.now())
//                .closedBy("User")
//                .closedOn(LocalDateTime.now())
                .build();
    }
}
