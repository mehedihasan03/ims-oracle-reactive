package net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRClosureEntity;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.repository.FDRClosureRepository;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRClosurePersistencePort;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRClosure;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
public class FDRClosurePersistenceAdapter implements FDRClosurePersistencePort {

    private final FDRClosureRepository repository;
    private final ModelMapper mapper;

    public FDRClosurePersistenceAdapter(FDRClosureRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<FDRClosure> saveFDRClosure(FDRClosure fdrClosure) {
        FDRClosureEntity entity = mapper.map(fdrClosure, FDRClosureEntity.class);
        return repository
                .getFDRClosureEntityBySavingsAccountId(fdrClosure.getSavingsAccountId())
                .switchIfEmpty(Mono.just(FDRClosureEntity.builder().build()))
                        .map(fdrClosureEntity -> {
                            if (fdrClosureEntity.getOid() != null) {
                                entity.setOid(fdrClosureEntity.getOid());
                            }
                            return entity;
                        })
                .flatMap(repository::save)
                .doOnSuccess(fdrClosureEntity -> log.info("successfully saved to db : {}", fdrClosureEntity))
                .map(fdrClosureEntity -> mapper.map(fdrClosureEntity, FDRClosure.class));
    }

    @Override
    public Mono<FDRClosure> getFDRClosureBySavingsAccountId(String savingsAccountId) {
        return repository
                .getFDRClosureEntityBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("fetching fdr closure by savings account id : {}", savingsAccountId))
                /*.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No FDR Closure Application found by Id : " + savingsAccountId)))
                .doOnError(throwable -> log.error("error occurred while fetching fdr closure by savings account id : {} | {}", savingsAccountId, throwable.getMessage()))*/
                .map(fdrClosureEntity -> mapper.map(fdrClosureEntity, FDRClosure.class));
    }

    @Override
    public Mono<FDRClosure> updateFDRClosureStatus(String savingsAccountId, String status, String loginId) {
        return repository
                .getFDRClosureEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account found by Id")))
                .map(fdrClosureEntity -> {
                    fdrClosureEntity.setStatus(status);
                    fdrClosureEntity.setApprovedOn(LocalDateTime.now());
                    fdrClosureEntity.setApprovedBy(loginId);
                    fdrClosureEntity.setUpdatedOn(LocalDateTime.now());
                    fdrClosureEntity.setUpdatedBy(loginId);

                    return fdrClosureEntity;
                })
                .flatMap(repository::save)
                .doOnSuccess(fdrClosureEntity -> log.info("fdr closure update successful"))
                .map(fdrClosureEntity -> mapper.map(fdrClosureEntity, FDRClosure.class));
    }

    @Override
    public Mono<Boolean> checkIfFDRClosureExistsBySavingsAccountId(String savingsAccountId) {
        return repository
                .existsFDRClosureEntityBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Flux<FDRClosure> getAllFDRClosureByOfficeId(String officeId) {
        return repository
                .findAllByOfficeId(officeId)
                .map(fdrClosureEntity -> mapper.map(fdrClosureEntity, FDRClosure.class));
    }
}
