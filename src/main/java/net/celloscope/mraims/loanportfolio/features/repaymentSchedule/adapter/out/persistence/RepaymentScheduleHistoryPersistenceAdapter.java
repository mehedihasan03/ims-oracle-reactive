package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.RepaymentScheduleHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepaymentScheduleHistoryPersistenceAdapter implements RepaymentScheduleHistoryPersistencePort {
    private final RepaymentScheduleHistoryRepository repaymentScheduleHistoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Mono<List<RepaymentSchedule>> saveRepaymentScheduleHistory(List<RepaymentSchedule> repaymentSchedule) {
        return Flux.fromIterable(repaymentSchedule)
                .doOnRequest(n -> log.info("Request Received for Saving Repayment Schedule History with size: {}", repaymentSchedule.size()))
                .map(repaymentScheduleDomain -> modelMapper.map(repaymentScheduleDomain, RepaymentScheduleHistoryEntity.class))
//                .doOnNext(repaymentScheduleHistoryEntity -> log.info("Repayment Schedule History to be saved: {}", repaymentScheduleHistoryEntity))
                .collectList()
                .flatMapMany(repaymentScheduleHistoryRepository::saveAll)
//                .doOnNext(repaymentScheduleEntities -> log.info("Repayment Schedule History saved successfully: {}", repaymentScheduleEntities))
                .map(repaymentScheduleHistoryEntity -> modelMapper.map(repaymentScheduleHistoryEntity, RepaymentSchedule.class))
                .collectList();
    }

    @Override
    public Flux<RepaymentSchedule> getAllRepaymentScheduleHistoryByManagementProcessId(String managementProcessId) {
        return repaymentScheduleHistoryRepository.findAllByManagementProcessId(managementProcessId)
                .doOnNext(repaymentScheduleHistoryEntity -> log.info("Repayment Schedule History fetched: {}", repaymentScheduleHistoryEntity))
                .map(repaymentScheduleHistoryEntity -> modelMapper.map(repaymentScheduleHistoryEntity, RepaymentSchedule.class))
                .doOnError(throwable -> log.error("Error while fetching Repayment Schedule History: {}", throwable.getMessage()));
    }

    @Override
    public Mono<Void> deleteAllRepaymentHistoryByLoanRepayOid(List<String> oidList) {
        return repaymentScheduleHistoryRepository.deleteAllByLoanRepayScheduleOidIn(oidList)
                .doOnRequest(n -> log.info("Request Received for Deleting Repayment Schedule History"))
                .doOnSuccess(aVoid -> log.info("Repayment Schedule History deleted successfully"));
    }

    @Override
    public Flux<RepaymentSchedule> getAllRepaymentScheduleHistoryByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId) {
        return repaymentScheduleHistoryRepository.findAllByManagementProcessIdAndLoanAccountId(managementProcessId, loanAccountId)
                .doOnRequest(n -> log.info("Request Received for fetching Repayment Schedule History for managementProcessId : {} and loanAccountId : {}", managementProcessId, loanAccountId))
                .collectList()
                .doOnNext(repaymentScheduleHistoryEntity -> log.info("Repayment Schedule History fetched with size : {} for managementProcessId : {} and loanAccountId : {}", repaymentScheduleHistoryEntity.size(), managementProcessId, loanAccountId))
                .flatMapMany(Flux::fromIterable)
                .map(repaymentScheduleHistoryEntity -> modelMapper.map(repaymentScheduleHistoryEntity, RepaymentSchedule.class))
                .doOnError(throwable -> log.error("Error while fetching Repayment Schedule History: {}", throwable.getMessage()));
    }
}
