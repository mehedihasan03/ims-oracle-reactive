package net.celloscope.mraims.loanportfolio.features.rebate.adapter;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebateDataEditHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanInfo;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.RebatePaymentInfo;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;
@Slf4j
@Component
public class LoanRebateEditHistoryAdapter implements LoanRebateDataEditHistoryPersistencePort {

    private final LoanRebateEditHistoryRepository loanRebateEditHistoryRepository;
    private final ModelMapper modelMapper;

    public LoanRebateEditHistoryAdapter(LoanRebateEditHistoryRepository loanRebateEditHistoryRepository, ModelMapper modelMapper) {
        this.loanRebateEditHistoryRepository = loanRebateEditHistoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<LoanRebate> saveLoanRebateEditHistory(LoanRebate loanRebate) {
        return Mono.just(loanRebate)
                .map(domain -> {
                    modelMapper.getConfiguration()
                            .setSkipNullEnabled(true)
                            .setMatchingStrategy(MatchingStrategies.STRICT);
                    LoanRebateEditHistoryEntity entity = modelMapper.map(domain, LoanRebateEditHistoryEntity.class);
                    entity.setLoanInfo(new Gson().toJson(domain.getLoanInfo()));
                    log.info("Mapped Loan Rebate Domain to Rebate History Entity before saving into DB: {}", entity);
                    return entity;
                })
                .flatMap(loanRebateHistoryEntity -> {
                    log.info("Requested Loan Rebate History Entity for saving into DB: {}", loanRebateHistoryEntity);
                    return loanRebateEditHistoryRepository.save(loanRebateHistoryEntity)
                            .doOnSuccess(res -> log.info("Successfully saved loan rebate history into DB: {}", res));
                })
                .doOnRequest(req -> log.info("Request received for saving loan rebate history into DB: {}", loanRebate))
                .map(entity -> modelMapper.map(entity, LoanRebate.class))
                .doOnSuccess(res -> log.info("Mapped LoanRebateEditHistoryEntity to LoanRebate after saving into DB: {}", res))
                .doOnError(e -> log.error("Error occurred while saving loan rebate history into DB: {}", e.getMessage()))
                .onErrorMap(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()))
                .onErrorMap(ExceptionHandlerUtil.class, e -> new ExceptionHandlerUtil(e.getCode(), e.getMessage()));
    }

    @Override
    public Mono<LoanRebate> getLastLoanRebateEditHistoryByLoanRebateDataId(String loanRebateDataOid) {
        return loanRebateEditHistoryRepository.findTopByLoanRebateDataIdOrderByCreatedOnDesc(loanRebateDataOid)
                .doOnRequest(req -> log.info("Request received for fetching last loan rebate history from DB for loanRebateDataOid: {}", loanRebateDataOid))
                .doOnSuccess(res -> log.info("Successfully fetched last loan rebate history from DB: {}", res))
                .map(entity -> {
                    LoanRebate rebate = modelMapper.map(entity, LoanRebate.class);
                    rebate.setLoanInfo(new Gson().fromJson(entity.getLoanInfo(), LoanInfo.class));
                    rebate.setPaymentInfo(CommonFunctions.buildGson(this).fromJson(entity.getPaymentInfo(), RebatePaymentInfo.class));
                    return rebate;
                })
                .doOnSuccess(res -> log.info("Mapped LoanRebateEditHistoryEntity to LoanRebate after fetching from DB: {}", res))
                .doOnError(e -> log.error("Error occurred while fetching last loan rebate history from DB: {}", e.getMessage()))
                .onErrorMap(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
