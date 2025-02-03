package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.LockCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.LockCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.LockCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockCollectionService implements LockCollectionUseCase {

    private final CollectionStagingDataPersistencePort port;

    @Override
    public Mono<LockCollectionResponseDto> lockCollectionBySamity(LockCollectionCommand command) {
        return port.lockCollectionBySamity(command.getSamityId(), command.getLoginId())
                .mapNotNull(dataLocked -> "Collection Data is Successfully Locked")
                .doOnSuccess(log::info)
                .doOnError(err -> log.error("Error occurred while locking data: {}", err.getMessage()))
                .mapNotNull(LockCollectionResponseDto::new);
    }

    @Override
    public Mono<LockCollectionResponseDto> unlockCollectionBySamity(LockCollectionCommand command) {
        return port.unlockCollectionBySamity(command.getSamityId())
                .mapNotNull(dataUnlocked -> "Collection Data is Successfully Unlocked")
                .doOnSuccess(log::info)
                .doOnError(err -> log.error("Error occurred while unlocking data: {}", err.getMessage()))
                .mapNotNull(LockCollectionResponseDto::new);
    }
}
