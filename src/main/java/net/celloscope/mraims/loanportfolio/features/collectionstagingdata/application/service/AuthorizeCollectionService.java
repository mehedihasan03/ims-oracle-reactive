package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.AuthorizeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.AuthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.RejectionCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.UnauthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
public class AuthorizeCollectionService implements AuthorizeCollectionUseCase {
	private final CollectionStagingDataPersistencePort persistencePort;
	private final ModelMapper modelMapper;
	
	private final TransactionUseCase transactionUseCase;
	private final PassbookUseCase passbookUseCase;
	private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
	private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
	private final SamityEventTrackerUseCase samityEventTrackerUseCase;
	private final TransactionalOperator rxtx;
	
	public AuthorizeCollectionService(
			TransactionalOperator rxtx,
			CollectionStagingDataPersistencePort persistencePort,
			ModelMapper modelMapper,
			TransactionUseCase transactionUseCase,
			PassbookUseCase passbookUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase,
			ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
			SamityEventTrackerUseCase samityEventTrackerUseCase) {
		this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
		this.samityEventTrackerUseCase = samityEventTrackerUseCase;
		this.rxtx = rxtx;
		this.persistencePort = persistencePort;
		this.modelMapper = modelMapper;
		this.transactionUseCase = transactionUseCase;
		this.passbookUseCase = passbookUseCase;
		this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
	}
	
	@Override
	public Mono<CollectionMessageResponseDTO> authorize(AuthorizeCollectionCommand command) {
		AtomicReference<String> managementProcessTrackerId = new AtomicReference<>();
		final String transactionProcessId = UUID.randomUUID().toString();
		final String passbookProcessId = UUID.randomUUID().toString();
		final String authorizationProcessId = UUID.randomUUID().toString();
//		@TODO: update managementProcessId in transaction and passbook tables
		log.info("Transaction Process Id: {} + Passbook Process Id: {}", transactionProcessId, passbookProcessId);
		return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
				.flatMap(managementProcessId -> this.validateAndUpdateCollectionDataForAuthorization(command, managementProcessId))
				.doOnRequest(r -> log.debug("Request Received for payment collection in service"))
				.doOnError(throwable -> log.error("Failed to save collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
				.flatMapMany(managementProcessId -> transactionUseCase.createTransactionForOneSamityV1(command.getSamityId(), managementProcessId, transactionProcessId)
						.flatMapMany(transactionResponseDTO -> Flux.fromIterable(transactionResponseDTO.getTransactionList())
								.flatMap(transaction -> {
											if (transaction.getAccountType().equals(Constants.ACCOUNT_TYPE_LOAN.getValue())) {
												return passbookUseCase.getRepaymentScheduleAndCreatePassbookEntryForLoanV1(
														PassbookRequestDTO
																.builder()
																.managementProcessId(transaction.getManagementProcessId())
																.processId(passbookProcessId)
																.amount(transaction.getAmount())
																.loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
																.savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
																.transactionId(transaction.getTransactionId())
																.transactionCode(transaction.getTransactionCode())
																.loginId(command.getLoginId())
																.mfiId(transaction.getMfiId())
																.transactionDate(transaction.getTransactionDate())
																.paymentMode(transaction.getPaymentMode())
																.build());
												
											} else {
												return passbookUseCase.createPassbookEntryForSavings(PassbookRequestDTO
														.builder()
														.managementProcessId(transaction.getManagementProcessId())
														.processId(passbookProcessId)
														.amount(transaction.getAmount())
														.loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
														.savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
														.transactionId(transaction.getTransactionId())
														.transactionCode(transaction.getTransactionCode())
														.loginId(command.getLoginId())
														.mfiId(transaction.getMfiId())
														.transactionDate(transaction.getTransactionDate())
														.paymentMode(transaction.getPaymentMode())
														.samityId(transaction.getSamityId())
														.build());
											}
											
										}
								)
						
						)
				)
				/*.concatMapIterable(passbookResponseDTOList -> passbookResponseDTOList)
				.filter(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equals(Constants.TRANSACTION_CODE_LOAN_REPAY.getValue()))
				.collectList()*/
				.map(passbookResponseDTOList -> {
					managementProcessTrackerId.set(passbookResponseDTOList.get(0).getManagementProcessId());
					return passbookResponseDTOList;
				})
				.filter(passbookResponseDTO -> passbookResponseDTO.get(0).getTransactionCode().equals(Constants.TRANSACTION_CODE_LOAN_REPAY.getValue()))
				.map(this::getFullyPaidInstallmentNos)
				.doOnNext(tuple2 -> log.debug("1  {} 2 {}", tuple2.getT1(), tuple2.getT2()))
				.flatMap(tuple2 -> {
					log.debug("I was here.....");
					return loanRepaymentScheduleUseCase.updateInstallmentStatus(tuple2.getT2(), Status.STATUS_PAID.getValue(), tuple2.getT1(), managementProcessTrackerId.get());
				})
				.doOnNext(repaymentScheduleResponseDTO -> log.debug("repaymentScheduleResponseDTO {}", repaymentScheduleResponseDTO))
				.collectList()
				.doOnNext(list -> log.info("ManagementProcessId: {}, TransactionProcessId: {}, PassbookProcessId: {}", managementProcessTrackerId.get(), transactionProcessId, passbookProcessId))
				.flatMap(list -> this.updateSamityEventsOnAuthorization(command, managementProcessTrackerId.get(), authorizationProcessId, transactionProcessId, passbookProcessId)
						.map(string -> list))
				.as(this.rxtx::transactional)
				.map(response -> buildResponse(command.getSamityId()));
	}
	
	private Mono<String> updateSamityEventsOnAuthorization(AuthorizeCollectionCommand command, String managementProcessId, String authorizationProcessId, String transactionProcessId, String passbookProcessId) {
		return samityEventTrackerUseCase.insertSamityEvent(managementProcessId, authorizationProcessId, command.getOfficeId(), command.getSamityId(), SamityEvents.COLLECTION_AUTHORIZED.getValue(), command.getLoginId())
				.flatMap(samityEventTracker -> samityEventTrackerUseCase.insertSamityEvent(managementProcessId, transactionProcessId, command.getOfficeId(), command.getSamityId(), SamityEvents.COLLECTION_TRANSACTION_COMPLETED.getValue(), command.getLoginId()))
				.flatMap(samityEventTracker -> samityEventTrackerUseCase.insertSamityEvent(managementProcessId, passbookProcessId, command.getOfficeId(), command.getSamityId(), SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue(), command.getLoginId()))
				.map(samityEventTracker -> "Samity Event Tracker Updated For Authorization")
				.doOnNext(s -> log.info("{}", s));
	}
	
	@Override
	public Mono<CollectionMessageResponseDTO> reject(RejectionCollectionCommand command) {
		return persistencePort.rejectCollectionBySamity(command)
				.map(count -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection Data is Rejected for samity: " + command.getSamityId())
						.build())
				.doOnError(throwable -> log.error("Failed to reject Collection Data: {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
						e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR,
								"Something went wrong. Please try again later")))
				
				;
	}
	
	@Override
	public Mono<CollectionMessageResponseDTO> unauthorize(UnauthorizeCollectionCommand command) {
		return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
				.map(s -> {
					command.setManagementProcessId(s);
					return command;
				})
				.flatMap(command1 -> samityEventTrackerUseCase.getAllSamityEventsForSamity(command.getManagementProcessId(), command.getSamityId())
						.collectList()
						.filter(list -> list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.COLLECTION_AUTHORIZED.getValue())))
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is not authorized yet"))))
				.map(samityEventTrackerList -> command)
				.flatMap(command1 -> persistencePort.getStagingDataIdListBySamity(command1.getSamityId()))
				.map(stagingDataIdList -> {
					command.setStagingDataIdList(stagingDataIdList);
					return command;
				})
				.flatMap(this::getTransactionAndLoanRepayScheduleIdListForUnauthorization)
				.doOnNext(collectionCommand -> log.info("Unauthorized Collection Command: {}", collectionCommand))
				.flatMap(this::updateAndDeleteForUnauthorization)
				.flatMap(this::updateSamityEventTrackerForCollectionUnauthorization)
				.as(this.rxtx::transactional)
				.map(response -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection is successfully unauthorized")
						.build());
	}

