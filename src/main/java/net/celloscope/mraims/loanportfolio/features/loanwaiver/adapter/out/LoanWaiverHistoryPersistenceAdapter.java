package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository.LoanWaiverHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository.LoanWaiverRepository;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanAccountDetails;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistenceHistoryPort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoanWaiverHistoryPersistenceAdapter implements LoanWaiverPersistenceHistoryPort {

        private final LoanWaiverHistoryRepository repository;
        private final ModelMapper modelMapper;
        private final TransactionalOperator rxtx;
        private final Gson gson;


    public LoanWaiverHistoryPersistenceAdapter(LoanWaiverHistoryRepository repository, ModelMapper modelMapper, TransactionalOperator rxtx) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<LoanWaiver> saveLoanWaiverHistory(LoanWaiver loanWaiver) {
            return repository.save(convertToEntity(loanWaiver))
                    .map(loanWaiverEntity -> modelMapper.map(loanWaiverEntity, LoanWaiver.class))
                    .as(rxtx::transactional)
                    .doOnError(throwable -> log.error("Exception encountered in saveLoanWaiverHistory\nReason - {}", throwable.getMessage()))
                    .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while saving loan waiver data"))
                    .doOnNext(dto -> log.info("after map loan waiver dto : {}", dto));
    }

    @Override
    public Mono<LoanWaiver> getLoanWaiverHistoryById(String loanWaiverOid) {
        return repository.findFirstByLoanWaiverDataOidOrderByCreatedOnDesc(loanWaiverOid)
                .doOnNext(entity -> log.info("loan waiver history entity by id : {} {}", entity, loanWaiverOid))
                .map(loanWaiverEntity -> {
                    LoanWaiver loanWaiver = modelMapper.map(loanWaiverEntity, LoanWaiver.class);
                    LoanAccountDetails loanAccountDetails = gson.fromJson(loanWaiverEntity.getLoanInfo(), LoanAccountDetails.class);
                    loanWaiver.setLoanInfo(loanAccountDetails);
                    loanWaiver.setOid(loanWaiverEntity.getLoanWaiverDataOid());
                    return loanWaiver;
                })
                .doOnNext(dto -> log.info("after map loan waiver history by id : {}", dto));
    }

    private LoanWaiverHistoryEntity convertToEntity(LoanWaiver loanWaiver) {
        LoanWaiverHistoryEntity historyEntity = modelMapper.map(loanWaiver, LoanWaiverHistoryEntity.class);
        historyEntity.setOid(null);
        historyEntity.setLoanWaiverDataOid(loanWaiver.getOid());
        return historyEntity;
    }

}
