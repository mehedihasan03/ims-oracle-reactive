package net.celloscope.mraims.loanportfolio.features.transaction.application.service;

/*import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionStagingDataResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.TransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.ITransactionCommands;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.CollectionDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.StagingDataDTO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;*/

//@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    /*@InjectMocks
    private TransactionService transactionService;

    @Mock
    private IStagingDataUseCase stagingDataUseCase;

    @Mock
    private CollectionStagingDataQueryUseCase collectionUseCase;
    @Mock
    private ITransactionCommands iTransactionCommands;

//    @Test
    void createTransactionForOneSamity() {
        Mockito
                .when(stagingDataUseCase.getStagingDataBySamityId("samity101"))
                .thenReturn(Flux.just(
                    StagingDataResponseDTO
                            .builder()
                            .stagingDataId("staging data id 1")
                            .memberId("member id 1")
                            .processId("process id 1")
                            .mfiId("mfi id 1")
                            .build(),
                    StagingDataResponseDTO
                            .builder()
                            .stagingDataId("staging data id 2")
                            .memberId("member id 2")
                            .processId("process id 1")
                            .mfiId("mfi id 1")
                            .build()
            ));

        Mockito
            .when(collectionUseCase.getCollectionStagingDataForSamityMembers("staging data id 1"))
            .thenReturn(Flux.just(
                    CollectionStagingDataResponseDTO
                            .builder()
                            .stagingDataId("staging data id 1")
                            .accountType("loan")
                            .loanAccountId("loan account id 1")
                            .amount(BigDecimal.TEN)
                            .paymentMode("CASH")
                            .collectionType("Regular")
                            .createdOn(LocalDateTime.of(2023,01,01,10,00,00))
                            .createdBy("Admin 123")
                            .submittedOn(LocalDateTime.of(2023,01,01,11,00,00))
                            .submittedBy("Admin 123")
                            .isUploaded("Yes")
                            .uploadedOn(LocalDateTime.of(2023,01,01,12,00,00))
                            .uploadedBy("Admin 123")
                            .approvedOn(LocalDateTime.of(2023,01,01,13,00,00))
                            .approvedBy("System.admin")
                            .currentVersion("v01")
                            .status("Active")
                            .build()
            ));

        Mockito
                .when(collectionUseCase.getCollectionStagingDataForSamityMembers("staging data id 2"))
                .thenReturn(Flux.just(
                        CollectionStagingDataResponseDTO
                                .builder()
                                .stagingDataId("staging data id 2")
                                .accountType("loan")
                                .loanAccountId("loan account id 2")
                                .amount(BigDecimal.valueOf(20))
                                .paymentMode("CASH")
                                .collectionType("Regular")
                                .createdOn(LocalDateTime.of(2023,01,01,10,00,00))
                                .createdBy("Admin 123")
                                .submittedOn(LocalDateTime.of(2023,01,01,11,00,00))
                                .submittedBy("Admin 123")
                                .isUploaded("Yes")
                                .uploadedOn(LocalDateTime.of(2023,01,01,12,00,00))
                                .uploadedBy("Admin 123")
                                .approvedOn(LocalDateTime.of(2023,01,01,13,00,00))
                                .approvedBy("System.admin")
                                .currentVersion("v01")
                                .status("Active")
                                .build()
                ));

        Mockito
                .when(iTransactionCommands.buildTransaction(CollectionDataDTO
                        .builder()
                        .stagingDataId("staging data id 1")
                        .accountType("loan")
                        .loanAccountId("loan account id 1")
                        .amount(BigDecimal.TEN)
                        .paymentMode("CASH")
                        .collectionType("Regular")
                        .createdOn(LocalDateTime.of(2023,01,01,10,00,00))
                        .createdBy("Admin 123")
                        .submittedOn(LocalDateTime.of(2023,01,01,11,00,00))
                        .submittedBy("Admin 123")
                        .isUploaded("Yes")
                        .uploadedOn(LocalDateTime.of(2023,01,01,12,00,00))
                        .uploadedBy("Admin 123")
                        .approvedOn(LocalDateTime.of(2023,01,01,13,00,00))
                        .approvedBy("System.admin")
                        .currentVersion("v01")
                        .status("Active")
                        .build(),

                        StagingDataDTO
                        .builder()
                        .stagingDataId("staging data id 1")
                        .memberId("member id 1")
                        .processId("process id 1")
                        .mfiId("mfi id 1")
                        .build()))
                .thenReturn(Flux.just(Transaction
                        .builder()
                        .stagingDataId("staging data id 1")
                        .accountType("loan")
                        .transactionId("UUID")
                        .processId("process id 1")
                        .memberId("member id 1")
                        .loanAccountId("loan account id 1")
                        .amount(BigDecimal.TEN)
                        .paymentMode("CASH")
                        .collectionType("Regular")
                        .transactionCode("LOAN_REPAY")
                        .mfiId("mfi id 1")
                        .transactionDate(LocalDate.of(2023,01,01))
                        .transactedBy("Admin 123")
                        .createdOn(LocalDateTime.of(2023,01,01,13,00,00))
                        .createdBy("System.admin")
                        .build()));


        Mockito
                .when(iTransactionCommands.buildTransaction(CollectionDataDTO
                                        .builder()
                                        .stagingDataId("staging data id 2")
                                        .accountType("loan")
                                        .loanAccountId("loan account id 2")
                                        .amount(BigDecimal.valueOf(20))
                                        .paymentMode("CASH")
                                        .collectionType("Regular")
                                        .createdOn(LocalDateTime.of(2023,01,01,10,00,00))
                                        .createdBy("Admin 123")
                                        .submittedOn(LocalDateTime.of(2023,01,01,11,00,00))
                                        .submittedBy("Admin 123")
                                        .isUploaded("Yes")
                                        .uploadedOn(LocalDateTime.of(2023,01,01,12,00,00))
                                        .uploadedBy("Admin 123")
                                        .approvedOn(LocalDateTime.of(2023,01,01,13,00,00))
                                        .approvedBy("System.admin")
                                        .currentVersion("v01")
                                        .status("Active")
                                        .build(),
                                StagingDataDTO
                                        .builder()
                                        .stagingDataId("staging data id 2")
                                        .memberId("member id 2")
                                        .processId("process id 1")
                                        .mfiId("mfi id 1")
                                        .build()
                                ))
                .thenReturn(Flux.just(Transaction
                        .builder()
                        .stagingDataId("staging data id 2")
                        .accountType("loan")
                        .transactionId("UUID")
                        .processId("process id 1")
                        .memberId("member id 2")
                        .loanAccountId("loan account id 2")
                        .amount(BigDecimal.valueOf(20))
                        .paymentMode("CASH")
                        .collectionType("Regular")
                        .transactionCode("LOAN_REPAY")
                        .mfiId("mfi id 1")
                        .transactionDate(LocalDate.of(2023,01,01))
                        .transactedBy("Admin 123")
                        .createdOn(LocalDateTime.of(2023,01,01,13,00,00))
                        .createdBy("System.admin")
                        .build()));

        TransactionResponseDTO expectedResponse = TransactionResponseDTO
                .builder()
                .transactionList(
                        List.of(
                                Transaction
                                        .builder()
                                        .stagingDataId("staging data id 1")
                                        .accountType("loan")
                                        .transactionId("UUID")
                                        .processId("process id 1")
                                        .memberId("member id 1")
                                        .loanAccountId("loan account id 1")
                                        .amount(BigDecimal.TEN)
                                        .paymentMode("CASH")
                                        .collectionType("Regular")
                                        .transactionCode("LOAN_REPAY")
                                        .mfiId("mfi id 1")
                                        .transactionDate(LocalDate.of(2023,01,01))
                                        .transactedBy("Admin 123")
                                        .createdOn(LocalDateTime.of(2023,01,01,13,00,00))
                                        .createdBy("System.admin")
                                        .build(),
                                Transaction
                                        .builder()
                                        .stagingDataId("staging data id 2")
                                        .accountType("loan")
                                        .transactionId("UUID")
                                        .processId("process id 1")
                                        .memberId("member id 2")
                                        .loanAccountId("loan account id 2")
                                        .amount(BigDecimal.valueOf(20))
                                        .paymentMode("CASH")
                                        .collectionType("Regular")
                                        .transactionCode("LOAN_REPAY")
                                        .mfiId("mfi id 1")
                                        .transactionDate(LocalDate.of(2023,01,01))
                                        .transactedBy("Admin 123")
                                        .createdOn(LocalDateTime.of(2023,01,01,13,00,00))
                                        .createdBy("System.admin")
                                        .build()
                        )
                )
                .build();
        final String officeId = "";
        final String transactionProcessId = UUID.randomUUID().toString();
        final String managementProcessId = UUID.randomUUID().toString();
        String source = "Application";
        Mono<TransactionResponseDTO> actualResponse = transactionService.createTransactionForOneSamity("samity101", managementProcessId, transactionProcessId, officeId, source);
        StepVerifier
                .create(actualResponse)
                .expectNext(expectedResponse)
                .verifyComplete();

    }*/

}