package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebateHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanInfo;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.RebatePaymentInfo;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;


@Component
@Slf4j
public class LoanRebateHistoryPersistenceAdapter implements LoanRebateHistoryPersistencePort {

    private final LoanRebateHistoryRepository loanRebateHistoryRepository;
    private final ModelMapper modelMapper;

    public LoanRebateHistoryPersistenceAdapter(LoanRebateHistoryRepository loanRebateHistoryRepository, ModelMapper modelMapper) {
        this.loanRebateHistoryRepository = loanRebateHistoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<LoanRebate> saveLoanRebateHistory(LoanRebate loanRebate) {
        return Mono.just(loanRebate)
                .map(domain -> {
                    modelMapper.getConfiguration()
                            .setSkipNullEnabled(true)
                            .setMatchingStrategy(MatchingStrategies.STRICT);
                    LoanRebateHistoryEntity entity = modelMapper.map(domain, LoanRebateHistoryEntity.class);
                    entity.setLoanInfo(new Gson().toJson(domain.getLoanInfo()));
                    log.info("Mapped Loan Rebate Domain to Rebate History Entity before saving into DB: {}", entity);
                    return entity;
                })
                .flatMap(loanRebateHistoryEntity -> {
                    log.info("Requested Loan Rebate History Entity for saving into DB: {}", loanRebateHistoryEntity);
                    return loanRebateHistoryRepository.save(loanRebateHistoryEntity)
                            .doOnSuccess(res -> log.info("Successfully saved loan rebate history into DB: {}", res));
                })
                .doOnRequest(req -> log.info("Request received for saving loan rebate history into DB: {}", loanRebate))
                .map(entity -> modelMapper.map(entity, LoanRebate.class))
                .doOnSuccess(res -> log.info("Mapped LoanRebateHistoryEntity to LoanRebate after saving into DB: {}", res))
                .doOnError(e -> log.error("Error occurred while saving loan rebate history into DB: {}", e.getMessage()))
                .onErrorMap(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()))
                .onErrorMap(ExceptionHandlerUtil.class, e -> new ExceptionHandlerUtil(e.getCode(), e.getMessage()));
    }

    @Override
    public Mono<LoanRebate> getLastLoanRebateHistoryByLoanRebateDataOid(String loanRebateDataOid) {
        return loanRebateHistoryRepository.findTopByLoanRebateDataOidOrderByCreatedOnDesc(loanRebateDataOid)
                .doOnRequest(req -> log.info("Request received for fetching last loan rebate history from DB for loanRebateDataOid: {}", loanRebateDataOid))
                .doOnSuccess(res -> log.info("Successfully fetched last loan rebate history from DB: {}", res))
                .map(entity -> {
                    LoanRebate rebate = modelMapper.map(entity, LoanRebate.class);
                    rebate.setLoanInfo(new Gson().fromJson(entity.getLoanInfo(), LoanInfo.class));
                    rebate.setPaymentInfo(new Gson().fromJson(entity.getPaymentInfo(), RebatePaymentInfo.class));
                    return rebate;
                })
                .doOnSuccess(res -> log.info("Mapped LoanRebateHistoryEntity to LoanRebate after fetching from DB: {}", res))
                .doOnError(e -> log.error("Error occurred while fetching last loan rebate history from DB: {}", e.getMessage()))
                .onErrorMap(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
