package net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.repository.FDRRepository;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRPersistencePort;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRSchedule;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FDRPersistenceAdapter implements FDRPersistencePort {
    private final FDRRepository repository;
    private final ModelMapper modelMapper;

    public FDRPersistenceAdapter(FDRRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<List<FDRSchedule>> saveInterestPostingSchedule(List<FDRSchedule> fdrScheduleList) {
        List<FDRScheduleEntity> scheduleEntities =  fdrScheduleList
                .stream()
                .map(fdrSchedule -> modelMapper.map(fdrSchedule, FDRScheduleEntity.class))
                .toList();

        return repository
                .saveAll(scheduleEntities)
                .doOnComplete(() -> log.info("FDR Interest Posting Schedule successfully persisted to DB"))
                .collectList()
                .map(fdrScheduleEntityList -> fdrScheduleList);
    }

    @Override
    public Flux<FDRSchedule> getFDRInterestPostingSchedulesByDateAndStatus(LocalDate interestPostingDate, String status) {
        return repository
                .findAllByInterestPostingDateAndStatus(interestPostingDate, status)
                .doOnRequest(l -> log.info("request received to fetch schedule by : interestPostingDate : {} & status : {}", interestPostingDate, status))
                .map(fdrScheduleEntity -> modelMapper.map(fdrScheduleEntity, FDRSchedule.class));
    }

    @Override
    public Flux<FDRSchedule> getSchedule(String savingsAccountId) {
        return repository
                .findFDRScheduleEntityBySavingsAccountIdOrderByPostingNo(savingsAccountId)
                .map(fdrScheduleEntity -> modelMapper.map(fdrScheduleEntity, FDRSchedule.class));
    }

    @Override
    public Mono<FDRSchedule> updateScheduleStatus(String savingsAccountId, LocalDate interestPostingDate, String updatedStatus) {
        return repository
                .findBySavingsAccountIdAndInterestPostingDate(savingsAccountId, interestPostingDate)
                .map(fdrScheduleEntity -> {
                    fdrScheduleEntity.setStatus(updatedStatus);
                    return fdrScheduleEntity;
                })
                .flatMap(repository::save)
                .map(fdrScheduleEntity -> modelMapper.map(fdrScheduleEntity, FDRSchedule.class));
    }

    @Override
    public Mono<Boolean> checkIfScheduleExistsBySavingsAccountId(String savingsAccountId) {
        return repository
                .existsDistinctBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Mono<Boolean> checkIfLastInterestPosting(String savingsAccountId, Integer postingNo) {
        return repository
                .findFirstBySavingsAccountIdOrderByPostingNoDesc(savingsAccountId)
                .doOnSuccess(fdrScheduleEntity -> log.info("last posting for : {} received : {}", savingsAccountId, fdrScheduleEntity))
                .map(FDRScheduleEntity::getPostingNo)
                .map(integer -> Objects.equals(integer, postingNo));
    }
}
