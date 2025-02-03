package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.LoanProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.DpsRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.ICommonValidationUseCase;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.LoanTypeID;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.gateway.IPassbookTransactionGateway;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.LoanAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.SavingsAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingSavingsAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.IStagingAccountDataCommands;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.LoanRepayScheduleDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.PassbookEntryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.PRODUCT_TYPE_FDR;

@Slf4j
@Service
public class StagingDataService implements IStagingDataUseCase {

	private final TransactionalOperator rxtx;
	private final IStagingAccountDataCommands stagingAccountDataCommands;
	private final IStagingAccountDataPersistencePort stagingAccountPort;
	private final IStagingDataPersistencePort stagingDataPort;
	private final IStagingProcessTrackerPersistencePort processTrackerPort;
	private final LoanAccountUseCase loanAccountUseCase;
	private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
	private final DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase;
	private final PassbookUseCase passbookUseCase;
	private final ISavingsAccountUseCase savingsAccountUseCase;
	private final IPassbookTransactionGateway passbookTransactionGateway;
	private final ManagementProcessTrackerUseCase managementProcessUseCase;
	private final OfficeEventTrackerUseCase officeEventUseCase;
	private final SamityEventTrackerUseCase samityEventUseCase;
	private final CommonRepository commonRepository;
	private final ICommonValidationUseCase validationUseCase;
	private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
	private final ModelMapper mapper;

	private final Gson gson;

	public StagingDataService(
            TransactionalOperator rxtx,
            IStagingAccountDataCommands stagingAccountDataCommands,
            IStagingAccountDataPersistencePort stagingAccountPort,
            IStagingDataPersistencePort stagingDataPort,
            IStagingProcessTrackerPersistencePort processTrackerPort,
            LoanAccountUseCase loanAccountUseCase,
            LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase,
            PassbookUseCase passbookUseCase,
            ISavingsAccountUseCase savingsAccountUseCase,
            IPassbookTransactionGateway passbookTransactionGateway,
            ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
            OfficeEventTrackerUseCase officeEventTrackerUseCase,
            SamityEventTrackerUseCase samityEventTrackerUseCase, CommonRepository commonRepository, ICommonValidationUseCase validationUseCase, DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort, ModelMapper mapper) {
        this.dpsRepaymentScheduleUseCase = dpsRepaymentScheduleUseCase;
        this.managementProcessUseCase = managementProcessTrackerUseCase;
		this.officeEventUseCase = officeEventTrackerUseCase;
		this.samityEventUseCase = samityEventTrackerUseCase;
		this.commonRepository = commonRepository;
		this.rxtx = rxtx;
		this.stagingAccountDataCommands = stagingAccountDataCommands;
		this.stagingAccountPort = stagingAccountPort;
		this.stagingDataPort = stagingDataPort;
		this.processTrackerPort = processTrackerPort;
		this.loanAccountUseCase = loanAccountUseCase;
		this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
		this.passbookUseCase = passbookUseCase;
		this.savingsAccountUseCase = savingsAccountUseCase;
		this.passbookTransactionGateway = passbookTransactionGateway;
        this.validationUseCase = validationUseCase;
        this.dayEndProcessTrackerPersistencePort = dayEndProcessTrackerPersistencePort;
        this.mapper = mapper;
		this.gson = CommonFunctions.buildGson(this);
	}

	@Override
	public Mono<StagingDataGenerationResponseDTO> generateStagingDataAndStagingAccountData(
			StagingDataRequestDTO request) {
		final String processId = UUID.randomUUID().toString();
		return this.checkIfStagingDataGenerationRequestIsValid(request)
				.flatMapMany(
						requestDTO -> processTrackerPort.getStagingDataGenerationStatusFlux(requestDTO.getOfficeId()))
				.flatMap(stagingDataGenerationStatusDTO -> managementProcessUseCase
						.getLastManagementProcessForOffice(request.getOfficeId())
						.map(managementProcessTracker -> {
							stagingDataGenerationStatusDTO
									.setManagementProcessId(managementProcessTracker.getManagementProcessId());
							stagingDataGenerationStatusDTO.setBusinessDate(managementProcessTracker.getBusinessDate());
							stagingDataGenerationStatusDTO.setBusinessDay(managementProcessTracker.getBusinessDay());
							stagingDataGenerationStatusDTO.setProcessId(processId);
							stagingDataGenerationStatusDTO.setStatus(Status.STATUS_WAITING.getValue());
							return stagingDataGenerationStatusDTO;
						}))
				.collectList()
                .filter(collectedList -> !collectedList.isEmpty())
				.map(list -> {
					list.sort(Comparator.comparing(StagingDataGenerationStatusDTO::getSamityId));
					return list;
				})
				.map(list -> StagingDataGenerationResponseDTO
						.builder()
						.stagingDataGenerationStatus(list)
						.totalCount(list.size())
						.build())
				.doOnNext(dto -> this.saveStagingDataGenerationStatusAndStagingDataWithStagingAccountData(dto,
						request.getOfficeId(), request.getLoginId(), processId));
	}

