package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionStagingDataResponseDTO;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.application.port.in.EqualInstallmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.EqualInstallmentMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.RepaymentScheduleRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RebateInfoResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepayScheduleMetaProperty;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleEditHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.commands.IRepaymentDatesCommands;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.ServiceChargeChartUseCase;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoanRepaymentScheduleService implements LoanRepaymentScheduleUseCase {

    private final RepaymentSchedulePersistencePort port;
    private final ModelMapper mapper;

    private final RepaymentScheduleRepository repository;
    private final CommonRepository commonRepository;
    private final HolidayUseCase holidayUseCase;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final ServiceChargeChartUseCase serviceChargeChartUseCase;
    private final LoanAccountUseCase loanAccountUseCase;
    private final EqualInstallmentUseCase equalInstallmentUseCase;
    private final RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort;
    private final RepaymentScheduleEditHistoryPersistencePort repaymentScheduleEditHistoryPersistencePort;
    private final TransactionalOperator rxtx;

    private final Gson gson;
    private final ModelMapper modelMapper;
    private final IRepaymentDatesCommands repaymentDatesCommands;

    public LoanRepaymentScheduleService(RepaymentSchedulePersistencePort port, ModelMapper mapper, RepaymentScheduleRepository repository, CommonRepository commonRepository, HolidayUseCase holidayUseCase, MetaPropertyUseCase metaPropertyUseCase, ServiceChargeChartUseCase serviceChargeChartUseCase, LoanAccountUseCase loanAccountUseCase, EqualInstallmentUseCase equalInstallmentUseCase, RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort, RepaymentScheduleEditHistoryPersistencePort repaymentScheduleEditHistoryPersistencePort, TransactionalOperator rxtx, ModelMapper modelMapper, IRepaymentDatesCommands repaymentDatesCommands) {
        this.port = port;
        this.holidayUseCase = holidayUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.serviceChargeChartUseCase = serviceChargeChartUseCase;
        this.loanAccountUseCase = loanAccountUseCase;
        this.equalInstallmentUseCase = equalInstallmentUseCase;
        this.repaymentScheduleHistoryPersistencePort = repaymentScheduleHistoryPersistencePort;
        this.repaymentScheduleEditHistoryPersistencePort = repaymentScheduleEditHistoryPersistencePort;
        this.rxtx = rxtx;
        this.repaymentDatesCommands = repaymentDatesCommands;
        this.mapper = new ModelMapper();
        this.repository = repository;
        this.commonRepository = commonRepository;
        this.gson = CommonFunctions.buildGson(this);
        this.modelMapper = modelMapper;
    }

    private static BigDecimal getServiceChargeRatePerPeriod(BigDecimal serviceChargeRate, String serviceChargeRateFrequency, String paymentPeriod, String daysInYear) {
        BigDecimal serviceChargeRatePerPeriod = BigDecimal.ZERO;

        if (daysInYear == null) {
            if (serviceChargeRateFrequency.equalsIgnoreCase("YEARLY")) {
                switch (paymentPeriod.toUpperCase()) {
                    case "WEEKLY" ->
                            serviceChargeRatePerPeriod = (serviceChargeRate.multiply(BigDecimal.valueOf(1.0 / 52.0)));
                    case "MONTHLY" ->
                            serviceChargeRatePerPeriod = (serviceChargeRate.multiply(BigDecimal.valueOf(1.0 / 12.0)));
                    case "HALF-YEARLY" ->
                            serviceChargeRatePerPeriod = (serviceChargeRate.multiply(BigDecimal.valueOf(0.5)));
                }
            }
            if (serviceChargeRateFrequency.equalsIgnoreCase("MONTHLY")) {
                switch (paymentPeriod.toUpperCase()) {
                    case "WEEKLY" ->
                            serviceChargeRatePerPeriod = (serviceChargeRate.multiply(BigDecimal.valueOf(12.0))).divide(BigDecimal.valueOf(52.0));
                    case "MONTHLY" -> serviceChargeRatePerPeriod = serviceChargeRate;
                    case "HALF-YEARLY" ->
                            serviceChargeRatePerPeriod = serviceChargeRate.multiply(BigDecimal.valueOf(6.0));
                }
            }
        } else {
            if (serviceChargeRateFrequency.equalsIgnoreCase("YEARLY")) {
                final BigDecimal serviceChargePerDay = serviceChargeRate.divide(BigDecimal.valueOf(Long.parseLong(daysInYear)), 8, RoundingMode.HALF_UP);
                switch (paymentPeriod.toUpperCase()) {
                    case "WEEKLY" -> serviceChargeRatePerPeriod = serviceChargePerDay.multiply(BigDecimal.valueOf(7));
                    case "MONTHLY" -> serviceChargeRatePerPeriod = serviceChargePerDay.multiply(BigDecimal.valueOf(30));
                    case "HALF-YEARLY" ->
                            serviceChargeRatePerPeriod = serviceChargePerDay.multiply(BigDecimal.valueOf(180));
                }
            }
            if (serviceChargeRateFrequency.equalsIgnoreCase("MONTHLY")) {
                switch (paymentPeriod.toUpperCase()) {
                    case "WEEKLY" ->
                            serviceChargeRatePerPeriod = ((serviceChargeRate.multiply(BigDecimal.valueOf(12.0))).divide(BigDecimal.valueOf(Double.parseDouble(daysInYear)), 8, RoundingMode.HALF_UP)).multiply(BigDecimal.valueOf(7));
                    case "MONTHLY" -> serviceChargeRatePerPeriod = serviceChargeRate;
                    case "HALF-YEARLY" ->
                            serviceChargeRatePerPeriod = serviceChargeRate.multiply(BigDecimal.valueOf(6.0));
                }
            }
        }
        return serviceChargeRatePerPeriod.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    //    @TODO: Implement method to get loanRepaymentScheduleList For One Active Loan
    @Override
    public Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleListByLoanAccountId(String loanAccountId) {
        return repository
                .getRepaymentScheduleEntitiesByLoanAccountIdOrderByInstallNo(loanAccountId)
                .map(r -> mapper.map(r, RepaymentScheduleResponseDTO.class))
//                .doOnNext(lrs -> log.info("Loan Repayment Schedule for Staging Data : {}", lrs))
                ;
    }

    @Override
    public Mono<RepaymentScheduleResponseDTO> getFirstRepaymentScheduleByLoanAccountId(String loanAccountId) {
        return repository.getRepaymentScheduleEntityByLoanAccountIdOrderByInstallNo(loanAccountId)
                .map(lrs -> mapper.map(lrs, RepaymentScheduleResponseDTO.class));
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> updateInstallmentStatus(List<Integer> installmentList, String status, String loanAccountId, String managementProcessId) {
        return updateStatus(installmentList, status, loanAccountId, managementProcessId)
                .flatMap(repaymentSchedule -> getUpdatedRepaymentSchedules(loanAccountId))
                .filter(List::isEmpty)
                .doOnNext(repaymentSchedules -> log.info("Loan Account : {} has no pending installments & to be Closed-paid-off", loanAccountId))
                .flatMap(repaymentSchedules -> this.updateLoanAccountStatusToPaidOff(loanAccountId, managementProcessId)
                        .map(loanAccountResponseDTO -> repaymentSchedules))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class));
    }


    private Mono<List<RepaymentSchedule>> updateStatus(List<Integer> installmentList, String status, String loanAccountId, String managementProcessId) {
        return port
                .updateInstallmentStatus(installmentList, status, loanAccountId, managementProcessId)
                .collectList()
                .doOnError(throwable -> log.error("Error Happened while updating status for installments : {}, updated status : {}, error : {}", installmentList, status, throwable.getMessage()));
    }

    private Mono<List<RepaymentSchedule>> getUpdatedRepaymentSchedules(String loanAccountId) {
        return port.getRepaymentScheduleListByLoanAccountId(loanAccountId)
                .doOnRequest(value -> log.info("request received to get repayment schedule list by loan account id : {}", loanAccountId))
                .flatMapMany(Flux::fromIterable)
                .filter(repaymentScheduleEntity -> repaymentScheduleEntity.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .collectList()
                .doOnNext(repaymentSchedules -> log.info("Pending Repayment Schedule List Size : {} for Loan Account : {}", repaymentSchedules.size(), loanAccountId))
                .doOnError(throwable -> log.error("Error Happened while fetching pending Repayment Schedule List for Loan Account : {}, error : {}", loanAccountId, throwable.getMessage()));
    }

    private Mono<LoanAccountResponseDTO> updateLoanAccountStatusToPaidOff(String loanAccountId, String managementProcessId) {
        return loanAccountUseCase
                .updateLoanAccountStatusForPaidOff(loanAccountId, managementProcessId, Status.STATUS_PAID_OFF.getValue())
                .doOnError(throwable -> log.error("Error Happened while updating Loan Account Status to Closed-Paid-Off for Loan Account : {}, error : {}", loanAccountId, throwable.getMessage()));
    }

    @Override
    public Mono<RebateInfoResponseDTO> getRebateInfoByLoanAccountId(String loanAccountId) {
        return port
                .getRebateInfoByLoanAccountId(loanAccountId)
                .doOnRequest(l -> log.info("Request Received to get Rebate info by Loan account id : {}", loanAccountId))
                .doOnNext(rebateInfoEntity -> log.info("Rebate info by Loan account id Received: {}", rebateInfoEntity))
                .doOnError(throwable -> log.error("Error Happened while fetching Rebate info by Loan account id : {}, error : {}", loanAccountId, throwable.getMessage()))
                .map(rebateInfoEntity -> mapper.map(rebateInfoEntity, RebateInfoResponseDTO.class));
    }

    @Override
    public Mono<List<String>> updateInstallmentStatusToPending(List<String> loanRepayScheduleIdList) {
        if (loanRepayScheduleIdList.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        return port.updateInstallmentStatusToPending(loanRepayScheduleIdList);
    }

    @Override
    public Mono<String> rescheduleLoanRepayScheduleOnSamityCancel(List<String> loanAccountIdList, String loginId, LocalDate businessDate) {
        log.info("request landed to reschedule loan repay schedule for loanAccountList: {}, login id : {}, business date : {}", loanAccountIdList, loginId, businessDate);
        return metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.LOAN_REPAY_SCHEDULE_META_PROPERTY_ID.getValue())
                .doOnNext(metaPropertyResponseDTO -> log.info("metaPropertyResponseDTO: {}", metaPropertyResponseDTO))
                .map(this::checkIfLoanRepayScheduleIsRescheduledOnSamityCancel)
                .flatMap(aBoolean -> aBoolean
                        ? this.rescheduleLoanRepayByLoanAccountList(loanAccountIdList, loginId, businessDate)
                        : Mono.just("Loan RepaySchedule is not rescheduled, As defined by meta property"))
                .doOnNext(s -> log.info("{}", s));
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleByInstallmentDate(LocalDate installmentDate) {
        return port.getRepaymentScheduleByInstallmentDate(installmentDate);
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> getUnprovisionedRepaymentSchedulesByInstallmentDate(LocalDate installmentDate, String officeId) {
        return port.getUnprovisionedRepaymentSchedulesByInstallmentDate(installmentDate, officeId)
                .collectList()
                .doOnNext(repaymentScheduleEntityList -> log.info("Unprovisioned Repayment Schedule List Size : {} for Installment Date : {} & Office Id : {}", repaymentScheduleEntityList.size(), installmentDate, officeId))
                .doOnNext(repaymentScheduleEntityList -> port
                        .updateIsProvisionedStatus(repaymentScheduleEntityList.stream().map(RepaymentScheduleResponseDTO::getLoanRepayScheduleId).toList(), Status.STATUS_PROCESSING.getValue())
                        .doOnRequest(l -> log.info("Request received to set Repayment schedule is_provisioned status 'Processing'.")).subscribeOn(Schedulers.boundedElastic()).subscribe())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Boolean> updateIsProvisionedStatus(String currentStatus, String updatedStatus) {
        return port.updateIsProvisionedStatus(currentStatus, updatedStatus);
    }

    @Override
    public Mono<String> revertRescheduledRepaymentSchedule(String managementProcessId) {
        return repaymentScheduleHistoryPersistencePort.getAllRepaymentScheduleHistoryByManagementProcessId(managementProcessId)
                .map(repaymentSchedule -> {
                    repaymentSchedule.setOid(repaymentSchedule.getLoanRepayScheduleOid());
                    return repaymentSchedule;
                })
                .collectList()
                .flatMap(port::saveAllRepaymentSchedule)
                .flatMapIterable(repaymentScheduleList -> repaymentScheduleList)
                .map(RepaymentSchedule::getOid)
                .collectList()
                .flatMap(repaymentScheduleHistoryPersistencePort::deleteAllRepaymentHistoryByLoanRepayOid)
                .as(rxtx::transactional)
                .thenReturn("Repayment Schedule is reverted successfully");
    }

    @Override
    public Mono<Boolean> archiveAndUpdateRepaymentScheduleForLoanRebate(List<LoanRebateDTO> loanRebateDTOList) {
        return Flux.fromIterable(loanRebateDTOList)
                .flatMap(loanRebateDTO -> port.getRepaymentScheduleListByLoanAccountId(loanRebateDTO.getLoanAccountId())
                        .flatMap(repaymentScheduleList -> {
                            List<RepaymentSchedule> pendingListToSaveInHistory = repaymentScheduleList.stream()
                                    .filter(repaymentSchedule -> repaymentSchedule.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
                                    .peek(repaymentSchedule -> {
                                        repaymentSchedule.setLoanRepayScheduleOid(repaymentSchedule.getOid());
                                        repaymentSchedule.setOid(null);
                                        repaymentSchedule.setManagementProcessId(loanRebateDTO.getManagementProcessId());
                                    }).toList();
                            return repaymentScheduleEditHistoryPersistencePort.saveRepaymentScheduleEditHistory(pendingListToSaveInHistory)
                                    .flatMap(pendingRepaymentHistory -> port.updateInstallmentStatus(pendingRepaymentHistory.stream().map(RepaymentSchedule::getInstallNo).toList(), Status.STATUS_PAID.getValue(), loanRebateDTO.getLoanAccountId(), loanRebateDTO.getManagementProcessId()).collectList());
//                                    .thenReturn(repaymentScheduleList);
                        })
                        .doOnSuccess(repaymentSchedules -> log.info("Archived Successfully Repayment Schedule List Size : {} for Loan Account : {}", repaymentSchedules.size(), loanRebateDTO.getLoanAccountId()))
                        .map(repaymentSchedules -> this.getRebatedRepaymentSchedules(repaymentSchedules, loanRebateDTO))
                        .flatMap(rebatedRepaymentSchedules ->
                            port.updateInstallmentStatus(rebatedRepaymentSchedules.stream().map(RepaymentSchedule::getInstallNo).toList(), Status.STATUS_REBATED.getValue(), loanRebateDTO.getLoanAccountId(), loanRebateDTO.getManagementProcessId())
                                .doOnComplete(() -> log.info("Repayment Schedule Status is updated to Rebated Successfully for Loan Account : {}", loanRebateDTOList.get(0).getLoanAccountId()))
                                .then(port.saveAllRepaymentSchedule(rebatedRepaymentSchedules))
                                .doOnSuccess(repaymentSchedules -> log.info("Updated Repayment Schedule is saved successfully for Loan Account : {}", loanRebateDTOList.get(0).getLoanAccountId()))
                                    .flatMap(repaymentSchedules -> port.getRepaymentScheduleListByLoanAccountId(loanRebateDTO.getLoanAccountId()))
                                    .map(repaymentSchedules -> true)
                        ))
                .collectList()
                .map(list -> list.stream().allMatch(Boolean::booleanValue));
    }

    @Override
    public Mono<Boolean> revertRepaymentScheduleByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId) {
        return rxtx.transactional(
                port.deleteRepaymentScheduleListByManagementProcessIdAndLoanAccountId(managementProcessId, loanAccountId)
                        .doOnRequest(l -> log.info("Request received to delete Repayment Schedule for Management Process Id : {} & loanAccountId : {}", managementProcessId, loanAccountId))
                        .doOnSuccess(aBoolean -> log.info("Repayment Schedule is deleted successfully for Management Process Id : {} & loanAccountId : {}", managementProcessId, loanAccountId))
                        .flatMap(aBoolean -> this.getRepaymentScheduleFromEditHistory(managementProcessId, loanAccountId)
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, ExceptionMessages.NO_REPAYMENT_SCHEDULE_EDIT_HISTORY_FOUND.getValue() + " for loan account : " + loanAccountId)))
                                .doOnSuccess(repaymentScheduleList -> log.info("Retrieved Repayment Schedule List Size from History : {} for loan account Id : {}", repaymentScheduleList.size(), loanAccountId))
                                .flatMap(repaymentScheduleListFromHistory -> port.saveAllRepaymentSchedule(repaymentScheduleListFromHistory)
                                        .doOnSuccess(repaymentSchedules -> log.info("Repayment Schedule is restored successfully for loanAccountId : {}", loanAccountId))
                                        .thenReturn(repaymentScheduleListFromHistory)))
                        .map(this::getListOfLoanRepayScheduleOid)
                        .flatMap(loanRepayScheduleOidList -> Mono.deferContextual(contextView -> Mono.fromRunnable(() -> {
                                    Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                    repaymentScheduleEditHistoryPersistencePort.deleteAllRepaymentEditHistoryByLoanRepayOid(loanRepayScheduleOidList)
                                            .contextWrite(context)
                                            .subscribeOn(Schedulers.immediate()).subscribe();
                                })
                                .thenReturn(true)))
                        .doOnRequest(l -> log.info("Request received to delete Repayment Schedule History for loanAccountId : {}", loanAccountId))
                        .doOnSuccess(aBoolean -> log.info("Repayment Schedule History is deleted successfully for for loanAccountId : {}", loanAccountId)));
    }

    @Override
    public Mono<Boolean> archiveAndUpdateRepaymentScheduleForSeasonalSingleLoan(CollectionStagingDataResponseDTO collectionStagingDataResponseDTO) {
        return port.getRepaymentScheduleListByLoanAccountId(collectionStagingDataResponseDTO.getLoanAccountId())
                .flatMap(repaymentSchedules -> repaymentSchedules
                        .stream()
                        .map(RepaymentSchedule::getTotalPayment)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .compareTo(collectionStagingDataResponseDTO.getAmount()) != 0
                        ? this.archiveUpdateAndSaveUpdateRepaymentSchdule(collectionStagingDataResponseDTO)
                        : Mono.just(repaymentSchedules))
                .flatMap(repaymentSchedules -> port.getRepaymentScheduleListByLoanAccountId(collectionStagingDataResponseDTO.getLoanAccountId()))
                .map(repaymentSchedules -> true);
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> updateInstallmentStatusFromInstallmentNoToLast(Integer installmentNo, String status, String loanAccountId, String managementProcessId) {
        return port.updateInstallmentStatusFromInstallmentNoToLast(installmentNo, status, loanAccountId, managementProcessId);
    }


    private Mono<List<RepaymentSchedule>> archiveUpdateAndSaveUpdateRepaymentSchdule(CollectionStagingDataResponseDTO collectionStagingDataResponseDTO) {
        return port.getRepaymentScheduleListByLoanAccountId(collectionStagingDataResponseDTO.getLoanAccountId())
                .flatMap(repaymentScheduleList -> {
                    List<RepaymentSchedule> pendingListToSaveInHistory = repaymentScheduleList.stream()
                            .filter(repaymentSchedule -> repaymentSchedule.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
                            .peek(repaymentSchedule -> {
                                repaymentSchedule.setLoanRepayScheduleOid(repaymentSchedule.getOid());
                                repaymentSchedule.setOid(null);
                                repaymentSchedule.setManagementProcessId(collectionStagingDataResponseDTO.getManagementProcessId());
                            }).toList();
                    return repaymentScheduleHistoryPersistencePort.saveRepaymentScheduleHistory(pendingListToSaveInHistory)
                            .thenReturn(repaymentScheduleList);
                })
                .doOnSuccess(repaymentSchedules -> log.info("Archived Successfully Repayment Schedule List Size : {} for Loan Account : {}", repaymentSchedules.size(), collectionStagingDataResponseDTO.getLoanAccountId()))
                .map(repaymentSchedules -> this.getRevisedRepaymentSchedulesForSeasonalSingleLoan(repaymentSchedules, collectionStagingDataResponseDTO))
                .flatMap(revisedRepaymentSchedule -> port.updateInstallmentStatus(revisedRepaymentSchedule.stream().map(RepaymentSchedule::getInstallNo).toList(), Status.STATUS_REVISED.getValue(), collectionStagingDataResponseDTO.getLoanAccountId(), collectionStagingDataResponseDTO.getManagementProcessId())
                        .doOnComplete(() -> log.info("Repayment Schedule Status is updated to Revised Successfully for Loan Account : {}", collectionStagingDataResponseDTO.getLoanAccountId()))
                        .then(port.saveAllRepaymentSchedule(revisedRepaymentSchedule)))
                .doOnSuccess(repaymentSchedules -> log.info("Updated Repayment Schedule is saved successfully for Loan Account : {}", collectionStagingDataResponseDTO.getLoanAccountId()));
    }



    public List<RepaymentSchedule> getRevisedRepaymentSchedulesForSeasonalSingleLoan(List<RepaymentSchedule> repaymentScheduleList, CollectionStagingDataResponseDTO collectionStagingDataResponseDTO) {

        List<RepaymentSchedule> revisedRepaymentScedule = new ArrayList<>();
        int i =0;

        for (RepaymentSchedule repaymentSchedule : repaymentScheduleList) {

            repaymentSchedule.setOid(null);
            repaymentSchedule.setLoanRepayScheduleId(UUID.randomUUID().toString());
            repaymentSchedule.setManagementProcessId(collectionStagingDataResponseDTO.getManagementProcessId());
            BigDecimal revisedServiceCharge = collectionStagingDataResponseDTO.getAmount().subtract(repaymentSchedule.getPrincipal()).abs();

            repaymentSchedule.setServiceCharge(revisedServiceCharge);
            revisedRepaymentScedule.add(repaymentSchedule);
            i++;
        }

        log.info("Revised Repayment Schedule List : {} for Loan Account : {}", revisedRepaymentScedule, collectionStagingDataResponseDTO.getLoanAccountId());
        return revisedRepaymentScedule;
    }


    private List<String> getListOfLoanRepayScheduleOid(List<RepaymentSchedule> repaymentScheduleList) {
        return repaymentScheduleList.stream().map(RepaymentSchedule::getLoanRepayScheduleOid).toList();
    }


    private Mono<List<RepaymentSchedule>> getRepaymentScheduleFromEditHistory(String managementProcessId, String loanAccountId) {
        return repaymentScheduleEditHistoryPersistencePort
                .getAllRepaymentScheduleEditHistoryByManagementProcessIdAndLoanAccountId(managementProcessId, loanAccountId)
                .collectList()
                .filter(repaymentSchedule -> !repaymentSchedule.isEmpty())
                .flatMapIterable(repaymentScheduleList -> repaymentScheduleList)
                .map(repaymentSchedule -> {
                    repaymentSchedule.setOid(null);
                    return repaymentSchedule;
                })
                .collectList();
    }

    public List<RepaymentSchedule> getRebatedRepaymentSchedules(List<RepaymentSchedule> repaymentScheduleList, LoanRebateDTO loanRebateDTO) {
        BigDecimal remainingRebateAmount = loanRebateDTO.getRebateAmount();
        repaymentScheduleList.sort(Comparator.comparing(RepaymentSchedule::getInstallNo).reversed());
        List<RepaymentSchedule> rebatedRepaymentScheduleList = new ArrayList<>();
        int i =0;

        while (remainingRebateAmount.compareTo(BigDecimal.ZERO) > 0) {
            RepaymentSchedule currentRepaySchedule = repaymentScheduleList.get(i);

            currentRepaySchedule.setOid(null);
            currentRepaySchedule.setLoanRepayScheduleId(UUID.randomUUID().toString());
            currentRepaySchedule.setManagementProcessId(loanRebateDTO.getManagementProcessId());
            BigDecimal revisedServiceCharge = (remainingRebateAmount.subtract(currentRepaySchedule.getServiceCharge()).compareTo(BigDecimal.ZERO) >= 0
                    ? BigDecimal.ZERO
                    : currentRepaySchedule.getServiceCharge().subtract(remainingRebateAmount).abs());

            remainingRebateAmount = remainingRebateAmount.subtract(repaymentScheduleList.get(i).getServiceCharge());
            currentRepaySchedule.setServiceCharge(revisedServiceCharge);
            currentRepaySchedule.setTotalPayment(currentRepaySchedule.getPrincipal().add(currentRepaySchedule.getServiceCharge()));
            rebatedRepaymentScheduleList.add(currentRepaySchedule);
            i++;
        }

        log.info("rebatable Repayment Schedule List Size : {} for Loan Account : {}", rebatedRepaymentScheduleList.size(), loanRebateDTO.getLoanAccountId());
        return rebatedRepaymentScheduleList;
    }

    private Mono<String> rescheduleLoanRepayByLoanAccountList(List<String> loanAccountIdList, String loginId, LocalDate businessDate) {
        return commonRepository.getManagementProcessIdByLoanAccountId(loanAccountIdList.get(0))
                .flatMap(managementProcessId -> Flux.fromIterable(loanAccountIdList)
                        .flatMap(loanAccountId -> this.rescheduleByLoanAccountId(loanAccountId, loginId, businessDate, managementProcessId))
                        .collectList()
                        .map(lists -> "Loan RepaySchedule is rescheduled successfully"));
    }

    private Mono<List<RepaymentScheduleEntity>> rescheduleByLoanAccountId(String loanAccountId, String loginId, LocalDate businessDate, String managementProcessId) {
        return port.getRepaymentScheduleListByLoanAccountId(loanAccountId)
                .doOnNext(list -> log.info("Installment Count before filter: {}", list.size()))
                .map(list -> list.stream().filter(item -> !item.getInstallDate().isBefore(businessDate) && item.getStatus().equals(Status.STATUS_PENDING.getValue())).collect(Collectors.toList()))
                .doOnNext(list -> log.info("Installment Count after filter: {}", list.size()))
                .flatMap(repaymentScheduleList -> holidayUseCase.getAllHolidaysOfASamityByLoanAccountId(loanAccountId)
                        .map(HolidayResponseDTO::getHolidayDate)
                        .collectList()
                        .doOnNext(holidayList -> log.info("Holiday List: {}", holidayList))
                        .map(holidayResponseDTOList -> Tuples.of(repaymentScheduleList, holidayResponseDTOList)))
                .flatMap(tuple -> serviceChargeChartUseCase.getServiceChargeDetailsByLoanAccountId(loanAccountId)
                        .map(serviceChargeChartResponseDTO -> Tuples.of(tuple.getT1(), tuple.getT2(), serviceChargeChartResponseDTO)))
                .flatMap(tuple -> {
                    return loanAccountUseCase.getLoanAccountDetailsByLoanAccountId(loanAccountId)
                            .map(loanAccountResponseDTO -> this.getRepaymentDates(tuple.getT2(), businessDate, businessDate.getDayOfWeek(), 1, tuple.getT1().size(), tuple.getT3().getRepaymentFrequency(), loanAccountResponseDTO.getMonthlyRepayDay()))
                            .doOnNext(localDates -> {
                                log.info("Repayment schedule Dates: {}", tuple.getT1().size());
                                log.info("Repayment rescheduled Dates: {}", localDates);
                            })
                            .map(localDates -> Tuples.of(tuple.getT1(), localDates));
                })
                .map(tuple -> {
                    List<LocalDate> newInstallmentDate = tuple.getT2().stream().sorted().toList();
                    List<RepaymentSchedule> repaymentScheduleList = tuple.getT1().stream().sorted(Comparator.comparing(RepaymentSchedule::getInstallDate)).toList();
                    log.info("Repayment Schedule List: {}", repaymentScheduleList.size());
                    log.info("Repayment Date List: {}", newInstallmentDate);
                    for (int i = 0; i < repaymentScheduleList.size(); i++) {
                        LocalDate currentInstallDate = repaymentScheduleList.get(i).getInstallDate();
                        LocalDate newInstallDate = newInstallmentDate.get(i);
                        log.info("Install NO: {}, Current Install Date: {}, New Install Date: {}", i, currentInstallDate, newInstallDate);
                        log.info("repay schedule to update: {}", repaymentScheduleList.get(i));
                        repaymentScheduleList.get(i).setInstallDate(newInstallmentDate.get(i));

                        log.info("repay schedule after update: {}", repaymentScheduleList.get(i).getInstallDate());
                    }
                    return repaymentScheduleList;
                })
                .flatMap(repaymentSchedules -> Flux.fromIterable(repaymentSchedules)
                        .map(RepaymentSchedule::getOid)
                        .collectList()
                        .zipWith(Mono.just(repaymentSchedules)))
                .flatMap(tupleOfOidListAndRepaymentScheduleList -> port.getRepaymentScheduleListByOidList(tupleOfOidListAndRepaymentScheduleList.getT1())
                        .map(repaymentSchedule -> {
                            repaymentSchedule.setLoanRepayScheduleOid(repaymentSchedule.getOid());
                            repaymentSchedule.setOid(null);
                            repaymentSchedule.setManagementProcessId(managementProcessId);
                            return repaymentSchedule;
                        })
                        .collectList()
                        .flatMap(repaymentScheduleHistoryPersistencePort::saveRepaymentScheduleHistory)
                        .thenReturn(tupleOfOidListAndRepaymentScheduleList.getT2())
                )
                .flatMap(repaymentScheduleList -> port.updateRepaymentScheduleForSamityCancel(repaymentScheduleList, loginId));
    }

    private Boolean checkIfLoanRepayScheduleIsRescheduledOnSamityCancel(MetaPropertyResponseDTO metaPropertyResponseDTO) {
        RepayScheduleMetaProperty repayScheduleMetaProperty = gson.fromJson(metaPropertyResponseDTO.getParameters(), RepayScheduleMetaProperty.class);
        return repayScheduleMetaProperty.getRescheduleOnSamityCancel().equals("Yes");
    }

    @Override
    public Mono<List<RepaymentScheduleResponseDTO>> getRepaymentScheduleForLoan(LoanRepaymentScheduleRequestDTO requestDTO) {

        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        RoundingMode roundingMode = getRoundingMode(requestDTO.getRoundingLogic());
        BigDecimal serviceChargeRatePerPeriod = BigDecimal.ZERO;

        switch (requestDTO.getServiceChargeRateFrequency().toUpperCase()) {
            case "MONTHLY" ->
                    serviceChargeRatePerPeriod = (requestDTO.getServiceChargeRate().multiply(BigDecimal.valueOf(12))).divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), 8, roundingMode);
            case "YEARLY" ->
                    serviceChargeRatePerPeriod = requestDTO.getServiceChargeRate().divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), 8, roundingMode);
        }

        log.info("SC Rate Per Period : {}", serviceChargeRatePerPeriod);
        BigDecimal initialServiceCharge = round(8, serviceChargeRatePerPeriod.multiply(requestDTO.getLoanAmount()), roundingMode);
        log.info("initial service charge : {}", initialServiceCharge);
        BigDecimal probableEI = round(2, BigDecimal.valueOf((requestDTO.getLoanAmount().doubleValue() * serviceChargeRatePerPeriod.doubleValue()) / (1 - Math.pow(1 + serviceChargeRatePerPeriod.doubleValue(), - requestDTO.getNoOfInstallments()))), roundingMode);
        log.info("probable Ei : {}", probableEI);
        BigDecimal adjustedAmount = requestDTO.getInstallmentAmount().subtract(probableEI);


        BigDecimal finalServiceChargeRatePerPeriod = serviceChargeRatePerPeriod;

        return holidayUseCase
                .getAllHolidaysOfASamityByLoanAccountId(requestDTO.getLoanAccountId())
                .doOnRequest(l -> log.info("Request received to fetch holidays"))
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                .map(holidays -> this.getRepaymentDates(holidays, requestDTO.getDisburseDate(), DayOfWeek.valueOf(requestDTO.getSamityDay().toUpperCase()), requestDTO.getGraceDays(), requestDTO.getNoOfInstallments(), requestDTO.getRepaymentFrequency(), requestDTO.getMonthlyRepaymentFrequencyDay()))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                            .map(EqualInstallmentMetaProperty::getServiceChargePrecision)
                            .map(serviceChargePrecision -> this.getRepaymentSchedulesList(requestDTO.getLoanAmount(), requestDTO.getNoOfInstallments(), requestDTO.getInstallmentAmount(), repaymentDates, repaymentScheduleList, roundingMode, finalServiceChargeRatePerPeriod, initialServiceCharge, probableEI, adjustedAmount, serviceChargePrecision))
                            .map(repaymentSchedules -> this.createRepaymentScheduleToSaveToDB(repaymentSchedules, requestDTO.getLoanAccountId(), requestDTO.getMemberId(), requestDTO.getMfiId(), requestDTO.getStatus(), requestDTO.getLoginId()));
                })
                .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                .flatMap(port::saveRepaymentSchedule)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> buildResponseDTO(repaymentSchedule, finalServiceChargeRatePerPeriod))
                .collectList();
    }

    @Override
    public Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleForLoan(LoanRepaymentScheduleRequestDTO requestDTO) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        RoundingMode roundingMode = getRoundingMode(requestDTO.getRoundingLogic());
        BigDecimal serviceChargeRatePerPeriod = BigDecimal.ZERO;
        BigDecimal serviceChargeRateInDecimal = requestDTO.getServiceChargeRate().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        log.info("serviceChargeRateInDecimal : {}", serviceChargeRateInDecimal);

        switch (requestDTO.getServiceChargeRateFrequency().toUpperCase()) {
            case "MONTHLY" ->
                    serviceChargeRatePerPeriod = (serviceChargeRateInDecimal.multiply(BigDecimal.valueOf(12))).divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), 8, roundingMode);
            case "YEARLY" ->
                    serviceChargeRatePerPeriod = serviceChargeRateInDecimal.divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), 8, roundingMode);
        }


        log.info("SC Rate Per Period : {}", serviceChargeRatePerPeriod);
        BigDecimal initialServiceCharge = round(8, serviceChargeRatePerPeriod.multiply(requestDTO.getLoanAmount()), roundingMode);
        log.info("initial service charge : {}", initialServiceCharge);
        BigDecimal probableEI = round(2, BigDecimal.valueOf((requestDTO.getLoanAmount().doubleValue() * serviceChargeRatePerPeriod.doubleValue()) / (1 - Math.pow(1 + serviceChargeRatePerPeriod.doubleValue(), -requestDTO.getNoOfInstallments()))), roundingMode);
        log.info("probable Ei : {}", probableEI);


        BigDecimal installmentAmount = this.getEI(requestDTO.getLoanAmount().doubleValue(), serviceChargeRatePerPeriod.doubleValue(), requestDTO.getNoOfInstallments(), probableEI.doubleValue(), requestDTO.getRoundingToNearest());
        BigDecimal adjustedAmount = installmentAmount.subtract(probableEI);


        BigDecimal finalServiceChargeRatePerPeriod = serviceChargeRatePerPeriod;

        return holidayUseCase.getAllHolidaysOfAOfficeByOfficeId(requestDTO.getOfficeId())
                .collectList()
                .map(holidays -> repaymentDatesCommands.getRepaymentDates(holidays, requestDTO.getDisburseDate(), DayOfWeek.valueOf(requestDTO.getSamityDay().toUpperCase()), requestDTO.getGraceDays(), requestDTO.getNoOfInstallments(), requestDTO.getRepaymentFrequency(), requestDTO.getMonthlyRepaymentFrequencyDay(), requestDTO.getLoanTerm() != null && requestDTO.getLoanTerm() > 0 ? requestDTO.getLoanTerm() : 12))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                            .map(EqualInstallmentMetaProperty::getServiceChargePrecision)
                            .map(serviceChargePrecision -> this.getRepaymentSchedulesList(requestDTO.getLoanAmount(), requestDTO.getNoOfInstallments(), installmentAmount, repaymentDates, repaymentScheduleList, roundingMode, finalServiceChargeRatePerPeriod, initialServiceCharge, probableEI, adjustedAmount, serviceChargePrecision)
                            .stream()
                            .skip(1)
                            .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                            .toList());
                });
    }

    @Override
    public Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleForLoanCalculator(LoanRepaymentScheduleRequestDTO requestDTO) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        RoundingMode roundingMode = CommonFunctions.getRoundingMode(requestDTO.getRoundingLogic());
        log.info("TEST ");
        BigDecimal initialServiceCharge = round(8, requestDTO.getServiceChargeRatePerPeriod().multiply(requestDTO.getLoanAmount()), roundingMode);
        log.info("initial service charge : {}", initialServiceCharge);
        log.info("service charge rate per period : {}", requestDTO.getServiceChargeRatePerPeriod());
        log.info("loan amount : {}", requestDTO.getLoanAmount());
        log.info("noofInstallments : {}", requestDTO.getNoOfInstallments());
        log.info("rounding mode : {}", roundingMode);

        BigDecimal probableEI = round(2, BigDecimal.valueOf((requestDTO.getLoanAmount().doubleValue()
                * requestDTO.getServiceChargeRatePerPeriod().doubleValue())
                / (1 - Math.pow(1 + requestDTO.getServiceChargeRatePerPeriod().doubleValue(), -requestDTO.getNoOfInstallments()))), roundingMode);
        log.info("probable Ei : {}", probableEI);

        BigDecimal adjustedAmount = requestDTO.getInstallmentAmount().subtract(probableEI);

        BigDecimal finalServiceChargeRatePerPeriod = requestDTO.getServiceChargeRatePerPeriod();

        return holidayUseCase.getAllHolidaysOfAOfficeByOfficeId(requestDTO.getOfficeId())
                .collectList()
                .map(holidays -> this.getRepaymentDates(holidays, requestDTO.getDisburseDate(), DayOfWeek.valueOf(requestDTO.getSamityDay().toUpperCase()), requestDTO.getGraceDays(), requestDTO.getNoOfInstallments(), requestDTO.getRepaymentFrequency(), requestDTO.getMonthlyRepaymentFrequencyDay()))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                            .map(EqualInstallmentMetaProperty::getServiceChargePrecision)
                            .map(serviceChargePrecision ->
                            this.getRepaymentSchedulesList(requestDTO.getLoanAmount(), requestDTO.getNoOfInstallments(), requestDTO.getInstallmentAmount(), repaymentDates, repaymentScheduleList, roundingMode, finalServiceChargeRatePerPeriod, initialServiceCharge, probableEI, adjustedAmount, serviceChargePrecision)
                            .stream()
                            .skip(1)
                            .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                            .toList());
                });
    }

    @Override
    public Mono<List<RepaymentScheduleViewDTO>> viewRepaymentScheduleFlat(LoanRepaymentScheduleRequestDTO requestDTO) {

        RoundingMode roundingMode1 = getRoundingMode(requestDTO.getRoundingLogic());
        BigDecimal serviceChargeRateInDecimal = requestDTO.getServiceChargeRate().divide(BigDecimal.valueOf(100), requestDTO.getServiceChargeRatePrecision(), RoundingMode.HALF_UP);
        BigDecimal serviceChargeRatePerAnnum = serviceChargeRateInDecimal;

        if (requestDTO.getServiceChargeRateFrequency().equalsIgnoreCase("MONTHLY"))
            serviceChargeRatePerAnnum = serviceChargeRateInDecimal.multiply(BigDecimal.valueOf(12));

        log.info("serviceChargeRatePerAnnum : {}", serviceChargeRatePerAnnum);

        BigDecimal totalServiceCharge = requestDTO.getLoanAmount().multiply(serviceChargeRatePerAnnum);
        BigDecimal totalRepaymentAmount = requestDTO.getLoanAmount().add(totalServiceCharge);
        log.info("totalRepaymentAmount : {}", totalRepaymentAmount);
        BigDecimal calculatedInstallmentAmount = totalRepaymentAmount.divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()),
                requestDTO.getInstallmentPrecision(), roundingMode1);
        log.info("calculatedInstallmentAmount : {}", calculatedInstallmentAmount);
        BigDecimal roundedEI = BigDecimal.ZERO;
        if (requestDTO.getRoundingToNearestIntegerLogic().equalsIgnoreCase("No_Rounding_To_Integer")) {
            roundedEI = calculatedInstallmentAmount;
        } else
            roundedEI = getRoundedEIForFlatInstallment(calculatedInstallmentAmount, requestDTO.getRoundingToNearest(), requestDTO.getRoundingToNearestIntegerLogic());

        if (requestDTO.getInstallmentAmount() != null && requestDTO.getInstallmentAmount().compareTo(BigDecimal.ZERO) > 0) {
            roundedEI = requestDTO.getInstallmentAmount();
        }

        BigDecimal principalPerInstallment = roundedEI.divide(serviceChargeRatePerAnnum.add(BigDecimal.ONE), requestDTO.getServiceChargeAmountPrecision(), roundingMode1);
        log.info("principal per installment : {}", principalPerInstallment);
        BigDecimal serviceChargePerInstallment = roundedEI.subtract(principalPerInstallment);
        BigDecimal adjustedAmount = roundedEI.subtract(totalRepaymentAmount.divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), roundingMode1));
        log.info("adjustedAmount : {}", adjustedAmount);
        BigDecimal totalExtraPayment = BigDecimal.valueOf(Math.abs(adjustedAmount.doubleValue() * (requestDTO.getNoOfInstallments()))).setScale(0, RoundingMode.HALF_UP);
        log.info("totalExtraPayment : {}", totalExtraPayment);

        BigDecimal finalRoundedEI = roundedEI;
        return holidayUseCase.getAllHolidaysOfAOfficeByOfficeId(requestDTO.getOfficeId())
                .collectList()
                .map(holidays -> repaymentDatesCommands.getRepaymentDates(holidays, requestDTO.getDisburseDate(), DayOfWeek.valueOf(requestDTO.getSamityDay().toUpperCase()), requestDTO.getGraceDays(), requestDTO.getNoOfInstallments(), requestDTO.getRepaymentFrequency(), requestDTO.getMonthlyRepaymentFrequencyDay(),12))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return metaPropertyUseCase.getEqualInstallmentMetaProperty()
                            .map(EqualInstallmentMetaProperty::getServiceChargePrecision)
                            .map(serviceChargePrecision -> {
                                return requestDTO.getRepaymentFrequency().equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_SINGLE)
                                        ? this.getRepaymentScheduleForSeasonalSingle(requestDTO.getLoanAmount(), calculatedInstallmentAmount, finalRoundedEI, repaymentDates, principalPerInstallment, serviceChargePerInstallment, adjustedAmount, serviceChargePerInstallment, serviceChargePrecision, roundingMode1)
                                        .stream()
                                        .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                                        .toList()
                                : this.getRepaymentScheduleForFlatInstallment(requestDTO.getLoanAmount(), requestDTO.getNoOfInstallments(), calculatedInstallmentAmount, finalRoundedEI, repaymentDates, principalPerInstallment, serviceChargePerInstallment, adjustedAmount, totalExtraPayment, serviceChargePrecision, roundingMode1)
                                        .stream()
                                        .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                                        .toList();
                            })
                            .doOnError(throwable -> log.error("Error while generating repayment schedule for Flat : {}", throwable.getMessage()));
                });
    }


    private List<RepaymentSchedule> getRepaymentScheduleForFlatInstallment(BigDecimal loanAmount, Integer noOfInstallments, BigDecimal calculatedInstallmentAmount, BigDecimal installmentAmount, List<LocalDate> repaymentDates, BigDecimal principalPerInstallment, BigDecimal serviceChargePerInstallment, BigDecimal adjustedAmount, BigDecimal totalExtraPayment, Integer serviceChargePrecision, RoundingMode roundingMode1) {

        serviceChargePerInstallment = serviceChargePerInstallment.setScale(serviceChargePrecision, roundingMode1);
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        BigDecimal lastInstallmentAmount;
        if (adjustedAmount.doubleValue() > 0) {
            lastInstallmentAmount = installmentAmount.subtract(totalExtraPayment);
        } else if (adjustedAmount.doubleValue() < 0) {
            lastInstallmentAmount = installmentAmount.add(totalExtraPayment);
        } else
            lastInstallmentAmount = installmentAmount;

        // build installment 1 to n-1
        if(repaymentDates.size() > 1) {
            for (int i = 0; i < repaymentDates.size() - 1; i++) {
                RepaymentSchedule repayment = RepaymentSchedule
                        .builder()
                        .installNo(i + 1)
                        .installDate(repaymentDates.get(i))
                        .dayOfWeek(repaymentDates.get(i).getDayOfWeek().toString())
                        .beginPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                                ? loanAmount
                                : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i))))
                        .scheduledPayment(calculatedInstallmentAmount)
                        .extraPayment(adjustedAmount)
                        .totalPayment(installmentAmount)
                        .principal(principalPerInstallment)
                        .serviceCharge(serviceChargePerInstallment)
                        .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                        .endPrinBalance(loanAmount.multiply(BigDecimal.valueOf(i)).compareTo(BigDecimal.ZERO) == 0
                                ? loanAmount.subtract(principalPerInstallment)
                                : loanAmount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(i + 1))))
                        .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + (i + 1))
                        .build();
                repaymentScheduleList.add(repayment);
            }
        }

        // build last installment
        BigDecimal lastInstallmentServiceCharge = lastInstallmentAmount.subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance());
        lastInstallmentServiceCharge = lastInstallmentServiceCharge.setScale(serviceChargePrecision, roundingMode1);

        repaymentScheduleList
                .add(RepaymentSchedule
                        .builder()
                        .installNo(repaymentDates.size())
                        .installDate(repaymentDates.get(repaymentDates.size() - 1))
                        .dayOfWeek(repaymentDates.get(repaymentDates.size() - 1).getDayOfWeek().toString())
                        .beginPrinBalance(BigDecimal.valueOf(loanAmount.doubleValue() - principalPerInstallment.doubleValue() * (noOfInstallments - 1)))
                        .scheduledPayment(calculatedInstallmentAmount)
                        .extraPayment(adjustedAmount.doubleValue() > 0 ? BigDecimal.valueOf(totalExtraPayment.doubleValue() * (-1)) : totalExtraPayment)
                        .totalPayment(lastInstallmentAmount)
                        .principal(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance())
                        .serviceCharge(lastInstallmentServiceCharge)
                        .endPrinBalance(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance().subtract(repaymentScheduleList.get(repaymentScheduleList.size() - 1).getEndPrinBalance()))
                        .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + repaymentDates.size())
                        .serviceChargeRatePerPeriod(serviceChargePerInstallment)
                        .build());
        log.info("last installment : {}", repaymentScheduleList.get(repaymentScheduleList.size() - 1));
        return repaymentScheduleList;
    }


    private BigDecimal getRoundedEIForFlatInstallment(BigDecimal installmentAmount, Integer roundingToNearest, String roundingLogic) {
        BigDecimal roundedEI = installmentAmount;
        double installmentAmountDouble = installmentAmount.doubleValue();

        if (roundingToNearest.equals(1)) {
            if (roundingLogic.equalsIgnoreCase("UP"))
                roundedEI = BigDecimal.valueOf(Math.ceil(installmentAmountDouble));
            else
                roundedEI = BigDecimal.valueOf(Math.floor(installmentAmountDouble));
        } else if (roundingToNearest.equals(5)) {
            if (roundingLogic.equalsIgnoreCase("UP"))
                roundedEI = BigDecimal.valueOf(5 * Math.ceil(installmentAmountDouble / 5));
            else
                roundedEI = BigDecimal.valueOf(5 * Math.floor(installmentAmountDouble / 5));
        } else if (roundingToNearest.equals(10)) {
            if (roundingLogic.equalsIgnoreCase("UP"))
                roundedEI = BigDecimal.valueOf(10 * Math.ceil(installmentAmountDouble / 10));
            else
                roundedEI = BigDecimal.valueOf(10 * Math.floor(installmentAmountDouble / 10));
        } else
            roundedEI = BigDecimal.valueOf(Math.ceil(installmentAmountDouble));

        return roundedEI;
    }

    private BigDecimal getEI(Double loanAmount, Double serviceChargeRatePerPeriod, Integer noOfInstallments, Double probableEI, Integer roundingToNearest) {
        int count = 0;
        double remainingBalance = loanAmount;
        BigDecimal selectedEI = BigDecimal.ZERO;
        double selectedEIDouble = 0.0;

        if (roundingToNearest.equals(1)) {
            double probableEIRoundedUp = Math.ceil(probableEI);
            double probableEIRoundedDown = Math.floor(probableEI);

            while (remainingBalance > 0 && count < noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedUp - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedUp;
            }

            System.out.println("RoundedUp EI : " + probableEIRoundedUp + " | Installment count : " + count);

            while (count != noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedDown - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedDown;
            }

            System.out.println("RoundedDown EI : " + probableEIRoundedDown + " | Installment count : " + count);
            selectedEI = BigDecimal.valueOf(selectedEIDouble);
        } else if (roundingToNearest.equals(5)) {
            BigDecimal probableEIRoundedUp = BigDecimal.valueOf(5 * (Math.ceil(probableEI / 5)));
            BigDecimal probableEIRoundedDown = BigDecimal.valueOf(5 * (Math.floor(probableEI / 5)));

            while (remainingBalance > 0 && count < noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedUp.doubleValue() - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedUp.doubleValue();
            }

            System.out.println("RoundedUp EI : " + probableEIRoundedUp + " | Installment count : " + count);

            while (count != noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedDown.doubleValue() - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedDown.doubleValue();
            }

            System.out.println("RoundedDown EI : " + probableEIRoundedDown + " | Installment count : " + count);
            selectedEI = BigDecimal.valueOf(selectedEIDouble);
            System.out.println("EI Rounded to Multiple of 5 : " + selectedEI);
        } else if (roundingToNearest.equals(10)) {

            BigDecimal probableEIRoundedDown = BigDecimal.valueOf(Math.floor(probableEI / 10.0) * 10);
            BigDecimal probableEIRoundedUp = BigDecimal.valueOf(Math.ceil(probableEI / 10.0) * 10);

            while (remainingBalance > 0 && count < noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedUp.doubleValue() - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedUp.doubleValue();
            }

            System.out.println("RoundedUp EI : " + probableEIRoundedUp + " | Installment count : " + count);

            while (count != noOfInstallments) {
                double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
                double calculatedPrincipal = probableEIRoundedDown.doubleValue() - interestAmount;
                remainingBalance = remainingBalance - calculatedPrincipal;
                count++;
                selectedEIDouble = probableEIRoundedDown.doubleValue();
            }

            System.out.println("RoundedDown EI : " + probableEIRoundedDown + " | Installment count : " + count);
            selectedEI = BigDecimal.valueOf(selectedEIDouble);

            if (selectedEI.doubleValue() == 0.0) {
                selectedEI = probableEIRoundedUp;

            }
            System.out.println("EI Rounded to Multiple of 10 : " + selectedEI);
        } else if (roundingToNearest.equals(0)) {
            selectedEI = BigDecimal.valueOf(probableEI);
        }

        System.out.println("Selected EI : " + selectedEI + "\n");

        return selectedEI;

    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanDecliningBalance(LoanRepaymentScheduleRequestDTO requestDTO) {

        BigDecimal loanAmount = requestDTO.getLoanAmount();
        BigDecimal serviceChargeRate = requestDTO.getServiceChargeRate();
        String serviceChargeRateFrequency = requestDTO.getServiceChargeRateFrequency();
        Integer noOfInstallments = requestDTO.getNoOfInstallments();
        BigDecimal installmentAmount = requestDTO.getInstallmentAmount();
        Integer graceDays = requestDTO.getGraceDays();
        LocalDate disburseDate = requestDTO.getDisburseDate();
        String samityDay = requestDTO.getSamityDay();
        Integer loanTerm = requestDTO.getLoanTerm();
        String repaymentFrequency = requestDTO.getRepaymentFrequency();
        String roundingLogic = requestDTO.getRoundingLogic();
        String loanAccountId = requestDTO.getLoanAccountId();
        String memberId = requestDTO.getMemberId();
        String mfiId = requestDTO.getMfiId();
        String status = requestDTO.getStatus();
        String createdBy = requestDTO.getLoginId();

        Integer monthlyRepaymentFrequencyDay = requestDTO.getMonthlyRepaymentFrequencyDay() > 1 ? requestDTO.getMonthlyRepaymentFrequencyDay() : 1;

        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        RoundingMode roundingMode = getRoundingMode(roundingLogic);
        BigDecimal serviceChargeRatePerPeriod = BigDecimal.ZERO;
        BigDecimal annualServiceChargeRate = BigDecimal.ZERO;

        switch (serviceChargeRateFrequency.toUpperCase()) {
            case "MONTHLY" -> {
                serviceChargeRatePerPeriod = (serviceChargeRate.multiply(BigDecimal.valueOf(12))).divide(BigDecimal.valueOf(noOfInstallments), requestDTO.getServiceChargeRatePrecision(), roundingMode);
                annualServiceChargeRate = serviceChargeRate.multiply(BigDecimal.valueOf(12));
            }
            case "YEARLY" -> {
                serviceChargeRatePerPeriod = serviceChargeRate.divide(BigDecimal.valueOf(noOfInstallments), requestDTO.getServiceChargeRatePrecision()
                        , roundingMode);
                annualServiceChargeRate = serviceChargeRate;
            }
        }

        serviceChargeRatePerPeriod = serviceChargeRatePerPeriod.multiply(getLoanTermInYears(loanTerm));

        log.info("SC Rate Per Period : {}", serviceChargeRatePerPeriod);
        BigDecimal initialServiceCharge = round(requestDTO.getServiceChargeAmountPrecision(), serviceChargeRatePerPeriod.multiply(loanAmount), roundingMode);
        log.info("initial service charge : {}", initialServiceCharge);
        BigDecimal probableEI = round(2, BigDecimal.valueOf((loanAmount.doubleValue() * serviceChargeRatePerPeriod.doubleValue()) / (1 - Math.pow(1 + serviceChargeRatePerPeriod.doubleValue(), -noOfInstallments))), roundingMode);
        log.info("probable Ei : {}", probableEI);
        BigDecimal adjustedAmount = installmentAmount.subtract(probableEI);


        BigDecimal finalServiceChargeRatePerPeriod = serviceChargeRatePerPeriod;

        log.info("TEST | installmentAmount amount : {}", installmentAmount);

        BigDecimal finalAnnualServiceChargeRate = annualServiceChargeRate;
        return Mono.just(requestDTO.getInstallmentAmount())
                .flatMap(eiAAmount -> holidayUseCase
                        .getAllHolidaysOfASamityByLoanAccountId(loanAccountId)
                        .doOnRequest(l -> log.info("Request received to fetch holidays"))
                        .map(HolidayResponseDTO::getHolidayDate)
                        .collectList()
                        .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                        .map(holidays -> repaymentDatesCommands.getRepaymentDates(holidays, disburseDate, DayOfWeek.valueOf(samityDay), graceDays, noOfInstallments, repaymentFrequency, monthlyRepaymentFrequencyDay, loanTerm))
                        .flatMap(repaymentDates -> {
                            log.info("repayment dates : {}", repaymentDates);
                            return Mono.just(this.getRepaymentSchedulesList(loanAmount, noOfInstallments, installmentAmount, repaymentDates, repaymentScheduleList, roundingMode, finalServiceChargeRatePerPeriod, initialServiceCharge, probableEI, adjustedAmount, requestDTO.getServiceChargeAmountPrecision()))
                                    .map(repaymentSchedules -> this.createRepaymentScheduleToSaveToDB(repaymentSchedules, loanAccountId, memberId, mfiId, status, createdBy));
                        })
                        .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                        .flatMap(port::saveRepaymentSchedule)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                        .flatMapMany(Flux::fromIterable)
                        .map(repaymentSchedule -> buildResponseDTO(repaymentSchedule, finalServiceChargeRatePerPeriod))
                        .collectList())
                .map(list -> Tuples.of(list, finalAnnualServiceChargeRate));

    }

    private BigDecimal getLoanTermInYears(Integer loanTermInMonth) {
        if (loanTermInMonth == null)
            return BigDecimal.ONE;
        return BigDecimal.valueOf(loanTermInMonth).divide(BigDecimal.valueOf(12));
    }

    @Override
    public Mono<Tuple2<List<RepaymentScheduleResponseDTO>, BigDecimal>> getRepaymentScheduleForLoanFlat(LoanRepaymentScheduleRequestDTO requestDTO) {
        RoundingMode roundingMode1 = getRoundingMode(requestDTO.getRoundingLogic());
        Integer monthlyRepaymentFrequencyDay = requestDTO.getMonthlyRepaymentFrequencyDay() > 1 ? requestDTO.getMonthlyRepaymentFrequencyDay() : 1;

        BigDecimal serviceChargeRateInDecimal = requestDTO.getServiceChargeRate();
        BigDecimal serviceChargeRatePerAnnum;

        if (requestDTO.getServiceChargeRateFrequency().equalsIgnoreCase(RepaymentScheduleEnum.FREQUENCY_MONTHLY.getValue()))
            serviceChargeRatePerAnnum = serviceChargeRateInDecimal.multiply(BigDecimal.valueOf(12));
        else {
            serviceChargeRatePerAnnum = serviceChargeRateInDecimal;
        }

        serviceChargeRatePerAnnum = serviceChargeRatePerAnnum.multiply(getLoanTermInYears(requestDTO.getLoanTerm()));
        serviceChargeRatePerAnnum = serviceChargeRatePerAnnum.setScale(requestDTO.getServiceChargeRatePrecision(), roundingMode1);

        log.info("serviceChargeRatePerAnnum : {}", serviceChargeRatePerAnnum);

        BigDecimal totalServiceCharge = requestDTO.getLoanAmount().multiply(serviceChargeRatePerAnnum);
        totalServiceCharge = totalServiceCharge.setScale(requestDTO.getServiceChargeAmountPrecision(), roundingMode1);


        BigDecimal totalRepaymentAmount = requestDTO.getLoanAmount().add(totalServiceCharge);
        log.info("totalRepaymentAmount : {}", totalRepaymentAmount);
        BigDecimal calculatedInstallmentAmount = totalRepaymentAmount.divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), requestDTO.getInstallmentPrecision(), roundingMode1);
        log.info("calculatedInstallmentAmount : {}", calculatedInstallmentAmount);
        BigDecimal installmentAmount = requestDTO.getInstallmentAmount();

        BigDecimal principalPerInstallment = installmentAmount.divide(serviceChargeRatePerAnnum.add(BigDecimal.ONE), requestDTO.getServiceChargeAmountPrecision(), roundingMode1);
        log.info("principal per installment : {}", principalPerInstallment);
        BigDecimal serviceChargePerInstallment = installmentAmount.subtract(principalPerInstallment);
        BigDecimal adjustedAmount = installmentAmount.subtract(totalRepaymentAmount.divide(BigDecimal.valueOf(requestDTO.getNoOfInstallments()), roundingMode1));
        log.info("adjustedAmount : {}", adjustedAmount);
        BigDecimal totalExtraPayment = BigDecimal.valueOf(Math.abs(adjustedAmount.doubleValue() * (requestDTO.getNoOfInstallments()))).setScale(2, RoundingMode.HALF_UP);
        log.info("totalExtraPayment : {}", totalExtraPayment);
        log.info("repayment frequency : {}", requestDTO.getRepaymentFrequency());

        BigDecimal finalServiceChargeRatePerAnnum = serviceChargeRatePerAnnum;
        return holidayUseCase.getAllHolidaysOfASamityByLoanAccountId(requestDTO.getLoanAccountId())
                .map(HolidayResponseDTO::getHolidayDate)
                .collectList()
                .doOnNext(localDates -> log.info("Holidays : {}", localDates))
                .map(holidays -> requestDTO.getRepaymentFrequency().equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_SINGLE)
                        ? this.getRepaymentDateForSeasonalSingle(requestDTO.getDisburseDate(), requestDTO.getGraceDays(), requestDTO.getLoanTerm())
                        : repaymentDatesCommands.getRepaymentDates(holidays, requestDTO.getDisburseDate(), DayOfWeek.valueOf(requestDTO.getSamityDay()), requestDTO.getGraceDays(), requestDTO.getNoOfInstallments(), requestDTO.getRepaymentFrequency(), monthlyRepaymentFrequencyDay, requestDTO.getLoanTerm()))
                .flatMap(repaymentDates -> {
                    log.info("repayment dates : {}", repaymentDates);
                    return Mono.just(requestDTO.getRepaymentFrequency().equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_SINGLE)
                                    ? this.getRepaymentScheduleForSeasonalSingle(requestDTO.getLoanAmount(), calculatedInstallmentAmount, requestDTO.getInstallmentAmount(), repaymentDates, principalPerInstallment, serviceChargePerInstallment, adjustedAmount, requestDTO.getServiceChargeRate(), requestDTO.getServiceChargeAmountPrecision(), roundingMode1)
                                    : this.getRepaymentScheduleForFlatInstallment(requestDTO.getLoanAmount(), requestDTO.getNoOfInstallments(), calculatedInstallmentAmount, requestDTO.getInstallmentAmount(), repaymentDates, principalPerInstallment, serviceChargePerInstallment, adjustedAmount, totalExtraPayment, requestDTO.getServiceChargeAmountPrecision(), roundingMode1))
                            .map(repaymentSchedules -> this.createRepaymentScheduleToSaveToDBFlat(repaymentSchedules, requestDTO.getLoanAccountId(), requestDTO.getMemberId(), requestDTO.getMfiId(), requestDTO.getStatus(), requestDTO.getLoginId()));
                })
                .doOnNext(repaymentSchedules -> log.debug("repayment schedule to be saved to db : {}", repaymentSchedules))
                .flatMap(port::saveRepaymentSchedule)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentSchedule -> buildResponseDTO(repaymentSchedule, requestDTO.getServiceChargeRate()))
                .collectList()
                .map(list -> Tuples.of(list, finalServiceChargeRatePerAnnum));
    }

    private List<RepaymentSchedule> getRepaymentScheduleForSeasonalSingle(BigDecimal loanAmount, BigDecimal calculatedInstallmentAmount, BigDecimal installmentAmount, List<LocalDate> repaymentDates, BigDecimal principalPerInstallment, BigDecimal serviceChargePerInstallment, BigDecimal adjustedAmount, BigDecimal serviceChargeRate, Integer serviceChargePrecision, RoundingMode roundingMode1) {
        List<RepaymentSchedule> repaymentScheduleList = new ArrayList<>();
        BigDecimal serviceCharge = installmentAmount.subtract(principalPerInstallment);
        serviceCharge = serviceCharge.setScale(serviceChargePrecision, roundingMode1);
        RepaymentSchedule repayment = RepaymentSchedule
                .builder()
                .installNo(1)
                .installDate(repaymentDates.get(0))
                .dayOfWeek(repaymentDates.get(0).getDayOfWeek().toString())
                .beginPrinBalance(loanAmount)
                .scheduledPayment(calculatedInstallmentAmount)
                .extraPayment(adjustedAmount)
                .totalPayment(installmentAmount)
                .principal(principalPerInstallment)
                .serviceCharge(serviceCharge)
                .serviceChargeRatePerPeriod(serviceChargeRate)
                .endPrinBalance(loanAmount.subtract(principalPerInstallment))
                .loanRepayScheduleId(UUID.randomUUID() + "-repay-" + (1))
                .build();
        repaymentScheduleList.add(repayment);
        log.info("repayment schedule : {}", repayment);
        return repaymentScheduleList;
    }

    private List<LocalDate> getRepaymentDateForSeasonalSingle(LocalDate disburseDate, Integer graceDays, Integer loanTerm) {
        LocalDate gracePeriodEndDate = disburseDate.plusDays(graceDays);
        log.info("Grace Period End Date : {}", gracePeriodEndDate);
        LocalDate singleInstallmentDate = gracePeriodEndDate.plusMonths(loanTerm);
        log.info("Seasonal Single Installment Date : {}", singleInstallmentDate);
        return List.of(singleInstallmentDate);
    }

    @Override
    public Mono<List<RepaymentScheduleViewDTO>> getRepaymentScheduleWithFlatPrincipal(BigDecimal loanAmount, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, Integer graceDays, LocalDate disburseDate, String samityDay, String loanTerm, String paymentPeriod, String roundingLogic, String daysInYear, Integer serviceChargeRatePrecision, Integer serviceChargePrecision, Integer installmentAmountPrecision, String installmentRoundingTo, Integer monthlyRepaymentFrequencyDay) {
        RoundingMode roundingMode = getRoundingMode(roundingLogic);
        List<LocalDate> holidays = new ArrayList<>();
        List<LocalDate> samityDays = new ArrayList<>(getRepaymentDates(holidays, disburseDate, DayOfWeek.valueOf(samityDay), graceDays, noOfInstallments, paymentPeriod, monthlyRepaymentFrequencyDay));
        BigDecimal serviceChargeRatePerPeriod = getServiceChargeRatePerPeriod(serviceChargeRate, serviceChargeRateFrequency, paymentPeriod, daysInYear);
        serviceChargeRatePerPeriod = round(serviceChargeRatePrecision, serviceChargeRatePerPeriod, roundingMode);
        BigDecimal flatPrincipal = loanAmount.divide(BigDecimal.valueOf(noOfInstallments), 2, RoundingMode.HALF_UP);
        List<RepaymentSchedule> list = new ArrayList<>();

        RepaymentSchedule table1 = new RepaymentSchedule();
        BigDecimal initialInterest = round(serviceChargePrecision, loanAmount.multiply(serviceChargeRatePerPeriod), roundingMode);
        BigDecimal initialInstallment = round(installmentAmountPrecision, flatPrincipal.add(initialInterest), roundingMode);
        table1.setEndPrinBalance(loanAmount);
        table1.setTotalPayment(roundInstallment(installmentRoundingTo, initialInstallment));
        table1.setInstallNo(1);
        table1.setPrincipal(flatPrincipal);
        table1.setServiceCharge(initialInterest);
        log.info("samityDays : {}", samityDays);
        table1.setInstallDate(samityDays.get(0));
        list.add(table1);

        BigDecimal finalServiceChargeRatePerPeriod = serviceChargeRatePerPeriod;

        for (int i = 1; i < noOfInstallments; i++) {
            RepaymentSchedule table2 = new RepaymentSchedule();
            BigDecimal updatedOutstandingPrincipal = list.get(i - 1).getEndPrinBalance().subtract(flatPrincipal);
            BigDecimal updatedInterest = round(serviceChargePrecision, updatedOutstandingPrincipal.multiply(finalServiceChargeRatePerPeriod), roundingMode);
            BigDecimal updatedWeeklyInstallment = round(installmentAmountPrecision, flatPrincipal.add(updatedInterest), roundingMode);
            int installmentNo = list.get(i - 1).getInstallNo() + 1;
            table2.setEndPrinBalance(updatedOutstandingPrincipal);
            table2.setServiceCharge(updatedInterest);
            table2.setTotalPayment(roundInstallment(installmentRoundingTo, updatedWeeklyInstallment));
            table2.setInstallNo(installmentNo);
            table2.setPrincipal(flatPrincipal);
            table2.setInstallDate(samityDays.get(i));
            list.add(table2);
        }

        return Flux.fromIterable(list)
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleViewDTO.class))
                .collectList();
    }

    @Override
    public Mono<RepaymentScheduleResponseDTO> getRepaymentDetailsByInstallmentNoAndLoanAccountId(Integer installmentNo, String loanAccountId) {
        return port
                .getRepaymentDetailsByInstallmentNoAndLoanAccountId(installmentNo, loanAccountId)
                .doOnRequest(value -> log.info("Service Log : request Received"))
                .doOnNext(repaymentScheduleEntity -> log.info("Got Repayment info : {}", repaymentScheduleEntity))
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleResponseDTO.class));
    }

    @Override
    public Mono<BigDecimal> getTotalLoanPay(String loanAccountId) {
        return port.getTotalLoanPay(loanAccountId);
    }

    @Override
    public Mono<List<RepaymentScheduleResponseDTO>> getRepaymentScheduleByLoanAccountId(String loanAccountId) {

        return commonRepository
                .getMemberInfoByLoanAccountId(loanAccountId)
                .flatMap(memberEntity ->
                        repository
                                .getRepaymentScheduleEntitiesByLoanAccountIdOrderByInstallNo(loanAccountId)
                                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentScheduleResponseDTO.class))
                                .map(repaymentScheduleResponseDTO -> {
                                    repaymentScheduleResponseDTO.setMemberNameEn(memberEntity.getMemberNameEn());
                                    repaymentScheduleResponseDTO.setMemberNameBn(memberEntity.getMemberNameBn());
                                    return repaymentScheduleResponseDTO;
                                })
                                .collectList()
                );
    }


    private RoundingMode getRoundingMode(String roundingLogic) {
        RoundingMode roundingMode = null;
        switch (roundingLogic.toUpperCase()) {
            case "HALFUP" -> roundingMode = RoundingMode.HALF_UP;
            case "HALFDOWN" -> roundingMode = RoundingMode.HALF_DOWN;
            case "UP" -> roundingMode = RoundingMode.UP;
            case "DOWN" -> roundingMode = RoundingMode.DOWN;
            case "NO_ROUNDING" -> roundingMode = RoundingMode.DOWN;
        }
        return roundingMode;
    }

    private RepaymentScheduleResponseDTO buildResponseDTO(RepaymentSchedule repaymentSchedule, BigDecimal serviceChargeRatePerPeriod) {
        return RepaymentScheduleResponseDTO
                .builder()
                .loanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId())
                .loanAccountId(repaymentSchedule.getLoanAccountId())
                .memberId(repaymentSchedule.getMemberId())
                .installNo(repaymentSchedule.getInstallNo())
                .installDate(repaymentSchedule.getInstallDate())
                .beginPrinBalance(repaymentSchedule.getBeginPrinBalance())
                .scheduledPayment(repaymentSchedule.getScheduledPayment())
                .extraPayment(repaymentSchedule.getExtraPayment())
                .totalPayment(repaymentSchedule.getTotalPayment())
                .principal(repaymentSchedule.getPrincipal())
                .serviceCharge(repaymentSchedule.getServiceCharge())
                .endPrinBalance(repaymentSchedule.getEndPrinBalance())
                .serviceChargeRatePerPeriod(serviceChargeRatePerPeriod)
                .build();
    }

    public List<LocalDate> getRepaymentDates(List<LocalDate> holidays, LocalDate disburseDate, DayOfWeek samityDay, Integer graceDays, Integer noOfInstallments, String repaymentFrequency, Integer monthlyRepaymentFrequencyDay) {


        log.info("holidays : {}", holidays);
        log.info("disburse date  : {}", disburseDate);
        log.info("samityday  : {}", samityDay);
        log.info("grace days  : {}", graceDays);
        log.info("no of installments : {}", noOfInstallments);
        log.info("payment period : {}", repaymentFrequency);

        List<LocalDate> samityDays = new LinkedList<>();

        LocalDate gracePeriodEndingDate = disburseDate.plusDays(graceDays);
        log.info("gracePeriodEndingDate : {}", gracePeriodEndingDate);
        LocalDate firstInstallmentDate;

        int daysToAdd = 0;
        switch (repaymentFrequency.toUpperCase()) {
            case Constants.REPAYMENT_FREQUENCY_WEEKLY -> daysToAdd = 7;
            case Constants.REPAYMENT_FREQUENCY_MONTHLY -> daysToAdd = 30;
            case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> daysToAdd = 180;
            case Constants.REPAYMENT_FREQUENCY_YEARLY -> daysToAdd = 365;
        }

        DayOfWeek expectedFirstInstallmentDay = gracePeriodEndingDate.getDayOfWeek();

        if (expectedFirstInstallmentDay.equals(samityDay)) {
            firstInstallmentDate = gracePeriodEndingDate;
        } else {
            firstInstallmentDate = gracePeriodEndingDate.with(samityDay);
            if (firstInstallmentDate.isBefore(gracePeriodEndingDate)) {
                firstInstallmentDate = firstInstallmentDate.plusDays(daysToAdd);
            }
        }

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            firstInstallmentDate = gracePeriodEndingDate.plusDays(daysToAdd);
        }



        log.info("Expected First installment Date: {}", firstInstallmentDate);

        /*if (holidays.contains(firstInstallmentDate)) firstInstallmentDate = firstInstallmentDate.plusDays(daysToAdd);*/

        while (holidays.contains(firstInstallmentDate) || firstInstallmentDate.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            firstInstallmentDate = firstInstallmentDate.plusDays(1);
        }

        log.info("Selected First installment considering Holiday : {}", firstInstallmentDate);

        int count = 0;

        if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_WEEKLY)) {
            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusDays(daysToAdd)) {

                if (date.getDayOfWeek() == samityDay && !holidays.contains(date)) {
                    count++;
                    samityDays.add(date);
                } else if (holidays.contains(date)) {
                    LocalDate dateCoincidedWithHoliday = date;
                    while (holidays.contains(date)) {
                        date = date.plusDays(7);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }
                    count++;
                    samityDays.add(date);
                    log.info("InstallNo : {}, InstallDate : {} coincided with holiday. Hence shifted to : {}", count, dateCoincidedWithHoliday, date);
                }
            }
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_MONTHLY) || repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_HALF_YEARLY)) {
            int incrementFactor = 0;
            switch (repaymentFrequency.toUpperCase()) {
                case Constants.REPAYMENT_FREQUENCY_MONTHLY -> incrementFactor = 1;
                case Constants.REPAYMENT_FREQUENCY_HALF_YEARLY -> incrementFactor = 6;
            }

            LocalDate probableFirstInstallmentDate = firstInstallmentDate;
            LocalDate targetFirstInstallmentDate = calculateTargetSamityDay(probableFirstInstallmentDate, monthlyRepaymentFrequencyDay, samityDay);

            log.info("into else if : increment factor : {}", incrementFactor);
            for (LocalDate date = targetFirstInstallmentDate; count < noOfInstallments; date = date.plusMonths(incrementFactor)) {

                if (holidays.contains(date) || date.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {

                    LocalDate providedDate = date;
                    date = date.plusDays(1);

                    while (holidays.contains(date)) {
                        date = date.plusDays(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }

                    LocalDate samityDate = calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay);
                    samityDays.add(samityDate);
                    count++;
                    log.info("Installment No : {}, Installment Date : {} coincides with Samity Off Day. Hence rescheduled to : {}", count, providedDate, date);
                } else {
                    samityDays.add(calculateTargetSamityDay(date, monthlyRepaymentFrequencyDay, samityDay));
                    count++;
                }

            }
        } else if (repaymentFrequency.equalsIgnoreCase(Constants.REPAYMENT_FREQUENCY_YEARLY)) {
            for (LocalDate date = firstInstallmentDate; count < noOfInstallments; date = date.plusDays(daysToAdd)) {

                if (date.getDayOfWeek() == samityDay && !holidays.contains(date)) {
                    count++;
                    samityDays.add(date);
                } else if (holidays.contains(date)) {
                    LocalDate dateCoincidedWithHoliday = date;
                    while (holidays.contains(date)) {
                        date = date.plusDays(1);
                        if (!holidays.contains(date)) {
                            break;
                        }
                    }
                    count++;
                    samityDays.add(date);
                    log.info("InstallNo : {}, InstallDate : {} coincided with holiday. Hence shifted to : {}", count, dateCoincidedWithHoliday, date);
                }
            }
        }

        log.info("samityDays from Service.getRepaymentDates : {}", samityDays);
        return samityDays;

    }


    private static LocalDate calculateTargetSamityDay(LocalDate probableInstallmentDate, int monthlyRepaymentFrequencyDay, DayOfWeek samityDay) {
        LocalDate targetDate = LocalDate.of(probableInstallmentDate.getYear(), probableInstallmentDate.getMonth(), monthlyRepaymentFrequencyDay);
        // Find the nearest samity day (Sunday) before the target date
        while (targetDate.getDayOfWeek() != samityDay) {
            targetDate = targetDate.plusDays(1);
        }

        return targetDate;
    }

    private BigDecimal round(int scale, BigDecimal amount, RoundingMode roundingMode) {
        return amount.setScale(scale, roundingMode);
    }


    private List<RepaymentSchedule> createRepaymentScheduleToSaveToDB(List<RepaymentSchedule> repaymentScheduleList, String loanAccountId, String memberId, String mfiId, String status, String createdBy) {
        return repaymentScheduleList
                .stream()
                .peek(repaymentSchedule -> {
                    repaymentSchedule.setLoanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId());
                    repaymentSchedule.setLoanAccountId(loanAccountId);
                    repaymentSchedule.setMemberId(memberId);
                    repaymentSchedule.setInstallNo(repaymentSchedule.getInstallNo());
                    repaymentSchedule.setInstallDate(repaymentSchedule.getInstallDate());
                    repaymentSchedule.setBeginPrinBalance(repaymentSchedule.getBeginPrinBalance());
                    repaymentSchedule.setScheduledPayment(repaymentSchedule.getScheduledPayment());
                    repaymentSchedule.setExtraPayment(repaymentSchedule.getExtraPayment());
                    repaymentSchedule.setTotalPayment(repaymentSchedule.getTotalPayment());
                    repaymentSchedule.setPrincipal(repaymentSchedule.getPrincipal());
                    repaymentSchedule.setServiceCharge(repaymentSchedule.getServiceCharge());
                    repaymentSchedule.setEndPrinBalance(repaymentSchedule.getEndPrinBalance());
                    repaymentSchedule.setMfiId(mfiId);
                    repaymentSchedule.setCreatedOn(LocalDateTime.now());
                    repaymentSchedule.setCreatedBy(createdBy);
                    repaymentSchedule.setStatus(status);
                })
                .skip(1)
                .toList();
    }

    private List<RepaymentSchedule> createRepaymentScheduleToSaveToDBFlat(List<RepaymentSchedule> repaymentScheduleList, String loanAccountId, String memberId, String mfiId, String status, String createdBy) {
        return repaymentScheduleList
                .stream()
                .peek(repaymentSchedule -> {
                    repaymentSchedule.setLoanRepayScheduleId(repaymentSchedule.getLoanRepayScheduleId());
                    repaymentSchedule.setLoanAccountId(loanAccountId);
                    repaymentSchedule.setMemberId(memberId);
                    repaymentSchedule.setInstallNo(repaymentSchedule.getInstallNo());
                    repaymentSchedule.setInstallDate(repaymentSchedule.getInstallDate());
                    repaymentSchedule.setBeginPrinBalance(repaymentSchedule.getBeginPrinBalance());
                    repaymentSchedule.setScheduledPayment(repaymentSchedule.getScheduledPayment());
                    repaymentSchedule.setExtraPayment(repaymentSchedule.getExtraPayment());
                    repaymentSchedule.setTotalPayment(repaymentSchedule.getTotalPayment());
                    repaymentSchedule.setPrincipal(repaymentSchedule.getPrincipal());
                    repaymentSchedule.setServiceCharge(repaymentSchedule.getServiceCharge());
                    repaymentSchedule.setEndPrinBalance(repaymentSchedule.getEndPrinBalance());
                    repaymentSchedule.setMfiId(mfiId);
                    repaymentSchedule.setCreatedOn(LocalDateTime.now());
                    repaymentSchedule.setCreatedBy(createdBy);
                    repaymentSchedule.setStatus(status);
                })
                .toList();
    }


    private List<RepaymentSchedule> getRepaymentSchedulesList(BigDecimal loanAmount, Integer noOfInstallments, BigDecimal installmentAmount, List<LocalDate> samityDays, List<RepaymentSchedule> repaymentScheduleList, RoundingMode roundingMode, BigDecimal serviceChargeRatePerPeriod, BigDecimal initialServiceCharge, BigDecimal probableEI, BigDecimal adjustedAmount, Integer serviceChargePrecision) {
        initialServiceCharge = round(serviceChargePrecision, initialServiceCharge, roundingMode);
        log.info("samity days : {}", samityDays);
        int count = 0;
        for (int j = 0; j <= noOfInstallments; j++) {
            RepaymentSchedule table = new RepaymentSchedule();
            // First Row , Installment 0
            if (count == 0) {
                table.setBeginPrinBalance(loanAmount);
                table.setEndPrinBalance(loanAmount);
                table.setPrincipal(BigDecimal.ZERO);
                table.setServiceCharge(BigDecimal.ZERO);
                table.setTotalPayment(BigDecimal.ZERO);
                table.setInstallNo(0);
                table.setExtraPayment(BigDecimal.ZERO);
                table.setScheduledPayment(BigDecimal.ZERO);
                table.setInstallDate(samityDays.get(0));
                table.setLoanRepayScheduleId("LoanRepayScheduleId");
                repaymentScheduleList.add(table);
                count++;
            }
            // Last Installment
            else if (count == noOfInstallments) {
                BigDecimal lastPrincipal = loanAmount.subtract(repaymentScheduleList.stream().skip(1).map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add));
                log.debug("last principal : {}", lastPrincipal);
                BigDecimal previousBalance = repaymentScheduleList.get(j - 1).getEndPrinBalance();
                log.debug("previous balance : {}", previousBalance);
                BigDecimal lastInterest = round(serviceChargePrecision, serviceChargeRatePerPeriod.multiply(previousBalance), roundingMode);
                log.debug("last interest : {}", lastInterest);
                BigDecimal paymentDue = round(0, previousBalance.add(lastInterest), RoundingMode.HALF_UP);
                log.debug("payment due : {}", paymentDue);
                BigDecimal updatedBalance = previousBalance.subtract(previousBalance).setScale(0, RoundingMode.FLOOR);
                log.debug("updated balance : {}", updatedBalance);
                table.setInstallNo(repaymentScheduleList.get(j - 1).getInstallNo() + 1);
                table.setPrincipal(previousBalance);
                table.setServiceCharge(lastInterest);
                table.setTotalPayment(paymentDue);
                table.setScheduledPayment(probableEI);
                table.setExtraPayment(table.getTotalPayment().subtract(table.getScheduledPayment()));
                table.setEndPrinBalance(updatedBalance);
                table.setBeginPrinBalance(previousBalance);
                table.setInstallDate(samityDays.get(samityDays.size() - 1));
                table.setLoanRepayScheduleId(UUID.randomUUID() + "-repay-" + table.getInstallNo());
                repaymentScheduleList.add(table);
                count++;
            }
            // Installment No (1 -> n-1)
            else {
                // installment No 1
                if (repaymentScheduleList.get(j - 1).getServiceCharge() == null) {
                    BigDecimal previousBalance = round(serviceChargePrecision, repaymentScheduleList.get(j - 1).getEndPrinBalance(), roundingMode);
                    createTableData(installmentAmount, repaymentScheduleList, j, table, previousBalance, initialServiceCharge, adjustedAmount, probableEI, samityDays, count - 1);
                    count++;
                }
                // installment No (2 -> n-1)
                else {
                    BigDecimal previousBalance = repaymentScheduleList.get(j - 1).getEndPrinBalance();
                    BigDecimal updatedInterest = round(serviceChargePrecision, serviceChargeRatePerPeriod.multiply(previousBalance), roundingMode);
                    createTableData(installmentAmount, repaymentScheduleList, j, table, previousBalance, updatedInterest, adjustedAmount, probableEI, samityDays, count - 1);
                    count++;
                }
            }
        }
        return repaymentScheduleList;
    }

    private void createTableData(BigDecimal installmentAmount, List<RepaymentSchedule> list, Integer j, RepaymentSchedule table, BigDecimal previousBalance, BigDecimal updatedInterest, BigDecimal adjustedAmount, BigDecimal scheduledAmount, List<LocalDate> repaymentDates, Integer count) {

        BigDecimal updatedPrincipal = installmentAmount.subtract(updatedInterest);
        table.setPrincipal(updatedPrincipal);
        BigDecimal updatedBalance = previousBalance.subtract(updatedPrincipal);
        table.setEndPrinBalance(updatedBalance);
        table.setExtraPayment(adjustedAmount);
        table.setScheduledPayment(scheduledAmount);
        table.setServiceCharge(updatedInterest);
        table.setInstallNo(list.get(j - 1).getInstallNo() + 1);
        table.setTotalPayment(installmentAmount);
        table.setBeginPrinBalance(previousBalance);
        table.setInstallDate(repaymentDates.get(count));
        table.setLoanRepayScheduleId(UUID.randomUUID() + "-repay-" + table.getInstallNo());
        list.add(table);
    }

    private BigDecimal roundInstallment(String installmentRoundingTo, BigDecimal installmentAmount) {
        if (installmentRoundingTo == null) {
            return installmentAmount;
        } else if (installmentRoundingTo.equals("1")) {
            return round(0, installmentAmount, RoundingMode.HALF_UP);
        } else {
            throw new IllegalArgumentException("Invalid installmentRoundingTo value: " + installmentRoundingTo);
        }
    }

}
