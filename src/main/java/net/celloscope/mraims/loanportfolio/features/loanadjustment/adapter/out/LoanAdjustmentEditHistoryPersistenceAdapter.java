package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.repository.LoanAdjustmentEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentEditHistoryPort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentDataEditHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.swing.*;

@Component
@Slf4j
public class LoanAdjustmentEditHistoryPersistenceAdapter implements LoanAdjustmentEditHistoryPort {

    private final LoanAdjustmentEditHistoryRepository repository;
    private final ModelMapper mapper;

    public LoanAdjustmentEditHistoryPersistenceAdapter(LoanAdjustmentEditHistoryRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<LoanAdjustmentDataEditHistory> saveAdjustmentEditHistory(LoanAdjustmentDataEditHistory dataEditHistory) {
        return Mono.just(dataEditHistory)
                .map(history -> mapper.map(history, LoanAdjustmentDataEditHistoryEntity.class))
                .flatMap(repository::save)
                .map(data -> mapper.map(dataEditHistory, LoanAdjustmentDataEditHistory.class));
    }
}
