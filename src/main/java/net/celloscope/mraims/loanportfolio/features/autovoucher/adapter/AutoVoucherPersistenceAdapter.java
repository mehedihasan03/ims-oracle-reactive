package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherPersistenceHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out.AutoVoucherPersistencePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherEnum;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class AutoVoucherPersistenceAdapter implements AutoVoucherPersistencePort {

    private final AutoVoucherPersistenceRepository repository;
    private final AutoVoucherPersistenceHistoryRepository historyRepository;
    private final ModelMapper modelMapper;
    private final TransactionalOperator rxtx;

    public AutoVoucherPersistenceAdapter(AutoVoucherPersistenceRepository repository, AutoVoucherPersistenceHistoryRepository historyRepository, ModelMapper modelMapper, TransactionalOperator rxtx) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
    }

    @Override
    public Mono<AutoVoucher> saveAutoVoucher(AutoVoucher autoVoucher) {
        return repository.save(modelMapper.map(autoVoucher, AutoVoucherEntity.class))
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class))
                .as(rxtx::transactional)
                .doOnSuccess(autoVoucherEntity -> log.info("Auto voucher saved successfully : {}", autoVoucherEntity))
                .doOnError(throwable -> log.error("Error occurred while saving auto voucher : {}", throwable.getMessage()))
                ;
    }

    @Override
    public Flux<Tuple2<String, AutoVoucher>> saveAutoVoucherList(List<AutoVoucher> autoVouchers) {
        return Mono.just(autoVouchers)
                .filter(autoVoucherList -> !autoVoucherList.isEmpty())
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucher -> modelMapper.map(autoVoucher, AutoVoucherEntity.class))
                .filter(autoVoucherEntity -> autoVoucherEntity.getVoucherAmount().compareTo(BigDecimal.ZERO) > 0)
                .flatMap(repository::save)
                .doOnRequest(l -> log.info("Requesting to save auto vouchers with size : {}", autoVouchers.size()))
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers saved successfully with size : {}", autoVoucherEntityList.size()))
                .doOnError(throwable -> log.error("Error occurred while saving auto vouchers : {}", throwable.getMessage()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherEntity -> Tuples.of(autoVoucherEntity.getOid(), modelMapper.map(autoVoucherEntity, AutoVoucher.class)));
    }

    @Override
    public Mono<String> saveAutoVoucherListIntoHistory(List<AutoVoucher> autoVouchers) {
        return Mono.just(autoVouchers)
                .filter(autoVoucherList -> !autoVoucherList.isEmpty())
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucher -> {
                    AutoVoucherHistoryEntity autoVoucherHistoryEntity = modelMapper.map(autoVoucher, AutoVoucherHistoryEntity.class);
                    autoVoucherHistoryEntity.setAutoVoucherOid(autoVoucher.getOid());
                    autoVoucherHistoryEntity.setOid(null);
                    return autoVoucherHistoryEntity;
                })
                .flatMap(historyRepository::save)
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers saved successfully into history with size : {}", autoVoucherEntityList.size()))
                .doOnError(throwable -> log.error("Error occurred while saving auto vouchers into history: {}", throwable.getMessage()))
                .then(Mono.just("Auto vouchers saved successfully into history"))
                ;
    }

    @Override
    public Flux<AutoVoucher> getAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return repository
                .findAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnRequest(l -> log.info("Requesting to get auto vouchers with managementProcessId : {} and processId : {}", managementProcessId, processId))
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers fetched successfully with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class))
                .doOnError(throwable -> log.error("Error occurred while fetching auto vouchers : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return repository
                .deleteAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnRequest(l -> log.info("Requesting to delete auto vouchers with managementProcessId : {} and processId : {}", managementProcessId, processId))
                .doOnSuccess(aBoolean -> log.info("Auto vouchers deleted successfully with managementProcessId : {} and processId : {}", managementProcessId, processId));
    }

    @Override
    public Mono<Boolean> deleteAutoVoucherListByManagementProcessId(String managementProcessId) {
        return repository
                .deleteAllByManagementProcessId(managementProcessId)
                .doOnRequest(l -> log.info("Requesting to delete auto vouchers with managementProcessId : {}", managementProcessId))
                .doOnSuccess(aBoolean -> log.info("Auto vouchers deleted successfully with managementProcessId : {}", managementProcessId));
    }

    @Override
    public Flux<AutoVoucher> updateAutoVoucherStatus(String managementProcessId, String processId, String loginId, String status) {
        return repository.findAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnRequest(l -> log.info("Requesting to fetch auto vouchers status with managementProcessId : {} and processId : {}", managementProcessId, processId))
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers fetched successfully with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherEntity -> {
                    autoVoucherEntity.setStatus(status);
                    autoVoucherEntity.setUpdatedBy(loginId);
                    autoVoucherEntity.setUpdatedOn(LocalDateTime.now());
                    return autoVoucherEntity;
                })
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers status updated. Requesting to save to db with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(repository::saveAll)
                .doOnRequest(l -> log.info("Requesting to update auto vouchers status with managementProcessId : {} and processId : {}", managementProcessId, processId))
                .doOnComplete(() -> log.info("Auto vouchers status updated successfully with managementProcessId : {} and processId : {}", managementProcessId, processId))
                .doOnError(throwable -> log.error("Error occurred while updating auto voucher : {}", throwable.getMessage()))
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class));
    }

    @Override
    public Flux<AutoVoucher> updateAutoVoucherStatus(String managementProcessId, String loginId, String status) {
        return repository.findAllByManagementProcessId(managementProcessId)
                .doOnRequest(l -> log.info("Requesting to fetch auto vouchers status with managementProcessId : {}", managementProcessId))
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers fetched successfully with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherEntity -> {
                    autoVoucherEntity.setStatus(status);
                    autoVoucherEntity.setUpdatedBy(loginId);
                    autoVoucherEntity.setUpdatedOn(LocalDateTime.now());
                    return autoVoucherEntity;
                })
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto vouchers status updated. Requesting to save to db with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(repository::saveAll)
                .doOnRequest(l -> log.info("Requesting to update auto vouchers status with managementProcessId : {}", managementProcessId))
                .doOnComplete(() -> log.info("Auto vouchers status updated successfully with managementProcessId : {}", managementProcessId))
                .doOnError(throwable -> log.error("Error occurred while updating auto voucher : {}", throwable.getMessage()))
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class));
    }

    @Override
    public Mono<AutoVoucherEntity> updateAutoVoucherWithAisRequest(String oid, String aisRequest) {
        return repository.findByOid(oid)
                .map(autoVoucherEntity -> {
                    autoVoucherEntity.setAisRequest(aisRequest);
                    return autoVoucherEntity;
                })
                .flatMap(repository::save);
    }

    @Override
    public Flux<AutoVoucher> getAutoVoucherListByManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessId(managementProcessId)
                .doOnRequest(l -> log.info("Requesting to get auto vouchers with managementProcessId : {} ", managementProcessId))
                .doOnNext(autoVoucherEntity -> log.info("Auto vouchers fetched successfully : {}", autoVoucherEntity))
                .map(autoVoucherEntity -> modelMapper.map(autoVoucherEntity, AutoVoucher.class))
                .doOnComplete(() -> log.info("Auto vouchers fetched successfully with managementProcessId : {}", managementProcessId))
                .doOnError(throwable -> log.error("Error occurred while fetching auto vouchers : {}", throwable.getMessage()));
    }
}
