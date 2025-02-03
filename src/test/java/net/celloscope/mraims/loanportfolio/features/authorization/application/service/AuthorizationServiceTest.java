package net.celloscope.mraims.loanportfolio.features.authorization.application.service;

import net.celloscope.mraims.loanportfolio.core.pubsub.ReactiveRedisSubscriber;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuthorizationServiceTest {

    @Autowired
    private AuthorizationService authorizationService;

    @MockBean
    private IStagingDataUseCase stagingDataUseCase;

    @MockBean
    private CommonRepository commonRepository;

  /*  @MockBean
    private ISmsNotificationUseCase smsNotificationUseCase;*/



    @Test
    void contextLoads() {
        assertNotNull(authorizationService);
    }


    @Test
    public void testCreateAndSaveSMSNotificationRequest() {
        // Arrange
        Transaction transaction = buildTransaction(); // Initialize this with appropriate values
        SMSNotificationMetaProperty smsNotificationMetaProperty = buildSMSNotificationMetaProperty(); // Initialize this with appropriate values
        StagingData stagingData = buildStagingData(); // Initialize this with appropriate values
        String instituteOid = "TEST-INSTITUTE-OID"; // Use a valid data from database
        String loginId = "TEST-USER";

        when(stagingDataUseCase.getStagingDataByAccountId(any(String.class))).thenReturn(Mono.just(stagingData));
        when(commonRepository.getInstituteOidByMFIId(any(String.class))).thenReturn(Mono.just(instituteOid));
//        when(smsNotificationUseCase.publishSmsRequest(any(SmsNotificationRequestDTO.class))).thenReturn(Mono.empty());

        // Act
        Mono<Transaction> result = authorizationService.createAndSaveSMSNotificationRequest(transaction, smsNotificationMetaProperty, loginId);

        // Assert
        StepVerifier.create(result)
                .expectNext(transaction)
                .verifyComplete();

        Mockito.verify(stagingDataUseCase, Mockito.times(1)).getStagingDataByAccountId(any(String.class));
        Mockito.verify(commonRepository, Mockito.times(1)).getInstituteOidByMFIId(any(String.class));
//        Mockito.verify(smsNotificationUseCase, Mockito.times(1)).publishSmsRequest(any(SmsNotificationRequestDTO.class));
    }

    @Test
    public void dirtyTestCreateAndSaveSMSNotificationRequest() {
        // Arrange
        Transaction transaction = buildTransaction(); // Initialize this with appropriate values
        SMSNotificationMetaProperty smsNotificationMetaProperty = buildSMSNotificationMetaProperty(); // Initialize this with appropriate values
        StagingData stagingData = buildStagingData(); // Initialize this with appropriate values
        String instituteOid = "MRA-IMS-MFI-Oid-Template"; // Use a valid data from database
        String loginId = "TEST-USER";

        when(stagingDataUseCase.getStagingDataByAccountId(any(String.class))).thenReturn(Mono.just(stagingData));
        when(commonRepository.getInstituteOidByMFIId(any(String.class))).thenReturn(Mono.just(instituteOid));
//        when(smsNotificationUseCase.publishSmsRequest(any(SmsNotificationRequestDTO.class))).thenReturn(Mono.empty());

        // Act
        Mono<Transaction> result = authorizationService.createAndSaveSMSNotificationRequest(transaction, smsNotificationMetaProperty, loginId);

        // Assert
        StepVerifier.create(result)
            .expectNext(transaction)
            .verifyComplete();

        Mockito.verify(stagingDataUseCase, Mockito.times(1)).getStagingDataByAccountId(any(String.class));
        Mockito.verify(commonRepository, Mockito.times(1)).getInstituteOidByMFIId(any(String.class));
//        Mockito.verify(smsNotificationUseCase, Mockito.times(1)).publishSmsRequest(any(SmsNotificationRequestDTO.class));
    }

    private Transaction buildTransaction() {
        return Transaction.builder()
                .savingsAccountId("TEST-ACCOUNT")
                .amount(BigDecimal.TEN)
                .mfiId("TEST-MFI")
                .transactionCode("TRANSACTION-CODE")
                .transactionId("TEST-ID")
                .transactionDate(LocalDate.now())
                .build();
    }

    private SMSNotificationMetaProperty buildSMSNotificationMetaProperty() {
        return SMSNotificationMetaProperty.builder()
                .type("TRANSACTION-CODE")
                .isSMSNotificationEnabled("YES")
                .template("SMS TEMPLATE WITH AMOUNT: ${amount}")
                .build();
    }

    private StagingData buildStagingData() {
        return StagingData.builder()
                .mobile("[{\"contact\":1,\"contactNo\":\"01914730587\"}]")
                .build();
    }
}
