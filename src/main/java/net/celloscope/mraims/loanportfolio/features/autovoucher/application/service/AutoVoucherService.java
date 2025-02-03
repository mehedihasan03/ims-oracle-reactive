package net.celloscope.mraims.loanportfolio.features.autovoucher.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestForFeeCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out.AutoVoucherDetailPersistencePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out.AutoVoucherPersistencePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherData;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherEnum;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.AccountingMetaProperty;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherEnum.VOUCHER_ID_PREFIX_FEE_COLLECTION_VOUCHER;
import static net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherEnum.VOUCHER_NAME_FEE_COLLECTION_VOUCHER;

@Service
@Slf4j
public class AutoVoucherService implements AutoVoucherUseCase {
    private final AutoVoucherPersistencePort autoVoucherPersistencePort;
    private final AutoVoucherDetailPersistencePort autoVoucherDetailPersistencePort;
    private final ModelMapper modelMapper;
    private final TransactionalOperator rxtx;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final MetaPropertyUseCase metaPropertyUseCase;

    public AutoVoucherService(AutoVoucherPersistencePort autoVoucherPersistencePort, AutoVoucherDetailPersistencePort autoVoucherDetailPersistencePort, ModelMapper modelMapper, TransactionalOperator rxtx, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, MetaPropertyUseCase metaPropertyUseCase) {
        this.autoVoucherPersistencePort = autoVoucherPersistencePort;
        this.autoVoucherDetailPersistencePort = autoVoucherDetailPersistencePort;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
    }

    @Override
    public Mono<List<AutoVoucher>> createAndSaveAutoVoucherFromAISRequest(AutoVoucherRequestDTO requestDTO) {
        return /*this.validateDebitAndCreditAmount(requestDTO)*/
                Mono.just(requestDTO)
                .map(AutoVoucherRequestDTO::getAisRequestList)
                .map(this::buildAutoVoucherDataMapFromAISRequest)
                .flatMap(voucherNameAndDataMap -> {
                    List<AutoVoucher> autoVoucherList = buildAutoVoucher(requestDTO, voucherNameAndDataMap);
                    List<AutoVoucher> finalAutoVoucherList = autoVoucherList.stream().filter(autoVoucher -> autoVoucher.getVoucherAmount().compareTo(BigDecimal.ZERO) > 0).toList();
                    return rxtx.transactional(
                            this.filterAutoVoucherListAccordingToAccountingMetaProperty(finalAutoVoucherList)
                            .flatMapMany(autoVoucherPersistencePort::saveAutoVoucherList)
                            .map(oidAndAutoVoucherTuple -> this.buildAutoVoucherDetail(voucherNameAndDataMap, oidAndAutoVoucherTuple, requestDTO))
                            .flatMap(autoVoucherDetailPersistencePort::saveAutoVoucherDetailList)
                            .collectList()
                            .map(autoVoucherDetailList -> finalAutoVoucherList));
                });
    }


