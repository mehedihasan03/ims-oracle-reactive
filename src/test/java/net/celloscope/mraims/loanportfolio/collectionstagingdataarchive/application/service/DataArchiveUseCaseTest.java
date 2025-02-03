package net.celloscope.mraims.loanportfolio.collectionstagingdataarchive.application.service;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.DataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.commands.DataArchiveCommandDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.response.DataArchiveResponseDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service.CollectionStagingDataArchiveService;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service.DataArchiveService;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service.StagingAccountDataArchiveService;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service.StagingDataArchiveService;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class DataArchiveUseCaseTest {

    private final DataArchiveUseCase service;

    @Mock
    StagingDataArchiveService stagingDataArchiveService;

    @Mock
    StagingAccountDataArchiveService stagingAccountDataArchiveService;

    @Mock
    CollectionStagingDataArchiveService collectionStagingDataArchiveService;

    public DataArchiveUseCaseTest(DataArchiveService dataArchiveService) {
        this.service = dataArchiveService;
    }

//    @Test
    public void archiveTest() {
        DataArchiveCommandDto commandDto = DataArchiveCommandDto.builder()
                .officeId("USHA")
                .build();

        DataArchiveResponseDto responseDto = DataArchiveResponseDto.builder()
                .userMessage("Data successfully archived.")
                .build();

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(stagingDataArchiveService.archiveStagingDataIntoHistory(Mockito.anyString()));

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(stagingAccountDataArchiveService.archiveStagingAccountDataIntoHistory(Mockito.anyString()));

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(collectionStagingDataArchiveService.archiveCollectionStagingDataIntoHistory(Mockito.anyString()));

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(stagingDataArchiveService.deleteLiveStagingData(Mockito.anyString()));

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(stagingAccountDataArchiveService.deleteLiveStagingAccountData(Mockito.anyString()));

        Mockito
                .doReturn(Mono.just(Boolean.TRUE))
                .when(collectionStagingDataArchiveService.deleteLiveCollectionStagingData(Mockito.anyString()));

        StepVerifier.create(service.archive(commandDto))
                .expectNext(responseDto)
                .verifyComplete();
    }

}
