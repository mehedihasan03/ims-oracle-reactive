package net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence.entity.SavingsClosureEntity;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence.repository.SavingsClosureRepository;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.out.SavingsClosurePort;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.domain.SavingsClosure;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SavingsClosureAdapter implements SavingsClosurePort {

    private final SavingsClosureRepository repository;
    private final ModelMapper mapper;

    public SavingsClosureAdapter(SavingsClosureRepository repository,
                                 ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<SavingsClosure> getSavingsClosureBySavingsAccountId(String savingsAccountId) {
        return repository.getSavingsClosureEntityBySavingsAccountId(savingsAccountId)
                .map(savingsClosureEntity -> mapper.map(savingsClosureEntity, SavingsClosure.class))
                .doOnNext(savingsClosure -> log.info("SavingsClosure fetched from db : {}", savingsClosure))
                .doOnRequest(l -> log.info("requesting to fetch SavingsClosure by savingsAccountId : {}", savingsAccountId));
    }

    @Override
    public Mono<SavingsClosure> saveSavingsClosure(SavingsClosure savingsClosure) {
        return repository.getSavingsClosureEntityBySavingsAccountId(savingsClosure.getSavingsAccountId())
                .switchIfEmpty(Mono.just(SavingsClosureEntity.builder().build()))
                .map(savingsClosureEntity -> {
                    if (savingsClosureEntity.getOid() != null) {
                        savingsClosure.setOid(savingsClosureEntity.getOid());
                    }
                    return mapper.map(savingsClosure, SavingsClosureEntity.class);
                })
                .flatMap(repository::save)
                .doOnRequest(l -> log.info("requesting to save SavingsClosure : {}", savingsClosure))
                .doOnSuccess(savingsClosureEntity -> log.info("SavingsClosure successfully saved to db : {}", savingsClosureEntity))
                .map(savingsClosureEntity -> mapper.map(savingsClosureEntity, SavingsClosure.class));
    }

    @Override
    public Mono<Boolean> checkIfSavingsClosureExistsBySavingsAccountId(String savingsAccountId) {
        return repository.existsSavingsClosureEntityBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Mono<SavingsClosure> updateSavingsClosureStatus(String savingsAccountId, String status, String loginId) {
        return repository.getSavingsClosureEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Savings Closure Application found by Id : " + savingsAccountId)))
                .map(savingsClosureEntity -> {
                    savingsClosureEntity.setStatus(status);
                    savingsClosureEntity.setApprovedOn(LocalDateTime.now());
                    savingsClosureEntity.setApprovedBy(loginId);
                    savingsClosureEntity.setUpdatedOn(LocalDateTime.now());
                    savingsClosureEntity.setUpdatedBy(loginId);
                    return savingsClosureEntity;
                })
                .flatMap(repository::save)
                .doOnSuccess(savingsClosureEntity -> log.info("SavingsClosure status updated to : {}", status))
                .map(savingsClosureEntity -> mapper.map(savingsClosureEntity, SavingsClosure.class));
    }
}
