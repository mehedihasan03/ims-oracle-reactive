package net.celloscope.mraims.loanportfolio.features.withdraw.adapter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity.WithdrawEntity;
import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.repository.WithdrawRepository;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out.WithdrawPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdraw.domain.Withdraw;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class WithdrawPersistenceAdapter implements WithdrawPersistencePort {
    private final WithdrawRepository repository;
    private final ModelMapper modelMapper;
    public WithdrawPersistenceAdapter(WithdrawRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Flux<Withdraw> saveWithdrawStagingData(List<Withdraw> withdrawList, String managementProcessId, String samityEventTrackerId) {

        List<WithdrawEntity> entityList = withdrawList.stream()
                .map(withdraw -> {
                    WithdrawEntity withdrawEntity = modelMapper.map(withdraw, WithdrawEntity.class);
                    withdrawEntity.setManagementProcessId(managementProcessId);
                    withdrawEntity.setProcessId(samityEventTrackerId);
                    return withdrawEntity;
                })
                .toList();

        return repository
                .saveAll(entityList)
                .map(withdrawEntity -> modelMapper.map(withdrawEntity, Withdraw.class));
    }

    @Override
    public Mono<Integer> updateAllWithdrawStagedDataBy(String samityId, String withdrawType, String approvedBy) {
        return repository
                .findAllBySamityIdAndWithdrawTypeAndStatus(samityId, withdrawType, Status.STATUS_STAGED.getValue())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, ExceptionMessages.NO_WITHDRAW_DATA_FOUND.getValue())))
                .doOnRequest(r -> log.info("Request received for getting withdraw data to database for samityId: {}, withdrawType: {}", samityId, withdrawType))
                .doOnError(e -> log.error("Error occurred during getting authorization withdraw data to database: {}", e.getMessage()))
                .doOnComplete(() -> log.info("Successfully got withdraw data in database"))
                .map(withdrawEntity -> {
                    withdrawEntity.setStatus(Status.STATUS_APPROVED.getValue());
                    withdrawEntity.setApprovedBy(approvedBy);
                    withdrawEntity.setApprovedOn(LocalDateTime.now());
                    return withdrawEntity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnRequest(r -> log.info("Request received for updating authorization withdraw data to database"))
                .doOnError(e -> log.error("Error occurred during updating authorization withdraw data to database: {}", e.getMessage()))
                .doOnSuccess(s -> log.info("Successfully updated authorization withdraw data in database {}", s.size()))
                .map(List::size);
    }

    @Override
    public Flux<Withdraw> getWithdrawStagedDataByStagingDataId(String stagingDataId) {
        return repository
                .findAllByStagingDataId(stagingDataId)
                .map(withdrawEntity -> modelMapper.map(withdrawEntity, Withdraw.class));
    }

    @Override
    public Mono<List<WithdrawEntity>> getAllWithdrawStagingDataByManagementProcessId(String managementProcessId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<String> deleteAllWithdrawStagingDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .then(Mono.just("Withdraw Staging Data Deleted Successfully"));
    }

    @Override
    public Mono<Withdraw> getWithdrawData(String oid) {
        return repository.findFirstByOid(oid)
                .doOnRequest(r -> log.info("Request received for getting withdraw data to database for oid: {}", oid))
                .doOnError(e -> log.error("Error occurred during getting withdraw data to database: {}", e.getMessage()))
                .doOnSuccess(s -> log.info("Successfully got withdraw data in database by oid"))
                .map(withdrawEntity -> modelMapper.map(withdrawEntity, Withdraw.class));
    }

    @Override
    public Mono<Withdraw> updateWithdrawAmount(Withdraw withdrawData) {
        WithdrawEntity entity = modelMapper.map(withdrawData, WithdrawEntity.class);
        entity.setStagingWithdrawDataId(UUID.randomUUID().toString());
        return modelMapper.map(withdrawData, WithdrawEntity.class).isNew()
                ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.WITHDRAW_DATA_NOT_FOUND.getValue()))
                : repository.save(entity)
                        .map(withdrawEntity -> modelMapper.map(withdrawEntity, Withdraw.class));
    }
}
