package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity.WelfareFundDataEntity;
import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.repository.WelfareFundDataRepository;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.out.WelfareFundPersistencePort;
import net.celloscope.mraims.loanportfolio.features.welfarefund.domain.WelfareFund;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelfareFundPersistenceAdapter implements WelfareFundPersistencePort {

    private final WelfareFundDataRepository fundDataRepository;
    private final ModelMapper mapper;

    @Override
    public Flux<WelfareFund> getWelfareFundByOfficeId(String officeId, long limit, long offset) {
        return fundDataRepository.getWelfareFundDataEntitiesByOfficeIdFilterByStatus(officeId, limit, offset, Status.STATUS_REJECTED.getValue())
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> getWelfareFundByBusinessDate(String loanAccountId, String officeId, LocalDate transactionDate) {
        log.info("Loan Account Id: {}, Transaction Date: {}", loanAccountId, transactionDate);
        return fundDataRepository.findFirstByLoanAccountIdAndOfficeIdAndTransactionDate(loanAccountId, officeId, transactionDate)
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> getWelfareFundByLoanAccountIdAndTransactionDate(String loanAccountId, LocalDate transactionDate) {
        return fundDataRepository.findFirstByLoanAccountIdAndTransactionDate(loanAccountId, transactionDate)
                .doOnRequest(l -> log.info("request for welfare data : {} {}", loanAccountId, transactionDate))
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> saveCollectedFundData(WelfareFund welfareFund) {
        return Mono.just(welfareFund)
                .map(welfareFundData -> mapper.map(welfareFundData, WelfareFundDataEntity.class))
                .flatMap(fundDataRepository::save)
                .map(entity -> mapper.map(entity, WelfareFund.class))
                .doOnRequest(value -> log.info("Save Request Received: {}", value))
                .doOnSubscribe(subscription -> log.info("subscribe for save to Db"))
                .doOnSuccess(entity -> log.info("successfully saved to DB: {}", entity))
                .doOnError(err -> log.info("Found error while saving MFI to DB : {}", err.getLocalizedMessage()));
    }

    @Override
    public Flux<WelfareFund> getWelfareFundByLoanAccountId(String loanAccountId) {
        return fundDataRepository.findAllByLoanAccountIdOrderByTransactionDate(loanAccountId)
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> authorizeWelfareFundData(String loanAccountId, LocalDate parse, String loginId) {
        return fundDataRepository.findFirstByLoanAccountIdAndTransactionDate(loanAccountId, parse)
                .map(welfareFundDataEntity -> {
                    welfareFundDataEntity.setStatus(Status.STATUS_APPROVED.getValue());
                    welfareFundDataEntity.setApprovedBy(loginId);
                    welfareFundDataEntity.setApprovedOn(LocalDateTime.now());
                    return welfareFundDataEntity;
                })
                .flatMap(fundDataRepository::save)
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> rejectWelfareFundData(String loanAccountId, LocalDate parse, String loginId) {
        return fundDataRepository.findFirstByLoanAccountIdAndTransactionDate(loanAccountId, parse)
                .map(welfareFundDataEntity -> {
                    welfareFundDataEntity.setStatus(Status.STATUS_REJECTED.getValue());
                    welfareFundDataEntity.setRejectedBy(loginId);
                    welfareFundDataEntity.setRejectedOn(LocalDateTime.now());
                    return welfareFundDataEntity;
                })
                .flatMap(fundDataRepository::save)
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Flux<WelfareFund> getPendingWelfareFundByOfficeId(String officeId) {
        return fundDataRepository.findAllByOfficeIdAndStatusOrderByTransactionDate(officeId, Status.STATUS_PENDING.getValue())
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Flux<WelfareFund> getAllWelfareFundTransactionForOfficeOnABusinessDay(String managementProcessId, String officeId) {
        return fundDataRepository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<WelfareFund> getWelfareFundByOid(String id) {
        return fundDataRepository.findById(id)
                .doOnNext(welfareFundDataEntity -> log.info("Welfare Fund Data Entity: {}", welfareFundDataEntity))
                .map(welfareFundDataEntity -> mapper.map(welfareFundDataEntity, WelfareFund.class));
    }

    @Override
    public Mono<String> deleteWelfareFundData(String id) {
        return fundDataRepository.deleteById(id)
                .then(Mono.just("Welfare Fund Data Deleted Successfully"));
    }
}
