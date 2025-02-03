package net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.ResultSet;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.queries.helpers.dto.PassbookGridViewDataDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PassbookRepository extends ReactiveCrudRepository<PassbookEntity, String> {

    @Query("""
            select max(p.created_on)
            from passbook p
            where transaction_code = :transactionCode
            and loan_account_id = :loanAccountId;
            """)
    LocalDate getLastCreatedDate(String transactionCode, String loanAccountId);

    @Query("""
            select * from passbook p
            where loan_account_id = :loanAccountId
            order by install_no desc, created_on desc
            limit 1;
            """)
    Mono<PassbookEntity> getLastPassbookEntry(String loanAccountId);

    @Query("""
            select loan_repay_schedule_id , install_no , install_date , installment_begin_balance , prin_remain , sc_remain , installment_end_balance , total_loan_paid_till_date , total_loan_pay_remain, prin_paid_till_date , sc_paid_till_date
            from passbook p
            where loan_account_id = :loanAccountId
            and transaction_code = :transactionCode
            order by created_on desc
            limit 1;
            """)
    Mono<ResultSet> getResultSetFromPassbook(String loanAccountId, String transactionCode);

    @Query("""
                    INSERT INTO passbook (
                      transaction_id,
                      transaction_code,
                      member_id,
                      loan_account_id,
                      loan_repay_schedule_id,
                      install_no,
                      install_date,
                      installment_begin_balance,
                      prin_paid,
                      prin_paid_till_date,
                      prin_remain,
                      service_charge_paid,
                      sc_paid_till_date,
                      sc_remain,
                      installment_end_balance,
                      total_loan_paid_till_date,
                      total_loan_pay_remain,
                      posted_on,
                      created_on,
                      created_by,
                      updated_on,
                      updated_by,
                      status,
                      mfi_id,
                      lump_sump_payment,
                      deposit_account_id,
                      accrued_inter_deposit_id,
                      deposit_acct_begin_balance,
                      deposit_acct_ending_balance
                    ) VALUES (
                      passbookEntity.transactionId,
                      passbookEntity.transactionCode,
                      passbookEntity.memberId,
                      passbookEntity.loanAccountId,
                      passbookEntity.loanRepayScheduleId,
                      passbookEntity.installNo,
                      passbookEntity.installDate,
                      passbookEntity.installmentBeginBalance,
                      passbookEntity.prinPaid,
                      passbookEntity.prinPaidTillDate,
                      passbookEntity.prinRemain,
                      passbookEntity.serviceChargePaid,
                      passbookEntity.scPaidTillDate,
                      passbookEntity.scRemain,
                      passbookEntity.installmentEndBalance,
                      passbookEntity.totalLoanPaidTillDate,
                      passbookEntity.totalLoanPayRemain,
                      passbookEntity.postedOn,
                      passbookEntity.createdOn,
                      passbookEntity.createdBy,
                      passbookEntity.updatedOn,
                      passbookEntity.updatedBy,
                      passbookEntity.status,
                      passbookEntity.mfiId,
                      passbookEntity.lumpSumpPayment,
                      passbookEntity.depositAccountId,
                      passbookEntity.accruedInterDepositId,
                      passbookEntity.depositAcctBeginBalance,
                      passbookEntity.depositAcctEndingBalance
                    );
            """)
    Mono<PassbookEntity> insertRecordPassbook(PassbookEntity passbookEntity);

/*    @Query("""
            select * from passbook p
            where savings_account_id = :savingsAccountId
            order by transaction_date desc, created_on desc
            limit 1;
            """)*/
    @Query("""
                SELECT * FROM PASSBOOK P
                WHERE SAVINGS_ACCOUNT_ID = :savingsAccountId
                ORDER BY TRANSACTION_DATE DESC, CREATED_ON DESC
                FETCH FIRST ROW ONLY;
                """)
    Mono<PassbookEntity> getLastPassbookEntryBySavingsAccountId(String savingsAccountId);

/*    @Query("""
            select * from passbook p
            where savings_account_oid = :savingsAccountOid
            order by created_on desc
            limit 1;
            """)*/
    @Query("""
            SELECT * FROM PASSBOOK P
            WHERE SAVINGS_ACCOUNT_OID = :savingsAccountOid
            ORDER BY CREATED_ON DESC
            FETCH FIRST ROW ONLY;
            """)
    Mono<PassbookEntity> getLastPassbookEntryBySavingsAccountOid(String savingsAccountOid);

    @Query("""
            select X.*, Y.passbook_count from
                (select s.samity_id, count(msopm.*) as total_member
                from mem_smt_off_pri_map msopm
                    join samity s
                        on s.samity_id = msopm.samity_id
                    WHERE msopm.status = 'Active'
                group by s.samity_id) X
            left join
                (select s.samity_id, count(t.*) as passbook_count
                from "passbook" t
                    join mem_smt_off_pri_map msopm
                        on t.member_id = msopm.member_id
                    join samity s
                        on s.samity_id = msopm.samity_id
                and t.transaction_date between :FROM_DATE and :TO_DATE
                and msopm.status = 'Active'
                group by s.samity_id) Y
            on X.samity_id = Y.samity_id;
            """)
    Flux<PassbookGridViewDataDTO> getPassbookCountGroupBySamityId(@Param("FROM_DATE") LocalDate fromDate, @Param("TO_DATE") LocalDate toDate);

    @Query("""
            select *
            from "passbook" t 
                join mem_smt_off_pri_map msopm
                    on t.member_id = msopm.member_id
                join samity s
                    on s.samity_id = msopm.samity_id
            where s.samity_id = :SAMITY_ID
            and t.transaction_date between :FROM_DATE and :TO_DATE
            and msopm.status = 'Active'
            and 
                case 
                    when (:ACCOUNT_NO is not null and :ACCOUNT_NO != '') then (t.loan_account_id = :ACCOUNT_NO or t.savings_account_id = :ACCOUNT_NO)
                    else 1 = 1
                end
            and 
                case 
                    when (:SEARCH_TEXT is not null and :SEARCH_TEXT != '') then (t.transaction_code ilike :SEARCH_TEXT)
                    else 1 = 1 
                end;
            """)
    Flux<PassbookEntity> getPassbookReportDataBySamityIdAndTransactionDateBetweenFromDateAndToDate(@Param("FROM_DATE") LocalDate fromDate,
                                                                                                   @Param("TO_DATE") LocalDate toDate,
                                                                                                   @Param("SAMITY_ID") String samityId,
                                                                                                   @Param("ACCOUNT_NO") String accountNo,
                                                                                                   @Param("SEARCH_TEXT") String searchText);

    @Query("""
            select *
            from "passbook" t
            join mem_smt_off_pri_map msopm
            on t.member_id = msopm.member_id
            join samity s
            on s.samity_id = msopm.samity_id
            where s.samity_id = :SAMITY_ID
            and t.transaction_date between :FROM_DATE and :TO_DATE
            and msopm.status = 'Active'
            and
                case
                    when (:ACCOUNT_NO is not null and :ACCOUNT_NO != '')
                        then (t.loan_account_oid = (select l."oid" from "loan_account" l where l.loan_account_id = :ACCOUNT_NO)
                            or loan_account_id = :ACCOUNT_NO
                            or t.savings_account_oid = (select l."oid" from "savings_account" l where l.savings_account_id = :ACCOUNT_NO)
                            or savings_account_id = :ACCOUNT_NO)
                    else 1 = 1
                end
            and
                case
                    when (:SEARCH_TEXT is not null and :SEARCH_TEXT != '') then (t.transaction_code ilike :SEARCH_TEXT)
                    else 1 = 1
                end;
            """)
    Flux<PassbookEntity> getPassbookReportV2DataBySamityIdAndTransactionDateBetweenFromDateAndToDate(@Param("FROM_DATE") LocalDate fromDate,
                                                                                                   @Param("TO_DATE") LocalDate toDate,
                                                                                                   @Param("SAMITY_ID") String samityId,
                                                                                                   @Param("ACCOUNT_NO") String accountNo,
                                                                                                   @Param("SEARCH_TEXT") String searchText);



    @Query("""
            select *
            from samity s
            where s.samity_id = :SAMITY_ID;
            """)
    Mono<Samity> getSamityForPassbookReport(@Param("SAMITY_ID") String samityId);

    @Query("""
            select * from passbook p
            WHERE DATE(transaction_date) = :transactionDate::date
            AND savings_account_id = :savingsAccountId
            ORDER BY created_on;
            """)
    Flux<PassbookEntity> findPassbookEntitiesBySavingsAccountIdAndTransactionDateOrderByCreatedOn(String savingsAccountId, LocalDate transactionDate);

    @Query("""
                SELECT p.loan_repay_schedule_id FROM passbook p WHERE p.loan_repay_schedule_id IS NOT NULL AND p.transaction_id IN (:transactionIdList);
            """)
    Flux<String> getRepayScheduleIdListByTransactionList(List<String> transactionIdList);

    @Query("""
                SELECT * FROM passbook p WHERE p.transaction_id IN (:transactionIdList);
            """)
    Flux<PassbookEntity> getPassbookEntitiesByTransactionIdList(List<String> transactionIdList);

    @Query("""
    SELECT * FROM passbook P
    WHERE EXTRACT(YEAR FROM transaction_date) = :yearValue
    AND EXTRACT(MONTH FROM transaction_date) = :monthValue
    AND savings_account_oid = :savingsAccountOid
    ORDER BY transaction_date, created_on;
    """)
    Flux<PassbookEntity> getPassbookEntitiesByYearMonthAndSavingsAccountOid(Integer yearValue, Integer monthValue, String savingsAccountOid);

    Flux<PassbookEntity> getPassbookEntitiesByManagementProcessIdAndLoanAccountIdIsNotNull(String managementProcessId);
    Flux<PassbookEntity> getPassbookEntitiesByLoanAccountIdIsNotNull();
    Flux<PassbookEntity> getPassbookEntitiesByManagementProcessIdAndSavingsAccountIdIsNotNull(String managementProcessId);
    
    Flux<PassbookEntity> getPassbookEntitiesBySavingsAccountIdIsNotNull();
    Flux<PassbookEntity> getPassbookEntitiesByManagementProcessIdAndPaymentMode(String managementProcessId, String paymentMode);
    Flux<PassbookEntity> getPassbookEntitiesByPaymentMode(String paymentMode);
    Mono<PassbookEntity> getPassbookEntityByDisbursedLoanAccountId(String disbursedLoanAccountId);
    Flux<PassbookEntity> getPassbookEntitiesByManagementProcessIdAndPaymentModeAndWithdrawAmountIsNotNull(String managementProcessId, String paymentMode);
    Flux<PassbookEntity> getPassbookEntitiesByManagementProcessIdAndWithdrawAmountIsNotNull(String managementProcessId);
    Mono<PassbookEntity> getFirstBySavingsAccountOidAndTotalAccruedInterDepositIsNotNullOrderByTransactionDateDesc(String savingsAccountOid);
    Mono<PassbookEntity> getFirstBySavingsAccountOidAndWithdrawAmountIsNotNullOrderByTransactionDateDesc(String savingsAccountOid);

    Flux<PassbookEntity> getAllByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
    Flux<PassbookEntity> getPassbookEntitiesBySavingsAccountOidAndTransactionDateIsBetween(String savingsAccountOid, LocalDate fromDate, LocalDate toDate);
    Flux<PassbookEntity> findAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndPaymentModeOrPaymentModeIsNull(String managementProcessId, String transactionCode, String paymentMode);

    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndPaymentModeIsNull(String managementProcessId, String transactionCode);
    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode);
    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndSavingsTypeIdAndSavingsTypeIdNotNull(String managementProcessId, String transactionCode, String savingsTypeId);

    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndSavingsTypeIdOrSavingsTypeIdIsNullAndPaymentMode(String managementProcessId, String transactionCode, String savingsTypeId, String paymentMode);
    @Query("""
    SELECT *
    FROM passbook p
    WHERE transaction_code = :transactionCode
    AND management_process_id = :managementProcessId
    AND (payment_mode = :paymentMode OR :paymentMode IS NULL);
    """)
    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode);

    Flux<PassbookEntity> getPassbookEntitiesByTransactionCodeAndLoanAccountOid(String transactionCode, String loanAccountOid);
    Flux<PassbookEntity> getPassbookEntitiesByTransactionCodeAndSavingsAccountOid(String transactionCode, String savingsAccountOid);

    Flux<PassbookEntity> findAllByManagementProcessIdAndTransactionCodeAndSavingsAccountIdIn(String managementProcessId, String transactionCode, List<String> savingsAccountIdList);

    Flux<PassbookEntity> findAllByOfficeIdAndInstallDateEqualsAndTransactionDateIsBeforeAndLoanAccountIdIsNotNull(String officeId, LocalDate businessDate, LocalDate businessDate1);
    Flux<PassbookEntity> findAllByOfficeIdAndInstallDateIsAfterAndTransactionDateEqualsAndLoanAccountIdIsNotNull(String officeId, LocalDate businessDate, LocalDate businessDate1);

    Flux<PassbookEntity> findAllByOfficeIdAndInstallDateEqualsAndLoanAccountIdIsNotNull(String officeId, LocalDate businessDate);
    Flux<PassbookEntity> findAllByOfficeIdAndInstallDateIsBeforeAndTransactionDateEqualsAndLoanAccountIdIsNotNull(String officeId, LocalDate businessDate, LocalDate businessDate1);

    Flux<PassbookEntity> findAllByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId);
    Flux<PassbookEntity> findAllByLoanAccountIdAndTransactionDateIsGreaterThanEqual(String loanAccountId, LocalDate transactionDate);
    Flux<PassbookEntity> findAllBySavingsAccountIdAndTransactionDateIsGreaterThanEqual(String loanAccountId, LocalDate transactionDate);

    @Query("""
    select * from passbook p
    where loan_account_oid = :loanAccountOid
    and transaction_code in (:transactionCodes)
    order by created_on desc , install_no desc
    limit 1;
    """)
    Mono<PassbookEntity> getLastPassbookEntryByLoanAccountOidAndTransactionCodes(String loanAccountOid, List<String> transactionCodes);

    Flux<PassbookEntity> findAllByTransactionId(String transactionId);
}
