package net.celloscope.mraims.loanportfolio.features.transaction.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.ITransactionCommands;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.CollectionDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.StagingDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.WithdrawDataDTO;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.StageWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out.WithdrawPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Service
@Slf4j
public class TransactionService implements TransactionUseCase {
	private final IStagingDataUseCase stagingDataUseCase;
	private final CollectionStagingDataQueryUseCase collectionUseCase;
	private final ITransactionCommands iTransactionCommands;
	private final ModelMapper mapper;
	private final TransactionPersistencePort port;
	private final StageWithdrawUseCase stageWithdrawUseCase;
	private final CommonRepository commonRepository;
	private final FDRPersistencePort fdrPersistencePort;
	private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
	private final LoanRepaymentScheduleUseCase repaymentScheduleUseCase;

	public TransactionService(IStagingDataUseCase stagingDataUseCase, CollectionStagingDataQueryUseCase collectionUseCase, ITransactionCommands iTransactionCommands, TransactionPersistencePort port, WithdrawPersistencePort withdrawPersistencePort, StageWithdrawUseCase stageWithdrawUseCase, CommonRepository commonRepository, FDRPersistencePort fdrPersistencePort, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, LoanRepaymentScheduleUseCase repaymentScheduleUseCase) {
		this.stagingDataUseCase = stagingDataUseCase;
		this.collectionUseCase = collectionUseCase;
		this.iTransactionCommands = iTransactionCommands;
		this.port = port;
		this.stageWithdrawUseCase = stageWithdrawUseCase;
		this.commonRepository = commonRepository;
		this.fdrPersistencePort = fdrPersistencePort;
		this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.repaymentScheduleUseCase = repaymentScheduleUseCase;
        this.mapper = new ModelMapper();
	}

