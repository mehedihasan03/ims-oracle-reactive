package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RebateInfoEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.RepaymentScheduleRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Status.STATUS_PENDING;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Status.STATUS_REBATED;

@Slf4j
@Component
public class RepaymentScheduleAdapter implements RepaymentSchedulePersistencePort {

    private final RepaymentScheduleRepository repository;
    private final ModelMapper mapper = new ModelMapper();

    private final Gson gson;

    public RepaymentScheduleAdapter(RepaymentScheduleRepository repository) {
        this.repository = repository;
        this.gson = CommonFunctions.buildGson(this);
    }

    private static String refactorWithTwoDecimalPlaces(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");
        return decimalFormat.format(number);
    }

    private static String refactorWithFourDecimalPlaces(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00.0000");
        return decimalFormat.format(number);
    }

    private static String refactorWholeNumbers(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        return decimalFormat.format(number);
    }

    private static String prettyPrintWithStringWithEqualLength(String number, Integer maximumLength) {
        return String.format("%1$" + maximumLength + "s", number);
    }

    private static void printWithTwoDecimalPlaces(Number number, String string) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");
        System.out.println(string + " = " + decimalFormat.format(number));
    }

    @Override
    public Mono<List<RepaymentSchedule>> saveRepaymentSchedule(List<RepaymentSchedule> repaymentScheduleList) {

//        log.info("InstallDate List : {}", repaymentScheduleList.stream().map(RepaymentSchedule::getInstallDate).toList());
//        log.info("Received Repayment Schedule List : {}", repaymentScheduleList.stream().map(repaymentSchedule -> gson.toJson(repaymentSchedule)).toList());

        /*TypeMap<RepaymentSchedule, RepaymentScheduleEntity> domainToEntityTypeMap =
                mapper.typeMap(RepaymentSchedule.class, RepaymentScheduleEntity.class)
                        .addMappings(mapping -> {
                            *//*mapping.map(RepaymentSchedule::getInstallNo, RepaymentScheduleEntity::setPaymentAttempts);*//*
                            *//*mapping.map(source -> LocalDate.now(), RepaymentScheduleEntity::setMakeUpInstallDate);*//*
                            *//*mapping.map(source -> BigDecimal.ZERO, RepaymentScheduleEntity::setPenalty);
                            mapping.map(source -> BigDecimal.ZERO, RepaymentScheduleEntity::setFees);
                            mapping.map(source -> BigDecimal.ZERO, RepaymentScheduleEntity::setInsurance);
                            mapping.map(source -> BigDecimal.ZERO, RepaymentScheduleEntity::setPenaltyOnHold);
                            mapping.map(source -> "Rescheduled ID", RepaymentScheduleEntity::setIfRescheduledId);*//*
                            *//*mapping.map(source -> "Deleted", RepaymentScheduleEntity::setDeleted);
                            mapping.map(source -> "SUNDAY", RepaymentScheduleEntity::setSamityDay);*//*
                            mapping.map(source -> STATUS_PENDING.getValue(), RepaymentScheduleEntity::setStatus);
                            mapping.map(RepaymentSchedule::getInstallDate, RepaymentScheduleEntity::setInstallDate);
                        });
*/
        List<RepaymentScheduleEntity> entityList = repaymentScheduleList
                .stream()
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleEntity.class))
                .toList();


        return Flux.fromIterable(entityList)
                .collectList()
                .flatMapMany(repository::saveAll)
