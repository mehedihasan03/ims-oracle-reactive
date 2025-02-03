package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.StatusYesNo;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.LoanWaiverUseCase;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.*;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistenceHistoryPort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.service.helper.LoanWaiverAdjustmentAndCollectionProcessUtil;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.service.helper.LoanWaiverUtil;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart.ServiceChargeChart;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanWaiverService implements LoanWaiverUseCase {

    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
    private final TransactionalOperator rxtx;
    private final CommonRepository commonRepository;
    private final LoanWaiverPersistencePort loanWaiverPersistencePort;
    private final LoanWaiverPersistenceHistoryPort loanWaiverPersistenceHistoryPort;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final ModelMapper modelMapper;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final LoanWaiverUtil loanWaiverUtil;
    private final LoanWaiverAdjustmentAndCollectionProcessUtil loanWaiverAdjustmentAndCollectionProcessUtil;
    private final CommonValidation commonValidation;
    private final LoanAccountUseCase loanAccountUseCase;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;

    @Override
    public Mono<LoanWaiverGridViewResponseDTO> getLoanWaiverList(LoanWaiverGridViewRequestDTO requestDto) {
        Flux<LoanWaiver> filteredDataList = loanWaiverPersistencePort.getLoanWaiverList()
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan waiver data found")))
                .sort(Comparator.comparing(LoanWaiver::getCreatedOn).reversed())
                .flatMap(loanWaiver -> Mono.zip(Mono.just(loanWaiver), commonRepository.getSamityBySamityId(loanWaiver.getSamityId())))
                .filter(tuple -> tuple.getT2().getOfficeId().equals(requestDto.getOfficeId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan waiver data Found by Office Id")))
                .map(tuple -> tuple.getT1())
                .flatMap(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(requestDto.getSamityId()) ?
                        Mono.just(loanWaiver).filterWhen(lw -> Mono.just(lw.getSamityId().equals(requestDto.getSamityId()))) :
                        Mono.just(loanWaiver))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan waiver data Found by Samity Id")))
                .flatMap(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(requestDto.getStatus()) ?
                        Mono.just(loanWaiver).filterWhen(lw -> Mono.just(lw.getStatus().equals(requestDto.getStatus()))) :
                        Mono.just(loanWaiver))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan waiver data Found by Status")))
                .flatMap(loanWaiver -> requestDto.getFromDate() != null && requestDto.getToDate() != null ?
                        Mono.just(loanWaiver).filterWhen(lw -> Mono.just(lw.getCreatedOn().isAfter(requestDto.getFromDate()) &&
                                lw.getCreatedOn().isBefore(requestDto.getToDate().plusDays(1L)))) :
                        Mono.just(loanWaiver))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No loan waiver data Found by Date Range")));

        Mono<Long> totalCountMono = filteredDataList.count();

        return filteredDataList
                .skip(requestDto.getOffset() * requestDto.getLimit())
                .take(requestDto.getLimit())
                .flatMap(this::getMemberAndLoanAmountData)
                .collectList()
                .zipWith(commonRepository.getOfficeEntityByOfficeId(requestDto.getOfficeId()))
                .zipWith(totalCountMono)
                .map(tuple -> loanWaiverUtil.buildLoanWaiverGridViewListResponseDto(tuple.getT1().getT2(), tuple.getT1().getT1(), tuple.getT2().intValue()));
    }

    @Override
    public Mono<LoanWaiverDetailViewResponseDTO> getLoanWaiverDetailView(LoanWaiverDetailViewRequestDTO requestDto) {
        return loanWaiverPersistencePort.getLoanWaiverById(requestDto.getId())
                .switchIfEmpty(loanWaiverPersistenceHistoryPort.getLoanWaiverHistoryById(requestDto.getId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan waiver Data not found")))
                )
                .flatMap(loanWaiver -> Mono.zip(Mono.just(loanWaiver),
                        commonRepository.getMemberEntityByMemberId(loanWaiver.getMemberId()),
                        collectionStagingDataQueryUseCase.getStagingAccountDataByLoanAccountId(loanWaiver.getLoanAccountId(), loanWaiver.getManagementProcessId())
                                .map(stagingAccountData -> modelMapper.map(stagingAccountData, StagingAccountData.class))
                                .flatMap(stagingAccountData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
                                        return commonRepository.getDisbursementDateByLoanAccountId(stagingAccountData.getLoanAccountId())
                                                .map(disbursementDate -> {
                                                    stagingAccountData.setDisbursementDate(disbursementDate);
                                                    return stagingAccountData;
                                                });
                                    } else
                                        return Mono.just(stagingAccountData);
                                })
                                .doOnNext(collectionStagingData -> log.info("Retrieved staging account data: {}", collectionStagingData))
                                .doOnError(collectionStagingData -> log.error("error while staging account data: {}", collectionStagingData)),
                        loanWaiverAdjustmentAndCollectionProcessUtil.getLoanAdjustmentAndPaymentCollection(loanWaiver),
                        loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanWaiver.getLoanAccountId()),
                        serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(loanWaiver.getLoanAccountId()),
                        stagingDataUseCase.getStagingAccountDataListByMemberId(loanWaiver.getMemberId()).collectList()))
                .map(tuple -> loanWaiverUtil.buildLoanWaiverDetailViewListResponseDto(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5(), tuple.getT6(), tuple.getT7()));
    }

    @Override
    public Mono<LoanWaiverMemberDetailViewResponseDTO> getLoanWaiverMemberDetailView(LoanWaiverMemberDetailViewRequestDTO requestDto) {
        return stagingDataUseCase.getStagingAccountDataListByMemberId(requestDto.getMemberId())
                .collectList()
                .flatMap(stagingAccountDataList -> commonRepository.getMemberOfficeAndSamityEntityByMemberId(requestDto.getMemberId()).zipWith(getLoanAccountDetails(stagingAccountDataList))
                        .map(memberAndOfficeAndSamityEntityAndLoanAccountList -> LoanWaiverMemberDetailViewResponseDTO.builder()
                                .memberId(requestDto.getMemberId())
                                .memberNameEn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getMemberNameEn())
                                .memberNameBn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getMemberNameBn())
                                .samityId(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getSamityId())
                                .samityNameBn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getSamityNameBn())
                                .samityNameEn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getSamityNameEn())
                                .officeId(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getOfficeId())
                                .officeNameBn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getOfficeNameBn())
                                .officeNameEn(memberAndOfficeAndSamityEntityAndLoanAccountList.getT1().getOfficeNameEn())
                                .loanAccountList(memberAndOfficeAndSamityEntityAndLoanAccountList.getT2())
                                .savingsAccountList(loanWaiverUtil.getSavingsAccountDetails(stagingAccountDataList))
                                .build()))
                .flatMap(this::updateSavingsBalanceForLoanAdjustment);
    }

    @Override
    public Mono<LoanWaiverResponseDTO> createLoanWaiver(LoanWaiverCreateUpdateRequestDTO requestDto) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        AtomicReference<String> processId = new AtomicReference<>(UUID.randomUUID().toString());
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.validateIfWaiverIsPossibleForSamity(managementProcessTracker, requestDto.getOfficeId(), requestDto.getSamityId()))
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfLoanWaiverTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), requestDto.getSamityId())
                        .map(managementProcessId -> {
                            requestDto.setManagementProcessId(managementProcessId);
                            requestDto.setProcessId(processId.get());
                            return requestDto;
                        }))
                .flatMap(loanWaiverUtil::validateLoanWaiverCreateRequest)
                .flatMap(bool -> this.validateLoanWaiverRequestExistsByLoanAccountId(requestDto))
                .flatMap(bool -> this.validateIfLoanAccountIdIsValidForLoanWaiver(requestDto.getLoanAccountId()))
                .flatMap(string -> this.getStagingAccountDataAndStagingDataByLoanAccountId(requestDto.getLoanAccountId(), requestDto.getMemberId()))
                .flatMap(tuple -> loanWaiverUtil.validatePayment(requestDto, tuple.getT1().getTotalPrincipalRemaining().add(tuple.getT1().getTotalServiceChargeRemaining()))
                        .thenReturn(tuple))
                .flatMap(tuple -> loanWaiverPersistencePort.saveLoanWaiver(loanWaiverUtil.buildLoanWaiver(requestDto, tuple.getT1(), managementProcess.get()))
                        .map(loanWaiver -> {
                            loanWaiver.setStagingDataId(tuple.getT2().getStagingDataId());
                            return loanWaiver;
                        })
                        .flatMap(loanWaiver -> loanWaiverAdjustmentAndCollectionProcessUtil.createLoanAdjustmentAndPaymentCollection(requestDto, loanWaiver)
                                .doOnNext(response -> log.info("createLoanAdjustmentAndPaymentCollection response: {}", response))))
                .as(rxtx::transactional)
                .map(data -> LoanWaiverResponseDTO.builder()
                        .userMessage("Loan waiver Created Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan waiver Response: {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan waiver: {}", throwable.getMessage()));
    }

    Mono<Tuple2<StagingAccountData, StagingData>> getStagingAccountDataAndStagingDataByLoanAccountId(String loanAccountId, String memberId) {
        return stagingDataUseCase.getStagingAccountDataByLoanAccountId(loanAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan Account Data not found")))
                .flatMap(stagingAccountData -> stagingDataUseCase.getStagingDataByMemberId(memberId)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Staging Data Found For Member")))
                        .map(stagingData -> Tuples.of(stagingAccountData, stagingData)));
    }

    @Override
    public Mono<LoanWaiverResponseDTO> updateLoanWaiver(LoanWaiverCreateUpdateRequestDTO requestDto) {
        return Mono.just(requestDto)
                .flatMap(loanWaiverUtil::validateLoanWaiverUpdateRequest)
                .flatMap(bool -> this.validateLoanWaiverForUpdate(requestDto))
                .flatMap(loanWaiver -> this.getStagingAccountDataAndStagingDataByLoanAccountId(requestDto.getLoanAccountId(), loanWaiver.getMemberId())
                        .zipWith(Mono.just(loanWaiver)))
                .flatMap(tuple -> commonRepository.getManagementProcessTrackerByProcessID(tuple.getT2().getManagementProcessId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Management Process Data not found")))
                        .map(managementProcessTracker -> modelMapper.map(managementProcessTracker, ManagementProcessTracker.class))
                        .flatMap(managementProcessTracker -> this.validateIfWaiverIsPossibleForSamity(managementProcessTracker, requestDto.getOfficeId(), tuple.getT2().getSamityId()))
                        .flatMap(managementProcessTracker -> this.validateIfDayEndProcessIsStartedForOffice(managementProcessTracker))
                        .flatMap(managementProcessTracker -> this.validateIfLoanWaiverTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), tuple.getT2().getSamityId()))
                        .flatMap(managementProcessTracker -> loanWaiverUtil.validatePayment(requestDto,
                                tuple.getT1().getT1().getTotalPrincipalRemaining().add(tuple.getT1().getT1().getTotalServiceChargeRemaining())))
                        .thenReturn(tuple))
                .flatMap(tuple -> {
                    String previousLoanWaiverPaymentMode = tuple.getT2().getPaymentMode();
                    return loanWaiverPersistenceHistoryPort.saveLoanWaiverHistory(tuple.getT2())
                            .flatMap(loanWaiver -> loanWaiverPersistencePort.saveLoanWaiver(loanWaiverUtil.updateLoanWaiverData(tuple.getT2(), requestDto, tuple.getT1().getT1()))
                                    .map(updatedLoanWaiver -> {
                                        updatedLoanWaiver.setStagingDataId(tuple.getT1().getT2().getStagingDataId());
                                        return updatedLoanWaiver;
                                    }))
                            .flatMap(updatedLoanWaiver -> loanWaiverAdjustmentAndCollectionProcessUtil.updateLoanAdjustmentAndPaymentCollection(
                                            updatedLoanWaiver, requestDto, previousLoanWaiverPaymentMode)
                                    .doOnNext(response -> log.info("updateLoanAdjustmentAndPaymentCollection response: {}", response)));
                })
                .as(rxtx::transactional)
                .map(data -> LoanWaiverResponseDTO.builder()
                        .userMessage("Loan waiver Data is Updated Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan waiver update Response: {}", response))
                .doOnError(throwable -> log.error("Error Updating Loan waiver: {}", throwable.getMessage()));
    }

    @Override
    public Mono<LoanWaiverResponseDTO> submitLoanWaiver(LoanWaiverSubmitRequestDTO requestDto) {
        return validateLoanWaiverForSubmit(requestDto)
                .flatMap(loanWaiver -> commonRepository.getManagementProcessTrackerByProcessID(loanWaiver.getManagementProcessId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Management Process Data not found")))
                        .map(managementProcessTracker -> modelMapper.map(managementProcessTracker, ManagementProcessTracker.class))
                        .flatMap(managementProcessTracker -> this.validateIfWaiverIsPossibleForSamity(managementProcessTracker, requestDto.getOfficeId(), loanWaiver.getSamityId()))
                        .flatMap(managementProcessTracker -> this.validateIfDayEndProcessIsStartedForOffice(managementProcessTracker))
                        .flatMap(managementProcessTracker -> this.validateIfLoanWaiverTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), loanWaiver.getSamityId()))
                        .thenReturn(loanWaiver)
                )
                .map(loanWaiver -> loanWaiverUtil.updateStatusToSubmitLoanWaiverDataForAuthorization(loanWaiver, requestDto.getLoginId()))
                .flatMap(loanWaiverPersistencePort::saveLoanWaiver)
                .flatMap(loanWaiver -> loanWaiverAdjustmentAndCollectionProcessUtil
                        .submitLoanAdjustmentAndPaymentCollection(requestDto, loanWaiver)
                        .doOnNext(response -> log.info("submitLoanAdjustmentAndPaymentCollection response: {}", response)))
                .as(rxtx::transactional)
                .map(data -> LoanWaiverResponseDTO.builder()
                        .userMessage("Loan waiver Data is Submitted For Authorization Successfully for Samity")
                        .build())
                .doOnNext(response -> log.info("Loan waiver submit Response: {}", response))
                .doOnError(throwable -> log.error("Error Submitting Loan waiver: {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<LoanWaiverDTO>> getLoanWaiverDataBySamityId(String samityId, String managementProcessId) {
        return loanWaiverPersistencePort.getAllLoanWaiverDataByManagementProcessId(managementProcessId)
                .flatMapMany(Flux::fromIterable)
                .filter(loanWaiver -> loanWaiver.getSamityId().equals(samityId))
                .map(loanWaiver -> modelMapper.map(loanWaiver, LoanWaiverDTO.class))
                .collectList();
    }


    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId) {
        return loanWaiverPersistencePort.lockSamityForAuthorization(samityId, managementProcessId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return loanWaiverPersistencePort.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<LoanWaiverDTO>> getAllLoanWaiverDataBySamityIdList(List<String> samityIdList) {
        return loanWaiverPersistencePort.getAllLoanWaiverDataBySamityIdList(samityIdList);
    }


    @Override
    public Mono<String> validateAndUpdateLoanWaiverDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return loanWaiverPersistencePort.validateAndUpdateLoanWaiverDataForRejectionBySamityId(managementProcessId, samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId) {
        return loanWaiverPersistencePort.getSamityIdListLockedByUserForAuthorization(loginId);
    }


    public Mono<List<LoanAccountDetails>> getLoanAccountDetails(
            List<StagingAccountData> stagingAccountDataList) {
        return Flux.fromIterable(stagingAccountDataList)
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .flatMap(stagingAccountData -> loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()).zipWith(serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(stagingAccountData.getLoanAccountId()))
                        .map(loanAccountAndServiceCharge -> LoanAccountDetails.builder()
                                .loanAccountId(stagingAccountData.getLoanAccountId())
                                .loanProductId(stagingAccountData.getProductCode())
                                .loanProductNameEn(stagingAccountData.getProductNameEn())
                                .loanProductNameBn(stagingAccountData.getProductNameBn())
                                .loanAmount(stagingAccountData.getLoanAmount())
                                .totalLoanAmount(stagingAccountData.getLoanAmount()
                                        .add(stagingAccountData.getServiceCharge()))
                                .serviceCharge(stagingAccountData.getServiceCharge())
                                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                                .totalPaid(stagingAccountData.getTotalPrincipalPaid()
                                        .add(stagingAccountData.getTotalServiceChargePaid()))
                                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(
                                        stagingAccountData.getTotalServiceChargeRemaining()))
                                .disbursementDate(stagingAccountData.getDisbursementDate())
                                .loanTerm(loanAccountAndServiceCharge.getT1().getLoanTerm())
                                .installmentAmount(loanAccountAndServiceCharge.getT1().getInstallmentAmount())
                                .noOfInstallment(loanAccountAndServiceCharge.getT1().getNoInstallment())
                                .advancePaid(stagingAccountData.getTotalAdvance())
                                .serviceChargeRate(loanAccountAndServiceCharge.getT2().getServiceChargeRate())
                                .build()))
                        .collectList();
    }


    private Mono<LoanWaiverMemberDetailViewResponseDTO> updateSavingsBalanceForLoanAdjustment(LoanWaiverMemberDetailViewResponseDTO responseDTO) {
        return Flux.fromIterable(responseDTO.getSavingsAccountList())
                .doOnNext(savingsAccountDetails -> {
                    savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance() == null ? BigDecimal.ZERO : savingsAccountDetails.getAvailableBalance());
                    savingsAccountDetails.setBalance(savingsAccountDetails.getBalance() == null ? BigDecimal.ZERO : savingsAccountDetails.getBalance());
                })
                .flatMap(savingsAccountDetails -> commonRepository.getCollectionStagingDataBySavingsAccountId(savingsAccountDetails.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountDetails.setBalance(savingsAccountDetails.getBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountDetails;
                        }))
                .flatMap(savingsAccountDetails -> commonRepository.getStagingWithdrawDataBySavingsAccountId(savingsAccountDetails.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountDetails.setBalance(savingsAccountDetails.getBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountDetails;
                        }))
                .collectList()
                .map(savingsAccountDetailsList -> {
                    responseDTO.setSavingsAccountList(savingsAccountDetailsList);
                    return responseDTO;
                });
    }

    private Mono<LoanWaiverDTO> getMemberAndLoanAmountData(LoanWaiver loanWaiver) {
        return commonRepository.getMemberInfoByLoanAccountId(loanWaiver.getLoanAccountId())
                .zipWith(commonRepository.getLoanAmountByLoanAccountId(loanWaiver.getLoanAccountId()))
                .flatMap(tuple -> commonRepository.getSamityBySamityId(loanWaiver.getSamityId())
                        .map(samity -> loanWaiverUtil.buildLoanWaiverDto(loanWaiver, tuple.getT1(), tuple.getT2(), samity)));
    }

    private Mono<String> validateIfLoanWaiverTransactionIsStillAvailableForSamity(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
                .collectList()
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity is Already Canceled and Cannot Create Transaction")))
                .flatMap(samityEventList -> loanWaiverPersistencePort.getLoanWaiverDataBySamity(samityId)
                        .filter(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(loanWaiver.getStatus()))
                        .collectList())
                .filter(loanWaiver -> loanWaiver.isEmpty() || loanWaiver.stream().noneMatch(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .doOnNext(loanAdjustmentDataList -> log.info("Loan Adjustment Data List: {}", loanAdjustmentDataList))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Loan Waiver Data is Already Authorized or Unauthorized")))
                .filter(loanWaiver -> loanWaiver.isEmpty() || loanWaiver.stream().allMatch(data -> data.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Loan Waiver Transaction is Already Locked for Authorization for Samity")))
                .map(loanAdjustmentDataList -> managementProcessId);
    }

    private Mono<ManagementProcessTracker> validateIfWaiverIsPossibleForSamity(ManagementProcessTracker managementProcessTracker,
                                                                               String officeId, String samityId) {
        return officeEventTrackerUseCase
                .getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
                .collectList()
                .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                        .anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventTrackerList -> !officeEventTrackerList.isEmpty() && officeEventTrackerList.stream()
                        .noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
                .flatMap(officeEventTrackerList -> stagingDataUseCase.getStagingProcessEntityForSamity(managementProcessTracker.getManagementProcessId(), samityId)
                        .filter(trackerEntity -> !HelperUtil.checkIfNullOrEmpty(trackerEntity.getStatus()) && (trackerEntity.getStatus().equals(Status.STATUS_FINISHED.getValue()) || trackerEntity.getStatus().equals(Status.STATUS_REGENERATED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Valid For Samity"))))
                .flatMap(trackerEntity -> Mono.just(managementProcessTracker));
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsStartedForOffice(ManagementProcessTracker managementProcessTracker) {
        return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office")))
                .map(dayEndProcessTrackers -> managementProcessTracker);
    }

    private Mono<Boolean> validateLoanWaiverRequestExistsByLoanAccountId(LoanWaiverCreateUpdateRequestDTO requestDto) {
        return Mono.just(requestDto)
                .flatMap(stagingData -> loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(requestDto.getLoanAccountId()))
                .flatMap(existingWaiver -> {
                    if (existingWaiver.getLoanAccountId() != null) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Waiver Already Found"));
                    }
                    return Mono.just(Boolean.TRUE);
                });
    }

    private Mono<LoanWaiver> validateLoanWaiverForUpdate(LoanWaiverCreateUpdateRequestDTO requestDto) {
        return loanWaiverPersistencePort.getLoanWaiverById(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan waiver Data not found")))
                .filter(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(loanWaiver.getCreatedBy()) &&
                        loanWaiver.getCreatedBy().equals(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan waiver Data is not created by this user")))
                .filter(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(loanWaiver.getStatus()) &&
                        (loanWaiver.getStatus().equals(Status.STATUS_STAGED.getValue()) ||
                                loanWaiver.getStatus().equals(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan waiver Data is already 'Submitted' or 'Approved'")));
    }

    private Mono<LoanWaiver> validateLoanWaiverForSubmit(LoanWaiverSubmitRequestDTO requestDto) {
        return loanWaiverPersistencePort.getLoanWaiverById(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan waiver Data not found")))
                .filter(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(loanWaiver.getIsLocked()) && loanWaiver.getIsLocked().equals(StatusYesNo.No.toString()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan waiver Data is locked")))
                .filter(loanWaiver -> !HelperUtil.checkIfNullOrEmpty(loanWaiver.getStatus()) &&
                        (loanWaiver.getStatus().equals(Status.STATUS_STAGED.getValue()) || loanWaiver.getStatus().equals(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan waiver Data is already 'Submitted' or 'Approved'")));
    }

    private Mono<Boolean> validateIfLoanAccountIdIsValidForLoanWaiver(String loanAccountId) {
        return commonValidation.checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId(loanAccountId)
                .doOnNext(response -> log.info("Loan Account Validation Response in loan waiver service: {}", response))
                .flatMap(doesNotExists -> {
                    if (doesNotExists) {
                        return Mono.just(Boolean.TRUE);
                    } else {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection/Adjustment data already found for this loan account"));
                    }
                });
    }
}