    private Mono<List<AutoVoucher>> filterAutoVoucherListAccordingToAccountingMetaProperty(List<AutoVoucher> autoVoucherList) {
        return metaPropertyUseCase.getAccountingMetaProperty()
                .flatMap(accountingMetaProperty -> {
                    boolean isSCProvisionAllowed = accountingMetaProperty.getAllowSCProvision();
                    List<AutoVoucher> filteredAutoVoucherList = autoVoucherList;
                    if (!isSCProvisionAllowed) {
                        filteredAutoVoucherList = autoVoucherList.stream()
                                .filter(autoVoucher -> !autoVoucher.getVoucherNameEn().equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_SC_PROVISION_VOUCHER.getValue()))
                                .toList();
                    }
                    return Mono.just(filteredAutoVoucherList);
                });
    }


    private Mono<AutoVoucherRequestDTO> validateDebitAndCreditAmount(AutoVoucherRequestDTO requestDTO) {
        return Mono.just(requestDTO)
                .flatMap(autoVoucherRequestDTO -> {
                    List<JournalRequestDTO> journalRequestDTOList = autoVoucherRequestDTO.getAisRequestList();
                    for (JournalRequestDTO journalRequestDTO : journalRequestDTOList) {
                        BigDecimal debitedAmount = journalRequestDTO.getJournalList().stream()
                                .map(journal -> journal.getDebitedAmount() != null ? journal.getDebitedAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal creditedAmount = journalRequestDTO.getJournalList().stream()
                                .map(journal -> journal.getCreditedAmount() != null ? journal.getCreditedAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        if (debitedAmount.compareTo(creditedAmount) != 0) {
                            log.error("Debited amount and credited amount are not equal for journal request : {}", journalRequestDTO);
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Debited amount and credited amount are not equal for journal request : " + journalRequestDTO.getDescription()));
                        }
                    }
                    return Mono.just(autoVoucherRequestDTO);
                });
    }


    @Override
    public Mono<List<AutoVoucher>> getAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessIdAndProcessId(managementProcessId, processId)
                .collectList();
    }

    @Override
    public Mono<List<AutoVoucher>> getAutoVoucherListByManagementProcessId(String managementProcessId) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<Boolean> deleteAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessIdAndProcessId(managementProcessId, processId)
                .collectList()
                .flatMap(autoVouchers -> !autoVouchers.isEmpty()
                        ? deleteAutoVoucherList(autoVouchers, managementProcessId, processId)
                        : Mono.just(true));
    }

    @Override
    public Mono<String> deleteAutoVoucherListByManagementProcessId(String managementProcessId) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessId(managementProcessId)
                .collectList()
                .flatMap(autoVouchers -> !autoVouchers.isEmpty()
                        ? deleteAutoVoucherListByManagementProcessId(autoVouchers, managementProcessId)
                        : Mono.just("No auto voucher list found for management process id : " + managementProcessId));
    }

    private Mono<Boolean> deleteAutoVoucherList(List<AutoVoucher> autoVouchers, String managementProcessId, String processId) {
        return Flux.fromIterable(autoVouchers)
                .map(AutoVoucher::getVoucherId)
                .collectList()
                .filter(voucherIdList -> !voucherIdList.isEmpty())
                .flatMap(voucherIdList -> rxtx.transactional(autoVoucherDetailPersistencePort.deleteAutoVoucherDetailListByVoucherIdList(voucherIdList)
                        .flatMap(aBoolean -> autoVoucherPersistencePort.deleteAutoVoucherListByManagementProcessIdAndProcessId(managementProcessId, processId))));
    }


    private Mono<String> deleteAutoVoucherListByManagementProcessId(List<AutoVoucher> autoVouchers, String managementProcessId) {
        return Flux.fromIterable(autoVouchers)
                .map(AutoVoucher::getVoucherId)
                .collectList()
                .filter(voucherIdList -> !voucherIdList.isEmpty())
                .flatMap(voucherIdList -> rxtx.transactional(autoVoucherDetailPersistencePort.deleteAutoVoucherDetailListByVoucherIdList(voucherIdList)
                        .flatMap(aBoolean -> autoVoucherPersistencePort.deleteAutoVoucherListByManagementProcessId(managementProcessId))))
                .thenReturn("Auto voucher list deleted successfully for management process id : " + managementProcessId);
    }

    @Override
    public Flux<AutoVoucherDetail> getAutoVoucherDetailListByVoucherId(String voucherId) {
        return autoVoucherDetailPersistencePort.getAutoVoucherDetailByVoucherId(voucherId);
    }

    @Override
    public Mono<Boolean> updateAutoVoucherAndVoucherDetailStatus(String managementProcessId, String processId, String loginId, String status) {
        return autoVoucherPersistencePort.updateAutoVoucherStatus(managementProcessId, processId, loginId, status)
                .flatMap(autoVoucher -> autoVoucherDetailPersistencePort.updateAutoVoucherDetailStatusByVoucherId(autoVoucher.getVoucherId(), loginId, status))
                .collectList()
                .map(autoVoucherDetailList -> true);
    }

    @Override
    public Mono<Boolean> updateAutoVoucherAndVoucherDetailStatus(String managementProcessId, String loginId, String status) {
        return autoVoucherPersistencePort.updateAutoVoucherStatus(managementProcessId, loginId, status)
                .flatMap(autoVoucher -> autoVoucherDetailPersistencePort.updateAutoVoucherDetailStatusByVoucherId(autoVoucher.getVoucherId(), loginId, status))
                .collectList()
                .map(autoVoucherDetailList -> true);
    }

    @Override
    public Mono<Boolean> saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(String managementProcessId, String processId) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessIdAndProcessId(managementProcessId, processId)
                .collectList()
                .flatMap(autoVouchers ->
                        autoVoucherPersistencePort
                                .saveAutoVoucherListIntoHistory(autoVouchers)
                                .thenReturn(autoVouchers)
                )
                .flatMapIterable(autoVouchers -> autoVouchers)
                .flatMap(autoVoucher ->
                        autoVoucherDetailPersistencePort
                                .getAutoVoucherDetailByVoucherId(autoVoucher.getVoucherId())
                )
                .collectList()
                .flatMap(autoVoucherDetailPersistencePort::saveAutoVoucherDetailListIntoHistory)
                .doOnSuccess(s -> log.info("Auto voucher history and Auto voucher detail history saved successfully"))
                .then(Mono.just(Boolean.TRUE))
                ;
    }


    @Override
    public Mono<String> saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(List<AutoVoucher> autoVoucherList) {
        return autoVoucherPersistencePort
                                .saveAutoVoucherListIntoHistory(autoVoucherList)
                                .thenReturn(autoVoucherList)
                .flatMapIterable(autoVouchers -> autoVouchers)
                .flatMap(autoVoucher ->
                        autoVoucherDetailPersistencePort
                                .getAutoVoucherDetailByVoucherId(autoVoucher.getVoucherId())
                                .map(autoVoucherDetail -> {
                                    autoVoucherDetail.setArchivedBy(autoVoucher.getArchivedBy());
                                    autoVoucherDetail.setArchivedOn(autoVoucher.getArchivedOn());
                                    return autoVoucherDetail;
                                })
                )
                .collectList()
                .flatMap(autoVoucherDetailPersistencePort::saveAutoVoucherDetailListIntoHistory)
                .doOnSuccess(s -> log.info("Auto voucher history and Auto voucher detail history saved successfully"));
    }


    @Override
    public Mono<AutoVoucher> createAndSaveAutoVoucherForFeeCollection(AutoVoucherRequestForFeeCollectionDTO requestDto) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> {
                    AutoVoucher autoVoucher = buildAutoVoucherForFeeCollection(requestDto, managementProcessTracker.getBusinessDate());
                    return autoVoucherPersistencePort.saveAutoVoucher(autoVoucher)
                            .flatMap(autoVoucherInfo -> {
                                List<AutoVoucherDetail> autoVoucherDetailList = buildAutoVoucherDetailListForFeeCollection(requestDto, autoVoucherInfo, managementProcessTracker.getBusinessDate());
                                return autoVoucherDetailPersistencePort.saveAutoVoucherDetailList(autoVoucherDetailList)
                                        .collectList()
                                        .map(autoVoucherDetails -> autoVoucherInfo);
                            });
                })
                .as(rxtx::transactional)
                .doOnNext(autoVoucher -> log.info("Auto voucher created and saved successfully for fee collection"))
                .doOnError(throwable -> log.error("Error occurred while creating and saving auto voucher for fee collection : {}", throwable.getMessage()));
    }

    @Override
    public Mono<AutoVoucher> updateAutoVoucherWithAisRequest(String oid, String aisRequest) {
        return autoVoucherPersistencePort.updateAutoVoucherWithAisRequest(oid, aisRequest)
                .doOnRequest(l -> log.info("Requesting to update auto voucher with oid : {}", oid))
                .doOnSuccess(autoVoucherEntity -> log.info("Auto voucher updated successfully with oid : {} and aisRequest : {}", oid, aisRequest))
                .doOnError(throwable -> log.error("Error occurred while updating auto voucher with oid : {} and aisRequest : {}, error : {}", oid, aisRequest, throwable.getMessage()))
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class));
    }

    public AutoVoucher buildAutoVoucherForFeeCollection(AutoVoucherRequestForFeeCollectionDTO command,
                                                        LocalDate businessDate) {
        return AutoVoucher
                .builder()
                .managementProcessId(command.getFeeCollectionList().get(0).getManagementProcessId())
                .processId(UUID.randomUUID().toString())
                .voucherId(VOUCHER_ID_PREFIX_FEE_COLLECTION_VOUCHER.getValue().concat("-").concat(command.getOfficeId()).concat("-").concat(businessDate.toString()))
                .voucherType(VOUCHER_NAME_FEE_COLLECTION_VOUCHER.getValue().replace(" ", ""))
                .voucherNameEn(VOUCHER_NAME_FEE_COLLECTION_VOUCHER.getValue())
                .voucherNameBn(VOUCHER_NAME_FEE_COLLECTION_VOUCHER.getValue())
                .voucherDate(businessDate)
                .voucherAmount(command.getFeeCollectionList().stream().map(FeeCollection::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .voucherPreparedBy(command.getLoginId())
                .officeId(command.getOfficeId())
                .mfiId(command.getMfiId())
                .isVisible(AutoVoucherEnum.VISIBLE_YES.getValue())
                .status(Status.STATUS_PENDING.getValue())
                .createdOn(LocalDateTime.now())
                .createdBy(command.getLoginId())
                .build();
    }

    private List<AutoVoucherDetail> buildAutoVoucherDetailListForFeeCollection(AutoVoucherRequestForFeeCollectionDTO command,
                                                                               AutoVoucher autoVoucher,
                                                                               LocalDate businessDate)
    {
        Map<String, List<FeeCollection>> groupedByFeeCollectionCode = command.getFeeCollectionList().stream()
                .collect(Collectors.groupingBy(FeeCollection::getFeeCollectionCode));

        List<AutoVoucherDetail> autoVoucherDetailList = new ArrayList<>();

        for (Map.Entry<String, List<FeeCollection>> creditableEntries : groupedByFeeCollectionCode.entrySet()) {
            List<FeeCollection> creditDetails = creditableEntries.getValue();

            BigDecimal totalCredit = creditDetails.stream().map(FeeCollection::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            autoVoucherDetailList.add(buildAutoVoucherDetailForFeeCollection(command, autoVoucher,
                    BigDecimal.ZERO, totalCredit, businessDate,
                    creditableEntries.getValue().get(0).getCreditLedgerId(),
                    creditableEntries.getValue().get(0).getCreditSubledgerId(),
                    "FEE COLLECTION -".concat(creditableEntries.getKey())));
        }

        Map<String, List<FeeCollection>> groupedByPaymentCode = command.getFeeCollectionList().stream()
                .collect(Collectors.groupingBy(FeeCollection::getReceiveMode));

        for (Map.Entry<String, List<FeeCollection>> debitableEntries : groupedByPaymentCode.entrySet()) {
            List<FeeCollection> details = debitableEntries.getValue();
            BigDecimal totalDebit = details.stream().map(FeeCollection::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            autoVoucherDetailList.add(buildAutoVoucherDetailForFeeCollection(command, autoVoucher,
                    totalDebit, BigDecimal.ZERO, businessDate,
                    debitableEntries.getValue().get(0).getDebitLedgerId(),
                    debitableEntries.getValue().get(0).getDebitSubledgerId(),
                    "FEE COLLECTION -".concat(debitableEntries.getKey())));
        }
        return autoVoucherDetailList;
    }

    AutoVoucherDetail buildAutoVoucherDetailForFeeCollection(AutoVoucherRequestForFeeCollectionDTO command,
                                                             AutoVoucher autoVoucher,
                                                             BigDecimal debitedAmount,
                                                             BigDecimal creditedAmount,
                                                             LocalDate businessDate,
                                                             String ledgerId,
                                                             String subledgerId,
                                                             String remarks
                                                             ) {
        return AutoVoucherDetail
                .builder()
                .voucherId(autoVoucher.getVoucherId())
                .transactionDate(businessDate)
                .debitedAmount(debitedAmount)
                .creditedAmount(creditedAmount)
                .ledgerId(ledgerId)
                .subledgerId(subledgerId)
                .remarks(remarks)
                .officeId(autoVoucher.getOfficeId())
                .mfiId(command.getMfiId())
                .status(Status.STATUS_PENDING.getValue())
                .createdBy(command.getLoginId())
                .createdOn(LocalDateTime.now())
                .build();
    }


    private List<AutoVoucherDetail> buildAutoVoucherDetail(Map<String, List<AutoVoucherData>> voucherNameAndDataMap, Tuple2<String, AutoVoucher> oidAndAutoVoucherTuple, AutoVoucherRequestDTO command) {
        List<AutoVoucherDetail> autoVoucherDetailList = buildAutoVoucherDetailListForAVoucher(voucherNameAndDataMap.get(oidAndAutoVoucherTuple.getT2().getVoucherNameEn()), oidAndAutoVoucherTuple.getT2().getVoucherId(), command);
        log.info("Auto voucher detail List built successfully for voucher : {}", oidAndAutoVoucherTuple.getT2().getVoucherNameEn());
        return autoVoucherDetailList;
    }

    private List<AutoVoucherDetail> buildAutoVoucherDetailListForAVoucher(List<AutoVoucherData> autoVoucherDataList, String voucherId, AutoVoucherRequestDTO command) {
        List<AutoVoucherDetail> autoVoucherDetailList = new ArrayList<>();
        List<AutoVoucherData> finalAutoVoucherDataList = new ArrayList<>();

        /* splitting to 2 entries if debit and credit are made in same ledger */
        for (AutoVoucherData autoVoucherData : autoVoucherDataList) {
            if (autoVoucherData.getDebitedAmount().compareTo(BigDecimal.ZERO) > 0 && autoVoucherData.getCreditedAmount().compareTo(BigDecimal.ZERO) > 0) {
                AutoVoucherData debitAutoVoucherData = AutoVoucherData
                        .builder()
                        .description(autoVoucherData.getDescription())
                        .ledgerId(autoVoucherData.getLedgerId())
                        .subledgerId(autoVoucherData.getSubledgerId())
                        .debitedAmount(autoVoucherData.getDebitedAmount())
                        .creditedAmount(BigDecimal.ZERO)
                        .build();

                AutoVoucherData creditAutoVoucherData = AutoVoucherData
                        .builder()
                        .description(autoVoucherData.getDescription())
                        .ledgerId(autoVoucherData.getLedgerId())
                        .subledgerId(autoVoucherData.getSubledgerId())
                        .debitedAmount(BigDecimal.ZERO)
                        .creditedAmount(autoVoucherData.getCreditedAmount())
                        .build();

                finalAutoVoucherDataList.add(setAutoVoucherDataDescription(debitAutoVoucherData));
                finalAutoVoucherDataList.add(setAutoVoucherDataDescription(creditAutoVoucherData));
            } else {
                finalAutoVoucherDataList.add(setAutoVoucherDataDescription(autoVoucherData));
            }
        }

        for (AutoVoucherData autoVoucherData : finalAutoVoucherDataList) {
            autoVoucherDetailList.add(AutoVoucherDetail
                    .builder()
                    .voucherId(voucherId)
                    .transactionDate(command.getBusinessDate())
                    .debitedAmount(autoVoucherData.getDebitedAmount())
                    .creditedAmount(autoVoucherData.getCreditedAmount())
                    .ledgerId(autoVoucherData.getLedgerId())
                    .subledgerId(autoVoucherData.getSubledgerId())
                    .remarks(autoVoucherData.getDescription())
                    .officeId(command.getOfficeId())
                    .mfiId(command.getMfiId())
                    .status(Status.STATUS_PENDING.getValue())
                    .createdBy(command.getLoginId())
                    .createdOn(LocalDateTime.now())
                    .build());
        }

        return autoVoucherDetailList;
    }

    private AutoVoucherData setAutoVoucherDataDescription(AutoVoucherData autoVoucherData) {
        if (autoVoucherData.getDescription().contains(AisMetaDataEnum.LEDGER_NAME_PREPAYMENT_LOAN_INSTALLMENT.getValue()) && autoVoucherData.getDebitedAmount().compareTo(BigDecimal.ZERO) > 0) {
            autoVoucherData.setDescription(autoVoucherData.getDescription().concat(" - ").concat("Adjustment"));
        } else if (autoVoucherData.getDescription().contains(AisMetaDataEnum.LEDGER_NAME_PREPAYMENT_LOAN_INSTALLMENT.getValue()) && autoVoucherData.getCreditedAmount().compareTo(BigDecimal.ZERO) > 0) {
            autoVoucherData.setDescription(autoVoucherData.getDescription().concat(" - ").concat("Advance"));
        }
        return autoVoucherData;
    }


    public Map<String, List<AutoVoucherData>> buildAutoVoucherDataMapFromAISRequest(List<JournalRequestDTO> journalRequestDTOList) {
        List<String> paymentVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_DISBURSEMENT.getValue(), AisMetaDataEnum.PROCESS_NAME_WITHDRAW.getValue(), AisMetaDataEnum.PROCESS_NAME_INTEREST_POSTING.getValue());
        List<String> receivedVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION.getValue(), AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION_NO_ADVANCE.getValue(), AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue(), AisMetaDataEnum.PROCESS_NAME_WELFARE_FUND.getValue());
        List<String> adjustmentVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_LOAN_ADJUSTMENT.getValue());
        List<String> serviceChargeProvisionVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_SC_PROVISION.getValue());
        List<String> reverseLoanRepayVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_REVERSE_LOAN_REPAY.getValue());
        List<String> adjustmentLoanRepayVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_LOAN_REPAY.getValue());
        List<String> reverseSavingsDepositVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_DEPOSIT.getValue());
        List<String> adjustmentSavingsDepositVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_DEPOSIT.getValue());
        List<String> reverseSavingsWithdrawVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_WITHDRAW.getValue());
        List<String> adjustmentSavingsWithdrawVoucherTransactionCodes = List.of(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_WITHDRAW.getValue());

        List<JournalRequestDTO> paymentVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, paymentVoucherTransactionCodes);
        List<JournalRequestDTO> receivedVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, receivedVoucherTransactionCodes);
        List<JournalRequestDTO> adjustmentVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, adjustmentVoucherTransactionCodes);
        List<JournalRequestDTO> serviceChargeProvisionVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, serviceChargeProvisionVoucherTransactionCodes);
        List<JournalRequestDTO> reverseLoanRepayVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, reverseLoanRepayVoucherTransactionCodes);
        List<JournalRequestDTO> adjustmentLoanRepayVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, adjustmentLoanRepayVoucherTransactionCodes);
        List<JournalRequestDTO> reverseSavingsDepositVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, reverseSavingsDepositVoucherTransactionCodes);
        List<JournalRequestDTO> adjustmentSavingsDepositVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, adjustmentSavingsDepositVoucherTransactionCodes);
        List<JournalRequestDTO> reverseSavingsWithdrawVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, reverseSavingsWithdrawVoucherTransactionCodes);
        List<JournalRequestDTO> adjustmentSavingsWithdrawVoucherJournalRequestDTOList = filterJournalRequestDTOsByTransactionCodes(journalRequestDTOList, adjustmentSavingsWithdrawVoucherTransactionCodes);

        Map<String, List<AutoVoucherData>> voucherNameAndDataMap = new HashMap<>();

        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_PAYMENT_VOUCHER.getValue(), buildVoucherDataList(paymentVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_RECEIVED_VOUCHER.getValue(), buildVoucherDataList(receivedVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_VOUCHER.getValue(), buildVoucherDataList(adjustmentVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_SC_PROVISION_VOUCHER.getValue(), buildVoucherDataList(serviceChargeProvisionVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_REVERSE_LOAN_REPAY_VOUCHER.getValue(), buildVoucherDataList(reverseLoanRepayVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_LOAN_REPAY_VOUCHER.getValue(), buildVoucherDataList(adjustmentLoanRepayVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_REVERSE_SAVINGS_DEPOSIT_VOUCHER.getValue(), buildVoucherDataList(reverseSavingsDepositVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_SAVINGS_DEPOSIT_VOUCHER.getValue(), buildVoucherDataList(adjustmentSavingsDepositVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_REVERSE_SAVINGS_WITHDRAW_VOUCHER.getValue(), buildVoucherDataList(reverseSavingsWithdrawVoucherJournalRequestDTOList));
        voucherNameAndDataMap.put(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_SAVINGS_WITHDRAW_VOUCHER.getValue(), buildVoucherDataList(adjustmentSavingsWithdrawVoucherJournalRequestDTOList));
log.info("Auto voucher data map built successfully : {}", voucherNameAndDataMap);
        return voucherNameAndDataMap;
    }

    private List<JournalRequestDTO> filterJournalRequestDTOsByTransactionCodes(List<JournalRequestDTO> journalRequestDTOList, List<String> transactionCodes) {
        return journalRequestDTOList.stream()
                .filter(journalRequestDTO -> transactionCodes.contains(journalRequestDTO.getJournalType()))
                .toList();
    }

    private List<AutoVoucherData> buildVoucherDataList(List<JournalRequestDTO> journalRequestDTOList) {
        return journalRequestDTOList
                .stream()
                .map(JournalRequestDTO::getJournalList)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(journal -> Arrays.asList(journal.getLedgerId(), journal.getSubledgerId())))
                .values()
                .stream()
                .map(journals -> journals.stream().reduce((j1, j2) -> {
                    j1.setDebitedAmount(j1.getDebitedAmount().add(j2.getDebitedAmount()));
                    j1.setCreditedAmount(j1.getCreditedAmount().add(j2.getCreditedAmount()));
                    return j1;
                }))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::buildAutoVoucherDataList)
                .toList();
    }

    public List<AutoVoucher> buildAutoVoucher(AutoVoucherRequestDTO command, Map<String, List<AutoVoucherData>> voucherNameAndDataMap) {

        List<AutoVoucher> autoVoucherList = new ArrayList<>();
        for (Map.Entry<String, List<AutoVoucherData>> entry : voucherNameAndDataMap.entrySet()) {
            AutoVoucher autoVoucher = AutoVoucher
                    .builder()
                    .managementProcessId(command.getManagementProcessId())
                    .processId(command.getProcessId())
                    .voucherId(buildVoucherId(entry.getKey(), command.getBusinessDate(), command.getOfficeId()))
                    .voucherType(entry.getKey().replace(" ", ""))
                    .voucherNameEn(entry.getKey())
                    .voucherNameBn(entry.getKey())
                    .voucherDate(command.getBusinessDate())
                    .voucherAmount(entry.getValue().stream().map(AutoVoucherData::getDebitedAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .voucherPreparedBy(command.getLoginId())
                    .officeId(command.getOfficeId())
                    .mfiId(command.getMfiId())
                    .isVisible(updateIsVisible(entry.getKey()) ? AutoVoucherEnum.VISIBLE_YES.getValue() : AutoVoucherEnum.VISIBLE_NO.getValue())
                    .status(Status.STATUS_PENDING.getValue())
                    .createdOn(LocalDateTime.now())
                    .createdBy(command.getLoginId())
                    .build();

            autoVoucherList.add(autoVoucher);
        }
        return autoVoucherList;
    }


    public String buildVoucherId(String voucherNameEn, LocalDate businessDate, String officeId) {

        String voucherIdPrefix = "";
        if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_PAYMENT_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_PAYMENT_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_RECEIVED_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_RECEIVED_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_ADJUSTMENT_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_SC_PROVISION_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_SC_PROVISION_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_REVERSE_LOAN_REPAY_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_REVERSE_LOAN_REPAY_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_LOAN_REPAY_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_ADJUSTMENT_LOAN_REPAY_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_REVERSE_SAVINGS_DEPOSIT_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_REVERSE_SAVINGS_DEPOSIT_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_SAVINGS_DEPOSIT_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_ADJUSTMENT_SAVINGS_DEPOSIT_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_REVERSE_SAVINGS_WITHDRAW_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_REVERSE_SAVINGS_WITHDRAW_VOUCHER.getValue();
        } else if (voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_ADJUSTMENT_SAVINGS_WITHDRAW_VOUCHER.getValue())) {
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_ADJUSTMENT_SAVINGS_WITHDRAW_VOUCHER.getValue();
        } else
            voucherIdPrefix = AutoVoucherEnum.VOUCHER_ID_PREFIX_UNLISTED_VOUCHER.getValue();

        return voucherIdPrefix.concat("-").concat(officeId).concat("-").concat(businessDate.toString());
    }

    private boolean updateIsVisible(String voucherNameEn) {
        return !voucherNameEn.equalsIgnoreCase(AutoVoucherEnum.VOUCHER_NAME_SC_PROVISION_VOUCHER.getValue());

    }

    private AutoVoucherData buildAutoVoucherDataList(Journal journal) {
        return AutoVoucherData
                .builder()
                .description(journal.getDescription())
                .debitedAmount(journal.getDebitedAmount())
                .creditedAmount(journal.getCreditedAmount())
                .ledgerId(journal.getLedgerId())
                .subledgerId(journal.getSubledgerId())
                .build();
    }


}