//                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .collectList()
//                .flatMap(this::printRepaymentScheduleWithDates)
                .doOnSuccess(savedEntityList -> log.info("Saving Repayment Schedule to DB successful.  List Size : {}", savedEntityList.size()))
                .doOnError(throwable -> log.error("Saving Repayment Schedule to DB Failed!!! "))
                .map(list -> repaymentScheduleList);
    }

    @Override
    public Mono<RepaymentSchedule> getRepaymentDetailsByInstallmentNoAndLoanAccountId(Integer installmentNo, String loanAccountId) {
        return repository
                .findAllByInstallNoAndLoanAccountId(installmentNo, loanAccountId)
                .doOnRequest(value -> log.info("Adapter Log : request Received"))
                .doOnNext(repaymentScheduleEntity -> log.info("Got Repayment info : {}", repaymentScheduleEntity))
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class));
    }

    @Override
    public Mono<BigDecimal> getTotalLoanPay(String loanAccountId) {
        return repository
                .getTotalLoanPay(loanAccountId);
    }

    @Override
    public Mono<List<RepaymentSchedule>> getRepaymentScheduleListByLoanAccountId(String loanAccountId) {

        return repository
                .getRepaymentScheduleEntitiesByLoanAccountIdOrderByInstallNo(loanAccountId)
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .collectList()
//                .doOnNext(this::printRepaymentScheduleWithDates)
                .doOnNext(repaymentSchedules -> log.debug("collected to list : {}", repaymentSchedules))
                .map(list -> list.stream()
                        .sorted(Comparator.comparing(RepaymentSchedule::getInstallNo))
                        .collect(Collectors.toList()));
    }

    @Override
    public Flux<RepaymentSchedule> updateInstallmentStatus(List<Integer> installmentList, String status, String loanAccountId, String managementProcessId) {
        return repository
                .getRepaymentScheduleEntitiesByLoanAccountIdAndInstallNoIn(loanAccountId, installmentList)
                .filter(repaymentScheduleEntity -> !repaymentScheduleEntity.getStatus().equals(STATUS_REBATED.getValue()))
                .map(repaymentScheduleEntity -> {
                    repaymentScheduleEntity.setStatus(status);
                    repaymentScheduleEntity.setManagementProcessId(managementProcessId);
                    return repaymentScheduleEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .doOnComplete(() -> log.info("Successfully updated loanAccountId : {} installmentNo : {} status to : {}",loanAccountId, installmentList, status))
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .doOnError(throwable -> log.error("Error occurred during updateInstallmentStatus repay schedule : {}", throwable.getMessage()));
    }

    @Override
    public Mono<RebateInfoEntity> getRebateInfoByLoanAccountId(String loanAccountId) {
        return repository
                .getRebateInfoByLoanAccountId(loanAccountId)
                .doOnRequest(l -> log.info("Request Received to get Rebate info by Loan account id : {}", loanAccountId));
    }

    @Override
    public Mono<List<String>> updateInstallmentStatusToPending(List<String> loanRepayScheduleIdList) {
        return repository.findAllByLoanRepayScheduleId(loanRepayScheduleIdList)
                .map(repaymentScheduleEntity -> {
                    repaymentScheduleEntity.setStatus(STATUS_PENDING.getValue());
//                    repaymentScheduleEntity.setManagementProcessId(null);
                    return repaymentScheduleEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> loanRepayScheduleIdList);
    }

    @Override
    public Mono<List<RepaymentScheduleEntity>> updateRepaymentScheduleForSamityCancel(List<RepaymentSchedule> repaymentScheduleList, String loginId) {
        List<String> loanRepayScheduleIdList = repaymentScheduleList.stream().map(RepaymentSchedule::getLoanRepayScheduleId).toList();

        log.info("Received Repayment Schedule id List: {}", loanRepayScheduleIdList);
//        repaymentScheduleList.forEach(item -> {
//            item.setOid(null);
//            item.setLoanRepayScheduleId(UUID.randomUUID().toString() + "-repay-" + item.getInstallNo());
//            item.setCreatedBy(loginId);
//            item.setCreatedOn(LocalDateTime.now());
//        });

        return Flux.fromIterable(repaymentScheduleList)
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleEntity.class))
                .doOnNext(repaymentScheduleEntity -> log.info("Old Repay Schedule Entity : {}", repaymentScheduleEntity))
                .map(repaymentScheduleEntity -> {
//                    repaymentScheduleEntity.setStatus(Status.STATUS_RESCHEDULED.getValue());
                    repaymentScheduleEntity.setUpdatedBy(loginId);
                    repaymentScheduleEntity.setUpdatedOn(LocalDateTime.now());
                    for (RepaymentSchedule item:
                            repaymentScheduleList) {
                        if(Objects.equals(item.getInstallNo(), repaymentScheduleEntity.getInstallNo())){
                            repaymentScheduleEntity.setIfRescheduledId(item.getLoanRepayScheduleId());
                        }
                    }
                    return repaymentScheduleEntity;
                })
                .collectList()
                .doOnNext(entityList -> log.info("Old Repay Schedule List: {}", entityList))
                .flatMap(repaymentScheduleEntityList -> repository.saveAll(repaymentScheduleEntityList)
                        .collectList())
                .doOnNext(repaymentScheduleEntityList -> log.info("Updated Repay Schedule List: {}", repaymentScheduleList));

//        return repository.findByLoanRepayScheduleIdInOrderByInstallNo(loanRepayScheduleIdList)
//                .doOnNext(repaymentScheduleEntity -> log.info("Old Repay Schedule Entity : {}", repaymentScheduleEntity))
//                .map(repaymentScheduleEntity -> {
//                    repaymentScheduleEntity.setStatus(Status.STATUS_RESCHEDULED.getValue());
//                    repaymentScheduleEntity.setUpdatedBy(loginId);
//                    repaymentScheduleEntity.setUpdatedOn(LocalDateTime.now());
//                    for (RepaymentSchedule item:
//                         repaymentScheduleList) {
//                        if(Objects.equals(item.getInstallNo(), repaymentScheduleEntity.getInstallNo())){
//                            repaymentScheduleEntity.setIfRescheduledId(item.getLoanRepayScheduleId());
//                        }
//                    }
//                    return repaymentScheduleEntity;
//                })
//                .collectList()
//                .doOnNext(entityList -> log.info("Old Repay Schedule List: {}", entityList))
//                .flatMap(repaymentScheduleEntityList -> repository.saveAll(repaymentScheduleEntityList)
//                        .collectList())
//                .flatMap(list -> {
//                    List<RepaymentScheduleEntity> entityList = list.stream().map(item -> gson.fromJson(item.toString(), RepaymentScheduleEntity.class)).toList();
//                    return repository.saveAll(entityList).collectList();
//                })
//                .doOnNext(repaymentScheduleEntityList -> log.info("Updated Repay Schedule List: {}", repaymentScheduleList));
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> getRepaymentScheduleByInstallmentDate(LocalDate installmentDate) {
        return repository
                .findAllByInstallDate(installmentDate)
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentScheduleResponseDTO.class));
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> getUnprovisionedRepaymentSchedulesByInstallmentDate(LocalDate installmentDate, String officeId) {
        return repository.findAllByInstallDateIsLessThanEqualAndOfficeId(installmentDate, officeId)
                .filter(repaymentScheduleEntity -> repaymentScheduleEntity.getIsProvisioned() == null || !repaymentScheduleEntity.getIsProvisioned().equals(Status.STATUS_YES.getValue()))
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentScheduleResponseDTO.class));
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> updateIsProvisionedStatus(List<String> loanRepayScheduleIdList, String status) {
        return repository.findAllByLoanRepayScheduleIdIn(loanRepayScheduleIdList)
                .map(repaymentScheduleEntity -> {
                    repaymentScheduleEntity.setIsProvisioned(status);
                    return repaymentScheduleEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(repaymentScheduleEntityList -> log.info("Updated Repayment Schedule List size : {} with status : {}", repaymentScheduleEntityList.size(), status))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentScheduleResponseDTO.class));
    }

    @Override
    public Mono<Boolean> updateIsProvisionedStatus(String currentStatus, String updatedStatus) {
        return repository.findAllByIsProvisioned(currentStatus)
                .collectList()
                .doOnNext(repaymentScheduleEntityList -> log.info("Received Repayment Schedule List size : {} with status : {}", repaymentScheduleEntityList.size(), currentStatus))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentScheduleEntity -> {
                    repaymentScheduleEntity.setIsProvisioned(updatedStatus);
                    return repaymentScheduleEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(repaymentScheduleEntityList -> log.info("Updated Repayment Schedule List size : {} with status : {}", repaymentScheduleEntityList.size(), updatedStatus))
                .flatMap(entityList -> Mono.just(Boolean.TRUE))
                .onErrorResume(e -> {
                    log.error("Error occurred while updating the provisioned status: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Failed to update the provisioned status", e));
                });
    }

    @Override
    public Mono<RepaymentSchedule> getFirstPendingRepaymentScheduleByLoanAccountId(String loanAccountId) {
        return repository.findFirstByLoanAccountIdAndStatusOrderByInstallNo(loanAccountId, STATUS_PENDING.getValue())
                .doOnRequest(req -> log.info("Request Received to get First Pending Repayment Schedule by Loan Account Id : {}", loanAccountId))
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .doOnSuccess(repaymentSchedule -> log.info("First Pending Repayment Schedule : {}", repaymentSchedule))
                .doOnError(throwable -> log.error("Error occurred while getting First Pending Repayment Schedule : {}", throwable.getMessage()));
    }

    @Override
    public Flux<RepaymentSchedule> getRepaymentScheduleListByOidList(List<String> oidList) {
        return repository.findAllById(oidList)
                .doOnRequest(req -> log.info("Request Received to get Repayment Schedule by OID : {}", oidList))
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .doOnNext(repaymentSchedule -> log.info("Repayment Schedule : {}", repaymentSchedule))
                .doOnError(throwable -> log.error("Error occurred while getting Repayment Schedule : {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<RepaymentSchedule>> saveAllRepaymentSchedule(List<RepaymentSchedule> repaymentScheduleList) {
        return Flux.fromIterable(repaymentScheduleList)
                .map(repaymentSchedule -> mapper.map(repaymentSchedule, RepaymentScheduleEntity.class))
//                .doOnNext(repaymentScheduleEntity -> log.info("Repayment Schedule Entity : {}", repaymentScheduleEntity))
                .collectList()
                .flatMapMany(repository::saveAll)
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentSchedule.class))
                .collectList()
                .doOnNext(repaymentSchedule -> log.info("Saved Repayment Schedule with size : {}", repaymentSchedule.size()))
                .doOnError(throwable -> log.error("Error occurred while saving Repayment Schedule : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteRepaymentScheduleListByOid(List<String> oidList) {
        return repository.deleteAllById(oidList)
                .doOnRequest(req -> log.info("Request Received to delete Repayment Schedule by OID : {}", oidList))
                .doOnSuccess(req -> log.info("Deleted Repayment Schedule by OID : {}", oidList))
                .doOnError(throwable -> log.error("Error occurred while deleting Repayment Schedule by OID : {}", throwable.getMessage()))
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> updateRepaymentScheduleStatusByOid(List<String> oidList, String status) {
        return repository.findAllById(oidList)
                .map(repaymentScheduleEntity -> {
                    repaymentScheduleEntity.setStatus(status);
                    repaymentScheduleEntity.setManagementProcessId(null);
                    return repaymentScheduleEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .doOnRequest(req -> log.info("Request Received to update Repayment Schedule status by OID : {}", oidList))
                .doOnComplete(() -> log.info("Updated Repayment Schedule status by OID : {}", oidList))
                .doOnError(throwable -> log.error("Error occurred while updating Repayment Schedule status by OID : {}", throwable.getMessage()))
                .collectList()
                .map(repaymentScheduleEntities -> true);
    }

    @Override
    public Mono<Boolean> deleteRepaymentScheduleListByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId) {
        return repository.deleteAllByManagementProcessIdAndLoanAccountId(managementProcessId, loanAccountId)
                .thenReturn(true);
    }

    @Override
    public Flux<RepaymentScheduleResponseDTO> updateInstallmentStatusFromInstallmentNoToLast(Integer installmentNo, String status, String loanAccountId, String managementProcessId) {
        return repository.updateInstallmentStatusFromInstallmentNoToLast(installmentNo, status, loanAccountId, managementProcessId)
                .map(repaymentScheduleEntity -> mapper.map(repaymentScheduleEntity, RepaymentScheduleResponseDTO.class));
    }

    private Mono<List<RepaymentSchedule>> printRepaymentScheduleWithDates(List<RepaymentSchedule> repaymentSchedules) {

        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        System.out.println("|  No. |       Date       |    Day    | Beginning Balance  |  Scheduled Payment |  Extra Payment |  Total Payment | Principal |  Interest  |  Ending Balance |");
        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        repaymentSchedules.stream()/*.skip(1)*/
                .forEach(item -> System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 5) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().toString()), 17) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().getDayOfWeek().toString()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getBeginPrinBalance()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getScheduledPayment()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getExtraPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getServiceCharge()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 16) + " |"));

        System.out.println("|______|__________________|___________|____________________|____________________|________________|________________|___________|____________|_________________|");
        BigDecimal totalPrincipal = repaymentSchedules.stream().skip(1).map(RepaymentSchedule::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentSchedules.stream().skip(1).map(RepaymentSchedule::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentSchedules.stream().skip(1).map(RepaymentSchedule::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");

        return Mono.just(repaymentSchedules);
    }

    private void printRepaymentScheduleWithoutDates(List<RepaymentScheduleResponseDTO> repaymentScheduleDtoList) {
        System.out.println("|-------------|---------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        System.out.println("| Payment No. |  Beginning Balance  |  Scheduled Payment |  Extra Payment |  Total Payment | Principal |  Interest  |  Ending Balance |");
        System.out.println("|-------------|---------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");

        repaymentScheduleDtoList.stream().skip(1)
                .forEach(item -> System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 12) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getBeginPrinBalance()), 20) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getScheduledPayment()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getExtraPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getServiceCharge()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 16) + " |"));

        System.out.println("|_____________|_____________________|____________________|________________|________________|___________|____________|_________________|");
        BigDecimal totalPrincipal = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");
    }

    private void printRepaymentScheduleFlatPrincipal(List<RepaymentScheduleResponseDTO> repaymentSchedulesDtoList) {
        System.out.println("|------|------------------|-----------|------------------------|------------|-----------|---------------------|");
        System.out.println("|  No. |       Date       |    Day    |  Outstanding_Principal |  Principal |  Interest |  Installment Amount |");
        System.out.println("|------|------------------|-----------|------------------------|------------|-----------|---------------------|");
        repaymentSchedulesDtoList.forEach(item ->
                System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 5) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().toString()), 17) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().getDayOfWeek().toString()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 23) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithFourDecimalPlaces(item.getServiceCharge()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 20) + " |"));
        System.out.println("|------|------------------|-----------|------------------------|------------------|------------|-----------|---------------------|");
        BigDecimal totalPrincipal = repaymentSchedulesDtoList.stream().map(RepaymentScheduleResponseDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentSchedulesDtoList.stream().map(RepaymentScheduleResponseDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = repaymentSchedulesDtoList.stream().map(RepaymentScheduleResponseDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "TOTAL PRINCIPAL");
        printWithTwoDecimalPlaces(totalInterest, "TOTAL INTEREST");
        printWithTwoDecimalPlaces(totalPaid, "TOTAL PAID");
    }

}
