package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RebateInfoEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepaymentScheduleRepository extends ReactiveCrudRepository<RepaymentScheduleEntity, String> {
	@Query("""
			select * from loan_repayment_schedule
			where loan_account_id = :loanAccountId
			and install_no = :installmentNo;
			""")
	Mono<RepaymentScheduleEntity> findAllByInstallNoAndLoanAccountId(Integer installmentNo, String loanAccountId);
	
	@Query("""
			        select sum(total_payment) from loan_repay_schedule
			        where loan_account_id = :loanAccountId;
			""")
	Mono<BigDecimal> getTotalLoanPay(String loanAccountId);
	
	Flux<RepaymentScheduleEntity> getRepaymentScheduleEntitiesByLoanAccountIdOrderByInstallNo(String loanAccountId);
	
	@Query("""
			    SELECT
			    	*
			    FROM
			    	loan_repay_schedule lrs
			    WHERE
			    	lrs.loan_account_id = :loanAccountId
			    ORDER BY
			    	lrs.install_no
			    LIMIT 1;
			""")
	Mono<RepaymentScheduleEntity> getRepaymentScheduleEntityByLoanAccountIdOrderByInstallNo(String loanAccountId);
	
	Flux<RepaymentScheduleEntity> getRepaymentScheduleEntitiesByLoanAccountIdAndInstallNoIn(String loanAccountId, List<Integer> installmentNoList);
	
	@Query("""
			SELECT
			sum(lrs.principal) AS "total_principal",
			sum(lrs.service_charge) AS "total_service_charge",
			sum(lrs.principal) + sum(lrs.service_charge) AS "total_payable"
			FROM loan_repay_schedule lrs
			WHERE loan_account_id = :loanAccountId;
			""")
	Mono<RebateInfoEntity> getRebateInfoByLoanAccountId(String loanAccountId);
	
	@Query("""
			    SELECT * FROM loan_repay_schedule lrs WHERE lrs.loan_repay_schedule_id IN (:loanRepayScheduleIdList) AND lrs.status = 'Paid';
			""")
	Flux<RepaymentScheduleEntity> findAllByLoanRepayScheduleId(List<String> loanRepayScheduleIdList);

	Flux<RepaymentScheduleEntity> findByLoanRepayScheduleIdInOrderByInstallNo(List<String> loanRepayScheduleIdList);

	Flux<RepaymentScheduleEntity> findAllByInstallDate(LocalDate installDate);


	@Query("""
		select * from loan_repay_schedule lrs
		where member_id in (select member_id 
			from mem_smt_off_pri_map msopm 
			where office_id = :officeId
			and msopm.status = 'Active')
		and lrs.install_date <= :installDate;
		""")
	Flux<RepaymentScheduleEntity> findAllByInstallDateIsLessThanEqualAndOfficeId(LocalDate installDate, String officeId);

	Flux<RepaymentScheduleEntity> findAllByLoanRepayScheduleIdIn(List<String> loanRepayScheduleIdList);

	Flux<RepaymentScheduleEntity> findAllByIsProvisioned(String currentStatus);

	Mono<RepaymentScheduleEntity> findFirstByLoanAccountIdAndStatusOrderByInstallNo(String loanAccountId, String status);

	@Query("""
delete from loan_repay_schedule lrs
where lrs.management_process_id = :managementProcessId and lrs.loan_account_id = :loanAccountId;
	""")
	Mono<Boolean> deleteAllByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);

	@Query("""
    UPDATE loan_repay_schedule
    SET status = :status, management_process_id = :managementProcessId
    WHERE loan_account_id = :loanAccountId
    AND install_no >= :installmentNo
    RETURNING *;
""")
	Flux<RepaymentScheduleEntity> updateInstallmentStatusFromInstallmentNoToLast(Integer installmentNo, String status, String loanAccountId, String managementProcessId);
}
