package net.celloscope.mraims.loanportfolio.features.accounting.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.AccountingUseCase;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.AutoVoucherJournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisJournalClientPort;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisMetaDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaData;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestForFeeCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherEnum;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.dto.request.FeeCollectionUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterest;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.in.TransactionAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.response.TransactionAdjustmentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Service
@Slf4j
public class AccountingService implements AccountingUseCase {
    private final AisMetaDataPersistencePort aisMetaDataPersistencePort;
    private final PassbookUseCase passbookUseCase;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final LoanAccountUseCase loanAccountUseCase;
    private final CommonRepository commonRepository;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final AisJournalClientPort aisJournalClientPort;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final CalendarUseCase calendarUseCase;
    private final AccruedInterestUseCase accruedInterestUseCase;
    private final AutoVoucherUseCase autoVoucherUseCase;
    private final FeeCollectionUseCase feeCollectionUseCase;
    private final TransactionAdjustmentUseCase transactionAdjustmentUseCase;

    public AccountingService(AisMetaDataPersistencePort aisMetaDataPersistencePort,
                             PassbookUseCase passbookUseCase, ISavingsAccountUseCase savingsAccountUseCase,
                             LoanAccountUseCase loanAccountUseCase, CommonRepository commonRepository,
                             ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                             AisJournalClientPort aisJournalClientPort,
                             LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase,
                             CalendarUseCase calendarUseCase,
                             AccruedInterestUseCase accruedInterestUseCase,
                             AutoVoucherUseCase autoVoucherUseCase,
                             FeeCollectionUseCase feeCollectionUseCase,
                             TransactionAdjustmentUseCase transactionAdjustmentUseCase) {
        this.aisMetaDataPersistencePort = aisMetaDataPersistencePort;
        this.passbookUseCase = passbookUseCase;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.loanAccountUseCase = loanAccountUseCase;
        this.commonRepository = commonRepository;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.aisJournalClientPort = aisJournalClientPort;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.calendarUseCase = calendarUseCase;
        this.accruedInterestUseCase = accruedInterestUseCase;
        this.autoVoucherUseCase = autoVoucherUseCase;
        this.feeCollectionUseCase = feeCollectionUseCase;
        this.transactionAdjustmentUseCase = transactionAdjustmentUseCase;
    }

    @Override
    public Mono<AisResponse> getAccountingJournalBody(AccountingRequestDTO requestDTO) {
        String processName = requestDTO.getProcessName();

        return aisMetaDataPersistencePort
                .getAisMetaDataByProcessName(processName)
                .doOnNext(aisMetaData -> log.info("aisMetaData received : {}", aisMetaData))
                .flatMap(aisMetaData ->
                        getTransactionInfoFluxAccordingToProcessName(aisMetaData, requestDTO)
                                .flatMap(object -> getProductAmountMapForEachEntity(object, aisMetaData)
                                        .reduce(new HashMap<String, BigDecimal>(), (accumulatedMap, mapFromFlux) -> {
                                            mapFromFlux.forEach((key, value) -> accumulatedMap.merge(key, value, BigDecimal::add));
                                            return accumulatedMap;})
                                        .map(finalProductAmountMap -> Tuples.of(finalProductAmountMap, aisMetaData, object))))
                .flatMap(tupleOfMapAisMetaDataAndObject -> getJournalList(tupleOfMapAisMetaDataAndObject,
                        requestDTO))
                .reduce(new ArrayList<Journal>(), (accumulator, nextList) -> {
                    accumulator.addAll(nextList);
                    return accumulator;
                })
                .flatMap(finalJournalList -> getJournalRequestDTO(finalJournalList, processName, requestDTO))
                .doOnNext(journalRequestDTO -> log.info("journal request : {}", journalRequestDTO))
                .filter(journalRequestDTO -> !journalRequestDTO.getJournalList().isEmpty()
                        && !(journalRequestDTO.getAmount().compareTo(BigDecimal.ZERO) == 0))
                .flatMap(aisJournalClientPort::postAccounting)
                .doOnRequest(l -> log.info("Request sent to webclient for accounting posting from service"));
    }


