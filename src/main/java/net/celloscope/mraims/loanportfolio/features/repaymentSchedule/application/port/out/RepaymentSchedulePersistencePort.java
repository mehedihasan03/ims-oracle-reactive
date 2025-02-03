package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RebateInfoEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RepaymentSchedulePersistencePort {

    Mono<List<RepaymentSchedule>> saveRepaymentSchedule(List<RepaymentSchedule> repaymentSchedule);

    Mono<RepaymentSchedule> getRepaymentDetailsByInstallmentNoAndLoanAccountId(Integer installmentNo, String loanAccountId);

    Mono<BigDecimal> getTotalLoanPay(String loanAccountId);

    Mono<List<RepaymentSchedule>> getRepaymentScheduleListByLoanAccountId(String loanAccountId);

    Flux<RepaymentSchedule> updateInstallmentStatus(List<Integer> installmentList, String status, String loanAccountId, String managementProcessId);

    Mono<RebateInfoEntity> getRebateInfoByLoanAccountId(String loanAccountId);

    /*Mono<List<RepaymentSchedule>> printRepaymentSchdeuleWithDates(List<RepaymentSchedule> repaymentSchedule);*/

    Mono<List<String>> updateInstallmentStatusToPending(List<String> loanRepayScheduleIdList);

    Mono<List<RepaymentScheduleEntity>> updateRepaymentScheduleForSamityCancel(List<RepaymentSchedule> repaymentScheduleList, String loginId);

    Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleByInstallmentDate(LocalDate installmentDate);

    Flux<RepaymentScheduleResponseDTO> getUnprovisionedRepaymentSchedulesByInstallmentDate(LocalDate installmentDate, String officeId);
    Flux<RepaymentScheduleResponseDTO> updateIsProvisionedStatus(List<String> loanRepayScheduleIdList, String status);

    Mono<Boolean> updateIsProvisionedStatus(String currentStatus, String updatedStatus);

    Mono<RepaymentSchedule> getFirstPendingRepaymentScheduleByLoanAccountId(String loanAccountId);

    Flux<RepaymentSchedule> getRepaymentScheduleListByOidList(List<String> oidList);
    Mono<List<RepaymentSchedule>> saveAllRepaymentSchedule(List<RepaymentSchedule> repaymentScheduleList);
    Mono<Boolean> deleteRepaymentScheduleListByOid(List<String> oidList);
    Mono<Boolean> updateRepaymentScheduleStatusByOid(List<String> oidList, String status);

    Mono<Boolean> deleteRepaymentScheduleListByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);

    Flux<RepaymentScheduleResponseDTO> updateInstallmentStatusFromInstallmentNoToLast(Integer installmentNo, String status, String loanAccountId, String managementProcessId);
}
