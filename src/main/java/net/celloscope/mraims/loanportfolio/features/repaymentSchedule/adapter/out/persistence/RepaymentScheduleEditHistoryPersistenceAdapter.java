package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.RepaymentScheduleEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleEditHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepaymentScheduleEditHistoryPersistenceAdapter implements RepaymentScheduleEditHistoryPersistencePort {

    private final ModelMapper modelMapper;
    private final RepaymentScheduleEditHistoryRepository repaymentScheduleEditHistoryRepository;

    @Override
    public Mono<List<RepaymentSchedule>> saveRepaymentScheduleEditHistory(List<RepaymentSchedule> repaymentSchedule) {
        return repaymentScheduleEditHistoryRepository.deleteRepaymentScheduleEditHistoryEntitiesByManagementProcessIdAndLoanAccountId(repaymentSchedule.get(0).getManagementProcessId(), repaymentSchedule.get(0).getLoanAccountId())
                .doOnRequest(repaymentScheduleDomain -> log.info("Request Received for delete Repayment Schedule Edit History Size if exist : {}", repaymentSchedule.size()))
                .flatMap(aBoolean -> Flux.fromIterable(repaymentSchedule)
                        .doOnRequest(repaymentScheduleDomain -> log.info("Request Received for Saving Repayment Schedule Edit History Size : {}", repaymentSchedule.size()))
                        .map(repaymentScheduleDomain -> modelMapper.map(repaymentScheduleDomain, RepaymentScheduleEditHistoryEntity.class))
                        .collectList()
                        .flatMapMany(repaymentScheduleEditHistoryRepository::saveAll)
                        .map(repaymentScheduleEditHistoryEntity -> modelMapper.map(repaymentScheduleEditHistoryEntity, RepaymentSchedule.class))
                        .collectList());
    }

    @Override
    public Flux<RepaymentSchedule> getAllRepaymentScheduleEditHistoryByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId) {
        return repaymentScheduleEditHistoryRepository.findAllByManagementProcessIdAndLoanAccountId(managementProcessId, loanAccountId)
                .doOnRequest(repaymentScheduleEditHistoryEntity -> log.info("Request Received for Getting Repayment Schedule Edit History By Management Process Id and Loan Account Id : {}, {}", managementProcessId, loanAccountId))
                .map(repaymentScheduleEditHistoryEntity -> modelMapper.map(repaymentScheduleEditHistoryEntity, RepaymentSchedule.class))
                .doOnNext(repaymentSchedule -> log.info("Repayment Schedule Edit History Retrieved : {}", repaymentSchedule))
                .doOnError(throwable -> log.error("Error Occurred while Getting Repayment Schedule Edit History By Management Process Id and Loan Account Id : {}, {}", managementProcessId, loanAccountId, throwable));
    }

    @Override
    public Mono<Void> deleteAllRepaymentEditHistoryByLoanRepayOid(List<String> oidList) {
        return repaymentScheduleEditHistoryRepository.deleteAllByLoanRepayScheduleOidIn(oidList)
                .doOnRequest(n -> log.info("Request Received for Deleting Repayment Schedule Edit History"))
                .doOnSuccess(aVoid -> log.info("Repayment Schedule Edit History deleted successfully"));
    }
}
