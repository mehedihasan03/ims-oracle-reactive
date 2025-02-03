package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.attendance.application.port.in.MemberAttendanceUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.*;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.CollectionStagingDataFieldOfficerDetailViewResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.CollectionStagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.CollectionStagingSavingsAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataEditHistoryPort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.EmployeePersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.StagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.StagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.Employee;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingDataEditHistory;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.CollectionStagingDataHistoryAdapter;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.StagingAccountDataHistoryAdapter;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingSavingsAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.ACCOUNT_TYPE_LOAN;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.ACCOUNT_TYPE_SAVINGS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_DPS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_FDR;
import static net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum.NO;

@Slf4j
@Service
public class CollectionStagingDataQueryService implements CollectionStagingDataQueryUseCase {
    private final StagingDataPersistencePort stagingDataPersistencePort;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final EmployeePersistencePort employeePersistencePort;
    private final StagingAccountDataPersistencePort stagingAccountDataPersistencePort;
    private final StagingAccountDataHistoryAdapter stagingAccountDataHistoryRepository;
    private final CollectionStagingDataEditHistoryPort editHistoryPort;

    private final IStagingDataUseCase stagingDataUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final LoanRebatePersistencePort loanRebatePersistencePort;
    private final LoanWaiverPersistencePort loanWaiverPersistencePort;

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final PassbookUseCase passbookUseCase;

    private final TransactionalOperator rxtx;
    private final ModelMapper mapper;
    private final ModelMapper modelMapper;
    private final CommonRepository commonRepository;
    private final Gson gson;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final MemberAttendanceUseCase memberAttendanceUseCase;
    private final CollectionStagingDataHistoryAdapter collectionStagingDataHistoryAdapter;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final RepaymentSchedulePersistencePort repaymentSchedulePersistencePort;

    public CollectionStagingDataQueryService(StagingDataPersistencePort stagingDataPersistencePort,
                                             CollectionStagingDataPersistencePort collectionStagingDataPersistencePort,
                                             EmployeePersistencePort employeePersistencePort,
//                                             @Qualifier("stagingAccountDataGatewayAdapter")
                                             StagingAccountDataPersistencePort stagingAccountDataPersistencePort,
                                             StagingAccountDataHistoryAdapter stagingAccountDataHistoryRepository, CollectionStagingDataEditHistoryPort editHistoryPort,
                                             IStagingDataUseCase stagingDataUseCase,
                                             @Lazy
                                             LoanAdjustmentUseCase loanAdjustmentUseCase, LoanRebatePersistencePort loanRebatePersistencePort, LoanWaiverPersistencePort loanWaiverPersistencePort, PassbookUseCase passbookUseCase, TransactionalOperator rxtx,
                                             ModelMapper mapper, CommonRepository commonRepository,
                                             ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                                             SamityEventTrackerUseCase samityEventTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, MemberAttendanceUseCase memberAttendanceUseCase, CollectionStagingDataHistoryAdapter collectionStagingDataHistoryAdapter, ISavingsAccountUseCase savingsAccountUseCase, RepaymentSchedulePersistencePort repaymentSchedulePersistencePort) {
        this.stagingDataPersistencePort = stagingDataPersistencePort;
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.employeePersistencePort = employeePersistencePort;
        this.stagingAccountDataPersistencePort = stagingAccountDataPersistencePort;
        this.stagingAccountDataHistoryRepository = stagingAccountDataHistoryRepository;
        this.editHistoryPort = editHistoryPort;
        this.stagingDataUseCase = stagingDataUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.loanRebatePersistencePort = loanRebatePersistencePort;
        this.loanWaiverPersistencePort = loanWaiverPersistencePort;
        this.passbookUseCase = passbookUseCase;
        this.rxtx = rxtx;
        this.mapper = mapper;
        this.commonRepository = commonRepository;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.memberAttendanceUseCase = memberAttendanceUseCase;
        this.collectionStagingDataHistoryAdapter = collectionStagingDataHistoryAdapter;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.repaymentSchedulePersistencePort = repaymentSchedulePersistencePort;
        this.gson = CommonFunctions.buildGson(this);
        this.modelMapper = new ModelMapper();
    }


    @Override
    public Mono<CollectionGridViewByOfficeResponseDTO> gridViewOfRegularCollectionByOffice(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByOfficeIdAndSamityDay(request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Regular Samity ID: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
//                            log.info("List of Paginated Regular Samity ID: {}", paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseForOffice(tuple2, totalSamity))
                .doOnNext(responseDTO -> log.info("Grid View Response For Regular Collection By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Regular Collection By Office, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridViewByFieldOfficerResponseDTO> gridViewOfRegularCollectionByFieldOfficer(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return commonRepository.getOfficeIdOfAFieldOfficer(request.getFieldOfficerId())
                .map(officeId -> {
                    request.setOfficeId(officeId);
                    return request;
                })
                .flatMap(requestDTO -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId()))
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByFieldOfficerIdAndSamityDay(request.getFieldOfficerId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Regular Samity ID: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
//                            log.info("List of Paginated Regular Samity ID: {}", paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseForOffice(tuple2, totalSamity))
                .flatMap(responseDTO -> this.buildGridViewResponseForFieldOfficer(responseDTO, request.getFieldOfficerId()))
                .doOnNext(responseDTO -> log.info("Grid View Response For Regular Collection By Field Officer: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Regular Collection By Field Officer, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridViewByOfficeResponseDTO> gridViewOfSpecialCollectionByOffice(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByOfficeIdAndNonSamityDay(request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Special Samity ID: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
//                            log.info("List of Paginated Special Samity ID: {}", paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseForOffice(tuple2, totalSamity))
                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection By Office, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridViewByFieldOfficerResponseDTO> gridViewOfSpecialCollectionByFieldOfficer(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return commonRepository.getOfficeIdOfAFieldOfficer(request.getFieldOfficerId())
                .map(officeId -> {
                    request.setOfficeId(officeId);
                    return request;
                })
                .flatMap(requestDTO -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId()))
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByFieldOfficerIdAndNonSamityDay(request.getFieldOfficerId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Special Samity ID: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
//                            log.info("List of Paginated Special Samity ID: {}", paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseForOffice(tuple2, totalSamity))
                .flatMap(responseDTO -> this.buildGridViewResponseForFieldOfficer(responseDTO, request.getFieldOfficerId()))
                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection By Field Officer: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection By Field Officer, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<AuthorizationGridViewResponseDTO> gridViewOfRegularCollectionAuthorizationByOffice(CollectionDataRequestDTO request) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByOfficeIdAndSamityDay(request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .doOnNext(list -> log.info("Regular Samity Id list: {}", list)))
                .flatMap(this::buildGridViewSamityObjectForAuthorization)
                .map(list -> AuthorizationGridViewResponseDTO.builder()
                        .officeId(request.getOfficeId())
                        .data(list)
                        .totalCount(list.size())
                        .build())
                .doOnNext(responseDTO -> log.info("Grid View Response For Regular Collection Authorization By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Regular Collection Authorization By Office, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<AuthorizationGridViewResponseDTO> gridViewOfSpecialCollectionAuthorizationByOffice(CollectionDataRequestDTO request) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> commonRepository.getSamityIdListByOfficeIdAndNonSamityDay(request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        .collectList()
                        .doOnNext(list -> log.info("Special Samity Id list: {}", list)))
                .flatMap(this::buildGridViewSamityObjectForAuthorization)
                .map(list -> AuthorizationGridViewResponseDTO.builder()
                        .officeId(request.getOfficeId())
                        .data(list)
                        .totalCount(list.size())
                        .build())
                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection Authorization By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection Authorization By Office, Error Message: {}", throwable.getMessage()));
    }

    //    Refactored Methods
    private Mono<CollectionGridViewByOfficeResponseDTO> buildGridViewResponseForOffice(Tuple2<List<String>, ManagementProcessTracker> tuple2, AtomicInteger totalSamity) {
        return officeEventTrackerUseCase
                .getAllOfficeEventsForOffice(tuple2.getT2().getManagementProcessId(), tuple2.getT2().getOfficeId())
                .collectList()
                .flatMap(list -> {
                    if (list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && item.getOfficeEvent().equalsIgnoreCase(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
                        return this.buildGridViewSamityObjectForCollection(tuple2);
                    }
                    List<GridViewDataObject> emptyList = new ArrayList<>();
                    totalSamity.set(0);
                    return Mono.just(emptyList);
                })
                .map(list -> CollectionGridViewByOfficeResponseDTO.builder()
                        .officeId(tuple2.getT2().getOfficeId())
                        .data(list)
                        .totalCount(totalSamity.get())
                        .build())
                .flatMap(responseDTO -> commonRepository.getOfficeEntityByOfficeId(responseDTO.getOfficeId())
                        .map(officeEntity -> {
                            responseDTO.setOfficeNameEn(officeEntity.getOfficeNameEn());
                            responseDTO.setOfficeNameBn(officeEntity.getOfficeNameBn());
                            return responseDTO;
                        }));
    }

    private Mono<CollectionGridViewByFieldOfficerResponseDTO> buildGridViewResponseForFieldOfficer(CollectionGridViewByOfficeResponseDTO OfficeResponseDTO, String fieldOfficerId) {
        return Mono.just(gson.fromJson(OfficeResponseDTO.toString(), CollectionGridViewByFieldOfficerResponseDTO.class))
                .flatMap(responseDTO -> {
                    if (responseDTO.getData().isEmpty()) {
                        return commonRepository.getFieldOfficerByFieldOfficerId(fieldOfficerId)
                                .map(fieldOfficerEntity -> {
                                    responseDTO.setFieldOfficerId(fieldOfficerEntity.getFieldOfficerId());
                                    responseDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
                                    responseDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
                                    return responseDTO;
                                });
                    }
                    responseDTO.setFieldOfficerId(responseDTO.getData().get(0).getFieldOfficerId());
                    responseDTO.setFieldOfficerNameEn(responseDTO.getData().get(0).getFieldOfficerNameEn());
                    responseDTO.setFieldOfficerNameBn(responseDTO.getData().get(0).getFieldOfficerNameBn());
                    return Mono.just(responseDTO);
                });
    }

    private Mono<List<GridViewDataObject>> buildGridViewSamityObjectForCollection(Tuple2<List<String>, ManagementProcessTracker> tuple2) {
        return Flux.fromIterable(tuple2.getT1())
                .flatMap(samityId -> stagingDataPersistencePort.getOneStagingDataEntityBySamityId(samityId)
                        .switchIfEmpty(commonRepository.getGridViewDataObjectForSamityWithNoMember(samityId)
                                .map(gridViewDataObject -> gson.fromJson(gridViewDataObject.toString(), StagingData.class)))
                        .map(stagingData -> gson.fromJson(stagingData.toString(), GridViewDataObject.class)))
                .flatMap(samityObject -> samityEventTrackerUseCase.getAllSamityEventsForSamity(tuple2.getT2().getManagementProcessId(), samityObject.getSamityId())
                        .collectList()
                        .map(samityEventTrackerList -> {
                            samityObject.setType(HelperUtil.checkIfNullOrEmpty(samityObject.getDownloadedBy()) ? "Online" : "Offline");

                            log.info("samity : {} | eventList : {}", samityObject.getSamityId(), samityEventTrackerList);

                            if (samityEventTrackerList.stream().anyMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()))) {
                                samityObject.setStatus("Collection Incomplete");
                            } else if (samityEventTrackerList.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.COLLECTED.getValue()))) {
                                samityObject.setStatus("Collection Completed");
                            } else if (samityEventTrackerList.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.WITHDRAWN.getValue()))) {
                                samityObject.setStatus("Collection Completed");
                            } else if (samityEventTrackerList.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.CANCELED.getValue()))) {
                                samityObject.setStatus("Samity Canceled");
                                samityObject.setType(null);
                            }
                            if (samityObject.getTotalMember() == null) {
                                samityObject.setTotalMember(0);
                                samityObject.setType(null);
                                samityObject.setStatus("Collection Unavailable");
                                samityObject.setRemarks("No member found in Samity!");
                            }
                            samityEventTrackerList.forEach(item -> {
                                if (!HelperUtil.checkIfNullOrEmpty(item.getSamityEvent()) && item.getSamityEvent().equals(SamityEvents.CANCELED.getValue())) {
                                    samityObject.setRemarks(item.getRemarks());
                                }
                            });
                            return samityObject;
                        }))
                .flatMap(samityObject -> collectionStagingDataPersistencePort.getOneCollectionBySamity(samityObject.getSamityId())
                        .map(collectionStagingData -> {
                            log.info("samity Id for debug : {} | status : {}", samityObject.getSamityId(), samityObject.getStatus());

                            if (samityObject.getStatus().equals("Collection Completed") && !collectionStagingData.getStatus().equals("Approved") && !HelperUtil.checkIfNullOrEmpty(collectionStagingData.getEditCommit())) {
                                if (collectionStagingData.getEditCommit().equals("Yes")) {
                                    samityObject.setIsCommittable("No");
                                    samityObject.setIsEditable("No");
                                    samityObject.setStatus("Collection Committed");
                                } else {
                                    samityObject.setIsCommittable("Yes");
                                    samityObject.setIsEditable("Yes");
                                }
                            } else if (!HelperUtil.checkIfNullOrEmpty(collectionStagingData.getStatus()) && collectionStagingData.getStatus().equalsIgnoreCase("Approved") && collectionStagingData.getEditCommit().equals("Yes")) {
                                samityObject.setIsCommittable("No");
                                samityObject.setIsEditable("No");
                                samityObject.setStatus("Collection Authorized");
                            } else {
                                samityObject.setIsCommittable(null);
                                samityObject.setIsEditable(null);
                            }
                            if (!HelperUtil.checkIfNullOrEmpty(samityObject.getType()) && samityObject.getType().equals("Offline")) {
                                samityObject.setUploadedBy(collectionStagingData.getUploadedBy());
                                samityObject.setUploadedOn(collectionStagingData.getUploadedOn());
                            }
                            return samityObject;
                        }))
                .flatMap(samityObject -> collectionStagingDataPersistencePort.getTotalCollectionBySamity(samityObject.getSamityId())
                        .map(totalCollection -> {
                            samityObject.setTotalCollectionAmount(totalCollection);
                            return samityObject;
                        }))
                .sort(Comparator.comparing(GridViewDataObject::getSamityId))
                .collectList();
    }

