package net.celloscope.mraims.loanportfolio.features.authorization.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.IAuthorizationUseCase;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.AuthorizationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.SamityAuthorizationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.authorization.application.service.dto.StatusDTO;
import net.celloscope.mraims.loanportfolio.features.authorization.application.service.dto.StatusVerificationDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.LoanWaiverUseCase;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.LoanRebateUseCase;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateAuthorizeCommand;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.LoanWriteOffAuthorizationCommand;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.WriteOffCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.dto.LoanWriteOffCollectionDTO;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthorizationService implements IAuthorizationUseCase {

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final CollectionStagingDataQueryUseCase collectionUseCase;
    private final IWithdrawStagingDataUseCase withdrawUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final CommonRepository commonRepository;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final TransactionalOperator rxtx;
    private final ISmsNotificationUseCase smsNotificationUseCase;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final LoanRebateUseCase loanRebateUseCase;
    private final LoanWaiverUseCase loanWaiverUseCase;
    private final WriteOffCollectionUseCase writeOffCollectionUseCase;

    public AuthorizationService(TransactionalOperator rxtx, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, IStagingDataUseCase stagingDataUseCase, CollectionStagingDataQueryUseCase collectionUseCase, IWithdrawStagingDataUseCase withdrawUseCase, LoanAdjustmentUseCase loanAdjustmentUseCase, TransactionUseCase transactionUseCase, PassbookUseCase passbookUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, CommonRepository commonRepository, MetaPropertyUseCase metaPropertyUseCase, ISmsNotificationUseCase smsNotificationUseCase, ModelMapper modelMapper, LoanRebateUseCase loanRebateUseCase, LoanWaiverUseCase loanWaiverUseCase, WriteOffCollectionUseCase writeOffCollectionUseCase) {
//        ReactiveTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory);
        this.rxtx = rxtx;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.collectionUseCase = collectionUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.commonRepository = commonRepository;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.smsNotificationUseCase = smsNotificationUseCase;
        this.modelMapper = modelMapper;
        this.loanRebateUseCase = loanRebateUseCase;
        this.loanWaiverUseCase = loanWaiverUseCase;
        this.writeOffCollectionUseCase = writeOffCollectionUseCase;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<AuthorizationGridViewResponseDTO> gridViewOfAuthorization(AuthorizationRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
//                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getOfficeEventByStatusForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), OfficeEvents.STAGED.getValue())
//                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
//                        .map(officeEventTracker -> managementProcessTracker))
                .flatMap(managementProcessTracker -> this.getGridViewOfSamityResponse(managementProcessTracker, requestDTO)
                        .map(samityDTOList -> Tuples.of(samityDTOList, managementProcessTracker)))
                .doOnNext(objects -> log.info("Samity List: {}", objects.getT1()))
                .flatMap(tuple -> Mono.zip(Mono.just(tuple.getT1()), Mono.just(tuple.getT2()), setBtnAuthorizationWithZeroCollection(tuple.getT2()), setBtnUnauthorizationWithZeroCollection(tuple.getT2())))
                .map(tuple -> AuthorizationGridViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(tuple.getT2().getBusinessDate())
                        .businessDay(tuple.getT2().getBusinessDay())
                        .data(tuple.getT1())
                        .totalCount(tuple.getT1().size())
                        .btnAuthorizationWithZeroCollectionEnabled(tuple.getT3())
                        .btnUnauthorizationWithZeroCollectionEnabled(tuple.getT4())
                        .build())
                .doOnNext(authorizationGridViewResponseDTO -> log.info("Authorization Grid View Response Total Samity: {}", authorizationGridViewResponseDTO.getTotalCount()))
                .doOnError(throwable -> log.error("Error in Authorization grid view: {}", throwable.getMessage()));
    }

    private Mono<String> setBtnAuthorizationWithZeroCollection(ManagementProcessTracker managementProcessTracker) {
        return commonRepository.getSamityBySamityDay(managementProcessTracker.getBusinessDay())
                .switchIfEmpty(Mono.just(Samity.builder().build()))
                .elementAt(0)
                .flatMap(samity -> {
                    if (!HelperUtil.checkIfNullOrEmpty(samity.getSamityId())) {
                        return Mono.just("No");
                    } else {
                        return samityEventTrackerUseCase.getAllSamityEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                                .map(samityEventTrackerList -> samityEventTrackerList.stream().anyMatch(samityEventTracker -> samityEventTracker.getSamityEvent().equals(SamityEvents.COLLECTED.getValue()) || samityEventTracker.getSamityEvent().equals(SamityEvents.LOAN_ADJUSTED.getValue()) || samityEventTracker.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue()) || samityEventTracker.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())) ? "No" : "Yes");
                    }
                })
                .doOnError(throwable -> log.error("Error in Set Btn Authorization With Zero Collection: {}", throwable.getMessage()));
    }

    private Mono<String> setBtnUnauthorizationWithZeroCollection(ManagementProcessTracker managementProcessTracker) {
        return commonRepository.getSamityBySamityDay(managementProcessTracker.getBusinessDay())
                .switchIfEmpty(Mono.just(Samity.builder().build()))
                .elementAt(0)
                .flatMap(samity -> {
                    if (!HelperUtil.checkIfNullOrEmpty(samity.getSamityId())) {
                        return Mono.just("No");
                    } else {
                        return officeEventTrackerUseCase.getLastOfficeEventForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                                .filter(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                                .switchIfEmpty(Mono.just(OfficeEventTracker.builder().build()))
                                .flatMap(officeEventTracker -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent())) {
                                        return commonRepository.getSamityIdListByOfficeId(managementProcessTracker.getOfficeId())
                                                .flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(), samityId)
                                                        .doOnNext(samityEventTracker -> log.info("Samity Event Tracker: {}", samityEventTracker))
                                                        .filter(samityEventTracker -> StringUtils.isNotBlank(samityEventTracker.getSamityEvent()) && samityEventTracker.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue()))
                                                        .hasElements()
                                                        .map(hasAuthorizedEvent -> Tuples.of(samityId, hasAuthorizedEvent))
                                                )
                                                .collectList()
                                                .map(samityEventStatusList -> samityEventStatusList.stream().allMatch(Tuple2::getT2) ? "Yes" : "No");
                                    }
                                    else {
                                        return Mono.just("No");
                                    }
                                });
                    }
                })
                .doOnError(throwable -> log.error("Error in Set Btn Unauthorization With Zero Collection: {}", throwable.getMessage()));
    }

    @Override
    public Mono<AuthorizationSummaryViewResponseDTO> tabViewOfAuthorization(AuthorizationRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcessTracker = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(tracker -> {
                    managementProcessTracker.set(tracker);
                    return this.getTabViewOfSamityResponse(tracker, requestDTO);
                })
                .map(samityDTOList -> samityDTOList.stream()
                        .peek(samityResponse -> {
                            samityResponse.setBtnLockEnabled(null);
                            samityResponse.setLockedBy(null);
                            samityResponse.setAuthorizedBy(null);
                            samityResponse.setUnauthorizedBy(null);
                        }).toList())
                .doOnNext(samityDTOList -> log.info("samityDTOList: {}", samityDTOList))
                .map(samityDTOList -> AuthorizationSummaryViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .businessDate(managementProcessTracker.get().getBusinessDate())
                        .businessDay(managementProcessTracker.get().getBusinessDay())
//                        .isLocked("Yes")
                        .lockedBy(requestDTO.getLoginId())
                        .samityIdList(samityDTOList.stream().map(AuthorizationGridViewSamityDTO::getSamityId).toList())
                        .regularCollectionSamity(this.buildRegularCollectionSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .specialCollectionSamity(this.buildSpecialCollectionSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .withdrawSamity(this.buildWithdrawSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .loanAdjustmentSamity(this.buildLoanAdjustmentSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .loanRebateSamity(this.buildLoanRebateSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .loanWaiverSamity(this.buildLoanWaiverSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .loanWriteOffCollectionSamity(this.buildLoanWriteOffCollectionSamityResponseForAuthorizationTabView(samityDTOList, managementProcessTracker.get().getBusinessDay()))
                        .build())
                .flatMap(this::getAuthorizationSummaryByProductForTabView)
                .flatMap(responseDTO -> commonRepository.getOfficeEntityByOfficeId(requestDTO.getOfficeId())
                        .map(officeEntity -> {
                            responseDTO.setOfficeNameEn(officeEntity.getOfficeNameEn());
                            responseDTO.setOfficeNameBn(officeEntity.getOfficeNameBn());
                            return responseDTO;
                        }))
                .flatMap(this::setStatusAndBtnStatusForAuthorizationTabView)
                .doOnNext(authorizationSummaryViewResponseDTO -> log.debug("authorizationSummaryViewResponseDTO: {}", authorizationSummaryViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Tab View Of Authorization: {}", throwable.getMessage()));
    }

    private SamityListDTO buildLoanWriteOffCollectionSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityDTOList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> loanWriteOffCollectionSamityList = samityDTOList.stream()
                .filter(samityResponse -> samityResponse.getTotalLoanWriteOffCollectionAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalLoanWriteOffCollectionAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(loanWriteOffCollectionSamityList)
                .totalCount(loanWriteOffCollectionSamityList.size())
                .build();
    }

    private SamityListDTO buildLoanWaiverSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityDTOList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> loanWaiverSamityList = samityDTOList.stream()
                .filter(samityResponse -> samityResponse.getTotalLoanWaivedAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalLoanWaivedAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(loanWaiverSamityList)
                .totalCount(loanWaiverSamityList.size())
                .build();
    }

    private SamityListDTO buildLoanRebateSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityDTOList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> loanRebateSamityList = samityDTOList.stream()
                .filter(samityResponse -> samityResponse.getTotalLoanRebateAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalLoanRebateAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(loanRebateSamityList)
                .totalCount(loanRebateSamityList.size())
                .build();
    }

    private Mono<AuthorizationSummaryViewResponseDTO> setStatusAndBtnStatusForAuthorizationTabView(AuthorizationSummaryViewResponseDTO responseDTO) {
        AtomicReference<String> managementProcessId = new AtomicReference<>();
        if (responseDTO.getSamityIdList().isEmpty()) {
            responseDTO.setStatus("Authorization Unavailable");
            responseDTO.setBtnAuthorizeEnabled("No");
            responseDTO.setBtnRejectEnabled("No");
            responseDTO.setBtnUnauthorizeEnabled("No");
            responseDTO.setUserMessage("Authorization Process Cannot be Run For Empty Samity List");
            return Mono.just(responseDTO);
        }
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .doOnNext(managementProcessTracker -> managementProcessId.set(managementProcessTracker.getManagementProcessId()))
                .flatMap(managementProcessTracker -> Flux.fromIterable(responseDTO.getSamityIdList())
                        .flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId.get(), samityId)
                                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                                .map(SamityEventTracker::getSamityEvent)
                                .collectList()
                                .map(samityEventList -> !samityEventList.isEmpty() && samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())) ? "Authorization Completed" : "Authorization Incomplete"))
                        .collectList()
                        .zipWith(officeEventTrackerUseCase.getAllOfficeEventsForManagementProcessId(managementProcessTracker.getManagementProcessId())
                                .map(officeEventTrackers -> officeEventTrackers.stream().map(OfficeEventTracker::getOfficeEvent).toList()))
                        .map(tuple2 -> {
                            List<String> samityEvents = tuple2.getT1();
                            List<String> officeEvents = tuple2.getT2();

                            if (samityEvents.stream().allMatch(s -> s.equals("Authorization Completed"))) {
                                responseDTO.setStatus("Authorization Completed");
                                responseDTO.setBtnAuthorizeEnabled("No");
                                responseDTO.setBtnRejectEnabled("No");
                                responseDTO.setBtnUnauthorizeEnabled("Yes");
                                responseDTO.setUserMessage("Authorization Process is Completed For Samity List");
                                if (officeEvents.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))) {
                                    responseDTO.setBtnUnauthorizeEnabled("No");
                                    responseDTO.setUserMessage("Authorization Process is Completed For Samity List and AutoVoucher is Generated");
                                }
                            } else {
                                responseDTO.setStatus("Authorization Incomplete");
                                responseDTO.setBtnAuthorizeEnabled("Yes");
                                responseDTO.setBtnRejectEnabled("Yes");
                                responseDTO.setBtnUnauthorizeEnabled("No");
                                responseDTO.setUserMessage("Authorization Process is Incomplete For Samity List");
                            }
                            return responseDTO;
                        }));
    }

    private SamityListDTO buildLoanAdjustmentSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityDTOList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> loanAdjustedSamityList = samityDTOList.stream()
                .filter(samityResponse -> samityResponse.getTotalLoanAdjustmentAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalLoanAdjustmentAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(loanAdjustedSamityList)
                .totalCount(loanAdjustedSamityList.size())
                .build();
    }

    private Mono<AuthorizationSummaryViewResponseDTO> getAuthorizationSummaryByProductForTabView(AuthorizationSummaryViewResponseDTO authorizationSummaryViewResponseDTO) {
        return stagingDataUseCase.getAllStagingAccountDataBySamityIdList(authorizationSummaryViewResponseDTO.getSamityIdList())
                .doOnNext(stagingAccountDataList -> log.debug("Staging Account Data List: {}", stagingAccountDataList))
                .flatMap(stagingAccountDataList -> this.getProductSummaryForAuthorizationTabView(stagingAccountDataList, authorizationSummaryViewResponseDTO.getSamityIdList()))
                .map(productListTuple -> {
                    SummaryDTO summaryDTO = SummaryDTO.builder()
                            .loanCollectionSummary(productListTuple.getT1())
                            .savingsCollectionSummary(productListTuple.getT2())
                            .withdrawSummary(productListTuple.getT3())
                            .loanAdjustmentSummary(productListTuple.getT4())
                            .loanRebateSummary(productListTuple.getT5())
                            .loanWaiverSummary(productListTuple.getT6())
                            .loanWriteOffCollectionSummary(productListTuple.getT7())
                            .build();
                    authorizationSummaryViewResponseDTO.setSummary(summaryDTO);
                    return authorizationSummaryViewResponseDTO;
                });
    }

    private Mono<Tuple7<ProductListDTO, ProductListDTO, ProductListDTO, ProductListDTO, ProductListDTO, ProductListDTO, ProductListDTO>> getProductSummaryForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        return this.getLoanCollectionSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                .flatMap(loanProductListDTO -> this.getSavingsCollectionSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(savingsProductListDTO -> Tuples.of(loanProductListDTO, savingsProductListDTO)))
                .flatMap(tuple -> this.getSavingsWithdrawSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(withdrawProductListDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), withdrawProductListDTO)))
                .flatMap(tuple -> this.getLoanAdjustmentSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(loanAdjustmentProductListDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), loanAdjustmentProductListDTO)))
                .flatMap(tuple -> this.getLoanRebateSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(loanRebateProductListDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), loanRebateProductListDTO)))
                .flatMap(tuple -> this.getLoanWaiverSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(loanWaiverProductListDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), loanWaiverProductListDTO)))
                .flatMap(tuple -> this.getLoanWriteOffCollectionSummaryByProductForAuthorizationTabView(stagingAccountDataList, samityIdList)
                        .map(loanWriteOffCollectionProductListDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), loanWriteOffCollectionProductListDTO)));
    }

    private Mono<ProductListDTO> getLoanWriteOffCollectionSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getProductCode())
                                .productNameEn(stagingAccountData.getProductNameEn())
                                .productNameBn(stagingAccountData.getProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getLoanAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getProductCode()).getAccountIdList().add(stagingAccountData.getLoanAccountId());
                    }
                });
        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return writeOffCollectionUseCase.getAllLoanWriteOffCollectionDataBySamityIdList(samityIdList)
                .map(loanWriteOffCollectionDTOList -> {
                    loanWriteOffCollectionDTOList.forEach(loanWriteOffCollectionDTO -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(loanWriteOffCollectionDTO.getLoanAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(loanWriteOffCollectionDTO.getWriteOffCollectionAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getLoanWaiverSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getProductCode())
                                .productNameEn(stagingAccountData.getProductNameEn())
                                .productNameBn(stagingAccountData.getProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getLoanAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getProductCode()).getAccountIdList().add(stagingAccountData.getLoanAccountId());
                    }
                });
        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return loanWaiverUseCase.getAllLoanWaiverDataBySamityIdList(samityIdList)
                .map(loanWaiverDTOList -> {
                    loanWaiverDTOList.forEach(loanWaiverDTO -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(loanWaiverDTO.getLoanAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(loanWaiverDTO.getWaivedAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getLoanRebateSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getProductCode())
                                .productNameEn(stagingAccountData.getProductNameEn())
                                .productNameBn(stagingAccountData.getProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getLoanAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getProductCode()).getAccountIdList().add(stagingAccountData.getLoanAccountId());
                    }
                });
        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return loanRebateUseCase.getAllLoanRebateDataBySamityIdList(samityIdList)
                .map(loanRebateDTOList -> {
                    loanRebateDTOList.forEach(loanRebateDTO -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(loanRebateDTO.getLoanAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(loanRebateDTO.getRebateAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getLoanCollectionSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getProductCode())
                                .productNameEn(stagingAccountData.getProductNameEn())
                                .productNameBn(stagingAccountData.getProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getLoanAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getProductCode()).getAccountIdList().add(stagingAccountData.getLoanAccountId());
                    }
                });
        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return collectionUseCase.getAllCollectionDataBySamityIdList(samityIdList)
                .map(collectionStagingDataList -> collectionStagingDataList.stream()
                        .filter(collectionStagingData -> collectionStagingData.getAccountType().equals("Loan"))
                        .toList())
                .map(collectionStagingDataList -> {
                    collectionStagingDataList.forEach(collectionStagingData -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(collectionStagingData.getLoanAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(collectionStagingData.getAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getLoanAdjustmentSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getProductCode())
                                .productNameEn(stagingAccountData.getProductNameEn())
                                .productNameBn(stagingAccountData.getProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getLoanAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getProductCode()).getAccountIdList().add(stagingAccountData.getLoanAccountId());
                    }
                });
        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return loanAdjustmentUseCase.getAllLoanAdjustmentDataBySamityIdList(samityIdList)
                .map(loanAdjustmentDataList -> loanAdjustmentDataList.stream()
                        .filter(collectionStagingData -> collectionStagingData.getAccountType().equals("Loan"))
                        .toList())
                .map(loanAdjustmentDataList -> {
                    loanAdjustmentDataList.forEach(loanAdjustmentData -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(loanAdjustmentData.getLoanAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(loanAdjustmentData.getAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getSavingsCollectionSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = this.buildSavingsProductListFromStagingAccountData(stagingAccountDataList);
//        log.debug("productSummaryHelperMap: {}", productSummaryHelperMap);
        return collectionUseCase.getAllCollectionDataBySamityIdList(samityIdList)
                .map(collectionStagingDataList -> collectionStagingDataList.stream()
                        .filter(collectionStagingData -> collectionStagingData.getAccountType().equals("Savings"))
                        .toList())
                .map(collectionStagingDataList -> {
                    collectionStagingDataList.forEach(collectionStagingData -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(collectionStagingData.getSavingsAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(collectionStagingData.getAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Mono<ProductListDTO> getSavingsWithdrawSummaryByProductForAuthorizationTabView(List<StagingAccountData> stagingAccountDataList, List<String> samityIdList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = this.buildSavingsProductListFromStagingAccountData(stagingAccountDataList);
        return withdrawUseCase.getAllWithdrawDataBySamityIdList(samityIdList)
                .map(stagingWithdrawDataList -> {
                    stagingWithdrawDataList.forEach(stagingWithdrawData -> {
                        productSummaryHelperMap.forEach((productId, productSummaryHelper) -> {
                            if (productSummaryHelper.getAccountIdList().contains(stagingWithdrawData.getSavingsAccountId())) {
                                productSummaryHelper.setAmount(productSummaryHelper.getAmount().add(stagingWithdrawData.getAmount()));
                            }
                        });
                    });
                    return this.buildProductListDTOFromSummaryHelperMap(productSummaryHelperMap);
                });
    }

    private Map<String, ProductSummaryHelperDTO> buildSavingsProductListFromStagingAccountData(List<StagingAccountData> stagingAccountDataList) {
        Map<String, ProductSummaryHelperDTO> productSummaryHelperMap = new HashMap<>();
        stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId()))
                .forEach(stagingAccountData -> {
                    if (productSummaryHelperMap.isEmpty() || !productSummaryHelperMap.containsKey(stagingAccountData.getSavingsProductCode())) {
                        ProductSummaryHelperDTO productSummaryHelperDTO = ProductSummaryHelperDTO.builder()
                                .productId(stagingAccountData.getSavingsProductCode())
                                .productNameEn(stagingAccountData.getSavingsProductNameEn())
                                .productNameBn(stagingAccountData.getSavingsProductNameBn())
                                .accountIdList(new ArrayList<>())
                                .amount(BigDecimal.ZERO)
                                .build();
                        productSummaryHelperDTO.getAccountIdList().add(stagingAccountData.getSavingsAccountId());
                        productSummaryHelperMap.put(productSummaryHelperDTO.getProductId(), productSummaryHelperDTO);
                    } else {
                        productSummaryHelperMap.get(stagingAccountData.getSavingsProductCode()).getAccountIdList().add(stagingAccountData.getSavingsAccountId());
                    }
                });
        return productSummaryHelperMap;
    }

    private ProductListDTO buildProductListDTOFromSummaryHelperMap(Map<String, ProductSummaryHelperDTO> productSummaryHelperMap) {
        List<ProductSummaryDTO> productSummaryDTOList = productSummaryHelperMap.values().stream()
                .map(productSummaryHelperDTO -> gson.fromJson(productSummaryHelperDTO.toString(), ProductSummaryDTO.class))
                .filter(productSummaryDTO -> productSummaryDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        return ProductListDTO.builder()
                .productSummaryList(productSummaryDTOList)
                .totalAmount(productSummaryDTOList.stream()
                        .map(ProductSummaryDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalCount(productSummaryDTOList.size())
                .build();
    }

    private SamityListDTO buildRegularCollectionSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityResponseList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> regularSamityList = samityResponseList.stream()
                .filter(samityResponse -> samityResponse.getSamityDay().equals(businessDay))
                .filter(samityResponse -> samityResponse.getTotalCollectionAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalCollectionAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(regularSamityList)
                .totalCount(regularSamityList.size())
                .build();
    }

    private SamityListDTO buildSpecialCollectionSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityResponseList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> specialSamityList = samityResponseList.stream()
                .filter(samityResponse -> !samityResponse.getSamityDay().equals(businessDay))
                .filter(samityResponse -> samityResponse.getTotalCollectionAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalCollectionAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(specialSamityList)
                .totalCount(specialSamityList.size())
                .build();
    }

    private SamityListDTO buildWithdrawSamityResponseForAuthorizationTabView(List<AuthorizationGridViewSamityDTO> samityResponseList, String businessDay) {
        List<AuthorizationTabViewSamityDTO> withdrawSamityList = samityResponseList.stream()
                .filter(samityResponse -> samityResponse.getTotalWithdrawAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(samityResponse -> {
                    AuthorizationTabViewSamityDTO authorizationTabViewSamityDTO = gson.fromJson(samityResponse.toString(), AuthorizationTabViewSamityDTO.class);
                    authorizationTabViewSamityDTO.setTotalAmount(samityResponse.getTotalWithdrawAmount());
                    return authorizationTabViewSamityDTO;
                })
                .sorted(Comparator.comparing(AuthorizationTabViewSamityDTO::getSamityId))
                .toList();
        return SamityListDTO.builder()
                .data(withdrawSamityList)
                .totalCount(withdrawSamityList.size())
                .build();
    }

    private Mono<List<AuthorizationGridViewSamityDTO>> getTabViewOfSamityResponse(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        return collectionUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                .flatMap(collectionSamityIdList -> withdrawUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                        .flatMap(withdrawSamityIdList -> loanAdjustmentUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                                .flatMap(loanAdjustmentSamityIdList -> loanRebateUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                                        .flatMap(loanRebateSamityIdList -> loanWaiverUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                                                .flatMap(loanWaiverSamityIdList -> writeOffCollectionUseCase.getSamityIdListLockedByUserForAuthorization(requestDTO.getLoginId())
                                                        .map(writeOffSamityIdList -> {
                                                            collectionSamityIdList.addAll(withdrawSamityIdList);
                                                            collectionSamityIdList.addAll(loanAdjustmentSamityIdList);
                                                            collectionSamityIdList.addAll(loanRebateSamityIdList);
                                                            collectionSamityIdList.addAll(loanWaiverSamityIdList);
                                                            collectionSamityIdList.addAll(writeOffSamityIdList);
                                                            return collectionSamityIdList.stream().distinct().toList();
                                                        }))))))
                .flatMapIterable(samityIdList -> samityIdList)
                .flatMap(samityId -> stagingDataUseCase.getStagingProcessEntityForSamity(managementProcessTracker.getManagementProcessId(), samityId)
                        .map(trackerEntity -> gson.fromJson(trackerEntity.toString(), AuthorizationGridViewSamityDTO.class)))
                .flatMap((AuthorizationGridViewSamityDTO samityDTO) -> getCollectionWithdrawAndLoanAdjustmentDetailsForSamity(samityDTO, managementProcessTracker))
                .collectList();
    }

    @Override
    public Mono<AuthorizationResponseDTO> lockSamityListForAuthorization(AuthorizationRequestDTO requestDTO) {
        AtomicReference<String> managementProcessIdForOffice = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcessIdForOffice::set)
                .flatMapMany(managementProcessId -> this.validateSamityForAuthorization(managementProcessId, requestDTO.getSamityIdList()))
                .flatMap(samityId -> collectionUseCase.lockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> withdrawUseCase.lockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> loanAdjustmentUseCase.lockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> loanRebateUseCase.lockSamityForAuthorization(samityId, managementProcessIdForOffice.get(), requestDTO.getLoginId()))
                .flatMap(samityId -> loanWaiverUseCase.lockSamityForAuthorization(samityId, managementProcessIdForOffice.get(), requestDTO.getLoginId()))
                .flatMap(samityId -> writeOffCollectionUseCase.lockSamityForAuthorization(samityId, managementProcessIdForOffice.get(), requestDTO.getLoginId()))
                .collectList()
                .as(this.rxtx::transactional)
                .map(list -> AuthorizationResponseDTO.builder()
                        .userMessage("SamityList is Locked For Authorization")
                        .build())
                .doOnNext(response -> log.info("Lock Samity for Authorization: {}", response))
                .doOnError(throwable -> log.error("Error Locking Samity For Authorization: {}", throwable.getMessage()));
    }

    @Override
    public Mono<AuthorizationResponseDTO> unlockSamityListForAuthorization(AuthorizationRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(requestDTO.getOfficeId())
                .flatMapMany(managementProcessId -> this.validateSamityForAuthorization(managementProcessId, requestDTO.getSamityIdList()))
                .flatMap(samityId -> collectionUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> withdrawUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> loanAdjustmentUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> loanRebateUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> loanWaiverUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .flatMap(samityId -> writeOffCollectionUseCase.unlockSamityForAuthorization(samityId, requestDTO.getLoginId()))
                .collectList()
                .as(this.rxtx::transactional)
                .map(list -> AuthorizationResponseDTO.builder()
                        .userMessage("SamityList is Unlocked For Authorization")
                        .build())
                .doOnNext(response -> log.info("Unlock Samity for Authorization: {}", response))
                .doOnError(throwable -> log.error("Error Unlocking Samity For Authorization: {}", throwable.getMessage()));
    }

    @Override
    public Mono<AuthorizationResponseDTO> authorizeSamityList(AuthorizationRequestDTO requestDTO) {
        return this.validateOfficeForAuthorizationProcessAndGetManagementProcess(requestDTO)
                .flatMap(managementProcessTracker -> {
                    if (!requestDTO.getSamityIdList().isEmpty())
                        return this.validateAndAuthorizeSamityList(managementProcessTracker, requestDTO);
                    else
                        return this.authorizeForZeroCollection(managementProcessTracker, requestDTO);
                })
                .as(rxtx::transactional)
                .map(response -> AuthorizationResponseDTO.builder()
                        .userMessage("Samity List is Authorized Successfully")
                        .build())
                .doOnError(throwable -> log.error("Error in Authorization: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Authorization Response: {}", response));
    }

    @Override
    public Mono<AuthorizationResponseDTO> authorizeSamityListMigration(AuthorizationRequestDTO requestDTO) {
        return this.validateOfficeForAuthorizationProcessAndGetManagementProcess(requestDTO)
                .flatMap(managementProcessTracker -> this.validateAndAuthorizeSamityList(managementProcessTracker, requestDTO))
//                .as(rxtx::transactional)
                .map(response -> AuthorizationResponseDTO.builder()
                        .userMessage("Samity List is Authorized Successfully")
                        .build())
                .doOnError(throwable -> log.error("Error in Authorization: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Authorization Response: {}", response));
    }

    @Override
    public Mono<AuthorizationResponseDTO> rejectSamityList(AuthorizationRequestDTO requestDTO) {
        return this.validateOfficeForAuthorizationProcessAndGetManagementProcess(requestDTO)
                .flatMap(managementProcessTracker -> this.validateAndRejectSamityList(managementProcessTracker, requestDTO))
                .as(rxtx::transactional)
                .map(response -> AuthorizationResponseDTO.builder()
                        .userMessage("Samity List is Rejected Successfully")
                        .build())
                .doOnError(throwable -> log.info("Error in Rejection Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Rejection Response: {}", response));
    }

    @Override
    public Mono<AuthorizationResponseDTO> unauthorizeSamityList(AuthorizationRequestDTO requestDTO) {
        return this.validateOfficeForAuthorizationProcessAndGetManagementProcess(requestDTO)
                .flatMap(managementProcessTracker -> {
                    if(!requestDTO.getSamityIdList().isEmpty()){
                        return this.validateAndUnauthorizeSamityList(managementProcessTracker, requestDTO);
                    }
                    else
                        return this.validateAndUnauthorizeSamityForZeroCollection(managementProcessTracker, requestDTO);
                })
                .as(rxtx::transactional)
                .map(response -> AuthorizationResponseDTO.builder()
                        .userMessage("Samity List is Unauthorized Successfully")
                        .build())
                .doOnError(throwable -> log.error("Error in Unauthorization Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Unauthorization Response: {}", response));
    }

    private Mono<List<String>> validateAndUnauthorizeSamityList(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        return Flux.fromIterable(requestDTO.getSamityIdList())
                .doOnNext(samityId -> log.info("Samity Id: {} Unauthorization Process Started", samityId))
                .flatMap(samityId -> this.validateAndUnauthorizeOneSamity(SamityAuthorizationRequestDTO.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
                        .officeId(managementProcessTracker.getOfficeId())
                        .loginId(requestDTO.getLoginId())
                        .samityId(samityId)
                        .build()))
                .collectList()
                .map(response -> requestDTO.getSamityIdList());
    }

    private Mono<List<String>> validateAndUnauthorizeSamityForZeroCollection(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        return samityEventTrackerUseCase.getAllSamityEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                .flatMapIterable(samityEventTrackerList -> samityEventTrackerList)
                .filter(samityEventTracker -> samityEventTracker.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Authorized Samity Found For Unauthorization")))
                .map(SamityEventTracker::getSamityEventTrackerId)
                .collectList()
                .flatMap(samityEventTrackerUseCase::deleteSamityEventTrackerByEventTrackerIdList);
    }

    private Mono<String> validateAndUnauthorizeOneSamity(SamityAuthorizationRequestDTO requestDTO) {
        log.info("Samity: {} Unauthorization Process Started", requestDTO.getSamityId());
        return this.validateSamityForUnauthorizationProcessAndGetSamityEventList(requestDTO)
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.COLLECTED.getValue()))) {
                        return this.unauthorizeSamityForCollection(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.WITHDRAWN.getValue()))) {
                        return this.unauthorizeSamityForWithdraw(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.LOAN_ADJUSTED.getValue()))) {
                        return this.unauthorizeSamityForLoanAdjustment(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> this.deleteTransactionAndPassbookEntryAndUpdateRepaymentScheduleForSamityUnauthorization(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId()))
                .flatMap(samityId -> this.unAuthorizeRebateForSamity(requestDTO))
                .flatMap(aBoolean -> this.unAuthorizeWriteOffForSamity(requestDTO))
                .flatMap(aBoolean -> samityEventTrackerUseCase.deleteSamityEventTrackerByEventList(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), List.of(SamityEvents.AUTHORIZED.getValue(), SamityEvents.TRANSACTION_COMPLETED.getValue(), SamityEvents.PASSBOOK_COMPLETED.getValue())))
                .map(samityEvents -> requestDTO.getSamityId())
                .doOnNext(samityId -> log.info("Samity: {} unauthorization Process Completed", requestDTO.getSamityId()));
    }


    private Mono<Boolean> unAuthorizeRebateForSamity(SamityAuthorizationRequestDTO requestDTO) {
        return loanRebateUseCase.getLoanRebateDataBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(loanRebateUseCase::updateLoanRebateDataOnUnAuthorization)
                .flatMap(loanRebateDTO -> loanRepaymentScheduleUseCase.revertRepaymentScheduleByManagementProcessIdAndLoanAccountId(requestDTO.getManagementProcessId(), loanRebateDTO.getLoanAccountId()))
                .collectList()
                .map(loanRebateDTOList -> !loanRebateDTOList.isEmpty());
    }

    private Mono<Boolean> unAuthorizeWriteOffForSamity(SamityAuthorizationRequestDTO requestDTO) {
        return writeOffCollectionUseCase.getLoanWriteOffDataBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(writeOffCollectionUseCase::updateLoanWriteOffDataOnUnAuthorization)
                .collectList()
                .map(loanRebateDTOList -> !loanRebateDTOList.isEmpty());
    }

    private Mono<String> unauthorizeSamityForLoanAdjustment(String managementProcessId, String providedSamityId, String loginId) {
        return loanAdjustmentUseCase.validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(managementProcessId, providedSamityId, loginId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Adjustment Data Updated For Unauthorization", samityId));
    }

    private Mono<String> unauthorizeSamityForWithdraw(String managementProcessId, String providedSamityId, String loginId) {
        return withdrawUseCase.validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(managementProcessId, providedSamityId, loginId)
                .doOnNext(samityId -> log.info("Samity Id: {} Withdraw Data Updated For Unauthorization", samityId));
    }

    private Mono<String> unauthorizeSamityForCollection(String managementProcessId, String providedSamityId, String loginId) {
        return collectionUseCase.validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(managementProcessId, providedSamityId, loginId)
                .doOnNext(samityId -> log.info("Samity Id: {} Collection Data Updated For Unauthorization", samityId));
    }

    private Mono<String> deleteTransactionAndPassbookEntryAndUpdateRepaymentScheduleForSamityUnauthorization(String managementProcessId, String samityId, String loginId) {
        return this.deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(managementProcessId, samityId)
                .doOnNext(loanRepayScheduleIdList -> log.info("Passbook Entry Deleted For Samity: {}", samityId))
                .doOnNext(loanRepayScheduleIdList -> log.info("Loan Repayment Schedule Id List to Update Status to 'Pending': {}", loanRepayScheduleIdList))
                .flatMap(loanRepaymentScheduleUseCase::updateInstallmentStatusToPending)
                .doOnNext(loanRepayScheduleIdList -> log.info("Loan Repayment Schedule Status Updated For Samity: {}", samityId))
                .flatMap(loanRepayScheduleIdList -> this.deleteTransactionsForSamityUnauthorization(managementProcessId, samityId))
                .doOnNext(response -> log.info("Transaction Entry Deleted For Samity: {}", samityId))
                .map(response -> samityId);
    }


    private Mono<List<String>> deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(managementProcessId, samityId, SamityEvents.PASSBOOK_COMPLETED.getValue())
                .map(SamityEventTracker::getSamityEventTrackerId)
                .flatMap(passbookProcessId -> passbookUseCase.deletePassbookEntriesAndGetLoanRepayScheduleIdListForSamityUnauthorization(managementProcessId, passbookProcessId));
    }

    private Mono<String> deleteTransactionsForSamityUnauthorization(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(managementProcessId, samityId, SamityEvents.TRANSACTION_COMPLETED.getValue())
                .map(SamityEventTracker::getSamityEventTrackerId)
                .flatMap(transactionProcessId -> transactionUseCase.deleteTransactionsForSamityUnauthorization(managementProcessId, transactionProcessId));
    }

    private Mono<List<String>> validateSamityForUnauthorizationProcessAndGetSamityEventList(SamityAuthorizationRequestDTO requestDTO) {
        return this.getSamityEventsAndCheckIfSamityIsValidForAuthorizationProcess(requestDTO)
                .filter(samityEvents -> samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id: " + requestDTO.getSamityId() + " is Not Authorized")))
                .doOnNext(samityEvents -> log.info("Samity {} Unauthorization validation Completed", requestDTO.getSamityId()));
    }

    private Mono<ManagementProcessTracker> validateAndRejectSamityList(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        return Flux.fromIterable(requestDTO.getSamityIdList())
                .doOnNext(samityId -> log.info("Samity Id: {} Rejection Process Started", samityId))
                .flatMap(samityId -> this.validateAndRejectOneSamity(SamityAuthorizationRequestDTO.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
                        .officeId(managementProcessTracker.getOfficeId())
                        .loginId(requestDTO.getLoginId())
                        .samityId(samityId)
                        .build()))
                .collectList()
                .map(response -> managementProcessTracker);
    }

    private Mono<String> validateAndRejectOneSamity(SamityAuthorizationRequestDTO requestDTO) {
        return this.validateSamityForAuthorizationOrRejectionProcessAndGetSamityEventList(requestDTO)
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.COLLECTED.getValue()))) {
                        return this.rejectSamityForCollection(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.WITHDRAWN.getValue()))) {
                        return this.rejectSamityForWithdraw(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if (samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.LOAN_ADJUSTED.getValue()))) {
                        return this.rejectSamityForLoanAdjustment(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> rejectSamityForLoanRebateIfExists(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId()))
                .flatMap(samityEvents -> rejectSamityForLoanWaiverIfExists(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId()))
                .flatMap(samityEvents -> rejectSamityForLoanWriteOffCollectionIfExists(requestDTO.getManagementProcessId(), requestDTO.getSamityId(), requestDTO.getLoginId()))
                .map(samityEvents -> requestDTO.getSamityId());
    }

    private Mono<String> rejectSamityForLoanWriteOffCollectionIfExists(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Write Off Collection Rejection Process Started", samityId))
                .flatMap(samityId -> writeOffCollectionUseCase.validateAndUpdateLoanWriteOffCOllectionDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Write Off Collection Rejection Process Completed", samityId))
                .switchIfEmpty(Mono.just(providedSamityId));
    }

    private Mono<String> rejectSamityForLoanAdjustment(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Adjustment Rejection Process Started", samityId))
                .flatMap(samityId -> loanAdjustmentUseCase.validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Adjustment Rejection Process Completed", samityId));
    }

    private Mono<String> rejectSamityForLoanRebateIfExists(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Rebate Rejection Process Started", samityId))
                .flatMap(samityId -> loanRebateUseCase.validateAndUpdateLoanRebateDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Rebate Rejection Process Completed", samityId))
                .switchIfEmpty(Mono.just(providedSamityId));
    }

    private Mono<String> rejectSamityForLoanWaiverIfExists(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Waiver Rejection Process Started", samityId))
                .flatMap(samityId -> loanWaiverUseCase.validateAndUpdateLoanWaiverDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Waiver Rejection Process Completed", samityId))
                .switchIfEmpty(Mono.just(providedSamityId));
    }

    private Mono<String> rejectSamityForWithdraw(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Withdraw Rejection Process Started", samityId))
                .flatMap(samityId -> withdrawUseCase.validateAndUpdateWithdrawStagingDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Withdraw Rejection Process Completed", samityId));
    }

    private Mono<String> rejectSamityForCollection(String managementProcessId, String providedSamityId, String loginId) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Collection Rejection Process Started", samityId))
                .flatMap(samityId -> collectionUseCase.validateAndUpdateCollectionStagingDataForRejectionBySamityId(managementProcessId, samityId, loginId))
                .doOnNext(samityId -> log.info("Samity Id: {} Collection Rejection Process Completed", samityId));
    }

    private Mono<ManagementProcessTracker> validateOfficeForAuthorizationProcessAndGetManagementProcess(AuthorizationRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .collectList()
                        .filter(officeEventTrackerList -> officeEventTrackerList.stream().anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated for office")))
                        .filter(officeEventTrackerList -> officeEventTrackerList.stream().noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed for office")))
                        .map(officeEventTrackerList -> managementProcessTracker));
    }

    private Mono<List<SMSNotificationMetaProperty>> getSMSNotificationMetaProperty() {
        return metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SMS_NOTIFICATION_META_PROPERTY_ID.getValue())
                .filter(metaPropertyResponseDTO -> !HelperUtil.checkIfNullOrEmpty(metaPropertyResponseDTO.getPropertyId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Meta Property Found For SMS Notification Service")))
                .handle((metaPropertyResponseDTO, sink) -> {
                    ModelMapper modelMapper = new ModelMapper();
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList;
                    try {
                        smsNotificationMetaPropertyList = new ArrayList<>(objectMapper.readValue(
                                metaPropertyResponseDTO.getParameters(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, SMSNotificationMetaProperty.class)
                        ));
                    } catch (JsonProcessingException e) {
                        sink.error(new RuntimeException(e));
                        return;
                    }
//                    log.info("Meta Property Value: {}", gson.toJson(metaPropertyResponseDTO.getParameters()));
//                    List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList = gson.fromJson(metaPropertyResponseDTO.getParameters(), ArrayList.class);
                    sink.next(smsNotificationMetaPropertyList);
                });
    }


    private Mono<ManagementProcessTracker> authorizeForZeroCollection(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        return commonRepository.getSamityIdListByOfficeId(managementProcessTracker.getOfficeId())
                .collectList()
                .flatMap(samityIdList -> samityEventTrackerUseCase.insertSamityListForAnEvent(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), samityIdList, requestDTO.getLoginId(), SamityEvents.AUTHORIZED.getValue(), null))
                .map(response -> managementProcessTracker);
    }

    private Mono<ManagementProcessTracker> validateAndAuthorizeSamityList(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        AtomicReference<List<SMSNotificationMetaProperty>> smsNotificationMetaPropertyList = new AtomicReference<>();
//        return Flux.fromIterable(requestDTO.getSamityIdList())
        return this.getSMSNotificationMetaProperty()
                .doOnNext(smsNotificationMetaPropertyList::set)
                .doOnNext(metaPropertyList -> log.info("SMS Notification Meta Property List: {}", metaPropertyList))
                .flatMapIterable(list -> requestDTO.getSamityIdList())
                .doOnNext(samityId -> log.info("Samity Id: {} authorization Process Started", samityId))
                .concatMap(samityId -> this.validateAndAuthorizeOneSamity(SamityAuthorizationRequestDTO.builder()
                                .managementProcessId(managementProcessTracker.getManagementProcessId())
                                .authorizationProcessId(UUID.randomUUID().toString())
                                .transactionProcessId(UUID.randomUUID().toString())
                                .passbookProcessId(UUID.randomUUID().toString())
                                .officeId(managementProcessTracker.getOfficeId())
                                .mfiId(managementProcessTracker.getMfiId())
                                .loginId(requestDTO.getLoginId())
                                .samityId(samityId)
                                .source(requestDTO.getSource())
                                .build(),
                        smsNotificationMetaPropertyList.get()))
                .collectList()
                .map(response -> managementProcessTracker);
    }

    private Mono<String> validateAndAuthorizeOneSamity(SamityAuthorizationRequestDTO requestDTO, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
//        log.info("Samity Authorization Request: {}", requestDTO);
        return this.validateSamityForAuthorizationOrRejectionProcessAndGetSamityEventList(requestDTO)
                .flatMap(samityEvents -> {
                    if(samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.COLLECTED.getValue()))){
                        return this.authorizeSamityForCollection(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), requestDTO.getLoginId(), smsNotificationMetaPropertyList, requestDTO.getSource())
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if(samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.WITHDRAWN.getValue()))){
                        return this.authorizeSamityForWithdraw(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), requestDTO.getLoginId(), smsNotificationMetaPropertyList)
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> {
                    if(samityEvents.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.LOAN_ADJUSTED.getValue()))){
                        return this.authorizeSamityForLoanAdjustment(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), requestDTO.getLoginId(), requestDTO.getMfiId(), smsNotificationMetaPropertyList)
                                .map(samityId -> samityEvents);
                    }
                    return Mono.just(samityEvents);
                })
                .flatMap(samityEvents -> loanRebateUseCase.getLoanRebateDataBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                        .filter(loanRebateDTOS -> !loanRebateDTOS.isEmpty())
                        .flatMap(loanRebateDTOS -> this.authorizeSamityForLoanRebate(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), requestDTO.getLoginId(), requestDTO.getMfiId(), smsNotificationMetaPropertyList))
                        .switchIfEmpty(Mono.just(requestDTO.getSamityId()))
                        .map(samityId -> samityEvents))
                .flatMap(samityEvents -> writeOffCollectionUseCase.getLoanWriteOffDataBySamityId(requestDTO.getSamityId(), requestDTO.getManagementProcessId())
                        .filter(loanWriteOffCollectionDTOS -> !loanWriteOffCollectionDTOS.isEmpty())
                        .flatMap(loanWriteOffCollectionDTOS -> this.authorizeSamityForLoanWriteOff(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), requestDTO.getLoginId(), requestDTO.getMfiId(), smsNotificationMetaPropertyList))
                        .switchIfEmpty(Mono.just(requestDTO.getSamityId()))
                        .map(samityId -> samityEvents))
                .flatMap(samityEvents -> this.updateSamityEventTrackerForAuthorization(requestDTO));
    }


    private Mono<String> updateSamityEventTrackerForAuthorization(SamityAuthorizationRequestDTO requestDTO) {
        return samityEventTrackerUseCase.insertSamityEvent(requestDTO.getManagementProcessId(), requestDTO.getAuthorizationProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), SamityEvents.AUTHORIZED.getValue(), requestDTO.getLoginId())
                .flatMap(samityEventTracker -> samityEventTrackerUseCase.insertSamityEvent(requestDTO.getManagementProcessId(), requestDTO.getTransactionProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), SamityEvents.TRANSACTION_COMPLETED.getValue(), requestDTO.getLoginId()))
                .flatMap(samityEventTracker -> samityEventTrackerUseCase.insertSamityEvent(requestDTO.getManagementProcessId(), requestDTO.getPassbookProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityId(), SamityEvents.PASSBOOK_COMPLETED.getValue(), requestDTO.getLoginId()))
                .map(samityEventTracker -> requestDTO.getSamityId());
    }

    private Mono<List<String>> validateSamityForAuthorizationOrRejectionProcessAndGetSamityEventList(SamityAuthorizationRequestDTO requestDTO) {
        return this.getSamityEventsAndCheckIfSamityIsValidForAuthorizationProcess(requestDTO)
                .filter(samityEvents -> samityEvents.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id: " + requestDTO.getSamityId() + " is Already Authorized")));
    }

    private Mono<List<String>> getSamityEventsAndCheckIfSamityIsValidForAuthorizationProcess(SamityAuthorizationRequestDTO requestDTO) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(requestDTO.getManagementProcessId(), requestDTO.getSamityId())
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
                .collectList()
                .doOnNext(samityEvents -> log.info("Samity: {} Event List: {}", requestDTO.getSamityId(), samityEvents))
                .filter(samityEvents -> !samityEvents.isEmpty() && samityEvents.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id: " + requestDTO.getSamityId() + " has No Data for Authorization Process")));
    }

    private Mono<String> authorizeSamityForLoanAdjustment(String managementProcessId, String transactionProcessId, String passbookProcessId, String officeId, String providedSamityId, String loginId, String mfiId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Adjustment Authorization Process Started", samityId))
                .flatMap(samityId -> loanAdjustmentUseCase.authorizeLoanAdjustmentForSamity(LoanAdjustmentRequestDTO.builder()
                        .managementProcessId(managementProcessId)
                        .transactionProcessId(transactionProcessId)
                        .passbookProcessId(passbookProcessId)
                        .mfiId(mfiId)
                        .loginId(loginId)
                        .officeId(officeId)
                        .samityId(samityId)
                        .smsNotificationMetaPropertyList(smsNotificationMetaPropertyList)
                        .build()))
                .map(loanAdjustmentResponseDTO -> providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Adjustment Authorization Process Completed", samityId));
    }


    private Mono<String> authorizeSamityForLoanRebate(String managementProcessId, String transactionProcessId, String passbookProcessId, String officeId, String providedSamityId, String loginId, String mfiId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Rebate Authorization Process Started", samityId))
                .flatMap(samityId -> loanRebateUseCase.authorizeSamityForLoanRebate(LoanRebateAuthorizeCommand.builder()
                        .managementProcessId(managementProcessId)
                        .transactionProcessId(transactionProcessId)
                        .passbookProcessId(passbookProcessId)
                        .mfiId(mfiId)
                        .loginId(loginId)
                        .officeId(officeId)
                        .samityId(samityId)
                        .smsNotificationMetaPropertyList(smsNotificationMetaPropertyList)
                        .build()))
                .map(loanAdjustmentResponseDTO -> providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Rebate Authorization Process Completed", samityId));
    }

    private Mono<String> authorizeSamityForLoanWriteOff(String managementProcessId, String transactionProcessId, String passbookProcessId, String officeId, String providedSamityId, String loginId, String mfiId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Write Off Authorization Process Started", samityId))
                .flatMap(samityId -> writeOffCollectionUseCase.authorizeSamityForLoanWriteOff(LoanWriteOffAuthorizationCommand.builder()
                        .managementProcessId(managementProcessId)
                        .transactionProcessId(transactionProcessId)
                        .passbookProcessId(passbookProcessId)
                        .mfiId(mfiId)
                        .loginId(loginId)
                        .officeId(officeId)
                        .samityId(samityId)
                        .smsNotificationMetaPropertyList(smsNotificationMetaPropertyList)
                        .build()))
                .map(loanAdjustmentResponseDTO -> providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Loan Write Off Authorization Process Completed", samityId));
    }

    private Mono<String> authorizeSamityForWithdraw(String managementProcessId, String transactionProcessId, String passbookProcessId, String officeId, String providedSamityId, String loginId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Withdraw Authorization Process Started", samityId))
                .flatMap(samityId -> withdrawUseCase.validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(managementProcessId, samityId, loginId))
                .flatMap(samityId -> this.createTransactionAndPassbookForWithdrawAuthorization(managementProcessId, officeId, samityId, loginId, transactionProcessId, passbookProcessId, smsNotificationMetaPropertyList))
                .doOnNext(samityId -> log.info("Samity Id: {} Withdraw Authorization Process Completed", samityId));
    }

    private Mono<String> createTransactionAndPassbookForWithdrawAuthorization(String managementProcessId, String officeId, String samityId, String loginId, String transactionProcessId, String passbookProcessId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return transactionUseCase.createTransactionForWithdrawBySamityId(managementProcessId, transactionProcessId, samityId, officeId)
                .flatMapMany(transactionResponseDTO -> Flux.fromIterable(transactionResponseDTO.getTransactionList())
                        .flatMap(transaction -> this.createSMSNotificationRequestForTransaction(transaction, smsNotificationMetaPropertyList, loginId)
                                .map(response -> transaction))
                        .flatMap(transaction -> passbookUseCase.createPassbookEntryForSavingsWithdraw(PassbookRequestDTO
                                .builder()
                                .managementProcessId(managementProcessId)
                                .processId(passbookProcessId)
                                .amount(transaction.getAmount())
                                .loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
                                .savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
                                .transactionId(transaction.getTransactionId())
                                .transactionCode(transaction.getTransactionCode())
                                .loginId(loginId)
                                .mfiId(transaction.getMfiId())
                                .transactionDate(transaction.getTransactionDate())
                                .paymentMode(transaction.getPaymentMode())
                                .officeId(officeId)
                                .samityId(transaction.getSamityId())
                                .build())))
                .collectList()
                .map(passbookResponseDTOS -> samityId);
    }

    private Mono<String> authorizeSamityForCollection(String managementProcessId, String transactionProcessId, String passbookProcessId, String officeId, String providedSamityId, String loginId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList, String source) {
        return Mono.just(providedSamityId)
                .doOnNext(samityId -> log.info("Samity Id: {} Collection Authorization Process Started", samityId))
                .flatMap(samityId -> collectionUseCase.validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(managementProcessId, samityId, loginId))
                .flatMap(samityId -> this.createTransactionAndPassbookForCollectionAuthorization(managementProcessId, officeId, samityId, loginId, transactionProcessId, passbookProcessId, smsNotificationMetaPropertyList, source))
                .doOnNext(samityId -> log.info("Samity Id: {} Collection Authorization Process Completed", samityId));
    }

    private Mono<String> createTransactionAndPassbookForCollectionAuthorization(String managementProcessId, String officeId, String samityId, String loginId, String transactionProcessId, String passbookProcessId, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList, String source) {
//        final String transactionProcessId =UUID.randomUUID().toString();
//        final String passbookProcessId =UUID.randomUUID().toString();
        return transactionUseCase.createTransactionForOneSamity(samityId, managementProcessId, transactionProcessId, officeId, source)
                .flatMapMany(transactionResponseDTO -> Flux.fromIterable(transactionResponseDTO.getTransactionList())
                        .flatMap(transaction -> this.createSMSNotificationRequestForTransaction(transaction, smsNotificationMetaPropertyList, loginId)
                                .map(response -> transaction))
                        .flatMap(transaction -> {
                                    if (transaction.getAccountType().equals(Constants.ACCOUNT_TYPE_LOAN.getValue())) {
                                        return passbookUseCase.getRepaymentScheduleAndCreatePassbookEntryForLoan(
                                                PassbookRequestDTO
                                                        .builder()
                                                        .managementProcessId(transaction.getManagementProcessId())
                                                        .processId(passbookProcessId)
                                                        .officeId(officeId)
                                                        .amount(transaction.getAmount())
                                                        .loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
                                                        .savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
                                                        .transactionId(transaction.getTransactionId())
                                                        .transactionCode(transaction.getTransactionCode())
                                                        .loginId(loginId)
                                                        .mfiId(transaction.getMfiId())
                                                        .transactionDate(transaction.getTransactionDate())
                                                        .paymentMode(transaction.getPaymentMode())
                                                        .source(source)
                                                        .samityId(transaction.getSamityId())
                                                        .build());

                                    } else {
                                        return passbookUseCase.createPassbookEntryForSavings(PassbookRequestDTO
                                                .builder()
                                                .managementProcessId(transaction.getManagementProcessId())
                                                .processId(passbookProcessId)
                                                .officeId(officeId)
                                                .amount(transaction.getAmount())
                                                .loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
                                                .savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
                                                .transactionId(transaction.getTransactionId())
                                                .transactionCode(transaction.getTransactionCode())
                                                .loginId(loginId)
                                                .mfiId(transaction.getMfiId())
                                                .transactionDate(transaction.getTransactionDate())
                                                .paymentMode(transaction.getPaymentMode())
                                                .source(source)
                                                .samityId(transaction.getSamityId())
                                                .build());
                                    }

                                }
                        )

                )
                .filter(passbookResponseDTO -> passbookResponseDTO.get(0).getTransactionCode().equals(Constants.TRANSACTION_CODE_LOAN_REPAY.getValue()))
                .map(this::getFullyPaidInstallmentNos)
                .flatMap(tuple2 -> {
                    if (!tuple2.getT2().isEmpty()) {
                        return loanRepaymentScheduleUseCase.updateInstallmentStatus(tuple2.getT2(), Status.STATUS_PAID.getValue(), tuple2.getT1(), managementProcessId);
                    }
                    return Flux.just(RepaymentScheduleResponseDTO.builder().build());
                })
                .collectList()
                .map(repaymentScheduleResponseDTOS -> samityId);
    }

    private Mono<Transaction> createSMSNotificationRequestForTransaction(Transaction transaction, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList, String loginId) {
        return Mono.fromSupplier(() -> smsNotificationMetaPropertyList.stream()
                        .filter(metaProperty -> metaProperty.getType().equalsIgnoreCase(transaction.getTransactionCode()))
                        .findFirst()
                        .get())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SMS Notification Meta Property Not Found for Transaction Code " + transaction.getTransactionCode())))
                .flatMap(smsNotificationMetaProperty -> {
                    if (smsNotificationMetaProperty.getIsSMSNotificationEnabled().equals("Yes")) {
                        return this.createAndSaveSMSNotificationRequest(transaction, smsNotificationMetaProperty, loginId);
                    }
                    return Mono.just(transaction);
                })
                .onErrorResume(throwable -> {
                    log.error("Error in Creating SMS Notification Request: {}", throwable.getMessage());
                    return Mono.just(transaction);
                });
    }

    public Mono<Transaction> createAndSaveSMSNotificationRequest(Transaction transaction, SMSNotificationMetaProperty smsNotificationMetaProperty, String loginId) {
        log.info("Creating and Saving SMS Notification Entry for Transaction with AccountId: {} and transaction Amount: {}", !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId(), transaction.getAmount());
        return stagingDataUseCase.getStagingDataByAccountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                .flatMap(stagingData -> commonRepository.getInstituteOidByMFIId(transaction.getMfiId())
                        .map(instituteOid -> Tuples.of(stagingData, instituteOid)))
                .map(tuples -> {
                    StagingData stagingData = tuples.getT1();
                    MobileInfoDTO mobileInfoDTO = gson.fromJson(gson.fromJson(stagingData.getMobile(), ArrayList.class)
                            .get(0)
                            .toString(), MobileInfoDTO.class);
                    return SmsNotificationRequestDTO.builder()
                            .type(transaction.getTransactionCode())
                            .id(transaction.getTransactionId())
                            .amount(String.valueOf(transaction.getAmount()))
                            .datetime(String.valueOf(transaction.getTransactionDate()))
                            .accountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                            .memberId(stagingData.getMemberId())
                            .mobileNumber(mobileInfoDTO.getContactNo())
                            .template(smsNotificationMetaProperty.getTemplate())
                            .mfiId(transaction.getMfiId())
                            .instituteOid(tuples.getT2())
                            .loginId(loginId)
                            .build();
                })
                /*.doOnNext(smsNotificationRequestDTO -> {
                    log.info("SMS Notification Request DTO: {}", smsNotificationRequestDTO);
                    smsNotificationUseCase.publishSmsRequest(smsNotificationRequestDTO)
                            .subscribeOn(Schedulers.immediate())
                            .subscribe();
                })*/
                .flatMap(smsNotificationRequestDTO -> Mono.deferContextual(contextView -> {
                    // Pass the captured context to the background task
                    return Mono.fromRunnable(() -> {
                                // Restore the context in the background task
                                Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                smsNotificationUseCase.publishSmsRequest(smsNotificationRequestDTO)
                                        .contextWrite(context)
                                        .subscribeOn(Schedulers.immediate())
                                        .subscribe();
                            })
                            .thenReturn(smsNotificationRequestDTO);
                }))
                .map(smsLog -> transaction)
                .onErrorResume(throwable -> {
//                log.error("Error in Creating and Saving SMS Notification Request: {}", throwable.getMessage());
                    return Mono.just(transaction);
                });
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
        log.info("loan account id : {}, Fulfilled installments : {}", tuples, fulfilledInstallments);
        return tuples;
    }

    private boolean isThisInstallmentFullyPaid(PassbookResponseDTO passbookResponseDTO) {
        if (passbookResponseDTO.getScRemainForThisInst() != null && passbookResponseDTO.getPrinRemainForThisInst() != null) {
//            return passbookResponseDTO.getScRemainForThisInst().toString().equals("0.00") && passbookResponseDTO.getPrinRemainForThisInst().toString().equals("0.00");
            return passbookResponseDTO.getScRemainForThisInst().compareTo(BigDecimal.ZERO) == 0 && passbookResponseDTO.getPrinRemainForThisInst().compareTo(BigDecimal.ZERO) == 0;
        } else return false;
    }

    private Mono<ManagementProcessTracker> validateSamityListForAuthorization(AuthorizationRequestDTO requestDTO) {
        log.info("SamityId List: {} Validation Process Started For Authorization", requestDTO.getSamityIdList());
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .collectList()
                        .filter(officeEventTrackerList -> officeEventTrackerList.stream().noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Already Completed For Office")))
                        .map(officeEventTrackerList -> managementProcessTracker))
                .flatMap(managementProcessTracker -> samityEventTrackerUseCase.getAllSamityEventsForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .map(samityEventTrackerList -> samityEventTrackerList.stream()
                                .filter(samityEventTracker -> requestDTO.getSamityIdList().stream().anyMatch(samityId -> samityId.equals(samityEventTracker.getSamityId())))
                                .toList())
                        .doOnNext(samityEventTrackerList -> log.debug("SamityEventTrackerList For Samity List: {}", samityEventTrackerList))
                        .filter(samityEventTrackerList -> samityEventTrackerList.stream().noneMatch(samityEventTracker -> samityEventTracker.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity List is Already Authorized")))
                        .doOnNext(samityEventTrackerList -> log.info("SamityId List: {} is Validated For Authorization", requestDTO.getSamityIdList()))
                        .map(samityEventTrackerList -> managementProcessTracker));
    }

    private Flux<String> validateSamityForAuthorization(String managementProcessId, List<String> samityIdList) {
        return Flux.fromIterable(samityIdList)
                .flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
                        .collectList()
                        .filter(samityEventTrackerList -> samityEventTrackerList.stream().anyMatch(eventTracker -> !HelperUtil.checkIfNullOrEmpty(eventTracker.getSamityEvent()) && (eventTracker.getSamityEvent().equals(SamityEvents.COLLECTED.getValue()) || eventTracker.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue()) || eventTracker.getSamityEvent().equals(SamityEvents.LOAN_ADJUSTED.getValue()) || eventTracker.getSamityEvent().equals(SamityEvents.LOAN_ADJUSTED.getValue()))))
                        .switchIfEmpty(loanRebateUseCase.getAllLoanRebateDataBySamityIdList(samityIdList)
                                .flatMap(loanRebateDTOS -> Mono.just(List.of(SamityEventTracker.builder().build()))))
                        .switchIfEmpty(loanWaiverUseCase.getAllLoanWaiverDataBySamityIdList(samityIdList)
                                .flatMap(loanRebateDTOS -> Mono.just(List.of(SamityEventTracker.builder().build()))))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " has No Collection or Withdraw or Loan Adjustment")))
                        .map(list -> samityId));
    }

    private Mono<List<AuthorizationGridViewSamityDTO>> getGridViewOfSamityResponse(ManagementProcessTracker managementProcessTracker, AuthorizationRequestDTO requestDTO) {
        Flux<StagingProcessTrackerEntity> stagingProcessTrackerEntities;

        if (Strings.isNotNullAndNotEmpty(requestDTO.getSamityId().trim())) {
            stagingProcessTrackerEntities = stagingDataUseCase.getStagingProcessEntityForSamity(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityId()).flux();
        } else if (Strings.isNotNullAndNotEmpty(requestDTO.getFieldOfficerId().trim())) {
            stagingProcessTrackerEntities = stagingDataUseCase.getStagingProcessEntityForFieldOfficer(managementProcessTracker.getManagementProcessId(), requestDTO.getFieldOfficerId())
                    .flatMapMany(Flux::fromIterable);
        } else {
            stagingProcessTrackerEntities = stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId());
        }

        return stagingProcessTrackerEntities
                .map(trackerEntity -> gson.fromJson(trackerEntity.toString(), AuthorizationGridViewSamityDTO.class))
                .flatMap(samityDTO -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(), samityDTO.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .collectList()
                        .doOnNext(samityEventTrackerList -> log.debug("samityEventTrackerList: {}", samityEventTrackerList))
                        .map(samityEventTrackerList -> {
                            if (samityEventTrackerList.isEmpty() || samityEventTrackerList.stream().anyMatch(samityEventTracker -> samityEventTracker.getSamityEvent().equals(SamityEvents.CANCELED.getValue()))) {
                                samityDTO.setSamityId(null);
                            }
                            return samityDTO;
                        }))
                .filter(samityDTO -> !HelperUtil.checkIfNullOrEmpty(samityDTO.getSamityId()))
                .flatMap(samityDTO1 -> getCollectionWithdrawAndLoanAdjustmentDetailsForSamity(samityDTO1, managementProcessTracker))
                .filter(samityDTO -> !HelperUtil.checkIfNullOrEmpty(samityDTO.getStatus()))
                .sort(Comparator.comparing(AuthorizationGridViewSamityDTO::getSamityId))
                .collectList();
    }

    private Mono<AuthorizationGridViewSamityDTO> getCollectionWithdrawAndLoanAdjustmentDetailsForSamity(AuthorizationGridViewSamityDTO samityDTO, ManagementProcessTracker managementProcessTracker) {
        return collectionUseCase.getAllCollectionStagingDataBySamity(samityDTO.getSamityId())
                .flatMap(collectionList -> withdrawUseCase.getAllWithdrawStagingDataBySamity(samityDTO.getSamityId())
                        .flatMap(withdrawList -> loanAdjustmentUseCase.getLoanAdjustmentDataBySamity(samityDTO.getSamityId())
                                .map(loanAdjustmentList -> Tuples.of(collectionList, withdrawList, loanAdjustmentList))))
                .flatMap(tuple3 -> getLoanRebateDataBySamityId(samityDTO, managementProcessTracker, tuple3))
                .flatMap(tuple4 -> getLoanWaiverDataBySamityId(samityDTO, managementProcessTracker, tuple4))
                .flatMap(tuple5 -> getLoanWriteOffDataBySamityId(samityDTO, managementProcessTracker, tuple5))
                .map(tuple -> {
                    StatusVerificationDTO collectionStatus = getStatusVerificationDTO(tuple.getT1().stream().map(collectionStagingData -> modelMapper.map(collectionStagingData, StatusDTO.class)).toList());
                    StatusVerificationDTO withdrawStatus = getStatusVerificationDTO(tuple.getT2().stream().map(stagingWithdrawData -> modelMapper.map(stagingWithdrawData, StatusDTO.class)).toList());
                    StatusVerificationDTO loanAdjustmentStatus = getStatusVerificationDTO(tuple.getT3().stream().map(loanAdjustmentData -> modelMapper.map(loanAdjustmentData, StatusDTO.class)).toList());
                    StatusVerificationDTO rebateStatus = getStatusVerificationDTO(tuple.getT4().stream().map(loanRebateDTO -> modelMapper.map(loanRebateDTO, StatusDTO.class)).toList());
                    StatusVerificationDTO waiverStatus = getStatusVerificationDTO(tuple.getT5().stream().map(loanWaiverDTO -> modelMapper.map(loanWaiverDTO, StatusDTO.class)).toList());
                    StatusVerificationDTO writeOffStatus = getStatusVerificationDTO(tuple.getT6().stream().map(loanWriteOffDTO -> modelMapper.map(loanWriteOffDTO, StatusDTO.class)).toList());


                    BigDecimal totalCollectionAmount = tuple.getT1().stream().map(CollectionStagingData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalWithdrawAmount = tuple.getT2().stream().map(StagingWithdrawData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalLoanAdjustmentAmount = tuple.getT3().stream().filter(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getLoanAccountId())).map(LoanAdjustmentData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalRebateAmount = tuple.getT4().stream().map(LoanRebateDTO::getRebateAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalWaivedAmount = tuple.getT5().stream().map(LoanWaiverDTO::getWaivedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalWriteOffCollectionAmount = tuple.getT6().stream().map(LoanWriteOffCollectionDTO::getWriteOffCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);


                    StatusVerificationDTO allEventStatus = this.getAllEventStatus(List.of(collectionStatus, withdrawStatus, loanAdjustmentStatus, rebateStatus, waiverStatus, writeOffStatus));
                    this.setSamityDTOStatus(allEventStatus, samityDTO);

                    if (!tuple.getT1().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT1().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT1().get(0).getApprovedBy());
                    } else if (!tuple.getT2().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT2().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT2().get(0).getApprovedBy());
                    } else if (!tuple.getT3().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT3().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT3().get(0).getApprovedBy());
                    } else if (!tuple.getT4().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT4().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT4().get(0).getApprovedBy());
                    } else if (!tuple.getT5().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT5().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT5().get(0).getApprovedBy());
                    } else if (!tuple.getT6().isEmpty()) {
                        samityDTO.setLockedBy(tuple.getT6().get(0).getLockedBy());
                        samityDTO.setAuthorizedBy(tuple.getT6().get(0).getApprovedBy());
                    }

                    samityDTO.setTotalCollectionAmount(totalCollectionAmount);
                    samityDTO.setTotalWithdrawAmount(totalWithdrawAmount);
                    samityDTO.setTotalLoanAdjustmentAmount(totalLoanAdjustmentAmount);
                    samityDTO.setTotalLoanRebateAmount(totalRebateAmount);
                    samityDTO.setTotalLoanWaivedAmount(totalWaivedAmount);
                    samityDTO.setTotalLoanWriteOffCollectionAmount(totalWriteOffCollectionAmount);
                    return samityDTO;
                });
    }

    private Mono<Tuple6<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>, List<LoanRebateDTO>, List<LoanWaiverDTO>, List<LoanWriteOffCollectionDTO>>> getLoanWriteOffDataBySamityId(AuthorizationGridViewSamityDTO samityDTO, ManagementProcessTracker managementProcessTracker, Tuple5<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>, List<LoanRebateDTO>, List<LoanWaiverDTO>> tuple5) {
        return writeOffCollectionUseCase
                .getLoanWriteOffDataBySamityId(samityDTO.getSamityId(), managementProcessTracker.getManagementProcessId())
                .map(loanWriteOffDTOList -> Tuples.of(tuple5.getT1(), tuple5.getT2(), tuple5.getT3(), tuple5.getT4(), tuple5.getT5(), loanWriteOffDTOList));
    }

    private Mono<Tuple5<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>, List<LoanRebateDTO>, List<LoanWaiverDTO>>> getLoanWaiverDataBySamityId(AuthorizationGridViewSamityDTO samityDTO, ManagementProcessTracker managementProcessTracker, Tuple4<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>, List<LoanRebateDTO>> tuple4) {
        return loanWaiverUseCase
                .getLoanWaiverDataBySamityId(samityDTO.getSamityId(), managementProcessTracker.getManagementProcessId())
                .map(loanWaiverDTOList -> Tuples.of(tuple4.getT1(), tuple4.getT2(), tuple4.getT3(), tuple4.getT4(), loanWaiverDTOList));
    }

    private Mono<Tuple4<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>, List<LoanRebateDTO>>> getLoanRebateDataBySamityId(AuthorizationGridViewSamityDTO samityDTO, ManagementProcessTracker managementProcessTracker, Tuple3<List<CollectionStagingData>, List<StagingWithdrawData>, List<LoanAdjustmentData>> tuple3) {
        return loanRebateUseCase
                .getLoanRebateDataBySamityId(samityDTO.getSamityId(), managementProcessTracker.getManagementProcessId())
                .map(loanRebateDTOList -> Tuples.of(tuple3.getT1(), tuple3.getT2(), tuple3.getT3(), loanRebateDTOList));
    }


    private StatusVerificationDTO getStatusVerificationDTO(List<StatusDTO> list) {
        Boolean isSubmitted = list.stream().allMatch(item -> item.getStatus().equals(Status.STATUS_SUBMITTED.getValue()));
        Boolean isAuthorized = list.stream().allMatch(item -> item.getStatus().equals(Status.STATUS_APPROVED.getValue()));
        Boolean isUnauthorized = list.stream().allMatch(item -> item.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()));
        Boolean isLocked = list.stream().filter(statusDTO -> Strings.isNotNullAndNotEmpty(statusDTO.getIsLocked()))
                .allMatch(item -> item.getIsLocked().equals(Status.STATUS_YES.getValue()));


        return StatusVerificationDTO
                .builder()
                .isSubmitted(isSubmitted)
                .isAuthorized(isAuthorized)
                .isUnAuthorized(isUnauthorized)
                .isLocked(isLocked)
                .build();
    }

    private StatusVerificationDTO getAllEventStatus(List<StatusVerificationDTO> eventStatusList) {
        Boolean allSubmitted = eventStatusList.stream().allMatch(StatusVerificationDTO::getIsSubmitted);
        Boolean allAuthorized = eventStatusList.stream().allMatch(StatusVerificationDTO::getIsAuthorized);
        Boolean allUnAuthorized = eventStatusList.stream().allMatch(StatusVerificationDTO::getIsUnAuthorized);
        Boolean allLocked = eventStatusList.stream().allMatch(StatusVerificationDTO::getIsLocked);

        return StatusVerificationDTO
                .builder()
                .allSubmitted(allSubmitted)
                .allAuthorized(allAuthorized)
                .allUnAuthorized(allUnAuthorized)
                .allLocked(allLocked)
                .build();
    }


    private AuthorizationGridViewSamityDTO setSamityDTOStatus(StatusVerificationDTO allEventStatus, AuthorizationGridViewSamityDTO samityDTO) {
        boolean allSubmitted = allEventStatus.getAllSubmitted();
        boolean allAuthorized = allEventStatus.getAllAuthorized();
        boolean allUnauthorized = allEventStatus.getAllUnAuthorized();
        boolean allLocked = allEventStatus.getAllLocked();

        if (allAuthorized) {
            samityDTO.setStatus("Authorization Completed");
        } else if (allSubmitted) {
            samityDTO.setStatus("Authorization Incomplete");
        } else if (allUnauthorized) {
            samityDTO.setStatus("Unauthorized");
        } else {
            samityDTO.setStatus(null);
        }

        if (allLocked) {
            samityDTO.setBtnLockEnabled("No");
        } else {
            samityDTO.setBtnLockEnabled("Yes");
        }

        return samityDTO;
    }

}
