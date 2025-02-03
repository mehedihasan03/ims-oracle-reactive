package net.celloscope.mraims.loanportfolio.features.dps.adapter.out.database;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.dps.adapter.out.database.entity.DPSClosureEntity;
import net.celloscope.mraims.loanportfolio.features.dps.adapter.out.database.repository.DPSClosureRepository;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.out.DPSClosurePersistencePort;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRClosureEntity;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRClosure;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Slf4j
public class DPSClosureAdapter implements DPSClosurePersistencePort {

    private final DPSClosureRepository repository;
    private final ModelMapper mapper;

    public DPSClosureAdapter(DPSClosureRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Boolean> checkIfDPSClosureExistsBySavingsAccountId(String savingsAccountId) {
        return repository.existsDPSClosureEntityBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Mono<DPSClosure> saveDPSClosure(DPSClosure dpsClosure) {
        DPSClosureEntity entity = mapper.map(dpsClosure, DPSClosureEntity.class);
        return repository
                .getDPSClosureEntityBySavingsAccountId(dpsClosure.getSavingsAccountId())
                .switchIfEmpty(Mono.just(DPSClosureEntity.builder().build()))
                .map(dpsClosureEntity -> {
                    if (dpsClosureEntity.getOid() != null) {
                        entity.setOid(dpsClosureEntity.getOid());
                    }
                    return entity;
                })
                .flatMap(repository::save)
                .doOnRequest(l -> log.info("requesting to save dpsClosure : {}", dpsClosure))
                .doOnSuccess(dpsClosureEntity -> log.info("dpsClosure successfully saved to db : {}", dpsClosureEntity))
                .map(dpsClosureEntity -> mapper.map(dpsClosureEntity, DPSClosure.class));
    }

    @Override
    public Mono<DPSClosure> getDPSClosureBySavingsAccountId(String savingsAccountId) {
        return repository
                .getDPSClosureEntityBySavingsAccountId(savingsAccountId)
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No DPS Closure Application found by Id : " + savingsAccountId)))
                .map(dpsClosureEntity -> mapper.map(dpsClosureEntity, DPSClosure.class));
    }

    @Override
    public Mono<DPSClosure> updateDPSClosureStatus(String savingsAccountId, String status, String loginId) {
        return repository
                .getDPSClosureEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account found by Id")))
                .map(dpsClosureEntity -> {
                    dpsClosureEntity.setStatus(status);
                    dpsClosureEntity.setApprovedOn(LocalDateTime.now());
                    dpsClosureEntity.setApprovedBy(loginId);
                    dpsClosureEntity.setUpdatedOn(LocalDateTime.now());
                    dpsClosureEntity.setUpdatedBy(loginId);

                    return dpsClosureEntity;
                })
                .flatMap(repository::save)
                .doOnSuccess(dpsClosureEntity -> log.info("dps closure update successful"))
                .map(dpsClosureEntity -> mapper.map(dpsClosureEntity, DPSClosure.class));
    }

    @Override
    public Flux<DPSClosure> getAllDPSClosureByOfficeId(String officeId) {
        return repository
                .getAllByOfficeId(officeId)
                .map(dpsClosureEntity -> mapper.map(dpsClosureEntity, DPSClosure.class));
    }
}
