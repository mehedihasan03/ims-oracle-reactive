package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsAccountInterestDepositEntity;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsAccountInterestDepositHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.repository.AccruedInterestRepository;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.repository.SavingsAccountInterestDepositHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.out.AccruedInterestPort;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterestDTODomain;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class AccruedInterestAdapter implements AccruedInterestPort {

    private final AccruedInterestRepository repository;
    private final SavingsAccountInterestDepositHistoryRepository historyRepository;
    private final ModelMapper mapper;

    public AccruedInterestAdapter(AccruedInterestRepository repository, SavingsAccountInterestDepositHistoryRepository historyRepository, ModelMapper mapper) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccruedInterestDTODomain> saveAccruedInterest(SavingsAccruedInterestResponseDTO savingsAccruedInterestResponseDTO, AccruedInterestCommand command) {
        SavingsAccountInterestDepositEntity entity = mapper.map(savingsAccruedInterestResponseDTO, SavingsAccountInterestDepositEntity.class);
        entity.setCreatedBy(command.getLoginId());
        entity.setCreatedOn(LocalDateTime.now());
//        entity.setManagementProcessId(command.getManagementProcessId());
        entity.setAccruedInterestId(UUID.randomUUID().toString());
        entity.setStatus(Status.STATUS_PENDING.getValue());
        entity.setProductId(savingsAccruedInterestResponseDTO.getSavingsProductId());

        return repository
                .save(entity)
                .doOnRequest(l -> log.info("request received to save entity to db : {}", entity))
                .doOnSuccess(entity1 -> log.info("successfully saved to db : {}", entity1))
                .map(entity1 -> AccruedInterestDTODomain
                        .builder()
                        .memberId(entity.getMemberId())
//                        .mfiId(entity1.getMfiId())
                        .savingsAccountId(entity1.getSavingsAccountId())
                        .interestCalculationMonth(entity1.getInterestCalculationMonth())
                        .accruedInterestAmount(entity1.getAccruedInterestAmount())
//                        .interestCalculationYear(entity1.getInterestCalculationYear())
                        .fromDate(entity1.getFromDate())
                        .toDate(entity1.getToDate())
                        .build());
    }

    @Override
    public Mono<String> saveAllSavingsAccountInterestDepositsIntoHistory(List<SavingsAccountInterestDeposit> savingsAccountInterestDepositList) {
        return historyRepository
                .saveAll(savingsAccountInterestDepositList
                        .stream()
                        .map(savingsAccountInterestDeposit -> mapper.map(savingsAccountInterestDeposit, SavingsAccountInterestDepositHistoryEntity.class))
                        .toList()
                )
                .doOnRequest(l -> log.info("request received to save all SavingsAccountInterestDeposit to history"))
                .doOnNext(entityList -> log.info("SavingsAccountInterestDeposit saved to history : {}", entityList))
                .collectList()
                .map(entityList -> "Successfully saved all SavingsAccountInterestDeposit to history")
                .onErrorReturn("Error occurred while saving all SavingsAccountInterestDeposit to history");
    }

    @Override
    public Mono<Boolean> checkIfExistsByYearMonthAndSavingsAccountId(Integer year, Integer month, String savingsAccountId) {
        return repository.checkIfExistsByYearMonthAndSavingsAccountId(year, month, savingsAccountId);
    }

    @Override
    public Flux<SavingsAccruedInterestResponseDTO> getAccruedInterestEntriesBySavingsAccountIdYearAndMonthListAndStatus(String savingsAccountId, Integer year, List<String> monthList, String status) {
        /*return repository
                .findAllBySavingsAccountIdAndInterestCalculationYearAndInterestCalculationMonthInAndStatus(savingsAccountId, year, monthList, status)
                .doOnRequest(l -> log.info("request received to get entities by savingsAccountId : {}, year : {}, monthList : {}, status : {}", savingsAccountId, year, monthList, status))
                .map(entity -> mapper.map(entity, SavingsAccruedInterestResponseDTO.class));*/
        return null;
    }

    @Override
    public Mono<Boolean> updateTransactionIdAndStatusByAccruedInterestIdList(List<String> accruedInterestIdList, String transactionId, String status) {
        return repository
                .findAllByAccruedInterestIdIn(accruedInterestIdList)
                .doOnRequest(l -> log.info("request received to find by accruedInterestIdList"))
                .map(entity -> {
                    entity.setStatus(status);
                    entity.setTransactionId(transactionId);
                    return entity;
                })
                .collectList()
                .doOnNext(savingsAccountInterestDepositEntities -> log.info("received + updated entities : {}", savingsAccountInterestDepositEntities))
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> true)
                .onErrorReturn(false);
    }

    @Override
    public Mono<SavingsAccountInterestDeposit> saveAccruedInterestV2(SavingsAccountInterestDeposit savingsAccountInterestDeposit) {
        SavingsAccountInterestDepositEntity entity = mapper.map(savingsAccountInterestDeposit, SavingsAccountInterestDepositEntity.class);
        log.info("entity to be saved : {}", entity);
        return repository
                .save(entity)
                .map(savingsAccountInterestDepositEntity -> mapper.map(savingsAccountInterestDepositEntity, SavingsAccountInterestDeposit.class));
    }

    @Override
    public Flux<AccruedInterest> getAccruedInterestEntriesByManagementProcessIdAndOfficeId(String managementProcessId, String officeId) {
        return repository
                .getAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .doOnRequest(l -> log.info("request received in repository . managementProcessId : {} , officeId : {}", managementProcessId, officeId))
                .map(savingsAccountInterestDepositEntity -> mapper.map(savingsAccountInterestDepositEntity, AccruedInterest.class));
    }

    @Override
    public Mono<Integer> deleteIfAlreadyAccrued(String managementProcessId, String samityId) {
        AtomicReference<Integer> totalCount = new AtomicReference<>(0);
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .doOnNext(entityList -> totalCount.set(entityList.size()))
                .flatMap(entityList -> {
                    if(totalCount.get() == 0) {
                        log.info("No Savings Interest Entity Found for Management Process Id: {} and Samity Id: {}", managementProcessId, samityId);
                        return Mono.just(totalCount.get());
                    }
                    log.info("Savings Interest Entity Found for Samity Id: {}, Total Entry: {}", samityId, totalCount.get());
                    return repository.deleteAll(entityList)
                            .then(Mono.just(totalCount.get()));
                });
    }

    @Override
    public Mono<String> deleteAllSavingsAccountInterestDepositByManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessId(managementProcessId)
                .collectList()
                .flatMap(repository::deleteAll)
                .then(Mono.just("Successfully deleted all SavingsAccountInterestDeposit by managementProcessId : " + managementProcessId))
                ;
    }

    @Override
    public Flux<SavingsAccountInterestDeposit> findAllSavingsAccountInterestDepositsForManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessId(managementProcessId)
                .doOnRequest(l -> log.info("request received to find all by managementProcessId : {}", managementProcessId))
                .map(entity -> mapper.map(entity, SavingsAccountInterestDeposit.class));
    }
}