    public <T> Flux<T> getTransactionInfoFluxAccordingToProcessName(AisMetaData aisMetaData, AccountingRequestDTO requestDTO) {
        Flux<T> flux = null;
        if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_INTEREST_ACCRUAL.getValue())) {
            flux = getTransactionInfoFluxForProcessNameInterestAccrual(aisMetaData, requestDTO);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_SC_PROVISION.getValue())) {
            flux = getTransactionInfoFluxForProcessNameServiceChargeProvision(requestDTO);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION_NO_ADVANCE.getValue())) {
            flux = getTransactionInfoFluxForProcessNameLoanCollectionNoAdvance(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Loan Collection");
            flux = getTransactionInfoFluxForProcessNameLoanCollection(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_LOAN_REPAY.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Reverse Loan Repay");
            flux = getTransactionInfoFluxForProcessNameReverseLoanRepay(requestDTO);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_DEPOSIT.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Reverse Savings Deposit");
            flux = getTransactionInfoFluxForProcessNameReverseSavingsDeposit(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_WITHDRAW.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Reverse Savings Withdraw");
            flux = getTransactionInfoFluxForProcessNameReverseSavingsWithdraw(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_LOAN_REPAY.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Adjustment Loan Repay");
            flux = getTransactionInfoFluxForProcessNameAdjustmentLoanRepay(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_DEPOSIT.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Adjustment Savings Deposit");
            flux = getTransactionInfoFluxForProcessNameAdjustmentSavingsDeposit(requestDTO, aisMetaData);
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_WITHDRAW.getValue())) {
            log.info("Getting Transaction Info Flux for Process Name Adjustment Savings Withdraw");
            flux = getTransactionInfoFluxForProcessNameAdjustmentSavingsWithdraw(requestDTO, aisMetaData);
        }
        else
            flux = getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        return flux;
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameAdjustmentSavingsWithdraw(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())) {
            return getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_COMPULSORY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_VOLUNTARY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_TERM_DEPOSIT.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_FIXED_DEPOSIT_PAYABLE.getValue())) {
            return passbookUseCase.getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(requestDTO.getManagementProcessId(), TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue(), aisMetaData.getSavingsTypeId())
                    .doOnRequest(l -> log.info("Request sent to webclient for getting passbook entries for Adjustment Savings Withdraw {},{}, {}",
                            requestDTO.getManagementProcessId(), TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue(), aisMetaData.getSavingsTypeId()))
                    .doOnNext(passbookResponseDTO -> log.info("Passbook Response for Adjustment Savings Withdraw : {}", passbookResponseDTO))
                    .switchIfEmpty(Flux.empty())
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else {
            return Flux.empty();
        }
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameAdjustmentSavingsDeposit(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())) {
            return getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_COMPULSORY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_VOLUNTARY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_TERM_DEPOSIT.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_FIXED_DEPOSIT_PAYABLE.getValue())) {
            return passbookUseCase.getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(requestDTO.getManagementProcessId(), TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue(), aisMetaData.getSavingsTypeId())
                    .doOnRequest(l -> log.info("Request sent to webclient for getting passbook entries for Adjustment Savings Deposit {},{}, {}",
                            requestDTO.getManagementProcessId(), TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue(), aisMetaData.getSavingsTypeId()))
                    .doOnNext(passbookResponseDTO -> log.info("Passbook Response for Adjustment Savings Deposit : {}", passbookResponseDTO))
                    .switchIfEmpty(Flux.empty())
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else {
            return Flux.empty();
        }
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameAdjustmentLoanRepay(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())) {
            return getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PRINCIPAL_OUTSTANDING.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_INCOME.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING.getValue())) {
            return passbookUseCase.getPassbookEntriesForPrincipalAndServiceChargeOutstandingV2(requestDTO.getManagementProcessId())
                    .filter(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equalsIgnoreCase(Constants.TRANSACTION_CODE_ADJUSTMENT_LOAN_REPAY.getValue()))
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else {
            return Flux.empty();
        }
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameInterestAccrual(AisMetaData aisMetaData, AccountingRequestDTO requestDTO) {
        return accruedInterestUseCase
                .getAccruedInterestEntriesByManagementProcessIdAndOfficeId(requestDTO.getManagementProcessId(), requestDTO.getOfficeId())
                .doOnNext(accruedInterestResponseDTO -> log.info("Accrued Interest Response Size : {}", accruedInterestResponseDTO.getData().size()))
                .map(AccruedInterestResponseDTO::getData)
                .flatMapMany(Flux::fromIterable)
                .flatMap(accruedInterest -> {
                    if (!HelperUtil.checkIfNullOrEmpty(aisMetaData.getSavingsTypeId()) && !HelperUtil.checkIfNullOrEmpty(accruedInterest.getSavingsAccountId())) {
                        return commonRepository.getSavingsTypeIdBySavingsAccountId(accruedInterest.getSavingsAccountId())
                                .map(savingsTypeIdFromDb -> {
                                    if (savingsTypeIdFromDb.equals(aisMetaData.getSavingsTypeId())) {
                                        return accruedInterest;
                                    } else {
                                        return AccruedInterest.builder().build();
                                    }
                                });
                    } else {
                        return Flux.just(accruedInterest);
                    }
                })
                .filter(accruedInterest -> !HelperUtil.checkIfNullOrEmpty(accruedInterest.getAccruedInterestId()))
                .collectList()
                .doOnNext(accruedInterests -> log.info("Accrued Interest Response Size with SavingsTypeId: {} : {}", aisMetaData.getSavingsTypeId(), accruedInterests.size()))
                .flatMapMany(Flux::fromIterable)
                .map(accruedInterest -> (T) accruedInterest);
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameServiceChargeProvision(AccountingRequestDTO requestDTO) {
        return managementProcessTrackerUseCase
                .getCurrentBusinessDateForOffice(requestDTO.getManagementProcessId(), requestDTO.getOfficeId())
                .flatMap(currentBusinessDate -> calendarUseCase.getNextBusinessDateForOffice(requestDTO.getOfficeId(), currentBusinessDate))
                .flatMapMany(installmentDate -> loanRepaymentScheduleUseCase
                        .getUnprovisionedRepaymentSchedulesByInstallmentDate(installmentDate, requestDTO.getOfficeId())
                        .collectList()
                        .doOnNext(repaymentScheduleResponseDTOS -> log.info("total SC for Provision in Accounting service : {}", repaymentScheduleResponseDTOS.stream().map(RepaymentScheduleResponseDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add)))
                        .flatMapMany(Flux::fromIterable))
                .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameReverseSavingsWithdraw(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
            if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())){
                return getTransactionAdjustmentForCashOrBank(requestDTO.getManagementProcessId(), TRANSACTION_CODE_REVERSE_SAVINGS_WITHDRAW.getValue(), aisMetaData.getPaymentMode())
                        .map(transactionAdjustment -> (T) transactionAdjustment);
            } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_COMPULSORY_SAVINGS.getValue())
                    || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_VOLUNTARY_SAVINGS.getValue())
                    || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_TERM_DEPOSIT.getValue())
                    || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_FIXED_DEPOSIT_PAYABLE.getValue())) {
                return getTransactionAdjustmentAccordingToLedgerKey(requestDTO.getManagementProcessId(), TRANSACTION_CODE_REVERSE_SAVINGS_WITHDRAW.getValue(), aisMetaData.getSavingsTypeId())
                        .map(transactionAdjustment -> (T) transactionAdjustment);
            } else {
                return Flux.empty();
            }
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameReverseSavingsDeposit(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())){
            return getTransactionAdjustmentForCashOrBank(requestDTO.getManagementProcessId(), TRANSACTION_CODE_REVERSE_SAVINGS_DEPOSIT.getValue(), aisMetaData.getPaymentMode())
                    .map(transactionAdjustment -> (T) transactionAdjustment);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_COMPULSORY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_VOLUNTARY_SAVINGS.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_TERM_DEPOSIT.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_FIXED_DEPOSIT_PAYABLE.getValue())) {
            return getTransactionAdjustmentAccordingToLedgerKey(requestDTO.getManagementProcessId(), TRANSACTION_CODE_REVERSE_SAVINGS_DEPOSIT.getValue(), aisMetaData.getSavingsTypeId())
                    .map(transactionAdjustment -> (T) transactionAdjustment);
        } else {
            return Flux.empty();
        }
    }

    private Flux<TransactionAdjustment> getTransactionAdjustmentForCashOrBank(String managementProcessId, String transactionCode, String paymentMode) {
        return transactionAdjustmentUseCase.getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndPaymentMode(managementProcessId, transactionCode, paymentMode)
                .map(TransactionAdjustmentResponseDto::getData);
    }

    private Flux<TransactionAdjustment> getTransactionAdjustmentAccordingToLedgerKey(String managementProcessId, String transactionCode, String savingsTypeId) {
        return transactionAdjustmentUseCase.getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndSavingsType(managementProcessId, transactionCode, savingsTypeId)
                .map(TransactionAdjustmentResponseDto::getData);
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameReverseLoanRepay(AccountingRequestDTO requestDTO) {
            return transactionAdjustmentUseCase.getTransactionAdjustmentsByManagementProcessIdAndTransactionCode(requestDTO.getManagementProcessId(), TRANSACTION_CODE_REVERSE_LOAN_REPAY.getValue())
                    .map(TransactionAdjustmentResponseDto::getData)
                    .doOnNext(transactionAdjustments -> log.info("Transaction Adjustments for Reverse Loan Repay : {}", transactionAdjustments))
                    .map(transactionAdjustment -> (T) transactionAdjustment);
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameLoanCollection(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        Mono<LocalDate> businessDateMono = managementProcessTrackerUseCase.getCurrentBusinessDateForOffice(requestDTO.getManagementProcessId(), requestDTO.getOfficeId());
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())) {
            return getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PRINCIPAL_OUTSTANDING.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING.getValue())) {
            log.info("Getting Principal and Service Charge Outstanding for Loan Collection");
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                    .getPassbookEntriesForPrincipalAndServiceChargeOutstanding(requestDTO.getManagementProcessId(), businessDate))
                    .filter(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TransactionCodes.LOAN_REPAY.getValue())
                                            || passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TransactionCodes.LOAN_REBATE.getValue()))
                    .collectList()
                    .doOnNext(passbookResponseDTOS -> log.info("Total Principal and Service Charge Outstanding for Loan Collection : {}", passbookResponseDTOS.size()))
                    .flatMapIterable(passbookResponseDTOS -> passbookResponseDTOS)
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT.getValue())
                && aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_DEBIT.getValue())) {
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                    .getPassbookEntriesForAdvanceLoanRepaymentDebit(requestDTO.getOfficeId(), businessDate))
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT.getValue())
                && aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_CREDIT.getValue())) {
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                            .getPassbookEntriesForAdvanceLoanRepaymentCredit(requestDTO.getOfficeId(), businessDate))
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        }else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_PRINCIPAL.getValue())
                && aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_CREDIT.getValue())) {
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                            .getPassbookEntriesForAdvanceLoanRepaymentDebit(requestDTO.getOfficeId(), businessDate))
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        }else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_SERVICE_CHARGE.getValue())
                && aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_CREDIT.getValue())) {
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                            .getPassbookEntriesForAdvanceLoanRepaymentDebit(requestDTO.getOfficeId(), businessDate))
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        } else {
            return Flux.empty();
        }
    }

    private <T> Flux<T> getTransactionInfoFluxForProcessNameLoanCollectionNoAdvance(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        Mono<LocalDate> businessDateMono = managementProcessTrackerUseCase.getCurrentBusinessDateForOffice(requestDTO.getManagementProcessId(), requestDTO.getOfficeId());
        if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())) {
            return getPassbookEntries(requestDTO, aisMetaData)
                    .map(passbookResponseDTO -> (T) passbookResponseDTO);
        } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PRINCIPAL_OUTSTANDING.getValue())
                || aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_INCOME.getValue())) {
            return businessDateMono
                    .flatMapMany(businessDate -> passbookUseCase
                            .getPassbookEntriesForPrincipalAndServiceChargeOutstandingV2(requestDTO.getManagementProcessId()))
                    .filter(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TransactionCodes.LOAN_REPAY.getValue())
                            || passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TransactionCodes.LOAN_REBATE.getValue()))
                    .map(repaymentScheduleResponseDTO -> (T) repaymentScheduleResponseDTO);
        } else {
            return Flux.empty();
        }
    }

    private <T> Flux<Map<String, BigDecimal>> getProductAmountMapForEachEntity(T object, AisMetaData aisMetaData) {
        Flux<Map<String, BigDecimal>> mapFlux = null;

//        log.info("Getting ProductAmount Map For Ledger key : {}", aisMetaData.getLedgerKey());
        BigDecimal amount = aisMetaData.getFieldNames()
                .stream()
                .map(fieldName -> CommonFunctions.getFieldValueByObjectAndFieldName(object,
                        fieldName) == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(Double.parseDouble(CommonFunctions
                        .getFieldValueByObjectAndFieldName(object, fieldName))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_INTEREST_ACCRUAL.getValue())) {
            AccruedInterest accruedInterest = (AccruedInterest) object;
            mapFlux = savingsAccountUseCase
                    .getProductIdBySavingsAccountId(accruedInterest.getSavingsAccountId())
                    .flatMapMany(productId -> Flux.just(Map.of(productId, amount)));

        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_SC_PROVISION.getValue())) {
//            @TODO: Test
            RepaymentScheduleResponseDTO repaymentScheduleResponseDTO = (RepaymentScheduleResponseDTO) object;
            mapFlux = loanAccountUseCase.getProductIdByLoanAccountId(repaymentScheduleResponseDTO.getLoanAccountId())
                    .flatMapMany(productId -> Flux.just(Map.of(productId, amount)));

        } else if (aisMetaData.getIsAggregated().equalsIgnoreCase(AisMetaDataEnum.YES.getValue())) {
            return Flux.just(Map.of(aisMetaData.getLedgerKey(), amount));
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_LOAN_REPAY.getValue())) {
            TransactionAdjustment transactionAdjustment = (TransactionAdjustment) object;
            mapFlux = Flux.just(Map.of(transactionAdjustment.getLoanProductId(), amount));
        } else if (aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_DEPOSIT.getValue())
                || aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_WITHDRAW.getValue())) {
            TransactionAdjustment transactionAdjustment = (TransactionAdjustment) object;
            mapFlux = Flux.just(Map.of(transactionAdjustment.getSavingsProductId(), amount));
        } else {
            PassbookResponseDTO passbookResponseDTO = (PassbookResponseDTO) object;

            passbookResponseDTO.setLoanAccountId(passbookResponseDTO.getLoanAccountId() == null ? passbookResponseDTO.getDisbursedLoanAccountId() : passbookResponseDTO.getLoanAccountId());

            if(aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_WELFARE_FUND.getValue())){
                passbookResponseDTO.setLoanAccountId(passbookResponseDTO.getWelfareFundLoanAccountId());
            }

            log.info("Passbook Loan Account Id:{}, Savings AccountId: {}", passbookResponseDTO.getLoanAccountId(), passbookResponseDTO.getSavingsAccountId());
            if (passbookResponseDTO.getSavingsAccountId() != null) {
                log.info("Savings Account Id: {}, Ledger Key: {}", passbookResponseDTO.getSavingsAccountId(), aisMetaData.getLedgerKey());
                mapFlux = savingsAccountUseCase
                        .getProductIdBySavingsAccountId(passbookResponseDTO.getSavingsAccountId())
                        .flatMapMany(productId -> Flux.just(Map.of(productId, amount)));
            } else if (passbookResponseDTO.getLoanAccountId() != null) {
                mapFlux = loanAccountUseCase
                        .getProductIdByLoanAccountId(passbookResponseDTO.getLoanAccountId())
                        .flatMapMany(productId -> Flux.just(Map.of(productId, amount)));
            }
        }
        return mapFlux;
    }

    @Override
    public Mono<JournalRequestDTO> getAccountingJournalRequestBody(AccountingRequestDTO requestDTO) {
        String processName = requestDTO.getProcessName();

        if (requestDTO.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_FEE_COLLECTION.getValue())) {
            return this.buildFeeCollectionAISRequestFromAutoVoucher(requestDTO)
                    .switchIfEmpty(Mono.just(JournalRequestDTO
                            .builder()
                                    .description(AisMetaDataEnum.PROCESS_NAME_FEE_COLLECTION.getValue())
                                    .amount(BigDecimal.ZERO)
                                    .journalType(requestDTO.getProcessName())
                                    .referenceNo(requestDTO.getBusinessDate())
                                    .journalProcess(AisMetaDataEnum.JOURNAL_PROCESS_AUTO.getValue())
                                    .officeId(requestDTO.getOfficeId())
                                    .mfiId(requestDTO.getMfiId())
                                    .createdBy(requestDTO.getLoginId())
                                    .journalList(new ArrayList<>())
                            .build()))
                    .doOnError(throwable -> log.info("Error Happened while processing FEE COLLECTION accounting. {}", throwable.getMessage()))
                    .onErrorResume(throwable -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Error Happened while processing FEE COLLECTION accounting. " + throwable.getMessage())));
        }

        return aisMetaDataPersistencePort.getAisMetaDataByProcessName(processName)
                .doOnNext(aisMetaData -> log.info("aisMetaData received : {}", aisMetaData))
                .concatMap(aisMetaData ->
                                getTransactionInfoFluxAccordingToProcessName(aisMetaData, requestDTO)
                                        .doOnError(throwable -> log.error("Error happened while getting Transaction info according to process name. {}", throwable.getMessage()))
                                        .flatMap(object -> getProductAmountMapForEachEntity(object, aisMetaData)
                                                .filter(map -> map.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(BigDecimal.ZERO) > 0)
                                                .doOnError(throwable -> log.error("Error happened while getting Product Amount Map for Each entity. {}", throwable.getMessage()))
                                                .reduce(new HashMap<String, BigDecimal>(), (accumulatedMap, mapFromFlux) -> {
                                                    mapFromFlux.forEach((key, value) -> accumulatedMap.merge(key, value, BigDecimal::add));
                                                    return accumulatedMap;
                                                })
                                                .doOnError(throwable -> log.error("Error happened while getting reduced Product Amount Map. {}", throwable.getMessage()))
//                                .doOnNext(stringBigDecimalHashMap -> log.info("Reduced Product Amount Map for {} : {}", processName, stringBigDecimalHashMap))
                                                .map(finalProductAmountMap -> Tuples.of(finalProductAmountMap, aisMetaData, object)))
                        /*.doOnNext(tupleOfMapAisMetaDataAndObject -> log.info(" Process Name : {} , ledgerKey : {} || Tuple of FinalProductAmountMap, aisMetaData and Object : {}", processName, aisMetaData.getLedgerKey(), tupleOfMapAisMetaDataAndObject))*/
                )
                .flatMap(tupleOfMapAisMetaDataAndObject -> getJournalList(tupleOfMapAisMetaDataAndObject, requestDTO))
                .doOnError(throwable -> log.error("Error happened while getting Journal List for Each aisMetaData. {}", throwable.getMessage()))
                .doOnNext(journalList -> log.info("Journal List : {}", journalList))
                .reduce(new ArrayList<Journal>(), (accumulator, nextList) -> {
                    for (Journal journal : nextList) {
                        mergeJournal(accumulator, journal);
                    }
                    return accumulator;
                })
                .doOnError(throwable -> log.error("Error happened while reducing Journal List by merging according to Ledger & subLedger Id. {}", throwable.getMessage()))
                .flatMap(finalJournalList -> getJournalRequestDTO(finalJournalList, processName, requestDTO))
                .doOnError(throwable -> log.error("Error happened while getting JournalRequestDTO with Journal List. {}", throwable.getMessage()));
    }

    private Mono<JournalRequestDTO> buildFeeCollectionAISRequestFromAutoVoucher(AccountingRequestDTO accountingRequestDTO) {
        return feeCollectionUseCase
                .updateNullableFeeCollectionByOfficeId(FeeCollectionUpdateRequestDTO.builder().officeId(accountingRequestDTO.getOfficeId()).build())
                .filter(feeCollectionList -> !feeCollectionList.isEmpty())
                .flatMap(feeCollectionList -> autoVoucherUseCase.createAndSaveAutoVoucherForFeeCollection(AutoVoucherRequestForFeeCollectionDTO.builder()
                        .mfiId(accountingRequestDTO.getMfiId())
                        .officeId(accountingRequestDTO.getOfficeId())
                        .loginId(accountingRequestDTO.getLoginId())
                        .feeCollectionList(feeCollectionList)
                        .build()))
                .flatMap(autoVoucher ->
                        autoVoucherUseCase.getAutoVoucherDetailListByVoucherId(autoVoucher.getVoucherId())
                                .collectList()
                                .flatMap(autoVoucherDetailList -> this.buildJournalRequestDTOFromAutoVoucherDetailList(autoVoucherDetailList, accountingRequestDTO)));
    }

    private Mono<JournalRequestDTO> buildJournalRequestDTOFromAutoVoucherDetailList(List<AutoVoucherDetail> autoVoucherDetailList, AccountingRequestDTO accountingRequestDTO) {
        List<Journal> journalList =
                autoVoucherDetailList
                        .stream()
                        .map(autoVoucherDetail ->
                                Journal
                                        .builder()
                                        .ledgerId(autoVoucherDetail.getLedgerId())
                                        .subledgerId(autoVoucherDetail.getSubledgerId())
                                        .debitedAmount(autoVoucherDetail.getDebitedAmount())
                                        .creditedAmount(autoVoucherDetail.getCreditedAmount())
                                        .description(autoVoucherDetail.getRemarks())
                                        .build())
                        .toList();

        return this.getJournalRequestDTO(journalList, accountingRequestDTO.getProcessName(), accountingRequestDTO);
    }

    private void mergeJournal(List<Journal> accumulator, Journal nextJournal) {
        for (Journal existingJournal : accumulator) {
            if (existingJournal.getSubledgerId() == null) {
                if (existingJournal.getLedgerId().equals(nextJournal.getLedgerId())) {
                    existingJournal.setDebitedAmount(existingJournal.getDebitedAmount().add(nextJournal.getDebitedAmount()));
                    existingJournal.setCreditedAmount(existingJournal.getCreditedAmount().add(nextJournal.getCreditedAmount()));
                    return;
                }
            } else if (existingJournal.getLedgerId().equals(nextJournal.getLedgerId()) &&
                    existingJournal.getSubledgerId().equals(nextJournal.getSubledgerId())) {
                existingJournal.setDebitedAmount(existingJournal.getDebitedAmount().add(nextJournal.getDebitedAmount()));
                existingJournal.setCreditedAmount(existingJournal.getCreditedAmount().add(nextJournal.getCreditedAmount()));
                return;
            }
        }
        // If no match found, add the journal to the accumulator
        accumulator.add(nextJournal);
    }

    @Override
    public Mono<AisResponse> saveAccountingJournal(JournalRequestDTO journalRequestDTO) {
        if (journalRequestDTO.getJournalList().isEmpty() && !(journalRequestDTO.getAmount().compareTo(BigDecimal.ZERO) == 0)) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Journal List is Empty or Amount is Zero"));
        } else {
            return aisJournalClientPort
                    .postAccounting(journalRequestDTO)
                    .doOnRequest(l -> log.info("Request sent to webclient for accounting posting from service"))
                    .doOnNext(aisResponse -> log.debug("AisResponse : {}", aisResponse))
                    .doOnSuccess(aisResponse -> log.info("Journal Body successfully saved."));
        }
    }

    @Override
    public Mono<List<AisResponse>> buildAndSaveAccountingJournalFromAutoVoucherList(AutoVoucherJournalRequestDTO autoVoucherJournalRequestDTO) {
        return buildJournalRequestDTOListForAutoVouchers(autoVoucherJournalRequestDTO)
                .flatMapMany(Flux::fromIterable)
                .concatMap(this::saveAccountingJournal)
                .collectList()
                .doOnNext(aisResponse -> log.info("Journal Save Response : {}", aisResponse));
    }


    private Mono<List<JournalRequestDTO>> buildJournalRequestDTOListForAutoVouchers(AutoVoucherJournalRequestDTO autoVoucherJournalRequestDTO) {
        return /*autoVoucherUseCase.getAutoVoucherListByManagementProcessIdAndProcessId(autoVoucherJournalRequestDTO.getManagementProcessId(), autoVoucherJournalRequestDTO.getProcessId())*/
                autoVoucherUseCase.getAutoVoucherListByManagementProcessId(autoVoucherJournalRequestDTO.getManagementProcessId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(autoVoucher -> autoVoucherUseCase
                        .getAutoVoucherDetailListByVoucherId(autoVoucher.getVoucherId())
                        .map(this::buildJournalFromAutoVoucherDetail)
                        .collectList()
                        .map(journalList -> this.buildJournalRequestDTOForAVoucher(autoVoucher, journalList, autoVoucherJournalRequestDTO))
                        .flatMap(journalRequestDTO -> autoVoucherUseCase.updateAutoVoucherWithAisRequest(autoVoucher.getOid(), journalRequestDTO.toString())
                                .thenReturn(journalRequestDTO)))
                .collectList();
    }

    private Journal buildJournalFromAutoVoucherDetail(AutoVoucherDetail autoVoucherDetail) {
        return Journal.builder()
                .description(autoVoucherDetail.getRemarks())
                .debitedAmount(autoVoucherDetail.getDebitedAmount())
                .creditedAmount(autoVoucherDetail.getCreditedAmount())
                .ledgerId(autoVoucherDetail.getLedgerId())
                .subledgerId(autoVoucherDetail.getSubledgerId())
                .build();
    }

    private JournalRequestDTO buildJournalRequestDTOForAVoucher(AutoVoucher autoVoucher, List<Journal> journalList, AutoVoucherJournalRequestDTO command) {
        return JournalRequestDTO.builder()
                .journalType(autoVoucher.getVoucherType())
                .description(autoVoucher.getVoucherNameEn() + " - " + command.getBusinessDate())
                .amount(autoVoucher.getVoucherAmount())
                .referenceNo(autoVoucher.getVoucherId())
                .journalProcess(AutoVoucherEnum.JOURNAL_PROCESS_AUTO.getValue())
                .officeId(command.getOfficeId())
                .mfiId(command.getMfiId())
                .createdBy(command.getLoginId())
                .processId(UUID.randomUUID().toString().concat("_").concat(autoVoucher.getVoucherType()))
                .journalList(journalList)
                .build();
    }

    private Mono<JournalRequestDTO> getJournalRequestDTO(List<Journal> finalJournalList, String processName,
            AccountingRequestDTO requestDTO) {
        return managementProcessTrackerUseCase
                .getCurrentBusinessDateForOffice(requestDTO.getManagementProcessId(), requestDTO.getOfficeId())
                .flatMap(businessDate -> this.buildJournalRequestBody(requestDTO, finalJournalList,
                        businessDate));
    }

    private Flux<PassbookResponseDTO> getPassbookEntries(AccountingRequestDTO requestDTO, AisMetaData aisMetaData) {
        String processName = requestDTO.getProcessName();
        String managementProcessId = requestDTO.getManagementProcessId();

        log.info("called getPassbookEntries with processName : {}, Ledger Key : {}, Payment Mode: {}, Savings Type Id: {}", processName, aisMetaData.getLedgerKey(), aisMetaData.getPaymentMode(), aisMetaData.getSavingsTypeId());
        return passbookUseCase
                .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(managementProcessId, aisMetaData.getTransactionCode(), aisMetaData.getPaymentMode(), aisMetaData.getSavingsTypeId())
                .collectList()
                .flatMapMany(passbookResponseDTOList -> {
                    if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())) {
                        return Flux.concat(
                                        Flux.fromIterable(passbookResponseDTOList),
                                        passbookUseCase.getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(
                                                managementProcessId,
                                                TransactionCodes.LOAN_REBATE.getValue(),
                                                Constants.PAYMENT_MODE_REBATE.getValue(),
                                                aisMetaData.getSavingsTypeId())
                                        );
                    }
                    return Flux.fromIterable(passbookResponseDTOList);
                })
                .collectList()
                .doOnNext(passbookResponseDTOs -> log.info("passbook Response size: {}", passbookResponseDTOs.size()))
                .flatMapIterable(responses -> responses)
                .doOnError(throwable -> log.error("Error Happened while fetching passbook entries with managementProcessId : {} | transactionCode : {} | paymentMode : {}, savingsTypeId : {}", managementProcessId, aisMetaData.getTransactionCode(), aisMetaData.getPaymentMode(), aisMetaData.getSavingsTypeId()));
    }


    private Mono<List<Journal>> getJournalList(Tuple3<HashMap<String, BigDecimal>, AisMetaData, Object> tupleOfMapAisMetaDataAndObject, AccountingRequestDTO requestDTO) {
        Map<String, BigDecimal> productAmountMap = tupleOfMapAisMetaDataAndObject.getT1();
        AisMetaData aisMetaData = tupleOfMapAisMetaDataAndObject.getT2();
        Object object = tupleOfMapAisMetaDataAndObject.getT3();
//        log.info("Ais Ledger Key: {}, Product Amount Map : {}", aisMetaData.getLedgerKey(), productAmountMap);
        if (aisMetaData.getHasSubledger().equalsIgnoreCase(AisMetaDataEnum.YES.getValue())) {
//            log.info("HasSubLedger = Yes");

            if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_PRINCIPAL.getValue())) {
                aisMetaData.setLedgerKey(AisMetaDataEnum.LEDGER_KEY_PRINCIPAL_OUTSTANDING.getValue());
                aisMetaData.setDescription("Adjustment");
            } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_SERVICE_CHARGE.getValue())) {
                aisMetaData.setLedgerKey(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING.getValue());
                aisMetaData.setDescription("Adjustment");
            } else if (aisMetaData.getLedgerKey().equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING.getValue())
            && !aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_REVERSE_LOAN_REPAY.getValue())
            && !aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_LOAN_REPAY.getValue())) {
                aisMetaData.setLedgerKey(requestDTO.getAccountingMetaProperty().getAllowSCProvision()
                        ? AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING.getValue()
                        : AisMetaDataEnum.LEDGER_KEY_SERVICE_CHARGE_INCOME.getValue());
            }
            return commonRepository
                    .getLedgerIdByLedgerKeyAndOfficeId(aisMetaData.getLedgerKey(), requestDTO.getOfficeId())
                            .flatMap(ledgerId -> getProductIdSubLedgerIdMap(ledgerId, productAmountMap, object)
                                    .flatMap(productSubLedgerMap -> buildJournalList(productAmountMap, productSubLedgerMap, aisMetaData, ledgerId)));
        } else if (aisMetaData.getHasSubledger().equalsIgnoreCase(AisMetaDataEnum.NO.getValue())) {
//            log.info("HasSubLedger = No");
            return commonRepository
                    .getLedgerIdByLedgerKeyAndOfficeId(aisMetaData.getLedgerKey(), requestDTO.getOfficeId())
                    .flatMap(ledgerId -> buildJournalList(productAmountMap, ledgerId, aisMetaData));
        } else
        return Mono.just(new ArrayList<>());

    }

    public Mono<Map<String, String>> getProductIdSubLedgerIdMap(String ledgerId,
            Map<String, BigDecimal> productIdAmountMap, Object object) {
        return Flux.fromIterable(productIdAmountMap.keySet())
                .flatMap(productId -> {
                    Mono<String> subLedgerIdMono;
                    if (productId.equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_AT_BANK.getValue())) {
                        if (object instanceof TransactionAdjustment transactionAdjustment){
                            subLedgerIdMono = commonRepository
                                    .getSubLedgerIdByLedgerIdAndReferenceId(ledgerId, transactionAdjustment.getReferenceId())
                                    .doOnRequest(l -> log.info("Requesting to get subledgerId with ledgerId : {} & referenceId : {}", ledgerId, transactionAdjustment.getReferenceId()))
                                    .switchIfEmpty(Mono.just(""))
                                    .flatMap(Mono::justOrEmpty);
                        } else {
                            PassbookResponseDTO passbookResponseDTO = (PassbookResponseDTO) object;
                            subLedgerIdMono = commonRepository
                                    .getSubLedgerIdByLedgerIdAndReferenceId(ledgerId, passbookResponseDTO.getReferenceId())
                                    .doOnRequest(l -> log.info("Requesting to get subledgerId with ledgerId : {} & referenceId : {}", ledgerId, passbookResponseDTO.getReferenceId()))
                                    .switchIfEmpty(Mono.just(""))
                                    .flatMap(Mono::justOrEmpty);
                        }
                    } else {
                        subLedgerIdMono = commonRepository
                                .getSubLedgerIdByLedgerIdAndProductId(ledgerId, productId)
                                .switchIfEmpty(Mono.just(""))
                                .flatMap(Mono::justOrEmpty);
                    }
                    return subLedgerIdMono.map(subLedgerId -> Tuples.of(productId, subLedgerId));
                })
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private Mono<List<Journal>> buildJournalList(Map<String, BigDecimal> productIdAmountMap,
            Map<String, String> productIdSubledgerIdMap, AisMetaData aisMetaData, String ledgerId) {
        return Flux.fromIterable(productIdAmountMap.keySet())
                .map(productId -> Tuples.of(productId, productIdSubledgerIdMap.get(productId),
                        productIdAmountMap.get(productId)))
                .map(tuple3 -> getDescriptionDebitedAmountCreditedAmountAndSubLedgerId(tuple3, aisMetaData))
                .flatMap(tuple4 -> this.buildJournal(tuple4.getT1(), tuple4.getT2(), tuple4.getT3(), ledgerId,
                        tuple4.getT4()))
                .collectList();
    }

    private Tuple4<String, BigDecimal, BigDecimal, String> getDescriptionDebitedAmountCreditedAmountAndSubLedgerId(
            Tuple3<String, String, BigDecimal> productIdSubLedgerIdAndAmountTuple, AisMetaData aisMetaData) {
        String subLedgerId = productIdSubLedgerIdAndAmountTuple.getT2();
        String productId = productIdSubLedgerIdAndAmountTuple.getT1();
        BigDecimal amount = productIdSubLedgerIdAndAmountTuple.getT3();
        BigDecimal debitedAmount = BigDecimal.ZERO;
        BigDecimal creditedAmount = BigDecimal.ZERO;

        String[] words = aisMetaData.getLedgerKey().split("(?<!^)(?=[A-Z])");
        String ledgerName = String.join(" ", words);

        String description = aisMetaData.getDescription() == null || aisMetaData.getDescription().isEmpty()
                ? aisMetaData.getProcessName().replace("_", " ") +  " - " + ledgerName + " - " + productId
                : aisMetaData.getProcessName().replace("_", " ") +  " - " + ledgerName + " - " + productId + " - " + aisMetaData.getDescription();

        description = productId.equalsIgnoreCase(AisMetaDataEnum.LEDGER_KEY_CASH_ON_HAND.getValue())
                ? aisMetaData.getProcessName().replace("_", " ") +  " - " + ledgerName
                : description;

        description = aisMetaData.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION_NO_ADVANCE.getValue())
                ? AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION.getValue().replace("_", " ") +  " - " + ledgerName + " - " + productId
                : description;
        if (aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_DEBIT.getValue()))
            debitedAmount = amount;
        else if (aisMetaData.getJournalEntryType().equalsIgnoreCase(AisMetaDataEnum.JOURNAL_ENTRY_TYPE_CREDIT.getValue()))
            creditedAmount = amount;
        return Tuples.of(description, debitedAmount, creditedAmount, subLedgerId);
    }

    private Mono<List<Journal>> buildJournalList(Map<String, BigDecimal> productIdAmountMap, String ledgerId, AisMetaData aisMetaData) {
        return Flux.fromIterable(productIdAmountMap.keySet())
                .map(productId -> Tuples.of(productId, "", productIdAmountMap.get(productId)))
                .map(tuple3 -> getDescriptionDebitedAmountCreditedAmountAndSubLedgerId(tuple3, aisMetaData))
                .flatMap(tuple4 -> this.buildJournal(tuple4.getT1(), tuple4.getT2(), tuple4.getT3(), ledgerId,
                        tuple4.getT4()))
                .reduce(new ArrayList<>(), (accumulator, nextList) -> {
                    accumulator.add(nextList);
                    return accumulator;
                });
    }


    private Mono<Journal> buildJournal(String description, BigDecimal debitedAmount, BigDecimal creditedAmount,
            String ledgerId, String subLedgerId) {
        return Mono.just(
                Journal
                        .builder()
                        .description(description)
                        .debitedAmount(debitedAmount)
                        .creditedAmount(creditedAmount)
                        .ledgerId(ledgerId)
                        .subledgerId(subLedgerId.isEmpty() ? null : subLedgerId)
                        .build());

    }

    private Mono<JournalRequestDTO> buildJournalRequestBody(AccountingRequestDTO requestDTO, List<Journal> journalList,
                                                            LocalDate businessDate) {
        Optional<BigDecimal> totalAmount = journalList
                .stream()
                .map(Journal::getDebitedAmount)
                .reduce(BigDecimal::add);

        BigDecimal totalDebit = journalList.stream().map(Journal::getDebitedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = journalList.stream().map(Journal::getCreditedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (requestDTO.getProcessName().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_SC_PROVISION.getValue())) {
            log.info("Journal List total debit for SC Provision: {}", totalDebit);
            log.info("Journal List total credit for SC Provision: {}", totalCredit);
        }

        return Mono.just(
                JournalRequestDTO
                        .builder()
                        .journalType(requestDTO.getProcessName())
                        .description(requestDTO.getProcessName() + ", Date: " + businessDate)
                        .amount(totalAmount.orElse(BigDecimal.ZERO))
                        .referenceNo(businessDate.toString())
                        .journalProcess(AisMetaDataEnum.JOURNAL_PROCESS_AUTO.getValue())
                        .officeId(requestDTO.getOfficeId())
                        .mfiId(requestDTO.getMfiId())
                        .createdBy(requestDTO.getLoginId())
                        .journalList(journalList)
                        .build());
    }
}
