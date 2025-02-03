package net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.entity.PassbookHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.ResultSet;
import net.celloscope.mraims.loanportfolio.features.passbookhistory.adapter.out.persistence.repository.PassbookHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.repository.PassbookRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.queries.PassbookReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.queries.helpers.dto.PassbookGridViewDataDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.DpsRepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.out.ISavingsAccountPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassbookPersistenceAdapter implements PassbookPersistencePort {

    private final PassbookRepository passbookRepository;
    private final PassbookHistoryRepository historyRepository;
    private final CommonRepository commonRepository;
    private final ModelMapper modelMapper;
    private final ISavingsAccountPersistencePort savingsAccountPersistencePort;
    private final DpsRepaymentSchedulePersistencePort dpsRepaymentSchedulePersistencePort;

    @Override
    public LocalDate getLastCreatedDate(String transactionCode, String loanAccountId) {
        return passbookRepository
                .getLastCreatedDate(transactionCode, loanAccountId);
        /*.doOnNext(localDate -> log.info("Received last Created Date from DB : {}", localDate));*/
    }

    @Override
    public Mono<Passbook> getLastPassbookEntry(String loanAccountId) {
        return passbookRepository
                .getLastPassbookEntry(loanAccountId)
                .doOnNext(passbookEntity -> log.info("Received Last Passbook Entry from DB for Loan Account: {}", passbookEntity.getLoanAccountId()))
                .switchIfEmpty(Mono.just(PassbookEntity.builder().build()))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<ResultSet> getResultSetFromPassbook(String loanAccountId, String transactionCode) {
        return passbookRepository
                .getResultSetFromPassbook(loanAccountId, transactionCode)
                .doOnRequest(value -> log.info("Request Received in Adapter to get resultSet. loanAccountId : {}, transactionCode : {}", loanAccountId, transactionCode))
                .doOnSuccess(resultSet -> log.info("Successfully received Resultset : {}", resultSet))
                .switchIfEmpty(Mono.just(ResultSet.builder().installNo(0).build()));
    }

    @Override
    public Mono<PassbookEntity> insertRecordPassbook(Passbook passbook) {
        /*log.info("Before mapping Passbook Domain from Adapter: {}", passbook);*/

        PassbookEntity passbookEntity = modelMapper.map(passbook, PassbookEntity.class);
        /*log.info("Mapped Passbook Entity from Adapter: {}", passbookEntity);*/
        return passbookRepository
                .save(passbookEntity)
                .doOnSuccess(entity -> log.info("Successfully persisted to DB"))
                .filter(entity -> entity.getSavingsAccountId() != null)
                .flatMap(entity ->
                        savingsAccountPersistencePort
                                .updateSavingsAccountBalance(passbookEntity.getSavingsAccountId(), passbookEntity.getSavgAcctEndingBalance(), Status.STATUS_ACTIVE.getValue())
                                .thenReturn(entity)
//                                .subscribeOn(Schedulers.immediate()).subscribe();
                );
    }

    @Override
    public Flux<Passbook> insertRecordPassbooks(Flux<Passbook> passbooks) {

        return passbooks
//                .doOnNext(passbook -> log.info("Passbook To Save to Db: {}", passbook))
                .map(passbook -> modelMapper.map(passbook, PassbookEntity.class))
                .collectList()
                .doOnNext(passbook -> log.info("Total Passbook Entry To Save to Db: {}", passbook.size()))
                .flatMapMany(passbookEntities -> passbookRepository.saveAll(Flux.fromIterable(passbookEntities))
                        .doOnComplete(() -> log.info("Successfully persisted to DB")))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> insertRecordPassbooksList(List<Passbook> passbooks) {
        return Flux.fromIterable(passbooks)
                .map(passbook -> modelMapper.map(passbook, PassbookEntity.class))
                .collectList()
                .flatMapMany(passbookRepository::saveAll)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<String> saveRecordsIntoPassBookHistory(List<PassbookResponseDTO> passbookDataList) {
        return Flux
                .fromIterable(passbookDataList)
                .map(passbookResponseDTO -> modelMapper.map(passbookResponseDTO, PassbookHistoryEntity.class))
                .collectList()
                .flatMapMany(historyRepository::saveAll)
                .doOnNext(passbookHistoryEntity -> log.info("Successfully Saved passbookDataList into Passbook History"))
                .then(Mono.just("Successfully Saved into Passbook History"))
                ;

    }

    @Override
    public Mono<Passbook> getLastPassbookEntryBySavingsAccountId(String savingsAccountId) {
        return passbookRepository
                .getLastPassbookEntryBySavingsAccountId(savingsAccountId)
                /*.doOnNext(passbookEntity -> log.info("savings passbook entity from db : {}", passbookEntity))*/
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<PassbookGridViewDataDTO> findPassbookGridViewData(PassbookGridViewQueryDTO queryDTO) {
        Flux<PassbookGridViewDataDTO> data = commonRepository.getFieldOfficersByOfficeId(queryDTO.getOfficeId())
                .doOnNext(fieldOfficer -> log.info("Field officer: {}", fieldOfficer.toString()))
                .flatMap(fieldOfficer -> commonRepository.getSamityByOfficeId(queryDTO.getOfficeId())
                        .doOnNext(samity -> log.info("Samity: {}", samity.toString()))
                        .filter(samity -> samity.getFieldOfficerId().equals(fieldOfficer.getFieldOfficerId()))
                        .doOnNext(samity -> log.info("Samity under officer {}: {}", fieldOfficer.getFieldOfficerId(), samity.toString()))
                        .mapNotNull(samity -> {
                            PassbookGridViewDataDTO dataDTO = modelMapper.map(samity, PassbookGridViewDataDTO.class);
                            dataDTO.setFieldOfficerNameEn(fieldOfficer.getFieldOfficerNameEn());
                            dataDTO.setFieldOfficerNameBn(fieldOfficer.getFieldOfficerNameBn());
                            return dataDTO;
                        }));
        if (Strings.isNotNullAndNotEmpty(queryDTO.getFieldOfficerId())) {
            data = data.filter(x -> x.getFieldOfficerId().equals(queryDTO.getFieldOfficerId()));
        }
        if (Strings.isNotNullAndNotEmpty(queryDTO.getSamityId())) {
            data = data.filter(x -> x.getSamityId().equals(queryDTO.getSamityId()));
        }
        Flux<PassbookGridViewDataDTO> finalData = data;
        return passbookRepository.getPassbookCountGroupBySamityId(queryDTO.getFromDate(), queryDTO.getToDate())
                .flatMap(countData -> finalData.filter(x -> x.getSamityId().equals(countData.getSamityId()))
                        .mapNotNull(c -> {
                            c.setPassbookCount(countData.getPassbookCount() == null ? 0 : countData.getPassbookCount());
                            c.setTotalMember(countData.getTotalMember() == null ? 0 : countData.getTotalMember());
                            return c;
                        }));
    }

    @Override
    public Flux<Passbook> findPassbookReportData(PassbookReportQueryDTO queryDTO) {
        log.info("db call with : {}", queryDTO);
        return passbookRepository.getPassbookReportDataBySamityIdAndTransactionDateBetweenFromDateAndToDate(
                        queryDTO.getFromDate(),
                        queryDTO.getToDate(),
                        queryDTO.getSamityId(),
                        queryDTO.getAccountNo(),
                        queryDTO.getSearchText()
                )
                .doOnNext(data -> log.info("Passbook Report Data from DB: {}", data))
                .mapNotNull(x -> modelMapper.map(x, Passbook.class));
    }

    @Override
    public Flux<Passbook> findPassbookReportDataV2(PassbookReportQueryDTO queryDTO) {
        return passbookRepository.getPassbookReportV2DataBySamityIdAndTransactionDateBetweenFromDateAndToDate(
                        queryDTO.getFromDate(),
                        queryDTO.getToDate(),
                        queryDTO.getSamityId(),
                        queryDTO.getAccountNo(),
                        queryDTO.getSearchText()
                )
                .doOnRequest(l -> log.info("call hoise "))
                .doOnNext(data -> log.info("Passbook Report Data from DB: {}", data))
                .mapNotNull(x -> modelMapper.map(x, Passbook.class));
    }

    @Override
    public Mono<Samity> findSamityDetailsForPassbookReportData(String samityId) {
        return passbookRepository.getSamityForPassbookReport(samityId);
    }

    @Override
    public Flux<Passbook> getPassbookEntriesBySavingsAccountIDAndDate(String savingsAccountId, LocalDate transactionDate) {
        return passbookRepository
                .findPassbookEntitiesBySavingsAccountIdAndTransactionDateOrderByCreatedOn(savingsAccountId, transactionDate)
                .doOnRequest(value -> log.info("Request Received to get Passbook entries with savingsAccountId : {} , transactionDate : {}", savingsAccountId, transactionDate))
                .doOnNext(passbookEntity -> log.info("Passbook entities received : {}", passbookEntity))
                .doOnError(throwable -> log.error("Error happened while getting passbook entries. {}", throwable.getMessage()))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<String> getRepayScheduleIdByTransactionList(List<String> transactionIdList) {
        return passbookRepository.getRepayScheduleIdListByTransactionList(transactionIdList);
        /*.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Passbook Entry Found")));*/
    }

    @Override
    public Mono<List<String>> deletePassbookEntryByTransaction(List<String> transactionIdList) {
        return passbookRepository.getPassbookEntitiesByTransactionIdList(transactionIdList)
                .collectList()
                .flatMap(passbookRepository::deleteAll)
                .then(Mono.fromCallable(() -> transactionIdList));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByYearMonthAndSavingsAccountOid(Integer yearValue, Integer monthValue, String savingsAccountOid) {
        return passbookRepository
                .getPassbookEntitiesByYearMonthAndSavingsAccountOid(yearValue, monthValue, savingsAccountOid)
                .doOnRequest(value -> log.info("Request Received to get Passbook entries with savingsAccountId : {} , year : {}, month : {}", savingsAccountOid, yearValue, monthValue))
                .doOnNext(passbookEntity -> log.info("Passbook entities received : {}", passbookEntity))
                .doOnError(throwable -> log.error("Error happened while getting passbook entries. {}", throwable.getMessage()))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getLoanPassbookEntriesByProcessManagementId(String managementProcessId) {
        return passbookRepository
                .getPassbookEntitiesByManagementProcessIdAndLoanAccountIdIsNotNull(managementProcessId)
//                .getPassbookEntitiesByLoanAccountIdIsNotNull()
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with managementProcessId : {} and loan account id is not null", managementProcessId))
                .doOnComplete(() -> log.info("successfully fetched loan passbook entries by managementProcessId : {}", managementProcessId))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getSavingsPassbookEntriesByProcessManagementId(String managementProcessId) {
        return passbookRepository
                .getPassbookEntitiesByManagementProcessIdAndSavingsAccountIdIsNotNull(managementProcessId)
//                .getPassbookEntitiesBySavingsAccountIdIsNotNull()
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with managementProcessId : {} and savings account id is not null", managementProcessId))
                .doOnComplete(() -> log.info("successfully fetched savings passbook entries by managementProcessId : {}", managementProcessId))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByProcessManagementIdAndPaymentMode(String managementProcessId, String paymentMode) {
        return passbookRepository
                .getPassbookEntitiesByManagementProcessIdAndPaymentMode(managementProcessId, paymentMode)
//                .getPassbookEntitiesByPaymentMode(paymentMode)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with managementProcessId : {} and paymentMode : {}", managementProcessId, paymentMode))
                .doOnComplete(() -> log.info("successfully fetched all passbook entries by managementProcessId : {} and paymentMode : {}", managementProcessId, paymentMode))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<Passbook> getDisbursementPassbookEntryByDisbursedLoanAccountId(String disbursedLoanAccountId) {
        return passbookRepository
                .getPassbookEntityByDisbursedLoanAccountId(disbursedLoanAccountId)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getWithdrawPassbookEntriesByManagementProcessIdAndPaymentMode(String managementProcessId, String paymentMode) {
        if (!HelperUtil.checkIfNullOrEmpty(paymentMode)) {
            return passbookRepository
                    .getPassbookEntitiesByManagementProcessIdAndPaymentModeAndWithdrawAmountIsNotNull(managementProcessId, paymentMode)
                    .doOnRequest(l -> log.info("request received to get withdraw passbook entities by managementProcessId : {}, paymentMode : {}", managementProcessId, paymentMode))
                    .doOnNext(passbookEntity -> log.info("withdraw passbook entity received : {}", passbookEntity))
                    .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
        } else {
            return passbookRepository
                    .getPassbookEntitiesByManagementProcessIdAndWithdrawAmountIsNotNull(managementProcessId)
                    .doOnRequest(l -> log.info("request received to get withdraw passbook entities by managementProcessId : {}, paymentMode : {}", managementProcessId, paymentMode))
                    .doOnNext(passbookEntity -> log.info("withdraw passbook entity received : {}", passbookEntity))
                    .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
        }
    }

    @Override
    public Mono<List<Passbook>> createPassbookEntryForLoanAdjustment(List<Passbook> passbookList) {
        return Flux.fromIterable(passbookList)
                .map(passbook -> {
                    PassbookEntity passbookEntity = modelMapper.map(passbook, PassbookEntity.class);
                    passbookEntity.setPassbookNumber(UUID.randomUUID().toString());
                    return passbookEntity;
                })
                .flatMap(passbookRepository::save)
                .doOnNext(passbookEntity -> {
                    if (passbookEntity.getSavingsAccountId() != null) {
                        savingsAccountPersistencePort
                                .updateSavingsAccountBalance(passbookEntity.getSavingsAccountId(), passbookEntity.getSavgAcctEndingBalance(), Status.STATUS_ACTIVE.getValue())
                                .subscribeOn(Schedulers.immediate()).subscribe();
                    }
                })
                .collectList()
                .map(passbookEntities -> passbookList);
    }

    @Override
    public Mono<Passbook> createPassbookEntryForLoanRebate(Passbook passbook) {
        return Mono.just(passbook)
                .map(passbook1 -> modelMapper.map(passbook1, PassbookEntity.class))
                .flatMap(passbookRepository::save)
                .map(entity -> modelMapper.map(entity, Passbook.class))
                .doOnNext(passbook1 -> log.info("Successfully saved passbook entries for loan rebate"));
    }

    @Override
    public Mono<Passbook> getLastPassbookEntryBySavingsAccountOid(String savingsAccountOid) {
        return passbookRepository
                .getLastPassbookEntryBySavingsAccountOid(savingsAccountOid)
                /*.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No passbook Record Found with SavingsAccountOid!")))*/
                /*.doOnNext(passbookEntity -> log.info("savings passbook entity from db : {}", passbookEntity))*/
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<Passbook> getLastInterestDepositPassbookEntryBySavingsAccountOid(String savingsAccountOid) {
        return passbookRepository
                .getFirstBySavingsAccountOidAndTotalAccruedInterDepositIsNotNullOrderByTransactionDateDesc(savingsAccountOid)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<Passbook> getLastWithdrawPassbookEntryBySavingsAccountOid(String savingsAccountOid) {
        return passbookRepository
                .getFirstBySavingsAccountOidAndWithdrawAmountIsNotNullOrderByTransactionDateDesc(savingsAccountOid)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesBetweenTransactionDates(String savingsAccountOid, LocalDate fromDate, LocalDate toDate) {
        return passbookRepository
                .getPassbookEntitiesBySavingsAccountOidAndTransactionDateIsBetween(savingsAccountOid, fromDate, toDate)
                .doOnRequest(l -> log.info("Requesting to fetch passbook entries with savingsAccountOid : {} | from : {} | to : {}", savingsAccountOid, fromDate, toDate))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<List<String>> deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(String managementProcessId, String passbookProcessId) {
        final List<String> loanRepayScheduleIdList = new ArrayList<>();
        return passbookRepository.findAllByManagementProcessIdAndProcessId(managementProcessId, passbookProcessId)
                .doOnNext(passbookEntity -> {
                    if (!HelperUtil.checkIfNullOrEmpty(passbookEntity.getLoanRepayScheduleId())) {
                        loanRepayScheduleIdList.add(passbookEntity.getLoanRepayScheduleId());
                    }
                })
                .collectList()
                .flatMap(this::updateDpsRepaymentSchedule)
                .flatMap(this::updateSavingsAccountBalance)
                .flatMap(passbookRepository::deleteAll)
                .then(Mono.just(loanRepayScheduleIdList));
    }

    private Mono<List<PassbookEntity>> updateDpsRepaymentSchedule(List<PassbookEntity> passbookEntityList) {
        return Flux.fromIterable(passbookEntityList)
                .filter(passbookEntity -> passbookEntity.getTransactionCode().equalsIgnoreCase(TransactionCodes.SAVINGS_DEPOSIT.getValue()))
                .filter(passbookEntity -> Strings.isNotNullAndNotEmpty(passbookEntity.getSavingsTypeId()) && passbookEntity.getSavingsTypeId().equalsIgnoreCase(SavingsProductType.SAVINGS_TYPE_ID_DPS.getValue()))
                .flatMap(passbookEntity -> dpsRepaymentSchedulePersistencePort.updateDPSRepaymentScheduleStatusByManagementProcessId(passbookEntity.getManagementProcessId(), Status.STATUS_PENDING.getValue(), passbookEntity.getCreatedBy()))
                .collectList()
                .map(booleans -> passbookEntityList);
    }


    private Mono<List<PassbookEntity>> updateSavingsAccountBalance(List<PassbookEntity> passbookEntityList) {
        return Flux.fromIterable(passbookEntityList)
                .filter(passbookEntity -> passbookEntity.getTransactionCode().equalsIgnoreCase(TransactionCodes.SAVINGS_DEPOSIT.getValue()))
                .flatMap(passbookEntity -> savingsAccountPersistencePort.updateSavingsAccountBalance(passbookEntity.getSavingsAccountId(), passbookEntity.getSavgAcctBeginBalance(), Status.STATUS_ACTIVE.getValue()))
                .collectList()
                .map(booleans -> passbookEntityList);
    }

    @Override
    public Mono<String> deletePassbookEntriesForTransactionCodeByManagementProcessId(String transactionCode, String managementProcessId) {
        return passbookRepository
                .findAllByManagementProcessIdAndTransactionCode(managementProcessId, transactionCode)
                .doOnComplete(() -> log.info("Passbook Entries Found for Transaction Code: {} and Management Process Id: {}", transactionCode, managementProcessId))
                .collectList()
                .flatMap(passbookRepository::deleteAll)
                .then(Mono.just("Successfully Deleted Passbook Entries for Transaction Code: " + transactionCode + " and Management Process Id: " + managementProcessId))
                ;
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId(String processManagementId, String transactionCode, String paymentMode, String savingsTypeId) {
        /*return Flux.from(HelperUtil.checkIfNullOrEmpty(paymentMode)
                ? passbookRepository.
                    findAllByManagementProcessIdAndTransactionCodeAndPaymentModeIsNull(processManagementId, transactionCode)
                        .doOnRequest(l -> log.info("null method called"))
                        .doOnRequest(l -> log.info("processManagementId : {} | transactionCode : {} | paymentMode : {}", processManagementId, transactionCode, paymentMode))
                : passbookRepository
                    .findAllByManagementProcessIdAndTransactionCodeAndPaymentMode(processManagementId, transactionCode, paymentMode)
                    .doOnRequest(l -> log.info("processManagementId : {} | transactionCode : {} | paymentMode : {}", processManagementId, transactionCode, paymentMode)))
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));*/

        return passbookRepository
                .findAllByManagementProcessIdAndTransactionCodeAndPaymentMode(processManagementId, transactionCode, paymentMode)
                .flatMap(passbookEntity -> {
                    log.info("passbook entity : {}", passbookEntity);
                    if (!HelperUtil.checkIfNullOrEmpty(savingsTypeId) && !HelperUtil.checkIfNullOrEmpty(passbookEntity.getSavingsAccountId())) {
                        return commonRepository.getSavingsTypeIdBySavingsAccountId(passbookEntity.getSavingsAccountId())
                                .map(savingsTypeIdFromDb -> {
                                    if (savingsTypeIdFromDb.equals(savingsTypeId)) {
                                        return passbookEntity;
                                    } else {
                                        return PassbookEntity.builder().build();
                                    }
                                });
                    } else {
                        return Flux.just(passbookEntity);
                    }
                })
                .filter(passbookEntity -> !HelperUtil.checkIfNullOrEmpty(passbookEntity.getOid()))
                .doOnRequest(l -> log.info("processManagementId : {} | transactionCode : {} | paymentMode : {} | savingsTypeId : {}", processManagementId, transactionCode, paymentMode, savingsTypeId))
                .doOnNext(passbookEntity -> log.info("passbook entity after db fetch : {}", passbookEntity))
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntriesByProcessManagementIdAndTransactionCodeAndSavingsTypeId(String processManagementId, String transactionCode, String savingsTypeId) {
        return passbookRepository
                .findAllByManagementProcessIdAndTransactionCodeAndSavingsTypeIdAndSavingsTypeIdNotNull(processManagementId, transactionCode, savingsTypeId)
                .doOnRequest(l -> log.info("processManagementId : {} | transactionCode : {} | savingsTypeId : {}", processManagementId, transactionCode, savingsTypeId))
                .doOnNext(passbookEntity -> log.info("passbook entity by savings type id after db fetch : {}", passbookEntity))
                .switchIfEmpty(Flux.empty())
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryByLoanAccountOidAndTransactionCodes(String loanAccountOid, List<String> transactionCodes) {
        return passbookRepository.getLastPassbookEntryByLoanAccountOidAndTransactionCodes(loanAccountOid, transactionCodes)
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Mono<PassbookResponseDTO> getLastPassbookEntryByTransactionCodeAndLoanAccountOid(String transactionCode, String loanAccountOid) {
        return passbookRepository.getPassbookEntitiesByTransactionCodeAndLoanAccountOid(transactionCode, loanAccountOid)
                .sort(Comparator.comparing(PassbookEntity::getCreatedOn, Comparator.reverseOrder())
                        .thenComparing(PassbookEntity::getInstallNo, Comparator.reverseOrder()))
                .doOnNext(passbookEntity -> log.info("after sorting by createdOn : {} | {}", passbookEntity.getInstallNo(), passbookEntity.getCreatedOn()))
                .next()
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Flux<PassbookResponseDTO> getPassbookEntryByTransactionCodeAndSavingsAccountOid(String transactionCode, String savingsAccountOid) {
        return passbookRepository
                .getPassbookEntitiesByTransactionCodeAndSavingsAccountOid(transactionCode, savingsAccountOid)
                .map(passbookEntity -> modelMapper.map(passbookEntity, PassbookResponseDTO.class));
    }

    @Override
    public Mono<Passbook> createPassbookEntryForWelfareFund(Passbook passbook) {
        return passbookRepository.getLastPassbookEntry(passbook.getWelfareFundLoanAccountId())
                .switchIfEmpty(passbookRepository.getPassbookEntityByDisbursedLoanAccountId(passbook.getWelfareFundLoanAccountId()))
                .map(lastPassbookEntry -> {
                    PassbookEntity passbookEntity = modelMapper.map(passbook, PassbookEntity.class);
                    passbookEntity.setPassbookNumber(lastPassbookEntry.getPassbookNumber());
                    passbookEntity.setLoanAccountOid(lastPassbookEntry.getLoanAccountOid());
                    return passbookEntity;
                })
                .flatMap(passbookRepository::save)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList) {
        return passbookRepository.findAllByManagementProcessIdAndTransactionCodeAndSavingsAccountIdIn(managementProcessId, TransactionCodes.INTEREST_DEPOSIT.getValue(), savingsAccountIdList)
                .collectList()
                .flatMap(passbookRepository::deleteAll)
                .then(Mono.just(savingsAccountIdList.size()))
                .doOnNext(count -> log.info("Total Interest Posting Entry Deleted: {}", count));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesForAdvanceLoanRepaymentDebit(String officeId, LocalDate businessDate) {
        return passbookRepository
                .findAllByOfficeIdAndInstallDateEqualsAndTransactionDateIsBeforeAndLoanAccountIdIsNotNull(officeId, businessDate, businessDate)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with officeId : {} and installDate equals : {} & transactionDate before : {}", officeId, businessDate, businessDate))
                .collectList()
                .doOnNext(passbookEntities -> log.info("Advance Loan Repayment Debit Passbook Entries received with size : {}", passbookEntities.size()))
                .flatMapMany(Flux::fromIterable)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesForAdvanceLoanRepaymentCredit(String officeId, LocalDate businessDate) {
        return passbookRepository
                .findAllByOfficeIdAndInstallDateIsAfterAndTransactionDateEqualsAndLoanAccountIdIsNotNull(officeId, businessDate, businessDate)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with officeId : {} and installDate is after : {} & transactionDate equals : {}", officeId, businessDate, businessDate))
                .collectList()
                .doOnNext(passbookEntities -> log.info("Advance Loan Repayment Credit Passbook Entries received with size : {}", passbookEntities.size()))
                .flatMapMany(Flux::fromIterable)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByInstallDateEqualsBusinessDate(String officeId, LocalDate businessDate) {
        return passbookRepository.findAllByOfficeIdAndInstallDateEqualsAndLoanAccountIdIsNotNull(officeId, businessDate)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with officeId : {} and installDate equals : {}", officeId, businessDate))
                .collectList()
                .doOnNext(passbookEntities -> log.info("Passbook Entries with officeId : {} and installDate equals : {} received with size : {}", officeId, businessDate, passbookEntities.size()))
                .flatMapMany(Flux::fromIterable)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByInstallDateBeforeBusinessDateAndTransactionDateEqualsBusinessDate(String officeId, LocalDate businessDate) {
        return passbookRepository.findAllByOfficeIdAndInstallDateIsBeforeAndTransactionDateEqualsAndLoanAccountIdIsNotNull(officeId, businessDate, businessDate)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with officeId : {} and installDate is before : {} & transactionDate equals : {}", officeId, businessDate, businessDate))
                .collectList()
                .doOnNext(passbookEntities -> log.info("Passbook Entries with officeId : {} and installDate is before : {} & transactionDate equals : {} received with size : {}", officeId, businessDate, businessDate, passbookEntities.size()))
                .flatMapMany(Flux::fromIterable)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId) {
        return passbookRepository
                .findAllByTransactionCodeAndManagementProcessId(transactionCode, managementProcessId)
                .doOnRequest(l -> log.info("requesting to fetch passbook entities with transactionCode : {} and managementProcessId : {}", transactionCode, managementProcessId))
                .doOnComplete(() -> log.info("Passbook Entries with transactionCode : {} and managementProcessId : {} received", transactionCode, managementProcessId))
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class))
                .doOnComplete(() -> log.info("Mapped passbookEntity into Passbook"));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByTransactionDateAndLater(String accountId, String accountType, LocalDate transactionDate) {
        return accountType.equals(Constants.ACCOUNT_TYPE_LOAN.getValue())
                ? passbookRepository.findAllByLoanAccountIdAndTransactionDateIsGreaterThanEqual(accountId, transactionDate)
                    .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class))
                : passbookRepository.findAllBySavingsAccountIdAndTransactionDateIsGreaterThanEqual(accountId, transactionDate)
                    .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

    @Override
    public Mono<Boolean> deletePassbookEntriesByOid(List<String> passbookOids) {
        return passbookRepository.deleteAllById(passbookOids)
                .then(Mono.just(true));
    }

    @Override
    public Flux<Passbook> getPassbookEntriesByTransactionId(String transactionId) {
        return passbookRepository.findAllByTransactionId(transactionId)
                .map(passbookEntity -> modelMapper.map(passbookEntity, Passbook.class));
    }

}
