package net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in;

import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.helpers.dto.DisbursementResponseDTO;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DisbursementUseCase {
    Mono<DisbursementResponseDTO> disburseLoan(String loanAccountId, LocalDate disbursementDate, String loginId, String officeId, String serviceChargeCalculationMethod);
    Mono<DisbursementResponseDTO> disburseLoanMigration(String loanAccountId, LocalDate disbursementDate, String loginId, String officeId, String serviceChargeCalculationMethod, LocalDate cutOffDate, Integer noOfPastInstallments, BigDecimal installmentAmount, BigDecimal disbursedLoanAmount, Boolean isMonthly, Integer loanTermInMonths);
    Mono<DisbursementResponseDTO> disburseLoanMigrationV3(MigrationMemberRequestDto requestDto, String loanAccountId, LocalDate disbursementDate, String loginId, String officeId, String serviceChargeCalculationMethod, LocalDate cutOffDate, Integer noOfPastInstallments, BigDecimal installmentAmount, BigDecimal disbursedLoanAmount, Boolean isMonthly, Integer loanTermInMonths);

}
