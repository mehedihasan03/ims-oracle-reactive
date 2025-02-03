package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionStagingDataResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response.LoanProductInfoResponseDTO;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RebateInfoResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepaymentScheduleUseCase {

    Mono<List<RepaymentScheduleResponseDTO>> getRepaymentScheduleForLoan(LoanRepaymentScheduleRequestDTO requestDTO);

    Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleForLoan(LoanRepaymentScheduleRequestDTO requestDTO);

    Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleForLoanCalculator(LoanRepaymentScheduleRequestDTO requestDTO);

    Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleFlat(LoanRepaymentScheduleRequestDTO requestDTO);

    Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanDecliningBalance(LoanRepaymentScheduleRequestDTO requestDTO);
    Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlat(LoanRepaymentScheduleRequestDTO requestDTO);

    Mono<List<RepaymentScheduleViewDTO>> getRepaymentScheduleWithFlatPrincipal(BigDecimal loanAmount, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, Integer graceDays, LocalDate disburseDate, String samityDay, String loanTerm, String paymentPeriod, String roundingLogic, String daysInYear, Integer serviceChargeRatePrecision, Integer serviceChargePrecision, Integer installmentAmountPrecision, String installmentRoundingTo, Integer monthlyRepaymentFrequencyDay);

    Mono<RepaymentScheduleResponseDTO> getRepaymentDetailsByInstallmentNoAndLoanAccountId(Integer installmentNo, String loanAccountId);

    Mono<BigDecimal> getTotalLoanPay(String loanAccountId);

    Mono<List<RepaymentScheduleResponseDTO>> getRepaymentScheduleByLoanAccountId(String loanAccountId);

    Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleListByLoanAccountId(String loanAccountId);

    Mono<RepaymentScheduleResponseDTO> getFirstRepaymentScheduleByLoanAccountId(String loanAccountId);

    Flux<RepaymentScheduleResponseDTO> updateInstallmentStatus(List<Integer> installmentList, String status, String loanAccountId, String managementProcessId);

    Mono<RebateInfoResponseDTO> getRebateInfoByLoanAccountId(String loanAccountId);

    Mono<List<String>> updateInstallmentStatusToPending(List<String> loanRepayScheduleIdList);

    Mono<String> rescheduleLoanRepayScheduleOnSamityCancel(List<String> loanAccountIdList, String loginId, LocalDate businessDate);

    Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleByInstallmentDate(LocalDate installmentDate);
    Flux<RepaymentScheduleResponseDTO> getUnprovisionedRepaymentSchedulesByInstallmentDate(LocalDate installmentDate, String officeId);
    Mono<Boolean> updateIsProvisionedStatus(String currentStatus, String updatedStatus);
    Mono<String> revertRescheduledRepaymentSchedule(String managementProcessId);

    Mono<Boolean> archiveAndUpdateRepaymentScheduleForLoanRebate(List<LoanRebateDTO> loanRebateDTOList);
    Mono<Boolean> revertRepaymentScheduleByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);
    Mono<Boolean> archiveAndUpdateRepaymentScheduleForSeasonalSingleLoan(CollectionStagingDataResponseDTO collectionStagingDataResponseDTO);
    Flux<RepaymentScheduleResponseDTO> updateInstallmentStatusFromInstallmentNoToLast(Integer installmentNo, String status, String loanAccountId, String managementProcessId);
}