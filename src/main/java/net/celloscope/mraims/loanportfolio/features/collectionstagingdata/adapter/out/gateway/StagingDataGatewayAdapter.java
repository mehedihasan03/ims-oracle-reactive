package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository.StagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.StagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class StagingDataGatewayAdapter implements StagingDataPersistencePort {
        private final ModelMapper mapper;
        private final StagingDataRepository stagingDataRepository;

        @Override
        public Flux<StagingData> getListOfStagingDataByFieldOfficerId(String fieldOfficerId) {
                return stagingDataRepository
                                .findAllByFieldOfficerId(fieldOfficerId)
                                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class))
                                .doOnComplete(
                                                () -> log.info("StagingDataEntity fetched from Db for fieldOfficerId - {}",
                                                                fieldOfficerId))
                                .doOnError(
                                                e -> log.error("Error while fetching StagingDataEntity from Db\nReason - {}",
                                                                e.getMessage()));
        }

        @Override
        public Flux<StagingData> getListOfStagingDataBySamityId(String samityId) {
                return stagingDataRepository
                                .findAllBySamityId(samityId)
                                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class))
                                .doOnComplete(() -> log.info("StagingDataEntity fetched from Db for samityId - {}",
                                                samityId))
                                .doOnError(
                                                e -> log.error("Error while fetching StagingDataEntity from Db\nReason - {}",
                                                                e.getMessage()));
        }

        @Override
        public Flux<StagingData> getListOfStagingDataByFieldOfficerIdAndSamityDay(String fieldOfficerId,
                        String samityDay) {
                log.info("field officer Id {} & Samity Day {}", fieldOfficerId, samityDay);
                return stagingDataRepository
                                .findAllByFieldOfficerIdAndSamityDay(fieldOfficerId, samityDay)
                                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class))
                                .doOnComplete(
                                                () -> log.info("StagingDataEntity fetched from Db for fieldOfficerId - {}",
                                                                fieldOfficerId))
                                .doOnError(
                                                e -> log.error("Error while fetching StagingDataEntity from Db\nReason - {}",
                                                                e.getMessage()));
        }


    @Override
    public Flux<StagingData> getListOfStagingDataByMfiId(String mfiId) {
        return null;
    }
    
    @Override
    public Flux<StagingData> getListOfStagingDataByFieldOfficerIdForNonSamityDay(String fieldOfficerId, String samityDay) {
        return stagingDataRepository
            .findAllByFieldOfficerIdForNonSamityDay(fieldOfficerId, samityDay)
            .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class))
            .doOnComplete(() -> log.info("StagingDataEntity fetched from Db for fieldOfficerId - {}", fieldOfficerId))
            .doOnError(e -> log.error("Error while fetching StagingDataEntity from Db\nReason - {}", e.getMessage()));
    }

    @Override
    public Flux<StagingData> getListOfStagingDataBySamityIdIdAndSamityDay(String samityId, String samityDay) {
        return stagingDataRepository.findAllBySamityIdAndSamityDay(samityId, samityDay)
                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class));
    }

    @Override
    public Mono<StagingData> getOneStagingDataEntityBySamityId(String samityId) {
        return stagingDataRepository.findFirstBySamityId(samityId)
                .map(stagingDataEntity -> mapper.map(stagingDataEntity, StagingData.class));
    }

}
