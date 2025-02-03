package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.DPSRepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.DPSRepaymentScheduleRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.DpsRepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.DpsRepaymentSchedule;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Status.STATUS_PENDING;

@Component
public class DpsRepaymentScheduleAdapter implements DpsRepaymentSchedulePersistencePort {
    private final DPSRepaymentScheduleRepository dpsRepaymentScheduleRepository;
    private final ModelMapper modelMapper;

    public DpsRepaymentScheduleAdapter(DPSRepaymentScheduleRepository dpsRepaymentScheduleRepository, ModelMapper modelMapper) {
        this.dpsRepaymentScheduleRepository = dpsRepaymentScheduleRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<List<DpsRepaymentSchedule>> saveRepaymentSchedule(List<DpsRepaymentSchedule> dpsRepaymentScheduleList) {
        return Flux.fromIterable(dpsRepaymentScheduleList)
                .map(dpsRepaymentSchedule -> modelMapper.map(dpsRepaymentSchedule, DPSRepaymentScheduleEntity.class))
                .map(dpsRepaymentScheduleEntity -> {
                    dpsRepaymentScheduleEntity.setDpsRepaymentScheduleId(UUID.randomUUID().toString());
                    dpsRepaymentScheduleEntity.setCreatedOn(LocalDateTime.now());
                    return dpsRepaymentScheduleEntity;
                })
                .flatMap(dpsRepaymentScheduleRepository::save)
                .map(dpsRepaymentScheduleEntity -> modelMapper.map(dpsRepaymentScheduleEntity, DpsRepaymentSchedule.class))
                .collectList();
    }

    @Override
    public Flux<DpsRepaymentSchedule> getDPSRepaymentScheduleBySavingsAccountId(String savingsAccountId) {
        return dpsRepaymentScheduleRepository
                .getDPSRepaymentScheduleEntitiesBySavingsAccountId(savingsAccountId)
                .map(dpsRepaymentScheduleEntity -> modelMapper.map(dpsRepaymentScheduleEntity, DpsRepaymentSchedule.class));
    }

    @Override
    public Mono<Boolean> updateDPSRepaymentScheduleStatus(String savingsAccountId, String status, List<Integer> paidRepaymentNos, String managementProcessId, LocalDate businessDate, String loginId) {
        return dpsRepaymentScheduleRepository.updateDpsRepaymentScheduleStatus(savingsAccountId, status, paidRepaymentNos, managementProcessId, businessDate, loginId);
    }

    @Override
    public Mono<Integer> countPendingDpsRepaymentScheduleBySavingsAccountId(String savingsAccountId) {
        return dpsRepaymentScheduleRepository.countDPSRepaymentScheduleEntitiesBySavingsAccountIdAndStatus(savingsAccountId, STATUS_PENDING.getValue());
    }

    @Override
    public Flux<DPSRepaymentScheduleEntity> updateDPSRepaymentScheduleStatusByManagementProcessId(String managementProcessId, String status, String loginId) {
        return dpsRepaymentScheduleRepository.getAllByManagementProcessId(managementProcessId)
                .map(dpsRepaymentScheduleEntity -> {
                    dpsRepaymentScheduleEntity.setStatus(status);
                    dpsRepaymentScheduleEntity.setUpdatedOn(LocalDateTime.now());
                    dpsRepaymentScheduleEntity.setUpdatedBy(loginId);
                    dpsRepaymentScheduleEntity.setManagementProcessId(null);
                    dpsRepaymentScheduleEntity.setActualRepaymentDate(null);
                    return dpsRepaymentScheduleEntity;
                })
                .flatMap(dpsRepaymentScheduleRepository::save);
    }
}