	private Mono<UnauthorizeCollectionCommand> updateSamityEventTrackerForCollectionUnauthorization(UnauthorizeCollectionCommand command) {
		return samityEventTrackerUseCase.getAllSamityEventsForSamity(command.getManagementProcessId(), command.getSamityId())
				.collectList()
				.map(list -> {
					List<String> newList = new ArrayList<>();
					list.forEach(item -> {
						if(!HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && (item.getSamityEvent().equals(SamityEvents.COLLECTION_AUTHORIZED.getValue()) || item.getSamityEvent().equals(SamityEvents.COLLECTION_TRANSACTION_COMPLETED.getValue()) || item.getSamityEvent().equals(SamityEvents.COLLECTION_PASSBOOK_COMPLETED.getValue()))){
							newList.add(item.getSamityEventTrackerId());
						}
					});
					return newList;
				})
				.flatMap(samityEventTrackerUseCase::deleteSamityEventTrackerByEventTrackerIdList)
				.map(samityEventTrackerIdList -> command);
	}

	private Mono<UnauthorizeCollectionCommand> getTransactionAndLoanRepayScheduleIdListForUnauthorization(UnauthorizeCollectionCommand command) {
		return transactionUseCase.getTransactionIdListForSamityByStagingIDList(command.getStagingDataIdList())
						.map(transactionIdList -> {
							command.setTransactionIdList(transactionIdList);
							return command;
						})
				.doOnNext(unauthorizeCollectionCommand -> log.info("unauthorizeCollectionCommand : {}", unauthorizeCollectionCommand))
				.flatMap(command1 -> passbookUseCase.getRepayScheduleIdListByTransactionList(command1.getTransactionIdList())
						.map(loanRepayScheduleIdList -> {
							command1.setLoanRepayScheduleIdList(loanRepayScheduleIdList);
							return command1;
						}));

	}
	
