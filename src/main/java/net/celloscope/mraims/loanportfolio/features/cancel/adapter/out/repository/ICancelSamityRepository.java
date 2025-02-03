package net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.repository;
import net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.entity.SamityEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@Repository
public interface ICancelSamityRepository extends ReactiveCrudRepository<SamityEventTrackerEntity, String> {
    Mono<SamityEventTrackerEntity> findSamityEventTrackerEntityByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

}