    private Mono<List<GridViewDataObject>> buildGridViewSamityObjectForAuthorization(List<String> samityIdList) {
        return Flux.fromIterable(samityIdList)
                .flatMap(samityId -> stagingDataPersistencePort.getOneStagingDataEntityBySamityId(samityId)
                        .map(stagingData -> {
                            GridViewDataObject gridViewDataObject = gson.fromJson(stagingData.toString(), GridViewDataObject.class);
                            gridViewDataObject.setType(
                                    HelperUtil.checkIfNullOrEmpty(gridViewDataObject.getDownloadedBy()) ? "Online" : "Offline"
                            );
                            return gridViewDataObject;
                        }))
                .flatMap(gridViewDataObject -> collectionStagingDataPersistencePort.getAllCollectionDataBySamity(gridViewDataObject.getSamityId())
                        .collectList()
                        .map(list -> {
                            if (list.isEmpty()) {
                                gridViewDataObject.setStatus(null);
                            } else {
                                if (list.stream().allMatch(item -> item.getStatus().equals("Approved") && item.getEditCommit().equalsIgnoreCase("Yes"))) {
                                    gridViewDataObject.setStatus("Authorization Completed");
                                } else if (list.stream().allMatch(item -> !item.getStatus().equals("Approved") && item.getEditCommit().equalsIgnoreCase("Yes"))) {
                                    gridViewDataObject.setStatus("Authorization Incomplete");
                                } else {
                                    gridViewDataObject.setStatus(null);
                                }
                                gridViewDataObject.setUploadedBy(list.get(0).getUploadedBy());
                                gridViewDataObject.setUploadedOn(list.get(0).getUploadedOn());
                            }
                            return gridViewDataObject;
                        })
                )
                .filter(gridViewDataObject -> !HelperUtil.checkIfNullOrEmpty(gridViewDataObject.getStatus()))
                .flatMap(this::getTotalCollectionForASamity)
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(GridViewDataObject::getSamityId));
                    return list;
                });
    }


    //    ==================================================
//    ==================================================
    private Mono<CollectionGridViewByFieldOfficerResponseDTO> getCollectionStagingDataGridViewResponseMono(CollectionDataRequestDTO request, String samityDay, String requiredStatus) {
        return stagingDataPersistencePort
                .getListOfStagingDataByFieldOfficerIdAndSamityDay(request.getFieldOfficerId(), samityDay)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_STAGING_DATA_FOUND.getValue())))
                .doOnComplete(() -> log.info("getListOfStagingDataByFieldOfficerIdAndSamityDay executed"))
                .distinct(StagingData::getSamityId)
                .doOnComplete(() -> log.info("groupBy executed"))
                .map(this::buildResponseNestedObjectForGridViewResponse)
                .flatMap(this::getTotalCollectionForASamity)
                .flatMap(gridViewDataObject -> this.setCompletionStatus(gridViewDataObject, requiredStatus))
//                .flatMap(gridViewDataObjectMono -> gridViewDataObjectMono)
                .filter(gridViewDataObjects -> gridViewDataObjects.getStatus() != null && !gridViewDataObjects.getStatus().isEmpty())
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(GridViewDataObject::getSamityId));
                    return list;
                })
                .doOnNext(list -> log.info("List of staging data for collection: {}", list))
                .doOnSuccess(gridViewDataObjects -> log.info("Combined List - {}", gridViewDataObjects))
                .flatMap(
                        gridViewDataObjects ->
                                employeePersistencePort
                                        .getEmployeeDetailByEmployeeId(request.getFieldOfficerId())
                                        .map(employee -> buildCollectionStagingDataGridViewResponse(gridViewDataObjects, request, employee))
                )
                .doOnSuccess(collectionStagingDataGridViewResponse -> log.info("CollectionStagingDataGridViewResponse - {}", collectionStagingDataGridViewResponse))
                .doOnError(throwable -> log.info("Error in Collection Grid View: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionStagingDataDetailViewResponse> getCollectionStagingDataDetailViewBySamityId(CollectionDataRequestDTO request) {
        StagingDataRequestDTO requestDTO = mapper.map(request, StagingDataRequestDTO.class);
//        @TODO: fix installment date issue
//      TypeMap<Installment, InstallmentDTO> castMapper =
//          mapper.typeMap(Installment.class, InstallmentDTO.class)
//              .addMappings(mapping -> {
//                mapping.map(Installment::getInstallmentDate, InstallmentDTO::setInstallmentDate);
//              });

        log.info("Request for Collection Staging Data Detail View: {}", requestDTO);

        return stagingDataUseCase.getStagingDataDetailViewResponseBySamityId(requestDTO)
                .map(stagingDataDetailView -> gson.fromJson(stagingDataDetailView.toString(), CollectionStagingDataDetailViewResponse.class))
                .doOnNext(response -> response.setOfficeId(request.getOfficeId()))
                .doOnNext(response -> log.info("Collection Staging Data Detail View Response: {}", response))
                .flatMap(this::buildCollectionStagingDataDetailViewWithLoanAccountInfo)
                .flatMap(collectionStagingDataDetailViewResponse -> this.setFlagsForCollectionStagingDataDetailView(collectionStagingDataDetailViewResponse, request.getLoginId()))
                .flatMap(this::updateSavingsAccountBalanceForDetailViewResponse)
                .flatMap(this::updateMemberAttendanceForRegularSamityDetailViewResponse)
                .flatMap(response -> this.filterCollectionDetailViewResponseForSpecialSamity(response, request.getOfficeId()))
                .doOnError(throwable -> log.error("Error in Collection Staging Data Detail View: {}", throwable.getMessage()));
    }

    private Mono<CollectionStagingDataDetailViewResponse> filterCollectionDetailViewResponseForSpecialSamity(CollectionStagingDataDetailViewResponse response, String officeId) {
        log.info("Filtering Collection Detail View Response For Special Samity: {}", response);
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .map(managementProcessTracker -> {
                    if (!response.getSamityDay().equals(managementProcessTracker.getBusinessDay())) {
                        List<MemberInfoDTO> filteredMemberList = response.getMemberList().stream()
                                .peek(memberInfoDTO -> {
                                    BigDecimal loanCollectionAmount = memberInfoDTO.getLoanAccountList().stream()
                                            .map(CollectionStagingLoanAccountInfoDTO::getAmount)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal savingsCollectionAmount = memberInfoDTO.getSavingsAccountList().stream()
                                            .map(CollectionStagingSavingsAccountInfoDTO::getAmount)
                                            .filter(Objects::nonNull)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    memberInfoDTO.setTotalCollection(loanCollectionAmount.add(savingsCollectionAmount));
                                })
                                .filter(memberInfoDTO -> memberInfoDTO.getTotalCollection().compareTo(BigDecimal.ZERO) > 0)
                                .toList();
                        response.setMemberList(filteredMemberList);
                    }
//                    response.setOfficeId(officeId);
                    return response;
                });
    }

    private Mono<CollectionStagingDataDetailViewResponse> updateMemberAttendanceForRegularSamityDetailViewResponse(CollectionStagingDataDetailViewResponse response) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(response.getOfficeId())
                .flatMap(managementProcessTracker -> memberAttendanceUseCase.getMemberAttendanceListForSamityByAttendanceDate(response.getSamityId(), managementProcessTracker.getBusinessDate()))
                .map(memberAttendanceList -> {
                    log.info("Member Attendance List size: {}", memberAttendanceList.size());
                    if (memberAttendanceList.isEmpty()) {
                        log.info("Member Attendance List is Empty");
                        return response;
                    }
                    response.getMemberList().forEach(memberInfoDTO -> memberAttendanceList.forEach(memberAttendanceDTO -> {
                        if (memberInfoDTO.getMemberId().equals(memberAttendanceDTO.getMemberId())) {
                            if (memberAttendanceDTO.getStatus().equals("Present")) {
                                memberInfoDTO.setIsPresent("Yes");
                            } else if (memberAttendanceDTO.getStatus().equals("Absent")) {
                                memberInfoDTO.setIsPresent("No");
                            }
                        }
                    }));
                    return response;
                });
    }

    private Mono<CollectionStagingDataDetailViewResponse> updateSavingsAccountBalanceForDetailViewResponse(CollectionStagingDataDetailViewResponse responseDTO) {
        return Flux.fromIterable(responseDTO.getMemberList())
                .doOnNext(memberInfoDTO -> log.info("Updating Savings balance For Member: {}", memberInfoDTO))
                .flatMap(this::updateSavingsAccountBalanceForOneMember)
                .collectList()
                .map(memberInfoDTOList -> {
                    List<MemberInfoDTO> sortedMemberInfoList = memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getMemberId)).toList();
                    responseDTO.setMemberList(sortedMemberInfoList);
                    return responseDTO;
                });
    }

    private Mono<MemberInfoDTO> updateSavingsAccountBalanceForOneMember(MemberInfoDTO memberInfoDTO) {
        return Flux.fromIterable(memberInfoDTO.getSavingsAccountList())
                .doOnNext(savingsAccountInfoDTO -> {
                    savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : savingsAccountInfoDTO.getSavingsAvailableBalance());
                    savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance() == null ? BigDecimal.ZERO : savingsAccountInfoDTO.getBalance());
                })
                .flatMap(savingsAccountInfoDTO -> commonRepository.getStagingWithdrawDataBySavingsAccountId(savingsAccountInfoDTO.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountInfoDTO;
                        }))
                .flatMap(savingsAccountInfoDTO -> commonRepository.getLoanAdjustmentDataBySavingsAccountId(savingsAccountInfoDTO.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().subtract(!list.isEmpty() ? list.stream().map(LoanAdjustmentDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().subtract(!list.isEmpty() ? list.stream().map(LoanAdjustmentDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountInfoDTO;
                        }))
                .flatMap(savingsAccountInfoDTO -> collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(savingsAccountInfoDTO.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().amount(BigDecimal.ZERO).build()))
                        .map(collectionStagingData -> {
                            savingsAccountInfoDTO.setBalance(savingsAccountInfoDTO.getBalance().add(collectionStagingData.getAmount()));
                            savingsAccountInfoDTO.setSavingsAvailableBalance(savingsAccountInfoDTO.getSavingsAvailableBalance().add(collectionStagingData.getAmount()));
                            return savingsAccountInfoDTO;
                        }))
                .collectList()
                .map(savingsAccountInfoDTOList -> {
//                    memberInfoDTO.setSavingsAccountList(savingsAccountInfoDTOList);
                    return memberInfoDTO;
                });
    }

    @Override
    public Mono<CollectionStagingDataAccountDetailViewResponse> getCollectionStagingDataDetailViewByAccountId(CollectionDataRequestDTO request) {
        StagingDataRequestDTO requestDTO = mapper.map(request, StagingDataRequestDTO.class);
//          @TODO: fix installment date issue
        return stagingDataUseCase.getStagingDataDetailViewResponseByAccountId(requestDTO)
                .map(response -> gson.fromJson(response.toString(), CollectionStagingDataAccountDetailViewResponse.class))
                .flatMap(this::buildCollectionStagingDataAccountDetailViewWithLoanAccountInfo)
                .flatMap(this::updateSavingsAccountBalanceForMemberDetailViewResponse);
    }


    @Override
    public Flux<CollectionStagingDataResponseDTO> getCollectionStagingDataForSamityMembers(String stagingDataId) {
        return collectionStagingDataPersistencePort
                .getCollectionStagingDataByStagingDataId(stagingDataId)
                .map(collectionStagingData -> gson.fromJson(collectionStagingData.toString(), CollectionStagingDataResponseDTO.class));
    }

    @Override
    public Mono<AuthorizationGridViewResponseDTO> gridViewCollectionAuthorizationDataByOfficeId(CollectionDataRequestDTO request) {
//        String samityDay = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        /*String samityDay = "Sunday";*/
        Mono<ManagementProcessTracker> lastManagementProcessForOffice = managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId());
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .flatMapMany(managementProcessTracker -> commonRepository.getSamityIdListByOfficeIdAndSamityDay(request.getOfficeId(), managementProcessTracker.getBusinessDay()))
                .zipWith(lastManagementProcessForOffice)
                .flatMap(tuple2 -> stagingDataPersistencePort.getListOfStagingDataBySamityIdIdAndSamityDay(tuple2.getT1(), tuple2.getT2().getBusinessDay()))
//        return commonRepository
//                .getFieldOfficersByOfficeId(request.getOfficeId())
//                .doOnNext(fieldOfficerEntity -> log.info("Field Officer Id: {}", fieldOfficerEntity.getFieldOfficerId()))
//                .zipWith(lastManagementProcessForOffice)
//                .flatMap(tuple -> stagingDataPersistencePort
//                        .getListOfStagingDataBySamityIdIdAndSamityDay(tuple.getT1().getFieldOfficerId(), tuple.getT2().getBusinessDay()))
                .distinct(StagingData::getSamityId)
                .map(stagingData -> {
                    GridViewDataObject gridViewDataObject = modelMapper.map(stagingData, GridViewDataObject.class);
                    gridViewDataObject.setType((stagingData.getDownloadedBy() != null || stagingData.getDownloadedOn() != null) ? "Offline" : "Online");
                    return gridViewDataObject;
                })
                .flatMap(gridViewDataObject -> collectionStagingDataPersistencePort.getAllCollectionData(gridViewDataObject.getSamityId(), CollectionType.REGULAR.getValue())
                        .filter(this::checkIfCollectionDataIsValidForAuthorizationGridView)
//                        .doOnNext(list -> log.info("after: {}", list))
                        .collectList()
                        .map(collectionStagingDataList -> {
                            if (!collectionStagingDataList.isEmpty()) {
                                gridViewDataObject.setUploadedBy(collectionStagingDataList.get(0).getUploadedBy());
                                gridViewDataObject.setUploadedOn(collectionStagingDataList.get(0).getUploadedOn());
                                gridViewDataObject.setStatus(collectionStagingDataList
                                        .stream()
                                        .anyMatch(collectionStagingData -> Objects.equals(collectionStagingData.getStatus(), Status.STATUS_STAGED.getValue()))
                                        ? "Authorization Incomplete" : "Authorization Completed");
                            }
                            return gridViewDataObject;
                        }))
                .filter(gridViewDataObject -> gridViewDataObject.getStatus() != null && !gridViewDataObject.getStatus().isEmpty())
                .flatMap(this::getTotalCollectionForASamity)
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(GridViewDataObject::getSamityId));
                    return list;
                })
