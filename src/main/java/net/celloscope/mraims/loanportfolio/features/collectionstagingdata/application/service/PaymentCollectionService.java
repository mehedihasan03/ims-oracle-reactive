package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.MemberAttendanceUseCase;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceInfo;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.dto.MemberAttendanceRequestDTO;
import net.celloscope.mraims.loanportfolio.features.attendance.domain.MemberAttendance;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionDataVerifyDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionByFieldOfficerCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.LoanProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.celloscope.mraims.loanportfolio.core.util.Constants.STATUS_STAGED;
import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.REGULAR;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_DPS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_FDR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
public class PaymentCollectionService implements PaymentCollectionUseCase {
	private final CollectionStagingDataPersistencePort persistencePort;
	private final ModelMapper modelMapper;

	private final TransactionalOperator rxtx;

	private final IStagingDataUseCase stagingDataUseCase;

	private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
	private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
	private final SamityEventTrackerUseCase samityEventTrackerUseCase;
	private final MemberAttendanceUseCase memberAttendanceUseCase;
	private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
	private final CommonValidation commonValidation;
	private final Gson gson;
	private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
	private final LoanAdjustmentUseCase loanAdjustmentUseCase;
	private final LoanRebatePersistencePort loanRebatePersistencePort;
	private final LoanWaiverPersistencePort loanWaiverPersistencePort;
	private final CommonRepository commonRepository;

	public PaymentCollectionService(
            TransactionalOperator rxtx,
            CollectionStagingDataPersistencePort persistencePort,
            ModelMapper modelMapper,
            IStagingDataUseCase stagingDataUseCase, ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
            OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, MemberAttendanceUseCase memberAttendanceUseCase, DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort, CommonValidation commonValidation, CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase, LoanAdjustmentUseCase loanAdjustmentUseCase, LoanRebatePersistencePort loanRebatePersistencePort, LoanWaiverPersistencePort loanWaiverPersistencePort, CommonRepository commonRepository) {
		this.stagingDataUseCase = stagingDataUseCase;
		this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
		this.officeEventTrackerUseCase = officeEventTrackerUseCase;
		this.samityEventTrackerUseCase = samityEventTrackerUseCase;
		this.rxtx = rxtx;
		this.persistencePort = persistencePort;
		this.modelMapper = modelMapper;
		this.memberAttendanceUseCase = memberAttendanceUseCase;
		this.dayEndProcessTrackerPersistencePort = dayEndProcessTrackerPersistencePort;
		this.commonValidation = commonValidation;
        this.collectionStagingDataQueryUseCase = collectionStagingDataQueryUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.loanRebatePersistencePort = loanRebatePersistencePort;
        this.loanWaiverPersistencePort = loanWaiverPersistencePort;
        this.commonRepository = commonRepository;
        this.gson = CommonFunctions.buildGson(this);
	}

