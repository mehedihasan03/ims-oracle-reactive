package net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.entity.HolidayEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface HolidayRepository extends ReactiveCrudRepository<HolidayEntity, String> {

	Mono<HolidayEntity> findFirstByOfficeIdAndHolidayDateAfterOrderByHolidayDate(String officeId, LocalDate currentBusinessDate);

	@Query("""
			select * from holiday h
			inner join mem_smt_off_pri_map msopm 
			on h.office_id = msopm.office_id
			inner join loan_account la 
			on la.member_id  = msopm.member_id
			where la.loan_account_id = :loanAccountId
			and msopm.status = 'Active';
			""")
	Flux<HolidayEntity> getAllHolidaysOfASamityByLoanAccountId(String loanAccountId);

	@Query("""
			select * from holiday h
			inner join mem_smt_off_pri_map msopm on
			h.office_id = msopm.office_id
			inner join savings_account sa on
			sa.member_id = msopm.member_id
			where sa.savings_account_id = :savingsAccountId
			and msopm.status = 'Active';
			""")
	Flux<HolidayEntity> getAllHolidaysOfASamityBySavingsAccountId(String savingsAccountId);

	Flux<HolidayEntity> findAllByOfficeIdOrderByHolidayDate(String officeId);

	Flux<HolidayEntity> findAllByManagementProcessIdAndOfficeIdOrderByHolidayDate(String managementProcessId, String officeId);
}
