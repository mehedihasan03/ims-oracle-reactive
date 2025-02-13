package net.celloscope.mraims.loanportfolio.features.common.queries.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.GridViewDataObject;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.SplitTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity.LoanAccountDetailsEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommonRepository extends ReactiveCrudRepository<StagingAccountDataEntity, String> {

    @Query("""
            SELECT DISTINCT
                s.field_officer_id,
                e.emp_name_en AS field_officer_name_en,
                e.emp_name_bn AS field_officer_name_bn
            FROM
                template.samity s
            INNER JOIN template.mem_smt_off_pri_map msopm ON
                s.samity_id = msopm.samity_id
            INNER JOIN template.employee e ON
                s.field_officer_id = e.employee_id
            WHERE
                msopm.office_id = :officeId
            AND 
                msopm.status = 'Active'
            ORDER BY
                s.field_officer_id;
            """)
    Flux<FieldOfficerEntity> getFieldOfficersByOfficeId(String officeId);

    @Query("""
            SELECT
                *
                FROM
                	template.samity s
                WHERE
                	s.office_id = :officeId;
                """)
    Flux<Samity> getSamityByOfficeId(String officeId);

    @Query("""
            select m.member_id, m.member_name_en , m.member_name_bn, msopm.office_id,
                		m.mobile, m.register_book_serial_id , m.gender , m.marital_status , m.father_name_en , m.father_name_bn , m.spouse_name_en , m.spouse_name_bn,
                        m.res_address_line_1 as res_address, m.per_address_line_1 as per_address
            from template."member" m inner join template.mem_smt_off_pri_map msopm
            on m.member_id = msopm.member_id
            WHERE m.member_id = :memberId
            AND msopm.status = 'Active';
            """)
    Mono<MemberEntity> getMemberEntityByMemberId(String memberId);

    @Query("""
            select m.*, msopm.office_id from template.loan_account la
            INNER JOIN template."member" m
            ON la.member_id = m.member_id
            inner join template.mem_smt_off_pri_map msopm
            on la.member_id = msopm.member_id
            WHERE la.loan_account_id = :loanAccountId
            AND msopm.status = 'Active';
            """)
    Mono<MemberEntity> getMemberInfoByLoanAccountId(String loanAccountId);

    @Query("""
            select m.*, msopm.office_id from template.savings_account sa
            INNER JOIN template."member" m
            ON sa.member_id = m.member_id
            inner join template.mem_smt_off_pri_map msopm
            on sa.member_id = msopm.member_id
            WHERE sa.savings_account_id = :savingsAccountId
            and msopm.status = 'Active';
            """)
    Mono<MemberEntity> getMemberEntityBySavingsAccountId(String savingsAccountId);

    @Query("""
                SELECT DISTINCT
                    *
                FROM
                    template.office o
                WHERE
                    o.office_id = :officeId
            """)
    Mono<OfficeEntity> getOfficeEntityByOfficeId(String officeId);

    @Query("""
                SELECT
                	DISTINCT
                  e.employee_id AS field_officer_id,
                	e.emp_name_en AS field_officer_name_en,
                	e.emp_name_bn AS field_officer_name_bn
                FROM
                	template.employee e
                WHERE
                	e.employee_id = :fieldOfficerId;
            """)
    Mono<FieldOfficerEntity> getFieldOfficerByFieldOfficerId(String fieldOfficerId);

    @Query("""
            select ic.interest as interest_rate, ic.provision_interest as provision_interest_rate, sp.savings_product_id, sp.savings_type_id, sa.oid as savings_account_oid, sa.savings_account_id, sa.acct_start_date , sa.member_id , sa.mfi_id , sp.interest_rate_terms , sp.interest_rate_frequency , sp.interest_calculated_using , sp.interest_posting_period , sp.interest_compounding_period , sp.balance_required_interest_calc , sp.status, sa.interest_posting_dates
            from template.savings_product sp
            inner join template.savings_account sa
            on sa.savings_product_id = sp.savings_product_id
            inner join template.interest_chart ic
            on ic.savings_product_id = sa.savings_product_id
            where sa.savings_account_id = :savingsAccountId
            and ic.status = 'Active';
            """)
    Mono<SavingsAccountProductEntity> getSavingsProductEntityBySavingsAccountId(String savingsAccountId);

    @Query("""
            select * from template.staging_data sd
            inner join template.savings_account sa
            on sd.member_id = sa.member_id
            where sa.savings_account_id = :savingsAccountId;
            """)
    Mono<StagingDataEntity> getStagingDataEntityBySavingsAccountId(String savingsAccountId);

    @Query("""
            select * from template.samity s where samity_id  = :samityId ;
            """)
    Mono<Samity> getSamityBySamityId(String samityId);

    @Query("""
            select * from template.samity s where samity_day  = :samityDay ;
            """)
    Flux<Samity> getSamityBySamityDay(String samityDay);

    @Query("""
            select exists (select 1 from template.samity_event_tracker set2 where samity_id = :samity_id and management_process_id = :management_process_id);
               """)
    Mono<Boolean> checkIfSamityEventExists(String samityId, String managementProcessID);

    @Query("""
            select * from template.management_process_tracker mpt where mpt.management_process_id = :management_process_id ;
            """)
    Mono<ManagementProcessTrackerEntity> getManagementProcessTrackerByProcessID(String processId);

    @Query("""
            SELECT * FROM template.office o WHERE o.mfi_id = :mfiId ORDER BY o.office_id LIMIT :limit OFFSET :offset;
            """)
    Flux<OfficeEntity> getOfficeListByMfi(String mfiId, Integer limit, Integer offset);

    @Query("""
            SELECT count(o.office_id) FROM template.office o WHERE o.mfi_id = :mfiId;
            """)
    Mono<Long> getTotalCountOfOfficeByMfi(String mfiId);

    @Query("""
            SELECT * FROM template.samity s WHERE s.office_id = :officeId ORDER BY s.samity_id;
            """)
    Flux<Samity> getSamityEntityListByOffice(String officeId);

    @Query("""
            select ls.ledger_id from template.ledger_setting ls
            where ledger_key = :ledgerKey
            and office_id = :officeId;
            """)
    Mono<String> getLedgerIdByLedgerKeyAndOfficeId(String ledgerKey, String officeId);

    @Query("""
            select s.subledger_id from template.subledger s
            where ledger_id = :ledgerId
            and product_id = :productId;
            """)
    Mono<String> getSubLedgerIdByLedgerIdAndProductId(String ledgerId, String productId);

    @Query("""
            select s.subledger_id from template.subledger s
            where ledger_id = :ledgerId
            and reference_id = :referenceId;
            """)
    Mono<String> getSubLedgerIdByLedgerIdAndReferenceId(String ledgerId, String referenceId);

    @Query("""
            	SELECT DISTINCT
            	set2.samity_id
              FROM
            	template.samity_event_tracker set2
              INNER JOIN template.management_process_tracker mpt ON
            	mpt.management_process_id = set2.management_process_id
              INNER JOIN template.samity s ON
            	s.samity_id = set2.samity_id
              WHERE
            	s.samity_day != mpt.business_day
            	AND set2.management_process_id = :managementProcessId
            	AND set2.office_id = :officeId;
            """)
    Flux<String> getSpecialSamityIdListForManagementProcessByOffice(String managementProcessId, String officeId);


    @Query("""
            	SELECT DISTINCT
            	set2.samity_id
              FROM
            	template.samity_event_tracker set2
              INNER JOIN template.management_process_tracker mpt ON
            	mpt.management_process_id = set2.management_process_id
              INNER JOIN template.samity s ON
            	s.samity_id = set2.samity_id
              WHERE
            set2.management_process_id = :managementProcessId
            	AND set2.office_id = :officeId
            	AND set2.samity_event = :samityEvent;
            """)
    Flux<String> getSamityIdListForManagementProcessByOfficeAndSamityEvent(String managementProcessId, String officeId, String samityEvent);

    @Query("""
            select msopm.office_id  from template."member" m
            inner join template.mem_smt_off_pri_map msopm
            on m.member_id = msopm.member_id
            where m.member_id = :memberId
            and msopm.status = 'Active';
            """)
    Mono<String> getOfficeIdByMemberId(String memberId);

    @Query("""
            select sa.savings_product_id , sa.savings_account_id, sa.savings_amount , ic.interest as interest_rate, sa.interest_rate_frequency , sa.interest_posting_period , sa.deposit_term , sa.deposit_term_period, sa.acct_start_date
            from template.savings_account sa
            inner join template.interest_chart ic
            on ic.savings_product_id = sa.savings_product_id
            where
            sa.savings_account_id = :savingsAccountId and ic.status = 'Active';
            """)
    Mono<FDRInterestCalculationEntity> getFDRInterestCalculationEntityBySavingsAccountId(String savingsAccountId);

    @Query("""
            	SELECT la.loan_account_id FROM template.loan_account la WHERE la.member_id IN (
                    SELECT DISTINCT msopm.member_id 
                    FROM template.mem_smt_off_pri_map msopm 
                    WHERE msopm.samity_id = :samityId 
                    AND msopm.status = 'Active'
                    ORDER BY msopm.member_id) 
            	AND la.status = :status;
            """)
    Flux<String> getLoanAccountIdListBySamityIdAndStatus(String samityId, String status);

    @Query("""
              SELECT sum(csd.amount) 
              FROM template.collection_staging_data csd 
              WHERE csd.management_process_id = :managementProcessId 
              AND csd.samity_id IN 
                (SELECT DISTINCT msopm.samity_id 
                FROM template.mem_smt_off_pri_map msopm 
                WHERE msopm.office_id = :officeId
                AND msopm.status = 'Active' 
                ORDER BY msopm.samity_id);
            """)
    Mono<Double> getTotalCollectionAmountByOffice(String managementProcessId, String officeId);

    @Query("""
            	SELECT sum(swd.amount) 
            	FROM template.staging_withdraw_data swd 
            	WHERE swd.management_process_id = :managementProcessId 
            	AND swd.samity_id IN 
            	    (SELECT DISTINCT msopm.samity_id 
            	    FROM template.mem_smt_off_pri_map msopm 
            	    WHERE msopm.office_id = :officeId 
            	    AND msopm.status = 'Active'
            	    ORDER BY msopm.samity_id);
            """)
    Mono<Double> getTotalWithdrawAmountByOffice(String managementProcessId, String officeId);

    @Query("""
            SELECT count(calendar_date) FROM template.calendar c WHERE office_id = :officeId AND calendar_date BETWEEN :businessDate AND :currentDate;
            """)
    Mono<Integer> getTotalDaysBetweenBusinessDateAndCurrentDateByOffice(String officeId, LocalDate businessDate,
                                                                        LocalDate currentDate);

    @Query("""
            SELECT count(holiday_date) FROM template.holiday h WHERE template.office_id = :officeId AND holiday_date BETWEEN :businessDate AND :currentDate;
            """)
    Mono<Integer> getTotalHolidaysBetweenBusinessDateAndCurrentDateByOffice(String officeId, LocalDate businessDate,
                                                                            LocalDate currentDate);

    @Query("""
            SELECT office_id FROM template.emp_office_map esom WHERE employee_id = :fieldOfficerId;
            """)
    Mono<String> getOfficeIdOfAFieldOfficer(String fieldOfficerId);

    @Query("""
            SELECT DISTINCT samity_id FROM template.samity s WHERE office_id = :officeId AND samity_day = :samityDay ORDER BY s.samity_id;
            """)
    Flux<String> getSamityIdListByOfficeIdAndSamityDay(String officeId, String samityDay);

    @Query("""
            SELECT DISTINCT samity_id FROM template.samity s WHERE office_id = :officeId AND samity_day != :samityDay ORDER BY s.samity_id;
            """)
    Flux<String> getSamityIdListByOfficeIdAndNonSamityDay(String officeId, String samityDay);

    @Query("""
            	SELECT DISTINCT samity_id FROM template.samity s WHERE field_officer_id = :fieldOfficerId AND samity_day = :samityDay ORDER BY s.samity_id;
            """)
    Flux<String> getSamityIdListByFieldOfficerIdAndSamityDay(String fieldOfficerId, String samityDay);

    @Query("""
            SELECT DISTINCT samity_id FROM template.samity s WHERE field_officer_id = :fieldOfficerId AND samity_day != :samityDay ORDER BY s.samity_id;
            """)
    Flux<String> getSamityIdListByFieldOfficerIdAndNonSamityDay(String fieldOfficerId, String samityDay);

    @Query("""
            SELECT s.samity_id, s.samity_name_en, s.samity_name_bn, s.samity_day, s.mfi_id, e.employee_id AS field_officer_id, e.emp_name_en AS field_officer_name_en, e.emp_name_bn AS field_officer_name_bn FROM template.samity s INNER JOIN template.employee e ON s.field_officer_id = e.employee_id WHERE samity_id = :samityId;
            """)
    Mono<GridViewDataObject> getGridViewDataObjectForSamityWithNoMember(String samityId);

    @Query("""
            SELECT DISTINCT s.samity_id FROM template.samity s WHERE s.office_id = :officeId;
            """)
    Flux<String> getSamityIdListByOfficeId(String officeId);

    @Query("""
            SELECT DISTINCT s.samity_id FROM template.samity s WHERE s.field_officer_id = :field_officer_id;
            """)
    Flux<String> getSamityIdListByFieldOfficerId(String employeeId);

    @Query("""
            	  SELECT
            		s.samity_id,
            		s.samity_name_en,
            		s.samity_name_bn,
            		e.employee_id AS field_officer_id,
            		e.emp_name_en AS field_officer_name_en,
            		e.emp_name_bn AS field_officer_name_bn,
            		s.samity_day
            	  FROM
            		template.samity s
            	  INNER JOIN template.employee e ON
            		s.field_officer_id = e.employee_id
            	  WHERE
            		s.samity_id IN (:samityIdList);
            """)
    Flux<StagingProcessTrackerEntity> getSamityDetailsForStagingDataBySamityIdList(List<String> samityIdList);

    @Query("""
            SELECT count(msopm.member_id) 
            FROM template.mem_smt_off_pri_map msopm 
            WHERE msopm.samity_id = :samityId
            AND msopm.status = 'Active';
            """)
    Mono<Integer> getTotalMemberOfASamity(String samityId);

    @Query("""
            SELECT count(la.loan_account_id) 
            FROM template.loan_account la 
            WHERE la.member_id IN 
                (SELECT msopm.member_id 
                FROM template.mem_smt_off_pri_map msopm 
                WHERE msopm.samity_id = :samityId
                AND msopm.status = 'Active') 
            AND la.status = 'Active';
            """)
    Mono<Integer> getTotalActiveLoanAccountOfASamity(String samityId);

    @Query("""
            SELECT count(sa.savings_account_id) 
            FROM template.savings_account sa 
            WHERE sa.member_id IN 
                (SELECT msopm.member_id 
                FROM template.mem_smt_off_pri_map msopm 
                WHERE msopm.samity_id = :samityId
                AND msopm.status = 'Active');
            """)
    Mono<Integer> getTotalActiveSavingsAccountOfASamity(String samityId);

    @Query("""
            SELECT msopm.member_id 
            FROM template.mem_smt_off_pri_map msopm 
            WHERE msopm.samity_id = :samityId
            AND msopm.status = 'Active';
            """)
    Flux<String> getMemberIdListOfASamity(String samityId);

    @Query("""
            SELECT * FROM template."member" m WHERE m.member_id IN (:memberIdList);
            """)
    Flux<MemberEntity> getMemberEntityByMemberIdList(List<String> memberIdList);

    @Query("""
            	  SELECT
            		lp.loan_product_name_en AS product_name_en,
            		lp.loan_product_name_bn AS product_name_bn,
            		la.loan_product_id AS product_code,
            		*
            	  FROM
            		loan_account la
            	  INNER JOIN template.loan_product lp ON
            		la.loan_product_id = lp.loan_product_id
            	  WHERE
            		la.status in (:statusList)
            		AND
            		  la.member_id IN (:memberIdList);
            """)
    Flux<StagingAccountData> getStagingLoanAccountListForMembersOfASamity(List<String> memberIdList, List<String> statusList);

    @Query("""
            	SELECT s.office_id FROM template.samity s WHERE s.samity_id = :samityId;
            """)
    Mono<String> getOfficeIdBySamityId(String samityId);

    @Query("""
            	SELECT "oid" FROM template.savings_account sa WHERE savings_account_id = :savingsAccountId;
            """)
    Mono<String> getSavingsAccountOidForSavingsAccount(String savingsAccountId);

    @Query("""
            	SELECT la.loan_account_id as account_id, la.loan_product_id as product_id, lp.loan_product_name_en as product_name_en, lp.loan_product_name_bn as product_name_bn FROM template.loan_account la INNER JOIN template.loan_product lp ON la.loan_product_id = lp.loan_product_id WHERE la.loan_account_id IN (:loanAccountIdList);
            """)
    Flux<AccountWithProductEntity> getLoanProductDetailsByLoanAccountList(List<String> loanAccountIdList);

    @Query("""
            	SELECT sa.savings_account_id as account_id, sa.savings_product_id as product_id, sp.savings_prod_name_en AS product_name_en, sp.savings_prod_name_bn AS product_name_bn FROM template.savings_account sa INNER JOIN template.savings_product sp ON sa.savings_product_id = sp.savings_product_id WHERE sa.savings_account_id IN (:savingsAccountIdList);
            """)
    Flux<AccountWithProductEntity> getSavingsProductDetailsBySavingsAccountList(List<String> savingsAccountIdList);

    @Query("""
            	select sum(amount) from template."transaction" t where management_process_id = :managementProcessId and office_id = :officeId and transaction_code = :transactionCode;
            """)
    Mono<Double> getTotalAmountByTransactionCodeForOffice(String managementProcessId, String officeId, String transactionCode);

    @Query("""
            select loan_amount from template.loan_account where loan_account_id = :loanAccountId;
              """)
    Mono<BigDecimal> getLoanAmountByLoanAccountId(String loanAccountId);

    @Query("""
            SELECT sum(service_charge) FROM template.loan_repay_schedule lrs WHERE loan_account_id = :loanAccountId;
              """)
    Mono<BigDecimal> getServiceChargeByLoanAccountId(String loanAccountId);

    @Query("""
            SELECT la.loan_account_id, la.loan_product_id, lp.loan_product_name_en, lp.loan_product_name_bn, la.status, la.loan_amount FROM template.loan_account la INNER JOIN template.loan_product lp ON la.loan_product_id = lp.loan_product_id WHERE la.loan_account_id = :loanAccountId;
            """)
    Mono<LoanAccountDetailsEntity> getLoanAccountEntityByLoanAccountId(String loanAccountId);

    @Query("""
            	SELECT savings_type_id FROM template.savings_account sa WHERE savings_account_id = :savingsAccountaId;
            """)
    Mono<String> getSavingsTypeIdBySavingsAccountId(String savingsAccountId);

    @Query("""
            	SELECT savings_product_id AS product_id, savings_prod_name_en AS product_name_en, savings_prod_name_bn AS product_name_bn  FROM template.savings_product sp WHERE sp.savings_product_id IN (:savingsProductIdList);
            """)
    Flux<AccountWithProductEntity> findAllBySavingsProductIdList(List<String> savingsProductIdList);

    @Query("""
            	SELECT * from template.collection_staging_data WHERE savings_account_id IS NOT NULL AND savings_account_id = :savingsAccountId;
            """)
    Flux<CollectionStagingDataEntity> getCollectionStagingDataBySavingsAccountId(String savingsAccountId);

    @Query("""
            	SELECT * from template.staging_withdraw_data WHERE savings_account_id IS NOT NULL AND savings_account_id = :savingsAccountId;
            """)
    Flux<StagingWithdrawDataEntity> getStagingWithdrawDataBySavingsAccountId(String savingsAccountId);

    @Query("""
            	SELECT * from template.loan_adjustment_data WHERE savings_account_id IS NOT NULL AND savings_account_id = :savingsAccountId;
            """)
    Flux<LoanAdjustmentDataEntity> getLoanAdjustmentDataBySavingsAccountId(String savingsAccountId);

    @Query("""
            	SELECT * FROM template.financial_period WHERE office_id = :officeId;
            """)
    Flux<FinancialPeriodEntity> getFinancialPeriodEntriesForOffice(String officeId);

    @Query("""
            	SELECT i.oid FROM common.institute i WHERE i.institute_id = :mfiId FETCH 1 ROWS ONLY;
            """)
    Mono<String> getInstituteOidByMFIId(String mfiId);

    @Query("""
            select disbursement_date from template.passbook p where disbursed_loan_account_id = :loanAccountId;
            """)
    Mono<LocalDate> getDisbursementDateByLoanAccountId(String loanAccountId);

    @Query("select COALESCE(SUM(lrs.service_charge), 0) from template.loan_repay_schedule lrs where loan_account_id = :loanAccountId and status = 'Pending' and install_date > :businessDate;")
    Mono<BigDecimal> getTotalRebateAmountByLoanAccountId(String loanAccountId, LocalDate businessDate);

    @Query("""
            select m.member_id , m.member_name_en , m.member_name_bn , la.oid as loan_account_oid ,la.loan_account_id , la.loan_amount from template.loan_account la inner join template."member" m on la.member_id = m.member_id where la.loan_account_id = :loanAccountId;
            """)
    Mono<MemberAndLoanAccountEntity> getMemberAndLoanAccountByLoanAccountId(String loanAccountId);

    @Query("""
            select * from template.loan_product lp
            where mfi_id = (select mfi_id from common.institute i where oid = :instituteOid) and status = :status;
            """)
    Flux<LoanProductEntity> getAllLoanProductsByMfi(String officeId, String status);

    @Query("""
            select * from template.loan_product lp where loan_product_id = :loanProductId;
            """)
    Mono<LoanProductEntity> getLoanProductEntityByLoanProductId(String loanProductId);

    @Query("""
            select * from template.service_charge_chart scc where loan_product_id = :loanProductId;
            """)
    Mono<ServiceChargeChartEntity> getServiceChargeChartEntityByLoanProductId(String loanProductId);

    @Query("""
            select * from template.member m 
            inner join template.mem_smt_off_pri_map msopm 
            on m.member_id = msopm.member_id  
            inner join template.office o 
            on msopm.office_id = o.office_id 
            inner join template.samity s 
            on msopm.samity_id = s.samity_id 
            where m.member_id = :memberId
            and msopm.status = 'Active';
            """)
    Mono<MemberAndOfficeAndSamityEntity> getMemberOfficeAndSamityEntityByMemberId(String memberId);

    @Query("SELECT * FROM template.loan_adjustment_data lad WHERE lad.loan_account_id = :loanAccountId FETCH 1 ROWS ONLY;")
    Mono<LoanAdjustmentDataEntity> findFirstLoanAdjustmentDataByLoanAccountId(String loanAccountId);

    @Query("SELECT * FROM template.collection_staging_data csd WHERE csd.loan_account_id = :loanAccountId FETCH 1 ROWS ONLY;")
    Mono<CollectionStagingDataEntity> findFirstCollectionPaymentDataByLoanAccountId(String loanAccountId);

    @Query("SELECT * FROM template.collection_staging_data csd WHERE csd.savings_account_id = :savingsAccountId FETCH 1 ROWS ONLY;")
    Mono<CollectionStagingDataEntity> findFirstCollectionPaymentDataBySavingsAccountId(String savingsAccountId);

    @Query("SELECT DISTINCT office_id  FROM template.office o WHERE status = 'Active';")
    Flux<String> getActiveOfficeIds();

    @Query("""
    select mpt.management_process_id 
    from template.management_process_tracker mpt 
    where mpt.office_id in 
        (select msopm.office_id 
        from template.mem_smt_off_pri_map msopm 
        inner join template.loan_account la 
        on msopm.member_id = la.member_id 
        where la.loan_account_id = :loanAccountId
        and msopm.status = 'Active') 
    order by mpt.business_date desc FETCH 1 ROWS ONLY;
    """)
    Mono<String> getManagementProcessIdByLoanAccountId(String loanAccountId);

    @Query("select * from template.loan_product lp where lp.loan_type_id = :loanTypeId;")
    Flux<LoanProductEntity> getAllLoanProductByLoanTypeId(String loanTypeId);

    @Query("select * from template.loan_product lp where loan_type_id in ('M', 'S');")
    Flux<LoanProductEntity> getAllRegularLoanProducts();

    @Query("""
    select * from 
    template.member m inner join 
    template.mem_smt_off_pri_map msopm 
    on msopm.member_id =m.member_id 
    where msopm.office_id = :officeId
    and msopm.status = 'Active';
    """)
    Flux<MemberEntity> getAllMemberEntityByOfficeId(String officeId);

    @Query("""
    select * from template.samity s 
    inner join template.mem_smt_off_pri_map msopm 
    on s.samity_id = msopm.samity_id 
    where msopm.member_id =:memberId
    and msopm.status = 'Active';
    """)
    Mono<Samity> getSamityByMemberId(String memberId);

    @Query("""
    SELECT s.samity_id, o.office_id, mpt.management_process_id
    FROM template.mem_smt_off_pri_map msopm
    INNER JOIN template.office o ON o.office_id = msopm.office_id
    INNER JOIN template.samity s ON s.samity_id = msopm.samity_id
    INNER JOIN template.savings_account sa ON sa.member_id = msopm.member_id
    INNER JOIN (
        SELECT mpt1.management_process_id, mpt1.office_id
        FROM template.management_process_tracker mpt1
        INNER JOIN (
            SELECT office_id, MAX(created_on) AS max_created_on
            FROM template.management_process_tracker
            GROUP BY office_id
        ) mpt2 ON mpt1.office_id = mpt2.office_id AND mpt1.created_on = mpt2.max_created_on
    ) mpt ON mpt.office_id = msopm.office_id
    WHERE sa.savings_account_id = :savingsAccountId
    AND msopm.status = 'Active';
    """)
    Mono<MemberSamityOfficeEntity> getMemberSamityOfficeInfoBySavingsAccountId(String savingsAccountId);


    @Query("""
    SELECT o.office_id, lp.loan_type_id, lp.monthly_repay_day, s.samity_day, s.samity_id, la.status as loan_account_status
    FROM template.loan_account la
    INNER JOIN template.mem_smt_off_pri_map msopm ON la.member_id = msopm.member_id
    INNER JOIN template.office o ON o.office_id = msopm.office_id
    INNER join template.samity s ON s.samity_id = msopm.samity_id
    INNER JOIN template.loan_product lp ON la.loan_product_id = lp.loan_product_id
    WHERE la.loan_account_id = :loanAccountId
    AND msopm.status = 'Active';
    """)
    Mono<MemberSamityOfficeEntity> getMemberSamityOfficeInfoByLoanAccountId(String loanAccountId);
}