	private Mono<UnauthorizeCollectionCommand> updateAndDeleteForUnauthorization(UnauthorizeCollectionCommand unauthorizeCollectionCommand) {
		return Mono.just(unauthorizeCollectionCommand)
				.flatMap(command -> {
					if (!command.getLoanRepayScheduleIdList().isEmpty()) {
						return loanRepaymentScheduleUseCase.updateInstallmentStatusToPending(command.getLoanRepayScheduleIdList())
								.map(stringList -> command);
					}
					return Mono.just(command);
				})
				.doOnNext(command -> log.info("Loan Repay Schedule Status Updated Successfully"))
				.flatMap(command -> passbookUseCase.deletePassbookEntryByTransaction(command.getTransactionIdList())
						.map(stringList -> command))
				.doOnNext(command -> log.info("Passbook Entry Deleted Successfully"))
				.flatMap(command -> transactionUseCase.deleteTransactionForSamityByStagingDataIDList(command.getStagingDataIdList())
						.map(stringList -> command))
				.doOnNext(command -> log.info("Transaction Entry Deleted Successfully"))
				.flatMap(persistencePort::unauthorizeBySamity)
				.doOnNext(command -> log.info("Collection Status Updated Successfully"))
				;
	}
	
	private Tuple2<String, List<Integer>> getFullyPaidInstallmentNos(List<PassbookResponseDTO> passbookResponseDTOList) {
		AtomicReference<String> loanAccountId = new AtomicReference<>();
		log.debug("passbookResponseDTOList : {}", passbookResponseDTOList);
		List<Integer> fulfilledInstallments = passbookResponseDTOList
				.stream()
				.peek(passbookResponseDTO -> log.debug("before filter passbook response dto : {}", passbookResponseDTO))
				.filter(this::isThisInstallmentFullyPaid)
				.peek(passbookResponseDTO -> log.debug("after filter passbook response dto : {}", passbookResponseDTO))
				.peek(passbookResponseDTO -> loanAccountId.set(passbookResponseDTO.getLoanAccountId()))
				.map(PassbookResponseDTO::getInstallNo)
				.peek(integer -> log.debug("fulfilled installments : {}", integer))
				.toList();
		log.debug("fulfilledInstallments : {}", fulfilledInstallments);
		Tuple2<String, List<Integer>> tuples;
		if (fulfilledInstallments.isEmpty()) {
			log.debug("I was here {}", loanAccountId);
			tuples = Tuples.of("", new ArrayList<>());
		} else tuples = Tuples.of(loanAccountId.get(), fulfilledInstallments);
		log.debug("Tuple2<String, List<Integer>> {}", tuples);
		return tuples;
	}
	
	private CollectionMessageResponseDTO buildResponse(String samityId) {
		return CollectionMessageResponseDTO.builder()
				.userMessage("Collection is successfully authorized")
//                .samityId(samityId)
				.build();
	}
	
	private boolean isThisInstallmentFullyPaid(PassbookResponseDTO passbookResponseDTO) {
		if (passbookResponseDTO.getScRemainForThisInst() != null && passbookResponseDTO.getPrinRemainForThisInst() != null) {
			return passbookResponseDTO.getScRemainForThisInst().toString().equals("0.00") && passbookResponseDTO.getPrinRemainForThisInst().toString().equals("0.00");
		} else return false;
	}
	
	private Mono<String> validateAndUpdateCollectionDataForAuthorization(AuthorizeCollectionCommand command, String managementProcessId){
		return persistencePort.getAllCollectionDataBy(command.getSamityId(), command.getCollectionType())
				.collectList()
//				.filter(collectionStagingDataEntityList -> collectionStagingDataEntityList.stream().allMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getLockedBy()) || item.getLockedBy().equalsIgnoreCase("")))
//				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Collection Data is Locked and cannot commit for Authorization for samity: " + command.getSamityId())))
				.filter(collectionStagingDataEntityList -> collectionStagingDataEntityList.stream().allMatch(item -> (!HelperUtil.checkIfNullOrEmpty(item.getEditCommit()) && item.getEditCommit().equalsIgnoreCase("Yes")) || (!HelperUtil.checkIfNullOrEmpty(item.getIsSubmitted()) && item.getIsSubmitted().equals("Yes"))))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Collection Data is not committed for Authorization for samity: " + command.getSamityId())))
				.flatMap(list -> persistencePort.updateAllCollectionDataBySamityForAuthorization(command.getSamityId(), command.getCollectionType(), command.getLoginId()))
				.map(integer -> managementProcessId);
	}
}