//                .doOnNext(list -> log.info("Collection Auth: {}", list))
                .map(gridViewDataObjects -> {
                    return AuthorizationGridViewResponseDTO
                            .builder()
                            .officeId(request.getOfficeId())
                            .data(gridViewDataObjects)
                            .totalCount(gridViewDataObjects.size())
                            .build(); // TODO: ১০/৭/২৩ pagination
                });
    }

    @Override
    public Mono<CollectionStagingDataAccountDetailViewResponse> getCollectionStagingDataDetailViewByMemberId(CollectionDataRequestDTO request) {

        StagingDataRequestDTO requestDTO = mapper.map(request, StagingDataRequestDTO.class);
        return stagingDataUseCase.getStagingDataDetailViewResponseByMemberId(requestDTO)
                .map(response -> gson.fromJson(response.toString(), CollectionStagingDataAccountDetailViewResponse.class))
                .flatMap(this::buildCollectionStagingDataAccountDetailViewWithLoanAccountInfo)
                .flatMap(this::updateSavingsAccountBalanceForMemberDetailViewResponse);
    }

    private Mono<CollectionStagingDataAccountDetailViewResponse> updateSavingsAccountBalanceForMemberDetailViewResponse(CollectionStagingDataAccountDetailViewResponse responseDTO) {
        return Flux.fromIterable(responseDTO.getMemberList())
                .flatMap(this::updateSavingsAccountBalanceForOneMember)
                .collectList()
                .map(memberInfoDTOList -> {
                    List<MemberInfoDTO> sortedMemberInfoList = memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getMemberId)).toList();
                    responseDTO.setMemberList(sortedMemberInfoList);
                    return responseDTO;
                });
    }


    @Override
    public Mono<CollectionOfficerDetailViewResponseDTO> getCollectionDetailViewByFieldOfficer(CollectionDataRequestDTO request) {
        return stagingDataUseCase.getSamityIdListByFieldOfficer(request.getFieldOfficerId())
                .map(samityId -> {
                    request.setSamityId(samityId);
                    return request;
                })
                .flatMap(this::getCollectionStagingDataDetailViewBySamityId)
                .map(collectionStagingDataDetailViewResponse -> gson.fromJson(collectionStagingDataDetailViewResponse.toString(), CollectionStagingDataFieldOfficerDetailViewResponse.class))
                .collectList()
                .flatMap(list -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                        .map(managementProcessTracker -> {
                            list.forEach(item -> {
                                item.setOfficeId(request.getOfficeId());
                                if (managementProcessTracker.getBusinessDay().equalsIgnoreCase(item.getSamityDay())) {
                                    item.setCollectionType(CollectionType.REGULAR.getValue());
                                } else {
                                    item.setCollectionType(CollectionType.SPECIAL.getValue());
                                }
                            });
                            return list;
                        }))
                .map(list -> CollectionOfficerDetailViewResponseDTO.builder()
                        .officeId(request.getOfficeId())
                        .fieldOfficerId(list.get(0).getFieldOfficerId())
                        .fieldOfficerNameEn(list.get(0).getFieldOfficerNameEn())
                        .fieldOfficerNameBn(list.get(0).getFieldOfficerNameBn())
                        .samityList(list)
                        .totalCount(list.size())
                        .build());

//        StagingDataRequestDTO requestDTO = mapper.map(request, StagingDataRequestDTO.class);
//        Mono<ManagementProcessTracker> lastManagementProcessForOffice = managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId());
//        return stagingDataUseCase.getSamityIdListByFieldOfficer(requestDTO.getFieldOfficerId())
//                .collectList()
//                .doOnNext(list -> log.info("SamityId List: {}", list))
//                .flatMapMany(Flux::fromIterable)
//                .map(samityId -> StagingDataRequestDTO.builder().samityId(samityId).build())
//                .flatMap(stagingDataUseCase::getStagingDataDetailViewResponseBySamityId)
//                .map(stagingDataDetailView -> gson.fromJson(stagingDataDetailView.toString(), CollectionStagingDataDetailViewResponse.class))
//                .flatMap(this::buildCollectionStagingDataDetailViewWithLoanAccountInfo)
//                .zipWith(lastManagementProcessForOffice)
//                .map(tuple -> {
//                    CollectionStagingDataFieldOfficerDetailViewResponse collectionStagingDataFieldOfficerDetailViewResponse = gson.fromJson(tuple.getT1().toString(), CollectionStagingDataFieldOfficerDetailViewResponse.class);
//                    String today = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
//                    collectionStagingDataFieldOfficerDetailViewResponse.setOfficeId(requestDTO.getOfficeId());
//                    collectionStagingDataFieldOfficerDetailViewResponse.setCollectionType(
//                            collectionStagingDataFieldOfficerDetailViewResponse.getSamityDay().equalsIgnoreCase(tuple.getT2().getBusinessDay())
//                                    ? CollectionType.REGULAR.getValue()
//                                    : CollectionType.SPECIAL.getValue()
//                    );
//                    return collectionStagingDataFieldOfficerDetailViewResponse;
//                })
//                .collectList()
//                .map(list -> {
//                    list.sort(Comparator.comparing(CollectionStagingDataFieldOfficerDetailViewResponse::getSamityId));
//                    return CollectionOfficerDetailViewResponseDTO.builder()
//                            .samityList(list)
//                            .totalCount(list.size())
//                            .build();
//                })
//                .doOnNext(collectionOfficerDetailViewResponseDTO -> log.debug("CollectionOfficerDetailViewResponseDTO: {}", collectionOfficerDetailViewResponseDTO))
//                .flatMap(collectionOfficerDetailViewResponseDTO -> commonRepository.getFieldOfficerByFieldOfficerId(request.getFieldOfficerId())
//                        .map(fieldOfficerEntity -> {
//                            collectionOfficerDetailViewResponseDTO.setOfficeId(request.getOfficeId());
//                            collectionOfficerDetailViewResponseDTO.setFieldOfficerId(fieldOfficerEntity.getFieldOfficerId());
//                            collectionOfficerDetailViewResponseDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
//                            collectionOfficerDetailViewResponseDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
//                            return collectionOfficerDetailViewResponseDTO;
//                        }))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_STAGING_DATA_FOUND_WITH_FIELD_OFFICER_ID.getValue().concat(requestDTO.getFieldOfficerId()))))
//                .doOnError(throwable -> log.info("Error in getCollectionDetailViewByFieldOfficer: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionMessageResponseDTO> editCommitForCollectionDataBySamity(CollectionDataRequestDTO request) {
        return collectionStagingDataPersistencePort.editCommitForCollectionDataBySamity(request)
                .collectList()
                .map(entityList -> CollectionMessageResponseDTO.builder()
                        .userMessage("Collection Data is committed for authorization")
                        .build())
                .doOnError(throwable -> log.error("Failed to commit collection Staging Data: {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<CollectionStagingData>> getAllCollectionStagingDataBySamity(String samityId) {
        return collectionStagingDataPersistencePort.getAllCollectionDataBySamity(samityId)
                .collectList();
    }

    //    Process Management v2
    @Override
    public Mono<CollectionGridViewOfOfficeResponseDTO> gridViewOfRegularCollectionForOffice(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
//                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getRegularSamityIdListByOfficeIdAndSamityDay(managementProcessTracker.getManagementProcessId(), request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Regular Samity Id: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
                            log.info("Limit: {}, Offset: {}, List of Paginated Regular Samity Id: {}", request.getLimit(), request.getOffset(), paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseDTOOfOffice(tuple2, totalSamity))
                .doOnNext(responseDTO -> log.info("Grid View Response For Regular Collection By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Regular Collection By Office, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridViewOfFieldOfficerResponseDTO> gridViewOfRegularCollectionForFieldOfficer(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return commonRepository.getOfficeIdOfAFieldOfficer(request.getFieldOfficerId())
                .map(officeId -> {
                    request.setOfficeId(officeId);
                    return request;
                })
                .flatMap(requestDTO -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId()))
//                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getRegularSamityIdListByFieldOfficerIdAndSamityDay(managementProcessTracker.getManagementProcessId(), request.getFieldOfficerId(), managementProcessTracker.getBusinessDay())
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Regular Samity Id: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
                            log.info("Limit: {}, Offset: {}, List of Paginated Regular Samity Id: {}", request.getLimit(), request.getOffset(), paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseDTOOfOffice(tuple2, totalSamity))
                .map(officeResponseDTO -> gson.fromJson(officeResponseDTO.toString(), CollectionGridViewOfFieldOfficerResponseDTO.class))
                .flatMap(responseDTO -> this.buildGridViewResponseOfFieldOfficer(responseDTO, request.getFieldOfficerId()))
                .doOnNext(responseDTO -> log.info("Grid View Response For Regular Collection By Field Officer: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Regular Collection By Field Officer, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridViewOfOfficeResponseDTO> gridViewOfSpecialCollectionForOffice(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
//                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getSpecialSamityIdListByOfficeIdAndSamityDay(managementProcessTracker.getManagementProcessId(), request.getOfficeId(), managementProcessTracker.getBusinessDay())
                        /*.flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcessTracker.getManagementProcessId(), request.getOfficeId(), SamityEvents.COLLECTED.getValue())
                                .filter(samityIdlist::contains)
                                .collectList())*/
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Special Samity Id: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
                            log.info("Limit: {}, Offset: {}, List of Paginated Special Samity Id: {}", request.getLimit(), request.getOffset(), paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseDTOOfOffice(tuple2, totalSamity))
                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection By Office: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection By Office, Error Message: {}", throwable.getMessage()));

    }

    @Override
    public Mono<CollectionGridViewOfOfficeResponseDTO> listOfSpecialCollectionSamity(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);

        return Mono.just(request.getFieldOfficerId().trim())
                .flatMap(commonRepository::getOfficeIdOfAFieldOfficer)
                .defaultIfEmpty(request.getOfficeId())
                .flatMap(office ->
                        managementProcessTrackerUseCase.getLastManagementProcessForOffice(office)
                                .flatMap(managementProcessTracker -> stagingDataUseCase.getSpecialSamityIdListByOfficeIdAndSamityDay(managementProcessTracker.getManagementProcessId(), office, managementProcessTracker.getBusinessDay())
                                        .doOnNext(samityIdList -> log.info("List of Special Samity Id: {}", samityIdList))
                                        .flatMap(samityIdlist -> {
                                            if (!request.getFieldOfficerId().trim().isEmpty()) {
                                                return commonRepository.getSamityIdListByFieldOfficerIdAndNonSamityDay(request.getFieldOfficerId().trim(), managementProcessTracker.getBusinessDay())
                                                        .doOnNext(samityIdList -> log.info("List of Special Samity Id: {}", samityIdList))
                                                        .filter(samityIdlist::contains)
                                                        .collectList();
                                            } else
                                                return Mono.just(samityIdlist);
                                        })
                                        .map(samityIdList -> {
                                            totalSamity.set(samityIdList.size());
                                            log.info("List of Special Samity Id: {}", samityIdList);
                                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
                                            log.info("Limit: {}, Offset: {}, List of Paginated Special Samity Id: {}", request.getLimit(), request.getOffset(), paginatedSamityIdList);
                                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                                        }))
                                .flatMap(tuple2 -> this.buildGridViewResponseDTOOfOffice(tuple2, totalSamity))
                                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection By Office: {}", responseDTO))
                                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection By Office, Error Message: {}", throwable.getMessage())));
    }

    @Override
    public Mono<CollectionGridViewOfFieldOfficerResponseDTO> gridViewOfSpecialCollectionForFieldOfficer(CollectionDataRequestDTO request) {
        AtomicInteger totalSamity = new AtomicInteger(0);
        return commonRepository.getOfficeIdOfAFieldOfficer(request.getFieldOfficerId())
                .map(officeId -> {
                    request.setOfficeId(officeId);
                    return request;
                })
                .flatMap(requestDTO -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId()))
//                .filter(managementProcessTracker -> !HelperUtil.checkIfNullOrEmpty(managementProcessTracker.getManagementProcessId()))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found For Office: " + request.getOfficeId())))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getSpecialSamityIdListByFieldOfficerIdAndSamityDay(managementProcessTracker.getManagementProcessId(), request.getFieldOfficerId(), managementProcessTracker.getBusinessDay())
                        .flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcessTracker.getManagementProcessId(), request.getOfficeId(), SamityEvents.COLLECTED.getValue())
                                .filter(samityIdlist::contains)
                                .collectList())
                        .map(samityIdList -> {
                            totalSamity.set(samityIdList.size());
                            log.info("List of Special Samity Id: {}", samityIdList);
                            List<String> paginatedSamityIdList = samityIdList.stream().skip((long) request.getLimit() * request.getOffset()).limit(request.getLimit()).toList();
                            log.info("Limit: {}, Offset: {}, List of Paginated Special Samity Id: {}", request.getLimit(), request.getOffset(), paginatedSamityIdList);
                            return Tuples.of(paginatedSamityIdList, managementProcessTracker);
                        }))
                .flatMap(tuple2 -> this.buildGridViewResponseDTOOfOffice(tuple2, totalSamity))
                .map(officeResponseDTO -> gson.fromJson(officeResponseDTO.toString(), CollectionGridViewOfFieldOfficerResponseDTO.class))
                .flatMap(responseDTO -> this.buildGridViewResponseOfFieldOfficer(responseDTO, request.getFieldOfficerId()))
                .doOnNext(responseDTO -> log.info("Grid View Response For Special Collection By Field Officer: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Grid View Response For Special Collection By Field Officer, Error Message: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionMessageResponseDTO> submitCollectionDataForAuthorizationBySamity(CollectionDataRequestDTO requestDTO) {
        return this.getManagementProcessIdAndValidateCollectionDataForSubmission(requestDTO.getOfficeId(), requestDTO.getSamityId())
                .flatMap(managementProcessId -> collectionStagingDataPersistencePort.validateAndUpdateCollectionDataForSubmission(managementProcessId, requestDTO.getSamityId(), requestDTO.getLoginId()))
                .map(data -> CollectionMessageResponseDTO.builder()
                        .userMessage("Collection Data is successfully submitted for Samity")
                        .build())
                .doOnSuccess(responseDTO -> log.info("Collection Submission Response: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Collection Submission: {}", throwable.getMessage()));
    }

    private Mono<String> getManagementProcessIdAndValidateCollectionDataForSubmission(String officeId, String samityId) {
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
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office"))))
                .flatMap(officeEventList -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcess.get().getManagementProcessId(), samityId)
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .filter(samityEventList -> samityEventList.contains(SamityEvents.COLLECTED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Not Collection Data Found For Samity"))))
                .map(samityEventList -> managementProcess.get().getManagementProcessId());
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
        return collectionStagingDataPersistencePort.lockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return collectionStagingDataPersistencePort.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
        return collectionStagingDataPersistencePort.getSamityIdListLockedByUserForAuthorization(lockedBy);
    }

    @Override
    public Mono<Map<String, BigDecimal>> getTotalCollectionAmountForSamityIdList(List<String> samityIdList) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataBySamityIdList(samityIdList)
                .map(collectionStagingDataList -> {
                    Map<String, BigDecimal> samityWithTotalCollection = new HashMap<>();
                    samityIdList.forEach(samityId -> {
                        BigDecimal totalAmount = collectionStagingDataList.stream().filter(collectionStagingData -> collectionStagingData.getSamityId().equals(samityId))
                                .map(CollectionStagingData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityWithTotalCollection.put(samityId, totalAmount);
                    });
                    return samityWithTotalCollection;
                });
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return collectionStagingDataPersistencePort.validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return collectionStagingDataPersistencePort.validateAndUpdateCollectionStagingDataForRejectionBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<List<CollectionStagingData>> getAllCollectionDataBySamityIdList(List<String> samityIdList) {
        return collectionStagingDataPersistencePort.getAllCollectionDataBySamityIdList(samityIdList)
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return collectionStagingDataPersistencePort.validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(managementProcessId, samityId, loginId)
                .map(response -> samityId);
    }

    @Override
    public Mono<StagingAccountData> getStagingAccountDataByLoanAccountId(String loanAccountId, String managementProcessId) {
        return stagingAccountDataPersistencePort.getStagingAccountDataByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId)
                .switchIfEmpty(stagingAccountDataHistoryRepository.getStagingAccountDataHistoryByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId)
                        .map(dto -> modelMapper.map(dto, StagingAccountData.class)))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Account Staging Data Found For Loan Account")));
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId,
                                                                               String managementProcessId,
                                                                               String processId, String version) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId, processId)
                .filter(collectionStagingData -> collectionStagingData.getCurrentVersion().equalsIgnoreCase(version))
                .switchIfEmpty(collectionStagingDataHistoryAdapter.getCollectionStagingDataByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId, processId)
                        .filter(collectionStagingData -> collectionStagingData.getCurrentVersion().equalsIgnoreCase(version))
                        .map(dto -> modelMapper.map(dto, CollectionStagingData.class)))
                .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()));
    }

    private Mono<CollectionGridViewOfOfficeResponseDTO> buildGridViewResponseDTOOfOffice(Tuple2<List<String>, ManagementProcessTracker> tuple2, AtomicInteger totalSamity) {
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(tuple2.getT2().getManagementProcessId(), tuple2.getT2().getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .collectList()
                .flatMap(list -> {
                    if (list.stream().anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
                        return this.buildGridViewOfSamityResponse(tuple2);
                    }
                    List<CollectionGridViewOfSamityResponseDTO> emptyList = new ArrayList<>();
                    totalSamity.set(0);
                    return Mono.just(emptyList);
                })
                .map(list -> CollectionGridViewOfOfficeResponseDTO.builder()
                        .officeId(tuple2.getT2().getOfficeId())
                        .officeNameEn(tuple2.getT2().getOfficeNameEn())
                        .officeNameBn(tuple2.getT2().getOfficeNameBn())
                        .businessDate(tuple2.getT2().getBusinessDate())
                        .businessDay(tuple2.getT2().getBusinessDay())
                        .data(list)
                        .totalCount(totalSamity.get())
                        .build());
    }

    private Mono<List<CollectionGridViewOfSamityResponseDTO>> buildGridViewOfSamityResponse(Tuple2<List<String>, ManagementProcessTracker> tuple2) {
        return stagingDataUseCase.getStagingProcessTrackerListBySamityIdList(tuple2.getT2().getManagementProcessId(), tuple2.getT1())
                .map(entityList -> entityList.stream()
                        .map(entity -> {
                            CollectionGridViewOfSamityResponseDTO responseDTO = gson.fromJson(entity.toString(), CollectionGridViewOfSamityResponseDTO.class);
                            responseDTO.setType(entity.getIsDownloaded().equalsIgnoreCase("Yes") ? "Offline" : "Online");
                            return responseDTO;
                        })
                        .toList())
                .flatMapIterable(list -> list)
                .flatMap(samityObject -> samityEventTrackerUseCase.getAllSamityEventsForSamity(tuple2.getT2().getManagementProcessId(), samityObject.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .map(samityEventList -> {
                            if (!samityEventList.isEmpty()) {
                                if (samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue()))) {
                                    samityObject.setBtnOpenEnabled("No");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnCommitEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                    samityObject.setStatus("Samity Canceled");
                                    samityObject.setType(null);
                                } else if (samityEventList.stream().anyMatch(samityEvent ->
                                        samityEvent.equals(SamityEvents.COLLECTED.getValue()))) {
                                    if (samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue()))) {
                                        samityObject.setBtnOpenEnabled("No");
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnCommitEnabled("No");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                        samityObject.setStatus("Collection Authorized");
                                    } else {
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnCommitEnabled("Yes");
                                        samityObject.setBtnEditEnabled("Yes");
                                        samityObject.setBtnSubmitEnabled("Yes");
                                        samityObject.setBtnOpenEnabled("No");
                                        samityObject.setStatus("Collection Completed");
                                    }
                                } else {
                                    samityObject.setBtnOpenEnabled("Yes");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnCommitEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                    samityObject.setStatus("Collection Incomplete");
                                }
                            } else {
                                samityObject.setBtnOpenEnabled("Yes");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnCommitEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                                samityObject.setStatus("Collection Incomplete");
                            }
                            if (samityObject.getTotalMember() == null || samityObject.getTotalMember() == 0) {
                                samityObject.setTotalMember(0);
                                samityObject.setType(null);
                                samityObject.setBtnOpenEnabled("No");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnCommitEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                                samityObject.setStatus("Collection Unavailable");
                                samityObject.setRemarks("No member found in Samity!");
                            }
                            return samityObject;
                        }))
                .flatMap(samityObject -> collectionStagingDataPersistencePort.getAllCollectionDataBySamity(samityObject.getSamityId())
                        .filter(collectionStagingData -> collectionStagingData.getCollectionType().equals(CollectionType.REGULAR.getValue()) || collectionStagingData.getCollectionType().equals(CollectionType.SPECIAL.getValue()))
                        .collectList()
                        .map(collectionStagingDataList -> {
                            if (!collectionStagingDataList.isEmpty()) {
                                if (samityObject.getStatus().equals("Collection Completed")) {
                                    if (collectionStagingDataList.stream().allMatch(item -> item.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))) {
                                        samityObject.setBtnCommitEnabled("No");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                        samityObject.setStatus("Collection Unauthorized");
                                    } else if (collectionStagingDataList.stream().allMatch(item -> item.getStatus().equals(Status.STATUS_REJECTED.getValue()))) {
                                        if (collectionStagingDataList.stream().allMatch(item -> item.getIsSubmitted().equals("Yes") || item.getEditCommit().equals("Yes"))) {
                                            samityObject.setBtnCommitEnabled("No");
                                            samityObject.setBtnEditEnabled("No");
                                            samityObject.setBtnSubmitEnabled("No");
                                            samityObject.setStatus("Collection Submitted");
                                        } else {
                                            samityObject.setBtnCommitEnabled("Yes");
                                            samityObject.setBtnEditEnabled("Yes");
                                            samityObject.setBtnSubmitEnabled("Yes");
                                            samityObject.setStatus("Collection Rejected");
                                        }
                                    } else if (collectionStagingDataList.stream().allMatch(item -> item.getIsLocked().equals("Yes"))) {
                                        samityObject.setBtnCommitEnabled("No");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                        samityObject.setStatus("Collection Locked");
                                    } else if (collectionStagingDataList.stream().allMatch(item -> item.getIsSubmitted().equals("Yes") || item.getEditCommit().equals("Yes"))) {
                                        samityObject.setBtnCommitEnabled("No");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                        samityObject.setStatus("Collection Submitted");
                                    } else {
                                        samityObject.setBtnCommitEnabled("Yes");
                                        samityObject.setBtnEditEnabled("Yes");
                                        samityObject.setBtnSubmitEnabled("Yes");
                                    }
                                }
                                BigDecimal loanCollection = collectionStagingDataList.stream()
                                        .filter(collectionStagingData -> !HelperUtil.checkIfNullOrEmpty(collectionStagingData.getLoanAccountId()))
                                        .map(CollectionStagingData::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                BigDecimal savingsCollection = collectionStagingDataList.stream()
                                        .filter(collectionStagingData -> !HelperUtil.checkIfNullOrEmpty(collectionStagingData.getSavingsAccountId()))
                                        .map(CollectionStagingData::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                samityObject.setLoanCollection(loanCollection);
                                samityObject.setSavingsCollection(savingsCollection);
                                samityObject.setTotalCollectionAmount(loanCollection.add(savingsCollection));
                            } else {
                                samityObject.setLoanCollection(BigDecimal.ZERO);
                                samityObject.setSavingsCollection(BigDecimal.ZERO);
                                samityObject.setTotalCollectionAmount(BigDecimal.ZERO);
                            }
                            return samityObject;
                        }))
                .flatMap(samityObject -> collectionStagingDataPersistencePort.getTotalCollectionBySamity(samityObject.getSamityId())
                        .map(totalCollection -> {
                            samityObject.setTotalCollectionAmount(totalCollection);
                            return samityObject;
                        }))
                .sort(Comparator.comparing(CollectionGridViewOfSamityResponseDTO::getSamityId))
                .collectList();
    }

    private Mono<CollectionGridViewOfFieldOfficerResponseDTO> buildGridViewResponseOfFieldOfficer(CollectionGridViewOfFieldOfficerResponseDTO responseDTO, String fieldOfficerId) {
        if (responseDTO.getData().isEmpty()) {
            return commonRepository.getFieldOfficerByFieldOfficerId(fieldOfficerId)
                    .map(fieldOfficerEntity -> {
                        responseDTO.setFieldOfficerId(fieldOfficerEntity.getFieldOfficerId());
                        responseDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
                        responseDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
                        return responseDTO;
                    });
        }
        responseDTO.setFieldOfficerId(responseDTO.getData().get(0).getFieldOfficerId());
        responseDTO.setFieldOfficerNameEn(responseDTO.getData().get(0).getFieldOfficerNameEn());
        responseDTO.setFieldOfficerNameBn(responseDTO.getData().get(0).getFieldOfficerNameBn());
        return Mono.just(responseDTO);
    }

    private Mono<List<GridViewDataObject>> getRegularCollectionDataForAuthorization(List<String> samityIdList) {
        return Flux.fromIterable(samityIdList)
                .flatMap(samityId -> stagingDataPersistencePort.getOneStagingDataEntityBySamityId(samityId)
                        .map(stagingData -> {
                            GridViewDataObject gridViewDataObject = gson.fromJson(stagingData.toString(), GridViewDataObject.class);
                            gridViewDataObject.setType(
                                    !HelperUtil.checkIfNullOrEmpty(gridViewDataObject.getDownloadedBy()) ? "Online" : "Offline"
                            );
                            return gridViewDataObject;
                        }))
                .flatMap(gridViewDataObject -> collectionStagingDataPersistencePort.getAllCollectionDataBySamity(gridViewDataObject.getSamityId())
                        .collectList()
                        .map(list -> {
                            if (list.isEmpty()) {
                                gridViewDataObject.setStatus(null);
                            } else {
                                if (list.stream().allMatch(item -> item.getEditCommit().equals("Yes"))) {
                                    gridViewDataObject.setStatus("Authorization Incomplete");
                                } else if (list.stream().allMatch(item -> item.getStatus().equals("Approved"))) {
                                    gridViewDataObject.setStatus("Authorization Completed");
                                }
                                gridViewDataObject.setUploadedBy(list.get(0).getUploadedBy());
                                gridViewDataObject.setUploadedOn(list.get(0).getUploadedOn());
                            }
                            return gridViewDataObject;
                        })
                )
                .filter(gridViewDataObject -> !HelperUtil.checkIfNullOrEmpty(gridViewDataObject.getStatus()))
                .flatMap(this::getTotalCollectionForASamity)
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(GridViewDataObject::getSamityId));
                    return list;
                });
    }

    private Boolean checkIfCollectionDataIsValidForAuthorizationGridView(CollectionStagingDataEntity entity) {
        if (!HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) && entity.getEditCommit().equals("Yes")) {
            return true;
        } else {
            return !HelperUtil.checkIfNullOrEmpty(entity.getStatus()) && entity.getStatus().equals(Status.STATUS_APPROVED.getValue());
        }
    }

    private Mono<CollectionGridViewByOfficeResponseDTO> buildCollectionGridViewByOfficeResponseDTO(List<GridViewDataObject> list, CollectionDataRequestDTO request) {
        return commonRepository.getOfficeEntityByOfficeId(request.getOfficeId())
                .map(officeEntity -> CollectionGridViewByOfficeResponseDTO.builder()
                        .officeId(officeEntity.getOfficeId())
                        .officeNameEn(officeEntity.getOfficeNameEn())
                        .officeNameBn(officeEntity.getOfficeNameBn())
                        .data(list)
                        .build());
    }

    private Mono<CollectionGridViewByFieldOfficerResponseDTO> getCollectionStagingDataGridViewResponseForSpecialCollection(CollectionDataRequestDTO request, String samityDay, String requiredStatus, String managementProcessId) {
        return stagingDataPersistencePort
                .getListOfStagingDataByFieldOfficerIdForNonSamityDay(request.getFieldOfficerId(), samityDay)
                .doOnComplete(() -> log.info("getListOfStagingDataByFieldOfficerIdAndSamityDay executed"))
                .distinct(StagingData::getSamityId)
                .doOnComplete(() -> log.info("groupBy executed"))
                .map(this::buildResponseNestedObjectForGridViewResponse)
                .flatMap(this::getTotalCollectionForASamity)
//        .flatMap(gridViewDataObject -> this.setCompletionStatus(gridViewDataObject, requiredStatus))
//                .flatMap(gridViewDataObjectMono -> gridViewDataObjectMono)
                .flatMap(gridViewDataObject -> this.setCompletionStatusForCollectionCompletion(gridViewDataObject, managementProcessId))
                .filter(gridViewDataObjects -> gridViewDataObjects.getStatus() != null && !gridViewDataObjects.getStatus().isEmpty())
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(GridViewDataObject::getSamityId));
                    return list;
                })
                .doOnNext(list -> log.info("List of staging data for collection: {}", list))
                .doOnSuccess(gridViewDataObjects -> log.info("Combined List - {}", gridViewDataObjects))
                .flatMap(
                        gridViewDataObjects ->
                                employeePersistencePort
                                        .getEmployeeDetailByEmployeeId(request.getFieldOfficerId())
                                        .map(employee -> buildCollectionStagingDataGridViewResponse(gridViewDataObjects, request, employee))
                )
                .doOnSuccess(collectionStagingDataGridViewResponse -> log.info("CollectionStagingDataGridViewResponse - {}", collectionStagingDataGridViewResponse));
    }

    private Flux<String> getMemberIdListFromSamityIdList(GridViewDataObject data) {
        return stagingDataPersistencePort.getListOfStagingDataBySamityId(data.getSamityId())
                .map(StagingData::getMemberId);
    }

    private Mono<AuthorizationGridViewResponseDTO> getAuthorizationResponseDTO(List<AuthorizationGridViewResponseDTO> list, String officeId) {

        log.info("list received : {}", list);

        List<GridViewDataObject> data = new ArrayList<>();
        AuthorizationGridViewResponseDTO responseDTO = new AuthorizationGridViewResponseDTO();
        /*list
                .stream()
                .map(AuthorizationGridViewResponseDTO::getData)
                .map(gridViewDataObjects -> gridViewDataObjects.stream().map(data::add));*/

        list.stream()
                .map(AuthorizationGridViewResponseDTO::getData)
                .forEach(data::addAll);

        log.info("list TO RETURN: {}", data);
        return Mono.just(AuthorizationGridViewResponseDTO
                .builder()
                .officeId(officeId)
                .data(data)
                .totalCount(data.size())
                .build());
    }

    private Mono<List<String>> getAccountListsByMemberIdList(List<String> memberId) {
        return stagingAccountDataPersistencePort
                .getAllStagingAccountDataByListOfMemberId(memberId)
                .map(stagingAccountData -> stagingAccountData.getLoanAccountId() != null ? stagingAccountData.getLoanAccountId() : stagingAccountData.getSavingsAccountId())
                .collectList();
    }

    private Mono<GridViewDataObject> setCompletionStatus(GridViewDataObject data, String requiredStatus) {
        return getMemberIdListFromSamityIdList(data)
                .collectList()
                .flatMap(listOfMemberId -> getAccountListsByMemberIdList(listOfMemberId)
                                .flatMap(listOfAccounts -> collectionStagingDataPersistencePort
                                                .getCountOfCollectionStagingDataByAccountIdList(listOfAccounts)
                                                .map(count -> {
//                                  String type = requiredStatus.equals(Status.STATUS_STAGED.getValue()) ? "Collection " : "Authorization ";
                                                    if (count == listOfAccounts.size() && listOfAccounts.size() > 0)
                                                        data.setStatus("Collection Completed");
                                                    else if (count != listOfAccounts.size() && listOfAccounts.size() > 0)
                                                        data.setStatus("Collection Incomplete");
                                                    return data;
                                                })
                                )
                );
    }

    private Mono<GridViewDataObject> setCompletionStatusForCollectionCompletion(GridViewDataObject data, String managementProcessId) {
        return samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(managementProcessId, data.getSamityId(), SamityEvents.COLLECTED.getValue())
                .map(samityEventTracker -> {
                    data.setStatus(
                            !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) && samityEventTracker.getSamityEvent().equals(SamityEvents.COLLECTED.getValue()) ?
                                    "Collection Completed" : "Collection Incomplete"
                    );
                    return data;
                });
    }


    private CollectionGridViewByFieldOfficerResponseDTO buildCollectionStagingDataGridViewResponse(List<GridViewDataObject> data, CollectionDataRequestDTO requestBody, Employee employee) {
        return CollectionGridViewByFieldOfficerResponseDTO
                .builder()
                .officeId(requestBody.getOfficeId())
                .fieldOfficerId(requestBody.getFieldOfficerId())
                .fieldOfficerNameEn(employee.getEmpNameEn())
                .fieldOfficerNameBn(employee.getEmpNameBn())
                .data(data)
                .totalCount(data.size())
                .build();
    }

    private AuthorizationGridViewResponseDTO buildAuthorizationGridViewResponse(List<GridViewDataObject> data, CollectionDataRequestDTO requestBody, Employee employee) {
        log.info("buildAuthorizationGridViewResponse : {}", data);

        return AuthorizationGridViewResponseDTO
                .builder()
                .officeId(requestBody.getOfficeId())
                .data(data)
                .totalCount(data.size())
                .build();
    }

    private Mono<GridViewDataObject> getTotalCollectionForASamity(GridViewDataObject gridViewDataObject) {
        return collectionStagingDataPersistencePort.getTotalCollectionBySamity(gridViewDataObject.getSamityId())
                .map(totalCollection -> {
                    gridViewDataObject.setTotalCollectionAmount(totalCollection);
                    return gridViewDataObject;
                });
    }

    private GridViewDataObject buildResponseNestedObjectForGridViewResponse(StagingData data) {
        return GridViewDataObject
                .builder()
                .fieldOfficerId(data.getFieldOfficerId())
                .fieldOfficerNameEn(data.getFieldOfficerNameEn())
                .fieldOfficerNameBn(data.getFieldOfficerNameBn())
                .samityId(data.getSamityId())
                .samityNameEn(data.getSamityNameEn())
                .samityNameBn(data.getSamityNameBn())
                .samityDay(data.getSamityDay())
                .totalMember(data.getTotalMember())
                .mfiId(data.getMfiId())
                .downloadedBy(data.getDownloadedBy())
                .downloadedOn(data.getDownloadedOn())
                .type((data.getDownloadedBy() == null && data.getDownloadedOn() == null) ? "Online" : "Offline")
                .build();

    }

    private Mono<CollectionStagingDataDetailViewResponse> buildCollectionStagingDataDetailViewWithLoanAccountInfo(CollectionStagingDataDetailViewResponse response) {
//        Mono<List<MemberInfoDTO>> memberInfoList = this.sortMemberInfoDTOByMemberId(response.getMemberList());

        return Mono.zip(Mono.just(response), this.sortedMemberInfoListByMemberId(response.getMemberList()))
                .map(tuple -> {
                    tuple.getT1().setMemberList(tuple.getT2());
                    return tuple.getT1();
                })
                .doOnNext(collectionStagingDataAccountDetailViewResponse -> log.debug("collectionStagingDataAccountDetailViewResponse : {}", collectionStagingDataAccountDetailViewResponse))
                .doOnError(throwable -> log.error("Error in buildCollectionStagingDataDetailViewWithLoanAccountInfo, Error Message: {}", throwable.getMessage()));
    }

    private Mono<CollectionStagingDataAccountDetailViewResponse> buildCollectionStagingDataAccountDetailViewWithLoanAccountInfo(CollectionStagingDataAccountDetailViewResponse response) {
//        Mono<List<MemberInfoDTO>> memberInfoList = this.sortMemberInfoDTOByMemberId(response.getMemberList());

        return Mono.zip(Mono.just(response), this.sortedMemberInfoListByMemberId(response.getMemberList()))
                .map(tuple -> {
                    tuple.getT1().setMemberList(tuple.getT2());
                    return tuple.getT1();
                })
                .doOnNext(collectionStagingDataAccountDetailViewResponse -> log.info("collectionStagingDataAccountDetailViewResponse : {}", collectionStagingDataAccountDetailViewResponse));
    }

    private Mono<List<MemberInfoDTO>> sortedMemberInfoListByMemberId(List<MemberInfoDTO> memberInfoDTOList) {
        return Flux.fromIterable(memberInfoDTOList)
                .flatMap(this::getCollectionMemberInfo)
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(MemberInfoDTO::getMemberId));
                    return list;
                });
    }

    private Mono<MemberInfoDTO> getCollectionMemberInfo(MemberInfoDTO memberInfo) {
        return Mono.zip(Mono.just(memberInfo), getCollectionMemberInfoForLoanAccounts(memberInfo), getCollectionMemberInfoSavingsAccounts(memberInfo))
                .map(tuple -> {
                    tuple.getT1().setLoanAccountList(tuple.getT2());
                    tuple.getT1().setSavingsAccountList(tuple.getT3());

                    log.info("Loan Account List {}", tuple.getT1().getLoanAccountList());
                    log.info("Savings Account List {}", tuple.getT1().getSavingsAccountList());
                    return tuple.getT1();
                });
    }

    private Mono<List<CollectionStagingLoanAccountInfoDTO>> getCollectionMemberInfoForLoanAccounts(MemberInfoDTO memberInfo) {
        return Flux.fromIterable(memberInfo.getLoanAccountList())
                .flatMap(loanAccount -> this.getCollectionMemberInfoByLoanAccountId(loanAccount.getLoanAccountId())
                        .zipWith(loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(loanAccount.getLoanAccountId()))
                        .flatMap(tuple -> loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(loanAccount.getLoanAccountId())
                                .switchIfEmpty(Mono.just(LoanWaiver.builder().build()))
                                .zipWith(loanRebatePersistencePort.getLoanRebateByLoanAccountId(loanAccount.getLoanAccountId())
                                        .switchIfEmpty(Mono.just(LoanRebate.builder().build())))
                                .map(waiverAndRebate -> {
                                    BigDecimal totalPrincipalRemaining = loanAccount.getTotalPrincipalRemaining();
                                    BigDecimal totalServiceChargeRemaining = loanAccount.getTotalServiceChargeRemaining();
                                    BigDecimal adjustedAmount = tuple.getT2().getAmount() == null ? BigDecimal.ZERO : tuple.getT2().getAmount();
                                    BigDecimal outstandingAmount = totalPrincipalRemaining.add(totalServiceChargeRemaining).subtract(adjustedAmount);
                                    BigDecimal waiverAmount = waiverAndRebate.getT1().getWaivedAmount() == null ? BigDecimal.ZERO : waiverAndRebate.getT1().getWaivedAmount();
                                    BigDecimal rebateAmount = waiverAndRebate.getT2().getRebateAmount() == null ? BigDecimal.ZERO : waiverAndRebate.getT2().getRebateAmount();
                                    BigDecimal finalOutstandingAmount = (waiverAmount.compareTo(BigDecimal.ZERO) == 0 && rebateAmount.compareTo(BigDecimal.ZERO) == 0) ? outstandingAmount : BigDecimal.ZERO;

                                    loanAccount.setAmount(tuple.getT1().getAmount());
                                    loanAccount.setPaymentMode(tuple.getT1().getPaymentMode());
                                    loanAccount.setCollectionType(tuple.getT1().getCollectionType());
                                    loanAccount.setUploadedBy(tuple.getT1().getUploadedBy());
                                    loanAccount.setUploadedOn(tuple.getT1().getUploadedOn());
                                    loanAccount.setStatus(tuple.getT1().getStatus());
                                    loanAccount.setIsCollectionCompleted(tuple.getT1().getIsCollectionCompleted());
                                    loanAccount.setAccountOutstanding(finalOutstandingAmount);
                                    return loanAccount;
                                }))
                )
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(CollectionStagingLoanAccountInfoDTO::getLoanAccountId));
                    return list;
                });
    }

    private Mono<List<CollectionStagingSavingsAccountInfoDTO>> getCollectionMemberInfoSavingsAccounts(MemberInfoDTO memberInfo) {
        return Flux.fromIterable(memberInfo.getSavingsAccountList())
                .flatMap(savingsAccount -> this.getCollectionMemberInfoBySavingsAccountId(savingsAccount.getSavingsAccountId())
                        .zipWith(savingsAccountUseCase.getSavingsAccountDetailsBySavingsAccountId(savingsAccount.getSavingsAccountId()))
                        .map(tupleOfStagingSavingsDataAndSavingsAccData -> {
                            savingsAccount.setAmount(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getAmount());
                            savingsAccount.setPaymentMode(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getPaymentMode());
                            savingsAccount.setCollectionType(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getCollectionType());
                            savingsAccount.setUploadedBy(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getUploadedBy());
                            savingsAccount.setUploadedOn(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getUploadedOn());
                            savingsAccount.setStatus(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getStatus());
                            savingsAccount.setIsCollectionCompleted(tupleOfStagingSavingsDataAndSavingsAccData.getT1().getIsCollectionCompleted());
                            savingsAccount.setSavingsTypeId(tupleOfStagingSavingsDataAndSavingsAccData.getT2().getSavingsTypeId());
                            log.info("Saving Account After Setting Collections {}", savingsAccount);
                            log.info("Collection Member Info  {}", tupleOfStagingSavingsDataAndSavingsAccData);
                            return savingsAccount;
                        }))
                .collectList()
                .map(list -> {
                    list.sort(Comparator.comparing(CollectionStagingSavingsAccountInfoDTO::getSavingsAccountId));
                    return list;
                });
    }

    private Mono<CollectionStagingLoanAccountInfoDTO> getCollectionMemberInfoByLoanAccountId(String accountId) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(accountId)
                .map(collectionStagingData -> mapper.map(collectionStagingData, CollectionStagingLoanAccountInfoDTO.class))
                .switchIfEmpty(Mono.just(CollectionStagingLoanAccountInfoDTO.builder().build()));