	private Mono<StagingDataRequestDTO> checkIfStagingDataGenerationRequestIsValid(StagingDataRequestDTO request) {
		return managementProcessUseCase.getLastManagementProcessIdForOffice(request.getOfficeId())
				.flatMap(managementProcessId -> officeEventUseCase
						.getAllOfficeEventsForOffice(managementProcessId, request.getOfficeId())
                        .collectList())
				.doOnNext(officeEventTrackerList -> log.info("Office Event List: {}", officeEventTrackerList))
				.filter(officeEventTrackerList -> officeEventTrackerList.stream()
						.noneMatch(item -> item.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						"Staging Data Already found for officeId: " + request.getOfficeId())))
				.map(officeEventTrackerList -> request)
				.flatMap(req -> processTrackerPort.getAllSamityIdListByOfficeId(req.getOfficeId())
						.filter(samityIdList -> samityIdList == null || samityIdList.isEmpty()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
						"Staging Data Already found for officeId: " + request.getOfficeId())))
				.map(stringList -> request);
	}


	@Override
	public Mono<StagingDataDetailViewResponseDTO> getStagingDataDetailViewResponseBySamityId(
			StagingDataRequestDTO request) {
		return stagingDataPort.getSamityInfoForStagingDataDetailView(request.getSamityId())
				.flatMap(s -> Mono.zip(Mono.just(s), processTrackerPort.getTotalMemberForOneSamity(s.getSamityId()))
						.map(t -> {
							t.getT1().setTotalMember(t.getT2());
							return t.getT1();
						}))
				.flatMap(this::getMemberInfoForStagingDataDetailViewBySamityId)
				.flatMap(this::getLoanAndSavingsAccountSummeryByProductCode)
				.doOnNext(s -> log.info("Staging Data Grid View Response: {}", s))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NO_CONTENT,
						ExceptionMessages.NO_STAGING_DATA_FOUND_FOR_SAMITY.getValue() + request.getSamityId())));
	}

	@Override
	public Mono<StagingDataMemberInfoDetailViewResponseDTO> getStagingDataDetailViewResponseByAccountId(
			StagingDataRequestDTO request) {
		return stagingDataPort.getSamityInfoForStagingDataDetailViewByAccountId(request.getAccountId())
				.flatMap(dto -> Mono
						.zip(Mono.just(dto), processTrackerPort.getTotalMemberForOneSamity(dto.getSamityId()))
						.map(t -> {
							t.getT1().setTotalMember(t.getT2());
							return t.getT1();
						}))
				.flatMap(dto -> this.getMemberInfoForStagingDataDetailViewByAccountId(dto, request.getAccountId()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NO_CONTENT,
						ExceptionMessages.NO_STAGING_DATA_FOUND_WITH_ACCOUNT_ID.getValue() + request.getAccountId())));
	}

	@Override
	public Mono<StagingDataGenerationStatusResponseDTO> getStagingDataGenerationStatusResponse(
			StagingDataRequestDTO requestDTO) {
		return processTrackerPort.getStagingDataGenerationStatusResponse(requestDTO.getOfficeId())
				.flatMap(statusDTO -> managementProcessUseCase
						.getLastManagementProcessForOffice(statusDTO.getOfficeId())
						.map(managementProcessTracker -> {
							statusDTO.setManagementProcessId(managementProcessTracker.getManagementProcessId());
							statusDTO.setBusinessDate(managementProcessTracker.getBusinessDate());
							statusDTO.setBusinessDay(managementProcessTracker.getBusinessDay());
							return statusDTO;
						}))
				.flatMap(statusDTO -> processTrackerPort.getTotalMemberForOneSamity(statusDTO.getSamityId())
						.map(total -> {
							statusDTO.setTotalMember(total);
							return statusDTO;
						}))
				.collectList()
                .filter(collectedList -> !collectedList.isEmpty())
				.map(list -> {
					list.sort(Comparator.comparing(StagingDataGenerationStatusDTO::getSamityId));
					return list;
				})
				.map(list -> StagingDataGenerationStatusResponseDTO.builder()
						.isEnabled(!list.isEmpty() ? "No" : "Yes")
						.userMessage("")
						.stagingDataGenerationStatus(list)
						.totalCount((long) list.size())
						.build())
				.doOnNext(response -> log.info("Staging Data Generation Status Response: {}", response));
	}


	@Override
	public Flux<StagingDataResponseDTO> getStagingDataBySamityId(String samityId) {
		return stagingDataPort
				.getStagingDataBySamityId(samityId)
				.map(stagingDataTransactionDTO -> mapper.map(stagingDataTransactionDTO, StagingDataResponseDTO.class));
	}

	@Override
	public Mono<StagingDataMemberInfoDetailViewResponseDTO> getStagingDataDetailViewResponseByMemberId(
			StagingDataRequestDTO request) {
		return stagingDataPort.getSamityInfoForStagingDataDetailViewByMemberId(request.getMemberId())
				.flatMap(dto -> processTrackerPort.getTotalMemberForOneSamity(dto.getSamityId())
						.map(count -> {
							dto.setTotalMember(count);
							return dto;
						}))
				.flatMap(dto -> stagingDataPort.getMemberInfoForStagingDataDetailViewByMemberId(request.getMemberId())
						.flatMap(this::getStagingLoanAccountListForStagingDataDetailViewByMemberId)
						.flatMap(this::getStagingSavingsAccountListForStagingDataDetailViewByMemberId)
						.map(memberInfoDTO -> {
							List<MemberInfoDTO> memberList = new ArrayList<>();
							memberList.add(memberInfoDTO);
							dto.setMemberList(memberList);
							return dto;
						}))
				.doOnNext(stagingDataMemberInfoDetailViewResponseDTO -> log.debug(
						"stagingDataMemberInfoDetailViewResponseDTO : {}", stagingDataMemberInfoDetailViewResponseDTO));
	}


	@Override
	public Flux<StagingData> getStagingDataByFieldOfficer(String fieldOfficerId) {
		return stagingDataPort.getStagingDataByFieldOfficer(fieldOfficerId)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
						ExceptionMessages.NO_STAGING_DATA_FOUND_WITH_FIELD_OFFICER_ID.getValue() + fieldOfficerId)));
	}


	private Mono<StagingDataDetailViewResponseDTO> getMemberInfoForStagingDataDetailViewBySamityId(
			StagingDataDetailViewResponseDTO dto) {
		Flux<MemberInfoDTO> memberInfoList = stagingDataPort
				.getMemberInfoListForStagingDataDetailViewBySamityId(dto.getSamityId())
				.flatMap(this::getStagingLoanAccountListForStagingDataDetailViewByMemberId)
				.flatMap(this::getStagingSavingsAccountListForStagingDataDetailViewByMemberId);
		return Mono.zip(Mono.just(dto), memberInfoList.collectList())
				.map(t -> {
					t.getT1().setMemberList(t.getT2());
					return t.getT1();
				});
	}

	private Mono<StagingDataMemberInfoDetailViewResponseDTO> getMemberInfoForStagingDataDetailViewByAccountId(
			StagingDataMemberInfoDetailViewResponseDTO dto, String accountId) {
		log.debug("Account Id received: {}", accountId);
		Mono<MemberInfoDTO> memberInfo = stagingDataPort.getMemberInfoForStagingDataDetailViewByAccountId(accountId)
				.flatMap(memberInfoDTO -> this
						.getStagingLoanOrSavingsAccountForStagingDataDetailViewByAccountId(memberInfoDTO, accountId));
		return memberInfo
				.map(m -> {
					List<MemberInfoDTO> list = new ArrayList<>();
					list.add(m);
					dto.setMemberList(list);
					return dto;
				});
	}

	private Mono<MemberInfoDTO> getStagingLoanOrSavingsAccountForStagingDataDetailViewByAccountId(MemberInfoDTO dto,
			String accountId) {
		List<StagingLoanAccountInfoDTO> loanAccountList = new ArrayList<>();
		List<StagingSavingsAccountInfoDTO> savingsAccountList = new ArrayList<>();

		return stagingAccountPort.getStagingLoanOrSavingsAccountByAccountId(accountId)
				.map(stagingAccountData -> {
					if (stagingAccountData.getLoanAccountId() != null
							&& stagingAccountData.getLoanAccountId().equals(accountId)) {
						StagingLoanAccountInfoDTO loanAccount = mapper.map(stagingAccountData,
								StagingLoanAccountInfoDTO.class);
						loanAccount.setInstallments(stagingAccountData.getInstallments());
						loanAccountList.add(loanAccount);

					} else if (stagingAccountData.getSavingsAccountId() != null
							&& stagingAccountData.getSavingsAccountId().equals(accountId)) {
						StagingSavingsAccountInfoDTO savingsAccount = mapper.map(stagingAccountData,
								StagingSavingsAccountInfoDTO.class);
						savingsAccountList.add(savingsAccount);

					}
					dto.setLoanAccountList(loanAccountList);
					dto.setSavingsAccountList(savingsAccountList);
					return dto;
				})
				.doOnNext(memberInfoDTO -> log.info("MemberInfoDTO: {}", memberInfoDTO));
	}

	private Mono<MemberInfoDTO> getStagingLoanAccountListForStagingDataDetailViewByMemberId(MemberInfoDTO dto) {
		Flux<StagingLoanAccountInfoDTO> loanAccountFlux = stagingAccountPort
				.getStagingLoanAccountDataListByMemberId(dto.getMemberId()).collectList()
				.zipWith(commonRepository
						.getAllRegularLoanProducts()
						.switchIfEmpty(Mono.just(LoanProductEntity.builder().loanProductId("").build()))
						.collectList())
				.map(stagingAccountDataAndLoanProductTuple -> {
					List<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> stagingAccountDataList = stagingAccountDataAndLoanProductTuple.getT1();
					List<String> regularLoanProductIds = stagingAccountDataAndLoanProductTuple.getT2().stream().map(LoanProductEntity::getLoanProductId).toList();
					return stagingAccountDataList.stream().filter(stagingAccountData -> regularLoanProductIds.contains(stagingAccountData.getProductCode())).toList();
				})
				.flatMapMany(Flux::fromIterable)
				.doOnNext(s -> log.info("Staging Account Data From Adapter: {}", s))
				.flatMap(sad -> {
					StagingLoanAccountInfoDTO stagingLoanAccountInfoDTO = mapper.map(sad,
							StagingLoanAccountInfoDTO.class);
					stagingLoanAccountInfoDTO.setInstallments(sad.getInstallments());
					return commonRepository.getDisbursementDateByLoanAccountId(sad.getLoanAccountId())
							.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Disbursement passbook data not found for loan account : " + sad.getLoanAccountId())))
							.map(disbursementDate -> {
								stagingLoanAccountInfoDTO.setDisbursementDate(disbursementDate);
								stagingLoanAccountInfoDTO.setScheduledInstallmentAmount(sad.getScheduledInstallmentAmount());
								return stagingLoanAccountInfoDTO;
							})
							.doOnError(throwable -> log.error("Error on getting disbursement date for loan account : {} Error : {}", sad.getLoanAccountId(), throwable.getMessage()));
				})
				.doOnNext(s -> log.info("StagingLoanAccountInfoDTO : {}", s));

		return Mono.zip(Mono.just(dto), loanAccountFlux.collectList())
				.map(t -> {
					t.getT1().setLoanAccountList(t.getT2());
					return t.getT1();
				});
	}

	private Mono<MemberInfoDTO> getStagingSavingsAccountListForStagingDataDetailViewByMemberId(MemberInfoDTO dto) {
		Flux<StagingSavingsAccountInfoDTO> savingsAccountInfoFlux = stagingAccountPort
				.getStagingSavingsAccountDataListByMemberId(dto.getMemberId())
				.map(sad -> mapper.map(sad, StagingSavingsAccountInfoDTO.class))
				.filter(savingsAccountInfoDTO -> !(savingsAccountInfoDTO.getSavingsProductType().equals(PRODUCT_TYPE_FDR.getValue()) && savingsAccountInfoDTO.getTargetAmount().compareTo(BigDecimal.ZERO) == 0));
		return Mono.zip(Mono.just(dto), savingsAccountInfoFlux.collectList())
				.map(t -> {
					t.getT1().setSavingsAccountList(t.getT2());
					return t.getT1();
				});
	}


	private Mono<StagingDataDetailViewResponseDTO> getLoanAndSavingsAccountSummeryByProductCode(
			StagingDataDetailViewResponseDTO dto) {
		Flux<LoanAccountSummeryDTO> loanAccountSummeryFlux = stagingAccountPort
				.getLoanAccountSummeryByProductCode(dto.getSamityId());
		Mono<BigDecimal> loanAccountTotalDue = loanAccountSummeryFlux
				.map(LoanAccountSummeryDTO::getTotalDue)
				.reduce(BigDecimal.valueOf(0.00), BigDecimal::add);

		Flux<SavingsAccountSummeryDTO> savingsAccountSummeryFlux = stagingAccountPort
				.getSavingsAccountSummeryByProductCode(dto.getSamityId());

		Mono<BigDecimal> savingsAccountTotalTarget = savingsAccountSummeryFlux
				.map(SavingsAccountSummeryDTO::getTotalTarget)
				.reduce(BigDecimal.valueOf(0.00), BigDecimal::add);

		return Mono
				.zip(Mono.just(dto), loanAccountSummeryFlux.collectList(), loanAccountTotalDue,
						savingsAccountSummeryFlux.collectList(), savingsAccountTotalTarget)
				.map(t -> {
					t.getT1().setLoanAccountSummery(t.getT2());
					t.getT1().setLoanAccountTotalDue(t.getT3());
					t.getT1().setSavingsAccountSummery(t.getT4());
					t.getT1().setSavingsAccountTotalTarget(t.getT5());
					return t.getT1();
				});
	}


	private void saveStagingDataGenerationStatusAndStagingDataWithStagingAccountData(StagingDataGenerationResponseDTO responseDTO, String officeId, String loginId, String processId) {
		processTrackerPort.saveProcessTrackerWithWaitingStatus(responseDTO.getStagingDataGenerationStatus())
				.flatMap(statusDto -> this.buildAndSaveStagingData(statusDto, loginId, statusDto.getBusinessDate()))
				.collectList()
				.flatMap(list -> officeEventUseCase.updateOfficeEvent(responseDTO.getStagingDataGenerationStatus().get(0).getManagementProcessId(), processId, officeId, OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue(), loginId))
				.doOnError(throwable -> log.error("Error on staging data generation: {}", throwable.getMessage()))
				.onErrorMap(throwable -> {
					stagingAccountPort.deleteStagingAccountDataByManagementProcessId(responseDTO.getStagingDataGenerationStatus().get(0).getManagementProcessId())
							.flatMap(s -> stagingDataPort.deleteStagingDataByManagementProcessId(responseDTO.getStagingDataGenerationStatus().get(0).getManagementProcessId()))
							.flatMap(s -> processTrackerPort.deleteStagingProcessTrackerByManagementProcessId(responseDTO.getStagingDataGenerationStatus().get(0).getManagementProcessId()))
							.flatMap(s -> officeEventUseCase.deleteOfficeEventForOffice(responseDTO.getStagingDataGenerationStatus().get(0).getManagementProcessId(), officeId, OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
							.subscribe();
					return throwable;
				})
				.subscribeOn(Schedulers.immediate())
				.doOnSuccess(list -> log.info("Staging data Generation Completed Successfully"))
				.subscribe();
	}

	private Flux<StagingDataGenerationStatusDTO> buildAndSaveStagingData(StagingDataGenerationStatusDTO statusDTO,
			String loginId, LocalDate businessDate) {
		return processTrackerPort.updateProcessTrackerStatusToProcessing(statusDTO)
				.flatMapMany(stagingDataGenerationStatusDTO -> stagingDataPort
						.getStagingDataMemberInfoBySamityId(statusDTO.getSamityId())
						.map(stagingData -> {
							String stagingDataId = UUID.randomUUID().toString();

							stagingData.setManagementProcessId(statusDTO.getManagementProcessId());
							stagingData.setProcessId(statusDTO.getProcessId());
							stagingData.setStagingDataId(stagingDataId);
							stagingData.setSamityId(statusDTO.getSamityId());
							stagingData.setSamityNameEn(statusDTO.getSamityNameEn());
							stagingData.setSamityNameBn(statusDTO.getSamityNameBn());
							stagingData.setSamityDay(statusDTO.getSamityDay());
							stagingData.setTotalMember(statusDTO.getTotalMember());
							stagingData.setFieldOfficerId(statusDTO.getFieldOfficerId());
							stagingData.setFieldOfficerNameEn(statusDTO.getFieldOfficerNameEn());
							stagingData.setFieldOfficerNameBn(statusDTO.getFieldOfficerNameBn());
							stagingData.setCreatedOn(LocalDateTime.now());
							stagingData.setCreatedBy(loginId);

							return stagingData;
						}))
				.collectList()
				.doOnNext(list -> log.debug("Staging Data List: {}", list))
                .flatMapMany(stagingDataPort::save)
				.flatMap(stagingData -> this.buildAndSaveStagingAccountData(statusDTO, stagingData, businessDate));
	}

	private Flux<StagingDataGenerationStatusDTO> buildAndSaveStagingAccountData(
			StagingDataGenerationStatusDTO statusDTO,
			StagingData stagingData, LocalDate businessDate) {
		return Mono.zip(
				this.buildStagingAccountDataListForLoanAccount(stagingData, businessDate),
				this.buildStagingAccountDataListForSavingsAccount(stagingData, businessDate)
						.doOnRequest(l -> log.debug("eta call hoise")))
				.map(tuple -> {
					List<StagingAccountData> stagingAccountDataList = new ArrayList<>();
					stagingAccountDataList.addAll(tuple.getT1());
					stagingAccountDataList.addAll(tuple.getT2());
					stagingAccountDataList.forEach(item -> {
						item.setManagementProcessId(statusDTO.getManagementProcessId());
						item.setProcessId(statusDTO.getProcessId());
					});

					return stagingAccountDataList;
				})
				.flatMapMany(list -> stagingAccountPort.save(list)
						.collectList()
						.then(processTrackerPort.updateProcessTrackerStatusToFinishedBySamity(statusDTO)));
	}

	private Mono<List<StagingAccountData>> buildStagingAccountDataListForLoanAccount(StagingData stagingData,
			LocalDate businessDate) {
		return loanAccountUseCase
				.getLoanAccountListForStagingAccountData(stagingData.getMemberId(), Status.STATUS_ACTIVE.getValue(),
						LoanTypeID.LOAN_TYPE_M.getValue())
				.flatMap(dto1 -> checkIfLoanIsValidForStaging(dto1, businessDate))
				.filter(dto -> dto.getLoanAccountId() != null)
				.flatMap(dto -> this.buildStagingAccountDataForOneLoanAccount(dto, businessDate))
				.collectList()
				.doOnNext(list -> log.debug("Staging Account Data List for loan Account: {}", list));
	}

	private Mono<StagingAccountData> checkIfLoanIsValidForStaging(StagingAccountData dto, LocalDate businessDate) {
		Mono<PassbookEntryDTO> lastPassbookEntry = this
				.getLastPassbookEntryForOneActiveLoanAccount(dto.getLoanAccountId());
		Mono<LoanRepayScheduleDTO> firstLrs = this
				.getFirstLoanRepayScheduleForOneActiveLoanAccount(dto.getLoanAccountId());
		return Mono.zip(Mono.just(dto), lastPassbookEntry, firstLrs)
				.map(tuple -> {
					if (tuple.getT2().getInstallNo() == null
							&& tuple.getT3().getInstallDate().isAfter(businessDate)) {
						return StagingAccountData.builder().build();
					} else {
						return tuple.getT1();
					}
				});
	}

	private Mono<StagingAccountData> buildStagingAccountDataForOneLoanAccount(StagingAccountData dto,
			LocalDate businessDate) {
		Mono<PassbookEntryDTO> lastPassbookEntry = this
				.getLastPassbookEntryForOneActiveLoanAccount(dto.getLoanAccountId());
		Flux<LoanRepayScheduleDTO> loanRepayScheduleList = this
				.getLoanRepayScheduleListForOneActiveLoanAccount(dto.getLoanAccountId());
		return loanRepayScheduleList.hasElements()
				.flatMap(lrs -> {
					Mono<StagingAccountData> stagingAccountData = this.getTotalPrincipalAndServiceCharge(Mono.just(dto),
							loanRepayScheduleList);
					return stagingAccountDataCommands.generateStagingAccountDataForOneActiveLoanAccount(
							stagingAccountData,
							lastPassbookEntry, loanRepayScheduleList, businessDate);
				})
				.switchIfEmpty(Mono.just(dto));
	}

	private Mono<List<StagingAccountData>> buildStagingAccountDataListForSavingsAccount(StagingData stagingData, LocalDate businessDate) {
		return savingsAccountUseCase.getSavingsAccountFluxByMemberId(stagingData.getMemberId())
				.filter(savingsAccountResponseDTO -> !savingsAccountResponseDTO.getStatus()
						.equalsIgnoreCase(Status.STATUS_INACTIVE.getValue()))
				.flatMap(savingsAccount -> {
					StagingAccountData stagingAccountData = mapper.map(savingsAccount, StagingAccountData.class);
					Mono<StagingAccountData> stagingAccountDataMono = Mono.just(stagingAccountData);
					if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_GS.getValue())) {
						stagingAccountData.setTargetAmount(savingsAccount.getGsInstallment());
						stagingAccountData.setEligibleToStage(checkIfGSVSIsEligibleToStage(savingsAccount));
						stagingAccountDataMono = Mono.just(stagingAccountData);
					} else if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_VS.getValue())) {
						stagingAccountData.setTargetAmount(savingsAccount.getVsInstallment());
						stagingAccountData.setEligibleToStage(checkIfGSVSIsEligibleToStage(savingsAccount));
						stagingAccountDataMono = Mono.just(stagingAccountData);
					} else if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_DPS.getValue()) ||
							savingsAccount.getSavingsProductType().equalsIgnoreCase(PRODUCT_TYPE_FDR.getValue())) {

						log.info("DPS FDR Account: {}", savingsAccount.getSavingsAccountId());
						stagingAccountDataMono = buildStagingAccountDataForDPSFDR(savingsAccount, stagingAccountData, businessDate);
					}
					return stagingAccountDataMono;
				})
				.filter(StagingAccountData::isEligibleToStage)
				.flatMap(this::buildStagingAccountDataForOneSavingsAccount)
				.collectList()
				.doOnNext(list -> log.debug("Staging Account Data List for Savings Account: {}", list));
	}

	private boolean checkIfGSVSIsEligibleToStage(SavingsAccountResponseDTO savingsAccount) {
		return !savingsAccount.getStatus().equalsIgnoreCase(Status.STATUS_CLOSED.getValue());
	}

	private Mono<StagingAccountData> buildStagingAccountDataForDPSFDR(SavingsAccountResponseDTO savingsAccount, StagingAccountData stagingAccountData, LocalDate businessDate) {

		String productType = savingsAccount.getSavingsProductType();

		if (productType.equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_DPS.getValue())) {
			return dpsRepaymentScheduleUseCase
					.getDpsRepaymentScheduleBySavingsAccountId(savingsAccount.getSavingsAccountId())
					.filter(dpsRepaymentScheduleDTO -> !dpsRepaymentScheduleDTO.isEmpty())
					.switchIfEmpty(Mono.just(List.of(DpsRepaymentDTO.builder()
							.savingsAccountId(savingsAccount.getSavingsAccountId())
							.repaymentDate(businessDate)
							.status(Status.STATUS_PENDING.getValue())
							.repaymentAmount(savingsAccount.getSavingsAmount()).build())))
					.map(dpsRepaymentScheduleDTO -> {
						stagingAccountData.setTargetAmount(this.getDPSTargetAmount(dpsRepaymentScheduleDTO));
						stagingAccountData.setEligibleToStage(this.checkIfDPSIsEligibleToStage(dpsRepaymentScheduleDTO));
						return stagingAccountData;
					})
					.flatMap(stagingAccountData1 -> dpsRepaymentScheduleUseCase.getCountOfPendingRepaymentScheduleBySavingsAccountId(stagingAccountData1.getSavingsAccountId())
							.map(dpsPendingCount -> {
								stagingAccountData1.setDpsPendingInstallmentNo(dpsPendingCount);
								return stagingAccountData1;
							})
					);
		} else if (productType.equalsIgnoreCase(PRODUCT_TYPE_FDR.getValue())) {
			BigDecimal fdrBalance = savingsAccount.getBalance() == null ? BigDecimal.ZERO : savingsAccount.getBalance();
            if (fdrBalance.compareTo(BigDecimal.ZERO) == 0) {
                stagingAccountData.setTargetAmount(savingsAccount.getSavingsAmount());
				stagingAccountData.setEligibleToStage(true);
            } else {
                stagingAccountData.setTargetAmount(BigDecimal.ZERO);
				stagingAccountData.setEligibleToStage(false);
            }
			return Mono.just(stagingAccountData);
        }


		return Mono.just(stagingAccountData);
	}

	private BigDecimal getDPSTargetAmount(List<DpsRepaymentDTO> dpsRepaymentScheduleDTO) {
		return dpsRepaymentScheduleDTO.get(0).getRepaymentAmount();
	}

	private boolean checkIfDPSIsEligibleToStage(List<DpsRepaymentDTO> dpsRepaymentScheduleDTO) {
		return dpsRepaymentScheduleDTO
				.stream()
				.anyMatch(dpsRepaymentDTO -> dpsRepaymentDTO.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()));
	}

	private Mono<StagingAccountData> buildStagingAccountDataForOneSavingsAccount(
			StagingAccountData stagingAccountData) {
		return passbookUseCase.getLastPassbookEntryBySavingsAccountForStagingData(stagingAccountData.getSavingsAccountId())
				.switchIfEmpty(Mono.just(PassbookResponseDTO.builder().build()))
				.map(passbook -> {
					stagingAccountData.setBalance(passbook.getSavgAcctEndingBalance());
					stagingAccountData.setSavingsAvailableBalance(passbook.getSavingsAvailableBalance() != null ? passbook.getSavingsAvailableBalance() : BigDecimal.ZERO);
					stagingAccountData.setTotalDeposit(passbook.getTotalDepositAmount() != null ? passbook.getTotalDepositAmount() : BigDecimal.ZERO);
					stagingAccountData.setTotalWithdraw(passbook.getTotalWithdrawAmount() != null ? passbook.getTotalWithdrawAmount() : BigDecimal.ZERO);
					stagingAccountData.setAccruedInterestAmount(passbook.getTotalAccruedInterDeposit() != null ? passbook.getTotalAccruedInterDeposit() : BigDecimal.ZERO);

					return stagingAccountData;
				})
				.flatMap(data -> passbookTransactionGateway
						.getLastPassbookEntryForDepositAmountWithSavingsAccount(data.getSavingsAccountId())
						.map(passbook -> {
							data.setLastDepositAmount(passbook.getLastDepositAmount());
							data.setLastDepositDate(passbook.getLastDepositDate());
							data.setLastDepositType(passbook.getLastDepositType());
							return data;
						}))
				.flatMap(data -> passbookTransactionGateway
						.getLastPassbookEntryForWithdrawAmountWithSavingsAccount(data.getSavingsAccountId())
						.map(passbook -> {
							data.setLastWithdrawAmount(passbook.getLastWithdrawAmount());
							data.setLastWithdrawDate(passbook.getLastWithdrawDate());
							data.setLastWithdrawType(passbook.getLastWithdrawType());
							return data;
						}));
	}

	private Mono<PassbookEntryDTO> getLastPassbookEntryForOneActiveLoanAccount(String loanAccountId) {
		return this.passbookUseCase.getLastPassbookEntry(loanAccountId)
				.map(p -> mapper.map(p, PassbookEntryDTO.class));
	}

	private Flux<LoanRepayScheduleDTO> getLoanRepayScheduleListForOneActiveLoanAccount(String loanAccountId) {
		return loanRepaymentScheduleUseCase.getRepaymentScheduleListByLoanAccountId(loanAccountId)
				.map(lrs -> mapper.map(lrs, LoanRepayScheduleDTO.class));
	}

	private Mono<LoanRepayScheduleDTO> getFirstLoanRepayScheduleForOneActiveLoanAccount(String loanAccountId) {
		return loanRepaymentScheduleUseCase.getFirstRepaymentScheduleByLoanAccountId(loanAccountId)
				.map(lrs -> mapper.map(lrs, LoanRepayScheduleDTO.class));
	}

	private Mono<StagingAccountData> getTotalPrincipalAndServiceCharge(Mono<StagingAccountData> stagingAccountData,
			Flux<LoanRepayScheduleDTO> loanRepayScheduleList) {
		Mono<BigDecimal> totalPrincipal = loanRepayScheduleList
				.map(LoanRepayScheduleDTO::getPrincipal)
				.reduce(BigDecimal.valueOf(0.00), BigDecimal::add);

		Mono<BigDecimal> totalServiceCharge = loanRepayScheduleList
				.map(LoanRepayScheduleDTO::getServiceCharge)
				.reduce(BigDecimal.valueOf(0.00), BigDecimal::add);

		return Mono.zip(stagingAccountData, totalPrincipal, totalServiceCharge)
				.map(t -> {
					t.getT1().setLoanAmount(t.getT2());
					t.getT1().setServiceCharge(t.getT3());
					return t.getT1();
				});
	}

	@Override
	public Mono<StagingDataSavingsAccountDetailDTO> getStagingDataSavingsAccountDetailBySavingsAccountId(
			String savingsAccountId) {
		return stagingAccountPort
				.getStagingLoanOrSavingsAccountByAccountId(savingsAccountId)
				.map(stagingAccountData -> mapper.map(stagingAccountData, StagingDataSavingsAccountDetailDTO.class));
	}

	@Override
	public Flux<String> findSamityIdListByFieldOfficerIdList(List<String> fieldOfficerIdList, Integer limit,
			Integer offset) {
		return stagingDataPort.findSamityIdListByFieldOfficerIdList(fieldOfficerIdList, limit, offset);
	}

	@Override
	public Mono<Integer> getTotalCountByFieldOfficerList(List<String> fieldOfficerIdList) {
		return stagingDataPort.getTotalCountOfStagingDataByFieldOfficerList(fieldOfficerIdList);
	}

	@Override
	public Flux<StagingData> getStagingDataBySamity(String samityId) {
		return stagingDataPort.getStagingDataBySamity(samityId);
	}

	@Override
	public Flux<String> getSamityIdListByFieldOfficer(String fieldOfficerId) {
		return stagingDataPort.getSamityIdListByFieldOfficer(fieldOfficerId);
	}

	// Staging Data: Process Management v2
	@Override
	public Mono<StagingDataStatusByOfficeResponseDTO> gridViewOfStagingDataStatusByOffice(StagingDataRequestDTO requestDTO) {
		return this.buildGridViewOfStagingDataStatusByOffice(requestDTO)
				.doOnError(throwable -> log.error("Error in Staging Data Grid View Response: {}", throwable.getMessage()));
	}

	@Override
	public Mono<StagingDataStatusByOfficeResponseDTO> gridViewOfStagingDataStatusByOfficeFilteredByFieldOfficer(StagingDataRequestDTO requestDTO) {
		return this.buildGridViewOfStagingDataStatusByOffice(requestDTO)
				.map(responseDTO -> {
					if(!responseDTO.getData().isEmpty()){
						List<StagingDataSamityStatusForOfficeDTO> filteredSamityList = responseDTO.getData().stream()
								.filter(samityStatusDTO -> samityStatusDTO.getFieldOfficerId().equals(requestDTO.getFieldOfficerId()))
								.toList();
						responseDTO.setData(filteredSamityList);
						responseDTO.setTotalCount(filteredSamityList.size());
					}
					return responseDTO;
				})
				.doOnNext(responseDTO -> log.info("Staging Data Grid View Response Filtered By Field Officer: {}", responseDTO))
				.doOnError(throwable -> log.error("Error in Staging Data Grid View Response Filtered By Field Officer: {}", throwable.getMessage()));
	}

	public Mono<StagingDataStatusByOfficeResponseDTO> buildGridViewOfStagingDataStatusByOffice(StagingDataRequestDTO requestDTO){
		final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnNext(managementProcess::set)
				.map(this::buildStagingDataGenerationStatusForOfficeFromManagementProcess)
				.flatMap(responseDTO -> this.getStagingDataGenerationSamityData(managementProcess.get().getManagementProcessId(), responseDTO))
				.flatMap(responseDTO -> this.setBtnStatusOfStagingDataGenerationForOffice(managementProcess.get().getManagementProcessId(), responseDTO))
				.flatMap(responseDTO -> this.setBtnDeleteStatusForStagingDataGenerationResponse(managementProcess.get().getManagementProcessId(), responseDTO));
	}
	private StagingDataStatusByOfficeResponseDTO buildStagingDataGenerationStatusForOfficeFromManagementProcess(ManagementProcessTracker managementProcessTracker){
		return StagingDataStatusByOfficeResponseDTO.builder()
				.mfiId(managementProcessTracker.getMfiId())
				.officeId(managementProcessTracker.getOfficeId())
				.officeNameEn(managementProcessTracker.getOfficeNameEn())
				.officeNameBn(managementProcessTracker.getOfficeNameBn())
				.businessDate(managementProcessTracker.getBusinessDate())
				.businessDay(managementProcessTracker.getBusinessDay())
				.build();
	}

	private Mono<StagingDataStatusByOfficeResponseDTO> setBtnStatusOfStagingDataGenerationForOffice(String managementProcessId, StagingDataStatusByOfficeResponseDTO responseDTO){
		return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, responseDTO.getOfficeId())
				.map(OfficeEventTracker::getOfficeEvent)
				.collectList()
				.map(officeEventList -> {
					if(officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))){
						responseDTO.setBtnStagingDataGenerateEnabled("No");
						responseDTO.setBtnStartProcessEnabled("No");
						responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
						responseDTO.setUserMessage("Staging Data Generation is Completed For Office");
					} else {
						if(responseDTO.getData().isEmpty()){
							responseDTO.setBtnStagingDataGenerateEnabled("Yes");
							responseDTO.setBtnStartProcessEnabled("Yes");
							responseDTO.setStatus(Status.STATUS_PENDING.getValue());
							responseDTO.setUserMessage("Staging Data is Not Generated For Office");
						} else if(responseDTO.getData().stream().map(StagingDataSamityStatusForOfficeDTO::getStatus).anyMatch(samityStatus -> samityStatus.equals(Status.STATUS_WAITING.getValue()) || samityStatus.equals(Status.STATUS_PROCESSING.getValue()))){
							responseDTO.setBtnStagingDataGenerateEnabled("No");
							responseDTO.setBtnStartProcessEnabled("No");
							responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
							responseDTO.setUserMessage("Staging Data Generation Process Is Running For Office");
						}
						else {
							responseDTO.setBtnStagingDataGenerateEnabled("No");
							responseDTO.setBtnStartProcessEnabled("No");
							responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
							responseDTO.setUserMessage("Staging Data Is Generated For Office");
						}
					}
					if(responseDTO.getBtnStartProcessEnabled().equals("Yes") || responseDTO.getBtnStagingDataGenerateEnabled().equals("Yes")){
						responseDTO.setBtnRefreshEnabled("No");
					} else {
						responseDTO.setBtnRefreshEnabled("Yes");
					}
					return responseDTO;
				});
	}

	private Mono<StagingDataStatusByOfficeResponseDTO> setBtnDeleteStatusForStagingDataGenerationResponse(String managementProcessId, StagingDataStatusByOfficeResponseDTO responseDTO){
		if(!responseDTO.getStatus().equals(Status.STATUS_FINISHED.getValue())){
			responseDTO.setBtnDeleteEnabled("No");
			return Mono.just(responseDTO);
		}
		return samityEventUseCase.getAllSamityEventsForOffice(managementProcessId, responseDTO.getOfficeId())
				.map(samityEventTrackerList -> samityEventTrackerList.stream().filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())).toList())
				.map(samityEventTrackerList -> {
					if (!samityEventTrackerList.isEmpty()){
						responseDTO.setBtnDeleteEnabled("No");
					} else if(responseDTO.getData().stream().map(StagingDataSamityStatusForOfficeDTO::getIsDownloaded).anyMatch(isDownloaded -> isDownloaded.equals("Yes"))){
						responseDTO.setBtnDeleteEnabled("No");
					} else {
						if(responseDTO.getBtnStartProcessEnabled().equals("Yes")){
							responseDTO.setBtnDeleteEnabled("No");
						} else {
							responseDTO.setBtnDeleteEnabled("Yes");
						}
					}
					return responseDTO;
				});
	}

	private Mono<StagingDataStatusByOfficeResponseDTO> getStagingDataGenerationSamityData(String managementProcessId, StagingDataStatusByOfficeResponseDTO responseDTO) {
		return processTrackerPort.getStagingProcessEntityByOffice(managementProcessId, responseDTO.getOfficeId())
				.map(processTrackerEntity -> gson.fromJson(processTrackerEntity.toString(), StagingDataSamityStatusForOfficeDTO.class))
				.doOnNext(stagingDataSamityResponse -> {
					if(stagingDataSamityResponse.getSamityDay().equals(responseDTO.getBusinessDay())){
						stagingDataSamityResponse.setSamityType("Regular");
					} else {
						stagingDataSamityResponse.setSamityType("Special");
					}
				})
				.flatMap(stagingDataSamityResponse -> this.setBtnStatusForSamityStagingDataGenerationForOffice(managementProcessId, stagingDataSamityResponse))
				.sort(Comparator.comparing(StagingDataSamityStatusForOfficeDTO::getSamityId))
				.collectList()
				.map(samityList -> {
					responseDTO.setData(samityList);
					responseDTO.setTotalCount(samityList.size());
					return responseDTO;
				});
	}

	@Override
	public Mono<StagingDataStatusByFieldOfficerResponseDTO> gridViewOfStagingDataStatusByFieldOfficer(StagingDataRequestDTO requestDTO) {
		return commonRepository.getSamityIdListByFieldOfficerId(requestDTO.getEmployeeId())
				.collectList()
				.doOnNext(stringList -> log.info("Staging Data: Samity Id List for Field Officer Id {} is: {}",
						requestDTO.getEmployeeId(), stringList))
				.flatMap(stringList -> commonRepository.getOfficeIdOfAFieldOfficer(requestDTO.getEmployeeId())
						.map(officeId -> {
							requestDTO.setOfficeId(officeId);
							return Tuples.of(requestDTO, stringList);
						}))
				.flatMap(this::buildStagingDataGenerationStatusResponse)
				.map(this::buildFieldOfficerStagingDataResponse)
				.doOnNext(responseDTO -> log.info("Staging Data Status Response DTO: {}", responseDTO));
	}

	@Override
	public Mono<StagingDataStatusByOfficeResponseDTO> generateStagingDataByOffice(StagingDataRequestDTO requestDTO) {
		final String stagingProcessId = UUID.randomUUID().toString();
		AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
		return validationUseCase.validateStagingDataGenerationRequestForOffice(requestDTO.getOfficeId())
				.map(managementProcessTracker -> {
					managementProcess.set(managementProcessTracker);
					return managementProcessTracker;
				})
				.map(managementProcessTracker -> StagingDataStatusByOfficeResponseDTO.builder()
						.btnStagingDataGenerateEnabled("No")
						.btnStartProcessEnabled("No")
						.btnRefreshEnabled("Yes")
						.btnDeleteEnabled("No")
						.officeId(requestDTO.getOfficeId())
						.officeNameEn(managementProcessTracker.getOfficeNameEn())
						.officeNameEn(managementProcessTracker.getOfficeNameBn())
						.businessDate(managementProcessTracker.getBusinessDate())
						.businessDay(managementProcessTracker.getBusinessDay())
						.status(Status.STATUS_PROCESSING.getValue())
						.userMessage("Staging Data Generation Process is Started for Office")
						.build())
				.flatMap(responseDTO -> commonRepository.getSamityIdListByOfficeId(responseDTO.getOfficeId())
						.collectList()
						.doOnNext(samityIdList -> log.info("Staging Process Tracker Samity Id List; {}", samityIdList))
						.map(samityIdList -> Tuples.of(managementProcess.get(), responseDTO, samityIdList)))
				.flatMap(tuple -> this.gridListOfStagingDataSamityStatusResponseForNotStagedOffice(tuple.getT3())
						.map(samityStatusList -> {
							samityStatusList.forEach(item -> {
								item.setStatus(Status.STATUS_WAITING.getValue());
								item.setBtnInvalidateEnabled("No");
								item.setBtnRegenerateEnabled("No");
							});
							tuple.getT2().setData(samityStatusList);
							tuple.getT2().setTotalCount(samityStatusList.size());
							return Tuples.of(tuple.getT1(), tuple.getT2());
						}))
            .flatMap(tuple -> Mono.deferContextual(contextView -> {
                return Mono.fromRunnable(() -> {
                    Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    this.buildAndSaveStagingDataForOffice(tuple.getT1(), tuple.getT2(), requestDTO, stagingProcessId)
                        .contextWrite(context)
                        .subscribeOn(Schedulers.immediate())
                        .subscribe();
                });
            }).thenReturn(tuple.getT2()))
				.doOnError(throwable -> log.error("Failed to generate Staging Data: {}", throwable.getMessage()));
	}

	@Override
	public Mono<InvalidateSamityResponseDTO> invalidateStagingDataBySamityList(StagingDataRequestDTO requestDTO) {
		return validationUseCase.validateSamityStagingDataInvalidationRequestForSamityList(requestDTO.getOfficeId(), requestDTO.getSamityIdList())
				.then(managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId()))
				.flatMap(managementProcessTracker -> processTrackerPort.getStagingProcessEntityListBySamityIdList(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityIdList())
						.collectList())
				.flatMap(trackerEntityList -> this.createStagingDataHistoryAndDeleteStagingDataForSamityList(trackerEntityList, requestDTO.getOfficeId(), requestDTO.getLoginId(), requestDTO.getRemarks()))
				.as(rxtx::transactional)
				.doOnSuccess(response -> log.info("Samity Invalidation Request Successful for Samity List: {}", requestDTO.getSamityIdList()))
				.doOnError(throwable -> log.error("Error in Samity Invalidation Request: {}", throwable.getMessage()));
	}

	private Mono<InvalidateSamityResponseDTO> createStagingDataHistoryAndDeleteStagingDataForSamityList(List<StagingProcessTrackerEntity> trackerEntityList, String officeId, String loginId, String remarks) {
		return Flux.fromIterable(trackerEntityList)
				.doOnNext(stagingProcessTrackerEntity -> log.info("Staging Data Deletion Process Started For Samity: {}", stagingProcessTrackerEntity.getSamityId()))
				.flatMap(trackerEntity -> this.createStagingDataHistoryAndDeleteStagingData(trackerEntity, loginId, remarks))
				.collectList()
				.map(memberIdList -> InvalidateSamityResponseDTO.builder()
						.userMessage("Samity Staging Data Invalidation Process is Successful")
						.build());
	}


	private Mono<List<String>> validateOfficeForStagingDataInvalidationAndRegeneration(String managementProcessId, String officeId){
		return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, officeId)
				.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
				.map(OfficeEventTracker::getOfficeEvent)
				.collectList()
				.filter(officeEventList -> officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Already Completed And Samity Staging Data Cannot Be Invalidated")))
				.filter(officeEventList -> !officeEventList.isEmpty() && officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Generation is Not Completed For Office And Samity Staging Data Cannot Be Invalidated")));
	}

	@Override
	public Mono<StagingDataStatusByOfficeResponseDTO> regenerateStagingDataBySamityList(StagingDataRequestDTO requestDTO) {
		AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnNext(managementProcess::set)
				.flatMap(managementProcessTracker -> this.validateSamityListForStagingDataRegeneration(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityIdList()))
				.flatMap(stagingProcessTrackerEntities -> Mono.deferContextual(contextView -> {
					return Mono.fromRunnable(() -> {
						Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
						this.updateAndRegenerateStagingDataForSamityList(stagingProcessTrackerEntities, managementProcess.get(), requestDTO.getLoginId(), requestDTO.getSamityIdList())
							.contextWrite(context)
							.subscribeOn(Schedulers.immediate())
							.subscribe();
					})
							.thenReturn(stagingProcessTrackerEntities);
				}))
				.map(stagingProcessTrackerEntityList -> StagingDataStatusByOfficeResponseDTO.builder()
                        .officeId(managementProcess.get().getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.get().getBusinessDay())
                        .userMessage("Staging Data Regeneration Process Started For Samity: " + requestDTO.getSamityIdList())
                        .totalCount(requestDTO.getSamityIdList().size())
                        .build())
				.doOnSuccess(response -> log.info("Staging Data Regeneration Response: {}", response.getUserMessage()))
				.doOnError(throwable -> log.error("Error in Staging Data Regeneration Request: {}", throwable.getMessage()));
	}

	private Mono<Void> updateAndRegenerateStagingDataForSamityList(List<StagingProcessTrackerEntity> stagingProcessTrackerEntityList, ManagementProcessTracker managementProcessTracker, String loginId, List<String> samityIdList) {
		return Flux.fromIterable(stagingProcessTrackerEntityList)
				.flatMap(stagingProcessTrackerEntity -> commonRepository.getSamityBySamityId(stagingProcessTrackerEntity.getSamityId())
						.flatMap(samity -> commonRepository.getFieldOfficerByFieldOfficerId(samity.getFieldOfficerId())
								.map(fieldOfficer -> {
									stagingProcessTrackerEntity.setFieldOfficerId(samity.getFieldOfficerId());
									stagingProcessTrackerEntity.setFieldOfficerNameEn(fieldOfficer.getFieldOfficerNameEn());
									stagingProcessTrackerEntity.setFieldOfficerNameBn(fieldOfficer.getFieldOfficerNameBn());
									return stagingProcessTrackerEntity;
								})))
				.flatMap(trackerEntity -> processTrackerPort.updateProcessTrackerEntityForRegeneration(trackerEntity, loginId))
				.flatMap(updatedTrackerEntity -> this.buildAndSaveStagingDataForSamity(updatedTrackerEntity.getSamityId(), loginId, managementProcessTracker.getMfiId(), managementProcessTracker.getBusinessDate()))
				.collectList()
				.subscribeOn(Schedulers.immediate())
				.doOnSuccess(response -> log.info("Staging Data Regeneration Process Completed For Samity List: {}", samityIdList))
				.doOnError(throwable -> log.error("Error in Staging Data Regeneration Process: {}", throwable.getMessage()))
				.then();
	}

	private Mono<List<StagingProcessTrackerEntity>> validateSamityListForStagingDataRegeneration(String managementProcessId, String officeId, List<String> samityIdList) {
		return this.validateOfficeForStagingDataInvalidationAndRegeneration(managementProcessId, officeId)
				.flatMapIterable(officeEventList -> samityIdList)
				.flatMap(samityId -> this.validateSamityForStagingDataRegeneration(managementProcessId, samityId))
				.collectList();
	}

	private Mono<StagingProcessTrackerEntity> validateSamityForStagingDataRegeneration(String managementProcessId, String samityId) {
		return Mono.just(samityId)
				.doOnNext(id -> log.info("Samity Staging Data Regeneration Request Processing For Samity Id: {}", id))
				.flatMap(id -> processTrackerPort.getStagingProcessEntityForSamity(managementProcessId, samityId))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Process Not Found. Regeneration Request Failed")))
				.filter(trackerEntity -> !trackerEntity.getStatus().equals(Status.STATUS_REGENERATED.getValue()) || !trackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Already Regenerated for Samity")))
				.filter(trackerEntity -> !trackerEntity.getIsDownloaded().equals("Yes"))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Already Downloaded For Samity. Please Delete Staging Data First.")))
				.filter(trackerEntity -> trackerEntity.getStatus().equals(Status.STATUS_FAILED.getValue()) || trackerEntity.getStatus().equals(Status.STATUS_INVALIDATED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data Is Not Invalidated. Staging Data Cannot be Regenerated")))
				.flatMap(stagingProcessTrackerEntity -> samityEventUseCase.getAllSamityEventsForSamity(managementProcessId, stagingProcessTrackerEntity.getSamityId())
						.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
						.map(SamityEventTracker::getSamityEvent)
						.collectList()
						.filter(List::isEmpty)
						.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event Already Found, Samity Staging Data Cannot Be Regenerated")))
						.map(samityEventList -> stagingProcessTrackerEntity))
				.doOnNext(stagingProcessTrackerEntity -> log.info("Staging Data Regeneration Request is Validated For Samity: {}", samityId));
	}

    @Override
    public Mono<StagingDataStatusByOfficeResponseDTO> deleteStagingDataByOffice(StagingDataRequestDTO requestDTO) {
        if (requestDTO.getIsScheduledRequest() != null && requestDTO.getIsScheduledRequest())
            return deleteStagingDataByOfficeForScheduler(requestDTO);
        else
            return deleteStagingDataWithEditHistoryByOffice(requestDTO);
    }

	private Mono<StagingDataStatusByOfficeResponseDTO> deleteStagingDataWithEditHistoryByOffice(StagingDataRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcessTracker -> officeEventUseCase.getOfficeEventByStatusForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())
						.onErrorMap(error -> new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Generation is not Completed For Office"))
						.map(officeEventTracker -> Tuples.of(managementProcessTracker.getManagementProcessId(), officeEventTracker.getOfficeEventTrackerId())))
				.doOnNext(tuple -> log.info("Office Id: {}, Management Process ID: {}, Staging Process ID: {}", requestDTO.getOfficeId(), tuple.getT1(), tuple.getT2()))
				.flatMap(tuple -> samityEventUseCase.getAllSamityEventsForOffice(tuple.getT1(), requestDTO.getOfficeId())
						.flatMap(samityEventList -> {
							if (!samityEventList.isEmpty()) {
								return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Cannot be Deleted, Samity Events Found"));
							}
							return Mono.just(tuple);
						}))
				.flatMap(tuple -> this.updateAndDeleteStagingDataTablesForOffice(tuple.getT1(), tuple.getT2(), requestDTO))
				.flatMap(tuple -> officeEventUseCase.deleteOfficeEventForOffice(tuple.getT1(), requestDTO.getOfficeId(), OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
				.as(rxtx::transactional)
				.then(Mono.just(StagingDataStatusByOfficeResponseDTO.builder()
						.userMessage("Staging Data is Successfully Deleted For Office")
						.build()))
				.doOnSuccess(responseDTO -> log.info("Staging Data Deletion For Office Response: {}", responseDTO.getUserMessage()))
				.doOnError(throwable -> log.error("Error in Staging Data Deletion Process: {}", throwable.getMessage()));
	}

    private Mono<StagingDataStatusByOfficeResponseDTO> deleteStagingDataByOfficeForScheduler(StagingDataRequestDTO requestDTO) {
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .map(ManagementProcessTracker::getManagementProcessId)
                .doOnNext(managementProcessId -> log.info("Office Id: {}, Management Process ID: {}", requestDTO.getOfficeId(), managementProcessId))
                .flatMap(managementProcessId -> samityEventUseCase.getAllSamityEventsForOffice(managementProcessId, requestDTO.getOfficeId())
                    .flatMap(samityEventList -> {
                        if (!samityEventList.isEmpty()) {
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Cannot be Deleted, Samity Events Found"));
                        }
                        return Mono.just(managementProcessId);
                    }))
                .flatMap(this::deleteStagingDataTablesForOffice)
                .as(rxtx::transactional)
                .then(Mono.just(StagingDataStatusByOfficeResponseDTO.builder()
                    .userMessage("Staging Data is Successfully Deleted For Office")
                    .build()))
                .doOnSuccess(responseDTO -> log.info("Staging Data Deletion For Office Response: {}", responseDTO.getUserMessage()))
                .doOnError(throwable -> log.error("Error in Staging Data Deletion Process: {}", throwable.getMessage()));
    }

	@Override
	public Mono<List<StagingProcessTrackerEntity>> getStagingProcessTrackerListBySamityIdList(String managementProcessId, List<String> samityIdList) {
		return processTrackerPort.getStagingProcessEntityListBySamityIdList(managementProcessId, samityIdList)
				.collectList();
	}

	@Override
	public Mono<List<String>> getRegularSamityIdListByOfficeIdAndSamityDay(String managementProcessId, String officeId,
			String businessDay) {
		return processTrackerPort.getStagingProcessEntityListForOffice(managementProcessId, officeId)
				.map(entityList -> entityList.stream()
						.filter(entity -> entity.getSamityDay().equals(businessDay))
						.map(StagingProcessTrackerEntity::getSamityId)
						.toList());
	}

	@Override
	public Mono<List<String>> getRegularSamityIdListByFieldOfficerIdAndSamityDay(String managementProcessId,
			String fieldOfficerId, String businessDay) {
		return processTrackerPort.getStagingProcessEntityListForFieldOfficer(managementProcessId, fieldOfficerId)
				.map(entityList -> entityList.stream()
						.filter(entity -> entity.getSamityDay().equals(businessDay))
						.map(StagingProcessTrackerEntity::getSamityId)
						.toList());
	}

	@Override
	public Mono<List<String>> getSpecialSamityIdListByOfficeIdAndSamityDay(String managementProcessId, String officeId,
			String businessDay) {
		return processTrackerPort.getStagingProcessEntityListForOffice(managementProcessId, officeId)
				.map(entityList -> entityList.stream()
						.filter(entity -> !entity.getSamityDay().equals(businessDay))
						.map(StagingProcessTrackerEntity::getSamityId)
						.toList());
	}

	@Override
	public Mono<List<String>> getSpecialSamityIdListByFieldOfficerIdAndSamityDay(String managementProcessId,
			String fieldOfficerId, String businessDay) {
		return processTrackerPort.getStagingProcessEntityListForFieldOfficer(managementProcessId, fieldOfficerId)
				.map(entityList -> entityList.stream()
						.filter(entity -> !entity.getSamityDay().equals(businessDay))
						.map(StagingProcessTrackerEntity::getSamityId)
						.toList());
	}

	@Override
	public Mono<StagingAccountData> getStagingAccountDataBySavingsAccountId(String savingsAccountId) {
		return stagingAccountPort.getStagingAccountDataBySavingsAccountId(savingsAccountId);
	}

	@Override
	public Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String loanAccountId) {
		return stagingAccountPort.getLoanAccountDataByLoanAccountId(loanAccountId)
				.doOnRequest(s -> log.info("Requesting Staging Account Data By Loan Account Id: {}", loanAccountId))
				.doOnNext(stagingLoanAccount -> log.info("Staging Loan Account Data: {}", stagingLoanAccount));
	}

	@Override
	public Mono<StagingData> getStagingDataByStagingDataId(String stagingDataId) {
		return stagingDataPort.getStagingDataByStagingDataId(stagingDataId);
	}

	@Override
	public Mono<SamityListResponseDTO> getStagedSamityListByFieldOfficerId(StagingDataRequestDTO stagingDataRequestDTO) {
		return stagingDataPort.getStagingDataByFieldOfficer(stagingDataRequestDTO.getFieldOfficerId())
				.map(stagingData -> Samity
						.builder()
						.samityId(stagingData.getSamityId())
						.samityDay(stagingData.getSamityDay())
						.samityNameEn(stagingData.getSamityNameEn())
						.samityNameBn(stagingData.getSamityNameBn())
						.build())
				.collectList()
				.map(samityList -> SamityListResponseDTO
						.builder()
						.fieldOfficerId(stagingDataRequestDTO.getFieldOfficerId())
						.data(samityList)
						.count(samityList.size())
						.userMessage("Samity-Id List by Field Officer Fetched Successfully.")
						.build())
				.doOnError(throwable -> log.error("Error happened while fetching samity-id list by field officer: {}", throwable.getMessage()));
	}

	@Override
	public Mono<String> resetStagingProcessTrackerEntriesByOfficeId(String officeId) {
		return managementProcessUseCase.getLastManagementProcessForOffice(officeId)
				.flatMap(managementProcessTracker -> officeEventUseCase.getAllOfficeEventsForManagementProcessId(managementProcessTracker.getManagementProcessId())
						.flatMapMany(Flux::fromIterable)
						.map(OfficeEventTracker::getOfficeEvent)
						.collectList()
						.flatMap(officeEventTrackerList -> officeEventTrackerList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())
										? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Generation is Completed For Office. Cannot be Reset."))
										: Mono.just(managementProcessTracker)))
				.flatMap(managementProcessTracker -> processTrackerPort
						.getAllStagingProcessTrackerEntityByManagementProcessId(managementProcessTracker.getManagementProcessId())
						.flatMap(stagingProcessTrackerEntities -> {
							boolean allEntriesWithStatusFinished = stagingProcessTrackerEntities
									.stream()
									.allMatch(stagingProcessTrackerEntity -> stagingProcessTrackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()));
							return allEntriesWithStatusFinished
									? Mono.just("Staging process finished successfully. Cannot be Reset.")
									: processTrackerPort.resetStagingProcessTrackerEntriesByManagementProcessId(managementProcessTracker.getManagementProcessId());
						}));
	}

	@Override
	public Mono<StagingData> getStagingDataByMemberId(String memberId) {
		return stagingDataPort.getStagingDataByMemberId(memberId);
	}

	@Override
	public Mono<List<StagingProcessTrackerEntity>> getStagingProcessEntityForFieldOfficer(String managementProcessId,
																			  String fieldOfficerId) {
		return processTrackerPort.getStagingProcessEntityListForFieldOfficer(managementProcessId, fieldOfficerId);
	}

	@Override
	public Mono<StagingProcessTrackerEntity> getStagingProcessEntityForSamity(String managementProcessId,
			String samityId) {
		return processTrackerPort.getStagingProcessEntityForSamity(managementProcessId, samityId);
	}

	@Override
	public Flux<StagingProcessTrackerEntity> getStagingProcessEntityByOffice(String managementProcessId,
			String officeId) {
		return processTrackerPort.getStagingProcessEntityByOffice(managementProcessId, officeId);
	}

	@Override
	public Flux<StagingData> getAllStagingDataBySamity(String managementProcessId, String samityId) {
		return stagingDataPort.getAllStagingDataBySamity(managementProcessId, samityId);
	}

	@Override
	public Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(String managementProcessId,
			List<String> memberIdList) {
		return stagingAccountPort.getAllStagingAccountDataByMemberIdList(managementProcessId, memberIdList);
	}

	@Override
	public Flux<StagingAccountData> getStagingAccountDataListByMemberId(String memberId) {
		return stagingAccountPort.getStagingAccountDataListByMemberId(memberId)
				.flatMap(stagingAccountData -> {
					if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
						return commonRepository.getDisbursementDateByLoanAccountId(stagingAccountData.getLoanAccountId())
								.map(disbursementDate -> {
									stagingAccountData.setDisbursementDate(disbursementDate);
									return stagingAccountData;
								});
					}
					else
						return Mono.just(stagingAccountData);
				})
				.doOnRequest(s -> log.info("Requesting Staging Account Data List By Member Id: {}", memberId))
				;
	}

	@Override
	public Flux<StagingAccountData> getStagingAccountDataBySavingsAccountIdList(List<String> savingsAccountIdList) {
		return stagingAccountPort.getStagingAccountDataBySavingsAccountIdList(savingsAccountIdList);
	}

	@Override
	public Mono<List<StagingAccountData>> getAllStagingAccountDataBySamityIdList(List<String> samityIdList) {
		return stagingDataPort.getMemberIdListFromSatgingDataBySamityIdList(samityIdList)
				.flatMap(memberIDList -> stagingAccountPort.getAllStagingAccountDataByMemberIdList(memberIDList)
						.collectList());
	}

	@Override
	public Flux<String> getSamityIdListByOfficeId(String managementProcessId, String officeId) {
		return processTrackerPort.getStagingProcessEntityByOffice(managementProcessId, officeId)
				.map(StagingProcessTrackerEntity::getSamityId)
				.sort(Comparator.comparing(String::toString));
	}

	@Override
	public Mono<StagingDataDownloadByFieldOfficerResponseDTO> downloadStagingDataByFieldOfficer(StagingDataRequestDTO requestDTO) {
		AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.doOnNext(managementProcess::set)
				.flatMap(managementProcessTracker -> this.validateStagingDataDownloadForFieldOfficer(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getFieldOfficerId()))
				.flatMap(managementProcessId -> this.downloadStagingDataAndUpdateProcessTrackerForDownloadByFieldOfficer(managementProcessId, requestDTO.getFieldOfficerId(), requestDTO.getLoginId()))
				.map(samityResponseList -> StagingDataDownloadByFieldOfficerResponseDTO.builder()
						.officeId(requestDTO.getOfficeId())
						.officeNameEn(managementProcess.get().getOfficeNameEn())
						.officeNameBn(managementProcess.get().getOfficeNameBn())
						.businessDate(managementProcess.get().getBusinessDate())
						.businessDay(managementProcess.get().getBusinessDay())
						.fieldOfficerId(requestDTO.getFieldOfficerId())
						.fieldOfficerNameEn(!samityResponseList.isEmpty() ? samityResponseList.get(0).getFieldOfficerNameEn() : null)
						.fieldOfficerNameBn(!samityResponseList.isEmpty() ? samityResponseList.get(0).getFieldOfficerNameBn(): null)
						.samityList(samityResponseList)
						.totalCount(samityResponseList.size())
						.build())
				.map(responseDTO -> {
					responseDTO.getSamityList().forEach(samityResponse -> {
						samityResponse.setCollectionType(samityResponse.getSamityDay().equalsIgnoreCase(responseDTO.getBusinessDay()) ? "Regular" : "Special");
					});
					return responseDTO;
				})
				.doOnSuccess(response -> log.info("Staging Data Download Response for Field Officer: {}", response))
				.doOnError(throwable -> log.error("Error in Staging Data Download Process Response for Field Officer: {}", throwable.getMessage()));
	}

	private Mono<List<StagingDataDetailViewResponseDTO>> downloadStagingDataAndUpdateProcessTrackerForDownloadByFieldOfficer(String managementProcessId, String fieldOfficerId, String loginId) {
		return processTrackerPort.UpdateStagingProcessEntityListForDownloadByFieldOfficer(managementProcessId, fieldOfficerId, loginId)
				.flatMapIterable(stagingProcessTrackerEntityList -> stagingProcessTrackerEntityList.stream().map(StagingProcessTrackerEntity::getSamityId).toList())
				.flatMap(samityId -> this.getStagingDataDetailViewResponseBySamityId(StagingDataRequestDTO.builder()
						.fieldOfficerId(fieldOfficerId)
						.samityId(samityId)
						.loginId(loginId)
						.build()))
				.collectList();
	}

	private Mono<String> validateStagingDataDownloadForFieldOfficer(String managementProcessId, String officeId, String fieldOfficerId) {
		return this.validateOfficeEventsForDownloadedStagingDataDeletion(managementProcessId, officeId)
				.flatMap(aBoolean -> this.validateSamityListForStagingDataDownloadByFieldOfficer(managementProcessId, fieldOfficerId))
				.doOnNext(samityIdList -> log.info("Samity Staging Data Download Process Verification is Successful"))
				.map(samityIdList -> managementProcessId);
	}

	private Mono<List<String>> validateSamityListForStagingDataDownloadByFieldOfficer(String managementProcessId, String fieldOfficerId) {
		return processTrackerPort.getStagingProcessEntityListForFieldOfficer(managementProcessId, fieldOfficerId)
				.filter(stagingProcessTrackerEntityList -> !stagingProcessTrackerEntityList.isEmpty())
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data is not Found for Field Officer")))
				.filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsDownloaded().equals("No")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data is Already Downloaded for Field Officer")))
				.flatMapIterable(entityList -> entityList.stream().map(StagingProcessTrackerEntity::getSamityId).toList())
				.flatMap(samityId -> this.validateSamityEventsForStagingDataDownloadAndDeletionByFieldOfficer(managementProcessId, samityId))
				.collectList();
	}

	@Override
	public Mono<StagingDataResponseDTO> deleteStagingDataByFieldOfficer(StagingDataRequestDTO requestDTO) {
		return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
				.flatMap(managementProcessTracker -> this.validateStagingDataDeletionForFieldOfficerAndUpdateProcessTracker(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getFieldOfficerId()))
				.map(message -> StagingDataResponseDTO.builder()
						.userMessage("Downloaded Staging Data is Successfully Deleted for Field Officer")
						.build())
				.doOnSuccess(responseDTO -> log.info("Staging Data Deletion For Field Officer Response: {}", responseDTO.getUserMessage()))
				.doOnError(throwable -> log.error("Error in Staging Data Deletion Process: {}", throwable.getMessage()));
	}

	@Override
	public Mono<StagingData> getStagingDataByAccountId(String accountId) {
		return stagingAccountPort.getStagingLoanOrSavingsAccountByAccountId(accountId)
				.map(StagingAccountData::getMemberId)
				.flatMap(stagingDataPort::getStagingDataByMemberId);
	}

	private Mono<String> validateStagingDataDeletionForFieldOfficerAndUpdateProcessTracker(String managementProcessId, String officeId, String fieldOfficerId) {
		return this.validateOfficeEventsForDownloadedStagingDataDeletion(managementProcessId, officeId)
				.flatMap(aBoolean -> this.validateSamityListForDownloadedStagingDataDeletion(managementProcessId, fieldOfficerId))
				.flatMap(samityIdList -> processTrackerPort.updateProcessTrackerForDownloadedStagingDataDeletionByFieldOfficer(managementProcessId, fieldOfficerId));
	}

	private Mono<Boolean> validateOfficeEventsForDownloadedStagingDataDeletion(String managementProcessId, String officeId) {
		return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, officeId)
				.filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
				.map(OfficeEventTracker::getOfficeEvent)
				.collectList()
				.filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated for Office")))
				.filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Already Completed for Office And Field Officer Staging Data Cannot Be Deleted")))
				.flatMap(officeEventList -> dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessId, officeId)
						.filter(dayEndProcessTrackerEntity -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTrackerEntity.getTransactionCode()))
						.collectList())
				.filter(List::isEmpty)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Running for Office And Field Officer Staging Data Cannot Be Deleted")))
				.map(dayEndProcessTrackers -> true);
	}

	private Mono<List<String>> validateSamityListForDownloadedStagingDataDeletion(String managementProcessId, String fieldOfficerId) {
		return processTrackerPort.getStagingProcessEntityListForFieldOfficer(managementProcessId, fieldOfficerId)
				.filter(stagingProcessTrackerEntityList -> !stagingProcessTrackerEntityList.isEmpty())
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data is not Found for Field Officer")))
				.filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsDownloaded().equals("Yes")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data is Not Downloaded for Field Officer")))
				.filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsUploaded().equals("No")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Staging Data is Already Uploaded for Field Officer")))
				.flatMapIterable(entityList -> entityList.stream().map(StagingProcessTrackerEntity::getSamityId).toList())
				.flatMap(samityId -> this.validateSamityEventsForStagingDataDownloadAndDeletionByFieldOfficer(managementProcessId, samityId))
				.collectList();
	}

	private Mono<String> validateSamityEventsForStagingDataDownloadAndDeletionByFieldOfficer(String managementProcessId, String samityId) {
		return samityEventUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
				.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
				.map(SamityEventTracker::getSamityEvent)
				.collectList()
				.filter(List::isEmpty)
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Event Already Found, Samity Staging Data Cannot Be Downloaded or Deleted")))
				.map(samityEventList -> samityId);
	}

	// Private methods
    private Mono<Tuple2<String, String>> updateAndDeleteStagingDataTablesForOffice(String managementProcessId, String officeEventId,
                                                                                   StagingDataRequestDTO requestDTO) {
        return processTrackerPort.updateStagingProcessTrackerDataToEditHistoryTable(managementProcessId, officeEventId, requestDTO.getOfficeId())
            .doOnNext(samityIdList -> log.info("Staging Process Tracker Data Dumped Into Edit History Table, Total Samity: {}", samityIdList.size()))
            .flatMap(samityIdList -> stagingDataPort.updateStagingDataToEditHistoryTable(managementProcessId, officeEventId, samityIdList))
            .doOnNext(memberIdList -> log.info("Staging Data Dumped Into Edit History Table, Total Member: {}", memberIdList.size()))
            .flatMap(memberIdList -> stagingAccountPort.updateStagingAccountDataToEditHistoryTable(managementProcessId, officeEventId, memberIdList))
            .doOnNext(accountIdList -> log.info("Staging Account Data Dumped Into Edit History Table, Total Loan Account: {}, Total Savings Account: {}", accountIdList.get("loanAccountIdList").size(), accountIdList.get("savingsAccountIdList").size()))
            .flatMap(accountIdList -> deleteStagingDataTablesForOffice(managementProcessId))
            .map(string -> Tuples.of(managementProcessId, officeEventId));
    }

    private Mono<String> deleteStagingDataTablesForOffice(String managementProcessId) {
        return stagingAccountPort.deleteAllStagingAccountDataByManagementProcessId(managementProcessId)
            .doOnNext(string -> log.info("{}", string))
            .flatMap(string -> stagingDataPort.deleteAllStagingDataByManagementProcessId(managementProcessId))
            .doOnNext(string -> log.info("{}", string))
            .flatMap(string -> processTrackerPort.deleteAllStagingProcessTrackerEntityByManagementProcessId(managementProcessId))
            .doOnNext(string -> log.info("{}", string))
            .flatMap(string -> Mono.just(managementProcessId));
    }


	private Mono<List<String>> createStagingDataHistoryAndDeleteStagingData(StagingProcessTrackerEntity trackerEntity, String loginId, String remarks) {
		return processTrackerPort.editUpdateAndDeleteProcessTracker(trackerEntity, loginId, remarks)
				.doOnNext(processTrackerEntity -> log.info("Staging Process Tracker Entry Updated For Samity: {}", trackerEntity.getSamityId()))
				.flatMap(processTrackerEntity -> stagingDataPort.editUpdateAndDeleteStagingDataOfASamity(trackerEntity.getProcessId(), trackerEntity.getSamityId()))
				.doOnNext(memberIdList -> log.info("Staging Data Deleted For Samity: {} containing Member List: {}", trackerEntity.getSamityId(), memberIdList))
				.flatMap(memberIdList -> stagingAccountPort.editUpdateAndDeleteStagingAccountDataOfASamity(trackerEntity.getProcessId(), trackerEntity.getSamityId(), memberIdList))
				.doOnNext(accountIdList -> log.info("Staging Account Data Deleted For Samity: {} containing Account List: {}", trackerEntity.getSamityId(), accountIdList))
				.doOnNext(accountIdList -> log.info("Staging Data is Invalidated Successfully For Samity: {} with Remarks: {}", trackerEntity.getSamityId(), remarks));
	}


	private Mono<Void> buildAndSaveStagingDataForOffice(ManagementProcessTracker managementProcessTracker, StagingDataStatusByOfficeResponseDTO responseDTO, StagingDataRequestDTO stagingDataRequestDTO, String stagingProcessId) {
		return Mono.just(stagingDataRequestDTO)
				.flatMap(requestDTO -> {
					List<StagingProcessTrackerEntity> entityList = new ArrayList<>();
					responseDTO.getData().forEach(samityStatus -> {
						StagingProcessTrackerEntity entity = gson.fromJson(samityStatus.toString(),
								StagingProcessTrackerEntity.class);
						entity.setManagementProcessId(managementProcessTracker.getManagementProcessId());
						entity.setProcessId(stagingProcessId);
						entity.setOfficeId(responseDTO.getOfficeId());
						entity.setIsDownloaded("No");
						entity.setIsUploaded("No");
						entity.setCurrentVersion(1);
						entityList.add(entity);
					});
					entityList.sort(Comparator.comparing(StagingProcessTrackerEntity::getSamityId));
					return this.clearPreviousStagingDataIfFoundAndSaveStagingProcessTracker(managementProcessTracker, entityList);
				})
				.flatMapIterable(entityList -> responseDTO.getData().stream()
						.map(StagingDataSamityStatusForOfficeDTO::getSamityId)
						.toList())
				.sort(String::compareTo)
				.concatMap(samityId -> this.buildAndSaveStagingDataForSamity(samityId, stagingDataRequestDTO.getLoginId(), stagingDataRequestDTO.getMfiId(), managementProcessTracker.getBusinessDate()))
				.collectList()
				.flatMap(stagingProcessTrackerEntityList -> officeEventUseCase.insertOfficeEvent(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId(), OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue(), stagingDataRequestDTO.getLoginId(), stagingProcessId))
				.doOnSuccess(entityList -> log.info("Staging Data Generation is Completed for office: {}", responseDTO.getOfficeId()))
				.doOnError(throwable -> log.error("Failed to generate Staging Data: {}", throwable.getMessage()))
            .then();
	}

	private Mono<List<StagingProcessTrackerEntity>> clearPreviousStagingDataIfFoundAndSaveStagingProcessTracker(ManagementProcessTracker managementProcessTracker, List<StagingProcessTrackerEntity> entityList){
		return stagingAccountPort.deleteStagingAccountDataByManagementProcessId(managementProcessTracker.getManagementProcessId())
				.flatMap(s -> stagingDataPort.deleteStagingDataByManagementProcessId(managementProcessTracker.getManagementProcessId()))
				.flatMap(s -> processTrackerPort.deleteStagingProcessTrackerByManagementProcessId(managementProcessTracker.getManagementProcessId()))
				.flatMap(s -> processTrackerPort.saveStagingProcessTrackerEntityList(entityList));
	}

	private Mono<StagingProcessTrackerEntity> buildAndSaveStagingDataForSamity(String samityId, String loginId, String mfiId, LocalDate businessDate) {
		return processTrackerPort.updateProcessTrackerEntityToProcessing(samityId, Status.STATUS_PROCESSING.getValue())
				.flatMap(trackerEntity -> this.buildStagingDataForMembersOfASamity(trackerEntity, loginId, mfiId)
						.flatMap(memberIdList -> this.buildAndSaveStagingAccountDataForMembersOfASamity(trackerEntity.getManagementProcessId(), trackerEntity.getProcessId(), memberIdList, loginId, businessDate)
								.map(stagingAccountDataIdList -> Tuples.of(memberIdList.size(), stagingAccountDataIdList.size()))))
				.flatMap(tuple -> processTrackerPort.updateProcessTrackerEntityToFinished(samityId, Status.STATUS_FINISHED.getValue(), tuple.getT1(), tuple.getT2()))
				.doOnError(throwable -> log.error("Failed to generate Staging Data: {}", throwable.getMessage()))
				.doOnSuccess(
						trackerEntity -> log.info("Staging Data Generation is Successful for samity: {}", samityId));
	}

	private Mono<List<String>> buildAndSaveStagingAccountDataForMembersOfASamity(String managementProcessId,
			String processId, List<String> memberIdList, String loginId, LocalDate businessDate) {
		return memberIdList.isEmpty()
				? Mono.just(new ArrayList<>())
				: this.buildStagingAccountDataForLoanAccountForMembersOfASamity(memberIdList, businessDate)
					.flatMap(stagingLoanAccountDataList -> this
						.buildStagingAccountDataForSavingsAccountForMembersOfASamity(memberIdList, businessDate)
						.map(stagingSavingsAccountDataList -> {
							List<StagingAccountData> stagingAccountDataList = new ArrayList<>();
							stagingAccountDataList.addAll(stagingLoanAccountDataList);
							stagingAccountDataList.addAll(stagingSavingsAccountDataList);
							stagingAccountDataList.forEach(stagingAccountData -> {
								stagingAccountData.setStagingAccountDataId(UUID.randomUUID().toString());
								stagingAccountData.setManagementProcessId(managementProcessId);
								stagingAccountData.setProcessId(processId);
								stagingAccountData.setCreatedBy(loginId);
							});
							return stagingAccountDataList;
						}))
					.flatMap(stagingAccountPort::saveAllStagingAccountData);
	}

	private Mono<List<StagingAccountData>> buildStagingAccountDataForLoanAccountForMembersOfASamity(
			List<String> memberIdList, LocalDate businessDate) {
		return commonRepository
				.getStagingLoanAccountListForMembersOfASamity(memberIdList, List.of(Status.STATUS_ACTIVE.getValue(), Status.STATUS_CLOSED_WRITTEN_OFF.getValue()))
				.doOnNext(loanAccountList -> log.info("Loan Account List: {}", loanAccountList))
				.filter(dto -> dto.getLoanAccountId() != null)
				.flatMap(dto -> this.buildStagingAccountDataForOneLoanAccount(dto, businessDate))
				.collectList()
				.doOnNext(stagingLoanAccountDataList -> {
					List<String> loanAccountIdList = stagingLoanAccountDataList.stream()
							.map(StagingAccountData::getLoanAccountId).toList();
					log.info("Loan Account Id List: {}", loanAccountIdList);
				});
	}

	private Mono<List<StagingAccountData>> buildStagingAccountDataForSavingsAccountForMembersOfASamity(
			List<String> memberIdList, LocalDate businessDate) {
		return savingsAccountUseCase.getSavingAccountForStagingAccountDataByMemberIdList(memberIdList)
				.filter(savingsAccountResponseDTO -> !savingsAccountResponseDTO.getStatus()
						.equalsIgnoreCase(Status.STATUS_INACTIVE.getValue()))
				.flatMap(savingsAccount -> {
					StagingAccountData stagingAccountData = mapper.map(savingsAccount, StagingAccountData.class);
					Mono<StagingAccountData> stagingAccountDataMono = Mono.just(stagingAccountData);
					if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_GS.getValue())) {
						stagingAccountData.setTargetAmount(savingsAccount.getGsInstallment());
						stagingAccountData.setEligibleToStage(checkIfGSVSIsEligibleToStage(savingsAccount));
						stagingAccountDataMono = Mono.just(stagingAccountData);
					} else if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_VS.getValue())) {
						stagingAccountData.setTargetAmount(savingsAccount.getVsInstallment());
						stagingAccountData.setEligibleToStage(checkIfGSVSIsEligibleToStage(savingsAccount));
						stagingAccountDataMono = Mono.just(stagingAccountData);
					} else if (savingsAccount.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_DPS.getValue()) ||
							savingsAccount.getSavingsProductType().equalsIgnoreCase(PRODUCT_TYPE_FDR.getValue())) {

						log.info("DPS FDR Account: {}", savingsAccount.getSavingsAccountId());
						stagingAccountDataMono = buildStagingAccountDataForDPSFDR(savingsAccount, stagingAccountData, businessDate);
					}
					return stagingAccountDataMono;
				})
				.flatMap(this::buildStagingAccountDataForOneSavingsAccount)
				.collectList()
				.doOnNext(stagingSavingsAccountDataList -> {
					List<String> savingsAccountIdList = stagingSavingsAccountDataList.stream()
							.map(StagingAccountData::getSavingsAccountId).toList();
					log.debug("Savings Account Id List: {}", savingsAccountIdList);
				});
	}

	private Mono<List<String>> buildStagingDataForMembersOfASamity(StagingProcessTrackerEntity trackerEntity,
			String loginId, String mfiId) {
		Flux<String> memberIdListOfASamityFlux = commonRepository.getMemberIdListOfASamity(trackerEntity.getSamityId());

		return memberIdListOfASamityFlux
				.hasElements()
				.flatMap(aBoolean -> {
					if (aBoolean) {
						return memberIdListOfASamityFlux
								.collectList()
								.filter(collectedList -> !collectedList.isEmpty())
								.doOnNext(memberIdList -> log.info("Member Id List Of a Samity: {}", memberIdList))
								.flatMapMany(commonRepository::getMemberEntityByMemberIdList)
								.sort(Comparator.comparing(MemberEntity::getMemberId))
								.map(memberEntity -> {
									StagingData stagingData = gson.fromJson(trackerEntity.toString(), StagingData.class);
									stagingData.setStagingDataId(UUID.randomUUID().toString());
									stagingData.setMemberId(memberEntity.getMemberId());
									stagingData.setMemberNameEn(memberEntity.getMemberNameEn());
									stagingData.setMemberNameBn(memberEntity.getMemberNameBn());
									stagingData.setMobile(memberEntity.getMobile());
									stagingData.setRegisterBookSerialId(memberEntity.getRegisterBookSerialId());
									stagingData.setCompanyMemberId(memberEntity.getCompanyMemberId());
									stagingData.setGender(memberEntity.getGender());
									stagingData.setMaritalStatus(memberEntity.getMaritalStatus());
									stagingData.setSpouseNameEn(memberEntity.getSpouseNameEn());
									stagingData.setSpouseNameBn(memberEntity.getSpouseNameBn());
									stagingData.setFatherNameEn(memberEntity.getFatherNameEn());
									stagingData.setFatherNameBn(memberEntity.getFatherNameBn());

									stagingData.setMfiId(mfiId);
									stagingData.setCreatedBy(loginId);
									stagingData.setCreatedOn(LocalDateTime.now());
									return stagingData;
								})
								.collectList()
								.filter(collectedList -> !collectedList.isEmpty())
								.doOnNext(stagingDataList -> log.debug("Staging Data List Of a Samity: {}", stagingDataList))
								.flatMap(stagingDataPort::saveAllStagingData);
					} else {
						return Mono.just(new ArrayList<>());
					}
				});
	}


	private StagingDataStatusByFieldOfficerResponseDTO buildFieldOfficerStagingDataResponse(
			StagingDataStatusByOfficeResponseDTO officeResponseDTO) {
		StagingDataStatusByFieldOfficerResponseDTO officerResponseDTO = gson.fromJson(officeResponseDTO.toString(),
				StagingDataStatusByFieldOfficerResponseDTO.class);
		if (!officeResponseDTO.getData().isEmpty()) {
			officerResponseDTO.setFieldOfficerId(officeResponseDTO.getData().get(0).getFieldOfficerId());
			officerResponseDTO.setFieldOfficerNameEn(officeResponseDTO.getData().get(0).getFieldOfficerNameEn());
			officerResponseDTO.setFieldOfficerNameBn(officeResponseDTO.getData().get(0).getFieldOfficerNameBn());
			if (!HelperUtil.checkIfNullOrEmpty(officeResponseDTO.getBtnStagingDataGenerateEnabled())
					&& officeResponseDTO.getBtnStagingDataGenerateEnabled().equals("No")) {
				if (!HelperUtil.checkIfNullOrEmpty(officeResponseDTO.getData().get(0).getIsDownloaded())
						&& officeResponseDTO.getData().get(0).getIsDownloaded().equals("Yes")) {
					officerResponseDTO.setBtnDownloadEnabled("No");
					officerResponseDTO.setBtnDeleteEnabled("Yes");
				} else {
					officerResponseDTO.setBtnDownloadEnabled("Yes");
					officerResponseDTO.setBtnDeleteEnabled("No");
				}
				officerResponseDTO.setUserMessage("Staging Data is Generated For Field Officer");
			} else {
				officerResponseDTO.setUserMessage("Staging Data is Not Generated For Field Officer");
				officerResponseDTO.setData(List.of());
				officerResponseDTO.setTotalCount(officerResponseDTO.getData().size());
			}
		}
		return officerResponseDTO;
	}

	private Mono<StagingDataStatusByOfficeResponseDTO> buildStagingDataGenerationStatusResponse(
			Tuple2<StagingDataRequestDTO, List<String>> tuple) {
		return managementProcessUseCase.getLastManagementProcessForOffice(tuple.getT1().getOfficeId())
				.flatMap(managementProcessTracker -> officeEventUseCase
						.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(),
								tuple.getT1().getOfficeId())
						.collectList()
						.map(list -> {
							String btnStagingDataGenerate = list.stream()
									.anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent())
											&& item.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())) ? "No"
													: "Yes";
							return StagingDataStatusByOfficeResponseDTO.builder()
									.officeId(tuple.getT1().getOfficeId())
									.businessDate(managementProcessTracker.getBusinessDate())
									.businessDay(managementProcessTracker.getBusinessDay())
									.btnStagingDataGenerateEnabled(btnStagingDataGenerate)
									.build();
						}))
				.flatMap(responseDTO -> {
					if (responseDTO.getBtnStagingDataGenerateEnabled().equals("No")) {
						return processTrackerPort.getStagingProcessTrackerEntityBySamityIdList(tuple.getT2())
								.map(stagingProcessTrackerEntity -> {
									StagingDataSamityStatusForOfficeDTO samityStatusDTO = gson.fromJson(
											stagingProcessTrackerEntity.toString(),
											StagingDataSamityStatusForOfficeDTO.class);
									return this.setBtnStatusForSamityStagingDataGeneration(samityStatusDTO);
								})
								.collectList()
								.flatMap(samityStatusList -> {
									List<String> samitIdList = new ArrayList<>();
									tuple.getT2().forEach(samityId -> {
										if (samityStatusList.isEmpty() || samityStatusList.stream().noneMatch(
												samityStatus -> samityStatus.getSamityId().equals(samityId))) {
											samitIdList.add(samityId);
										}
									});
									if (!samitIdList.isEmpty()) {
										return this
												.gridListOfStagingDataSamityStatusResponseForNotStagedOffice(
														samitIdList)
												.map(notFoundSamityStatusList -> {
													samityStatusList.addAll(notFoundSamityStatusList);
													return samityStatusList;
												});
									}
									return Mono.just(samityStatusList);
								})
								.map(samityStatusList -> {
									samityStatusList.sort(
											Comparator.comparing(StagingDataSamityStatusForOfficeDTO::getSamityId));
									responseDTO.setData(samityStatusList);
									responseDTO.setTotalCount(tuple.getT2().size());
									return responseDTO;
								});
					}
					return this.gridListOfStagingDataSamityStatusResponseForNotStagedOffice(tuple.getT2())
							.map(samityStatusList -> {
								responseDTO.setData(samityStatusList);
								responseDTO.setTotalCount(samityStatusList.size());
								return responseDTO;
							});
				})
				.flatMap(responseDTO -> commonRepository.getOfficeEntityByOfficeId(responseDTO.getOfficeId())
						.map(officeEntity -> {
							responseDTO.setOfficeNameEn(officeEntity.getOfficeNameEn());
							responseDTO.setOfficeNameBn(officeEntity.getOfficeNameBn());
							return responseDTO;
						}));
	}

	private Mono<List<StagingDataSamityStatusForOfficeDTO>> gridListOfStagingDataSamityStatusResponseForNotStagedOffice(List<String> samityIdList) {
		return commonRepository.getSamityDetailsForStagingDataBySamityIdList(samityIdList)
				.map(stagingProcessTrackerEntity -> gson.fromJson(stagingProcessTrackerEntity.toString(),
						StagingDataSamityStatusForOfficeDTO.class))
				.flatMap(samityStatus -> commonRepository.getTotalMemberOfASamity(samityStatus.getSamityId())
						.map(totalMember -> {
							samityStatus.setTotalMember(totalMember);
							return samityStatus;
						}))
				.flatMap(samityStatus -> commonRepository.getTotalActiveLoanAccountOfASamity(samityStatus.getSamityId())
						.flatMap(totalLoanAccount -> commonRepository
								.getTotalActiveSavingsAccountOfASamity(samityStatus.getSamityId())
								.map(totalSavingsAccount -> {
									samityStatus.setTotalAccount(totalLoanAccount + totalSavingsAccount);
									return samityStatus;
								})))
				.sort(Comparator.comparing(StagingDataSamityStatusForOfficeDTO::getSamityId))
				.collectList();
	}

	private Mono<StagingDataSamityStatusForOfficeDTO> setBtnStatusForSamityStagingDataGenerationForOffice(String managementProcessId, StagingDataSamityStatusForOfficeDTO samityStatus) {
		return samityEventUseCase.getAllSamityEventsForSamity(managementProcessId, samityStatus.getSamityId())
				.filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
				.collectList()
				.map(samityEventList -> {
					if(!samityEventList.isEmpty()){
						samityStatus.setBtnInvalidateEnabled("No");
						samityStatus.setBtnRegenerateEnabled("No");
					} else {
						this.setBtnStatusForSamityStagingDataGeneration(samityStatus);
					}
					return samityStatus;
				});
	}


	private StagingDataSamityStatusForOfficeDTO setBtnStatusForSamityStagingDataGeneration(StagingDataSamityStatusForOfficeDTO samityStatus) {
		if(samityStatus.getIsDownloaded().equals("Yes")){
			samityStatus.setBtnInvalidateEnabled("No");
			samityStatus.setBtnRegenerateEnabled("No");
		} else {
			if(samityStatus.getStatus().equals(Status.STATUS_FINISHED.getValue()) || samityStatus.getStatus().equals(Status.STATUS_REGENERATED.getValue())){
				samityStatus.setBtnInvalidateEnabled("Yes");
				samityStatus.setBtnRegenerateEnabled("No");
			} else if(samityStatus.getStatus().equals(Status.STATUS_FAILED.getValue()) || samityStatus.getStatus().equals(Status.STATUS_INVALIDATED.getValue())){
				samityStatus.setBtnInvalidateEnabled("No");
				samityStatus.setBtnRegenerateEnabled("Yes");
			} else{
				samityStatus.setBtnInvalidateEnabled("No");
				samityStatus.setBtnRegenerateEnabled("No");
			}
		}
		return samityStatus;
	}

}
