package net.celloscope.mraims.loanportfolio.features.migration.components.lendingcategory;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MigrationLendingCategoryService {
    private final MigrationLendingCategoryRepository migrationLendingCategoryRepository;

    public Mono<LendingCategory> save(LendingCategory lendingCategory) {
        return migrationLendingCategoryRepository.save(lendingCategory);
    }

    private LendingCategory buildLendingCategory() {
        return LendingCategory.builder()
                .oid("1")             // This is the primary key
                .lendingCategoryId("1")
                .lendingCategoryShortName("Short Name") // *Required
                .lendingCategoryNameEn("English Name")
                .lendingCategoryNameBn("Bangla Name")
                .noPrimaryLoanProduct("No Primary Loan Product")
                .noOtherLoanProduct("No Other Loan Product")
                .currentVersion("1.0")
                .isNewRecord("Yes")
                .approvedBy("User")
                .approvedOn(LocalDateTime.now())
                .remarkedBy("User")
                .remarkedOn(LocalDateTime.now())
                .isApproverRemarks("Yes")
                .approverRemarks("Approver Remarks")
                .mfiId("1")         // *Required
                .status("Active")
                .createdBy("User")  // *Required
                .createdOn(LocalDateTime.now())    // *Required
                .updatedBy("User")
                .updatedOn(LocalDateTime.now())
                .build();
    }

}
