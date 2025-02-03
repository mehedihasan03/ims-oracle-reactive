package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.AccountDataInfo;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.AccountDetailsInfo;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionDetailResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionGridResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.EmployeePersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.FieldOfficerEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingSavingsAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.StageWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StagingWithdrawDataDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawPaymentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawStagingDataQueryDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper.WithdrawGridViewDataObject;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper.WithdrawMemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper.WithdrawSavingsAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Slf4j
@Service
public class WithdrawStagingDataQueryService implements IWithdrawStagingDataUseCase {

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final LoanAdjustmentPersistencePort adjustmentPersistencePort;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final CommonRepository commonRepository;
    private final IWithdrawStagingDataPersistencePort port;
    private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
    private final Gson gson;
    private final ModelMapper mapper;
    private final StageWithdrawUseCase stageWithdrawUseCase;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final EmployeePersistencePort employeePersistencePort;
    private final TransactionalOperator rxtx;

    public WithdrawStagingDataQueryService(ManagementProcessTrackerUseCase managementProcessTrackerUseCase, CollectionStagingDataPersistencePort collectionStagingDataPersistencePort, LoanAdjustmentPersistencePort adjustmentPersistencePort, OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, IStagingDataUseCase stagingDataUseCase, CommonRepository commonRepository, IWithdrawStagingDataPersistencePort port, DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort, ModelMapper mapper, StageWithdrawUseCase stageWithdrawUseCase, ISavingsAccountUseCase savingsAccountUseCase, EmployeePersistencePort employeePersistencePort, TransactionalOperator rxtx) {
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.adjustmentPersistencePort = adjustmentPersistencePort;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.commonRepository = commonRepository;
        this.port = port;
        this.dayEndProcessTrackerPersistencePort = dayEndProcessTrackerPersistencePort;
        this.mapper = mapper;
        this.stageWithdrawUseCase = stageWithdrawUseCase;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.employeePersistencePort = employeePersistencePort;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<WithdrawGridViewByOfficeResponseDTO> gridViewOfWithdrawStagingDataByOfficeV1(WithdrawStagingDataQueryDTO queryDTO) {
      Mono<List<String>> fieldOfficerList = commonRepository.getFieldOfficersByOfficeId(queryDTO.getOfficeId())
          .map(FieldOfficerEntity::getFieldOfficerId)
          .collectList();
      
      Mono<WithdrawGridViewByOfficeResponseDTO> withdrawGridViewByOfficeResponseDTOMono = fieldOfficerList
          .flatMapMany(list -> stagingDataUseCase.findSamityIdListByFieldOfficerIdList(list, queryDTO.getLimit(), queryDTO.getLimit() * queryDTO.getOffset()))
          .flatMap(stagingDataUseCase::getStagingDataBySamity)
          .distinct(StagingData::getSamityId)
          .map(stagingData -> {
            WithdrawGridViewDataObject gridViewDataObject = gson.fromJson(stagingData.toString(), WithdrawGridViewDataObject.class);
            gridViewDataObject.setType((gridViewDataObject.getDownloadedBy() == null && gridViewDataObject.getDownloadedOn() == null) ? "Online" : "Offline");
            return gridViewDataObject;
          })
          .flatMap(withdrawGridViewDataObject -> port.getTotalWithdrawAmountOfASamity(withdrawGridViewDataObject.getSamityId())
              .map(totalAmount -> {
                withdrawGridViewDataObject.setTotalWithdrawAmount(totalAmount);
                return withdrawGridViewDataObject;
              }))
          .collectList()
          .flatMap(list -> this.buildWithdrawGridViewByOfficeResponseDTO(list, queryDTO))
          .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_WITHDRAW_STAGING_DATA_FOUND_FOR_OFFICE.getValue() + queryDTO.getOfficeId())))
          .doOnError(throwable -> log.info("{}", throwable.getMessage()));
      
      return Mono.zip(withdrawGridViewByOfficeResponseDTOMono, fieldOfficerList)
          .flatMap(tuple -> stagingDataUseCase.getTotalCountByFieldOfficerList(tuple.getT2())
              .map(totalCount -> {
                tuple.getT1().setTotalCount(totalCount);
                return tuple.getT1();
              }));
    }

    @Override
    public Mono<WithdrawGridViewByFieldOfficerResponseDTO> gridViewOfWithdrawStagingDataByFieldOfficerV1(WithdrawStagingDataQueryDTO queryDTO) {
        return stagingDataUseCase.getStagingDataByFieldOfficer(queryDTO.getFieldOfficerId())
                .distinct(StagingData::getSamityId)
                .map(stagingData -> {
                    WithdrawGridViewDataObject gridViewDataObject = gson.fromJson(stagingData.toString(), WithdrawGridViewDataObject.class);
                    gridViewDataObject.setType((gridViewDataObject.getDownloadedBy() == null && gridViewDataObject.getDownloadedOn() == null) ? "Online" : "Offline");
                    return gridViewDataObject;
                })
                .flatMap(withdrawGridViewDataObject -> port.getTotalWithdrawAmountOfASamity(withdrawGridViewDataObject.getSamityId())
                        .map(totalAmount -> {
                            withdrawGridViewDataObject.setTotalWithdrawAmount(totalAmount);
                            return withdrawGridViewDataObject;}))
                .collectList()
                .doOnNext(withdrawGridViewDataObjectList -> log.info("withdrawGridViewDataObjectList: {}", withdrawGridViewDataObjectList))
                .map(list -> this.getWithdrawStagingDataGridViewResponseByFieldOfficer(list, queryDTO))
                .doOnNext(withdrawStagingDataGridViewResponseDTO -> log.info("withdrawStagingDataGridViewResponseDTO: {}", withdrawStagingDataGridViewResponseDTO));
    }

    @Override
    public Mono<AuthorizationWithdrawGridViewResponseDTO> gridViewOfWithdrawStagingDataForAuthorizationByOffice(WithdrawStagingDataQueryDTO queryDTO) {
        return commonRepository.getFieldOfficersByOfficeId(queryDTO.getOfficeId())
                .map(FieldOfficerEntity::getFieldOfficerId)
                .doOnNext(s -> log.info("FieldOfficerId for Withdraw Authorization: {}", s))
                .flatMap(stagingDataUseCase::getStagingDataByFieldOfficer)
                .distinct(StagingData::getSamityId)
                .doOnNext(stagingData -> log.info("Staging Data for withdraw authorization: {}", stagingData))
                .map(stagingData -> {
                    WithdrawGridViewDataObject withdrawGridViewDataObject = gson.fromJson(stagingData.toString(), WithdrawGridViewDataObject.class);
                    withdrawGridViewDataObject.setType((stagingData.getDownloadedBy() != null || stagingData.getDownloadedOn() != null) ? "Offline" : "Online");
//                    @TODO: set withdrawType to "Regular" or "Special" with check
                    withdrawGridViewDataObject.setWithdrawType(WithdrawType.SPECIAL.getValue());
                    return withdrawGridViewDataObject;
                })
                .doOnNext(withdrawGridViewDataObject -> log.info("withdrawGridViewDataObject: {}", withdrawGridViewDataObject))
                .flatMap(this::checkWithdrawGridViewDataObjectForAuthorizationCompletion)
                .doOnNext(withdrawGridViewDataObject -> log.info("before filter: {}", withdrawGridViewDataObject))
                .filter(gridViewDataObject -> gridViewDataObject.getStatus() != null && !gridViewDataObject.getStatus().isEmpty())
                .doOnNext(withdrawGridViewDataObject -> log.info("after filter: {}", withdrawGridViewDataObject))
                .collectList()
                .doOnNext(list -> log.info("withdrawGridViewDataObject List: {}", list))
                .map(list -> {
                    list.sort(Comparator.comparing(WithdrawGridViewDataObject::getSamityId));
                    return AuthorizationWithdrawGridViewResponseDTO.builder()
                            .officeId(queryDTO.getOfficeId())
                            .data(list)
                            .totalCount(list.size())
                            .build();
                });
    }

    @Override
    public Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataBySamityId(WithdrawStagingDataQueryDTO queryDTO) {
        return stagingDataUseCase.getStagingDataDetailViewResponseBySamityId(mapper.map(queryDTO, StagingDataRequestDTO.class))
                .map(dto -> {
                    WithdrawDetailViewResponseDTO responseDTO = gson.fromJson(dto.toString(), WithdrawDetailViewResponseDTO.class);
                    responseDTO.setOfficeId(queryDTO.getOfficeId());
                    return responseDTO;
                })
                .doOnNext(responseDTO -> log.info("WithdrawDetailViewResponseDTO from staging data: {}", responseDTO))
                .flatMap(this::getMemberListForWithdrawStagingData)
                .flatMap(this::updateSavingsAccountBalanceForWithdrawStagingDataDetailView);
    }

    private Mono<WithdrawDetailViewResponseDTO> updateSavingsAccountBalanceForWithdrawStagingDataDetailView(WithdrawDetailViewResponseDTO responseDTO) {
        return Flux.fromIterable(responseDTO.getMemberList())
                .flatMap(this::updateSavingsAccountBalanceForOneMember)
                .collectList()
                .map(memberList -> {
                    List<WithdrawMemberInfoDTO> sortedMemberList = memberList.stream().sorted(Comparator.comparing(WithdrawMemberInfoDTO::getMemberId)).toList();
                    responseDTO.setMemberList(sortedMemberList);
                    return responseDTO;
                });
    }

    private Mono<WithdrawMemberInfoDTO> updateSavingsAccountBalanceForOneMember(WithdrawMemberInfoDTO memberInfoDTO) {
        return Flux.fromIterable(memberInfoDTO.getSavingsAccountList())
                .flatMap((WithdrawSavingsAccountInfoDTO savingsAccountInfoDTO) -> updateSavingsAccountBalanceForOneSavingsAccount(savingsAccountInfoDTO, memberInfoDTO))
                .collectList()
                .map(savingsAccountList -> {
                    memberInfoDTO.setSavingsAccountList(savingsAccountList);
                    return memberInfoDTO;
                });
    }

    private Mono<WithdrawSavingsAccountInfoDTO> updateSavingsAccountBalanceForOneSavingsAccount(WithdrawSavingsAccountInfoDTO savingsAccountInfoDTO, WithdrawMemberInfoDTO memberInfoDTO) {
        savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance() == null ? BigDecimal.ZERO : savingsAccountInfoDTO.getBalance());
        savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : savingsAccountInfoDTO.getSavingsAvailableBalance());
        savingsAccountInfoDTO.setMinBalance(savingsAccountInfoDTO.getBalance().subtract(savingsAccountInfoDTO.getSavingsAvailableBalance()));
        return commonRepository.getCollectionStagingDataBySavingsAccountId(savingsAccountInfoDTO.getSavingsAccountId())
                .collectList()
                .doOnNext(list -> {
                    savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                    savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                })
                .flatMap(list -> commonRepository.getLoanAdjustmentDataBySavingsAccountId(savingsAccountInfoDTO.getSavingsAccountId())
                        .collectList())
                .map(list -> {
                    savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().subtract(!list.isEmpty() ? list.stream().map(LoanAdjustmentDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                    savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().subtract(!list.isEmpty() ? list.stream().map(LoanAdjustmentDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                    return savingsAccountInfoDTO;
                })
                .flatMap(withdrawSavingsAccountInfoDTO -> stageWithdrawUseCase.getStagingWithdrawDataByStagingDataId(memberInfoDTO.getStagingDataId())
                        .collectList()
                        .filter(stagingWithdrawDataDTOS -> !stagingWithdrawDataDTOS.isEmpty())
                        .map(staginWithdrawDataDTOS -> staginWithdrawDataDTOS.stream().map(StagingWithdrawDataDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                        .map(totalWithdrawAmount -> {
                            savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().subtract(totalWithdrawAmount));
                            savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().subtract(totalWithdrawAmount));
                            return savingsAccountInfoDTO;
                        })
                        .switchIfEmpty(Mono.just(withdrawSavingsAccountInfoDTO)));
    }

    @Override
    public Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataByMemberId(WithdrawStagingDataQueryDTO queryDTO) {
        return stagingDataUseCase.getStagingDataDetailViewResponseByMemberId(mapper.map(queryDTO, StagingDataRequestDTO.class))
                .map(dto -> {
                    WithdrawDetailViewResponseDTO responseDTO = gson.fromJson(dto.toString(), WithdrawDetailViewResponseDTO.class);
                    responseDTO.setOfficeId(queryDTO.getOfficeId());
                    return responseDTO;
                })
                .doOnNext(responseDTO -> log.info("WithdrawDetailViewResponseDTO from staging data: {}", responseDTO))
                .flatMap(this::getMemberListForWithdrawStagingData)
                .flatMap(this::updateSavingsAccountBalanceForWithdrawStagingDataDetailView);
    }

    @Override
    public Mono<WithdrawDetailViewResponseDTO> detailViewOfWithdrawStagingDataByAccountId(WithdrawStagingDataQueryDTO queryDTO) {
        return stagingDataUseCase.getStagingDataDetailViewResponseByAccountId(mapper.map(queryDTO, StagingDataRequestDTO.class))
                .map(dto -> {
                    WithdrawDetailViewResponseDTO responseDTO = gson.fromJson(dto.toString(), WithdrawDetailViewResponseDTO.class);
                    responseDTO.setOfficeId(queryDTO.getOfficeId());
                    return responseDTO;
                })
                .flatMap(this::getMemberListForWithdrawStagingData)
                .flatMap(this::updateSavingsAccountBalanceForWithdrawStagingDataDetailView);
    }

    @Override
    public Mono<List<StagingWithdrawData>> getAllWithdrawStagingDataBySamity(String samityId) {
        return port.getAllWithdrawStagingDataBySamity(samityId)
                .collectList();
    }


//    Process Management v2
    @Override
    public Mono<WithdrawPaymentResponseDTO> withdrawPayment(WithdrawPaymentRequestDTO requestDTO) {
        final String processId = UUID.randomUUID().toString();
        return this.validateWithdrawPaymentRequestAndGetManagementProcessId(requestDTO)
                .flatMap(managementProcessId -> this.validateIfWithdrawTransactionIsStillAvailableForSamity(managementProcessId, requestDTO.getSamityId()))
                .flatMap(managementProcessId -> this.getAndSaveSamityEventForWithdraw(managementProcessId, processId, requestDTO.getOfficeId(), requestDTO.getSamityId(), SamityEvents.WITHDRAWN.getValue(), requestDTO.getLoginId())
                        .map(samityEventId -> Tuples.of(managementProcessId, samityEventId)))
                .map(tuple -> requestDTO.getData().stream()
                        .map(dataObject -> StagingWithdrawData.builder()
                                .stagingWithdrawDataId(UUID.randomUUID().toString())
                                .managementProcessId(tuple.getT1())
                                .processId(tuple.getT2())
                                .stagingDataId(dataObject.getStagingDataId())
                                .samityId(requestDTO.getSamityId())
                                .savingsAccountId(dataObject.getSavingsAccountId())
                                .amount(dataObject.getAmount())
                                .paymentMode(dataObject.getPaymentMode())
                                .withdrawType(requestDTO.getWithdrawType())
                                .createdOn(LocalDateTime.now())
                                .createdBy(requestDTO.getLoginId())
                                .isUploaded("No")
                                .isNew("Yes")
                                .currentVersion(1)
                                .isLocked("No")
                                .isSubmitted("No")
                                .status(Status.STATUS_STAGED.getValue())
                                .build())
                        .toList())
                .flatMap(port::saveStagingWithdrawData)
                .map(stagingWithdrawDataList -> WithdrawPaymentResponseDTO.builder()
                        .userMessage("Withdraw is successfully completed")
                        .build())
                .doOnError(throwable -> log.error("Error in Withdraw Payment: {}", throwable.getMessage()));
    }

    private Mono<String> validateIfWithdrawTransactionIsStillAvailableForSamity(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
//                .filter(samityEvent -> !samityEvent.equals(SamityEvents.CANCELED.getValue()))
                .collectList()
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity is Already Canceled and Cannot Create Transaction")))
                .flatMap(samityEventList -> port.getAllWithdrawDataForSamity(managementProcessId, samityId)
                        .filter(stagingWithdrawData -> !HelperUtil.checkIfNullOrEmpty(stagingWithdrawData.getStatus()))
                        .collectList())
                .filter(stagingWithdrawDataList -> stagingWithdrawDataList.isEmpty() || stagingWithdrawDataList.stream().noneMatch(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Transaction is Already Authorized or Unauthorized for Samity")))
                .filter(stagingWithdrawDataList -> stagingWithdrawDataList.isEmpty() || stagingWithdrawDataList.stream().allMatch(stagingWithdrawData -> stagingWithdrawData.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Transaction is Already Locked for Authorization for Samity")))
                .map(stagingWithdrawDataList -> managementProcessId);
    }

    @Override
    public Mono<WithdrawPaymentResponseDTO> updateWithdrawPayment(WithdrawPaymentRequestDTO requestDTO) {
        return this.validateWithdrawPaymentRequestAndGetManagementProcessId(requestDTO)
                .flatMap(managementProcessId -> samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(managementProcessId, requestDTO.getSamityId(), SamityEvents.WITHDRAWN.getValue())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Withdraw is made for Samity")))
                        .map(samityEventTracker -> managementProcessId))
                .map(managementProcessId -> requestDTO.getData().stream()
                        .map(dataObject -> StagingWithdrawData.builder()
                                .stagingDataId(dataObject.getStagingDataId())
                                .savingsAccountId(dataObject.getSavingsAccountId())
                                .amount(dataObject.getAmount())
                                .build())
                        .toList())
                .flatMap(stagingWithdrawDataList -> port.updateWithdrawPayment(stagingWithdrawDataList, requestDTO.getLoginId()))
                .map(stagingWithdrawDataList -> WithdrawPaymentResponseDTO.builder()
                        .userMessage("Withdraw is successfully updated")
                        .build())
                .doOnError(throwable -> log.error("Error in Withdraw Payment: {}", throwable.getMessage()));
    }

    @Override
    public Mono<WithdrawPaymentResponseDTO> submitWithdrawPayment(WithdrawStagingDataQueryDTO requestDTO) {
        return this.getManagementProcessIdAndValidateSamityWithdrawDataForSubmission(requestDTO.getOfficeId(), requestDTO.getSamityId())
                .flatMap(managementProcessId ->port.validateAndUpdateWithdrawDataForSubmission(managementProcessId, requestDTO.getSamityId(), requestDTO.getLoginId()))
                .map(data -> WithdrawPaymentResponseDTO.builder()
                        .userMessage("Withdraw is successfully submitted for Samity")
                        .build());
    }

    private Mono<String> getManagementProcessIdAndValidateSamityWithdrawDataForSubmission(String officeId, String samityId){
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList()
                        .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                        .filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Completed For Office"))))
                .flatMap(officeEventList -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcess.get().getManagementProcessId(), samityId)
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .filter(samityEventList -> samityEventList.contains(SamityEvents.WITHDRAWN.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Withdraw Data Found for Samity"))))
                .map(samityEventList -> managementProcess.get().getManagementProcessId());
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
        return port.lockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return port.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
        return port.getSamityIdListLockedByUserForAuthorization(lockedBy);
    }

    @Override
    public Mono<Map<String, BigDecimal>> getTotalWithdrawAmountForSamityIdList(List<String> samityIdList) {
        return port.getWithdrawStagingDataListBySamityIdList(samityIdList)
                .map(stagingWithdrawDataList -> {
                    Map<String, BigDecimal> samityWithTotalWithdraw = new HashMap<>();
                    samityIdList.forEach(samityId -> {
                        BigDecimal totalAmount = stagingWithdrawDataList.stream().filter(collectionStagingData -> collectionStagingData.getSamityId().equals(samityId))
                                .map(StagingWithdrawData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityWithTotalWithdraw.put(samityId, totalAmount);
                    });
                    return samityWithTotalWithdraw;
                });
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return port.validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return port.validateAndUpdateWithdrawStagingDataForRejectionBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<List<StagingWithdrawData>> getAllWithdrawDataBySamityIdList(List<String> samityIdList) {
        return port.getWithdrawStagingDataListBySamityIdList(samityIdList);
    }

    @Override
    public Mono<String> validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return port.validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(managementProcessId, samityId, loginId)
                .map(response -> samityId);
    }

    @Override
    public Mono<WithdrawGridViewByOfficeResponseDTO> gridViewOfWithdrawStagingDataByOffice(WithdrawStagingDataQueryDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .flatMap(officeEventList -> {
                    if(officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))){
                        return stagingDataUseCase.getSamityIdListByOfficeId(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId())
                                .collectList()
                                .flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), SamityEvents.WITHDRAWN.getValue())
                                        .filter(samityIdlist::contains)
                                        .collectList())
                                .flatMap(samityIdList -> this.buildWithdrawGridViewSamityResponse(managementProcess.get().getManagementProcessId(), samityIdList, requestDTO.getLimit(), requestDTO.getOffset()));
                    }
                    List<WithdrawGridViewDataObject> emptyList = new ArrayList<>();
                    return Mono.just(emptyList);
                })
                .map(samityObjectList -> WithdrawGridViewByOfficeResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.get().getBusinessDay())
                        .data(samityObjectList)
                        .totalCount(samityObjectList.size())
                        .build())
                .doOnSuccess(responseDTO -> log.info("Withdraw Grid View by Office ResponseDTO: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Withdraw Grid View by Office: {}", throwable.getMessage()));
    }

    @Override
    public Mono<WithdrawGridViewByFieldOfficerResponseDTO> gridViewOfWithdrawStagingDataByFieldOfficer(WithdrawStagingDataQueryDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .flatMap(officeEventList -> {
                    if(officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))){
                        return stagingDataUseCase.getSamityIdListByFieldOfficer(requestDTO.getFieldOfficerId())
                                .collectList()
                                .flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), SamityEvents.WITHDRAWN.getValue())
                                        .filter(samityIdlist::contains)
                                        .collectList())
                                .flatMap(samityIdList -> this.buildWithdrawGridViewSamityResponse(managementProcess.get().getManagementProcessId(), samityIdList, requestDTO.getLimit(), requestDTO.getOffset()));
                    }
                    List<WithdrawGridViewDataObject> emptyList = new ArrayList<>();
                    return Mono.just(emptyList);
                })
                .map(samityObjectList -> WithdrawGridViewByFieldOfficerResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.get().getBusinessDay())
                        .fieldOfficerId(requestDTO.getFieldOfficerId())
                        .data(samityObjectList)
                        .totalCount(samityObjectList.size())
                        .build())
                .flatMap(responseDTO -> {
                    if(responseDTO.getData().isEmpty()){
                        return commonRepository.getFieldOfficerByFieldOfficerId(requestDTO.getFieldOfficerId())
                                .map(fieldOfficerEntity -> {
                                    responseDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
                                    responseDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
                                    return responseDTO;
                                });
                    }
                    responseDTO.setFieldOfficerNameEn(responseDTO.getData().get(0).getFieldOfficerNameEn());
                    responseDTO.setFieldOfficerNameBn(responseDTO.getData().get(0).getFieldOfficerNameBn());
                    return Mono.just(responseDTO);
                })
                .doOnSuccess(responseDTO -> log.info("Withdraw Grid View by Field Officer ResponseDTO: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Withdraw Grid View by Field Officer: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridResponse> withdrawCollectionGridView(WithdrawStagingDataQueryDTO request) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No management process found for office" + request.getOfficeId())))
                .flatMap(managementProcessTracker -> getAccountDataInfo(request, managementProcessTracker.getManagementProcessId())
                        .collectList()
                        .map(dataList -> CollectionGridResponse.builder()
                                .mfiId(managementProcessTracker.getMfiId())
                                .officeId(managementProcessTracker.getOfficeId())
                                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                                .businessDate(managementProcessTracker.getBusinessDate())
                                .businessDay(managementProcessTracker.getBusinessDay())
                                .data(dataList)
                                .build())
                        .flatMap(collectionGridResponse -> getWithdrawCount(request, managementProcessTracker.getManagementProcessId())
                                .map(count -> {
                                    log.info("Total Count: {}", count);
                                    collectionGridResponse.setTotalCount(count);
                                    return collectionGridResponse;
                                })
                        )
                );
    }

    private Mono<String> getLoginIdByFieldOfficerId(WithdrawStagingDataQueryDTO request) {
        return employeePersistencePort.getEmployeeByEmployeeId(request.getFieldOfficerId().trim())
                .map(employee -> employee.getLoginId().trim());
    }

    private Mono<String> setRequestLoginId(WithdrawStagingDataQueryDTO request) {
        return HelperUtil.checkIfNullOrEmpty(request.getFieldOfficerId()) ? Mono.just(request.getFieldOfficerId().trim()) : getLoginIdByFieldOfficerId(request);
    }

    private Mono<Long> getWithdrawCount(WithdrawStagingDataQueryDTO request, String managementProcessId) {
        return setRequestLoginId(request).flatMap(loginId -> port.countWithdrawData(managementProcessId, loginId));
    }

    private Flux<AccountDataInfo> getAccountDataInfo(WithdrawStagingDataQueryDTO request, String managementProcessId) {
        return setRequestLoginId(request)
                .flatMapMany(loginId -> port.getAllWithdrawCollectionDataByLoginId(managementProcessId, loginId, request.getLimit(), request.getOffset())
                        .switchIfEmpty(Mono.just(WithdrawStagingData.builder().build()))
                        .filter(withdrawStagingData -> withdrawStagingData.getSamityId() != null && withdrawStagingData.getSamityId().startsWith(request.getOfficeId()))
                        .switchIfEmpty(Mono.just(WithdrawStagingData.builder().build()))
                        .flatMap(withdrawStagingData -> stagingDataUseCase.getStagingDataByStagingDataId(withdrawStagingData.getStagingDataId())
                                .map(stagingData -> buildAccountDataInfo(withdrawStagingData, stagingData))));
    }

    @Override
    public Mono<CollectionDetailResponse> withdrawCollectionDetailView(WithdrawStagingDataQueryDTO request) {
        return port.getWithdrawCollectionDataByOid(request.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No withdraw collection data found for oid: " + request.getId())))
                .doOnNext(withdrawStagingData -> log.info("Withdraw Staging Data: {}", withdrawStagingData))
                .flatMap(withdrawStagingData -> commonRepository.getMemberEntityBySavingsAccountId(withdrawStagingData.getSavingsAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No member found for savings account id: " + withdrawStagingData.getSavingsAccountId())))
                        .map(this::extractMobileNumberFromMobileDetails)
                        .flatMap(memberInfoDTO -> Mono.zip(
                                Mono.just(memberInfoDTO),
                                stagingDataUseCase.getStagingAccountDataBySavingsAccountId(withdrawStagingData.getSavingsAccountId()),
                                stagingDataUseCase.getStagingDataByStagingDataId(withdrawStagingData.getStagingDataId()),
                                collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(withdrawStagingData.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().build())),
                                adjustmentPersistencePort.getLoanAdjustmentCollectionDataBySavingsAccountId(withdrawStagingData.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build()))
                        ))
                        .map(tuple -> buildSavingsAccountDetailResponse(withdrawStagingData, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()))
                        .doOnNext(tuple -> log.info("Collection Detail Response: {}", tuple)));
    }

    private CollectionDetailResponse buildSavingsAccountDetailResponse(WithdrawStagingData withdrawStagingData,
                                                                       MemberInfoDTO memberInfo,
                                                                       StagingAccountData savingsAccountInfo,
                                                                       StagingData stagingData,
                                                                       CollectionStagingData collectionStagingData,
                                                                       LoanAdjustmentData loanAdjustmentData) {
        BigDecimal collectionAmount = collectionStagingData.getAmount() == null ? BigDecimal.ZERO : collectionStagingData.getAmount();
        BigDecimal adjustmentAmount = loanAdjustmentData.getAmount() == null ? BigDecimal.ZERO : loanAdjustmentData.getAmount();
        return CollectionDetailResponse.builder()
                .userMessage("Data fetch successfully")
                .fieldOfficerId(stagingData.getFieldOfficerId())
                .fieldOfficerNameEn(stagingData.getFieldOfficerNameEn())
                .fieldOfficerNameBn(stagingData.getFieldOfficerNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .paymentMode(withdrawStagingData.getPaymentMode())
                .amount(withdrawStagingData.getAmount())
                .status(withdrawStagingData.getStatus())
                .data(AccountDetailsInfo.builder()
                        .memberInfo(MemberInfoDTO.builder()
                                .memberId(memberInfo.getMemberId())
                                .memberNameEn(memberInfo.getMemberNameEn())
                                .memberNameBn(memberInfo.getMemberNameBn())
                                .mobile(memberInfo.getMobile())
                                .registerBookSerialId(memberInfo.getRegisterBookSerialId())
                                .companyMemberId(memberInfo.getCompanyMemberId())
                                .gender(memberInfo.getGender())
                                .maritalStatus(memberInfo.getMaritalStatus())
                                .fatherNameBn(memberInfo.getFatherNameBn())
                                .fatherNameEn(memberInfo.getFatherNameEn())
                                .spouseNameBn(memberInfo.getSpouseNameBn())
                                .spouseNameEn(memberInfo.getSpouseNameEn())
                                .build())
                        .savingsAccountInfo(StagingSavingsAccountInfoDTO.builder()
                                .oid(savingsAccountInfo.getOid())
                                .savingsAccountId(savingsAccountInfo.getSavingsAccountId())
                                .savingsProductCode(savingsAccountInfo.getSavingsProductCode())
                                .savingsProductNameEn(savingsAccountInfo.getSavingsProductNameEn())
                                .savingsProductNameBn(savingsAccountInfo.getSavingsProductNameBn())
                                .savingsProductType(savingsAccountInfo.getSavingsProductType())
                                .targetAmount(savingsAccountInfo.getTargetAmount())
                                .balance(savingsAccountInfo.getBalance())
                                .savingsAvailableBalance(
                                        savingsAccountInfo.getSavingsAvailableBalance() == null ?
                                                BigDecimal.ZERO :
                                                savingsAccountInfo.getSavingsAvailableBalance()
                                                        .add(collectionAmount)
                                                        .subtract(adjustmentAmount)
                                )
                                .build())
                        .build())
                .build();
    }

    private AccountDataInfo buildAccountDataInfo(WithdrawStagingData withdrawStagingData, StagingData stagingData) {
        return AccountDataInfo.builder()
                .btnViewEnabled(Status.STATUS_YES.getValue())
//                .btnOpenEnabled("")
                .btnEditEnabled(withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
                .btnSubmitEnabled(withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
//                .btnCommitEnabled("")
                .oid(withdrawStagingData.getOid())
                .collectionStagingDataId(withdrawStagingData.getWithdrawStagingDataId())
                .stagingDataId(stagingData.getStagingDataId())
                .memberId(stagingData.getMemberId())
                .memberNameEn(stagingData.getMemberNameEn())
                .memberNameBn(stagingData.getMemberNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .savingsAccountId(withdrawStagingData.getSavingsAccountId())
                .collectionType(withdrawStagingData.getWithdrawType())
                .amount(withdrawStagingData.getAmount())
                .paymentMode(withdrawStagingData.getPaymentMode())
                .status(withdrawStagingData.getStatus())
                .createdBy(withdrawStagingData.getCreatedBy())
                .build();
    }

    private Mono<List<WithdrawGridViewDataObject>> buildWithdrawGridViewSamityResponse(String managementProcessId, List<String> samityIdList, Integer limit, Integer offset) {
        log.info("Withdraw Samity Id List: {}", samityIdList);
        List<String> paginatedSamityIdList = samityIdList.stream().skip((long) limit * offset).limit(limit).toList();
        log.info("Paginated Withdraw Samity Id List: {}", paginatedSamityIdList);
        return stagingDataUseCase.getStagingProcessTrackerListBySamityIdList(managementProcessId, paginatedSamityIdList)
                .map(stagingProcessTrackerEntityList -> stagingProcessTrackerEntityList.stream()
                        .map(stagingProcessTrackerEntity -> gson.fromJson(stagingProcessTrackerEntity.toString(), WithdrawGridViewDataObject.class))
                        .toList())
                .flatMapIterable(samityObjectList -> samityObjectList)
                .flatMap(samityObject -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityObject.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .map(samityEventList -> {
                            if(samityEventList.isEmpty()){
                                samityObject.setStatus("Withdraw Incomplete");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else {
                                if(samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue()))){
                                    samityObject.setStatus("Samity Canceled");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                } else if(samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.WITHDRAWN.getValue()))){
                                    if(samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue()))){
                                        samityObject.setStatus("Withdraw Authorized");
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                    } else{
                                        samityObject.setStatus("Withdraw Completed");
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnEditEnabled("Yes");
                                        samityObject.setBtnSubmitEnabled("Yes");
                                    }
                                } else {
                                    samityObject.setStatus("Withdraw Incomplete");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                }
                            }

                            if(samityObject.getTotalMember() == null || samityObject.getTotalMember() == 0){
                                samityObject.setStatus("Withdraw Unavailable");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                                samityObject.setRemarks("No Member in Samity");
                            }
                            return samityObject;
                        }))
                .flatMap(samityObject -> this.getWithdrawAmountAndBuildSamityDataResponse(managementProcessId, samityObject))
                .sort(Comparator.comparing(WithdrawGridViewDataObject::getSamityId))
                .collectList();
    }

    private Mono<WithdrawGridViewDataObject> getWithdrawAmountAndBuildSamityDataResponse(String managementProcessId, WithdrawGridViewDataObject samityObject) {
        return port.getAllWithdrawDataForSamity(managementProcessId, samityObject.getSamityId())
                .filter(stagingWithdrawData -> !HelperUtil.checkIfNullOrEmpty(stagingWithdrawData.getSavingsAccountId()))
                .collectList()
                .doOnNext(withdrawDataList -> log.info("Samity Id: {} Withdraw Data List size: {}", samityObject.getSamityId(), withdrawDataList.size()))
                .map(withdrawDataList -> {
                    if(!withdrawDataList.isEmpty()){
                        if(samityObject.getStatus().equals("Withdraw Completed")){
                            if(withdrawDataList.stream().allMatch(withdrawData -> withdrawData.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))){
                                samityObject.setStatus("Withdraw Unauthorized");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else if(withdrawDataList.stream().allMatch(withdrawData -> withdrawData.getStatus().equals(Status.STATUS_REJECTED.getValue()))){
                                if(withdrawDataList.stream().allMatch(withdrawData -> withdrawData.getIsSubmitted().equals("Yes"))){
                                    samityObject.setStatus("Withdraw Submitted");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                } else {
                                    samityObject.setStatus("Withdraw Rejected");
                                    samityObject.setBtnEditEnabled("Yes");
                                    samityObject.setBtnSubmitEnabled("Yes");
                                }
                            } else if(withdrawDataList.stream().allMatch(withdrawData -> withdrawData.getIsLocked().equals("Yes"))){
                                samityObject.setStatus("Withdraw Locked");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else if(withdrawDataList.stream().allMatch(withdrawData -> withdrawData.getIsSubmitted().equals("Yes"))){
                                samityObject.setStatus("Withdraw Submitted");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            }
                        }

                        BigDecimal totalWithdrawAmount = withdrawDataList.stream()
                                .map(StagingWithdrawData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityObject.setTotalWithdrawAmount(totalWithdrawAmount);
                    } else {
                        samityObject.setTotalWithdrawAmount(BigDecimal.ZERO);
                    }
                    return samityObject;
                });
    }

    private Mono<List<StagingWithdrawData>> validateWithdrawDatForSubmission(String managementProcessId, String samityId, String loginId) {
        return port.getAllWithdrawDataForSamity(managementProcessId, samityId)
                .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getCreatedBy()) && data.getCreatedBy().equals(loginId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Withdraw data found for samity and user"))).filter(data -> data.getStatus().equals(Status.STATUS_STAGED.getValue()) || data.getStatus().equals(Status.STATUS_REJECTED.getValue()))
                .filter(data -> data.getStatus().equals(Status.STATUS_STAGED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data is already Submitted")))
                .collectList();
    }

    private Mono<String> validateWithdrawPaymentRequestAndGetManagementProcessId(WithdrawPaymentRequestDTO withdrawPaymentRequestDTO) {
        return this.validateAndGetManagementProcessForWithdraw(withdrawPaymentRequestDTO.getOfficeId(), withdrawPaymentRequestDTO.getSamityId())
                .filter(managementProcessId -> withdrawPaymentRequestDTO.getWithdrawType().equals("Special"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Type Mismatch for Samity")))
                .flatMap(managementProcessId -> Flux.fromIterable(withdrawPaymentRequestDTO.getData())
                    .filter(dataObject -> !HelperUtil.checkIfNullOrEmpty(dataObject.getPaymentMode()))
                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Payment Mode is not provided")))
                    .flatMap(dataObject -> stagingDataUseCase.getStagingAccountDataBySavingsAccountId(dataObject.getSavingsAccountId())
                            .doOnNext(stagingAccountData -> log.debug("Staging Account Data: {}", stagingAccountData))
                            .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getStagingAccountDataId()))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Account Data is Not Found For Account")))
                            .doOnNext(stagingAccountData -> stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance()))
                            .flatMap(this::getSavingsAccountTransactionsAndUpdateAvailableBalance)
                            .flatMap(stagingAccountData -> isSavingsAccountStatusActive(stagingAccountData.getSavingsAccountId())
                                    .flatMap(aBoolean -> aBoolean
                                            ? Mono.just(stagingAccountData)
                                            : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAVINGS_ACCOUNT_IS_NOT_ACTIVE.getValue() + stagingAccountData.getSavingsAccountId()))))
                            .map(stagingAccountData -> Tuples.of(dataObject, stagingAccountData)))
                    .flatMap(tuple -> stagingDataUseCase.getStagingDataByMemberId(tuple.getT2().getMemberId())
                            .filter(stagingData -> !HelperUtil.checkIfNullOrEmpty(stagingData.getSamityId()) && stagingData.getSamityId().equals(withdrawPaymentRequestDTO.getSamityId()))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id mismatch with Account Id")))
                            .filter(stagingData -> !HelperUtil.checkIfNullOrEmpty(stagingData.getStagingDataId()) && stagingData.getStagingDataId().equals(tuple.getT1().getStagingDataId()))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Id mismatch with Account Id")))
                            .map(stagingData -> Tuples.of(tuple.getT1(), tuple.getT2(), stagingData)))
                    .flatMap(tuple -> {
                        log.info("savingsAvailableBalance : {}", tuple.getT2().getSavingsAvailableBalance());
                        if (tuple.getT2().getSavingsAvailableBalance() == null) {
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Available Balance is not found for Account : " + tuple.getT2().getSavingsAccountId()));
                        }

                        log.info("subtract: {}", tuple.getT1().getAmount().doubleValue() <= tuple.getT2().getSavingsAvailableBalance().doubleValue());
                        if (tuple.getT1().getAmount().compareTo(tuple.getT2().getSavingsAvailableBalance()) > 0) {
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Cannot withdraw more than available balance for savings account Id : " + tuple.getT2().getSavingsAccountId()));
                        }

                        return Mono.just(managementProcessId);
                    })
                    .collectList()
                        .map(strings -> {
                            log.info("Management Process Id List: {}", strings);
                            return strings;
                        })
                    .map(s -> s.get(0)));
    }


    private Mono<Boolean> isSavingsAccountStatusActive(String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(savingsAccountDTO -> savingsAccountDTO.getStatus().equals(Status.STATUS_ACTIVE.getValue()));
    }



    private Mono<StagingAccountData> getSavingsAccountTransactionsAndUpdateAvailableBalance(StagingAccountData stagingAccountData) {
        return commonRepository.getCollectionStagingDataBySavingsAccountId(stagingAccountData.getSavingsAccountId())
                .switchIfEmpty(Mono.just(CollectionStagingDataEntity.builder()
                        .savingsAccountId(stagingAccountData.getSavingsAccountId())
                        .amount(BigDecimal.ZERO)
                        .build()))
                .collectList()
                .flatMap(collectionStagingDataEntityList -> commonRepository.getLoanAdjustmentDataBySavingsAccountId(stagingAccountData.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(LoanAdjustmentDataEntity.builder()
                                .savingsAccountId(stagingAccountData.getSavingsAccountId())
                                .amount(BigDecimal.ZERO)
                                .build()))
                        .collectList()
                        .map(loanAdjustmentDataEntityList -> Tuples.of(collectionStagingDataEntityList, loanAdjustmentDataEntityList)))
                .map(tuple -> {
                    BigDecimal collectionAmount = tuple.getT1().stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal loanAdjustedAmount = tuple.getT2().stream().map(LoanAdjustmentDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance().add(collectionAmount).subtract(loanAdjustedAmount));
//                    stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance().subtract(loanAdjustedAmount));
                    return stagingAccountData;
                });
    }

    private Mono<String> validateAndGetManagementProcessForWithdraw(String officeId, String samityId){
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Management Process Found For Office")))
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getOfficeEventByStatusForOffice(managementProcessTracker.getManagementProcessId(), officeId, OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .flatMap(officeEventTracker -> stagingDataUseCase.getStagingProcessEntityForSamity(officeEventTracker.getManagementProcessId(), samityId))
                .doOnNext(trackerEntity -> log.debug("Process Tracker Entity: {}", trackerEntity))
                .filter(trackerEntity -> !HelperUtil.checkIfNullOrEmpty(trackerEntity.getOfficeId()) && trackerEntity.getOfficeId().equals(officeId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Office Id Mismatch with Samity Id")))
                .map(StagingProcessTrackerEntity::getManagementProcessId);
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsStartedForOffice(ManagementProcessTracker managementProcessTracker) {
        return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office")))
                .map(dayEndProcessTrackers -> managementProcessTracker);
    }

    private Mono<String> getAndSaveSamityEventForWithdraw(String managementProcessId, String processId, String officeId, String samityId, String samityEvent, String loginId){
        return samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(managementProcessId, samityId, samityEvent)
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEventTrackerId()))
                .doOnNext(samityEventTracker -> log.debug("samityEventTracker: {}", samityEventTracker))
                .switchIfEmpty(samityEventTrackerUseCase.insertSamityEvent(managementProcessId, processId, officeId, samityId, samityEvent, loginId))
//                .flatMap(samityEventTracker -> {
//                    if(HelperUtil.checkIfNullOrEmpty(samityEventTracker.getManagementProcessId())){
//                        return samityEventTrackerUseCase.insertSamityEvent(managementProcessId, processId, officeId, samityId, samityEvent, loginId)
//                                .map(SamityEventTracker::getSamityEventTrackerId);
//                    } else {
//                        return Mono.just(processId);
//                    }
//                });
                .map(SamityEventTracker::getSamityEventTrackerId);
    }
    private Mono<WithdrawGridViewByOfficeResponseDTO> buildWithdrawGridViewByOfficeResponseDTO(List<WithdrawGridViewDataObject> list, WithdrawStagingDataQueryDTO queryDTO){
        return commonRepository.getOfficeEntityByOfficeId(queryDTO.getOfficeId())
                .map(officeEntity -> WithdrawGridViewByOfficeResponseDTO.builder()
                        .officeId(officeEntity.getOfficeId())
                        .officeNameEn(officeEntity.getOfficeNameEn())
                        .officeNameBn(officeEntity.getOfficeNameBn())
                        .data(list)
                        .build());
    }

    private Mono<WithdrawGridViewDataObject> checkWithdrawGridViewDataObjectForAuthorizationCompletion(WithdrawGridViewDataObject withdrawGridViewDataObject){
        return port.getWithdrawStagingDataBySamityIdAndWithdrawType(withdrawGridViewDataObject.getSamityId(), withdrawGridViewDataObject.getWithdrawType())
                .collectList()
                .doOnNext(list -> log.info("withdrawStagingData List: {}", list))
                .map(withdrawStagingDataList -> {
                    if(withdrawStagingDataList.size() > 0) {
                        withdrawGridViewDataObject.setUploadedBy(withdrawStagingDataList.get(0).getUploadedBy());
                        withdrawGridViewDataObject.setUploadedOn(withdrawStagingDataList.get(0).getUploadedOn());
                        withdrawGridViewDataObject.setStatus(withdrawStagingDataList
                                .stream()
                                .anyMatch(withdrawStagingData -> Objects.equals(withdrawStagingData.getStatus(), Status.STATUS_STAGED.getValue()))
                                ? "Authorization Incomplete" : "Authorization Completed");
                    }
                    return withdrawGridViewDataObject;
                });
    }

    private WithdrawGridViewByFieldOfficerResponseDTO getWithdrawStagingDataGridViewResponseByFieldOfficer(List<WithdrawGridViewDataObject> withdrawGridViewDataObjectList, WithdrawStagingDataQueryDTO queryDTO){

        withdrawGridViewDataObjectList.sort(Comparator.comparing(WithdrawGridViewDataObject::getSamityId));

        return WithdrawGridViewByFieldOfficerResponseDTO.builder()
                .officeId(queryDTO.getOfficeId())
                .fieldOfficerId(queryDTO.getFieldOfficerId())
                .fieldOfficerNameEn(withdrawGridViewDataObjectList.get(0).getFieldOfficerNameEn())
                .fieldOfficerNameBn(withdrawGridViewDataObjectList.get(0).getFieldOfficerNameBn())
                .data(withdrawGridViewDataObjectList)
                .totalCount(withdrawGridViewDataObjectList.size())
                .build();
    }

    private Mono<WithdrawDetailViewResponseDTO> getMemberListForWithdrawStagingData(WithdrawDetailViewResponseDTO responseDTO){
        return Flux.fromIterable(responseDTO.getMemberList())
                .flatMap(this::getAccountListForWithdrawStagingData)
                .collectList()
                .map(list -> {
                    list.sort((Comparator.comparing(WithdrawMemberInfoDTO::getMemberId)));
                    return list;
                })
                .doOnNext(list -> log.info("MemberInfoDTO List for withdraw: {}", list))
                .map(list -> {
                    responseDTO.setMemberList(list);
                    return responseDTO;
                });
    }

    private Mono<WithdrawMemberInfoDTO> getAccountListForWithdrawStagingData(WithdrawMemberInfoDTO memberInfoDTO){
        return Flux.fromIterable(memberInfoDTO.getSavingsAccountList())
                .doOnNext(list -> log.info("Savings Account List before with memberId {} : {}", memberInfoDTO.getMemberId(), list))
                .flatMap(this::getSavingsAccountInfoForWithdrawStagingData)
                .collectList()
                .map(list -> {
                    list.sort((Comparator.comparing(WithdrawSavingsAccountInfoDTO::getSavingsAccountId)));
                    return list;
                })
                .doOnNext(list -> log.info("Savings Account List after with memberId {} : {}", memberInfoDTO.getMemberId(), list))
                .map(list -> {
                    memberInfoDTO.setSavingsAccountList(list);
                    return memberInfoDTO;
                });
    }

    private Mono<WithdrawSavingsAccountInfoDTO> getSavingsAccountInfoForWithdrawStagingData(WithdrawSavingsAccountInfoDTO savingsAccount){
        return port.getWithdrawStagingDataBySavingsAccountId(savingsAccount.getSavingsAccountId())
                .map(withdrawStagingData -> {
                    gson.fromJson(withdrawStagingData.toString(), WithdrawSavingsAccountInfoDTO.class);
                    savingsAccount.setAmount(withdrawStagingData.getAmount());
                    savingsAccount.setPaymentMode(withdrawStagingData.getPaymentMode());
                    savingsAccount.setUploadedBy(withdrawStagingData.getUploadedBy());
                    savingsAccount.setUploadedOn(withdrawStagingData.getUploadedOn());
                    savingsAccount.setStatus(withdrawStagingData.getStatus());
                    return savingsAccount;
                })
                .doOnNext(savingsAccountInfoDTO -> log.info("savingsAccountInfoDTO: {}", savingsAccountInfoDTO));
    }

    private MemberInfoDTO extractMobileNumberFromMobileDetails(MemberEntity entity) {
        MemberInfoDTO member = mapper.map(entity, MemberInfoDTO.class);
        ArrayList mobileList = gson.fromJson(member.getMobile(), ArrayList.class);
        if (!mobileList.isEmpty()) {
            MobileInfoDTO mobileInfoDTO;
            try {
                mobileInfoDTO = gson.fromJson(mobileList.get(0).toString(), MobileInfoDTO.class);
            } catch (Exception e) {
                log.error("Error in parsing mobile info: {}", e.getMessage());
                mobileInfoDTO = new MobileInfoDTO();
            }
            member.setMobile(mobileInfoDTO.getContactNo());
        }
        return member;
    }

    @Override
    public Mono<WithdrawPaymentResponseDTO> submitWithdrawDataEntity(WithdrawEntitySubmitRequestDto requestDto) {
        return port.getWithdrawDataByOidList(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw staging data not found.")))
                .collectList()
                .filter(dataList -> dataList.size() == requestDto.getId().size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw staging data not found.")))
                .flatMapMany(Flux::fromIterable)
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getCreatedBy()) && data.getCreatedBy().equalsIgnoreCase(requestDto.getLoginId()))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw data can only be submitted by the creator!"));
                })
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getStatus()) && (data.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || data.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Data is already submitted or locked!"));
                })
                .flatMap(withdraw -> this.getManagementProcessIdAndValidateSamityWithdrawDataForSubmission(requestDto.getOfficeId(), withdraw.getSamityId())
                        .flatMap(managementProcessId -> port.updateSubmittedWithdrawData(requestDto.getLoginId(), withdraw.getOid())))
                .as(rxtx::transactional)
                .collectList()
                .map(list -> WithdrawPaymentResponseDTO.builder()
                        .userMessage("Withdraw data submitted successfully.")
                        .build())
                .doOnRequest(req -> log.info("Request for submit withdraw data: {}", req))
                .doOnError(throwable -> log.error("Error in submit withdraw data: {}", throwable.getMessage()));
    }

    @Override
    public Mono<WithdrawPaymentResponseDTO> deleteWithdrawData(WithdrawStagingDataQueryDTO requestDTO) {
        return port.getWithdrawCollectionDataByOid(requestDTO.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No withdraw collection data found for oid: " + requestDTO.getId())))
                .filter(withdrawStagingData -> withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || withdrawStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw data is not eligible to reset!")))
                .flatMap(withdrawStagingData -> port.deleteWithdrawData(requestDTO.getId())
                        .then(port.getAllWithdrawDataByManagementProcessIdAndSamityId(withdrawStagingData.getManagementProcessId(), withdrawStagingData.getSamityId()))
                        .filter(List::isEmpty)
                        .flatMap(withdrawStagingDataList -> samityEventTrackerUseCase.deleteSamityEventTrackerByEventList(withdrawStagingData.getManagementProcessId(), withdrawStagingData.getSamityId(), List.of(SamityEvents.WITHDRAWN.getValue())))
                )
                .as(rxtx::transactional)
                .doOnSuccess(responseDTO -> log.info("Withdraw data deleted successfully"))
                .thenReturn(WithdrawPaymentResponseDTO.builder()
                        .userMessage("Withdraw data deleted successfully.")
                        .build())
                .doOnError(throwable -> log.error("Error in delete withdraw data: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), throwable -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.")));
    }

    @Override
    public Mono<StagingWithdrawData> getWithdrawStagingDataBySavingsAccountId(String savingsAccountId, String managementProcessId) {
        return port.getWithdrawCollectionDataBySavingsAccountIdAndManagementProcessId(savingsAccountId, managementProcessId)
                .doOnError(throwable -> log.error("Error in get withdraw staging data by savings account id: {}", throwable.getMessage()));
    }
}