	@Override
	public Mono<CollectionMessageResponseDTO> collectPaymentBySamityV1(PaymentCollectionBySamityCommand command) {

		return this.validateCollectionDataBySamity(command)
				.then(this.saveCollectionData(command, "No"))
				.doOnError(throwable -> log.error("Failed to save collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
						e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR,
								"Something went wrong. Please try again later")))
				.as(this.rxtx::transactional)
				.map(response -> this.buildResponseForCollectionPayment(
						"Collection is successfully created for samity: " + command.getSamityId()));
	}

	@Override
	public Mono<CollectionMessageResponseDTO> collectPaymentByFieldOfficer(
			PaymentCollectionByFieldOfficerCommand command) {
		return checkCollectionDataValidationForFieldOfficer(command)
				.then(this.convertCollectionPaymentRequestDataToDomainByFieldOfficer(command)
						.flatMap(command1 -> this.saveCollectionData(command1, "Yes"))
						.collectList())
				.doOnError(throwable -> log.error("Failed to save collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
						e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR,
								"Something went wrong. Please try again later")))
				.as(this.rxtx::transactional)
				.map(response -> this.buildResponseForCollectionPayment(
						"Collection is successfully created for fieldOfficer: " + command.getFieldOfficerId()));
	}

	@Override
	public Mono<CollectionMessageResponseDTO> editAndUpdatePaymentBySamity(PaymentCollectionBySamityCommand command) {
		final String processId = UUID.randomUUID().toString();
		return this.validateCollectionDataBySamity(command)
				.then(managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
						.flatMap(managementProcessTracker ->
								this.validateIfCollectionAmountExceedsTotalOutstandingAmount(managementProcessTracker.getManagementProcessId(), command)
										.map(booleanList -> managementProcessTracker))
						.flatMap(managementProcessTracker -> persistencePort.editUpdateAllCollectionData(convertCollectionPaymentRequestDataToDomainBySamity(command, managementProcessTracker.getManagementProcessId(), processId), command.getLoginId())))
				.doOnError(throwable -> log.error("Failed to update collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later")))
				.as(this.rxtx::transactional)
				.map(response -> this.buildResponseForCollectionPayment("Collection is successfully updated for samity: " + command.getSamityId()));
	}

	// Process Management V2
	@Override
	public Mono<CollectionMessageResponseDTO> collectPaymentBySamity(PaymentCollectionBySamityCommand command) {
		log.debug("Collection Data: {}", gson.toJson(command));
		AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
		final String processId = StringUtils.isNotBlank(command.getProcessId()) ? command.getProcessId() : UUID.randomUUID().toString();

		return this.validateIfCollectionIsPossibleForSamity(command.getOfficeId(), command.getSamityId(), command.getCollectionType())
				.doOnNext(managementProcessTrackerAtomicReference::set)
				.flatMap(this::validateIfDayEndProcessIsStartedForOffice)
				.flatMap(managementProcessTracker -> this.validateIfCollectionTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), command.getSamityId())
						.map(string -> managementProcessTracker))
				.flatMap(managementProcessTracker -> this.validateIfCollectionDataIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateIfLoanAccountIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateIfSavingsAccountIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateDpsAmountForCollection(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateFdrAmountForCollection(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> handleEventTrackerValidation(command, managementProcessTracker, processId))
				.map(tuple -> {
					log.info("Process Id to be build: {}", tuple.getT2());
					return this.buildCollectionDataToSave(tuple.getT1(), command, tuple.getT2());
				})
				.flatMapMany(Flux::fromIterable)
				.flatMap(collectionStagingData -> {
					if (collectionStagingData.getAccountType().equalsIgnoreCase(Constants.ACCOUNT_TYPE_LOAN.getValue()))
						return Mono.zip(Mono.just(collectionStagingData),
								stagingDataUseCase.getStagingAccountDataByLoanAccountId(collectionStagingData.getLoanAccountId()),
								loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(collectionStagingData.getLoanAccountId()))
								.flatMap(tuple3 -> this.validateLoanAmountForCollectionData(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()));
                    else return Mono.just(collectionStagingData);
                })
				.collectList()
				.flatMap(persistencePort::saveAllCollectionDataToDatabase)
				.flatMap(collectionStagingDataList -> {
					if(command.getCollectionType().equals(REGULAR.getValue())){
						return this.saveMemberAttendanceForSamity(command)
								.map(memberAttendanceList -> collectionStagingDataList);
					}
					return Mono.just(collectionStagingDataList);
				})
				.flatMap(list -> samityEventTrackerUseCase
						.getSamityEventByEventTypeForSamity(list.get(0).getManagementProcessId(), command.getSamityId(),
								SamityEvents.COLLECTED.getValue())
						.flatMap(samityEventTracker -> {
							if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
								return samityEventTrackerUseCase.insertSamityEvent(list.get(0).getManagementProcessId(),
										list.get(0).getProcessId(), command.getOfficeId(), command.getSamityId(),
										SamityEvents.COLLECTED.getValue(), command.getLoginId());
							}
							return Mono.just(samityEventTracker);
						}))
				.as(this.rxtx::transactional)
				.map(samityEventTracker -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection is completed for samity")
						.build());
	}

	@Override
	public Mono<CollectionMessageResponseDTO> collectRebatePayment(PaymentCollectionBySamityCommand command) {
		log.debug("Collection Data: {}", gson.toJson(command));
		AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
		final String processId = StringUtils.isNotBlank(command.getProcessId()) ? command.getProcessId() : UUID.randomUUID().toString();
		return this.validateIfCollectionIsPossibleForSamity(command.getOfficeId(), command.getSamityId(), command.getCollectionType())
				.doOnNext(managementProcessTrackerAtomicReference::set)
				.flatMap(this::validateIfDayEndProcessIsStartedForOffice)
				.flatMap(managementProcessTracker -> this.validateIfCollectionTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), command.getSamityId())
						.map(string -> managementProcessTracker))
				.flatMap(managementProcessTracker -> this.validateIfCollectionDataIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> handleEventTrackerValidation(command, managementProcessTracker, processId))
				.map(tuple -> {
					log.info("Process Id to be build: {}", tuple.getT2());
					return this.buildCollectionDataToSave(tuple.getT1(), command, tuple.getT2());
				})
				.flatMap(collectionStagingData -> persistencePort.getCollectionStagingDataByLoanAccountId(command.getData().get(0).getLoanAccountId())
						.map(collectionStagingData1 -> {
							collectionStagingData.get(0).setOid(collectionStagingData1.getOid());
							return collectionStagingData;
						})
						.switchIfEmpty(Mono.just(collectionStagingData)))
				.flatMap(persistencePort::saveAllCollectionDataToDatabase)
				.flatMap(collectionStagingDataList -> {
					if(command.getCollectionType().equals(REGULAR.getValue())){
						return this.saveMemberAttendanceForSamity(command)
								.map(memberAttendanceList -> collectionStagingDataList);
					}
					return Mono.just(collectionStagingDataList);
				})
				.flatMap(list -> samityEventTrackerUseCase
						.getSamityEventByEventTypeForSamity(list.get(0).getManagementProcessId(), command.getSamityId(),
								SamityEvents.COLLECTED.getValue())
						.flatMap(samityEventTracker -> {
							if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
								return samityEventTrackerUseCase.insertSamityEvent(list.get(0).getManagementProcessId(),
										list.get(0).getProcessId(), command.getOfficeId(), command.getSamityId(),
										SamityEvents.COLLECTED.getValue(), command.getLoginId());
							}
							return Mono.just(samityEventTracker);
						}))
				.as(this.rxtx::transactional)
				.map(samityEventTracker -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection is completed for samity")
						.build());
	}

	private Mono<CollectionStagingData> validateLoanAmountForCollectionData(CollectionStagingData collectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData stagingAccountData, LoanAdjustmentData loanAdjustmentData) {
		BigDecimal principalRemaining = stagingAccountData.getTotalPrincipalRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalPrincipalRemaining();
		BigDecimal serviceChargeRemaining = stagingAccountData.getTotalServiceChargeRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalServiceChargeRemaining();
		BigDecimal loanAdjustmentAmount = loanAdjustmentData.getAmount() == null ? BigDecimal.ZERO : loanAdjustmentData.getAmount();
		BigDecimal loanAccountOutstanding = principalRemaining.add(serviceChargeRemaining).subtract(loanAdjustmentAmount);
		BigDecimal requestedAmount = collectionStagingData.getAmount() == null ? BigDecimal.ZERO : collectionStagingData.getAmount();
		log.info("Principal Remaining: {}, Service Charge Remaining: {}, Requested Amount: {}, loan Adjustment Amount: {}", principalRemaining, serviceChargeRemaining, requestedAmount, loanAdjustmentAmount);
		if (loanAccountOutstanding.compareTo(requestedAmount) >= 0 && requestedAmount.compareTo(BigDecimal.ZERO) >= 0) {
			return Mono.just(collectionStagingData);
		}
		else return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection amount must be positive and can not exceed loan outstanding amount..!"));
	}

	@Override
	public Mono<CollectionMessageResponseDTO> collectSeasonalLoanPaymentBySamity(PaymentCollectionBySamityCommand command) {
		log.info("Collection Data: {}", gson.toJson(command));
		AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
		final String processId = StringUtils.isNotBlank(command.getProcessId()) ? command.getProcessId() : UUID.randomUUID().toString();
		return this.validateIfCollectionIsPossibleForSamity(command.getOfficeId(), command.getSamityId(), command.getCollectionType())
				.doOnNext(managementProcessTrackerAtomicReference::set)
				.flatMap(this::validateIfDayEndProcessIsStartedForOffice)
				.flatMap(managementProcessTracker -> this.validateIfCollectionTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), command.getSamityId())
						.map(string -> managementProcessTracker))
				.flatMap(managementProcessTracker -> this.validateIfCollectionDataIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateIfLoanAccountIsValidForPayment(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateDpsAmountForCollection(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> this.validateFdrAmountForCollection(managementProcessTracker, command))
				.flatMap(managementProcessTracker -> handleEventTrackerValidation(command, managementProcessTracker, processId))
				.map(tuple -> {
					log.info("Process Id to be build: {}", tuple.getT2());
					return this.buildCollectionDataToSave(tuple.getT1(), command, tuple.getT2());
				})
				.flatMap(persistencePort::saveAllCollectionDataToDatabase)
				.flatMap(collectionStagingDataList -> {
					if(command.getCollectionType().equals(REGULAR.getValue())){
						return this.saveMemberAttendanceForSamity(command)
								.map(memberAttendanceList -> collectionStagingDataList);
					}
					return Mono.just(collectionStagingDataList);
				})
				.flatMap(list -> samityEventTrackerUseCase
						.getSamityEventByEventTypeForSamity(list.get(0).getManagementProcessId(), command.getSamityId(),
								SamityEvents.COLLECTED.getValue())
						.flatMap(samityEventTracker -> {
							if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
								return samityEventTrackerUseCase.insertSamityEvent(list.get(0).getManagementProcessId(),
										list.get(0).getProcessId(), command.getOfficeId(), command.getSamityId(),
										SamityEvents.COLLECTED.getValue(), command.getLoginId());
							}
							return Mono.just(samityEventTracker);
						}))
				.as(this.rxtx::transactional)
				.map(samityEventTracker -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection is completed for samity")
						.build());
	}

	private Mono<Tuple2<ManagementProcessTracker, String>> handleEventTrackerValidation(PaymentCollectionBySamityCommand command, ManagementProcessTracker managementProcessTracker, String processId) {
		return samityEventTrackerUseCase
				.getSamityEventByEventTypeForSamity(managementProcessTracker.getManagementProcessId(),
						command.getSamityId(), SamityEvents.COLLECTED.getValue())
				.flatMap(samityEventTracker -> {
					if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
						log.info("Samity Event Tracker is Empty");
						return samityEventTrackerUseCase
								.insertSamityEvent(managementProcessTracker.getManagementProcessId(), processId,
										command.getOfficeId(), command.getSamityId(),
										SamityEvents.COLLECTED.getValue(), command.getLoginId())
								.map(newSamityEventTracker -> Tuples.of(managementProcessTracker,
										newSamityEventTracker.getSamityEventTrackerId()));
					} else if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) &&
							!HelperUtil.checkIfNullOrEmpty(command.getManagementProcessId()) &&
							!HelperUtil.checkIfNullOrEmpty(command.getProcessId())) {
						log.info("Samity Event Tracker is Not Empty and process id is provided");
						return Mono.just(Tuples.of(managementProcessTracker, processId));
					} else {
						log.info("Samity Event Tracker is Not Empty process id not provided");
						return Mono.just(Tuples.of(managementProcessTracker, samityEventTracker.getSamityEventTrackerId()));
					}
				});
	}

	private Mono<ManagementProcessTracker> validateFdrAmountForCollection(ManagementProcessTracker managementProcessTracker, PaymentCollectionBySamityCommand command) {
		return Flux.fromIterable(command.getData())
				.filter(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getSavingsTypeId()) && collectionData.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue()))
				.filter(fdrAccountData -> fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) != 0)
				.map(fdrAccountData -> {
					log.info("FDR Account Data condition1: {}", !(fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) >= 0));
					log.info("FDR Account Data condition2: {}", fdrAccountData.getAmount().compareTo(fdrAccountData.getTargetAmount()) != 0);
					if (!(fdrAccountData.getAmount().compareTo(BigDecimal.ZERO) > 0) ||fdrAccountData.getAmount().compareTo(fdrAccountData.getTargetAmount()) != 0){
						return Tuples.of("Invalid", fdrAccountData.getSavingsAccountId());
					}
					return Tuples.of("Valid", fdrAccountData.getSavingsAccountId());
				})
				.collectMap(Tuple2::getT1, Tuple2::getT2)
				.flatMap(msgMap -> {
					if(msgMap.containsKey("Invalid")){
						return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate FDR amount for account: " + msgMap.get("Invalid")));
					}
					else
						return Mono.just(managementProcessTracker);
				})
				.doOnError(throwable -> log.error("validation error -> if FDR amount is valid for collection: {}", throwable.getMessage()));
	}


	private Mono<ManagementProcessTracker> validateDpsAmountForCollection(ManagementProcessTracker managementProcessTracker, PaymentCollectionBySamityCommand command) {
		return Flux.fromIterable(command.getData())
				.filter(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getSavingsTypeId()) && collectionData.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue()))
				.filter(dpsAccountData -> dpsAccountData.getAmount().compareTo(BigDecimal.ZERO) != 0)
				.map(dpsAccountData -> {
					if (!(dpsAccountData.getAmount().compareTo(BigDecimal.ZERO) > 0) ||dpsAccountData.getAmount().remainder(dpsAccountData.getTargetAmount()).compareTo(BigDecimal.ZERO) != 0 || dpsAccountData.getAmount().compareTo(dpsAccountData.getTargetAmount().multiply(BigDecimal.valueOf(dpsAccountData.getDpsPendingInstallmentNo()))) > 0){
						return Tuples.of("Invalid", dpsAccountData.getSavingsAccountId());
					}
					return Tuples.of("Valid", dpsAccountData.getSavingsAccountId());
				})
				.collectMap(Tuple2::getT1, Tuple2::getT2)
				.flatMap(msgMap -> {
					if(msgMap.containsKey("Invalid")){
						return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate DPS amount for account: " + msgMap.get("Invalid")));
					}
					else
						return Mono.just(managementProcessTracker);
				})
				.doOnError(throwable -> log.error("validation error -> if DPS amount is valid for collection: {}", throwable.getMessage()));
	}



	private Mono<ManagementProcessTracker> validateIfDayEndProcessIsStartedForOffice(ManagementProcessTracker managementProcessTracker) {
		return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
				.filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
				.collectList()
				.filter(List::isEmpty)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office")))
				.map(dayEndProcessTrackers -> managementProcessTracker)
				.doOnError(throwable -> log.error("validation error -> if day end process is started for office: {}", throwable.getMessage()));
	}

	private Mono<String> validateIfCollectionTransactionIsStillAvailableForSamity(String managementProcessId, String samityId) {
		return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
				.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
				.map(SamityEventTracker::getSamityEvent)
//				.filter(samityEvent -> !samityEvent.equals(SamityEvents.CANCELED.getValue()))
				.collectList()
				.filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
				.filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity is Already Canceled and Cannot Create Transaction")))
				.flatMap(samityEventList -> persistencePort.getAllCollectionDataBySamity(samityId)
						.filter(collectionStagingData -> !HelperUtil.checkIfNullOrEmpty(collectionStagingData.getStatus()))
						.collectList())
				.filter(collectionStagingDataList -> collectionStagingDataList.isEmpty() || collectionStagingDataList.stream().noneMatch(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Collection Data is Already Authorized or Unauthorized")))
				.filter(collectionStagingDataList -> collectionStagingDataList.isEmpty() || collectionStagingDataList.stream().allMatch(data -> data.getIsLocked().equals("No")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Collection Transaction is Already Locked for Authorization for Samity")))
				.map(loanAdjustmentDataList -> managementProcessId)
				.doOnError(throwable -> log.error("validation error -> if collection transaction is still available for samity: {}", throwable.getMessage()));
	}

	private Mono<List<MemberAttendance>> saveMemberAttendanceForSamity(PaymentCollectionBySamityCommand command) {
		List<MemberAttendanceInfo> memberAttendanceInfoList = command.getAttendanceList().stream()
				.distinct()
				.map(attendance -> MemberAttendanceInfo.builder()
						.memberId(attendance.getMemberId())
						.status(attendance.getStatus())
						.build())
				.toList();
		log.info("Member Attendance List size: {}", memberAttendanceInfoList.size());
		return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
				.map(managementProcessTracker -> MemberAttendanceRequestDTO.builder()
						.mfiId(managementProcessTracker.getMfiId())
						.samityId(command.getSamityId())
						.attendanceDate(managementProcessTracker.getBusinessDate())
						.loginId(command.getLoginId())
						.data(memberAttendanceInfoList)
						.build())
				.flatMap(memberAttendanceUseCase::saveMemberAttendanceListForSamity);
	}

	@Override
	public Mono<CollectionMessageResponseDTO> updateCollectionPaymentBySamity(PaymentCollectionBySamityCommand command) {
		final String processId = UUID.randomUUID().toString();
		AtomicReference<ManagementProcessTracker> managementProcessTrackerReference = new AtomicReference<>();
		return this.validateCollectionDataBySamity(command)
				.then(managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
						.doOnNext(managementProcessTrackerReference::set)
						.flatMap(managementProcessTracker ->
								this.validateIfCollectionAmountExceedsTotalOutstandingAmount(managementProcessTracker.getManagementProcessId(), command)
										.map(booleanList -> managementProcessTracker))
						.flatMap(managementProcessTracker -> validateDpsAmountForCollection(managementProcessTracker, command))
						.flatMap(managementProcessTracker -> validateFdrAmountForCollection(managementProcessTracker, command))
						.flatMap(managementProcessTracker -> persistencePort.editUpdateAllCollectionData(convertCollectionPaymentRequestDataToDomainBySamity(command, managementProcessTracker.getManagementProcessId(), processId), command.getLoginId())))
				.flatMap(existingEntities -> {
					// Find items in dataList that aren't present in existingEntities
					List<CollectionStagingData> dataList = convertCollectionPaymentRequestDataToDomainBySamity(command, managementProcessTrackerReference.get().getManagementProcessId(), processId);
					log.info("Data List loan account ids: {}", dataList.stream().map(CollectionStagingData::getLoanAccountId).collect(Collectors.toList()));
					List<CollectionData> newEntries = dataList.stream()
							.filter(data -> existingEntities.stream()
									.noneMatch(entity -> entity.getLoanAccountId() != null && entity.getLoanAccountId().equals(data.getLoanAccountId()) ||
											entity.getSavingsAccountId() != null && entity.getSavingsAccountId().equals(data.getSavingsAccountId())))
							.map(collectionStagingData -> modelMapper.map(collectionStagingData, CollectionData.class))
							.toList();

					log.info("New Entries loan account ids: {}", newEntries.stream().map(CollectionData::getLoanAccountId).collect(Collectors.toList()));

					return newEntries.isEmpty()
							? Mono.just(existingEntities)
							: Mono.just(newEntries)
								.flatMap(newData -> {
									command.setData(newEntries);
									command.setCollectionType(Strings.isNotNullAndNotEmpty(command.getCollectionType()) ? command.getCollectionType() : REGULAR.getValue());
									return Mono.just(this.buildCollectionDataToSave(managementProcessTrackerReference.get(), command, processId))
											.flatMapMany(Flux::fromIterable)
											.flatMap(collectionStagingData -> {
												if (collectionStagingData.getAccountType().equalsIgnoreCase(Constants.ACCOUNT_TYPE_LOAN.getValue()))
													return Mono.zip(Mono.just(collectionStagingData),
																	stagingDataUseCase.getStagingAccountDataByLoanAccountId(collectionStagingData.getLoanAccountId()),
																	loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(collectionStagingData.getLoanAccountId()))
															.flatMap(tuple3 -> this.validateLoanAmountForCollectionData(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()));
												else return Mono.just(collectionStagingData);
											})
											.collectList()
											.flatMap(persistencePort::saveAllCollectionDataToDatabase);
								});
				})
				.doOnError(throwable -> log.error("Failed to update collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later")))
				.as(this.rxtx::transactional)
				.map(response -> this.buildResponseForCollectionPayment("Collection is successfully updated for samity: " + command.getSamityId()));
	}

	@Override
	public Mono<CollectionMessageResponseDTO> updateCollectionPaymentByManagementId(PaymentCollectionBySamityCommand command) {
		return persistencePort.editUpdateCollectionDataByManagementProcessId(convertCollectionPaymentRequestDataToDomainBySamity(
						command, command.getManagementProcessId(), command.getProcessId()) , command.getManagementProcessId(),  command.getProcessId(), command.getLoginId())
				.doOnError(throwable -> log.error("Failed to update collection data : {}", throwable.getMessage()))
				.onErrorResume(ExceptionHandlerUtil.class, Mono::error)
				.onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later")))
				.as(this.rxtx::transactional)
				.map(response -> this.buildResponseForCollectionPayment("Collection is successfully updated for samity: " + command.getSamityId()));

	}

	@Override
	public Mono<CollectionMessageResponseDTO> submitCollectionPaymentForAuthorization(String managementProcessId,
																					  String processId,
																					  String loginId) {
		return persistencePort.updateStatusToSubmitCollectionDataForAuthorizationByManagementProcessId(managementProcessId, processId, loginId)
				.map(data -> CollectionMessageResponseDTO.builder()
						.userMessage("Collection Data is Submitted For Authorization Successfully")
						.build())
				.doOnNext(response -> log.info("Collection submit Response: {}", response))
				.doOnError(throwable -> log.error("Error submitting Collection: {}", throwable.getMessage()));
	}

	@Override
	public Mono<List<CollectionStagingData>> removeCollectionPayment(PaymentCollectionBySamityCommand command) {
		return persistencePort.removeCollectionData(command.getManagementProcessId(), command.getProcessId())
				.doOnNext(response -> log.info("Collection remove Response: {}", response));
	}

	private List<CollectionStagingData> buildCollectionDataToSave(ManagementProcessTracker managementProcessTracker,
																  PaymentCollectionBySamityCommand command, String processId) {
		return command.getData().stream().map(collectionData -> CollectionStagingData.builder()
				.collectionStagingDataId(UUID.randomUUID().toString())
				.managementProcessId(managementProcessTracker.getManagementProcessId())
				.stagingDataId(collectionData.getStagingDataId())
				.processId(processId)
				.samityId(command.getSamityId())
				.accountType(collectionData.getAccountType())
				.loanAccountId(collectionData.getLoanAccountId())
				.savingsAccountId(collectionData.getSavingsAccountId())
				.collectionType(command.getCollectionType())
				.amount(collectionData.getAmount())
				.paymentMode(collectionData.getPaymentMode())
				.status(Status.STATUS_STAGED.getValue())
				.createdBy(command.getLoginId())
				.createdOn(LocalDateTime.now())
				.isNew(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? "No" : "Yes")
				.currentVersion(collectionData.getCurrentVersion() == null ? "1" : collectionData.getCurrentVersion().toString())
				.editCommit("No")
				.isLocked("No")
				.isSubmitted("No")
				.updatedBy(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? command.getLoginId() : null)
				.updatedOn(collectionData.getCurrentVersion() != null && collectionData.getCurrentVersion() > 1 ? LocalDateTime.now() : null)
				.build()).toList();
	}

	private Mono<ManagementProcessTracker> validateIfCollectionIsPossibleForSamity(String officeId, String samityId, String collectionType) {
		AtomicReference<ManagementProcessTracker> managementProcessTrackerAtomicReference = new AtomicReference<>();
		return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
				.doOnNext(managementProcessTrackerAtomicReference::set)
				.flatMap(managementProcessTracker -> officeEventTrackerUseCase
						.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
						.collectList())
				.filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
						.anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
				.filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
						.noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
				.flatMap(officeEventTrackerList -> stagingDataUseCase
						.getStagingProcessEntityForSamity(managementProcessTrackerAtomicReference.get().getManagementProcessId(), samityId)
						.doOnRequest(l -> log.info("Staging Data Requested For Samity: {}, {}", managementProcessTrackerAtomicReference.get().getManagementProcessId(), samityId))
						.doOnNext(trackerEntity -> log.debug("Tracker Entity: {}", trackerEntity))
						.filter(trackerEntity -> !HelperUtil.checkIfNullOrEmpty(trackerEntity.getStatus()) && (trackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()) || trackerEntity.getStatus().equals(Status.STATUS_REGENERATED.getValue())))
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Valid For Samity"))))
				.flatMap(trackerEntity -> this.validateIfSamityIsRegularOrSpecialForCollection(managementProcessTrackerAtomicReference.get(), samityId, trackerEntity.getSamityDay(), collectionType))
				.doOnError(throwable -> log.error("validation error -> if collection is possible for samity: {}", throwable.getMessage()));
	}

	private Mono<ManagementProcessTracker> validateIfSamityIsRegularOrSpecialForCollection(
			ManagementProcessTracker managementProcessTracker, String samityId, String samityDay,
			String collectionType) {
		return Mono.just(managementProcessTracker)
				.filter(tracker -> !HelperUtil.checkIfNullOrEmpty(collectionType)
						&& (collectionType.equals(REGULAR.getValue())
						|| collectionType.equals(CollectionType.SPECIAL.getValue())
						|| collectionType.equals(CollectionType.REBATE.getValue())
						|| collectionType.equals(CollectionType.WAIVER.getValue())
						|| collectionType.equals(CollectionType.WRITE_OFF.getValue()))
						|| collectionType.equals(CollectionType.SINGLE.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						"Collection Type Must be 'Regular', 'Special', 'Rebate' or 'Waiver' or 'Single' For Samity")))
				/*.filter(tracker -> (collectionType.equals(CollectionType.REGULAR.getValue())
						&& samityDay.equals(managementProcessTracker.getBusinessDay()))
						|| (collectionType.equals(CollectionType.SPECIAL.getValue())
								&& !samityDay.equals(managementProcessTracker.getBusinessDay()))
						|| (collectionType.equals(CollectionType.REBATE.getValue())
						&& !samityDay.equals(managementProcessTracker.getBusinessDay()))
						|| (collectionType.equals(CollectionType.WAIVER.getValue())
						&& !samityDay.equals(managementProcessTracker.getBusinessDay())))
				.switchIfEmpty(Mono
						.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Type Mismatch For Samity")))*/
				.flatMap(tracker -> {
					if (collectionType.equals(REGULAR.getValue())) {
						return samityEventTrackerUseCase
								.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(),
										samityId)
								.collectList()
								.filter(samityEventTrackerList -> samityEventTrackerList.isEmpty()
										|| samityEventTrackerList.stream()
										.noneMatch(samityEventTracker -> !HelperUtil
												.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())
												&& samityEventTracker.getSamityEvent()
												.equals(SamityEvents.COLLECTED.getValue())))
								.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
										"Collection Is Already Completed For This Regular Samity")))
								.map(samityEventTrackerList -> managementProcessTracker);
					}
					return Mono.just(managementProcessTracker);
				});
	}

	private Mono<ManagementProcessTracker> validateIfCollectionDataIsValidForPayment(
			ManagementProcessTracker managementProcessTracker,
			PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
		return Mono.just(paymentCollectionBySamityCommand)
				.filter(command -> command.getData() != null && !command.getData().isEmpty())
				.switchIfEmpty(
						Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data is Provided")))
				.filter(command -> command.getData().stream()
						.noneMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getStagingDataId())))
				.switchIfEmpty(Mono.error(
						new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Id Field Cannot Be Empty")))
				.filter(command -> command.getData().stream()
						.noneMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getPaymentMode())))
				.switchIfEmpty(Mono
						.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Payment Mode Field Cannot Be Empty")))
				.filter(command -> command.getData().stream()
						.allMatch(collectionData -> !HelperUtil.checkIfNullOrEmpty(collectionData.getAccountType())
								&& (collectionData.getAccountType().equals(Constants.ACCOUNT_TYPE_LOAN.getValue())
								|| collectionData.getAccountType().equals(Constants.ACCOUNT_TYPE_SAVINGS.getValue()))))
				.switchIfEmpty(Mono.error(
						new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Account Type Must be 'Loan' or 'Savings'")))
				.filter(command -> command.getData().stream()
						.allMatch(item -> item.getAmount() != null && item.getAmount().compareTo(BigDecimal.ZERO) >= 0))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						"Collection Amount Cannot be Empty or Negative")))
				.flatMap(command -> this.validateIfStagingDataIdAndAccountIdListIsValidForSamity(
						managementProcessTracker.getManagementProcessId(), command))
				.flatMap(map -> this.validateIfCollectionAmountExceedsTotalOutstandingAmount(managementProcessTracker.getManagementProcessId(), paymentCollectionBySamityCommand))
				.map(data -> managementProcessTracker)
				.doOnError(throwable -> log.error("validation error -> if collection data is valid for payment: {}", throwable.getMessage()));
	}

	private Mono<List<Boolean>> validateIfCollectionAmountExceedsTotalOutstandingAmount(String managementProcessId, PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
		return Flux.fromIterable(paymentCollectionBySamityCommand.getData())
				.flatMap(collectionData -> {
					if (collectionData.getLoanAccountId() != null && !collectionData.getLoanAccountId().isEmpty()) {
						return this.validateCollectionAmountForLoanAccounts(managementProcessId, collectionData);
					}
					return Mono.just(false);
				})
				.collectList()
				.doOnError(throwable -> log.error("Error validating collection amount: {}", throwable.getMessage()));
	}

	private Mono<Boolean> validateCollectionAmountForLoanAccounts(String managementProcessId, CollectionData collectionData) {
		return this.getStagingAccountDataByLoanAccountId(managementProcessId, collectionData.getLoanAccountId())
				.flatMap(stagingAccountData -> validateCollectionAmount(stagingAccountData, collectionData))
				.doOnError(throwable -> log.error("Error validating collection amount: {}", throwable.getMessage()));
	}



	private Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String managementProcessId, String loanAccountId) {
		return collectionStagingDataQueryUseCase.getStagingAccountDataByLoanAccountId(loanAccountId, managementProcessId);
	}



	private Mono<Boolean> validateCollectionAmount(StagingAccountData stagingAccountData, CollectionData collectionData) {
		BigDecimal collectionAmount = collectionData.getAmount();
		BigDecimal totalOutstandingAmount = stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining());
		if (collectionAmount.compareTo(totalOutstandingAmount) > 0) {
			return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Amount Exceeds Total Outstanding Amount for Loan Account : " + collectionData.getLoanAccountId()));
		} else {
			return Mono.just(true);
		}
	}

	private Mono<ManagementProcessTracker> validateIfLoanAccountIsValidForPayment(
			ManagementProcessTracker managementProcessTracker,
			PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {

		log.debug("Collection Type: {}", paymentCollectionBySamityCommand.getCollectionType());
		log.debug("checkIfNullOrEmpty: {}", HelperUtil.checkIfNullOrEmpty(paymentCollectionBySamityCommand.getCollectionType()));
		log.debug("checkIfRegular : {}", paymentCollectionBySamityCommand.getCollectionType().equals(REGULAR.getValue()));
		log.debug("checkIfSpecial : {}", paymentCollectionBySamityCommand.getCollectionType().equals(CollectionType.SPECIAL.getValue()));

		log.info("boolean : {}", !HelperUtil.checkIfNullOrEmpty(paymentCollectionBySamityCommand.getCollectionType()) &&
				(!paymentCollectionBySamityCommand.getCollectionType().equals(REGULAR.getValue()) ||
						!paymentCollectionBySamityCommand.getCollectionType().equals(CollectionType.SPECIAL.getValue())));

		if (!HelperUtil.checkIfNullOrEmpty(paymentCollectionBySamityCommand.getCollectionType()) &&
				(!paymentCollectionBySamityCommand.getCollectionType().equals(REGULAR.getValue()) &&
						!paymentCollectionBySamityCommand.getCollectionType().equals(CollectionType.SPECIAL.getValue()))) {
			return Mono.just(managementProcessTracker);
		}
		log.info("Loan Account Validation Started");

		return this.filterOutRebatedAndWaivedCollectionData(paymentCollectionBySamityCommand)
				.map(PaymentCollectionBySamityCommand::getData)
				.flatMapMany(Flux::fromIterable)
				.filter(collectionData -> Strings.isNotNullAndNotEmpty(collectionData.getLoanAccountId()))
				.map(CollectionData::getLoanAccountId)
				.flatMap(commonRepository::findFirstCollectionPaymentDataByLoanAccountId)
				.flatMap(isExist -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data exists. Please update the existing data.")))
				.doOnNext(response -> log.info("Loan Account Validation Response in payment collection: {}", response))
				.any(Boolean.FALSE::equals)
				.flatMap(exists -> Mono.just(managementProcessTracker))
				.switchIfEmpty(Mono.just(managementProcessTracker))
				.doOnError(throwable -> log.error("validation error -> if loan account is valid for payment: {}", throwable.getMessage()));
	}

	private Mono<PaymentCollectionBySamityCommand> filterOutRebatedAndWaivedCollectionData(PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {
		return this.getRebatedAndWaivedLoanAccountsBySamity(paymentCollectionBySamityCommand.getSamityId())
				.flatMap(rebatedAndWaivedLoanAccounts -> {
					List<CollectionData> filteredCollectionDataList = paymentCollectionBySamityCommand.getData().stream()
							.filter(collectionData -> !rebatedAndWaivedLoanAccounts.contains(collectionData.getLoanAccountId()))
							.collect(Collectors.toList());
					paymentCollectionBySamityCommand.setData(filteredCollectionDataList);
					return Mono.just(paymentCollectionBySamityCommand);
				});
	}

	private Mono<List<String>> getRebatedAndWaivedLoanAccountsBySamity(String samityId) {
		return loanRebatePersistencePort.getAllLoanRebateDataBySamityIdList(List.of(samityId))
				.zipWith(loanWaiverPersistencePort.getAllLoanWaiverDataBySamityIdList(List.of(samityId)))
				.map(rebateAndWaiverTuple -> {
					List<LoanRebateDTO> loanRebateDTOList = rebateAndWaiverTuple.getT1();
					List<LoanWaiverDTO> loanWaiverDTOList = rebateAndWaiverTuple.getT2();

					List<String> rebatedLoanAccounts = loanRebateDTOList.stream()
							.filter(loanRebateDTO -> loanRebateDTO.getRebateAmount() != null && loanRebateDTO.getRebateAmount().compareTo(BigDecimal.ZERO) > 0)
							.map(LoanRebateDTO::getLoanAccountId)
							.toList();

					List<String> waivedLoanAccounts = loanWaiverDTOList.stream()
							.filter(loanWaiverDTO -> loanWaiverDTO.getWaivedAmount() != null && loanWaiverDTO.getWaivedAmount().compareTo(BigDecimal.ZERO) > 0)
							.map(LoanWaiverDTO::getLoanAccountId)
							.toList();

					List<String> rebatedAndWaivedLoanAccounts = Stream.concat(rebatedLoanAccounts.stream(), waivedLoanAccounts.stream())
							.toList();

					return rebatedAndWaivedLoanAccounts;
				});
	}


	private Mono<ManagementProcessTracker> validateIfSavingsAccountIsValidForPayment(
			ManagementProcessTracker managementProcessTracker,
			PaymentCollectionBySamityCommand paymentCollectionBySamityCommand) {


		if (!HelperUtil.checkIfNullOrEmpty(paymentCollectionBySamityCommand.getCollectionType()) &&
				(!paymentCollectionBySamityCommand.getCollectionType().equals(REGULAR.getValue()) &&
						!paymentCollectionBySamityCommand.getCollectionType().equals(CollectionType.SPECIAL.getValue()))) {
			return Mono.just(managementProcessTracker);
		}

		log.info("Savings Account Validation Started");
		return Flux.fromIterable(paymentCollectionBySamityCommand.getData())
				.filter(collectionData -> Strings.isNotNullAndNotEmpty(collectionData.getSavingsAccountId()))
				.map(CollectionData::getSavingsAccountId)
				.flatMap(commonValidation::checkIfCollectionDataNotExistsForSavingsAccountId)
				.doOnNext(response -> log.info("Savings Account Validation Response in payment collection: {}", response))
				.any(Boolean.FALSE::equals)
				.flatMap(exists -> {
					if (exists) {
						return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data exists. Please update the existing data."));
					} else {
						return Mono.just(managementProcessTracker);
					}
				})
				.switchIfEmpty(Mono.just(managementProcessTracker))
				.doOnError(throwable -> log.error("validation error -> if Savings account is valid for payment: {}", throwable.getMessage()));
	}

	private Mono<Map<String, String>> validateIfStagingDataIdAndAccountIdListIsValidForSamity(
			String managementProcessId, PaymentCollectionBySamityCommand command) {
		return stagingDataUseCase.getAllStagingDataBySamity(managementProcessId, command.getSamityId())
				.collectList()
				.flatMap(stagingDataList -> {
					List<String> memberIdList = stagingDataList.stream().map(StagingData::getMemberId).toList();
					return stagingDataUseCase.getAllStagingAccountDataByMemberIdList(managementProcessId, memberIdList).collectList()
							.zipWith(commonRepository
									.getAllRegularLoanProducts()
									.switchIfEmpty(Mono.just(LoanProductEntity.builder().loanProductId("").build()))
									.collectList())
							.map(stagingAccountDataAndLoanProductTuple -> {
								List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> stagingAccountDataList = stagingAccountDataAndLoanProductTuple.getT1();
								List<String> regularLoanProductIds = stagingAccountDataAndLoanProductTuple.getT2().stream().map(LoanProductEntity::getLoanProductId).toList();
								log.info("Regular Loan Product Ids: {}", regularLoanProductIds);

								List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> savingsAccountList =
										new ArrayList<>(stagingAccountDataList
												.stream()
												.filter(stagingAccountData -> stagingAccountData.getSavingsAccountId() != null)
												.filter(stagingAccountData -> !((stagingAccountData.getSavingsProductType().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue())
																				|| stagingAccountData.getSavingsProductType().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue()))
																				&& stagingAccountData.getTargetAmount().compareTo(BigDecimal.ZERO) == 0))
												.toList());
								List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> loanAccountList =
										stagingAccountDataList.stream().filter(stagingAccountData -> stagingAccountData.getLoanAccountId() != null).toList();

								if (command.getCollectionType().equals(REGULAR.getValue())) {
									loanAccountList = loanAccountList.stream().filter(stagingAccountData -> regularLoanProductIds.contains(stagingAccountData.getProductCode())).toList();
								}
								log.info("Loan Account List size: {}", loanAccountList.size());
								log.info("Savings Account List size: {}", savingsAccountList.size());

								savingsAccountList.addAll(loanAccountList);
								return savingsAccountList;
							})
							.map(stagingAccountDataList -> Tuples.of(stagingDataList, stagingAccountDataList));
				})
				.doOnNext(tuple2 -> log.info("Staging Account Data List size: {} | command.getData size : {}", tuple2.getT2().size(), command.getData().size()))
				.flatMap(tuple -> {
					log.info("Collection Type: {}", command.getCollectionType());
					if (command.getCollectionType().equals(CollectionType.REGULAR.getValue())) {
						if (tuple.getT2().size() == command.getData().size()) {
							return Mono.just(tuple);
						} else return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Not Enough Collection Data is Provided for this Member"));
					}
					return Mono.just(tuple);
				})
				.filter(tuple -> command.getCollectionType().equals(CollectionType.REGULAR.getValue())
						|| command.getCollectionType().equals(CollectionType.SPECIAL.getValue())
						|| command.getCollectionType().equals(CollectionType.REBATE.getValue())
						|| command.getCollectionType().equals(CollectionType.WAIVER.getValue())
						|| command.getCollectionType().equals(CollectionType.WRITE_OFF.getValue())
						|| command.getCollectionType().equals(CollectionType.SINGLE.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						"Not Enough Collection Data is Provided for this regular collection")))
//				.doOnNext(tuple2 -> log.info("staging data : {}, staging account data : {}", tuple2.getT1(), tuple2.getT2()))
				.map(tuple -> {
					Map<String, String> accountIdWithStagingDataIdMap = new HashMap<>();
					tuple.getT1().forEach(stagingData -> tuple.getT2().forEach(stagingAccountData -> {
						if (stagingData.getMemberId().equals(stagingAccountData.getMemberId())) {
							if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
								accountIdWithStagingDataIdMap.put(stagingAccountData.getLoanAccountId(),
										stagingData.getStagingDataId());
							} else if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
								accountIdWithStagingDataIdMap.put(stagingAccountData.getSavingsAccountId(),
										stagingData.getStagingDataId());
							}
						}
					}));
					return accountIdWithStagingDataIdMap;
				})
				.doOnNext(accountIdWithStagingDataIdMap -> log.debug("AccountId with StagingDataId Map For Samity: {}",
						accountIdWithStagingDataIdMap))
				.filter(accountIdWithStagingDataIdMap -> command.getData().stream()
						.allMatch(collectionData -> (collectionData.getAccountType().equals("Loan")
								&& collectionData.getStagingDataId()
								.equals(accountIdWithStagingDataIdMap.get(collectionData.getLoanAccountId())))
								|| (collectionData.getAccountType().equals("Savings")
								&& collectionData.getStagingDataId()
								.equals(accountIdWithStagingDataIdMap
										.get(collectionData.getSavingsAccountId())))))
				.switchIfEmpty(Mono.error(
						new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Id Mismatch with AccountId")));
	}

	private Mono<List<CollectionStagingData>> saveCollectionData(PaymentCollectionBySamityCommand command, String isUploaded) {
		final String processId = UUID.randomUUID().toString();
		return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
				.flatMap(
						managementProcessTracker -> persistencePort
								.saveAllCollectionData(
										convertCollectionPaymentRequestDataToDomainBySamity(command,
												managementProcessTracker.getManagementProcessId(), processId),
										isUploaded)
								.map(collectionStagingDataList -> Tuples.of(managementProcessTracker,
										collectionStagingDataList)))
				.flatMap(tuple -> samityEventTrackerUseCase
						.getSamityEventByEventTypeForSamity(tuple.getT1().getManagementProcessId(),
								tuple.getT2().get(0).getSamityId(), SamityEvents.COLLECTED.getValue())
						.map(samityEventTracker -> Tuples.of(tuple.getT1(), tuple.getT2(), samityEventTracker)))
				.flatMap(tuple -> {
					if (HelperUtil.checkIfNullOrEmpty(tuple.getT3().getSamityEvent())) {
						return samityEventTrackerUseCase
								.insertSamityEvent(tuple.getT1().getManagementProcessId(), processId,
										command.getOfficeId(), command.getSamityId(), SamityEvents.COLLECTED.getValue(),
										command.getLoginId())
								.map(samityEventTracker -> tuple.getT2());
					}
					return Mono.just(tuple.getT2());
				});
	}

	private Mono<Boolean> checkIfLogInIdIsMatchedWithLockedBy(PaymentCollectionBySamityCommand command) {
		return persistencePort
				.getAllCollectionData(command.getSamityId(),
						command.getData().get(0).getCollectionType())
				.switchIfEmpty(Mono
						.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found in Database")))
				// .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getLockedBy()))
				// .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
				// "Collection Data is not Locked")))
				// .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getLockedBy()) &&
				// entity.getLockedBy().equals(command.getLoginId()))
				// .doOnNext(entity -> log.info("lockedBy Id matched with loginId: {}",
				// command.getLoginId()))
				// .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
				// "lockedBy Id is not matched with loginId")))
				.collectList()
				.filter(list -> list.size() == command.getData().size())
				.hasElement()
				.doOnError(
						throwable -> log.error("Failed to verify Collection Staging Data: {}",
								throwable.getMessage()));
	}

	private CollectionMessageResponseDTO buildResponseForCollectionPayment(String userMessage) {
		return CollectionMessageResponseDTO.builder()
				.userMessage(userMessage)
				.build();
	}

	private Mono<Boolean> validateCollectionDataBySamity(PaymentCollectionBySamityCommand command) {
		return Mono.just(command)
				.filter(c -> !c.getData().isEmpty())
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.NO_COLLECTION_DATA_FOUND.getValue())))
				.flatMap(this::getCollectionDataToVerifyPayment)
				.hasElement()
				.doOnNext(aBoolean -> log.info("Collection Data Verified for samity, stagingDataId with accountId and collectionType."))
				.doOnError(throwable -> log.error("Failed to verify Collection Staging Data: {}", throwable.getMessage()));
	}

	private Mono<Boolean> checkCollectionDataValidationForFieldOfficer(
			PaymentCollectionByFieldOfficerCommand command) {
		return Mono.just(command)
				.filter(c -> !c.getData().isEmpty())
				.doOnNext(c -> log.info("Collection Data is present"))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						ExceptionMessages.NO_COLLECTION_DATA_FOUND.getValue())))
				.flatMapMany(c -> {
					List<PaymentCollectionBySamityCommand> list = new ArrayList<>();
					c.getData().forEach(data -> {
						PaymentCollectionBySamityCommand buildData = PaymentCollectionBySamityCommand.builder()
								.mfiId(c.getMfiId())
								.loginId(c.getLoginId())
								.officeId(c.getOfficeId())
								.fieldOfficerId(c.getFieldOfficerId())
								.samityId(data.getSamityId())
								.data(data.getCollectionStagingDataList())
								.build();
						list.add(buildData);
					});
					log.info("Incoming DataList: {}", list);
					return Flux.fromIterable(list);
				})
				.flatMap(this::getCollectionDataToVerifyPayment)
				.collectList()
				.hasElement()
				.doOnNext(aBoolean -> log
						.info("Collection Data Verified for samity, stagingDataId with accountId and collectionType."))
				.doOnError(
						throwable -> log.error("Failed to verify Collection Staging Data: {}",
								throwable.getMessage()));
	}

	private Mono<CollectionDataVerifyDTO> getCollectionDataToVerifyPayment(PaymentCollectionBySamityCommand command) {
		List<String> stagingDataIdForLoanList = new ArrayList<>();
		List<String> loanAccountIdList = new ArrayList<>();
		List<String> stagingDataIdForSavingsList = new ArrayList<>();
		List<String> savingsAccountIdList = new ArrayList<>();

		command.getData().forEach(data -> {
			if (data.getStagingDataId() != null && !data.getStagingDataId().isEmpty()
					&& data.getAccountType().equals("Loan")) {
				stagingDataIdForLoanList.add(data.getStagingDataId());
				if (data.getLoanAccountId() != null && !data.getLoanAccountId().isEmpty()) {
					loanAccountIdList.add(data.getLoanAccountId());
				}
			}
			if (data.getStagingDataId() != null && !data.getStagingDataId().isEmpty()
					&& data.getAccountType().equals("Savings")) {
				stagingDataIdForSavingsList.add(data.getStagingDataId());
				if (data.getSavingsAccountId() != null && !data.getSavingsAccountId().isEmpty()) {
					savingsAccountIdList.add(data.getSavingsAccountId());
				}
			}
		});

		CollectionDataVerifyDTO verifyDTO = CollectionDataVerifyDTO.builder()
				.officeId(command.getOfficeId())
				.samityId(command.getSamityId())
				.stagingDataIdForLoanList(stagingDataIdForLoanList)
				.loanAccountIdList(loanAccountIdList)
				.stagingDataIdForSavingsList(stagingDataIdForSavingsList)
				.savingsAccountIdList(savingsAccountIdList)
				.totalCount(0)
				.build();

		// log.info("CollectionDataVerifyDTO Build: {}", verifyDTO);
		return persistencePort.getCollectionDataToVerifyPayment(verifyDTO)
				// .map(dto -> this.checkForRegularOrSpecialCollectionVerification(command,
				// dto))
				.flatMap(dto -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(dto.getOfficeId())
						.map(managementProcessTracker -> Tuples.of(dto, managementProcessTracker)))
				.doOnNext(tuple -> log.info("CollectionDataVerifyDTO: {}\n managementProcessTracker: {}", tuple.getT1(),
						tuple.getT2()))
				.filter(tuple -> tuple.getT1().getTotalCount() > 0 || tuple.getT1().getSamityDay()
						.equalsIgnoreCase(tuple.getT2().getBusinessDay()))
				.switchIfEmpty(Mono.error(
						new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
								ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_SAMITY
										.getValue()
										.concat(" Samity ID: " + verifyDTO
												.getSamityId()))))
				.map(Tuple2::getT1)
				.filter(dto -> dto.getTotalCount() == command.getData().size())
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_STAGING_DATA_ID_AND_ACCOUNT_ID
								.getValue()
								.concat(" Samity ID: " + verifyDTO.getSamityId()))));