//                .flatMap(collectionStagingLoanAccountInfoDTO -> {
//                    log.info("Loan Account Info: {}", collectionStagingLoanAccountInfoDTO);
//                    return loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(accountId)
//                            .map(loanAdjustmentData -> {
//                                collectionStagingLoanAccountInfoDTO.setAccountOutstanding(
//                                        collectionStagingLoanAccountInfoDTO.getTotalPrincipalRemaining().add(collectionStagingLoanAccountInfoDTO.getTotalServiceChargeRemaining())
//                                                .subtract(loanAdjustmentData.getAmount() == null ? BigDecimal.ZERO : loanAdjustmentData.getAmount())
//                                );
//                                return collectionStagingLoanAccountInfoDTO;
//                            });
//
//                });
//                .flatMap(collectionStagingData -> repaymentSchedulePersistencePort.getFirstPendingRepaymentScheduleByLoanAccountId(collectionStagingData.getLoanAccountId())
//                        .switchIfEmpty(Mono.just(RepaymentSchedule.builder().build()))
//                        .map(repaymentSchedule -> {
//                            log.info("Repayment Schedule Pending amount: {}", repaymentSchedule);
//                            collectionStagingData.setScheduledInstallmentAmount(repaymentSchedule.getTotalPayment() == null ? BigDecimal.ZERO : repaymentSchedule.getTotalPayment());
//                            return collectionStagingData;
//                        }));
    }

    private Mono<CollectionStagingSavingsAccountInfoDTO> getCollectionMemberInfoBySavingsAccountId(String accountId) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(accountId)
                .map(collectionStagingData -> mapper.map(collectionStagingData, CollectionStagingSavingsAccountInfoDTO.class))
                .switchIfEmpty(Mono.just(CollectionStagingSavingsAccountInfoDTO.builder().build()));
    }

    private Mono<CollectionStagingDataDetailViewResponse> setFlagsForCollectionStagingDataDetailView(CollectionStagingDataDetailViewResponse response, String loginId) {
        return collectionStagingDataPersistencePort.getAllCollectionDataBySamityId(response.getSamityId())
                .map(list -> {
                    if (!list.isEmpty()) {
                        response.setIsCollected("Yes");
                        response.setCollectedBy(list.get(0).getCreatedBy());
                        response.setIsLocked(list.stream().allMatch(item -> item.getIsLocked().equals("Yes")) ? "Yes" : "No");
                        response.setLockedBy(list.get(0).getLockedBy());
                        response.setIsCommitted(list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getEditCommit()) && item.getEditCommit().equals("Yes")) ? "Yes" : "No");
                        response.setIsEditable(list.stream().allMatch(item -> item.getIsSubmitted().equals("Yes")) ? "No" : "Yes");
                        response.setIsSubmitted(list.stream().allMatch(item -> item.getIsSubmitted().equals("Yes")) ? "Yes" : "No");
                        response.setCommittedBy(list.get(0).getSubmittedBy());
                        response.setSubmittedBy(list.get(0).getSubmittedBy());

                        if (list.stream().allMatch(item -> item.getIsSubmitted().equals("Yes"))) {
                            response.setBtnEditEnabled("No");
                            response.setBtnSubmitEnabled("No");
                        } else {
                            response.setBtnSubmitEnabled("Yes");
                            response.setBtnEditEnabled("Yes");
                        }
                    } else {
                        response.setIsCollected("No");
                    }
                    return response;
                });
    }

    @Override
    public Mono<CollectionGridResponse> getCollectionStagingGridView(CollectionStagingRequestDto request) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No management process found for office")))
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
                        .flatMap(collectionGridResponse -> getCollectionStagingDataCount(request, managementProcessTracker.getManagementProcessId())
                                .map(count -> {
                                    log.info("Total Count: {}", count);
                                    collectionGridResponse.setTotalCount(count);
                                    return collectionGridResponse;
                                })
                        )
                );
    }

    private Mono<String> getLoginIdByFieldOfficerId(CollectionStagingRequestDto request) {
        return employeePersistencePort.getEmployeeByEmployeeId(request.getFieldOfficerId().trim())
                .map(employee -> employee.getLoginId().trim());
    }

    private Mono<String> setRequestLoginId(CollectionStagingRequestDto request) {
        return HelperUtil.checkIfNullOrEmpty(request.getFieldOfficerId()) ? Mono.just(request.getFieldOfficerId().trim()) : getLoginIdByFieldOfficerId(request);
    }

    private Mono<Long> getCollectionStagingDataCount(CollectionStagingRequestDto request, String managementProcessId) {
        return setRequestLoginId(request).flatMap(loginId -> collectionStagingDataPersistencePort.collectionStagingDataCount(managementProcessId, request.getCollectionType().trim(), loginId));
    }

    private Flux<AccountDataInfo> getAccountDataInfo(CollectionStagingRequestDto request, String managementProcessId) {
        return setRequestLoginId(request)
                .flatMapMany(loginId -> collectionStagingDataPersistencePort.getAllCollectionStagingDataByManagementProcessIdAndLoginId(managementProcessId, request.getCollectionType().trim(), loginId, request.getLimit(), request.getOffset())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()))
                        .filter(withdrawStagingData -> withdrawStagingData.getSamityId() != null && withdrawStagingData.getSamityId().startsWith(request.getOfficeId()))
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()))
                        .flatMap(withdrawStagingData -> stagingDataUseCase.getStagingDataByStagingDataId(withdrawStagingData.getStagingDataId())
                                .map(stagingData -> buildAccountDataInfo(withdrawStagingData, stagingData))));
    }

    private AccountDataInfo buildAccountDataInfo(CollectionStagingData collectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData stagingData) {
        return AccountDataInfo.builder()
                .btnViewEnabled(Status.STATUS_YES.getValue())
//                .btnOpenEnabled("")
                .btnEditEnabled(collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
                .btnSubmitEnabled(collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
//                .btnCommitEnabled("")
                .oid(collectionStagingData.getOid())
                .collectionStagingDataId(collectionStagingData.getCollectionStagingDataId())
                .stagingDataId(stagingData.getStagingDataId())
                .memberId(stagingData.getMemberId())
                .memberNameEn(stagingData.getMemberNameEn())
                .memberNameBn(stagingData.getMemberNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .loanAccountId(collectionStagingData.getLoanAccountId())
                .savingsAccountId(collectionStagingData.getSavingsAccountId())
                .accountId(collectionStagingData.getLoanAccountId() != null ? collectionStagingData.getLoanAccountId() : collectionStagingData.getSavingsAccountId())
                .accountType(collectionStagingData.getAccountType())
                .collectionType(collectionStagingData.getCollectionType())
                .amount(collectionStagingData.getAmount())
                .paymentMode(collectionStagingData.getPaymentMode())
                .status(collectionStagingData.getStatus())
                .createdBy(collectionStagingData.getCreatedBy())
                .build();
    }

    @Override
    public Mono<CollectionDetailResponse> getCollectionStagingDetailView(CollectionStagingRequestDto request) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByOid(request.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found for oid: " + request.getId())))
                .flatMap(collectionStagingData -> {
                    if (collectionStagingData.getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_LOAN.getValue())) {
                        return getCollectionResponseForLoanAccount(collectionStagingData);
                    }
                    if (collectionStagingData.getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_SAVINGS.getValue())) {
                        return getCollectionResponseForSavingsAccount(collectionStagingData);
                    }
                    return Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Account type not found"));
                });
    }
    private Mono<CollectionDetailResponse> getCollectionResponseForSavingsAccount(CollectionStagingData collectionStagingData) {
        return commonRepository.getMemberEntityBySavingsAccountId(collectionStagingData.getSavingsAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No member found for Savings account id: " + collectionStagingData.getSavingsAccountId())))
                .map(this::extractMobileNumberFromMobileDetails)
                .flatMap(memberInfoDTO -> Mono.zip(
                        Mono.just(memberInfoDTO),
                        stagingDataUseCase.getStagingAccountDataBySavingsAccountId(collectionStagingData.getSavingsAccountId()),
                        stagingDataUseCase.getStagingDataByStagingDataId(collectionStagingData.getStagingDataId())
                ))
                .map(tuple -> buildSavingsAccountDetailResponse(collectionStagingData, tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private Mono<CollectionDetailResponse> getCollectionResponseForLoanAccount(CollectionStagingData collectionStagingData) {
        return commonRepository.getMemberInfoByLoanAccountId(collectionStagingData.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No member found for Loan account id: " + collectionStagingData.getLoanAccountId())))
                .map(this::extractMobileNumberFromMobileDetails)
                .flatMap(memberInfoDTO -> Mono.zip(
                                Mono.just(memberInfoDTO),
                                stagingDataUseCase.getStagingAccountDataByLoanAccountId(collectionStagingData.getLoanAccountId()),
                                passbookUseCase.getDisbursementPassbookEntryByDisbursedLoanAccountId(collectionStagingData.getLoanAccountId()),
                                stagingDataUseCase.getStagingDataByStagingDataId(collectionStagingData.getStagingDataId()),
                                loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(collectionStagingData.getLoanAccountId())
                                        .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().amount(BigDecimal.ZERO).build())),
                                getLoanRebateAndWaiverDataByLoanAccountId(collectionStagingData)
                        )
                )
                .map(tuple -> buildLoanAccountDetailResponse(collectionStagingData, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6()));
    }

    private Mono<Tuple2<LoanRebate, LoanWaiver>> getLoanRebateAndWaiverDataByLoanAccountId(CollectionStagingData collectionStagingData) {
        return loanRebatePersistencePort.getLoanRebateByLoanAccountId(collectionStagingData.getLoanAccountId())
                    .switchIfEmpty(Mono.just(LoanRebate.builder().rebateAmount(BigDecimal.ZERO).build()))
                .zipWith(loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(collectionStagingData.getLoanAccountId())
                        .switchIfEmpty(Mono.just(LoanWaiver.builder().waivedAmount(BigDecimal.ZERO).build())));
    }

    private CollectionDetailResponse buildSavingsAccountDetailResponse(CollectionStagingData collectionStagingData, MemberInfoDTO memberInfo, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData savingsAccountInfo, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData stagingData) {
        return CollectionDetailResponse.builder()
                .userMessage("Data fetch successfully")
                .fieldOfficerId(stagingData.getFieldOfficerId())
                .fieldOfficerNameEn(stagingData.getFieldOfficerNameEn())
                .fieldOfficerNameBn(stagingData.getFieldOfficerNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .collectionType(collectionStagingData.getCollectionType())
                .paymentMode(collectionStagingData.getPaymentMode())
                .amount(collectionStagingData.getAmount())
                .status(collectionStagingData.getStatus())
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
                                .balance(savingsAccountInfo.getBalance().add(collectionStagingData.getAmount()))
                                .savingsAvailableBalance(savingsAccountInfo.getSavingsAvailableBalance() == null ? BigDecimal.ZERO.add(collectionStagingData.getAmount()) : savingsAccountInfo.getSavingsAvailableBalance().add(collectionStagingData.getAmount()))
                                .build())
                        .build())
                .build();
    }

    private CollectionDetailResponse buildLoanAccountDetailResponse(CollectionStagingData collectionStagingData,
                                                                    MemberInfoDTO memberInfo,
                                                                    net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData loanAccountInfo,
                                                                    PassbookResponseDTO passbookInfo,
                                                                    net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData stagingData,
                                                                    LoanAdjustmentData loanAdjustmentData,
                                                                    Tuple2<LoanRebate, LoanWaiver> rebateAndWaiver) {
        log.info("Loan installments : {}", loanAccountInfo);
        BigDecimal rebateAmount = rebateAndWaiver.getT1().getRebateAmount() == null ? BigDecimal.ZERO : rebateAndWaiver.getT1().getRebateAmount();
        BigDecimal waivedAmount = rebateAndWaiver.getT2().getWaivedAmount() == null ? BigDecimal.ZERO : rebateAndWaiver.getT2().getWaivedAmount();

        BigDecimal loanOutstandingAmount = loanAccountInfo.getTotalPrincipalRemaining().add(loanAccountInfo.getTotalServiceChargeRemaining())
                .subtract(loanAdjustmentData.getAmount() == null ? BigDecimal.ZERO : loanAdjustmentData.getAmount());
        return CollectionDetailResponse.builder()
                .userMessage("Data fetch successfully")
                .fieldOfficerId(stagingData.getFieldOfficerId())
                .fieldOfficerNameEn(stagingData.getFieldOfficerNameEn())
                .fieldOfficerNameBn(stagingData.getFieldOfficerNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .collectionType(collectionStagingData.getCollectionType())
                .paymentMode(collectionStagingData.getPaymentMode())
                .amount(collectionStagingData.getAmount())
                .status(collectionStagingData.getStatus())
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
                        .loanAccountInfo(StagingLoanAccountInfoDTO.builder()
                                .oid(loanAccountInfo.getOid())
                                .loanAccountId(loanAccountInfo.getLoanAccountId())
                                .productCode(loanAccountInfo.getProductCode())
                                .productNameEn(loanAccountInfo.getProductNameEn())
                                .productNameBn(loanAccountInfo.getProductNameBn())
                                .totalDue(loanAccountInfo.getTotalDue())
                                .totalPrincipalPaid(loanAccountInfo.getTotalPrincipalPaid())
                                .totalPrincipalRemaining(loanAccountInfo.getTotalPrincipalRemaining())
                                .totalServiceChargePaid(loanAccountInfo.getTotalServiceChargePaid())
                                .totalServiceChargeRemaining(loanAccountInfo.getTotalServiceChargeRemaining())
                                .accountOutstanding(
                                        (rebateAmount.compareTo(BigDecimal.ZERO) == 0 && waivedAmount.compareTo(BigDecimal.ZERO) == 0) ?
                                                loanOutstandingAmount : BigDecimal.ZERO
                                )
                                .disbursementDate(passbookInfo.getDisbursementDate())
                                .installments(loanAccountInfo.getInstallments())
                                .scheduledInstallmentAmount(loanAccountInfo.getScheduledInstallmentAmount() == null ? BigDecimal.ZERO : loanAccountInfo.getScheduledInstallmentAmount())
                                .build())
                        .build())
                .build();
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
    public Mono<CollectionDetailResponse> editSpecialCollectionData(CollectionStagingRequestDto request) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByOid(request.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found for id : " + request.getId())))
                .filter(data -> request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) >= 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection amount must be positive!")))
                .flatMap(this::validateLoanRebateAndWaiver)
                .flatMap(collectionStagingData -> Mono.zip(
                        Mono.just(collectionStagingData),
                        getStagingAccountDataMono(collectionStagingData)
                ))
                .flatMap(tuple2 -> validateFdrAmountForCollection(tuple2, request))
                .flatMap(tuple2 -> validateDpsAmountForCollection(tuple2, request)
                        .zipWith(loanAdjustmentUseCase.loanAdjustmentCollectionByLoanAccountId(tuple2.getT1().getLoanAccountId())))
                .flatMap(tuple2 -> {
                    if (tuple2.getT1().getT1().getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_LOAN.getValue()))
                        return validateLoanAmountForCollectionData(request, tuple2.getT1().getT1(), tuple2.getT1().getT2(), tuple2.getT2());
                    else return Mono.just(tuple2.getT1().getT1());
                })
                .flatMap(collectionStagingData -> editHistoryPort.saveCollectionStagingDataEditHistory(buildEditHistoryDomain(gson.fromJson(gson.toJson(collectionStagingData), CollectionStagingData.class)))
                        .flatMap(history -> collectionStagingDataPersistencePort.saveCollectionStagingData(buildCollectionEditedData(request, collectionStagingData))))
                .as(rxtx::transactional)
                .map(collectionStagingData -> CollectionDetailResponse.builder()
                        .userMessage("Data edited successfully")
                        .build());
    }

    private Mono<CollectionStagingData> validateLoanRebateAndWaiver(CollectionStagingData collectionStagingData) {
        return loanRebatePersistencePort.getLoanRebateByLoanAccountId(collectionStagingData.getLoanAccountId())
                .filter(loanRebate -> loanRebate.getOid() == null)
                .flatMap(loanRebate -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Already Collected By Loan Rebate")))
                .switchIfEmpty(loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(collectionStagingData.getLoanAccountId())
                        .filter(loanWaiver -> loanWaiver.getOid() != null)
                        .flatMap(loanWaiver -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Already Collected By Loan Waiver")))
                        .switchIfEmpty(Mono.just(collectionStagingData)))
                .map(object -> collectionStagingData);
    }

    private Mono<Tuple2<CollectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData>> validateFdrAmountForCollection(Tuple2<CollectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> tupleOfCollectionStagingDataAndStagingAccountData, CollectionStagingRequestDto command) {
        log.info("FDR Collection Data: {}", tupleOfCollectionStagingDataAndStagingAccountData.getT1());
        log.info("FDR Account Data: {}", tupleOfCollectionStagingDataAndStagingAccountData.getT2());
        return Mono.just(tupleOfCollectionStagingDataAndStagingAccountData)
                .filter(tuple2 -> !HelperUtil.checkIfNullOrEmpty(tuple2.getT1().getAccountType()) && tuple2.getT1().getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_SAVINGS.getValue()))
                .filter(tuple2 -> !HelperUtil.checkIfNullOrEmpty(tuple2.getT2().getSavingsProductType()) && tuple2.getT2().getSavingsProductType().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue()))
                .filter(tuple2 -> command.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .switchIfEmpty(Mono.just(Tuples.of(CollectionStagingData.builder().build(), net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData.builder().build())))
                .flatMap(tuple2 -> {
                    if(StringUtils.isNotBlank(tuple2.getT1().getCollectionStagingDataId())){
                        log.info("FDR Account Data condition1: {}", !(command.getAmount().compareTo(BigDecimal.ZERO) > 0));
                        log.info("FDR Account Data condition2: {}", command.getAmount().compareTo(tuple2.getT2().getTargetAmount()) != 0);
                        if (!(command.getAmount().compareTo(BigDecimal.ZERO) > 0) ||command.getAmount().compareTo(tuple2.getT2().getTargetAmount()) != 0){
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate FDR amount for account: "+tuple2.getT2().getSavingsAccountId()));
                        }
                    }
                    return Mono.just(tupleOfCollectionStagingDataAndStagingAccountData);
                });
    }


    private Mono<Tuple2<CollectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData>> validateDpsAmountForCollection(Tuple2<CollectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> tupleOfCollectionStagingDataAndStagingAccountData, CollectionStagingRequestDto command) {
        log.info("DPS Collection Data: {}", tupleOfCollectionStagingDataAndStagingAccountData.getT1());
        log.info("DPS Account Data: {}", tupleOfCollectionStagingDataAndStagingAccountData.getT2());
        return Mono.just(tupleOfCollectionStagingDataAndStagingAccountData)
                .filter(tuple2 -> !HelperUtil.checkIfNullOrEmpty(tuple2.getT1().getAccountType()) && tuple2.getT1().getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_SAVINGS.getValue()))
                .filter(tuple2 -> !HelperUtil.checkIfNullOrEmpty(tuple2.getT2().getSavingsProductType()) && tuple2.getT2().getSavingsProductType().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue()))
                .filter(tuple2 -> command.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .switchIfEmpty(Mono.just(Tuples.of(CollectionStagingData.builder().build(), net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData.builder().build())))
                .flatMap(tuple2 -> {
                    if(StringUtils.isNotBlank(tuple2.getT1().getCollectionStagingDataId())) {
                        if (!(command.getAmount().compareTo(BigDecimal.ZERO) > 0) || command.getAmount().remainder(tuple2.getT2().getTargetAmount()).compareTo(BigDecimal.ZERO) != 0 || command.getAmount().compareTo(tuple2.getT2().getTargetAmount().multiply(BigDecimal.valueOf(tuple2.getT2().getDpsPendingInstallmentNo()))) > 0) {
                            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Failed to validate DPS amount for account: " + tuple2.getT2().getSavingsAccountId()));
                        }
                    }
                    return Mono.just(tupleOfCollectionStagingDataAndStagingAccountData);
                });
    }


    private Mono<net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData> getStagingAccountDataMono(CollectionStagingData collectionStagingData) {
        return collectionStagingData.getAccountType().equalsIgnoreCase(ACCOUNT_TYPE_LOAN.getValue()) ?
                stagingDataUseCase.getStagingAccountDataByLoanAccountId(collectionStagingData.getLoanAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan account staging data found for loan account id: " + collectionStagingData.getLoanAccountId()))) :
                stagingDataUseCase.getStagingAccountDataBySavingsAccountId(collectionStagingData.getSavingsAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account staging data found for savings account id: " + collectionStagingData.getSavingsAccountId())));
    }

    private CollectionStagingData buildCollectionEditedData(CollectionStagingRequestDto request, CollectionStagingData collectionStagingData) {
        collectionStagingData.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
        collectionStagingData.setCurrentVersion(String.valueOf(Integer.parseInt(collectionStagingData.getCurrentVersion()) + 1));
        collectionStagingData.setIsNew(NO.getValue());
        collectionStagingData.setUpdatedBy(request.getLoginId());
        collectionStagingData.setUpdatedOn(LocalDateTime.now());
        return collectionStagingData;
    }

    private CollectionStagingDataEditHistory buildEditHistoryDomain(CollectionStagingData data){
        CollectionStagingDataEditHistory editHistory = modelMapper.map(data, CollectionStagingDataEditHistory.class);
        editHistory.setCollectionStagingDataId(data.getOid());
        editHistory.setCollectionStagingDataEditHistoryId(UUID.randomUUID().toString());
        editHistory.setOid(null);
        return editHistory;
    }

    private Mono<CollectionStagingData> validateLoanAmountForCollectionData(CollectionStagingRequestDto request, CollectionStagingData collectionStagingData, net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData stagingAccountData, LoanAdjustmentData loanAdjustmentData) {
        BigDecimal principalRemaining = stagingAccountData.getTotalPrincipalRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalPrincipalRemaining();
        BigDecimal serviceChargeRemaining = stagingAccountData.getTotalServiceChargeRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalServiceChargeRemaining();
        BigDecimal loanAdjustmentAmount = loanAdjustmentData.getAmount() == null ? BigDecimal.ZERO : loanAdjustmentData.getAmount();
        BigDecimal loanAccountOutstanding = principalRemaining.add(serviceChargeRemaining).subtract(loanAdjustmentAmount);
        BigDecimal requestedAmount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
        log.info("Principal Remaining: {}, Service Charge Remaining: {}, Requested Amount: {}, loan Adjustment Amount: {}", principalRemaining, serviceChargeRemaining, requestedAmount, loanAdjustmentAmount);
        if (loanAccountOutstanding.compareTo(requestedAmount) >= 0 && requestedAmount.compareTo(BigDecimal.ZERO) >= 0) {
            return Mono.just(collectionStagingData);
        }
        else return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection amount must be positive and can not exceed loan outstanding amount..!"));
    }


    @Override
    public Mono<CollectionMessageResponseDTO> submitCollectionDataForAuthorizationByOid(CollectionDataRequestDTO requestDTO) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByOid(requestDTO.getOid())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found for oid: " + requestDTO.getOid())))
                .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getStatus()) && (data.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || data.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Data is already submitted or locked!")))
                .flatMap(collectionStagingData -> this.getManagementProcessIdAndValidateCollectionDataForSubmission(requestDTO.getOfficeId(), collectionStagingData.getSamityId())
                    .flatMap(managementProcessId -> collectionStagingDataPersistencePort.validateAndUpdateCollectionDataForSubmissionByOid( requestDTO.getLoginId(), requestDTO.getOid())))
                .map(data -> CollectionMessageResponseDTO.builder()
                        .userMessage("Collection Data is successfully submitted for the Collection")
                        .build())
                .doOnSuccess(responseDTO -> log.info("Collection Submission Response: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Collection Submission: {}", throwable.getMessage()));

    }

    @Override
    public Mono<CollectionMessageResponseDTO> submitCollectionDataEntity(CollectionEntitySubmitRequestDto requestDto) {
        return collectionStagingDataPersistencePort.getAllCollectionDataByOidList(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found for oid: " + requestDto.getId())))
                .collectList()
                .filter(dataList -> dataList.size() == requestDto.getId().size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection staging data not found.")))
                .flatMapMany(Flux::fromIterable)
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getCreatedBy()) && data.getCreatedBy().equalsIgnoreCase(requestDto.getLoginId()))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data can only be submitted by the creator!"));
                })
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getStatus()) && (data.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || data.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Data is already submitted or locked!"));
                })
                .flatMap(collectionStagingData -> this.getManagementProcessIdAndValidateCollectionDataForSubmission(requestDto.getOfficeId(), collectionStagingData.getSamityId())
                        .flatMap(managementProcessId -> collectionStagingDataPersistencePort.validateAndUpdateCollectionDataForSubmissionByOid(requestDto.getLoginId(), collectionStagingData.getOid())))
                .as(rxtx::transactional)
                .collectList()
                .map(data -> CollectionMessageResponseDTO.builder()
                        .userMessage("Collection Data  is successfully submitted.")
                        .build())
                .doOnRequest(responseDTO -> log.info("Collection entity Submit Response: {}", responseDTO))
                .doOnError(throwable -> log.error("Error in Collection entity Submission: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(loanAccountId)
                .doOnSuccess(data -> log.info("Collection Staging Data by loan account id: {}", data));
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataByManagementProcessIdAndProcessId(String loanAccountId, String managementProcessId, String processId) {
        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId, processId)
                .doOnSuccess(data -> log.info("Collection Staging Data by loan account id and management process id: {}", data));
    }

    @Override
    public Mono<Long> countCollectionStagingData(String managementProcessId, String samityId) {
        return collectionStagingDataPersistencePort.collectionStagingDataCount(managementProcessId, samityId);
    }
}
