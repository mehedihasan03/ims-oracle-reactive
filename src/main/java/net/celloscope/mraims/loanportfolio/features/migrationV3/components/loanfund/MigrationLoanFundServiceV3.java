package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanfund;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MigrationLoanFundServiceV3 {
    private final MigrationLoanFundRepositoryV3 migrationLoanFundRepository;

    public Mono<LoanFund> createLoanFund(LoanFund loanFund) {
        return migrationLoanFundRepository.save(buildLoanFund());
    }

    private LoanFund buildLoanFund() {
        return LoanFund.builder()
                .oid("1")             // This is the primary key
                .loanFundId("1")
                .loanFundingOrganizationId("1") // *Required
                .bankAccountId("1")
                .amount(BigDecimal.ZERO)    // *Required
                .fundingNotes("Funding Notes")
                .loanAvailabilityDate(LocalDate.now())
                .mfiId("1")            // *Required
                .status("Active")
                .createdBy("User")      // *Required
                .createdOn(LocalDateTime.now())    // *Required
                .updatedBy("User")
                .updatedOn(LocalDateTime.now())
                .build();
    }

}