//				.filter(dto -> command.getData().stream()
//						.allMatch(data -> data.getCollectionType()
//								.equals(dto.getCollectionType())))
//				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
//						ExceptionMessages.COLLECTION_DATA_MISMATCH_FOR_COLLECTION_TYPE
//								.getValue()
//								.concat(" Samity ID: " + verifyDTO.getSamityId()))));
	}

	private Flux<PaymentCollectionBySamityCommand> convertCollectionPaymentRequestDataToDomainByFieldOfficer(
			PaymentCollectionByFieldOfficerCommand data) {
		List<PaymentCollectionBySamityCommand> list = new ArrayList<>();
		data.getData().forEach(collectionDataForFieldOfficer -> {
			PaymentCollectionBySamityCommand buildData = PaymentCollectionBySamityCommand.builder()
					.fieldOfficerId(data.getFieldOfficerId())
					.mfiId(data.getMfiId())
					.loginId(data.getLoginId())
					.officeId(data.getOfficeId())
					.samityId(collectionDataForFieldOfficer.getSamityId())
					.data(collectionDataForFieldOfficer.getCollectionStagingDataList())
					.build();
			list.add(buildData);
		});
		return Flux.fromIterable(list);
	}

	private List<CollectionStagingData> convertCollectionPaymentRequestDataToDomainBySamity(
			PaymentCollectionBySamityCommand command, String managementProcessId, String processId) {
		log.info("Management Process Id: {} and process id: {}", managementProcessId, processId);
		return command.getData().stream()
				.map(data -> {
					CollectionStagingData collectionStagingData = modelMapper.map(data,
							CollectionStagingData.class);
					collectionStagingData.setManagementProcessId(managementProcessId);
					collectionStagingData.setProcessId(processId);
					collectionStagingData.setSamityId(command.getSamityId());
					collectionStagingData.setCreatedBy(command.getLoginId());
					collectionStagingData.setStatus(STATUS_STAGED);
					log.debug("Converted Collection Staging Data: {}", collectionStagingData);
					return collectionStagingData;
				})
				.collect(Collectors.toList());
	}

}