	@Override
	public Mono<TransactionResponseDTO> createTransactionForOneSamity(String samityId, String managementProcessId, String transactionProcessId, String officeId, String source) {
		List<String> transactionCodesToBeFilteredOut = List.of(TRANSACTION_CODE_LOAN_REPAY_PRIN.getValue(), TRANSACTION_CODE_LOAN_REPAY_SC.getValue());
		return stagingDataUseCase
				.getStagingDataBySamityId(samityId)
				.flatMap(stagingDataResponseDTO -> collectionUseCase
						.getCollectionStagingDataForSamityMembers(stagingDataResponseDTO.getStagingDataId())
						.filter(collectionStagingDataResponseDTO -> collectionStagingDataResponseDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
						.filter(collectionStagingDataResponseDTO -> !collectionStagingDataResponseDTO.getCollectionType().equalsIgnoreCase(CollectionType.REBATE.getValue()))
						.flatMap(collectionStagingDataResponseDTO -> {
							if (collectionStagingDataResponseDTO.getCollectionType().equalsIgnoreCase(CollectionType.SINGLE.getValue())) {
								return repaymentScheduleUseCase.archiveAndUpdateRepaymentScheduleForSeasonalSingleLoan(collectionStagingDataResponseDTO)
										.doOnRequest(l -> log.info("request received to archive and update repayment schedule for seasonal single loan. "))
										.doOnSuccess(aBoolean -> log.info("Repayment Schedule Archived and Updated Successfully for seasonal single loan. "))
										.doOnError(throwable -> log.error("Failed to archive and update repayment schedule for seasonal single loan. ", throwable))
										.thenReturn(collectionStagingDataResponseDTO);
							}
							return Mono.just(collectionStagingDataResponseDTO);
						})
						.map(collectionStagingDataResponseDTO -> Tuples.of(collectionStagingDataResponseDTO, stagingDataResponseDTO)))
				.flatMap(tuples -> managementProcessTrackerUseCase
						.getCurrentBusinessDateForOffice(managementProcessId, officeId)
						.map(businessDate -> Tuples.of(tuples.getT1(), tuples.getT2(), businessDate)))
				.flatMap(tuples -> {
					CollectionDataDTO collectionDataDTO = mapper.map(tuples.getT1(), CollectionDataDTO.class);
					StagingDataDTO stagingDataDTO = mapper.map(tuples.getT2(), StagingDataDTO.class);
					stagingDataDTO.setManagementProcessId(managementProcessId);
					stagingDataDTO.setProcessId(transactionProcessId);
					stagingDataDTO.setBusinessDate(tuples.getT3());

					return iTransactionCommands.buildTransaction(collectionDataDTO, stagingDataDTO);})
				.map(transaction -> {
					transaction.setOfficeId(officeId);
					transaction.setSource(source);
					return transaction;
				})
				.collectList()
				.doOnNext(transactions -> log.info("Transaction List prepared : {}", transactions))
				.flatMap(transactionList -> port
						.saveTransactionsToDb(transactionList)
						.map(aBoolean -> Tuples.of(transactionList
									.stream()
									.filter(transaction -> !transactionCodesToBeFilteredOut.contains(transaction.getTransactionCode()))
									.toList(),
								aBoolean)))
				.flatMap(tuple2 -> tuple2.getT2()
						? Mono.just(TransactionResponseDTO
							.builder()
							.transactionList(tuple2.getT1())
							.transactionCount(tuple2.getT1().size())
							.build())
						: Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.FAILED_TO_SAVE_TRANSACTION.getValue())));
	}


	@Override
	public Mono<TransactionResponseDTO> createTransactionForWithdrawBySamityId(String managementProcessId, String transactionProcessId, String samityId, String officeId) {
		return stagingDataUseCase
				.getStagingDataBySamityId(samityId)
				.flatMap(stagingDataResponseDTO -> stageWithdrawUseCase
						.getStagingWithdrawDataByStagingDataId(stagingDataResponseDTO.getStagingDataId())
						.filter(stagingWithdrawDataDTO -> stagingWithdrawDataDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
						.map(stagingWithdrawDataDTO -> Tuples.of(stagingWithdrawDataDTO, stagingDataResponseDTO)))
				.flatMap(tuples -> managementProcessTrackerUseCase
						.getCurrentBusinessDateForOffice(managementProcessId, officeId)
						.map(businessDate -> Tuples.of(tuples.getT1(), tuples.getT2(), businessDate)))
				.flatMap(tuples -> {
					WithdrawDataDTO withdrawDataDTO = mapper.map(tuples.getT1(), WithdrawDataDTO.class);
					StagingDataDTO stagingDataDTO = mapper.map(tuples.getT2(), StagingDataDTO.class);
					stagingDataDTO.setBusinessDate(tuples.getT3());
					return iTransactionCommands.buildTransactionForWithdraw(withdrawDataDTO, stagingDataDTO, managementProcessId, transactionProcessId, officeId);
				})
				.collectList()
				.flatMap(transactionList -> port.saveTransactionsToDb(transactionList)
						.thenReturn(TransactionResponseDTO
								.builder()
								.transactionList(transactionList)
								.transactionCount(transactionList.size())
								.build()));
	}

	@Override
	public Mono<List<String>> getTransactionIdListForSamityByStagingIDList(List<String> stagingDataIdList) {
		return port.getTransactionIdListForSamityByStagingDataId(stagingDataIdList)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Transaction Data Found")));
	}

	@Override
	public Mono<List<String>> deleteTransactionForSamityByStagingDataIDList(List<String> stagingDataIdList) {
		return port.deleteTransactionForSamityByStagingDataIDList(stagingDataIdList);
	}

	@Override
	public Mono<Transaction> createTransactionForSavingsInterestDeposit(String loginId, String savingsAccountId, LocalDate interestPostingDate, BigDecimal accruedInterest, String officeId) {
		return commonRepository
				.getStagingDataEntityBySavingsAccountId(savingsAccountId)
				.doOnRequest(l -> log.info("request received to fetch staging data entity with savings account id : {}", savingsAccountId))
				.doOnNext(stagingDataEntity -> log.info("Staging data entity received : {}", stagingDataEntity))
				.flatMap(stagingDataEntity -> iTransactionCommands
						.buildTransactionForInterestDeposit(loginId, stagingDataEntity, savingsAccountId, accruedInterest, interestPostingDate, officeId)
						.doOnRequest(l -> log.info("request received to build transaction. "))
						.doOnNext(transaction -> log.info("Transaction built successfully : {}", transaction)))
				.flatMap(transaction -> port
						.saveTransactionsToDb(List.of(transaction))
						.doOnRequest(l -> log.info("request received to save transaction to db : {}", transaction))
						.thenReturn(transaction));
						/*.thenReturn(TransactionResponseDTO
								.builder()
								.transactionList(List.of(transaction))
								.transactionCount(List.of(transaction).size())
								.build()));*/
	}

	@Override
	public Mono<TransactionResponseDTO> createTransactionForHalfYearlyInterestPosting(String savingsAccountId, Integer interestPostingYear, String closingType, String loginId) {
		/*return accruedInterestUseCase
				.getAccruedInterestEntriesBySavingsAccountIdYearAndClosingType(savingsAccountId, interestPostingYear, closingType)
				.doOnRequest(l -> log.info("request received to fetch accrued interest entities with savingsAccountId : {}, interestCalculationYear : {}, closingType : {}, loginId : {}", savingsAccountId, interestPostingYear, closingType, loginId))
				.collectList()
				.doOnSuccess(list -> log.info("accrued interest entities received. {}", list))
				.zipWith(commonRepository.getStagingDataEntityBySavingsAccountId(savingsAccountId))
				.flatMap(tuples -> getListOfAccruedInterestIdAndTransaction(tuples, loginId, savingsAccountId))
				.flatMap(tupleOfAccruedIdListAndTransaction -> {
					List<String> accruedInterestIdList = tupleOfAccruedIdListAndTransaction.getT1();
					Transaction transaction = tupleOfAccruedIdListAndTransaction.getT2();
					return port.saveSingleTransactionToDB(transaction)
							.flatMap(savedTransactionResponseDTO ->
									accruedInterestUseCase
											.updateTransactionIdAndStatusByAccruedInterestIdList(accruedInterestIdList, savedTransactionResponseDTO.getTransactionId(), Status.STATUS_PAID.getValue()))
							.thenReturn(TransactionResponseDTO
									.builder()
									.transactionList(List.of(transaction))
									.transactionCount(List.of(transaction).size())
									.build());
				});*/
		return null;
	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForLoanDisbursement(String disbursementLoanAccountId, BigDecimal loanAmount, LocalDate disbursementDate, String memberId, String mfiId, String loginId, String managementProcessId, String officeId, String source) {
		return commonRepository.getMemberSamityOfficeInfoByLoanAccountId(disbursementLoanAccountId)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Member Samity Office Info Not Found for Loan Account Id : " + disbursementLoanAccountId)))
				.flatMap(memberSamityOfficeEntity -> iTransactionCommands
					.buildTransactionForDisbursement(disbursementLoanAccountId, loanAmount, disbursementDate, memberId, mfiId, loginId, managementProcessId, officeId, source, memberSamityOfficeEntity))
					.flatMap(port::saveSingleTransactionToDB);
	}

	@Override
	public Mono<TransactionResponseDTO> createTransactionForMultipleFDRInterestPosting(String loginId, String savingsAccountId, LocalDate interestPostingDate) {
		/*return commonRepository
				.getStagingDataEntityBySavingsAccountId(savingsAccountId)
				.doOnRequest(l -> log.info("request received to fetch staging data entity with savings account id : {}", savingsAccountId))
				.doOnNext(stagingDataEntity -> log.info("Staging data entity received : {}", stagingDataEntity))
				.flatMapMany(stagingDataEntity -> fdrPersistencePort
								.getFDRInterestPostingSchedulesByDateAndStatus(interestPostingDate, Status.STATUS_PENDING.getValue())
								.doOnNext(fdrSchedule -> log.info("FDR Schedule Received for Interest Posting : {}", fdrSchedule))
								.flatMap(fdrSchedule -> iTransactionCommands
												.buildTransactionForInterestDeposit(loginId, stagingDataEntity, savingsAccountId, fdrSchedule.getCalculatedInterest(), interestPostingDate, officeId)))
				.collectList()
				.doOnSuccess(transactionList -> log.info("FDR Interest Posting Transaction List built"))
				.flatMap(transactionList -> port.saveTransactionsToDb(transactionList)
						.thenReturn(TransactionResponseDTO
								.builder()
								.transactionList(transactionList)
								.transactionCount(transactionList.size())
								.build()));*/
		return null;

	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForSingleFDRInterest(String loginId, String savingsAccountId, LocalDate interestPostingDate, BigDecimal calculatedInterest, String officeId) {
		return commonRepository
				.getStagingDataEntityBySavingsAccountId(savingsAccountId)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_STAGING_DATA_FOUND.getValue())))
				.doOnRequest(l -> log.info("request received to fetch staging data entity with savings account id : {}", savingsAccountId))
				.doOnNext(stagingDataEntity -> log.info("Staging data entity received : {}", stagingDataEntity))
				.flatMap(stagingDataEntity -> iTransactionCommands
								.buildTransactionForInterestDeposit(loginId, stagingDataEntity, savingsAccountId, calculatedInterest, interestPostingDate, officeId))
				.doOnSuccess(transactionList -> log.info("FDR Interest Posting Transaction List built : {}", transactionList))
				.flatMap(port::saveSingleTransactionToDB);
	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForFDRActivation(String savingsAccountId, BigDecimal fdrAmount, LocalDate activationDate, String loginId) {
		return commonRepository
				.getMemberEntityBySavingsAccountId(savingsAccountId)
				.flatMap(memberEntity ->
						commonRepository.getMemberSamityOfficeInfoBySavingsAccountId(savingsAccountId)
							.flatMap(memberSamityOfficeEntity -> iTransactionCommands
							.buildTransactionForFDRActivation(savingsAccountId, fdrAmount, activationDate, memberEntity.getMemberId(), memberEntity.getMfiId(), loginId, memberSamityOfficeEntity.getSamityId())))
				.flatMap(port::saveSingleTransactionToDB);
	}

	@Override
	public Mono<List<Transaction>> createTransactionEntryForLoanAdjustmentForSamity(List<Transaction> transactionList) {
		return port.saveTransactionListToDB(transactionList);
	}

	@Override
	public Mono<List<Transaction>> getLoanAdjustedTransactionsForLoanAccountsOfMember(String memberId) {
		return port.getLoanAdjustedTransactionsForLoanAccountsOfMember(memberId)
				.collectList();
	}

	@Override
	public Mono<List<Transaction>> getSavingsAccountsForLoanAdjustedTransactions(List<String> loanAdjustmentProcessIdList) {
		return port.getSavingsAccountsForLoanAdjustedTransactions(loanAdjustmentProcessIdList)
				.collectList();
	}

	@Override
	public Mono<Map<String, BigDecimal>> getTotalLoanDisbursementAmountForSamityResponse(List<String> samityIdList) {
		return port.getAllLoanDisbursementTransactionDataForSamityIdList(samityIdList)
				.map(transactionList -> {
					Map<String, BigDecimal> samityWithTotalLoanDisbursement = new HashMap<>();
					samityIdList.forEach(samityId -> {
						BigDecimal totalAmount = transactionList.stream()
								.filter(transaction -> transaction.getSamityId().equals(samityId) && !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) && transaction.getTransactionCode().equals("LOAN_DISBURSEMENT"))
								.map(Transaction::getAmount)
								.reduce(BigDecimal.ZERO, BigDecimal::add);
						samityWithTotalLoanDisbursement.put(samityId, totalAmount);
					});
					return samityWithTotalLoanDisbursement;
				});
	}

	@Override
	public Mono<List<Transaction>> getAllTransactionsOnABusinessDayForOffice(String managementProcessId, LocalDate businessDate) {
		return port.getAllTransactionsOnABusinessDayForOffice(managementProcessId, businessDate)
//				.filter(transaction -> !transaction.getTransactionCode().equals(TransactionCodes.LOAN_REPAY_PRIN.getValue()) || !transaction.getTransactionCode().equals(TransactionCodes.LOAN_REPAY_SC.getValue()))
				.collectList();
	}

	@Override
	public Mono<TransactionResponseDTO> createTransactionForOneSamityV1(String samityId, String managementProcessId, String transactionProcessId) {

		return stagingDataUseCase
				.getStagingDataBySamityId(samityId)
				.flatMap(stagingDataResponseDTO -> collectionUseCase
						.getCollectionStagingDataForSamityMembers(stagingDataResponseDTO.getStagingDataId())
						.filter(collectionStagingDataResponseDTO -> collectionStagingDataResponseDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
						.map(collectionStagingDataResponseDTO -> Tuples.of(collectionStagingDataResponseDTO, stagingDataResponseDTO)))
				.flatMap(tuples -> {
					CollectionDataDTO collectionDataDTO = mapper.map(tuples.getT1(), CollectionDataDTO.class);
					StagingDataDTO stagingDataDTO = mapper.map(tuples.getT2(), StagingDataDTO.class);
					stagingDataDTO.setManagementProcessId(managementProcessId);
					stagingDataDTO.setProcessId(transactionProcessId);
					return iTransactionCommands.buildTransaction(collectionDataDTO, stagingDataDTO);
				})
				.collectList()
				.flatMap(transactionList -> port.saveTransactionsToDb(transactionList)
						.flatMap(aBoolean -> {
							if (aBoolean) {
								return Mono.just(TransactionResponseDTO
										.builder()
										.transactionList(transactionList)
										.transactionCount(transactionList.size())
										.build());
							} else {
								return Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.FAILED_TO_SAVE_TRANSACTION.getValue()));
							}
						}));
	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForFDRClosure(Transaction transaction) {
		return port
				.checkIfTransactionAlreadyExistsBySavingsAccountIdAndTransactionCode(transaction.getSavingsAccountId(), transaction.getTransactionCode())
				.flatMap(aBoolean -> aBoolean
					? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Closure Transaction already exists!"))
					: port.saveSingleTransactionToDB(transaction));
	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForDPSClosure(Transaction transaction) {
		return port
				.checkIfTransactionAlreadyExistsBySavingsAccountIdAndTransactionCode(transaction.getSavingsAccountId(), transaction.getTransactionCode())
				.flatMap(aBoolean -> aBoolean
						? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Transaction already exists!"))
						: port.saveSingleTransactionToDB(transaction));
	}

	@Override
	public Mono<SingleTransactionResponseDTO> createTransactionForSavingsClosure(Transaction transaction) {
		return port
				.checkIfTransactionAlreadyExistsByManagementProcessIdAndSavingsAccountIdAndTransactionCode(transaction.getManagementProcessId(), transaction.getSavingsAccountId(), transaction.getTransactionCode())
				.flatMap(aBoolean -> aBoolean
						? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Transaction already exists!"))
						: port.saveSingleTransactionToDB(transaction));
	}

	@Override
	public Mono<String> deleteTransactionsForSamityUnauthorization(String managementProcessId, String transactionProcessId) {
		return port.deleteTransactionsForSamityUnauthorization(managementProcessId, transactionProcessId);
	}

	@Override
	public Flux<Transaction> getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode) {
		return port.getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, officeId, transactionCode);
	}

	@Override
	public Mono<Transaction> createTransactionForWelfareFund(Transaction transaction) {
		return port.createTransactionForWelfareFund(transaction);
	}

	@Override
	public Mono<List<Transaction>> getWelfareFundTransactionForOfficeByManagementProcessId(String managementProcessId) {
		return port.getWelfareFundTransactionForOfficeByManagementProcessId(managementProcessId)
				.collectList();
	}

	@Override
	public Mono<Integer> deletePostedInterestBySavingsAccountIdList(String managementProcessId, List<String> savingsAccountIdList) {
		if (savingsAccountIdList.isEmpty()) {
			return Mono.just(0);
		}
		return port.deletePostedInterestBySavingsAccountIdList(managementProcessId, savingsAccountIdList);
	}

	@Override
	public Mono<List<Transaction>> getTransactionsByTransactionCodeAndManagementProcessId(String transactionCode, String managementProcessId) {
		return port
				.getTransactionEntriesByTransactionCodeAndManagementProcessId(transactionCode, managementProcessId)
				.doOnSuccess(transactions -> log.info("Transactions received for transaction code : {} and management process id : {}", transactionCode, managementProcessId))
				.doOnError(throwable -> log.error("Failed to get transactions for transaction code : {} and management process id : {}. Error : {}", transactionCode, managementProcessId, throwable.getMessage()));
	}

	@Override
	public Mono<Transaction> getTransactionByTransactionId(String transactionId) {
		return port.getTransactionByTransactionId(transactionId);
	}

	@Override
	public Mono<Transaction> saveTransaction(Transaction transaction) {
		return port.saveSingleTransactionToDB(transaction)
				.map(singleTransactionResponseDTO -> mapper.map(singleTransactionResponseDTO, Transaction.class));
	}

	/*private Mono<Tuple2<List<String>, Transaction>> getListOfAccruedInterestIdAndTransaction(Tuple2<List<SavingsAccruedInterestResponseDTO>, StagingDataEntity> tuples, String loginId, String savingsAccountId) {
		List<String> accruedInterestIdList = new ArrayList<>();
		BigDecimal totalAccruedInterest = BigDecimal.ZERO;
		List<SavingsAccruedInterestResponseDTO> responseDTOList = tuples.getT1();
		StagingDataEntity stagingDataEntity = tuples.getT2();

        for (SavingsAccruedInterestResponseDTO savingsAccruedInterestResponseDTO : responseDTOList) {
            accruedInterestIdList.add(savingsAccruedInterestResponseDTO.getAccruedInterestId());
            totalAccruedInterest = totalAccruedInterest.add(savingsAccruedInterestResponseDTO.getAccruedInterestAmount());
        }

		log.info("accruedInterestId list : {}", accruedInterestIdList);
		log.info("total accrued interest : {}", totalAccruedInterest);

		return iTransactionCommands
				.buildTransactionForInterestDeposit(loginId, stagingDataEntity, savingsAccountId, totalAccruedInterest, LocalDate.now(), officeId)
				.doOnSuccess(transaction -> log.info("transaction built for accrued interest posting : {}", transaction))
				.map(transaction -> Tuples.of(accruedInterestIdList, transaction));

	}*/
}
