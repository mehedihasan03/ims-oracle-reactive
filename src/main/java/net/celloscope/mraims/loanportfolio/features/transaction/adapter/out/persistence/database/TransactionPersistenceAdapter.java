package net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.repository.TransactionHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.repository.TransactionRepository;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionGridViewQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.queries.TransactionReportQueryDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.queries.helpers.dto.TransactionGridViewDataDTO;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final CommonRepository repository;
    private final TransactionRepository transactionRepository;
    private final TransactionHistoryRepository historyRepository;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public TransactionPersistenceAdapter(TransactionRepository transactionRepository, CommonRepository repository, ModelMapper modelMapper, TransactionHistoryRepository historyRepository, Gson gson) {
        this.transactionRepository = transactionRepository;
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.gson = gson;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public Mono<Boolean> saveTransactionsToDb(List<Transaction> transactionList) {

//		log.info("List<Transaction> to save to DB : {}", transactionList);
        List<TransactionEntity> transactionEntityList = transactionList
                .stream()
                .map(transaction -> modelMapper.map(transaction, TransactionEntity.class))
                .toList();

        log.info("Total Transaction List to save to DB : {}", transactionEntityList.size());

        return transactionRepository
                .saveAll(transactionEntityList)
                .doOnRequest(value -> log.info("Request Received to save transaction entity list to db"))
                .doOnComplete(() -> log.info("Transaction Entity List successfully saved to db"))
                .doOnError(throwable -> log.info("Error happened while transaction list saving to db. Error : {}", throwable.getMessage()))
                .collectList()
                .flatMap(transactionEntities -> Mono.just(true))
                .onErrorReturn(false);
    }

    @Override
    public Mono<String> saveTransactionsIntoTransactionHistory(List<Transaction> transactionList) {
        return Flux
                .fromIterable(transactionList)
                .doOnComplete(() -> log.info("Transaction List Converted into Flux of Publisher"))
                .map(transaction -> {
                    TransactionHistoryEntity mapped = modelMapper.map(transaction, TransactionHistoryEntity.class);
                    mapped.setOid(null);
                    return mapped;
                })
                .flatMap(historyRepository::save)
                .then(Mono.just("Transaction History Data Saved Successfully"))
                ;
    }

    @Override
    public Flux<TransactionGridViewDataDTO> findTransactionGridViewData(TransactionGridViewQueryDTO queryDTO) {
        Flux<TransactionGridViewDataDTO> data = repository.getFieldOfficersByOfficeId(queryDTO.getOfficeId())
                .doOnNext(fieldOfficer -> log.info("Field officer: {}", fieldOfficer.toString()))
                .flatMap(fieldOfficer -> repository.getSamityByOfficeId(queryDTO.getOfficeId())
                        .doOnNext(samity -> log.info("Samity: {}", samity.toString()))
                        .filter(samity -> samity.getFieldOfficerId().equals(fieldOfficer.getFieldOfficerId()))
                        .doOnNext(samity -> log.info("Samity under officer {}: {}", fieldOfficer.getFieldOfficerId(), samity.toString()))
                        .mapNotNull(samity -> {
                            TransactionGridViewDataDTO dataDTO = modelMapper.map(samity, TransactionGridViewDataDTO.class);
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
        Flux<TransactionGridViewDataDTO> finalData = data;
        return transactionRepository.getTransactionCountBySamityId(queryDTO.getFromDate(), queryDTO.getToDate())
                .flatMap(countData -> finalData.filter(x -> x.getSamityId().equals(countData.getSamityId()))
                        .mapNotNull(c -> {
                            c.setTransactionCount(countData.getTransactionCount() == null ? 0 : countData.getTransactionCount());
                            c.setTransactionAmount(countData.getTransactionAmount() == null ? BigDecimal.ZERO : countData.getTransactionAmount());
                            c.setTotalMember(countData.getTotalMember() == null ? 0 : countData.getTotalMember());
                            return c;
                        }));
    }

    @Override
    public Flux<Transaction> findTransactionReportData(TransactionReportQueryDTO queryDTO) {
        return transactionRepository.getTransactionReportDataBySamityIdAndTransactionDateBetweenFromDateAndToDate(
                        queryDTO.getFromDate(),
                        queryDTO.getToDate(),
                        queryDTO.getSamityId(),
                        queryDTO.getAccountNo(),
                        queryDTO.getSearchText()
                )
                .doOnNext(data -> log.info("Transaction Report Data from DB: {}", data))
                .mapNotNull(x -> modelMapper.map(x, Transaction.class));
    }

    @Override
    public Mono<Samity> findSamityDetailsForTransactionReportData(String samityId) {
        return transactionRepository.getSamityForTransactionReport(samityId);
    }

    @Override
    public Mono<List<String>> getTransactionIdListForSamityByStagingDataId(List<String> stagingDataIdList) {
        return transactionRepository.findTransactionIdListByStagingDataIdList(stagingDataIdList)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Transaction Data Found")))
                .collectList()
                .doOnNext(stringList -> log.debug("Transaction ID List: {}", stringList));
    }

    @Override
    public Mono<List<String>> deleteTransactionForSamityByStagingDataIDList(List<String> stagingDataIdList) {
        return transactionRepository.findAllByStagingDataIdList(stagingDataIdList)
                .collectList()
                .flatMap(transactionRepository::deleteAll)
                .then(Mono.fromCallable(() -> stagingDataIdList));
    }

    @Override
    public Mono<SingleTransactionResponseDTO> saveSingleTransactionToDB(Transaction transaction) {
        return transactionRepository
                .save(modelMapper.map(transaction, TransactionEntity.class))
                .doOnRequest(l -> log.info("Request received to save transaction to db : {}", transaction))
                .doOnSuccess(transactionEntity -> log.info("Transaction Entity successfully saved to db : {}", transactionEntity))
                .map(transactionEntity -> modelMapper.map(transactionEntity, SingleTransactionResponseDTO.class))
                .doOnError(throwable -> log.info("Error happened while saving transaction to db. Error : {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<Transaction>> saveTransactionListToDB(List<Transaction> transactionList) {
        return Flux.fromIterable(transactionList)
                .map(transaction -> modelMapper.map(transaction, TransactionEntity.class))
                .flatMap(transactionRepository::save)
                .collectList()
                .map(transactionEntity -> transactionList);
    }

    @Override
    public Flux<Transaction> getLoanAdjustedTransactionsForLoanAccountsOfMember(String memberId) {
        return transactionRepository.findAllByMemberIdAndTransactionCodeAndLoanAccountIdNotNull(memberId, "LOAN_ADJUSTMENT")
                .map(transactionEntity -> modelMapper.map(transactionEntity, Transaction.class));
    }

    @Override
    public Flux<Transaction> getSavingsAccountsForLoanAdjustedTransactions(List<String> loanAdjustmentProcessIdList) {
        return transactionRepository.findAllBySavingsAccountIdNotNullAndLoanAdjustmentProcessIdIn(loanAdjustmentProcessIdList)
                .map(transactionEntity -> modelMapper.map(transactionEntity, Transaction.class));
    }

    @Override
    public Mono<List<Transaction>> getAllLoanDisbursementTransactionDataForSamityIdList(List<String> samityIdList) {
        return transactionRepository.findAllBySamityIdInAndTransactionDateGreaterThanEqual(samityIdList, LocalDate.now())
                .map(transactionEntity -> modelMapper.map(transactionEntity, Transaction.class))
                .collectList();
    }

    @Override
    public Flux<Transaction> getAllTransactionsOnABusinessDayForOffice(String managementProcessId, LocalDate businessDate) {
        return transactionRepository.findAllByManagementProcessId(managementProcessId)
                .map(entity -> modelMapper.map(entity, Transaction.class));
    }

    @Override
    public Mono<Boolean> checkIfTransactionAlreadyExistsBySavingsAccountIdAndTransactionCode(String savingsAccountId, String transactionCode) {
        return transactionRepository.existsTransactionEntityBySavingsAccountIdAndTransactionCode(savingsAccountId, transactionCode);
    }

    @Override
    public Mono<Boolean> checkIfTransactionAlreadyExistsByManagementProcessIdAndSavingsAccountIdAndTransactionCode(String managementProcessId, String savingsAccountId, String transactionCode) {
        return transactionRepository.existsTransactionEntityByManagementProcessIdAndSavingsAccountIdAndTransactionCode(managementProcessId, savingsAccountId, transactionCode);
    }

    @Override
    public Mono<String> deleteTransactionsForSamityUnauthorization(String managementProcessId, String transactionProcessId) {
        return transactionRepository.findAllByManagementProcessIdAndProcessId(managementProcessId, transactionProcessId)
                .collectList()
                .flatMap(transactionRepository::deleteAll)
                .then(Mono.just(transactionProcessId))
                .doOnNext(processId -> log.info("Transaction Entry Deleted for Process Id: {}", processId));
    }

    @Override
    public Mono<String> deleteTransactionsForTransactionCodeByManagementProcessId(String transactionCode, String managementProcessId) {
        return transactionRepository
                .findAllByTransactionCodeAndManagementProcessId(transactionCode, managementProcessId)
                .collectList()
                .flatMap(transactionRepository::deleteAll)
                .then(Mono.just("Transaction Entry Deleted for Transaction Code: " + transactionCode + " and Management Process Id: " + managementProcessId))
                ;
    }

    @Override
    public Flux<Transaction> getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode) {
        return transactionRepository.findAllByManagementProcessIdAndTransactionCode(managementProcessId, transactionCode)
                .map(entity -> modelMapper.map(entity, Transaction.class));
    }

    @Override
    public Mono<Transaction> createTransactionForWelfareFund(Transaction transaction) {
        return Mono.just(modelMapper.map(transaction, TransactionEntity.class))
                .flatMap(transactionRepository::save)
                .map(entity -> modelMapper.map(entity, Transaction.class));
    }

    @Override
    public Flux<Transaction> getWelfareFundTransactionForOfficeByManagementProcessId(String managementProcessId) {
        return transactionRepository.findAllByManagementProcessIdAndTransactionCode(managementProcessId, TransactionCodes.WELFARE_FUND.getValue())
                .map(entity -> modelMapper.map(entity, Transaction.class));
    }

    @Override
    public Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList) {
        return transactionRepository.findAllByManagementProcessIdAndTransactionCodeAndSavingsAccountIdIn(managementProcessId, TransactionCodes.INTEREST_DEPOSIT.getValue(), savingsAccountIdList)
                .collectList()
                .flatMap(transactionRepository::deleteAll)
                .then(Mono.just(savingsAccountIdList.size()))
                .doOnNext(count -> log.info("Total Interest Posting Entry Deleted: {}", count));
    }

    @Override
    public Mono<List<Transaction>> getTransactionEntriesByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId) {
        return transactionRepository
                .findAllByTransactionCodeAndManagementProcessId(transactionCode, managementProcessId)
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Transaction Data Found for the given Transaction Code and Management Process Id")))
                .map(transactionEntity -> modelMapper.map(transactionEntity, Transaction.class))
                .collectList()
                ;
    }

    @Override
    public Mono<Transaction> getTransactionByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .map(entity -> modelMapper.map(entity, Transaction.class));
    }
}
