package net.celloscope.mraims.loanportfolio.features.accounting.application.service;

import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.out.AisMetaDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaData;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.testcontainers.shaded.com.google.common.util.concurrent.AtomicDouble;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AccountingServiceTest {
    @InjectMocks
    private AccountingService accountingService;

    @Mock
    private CommonRepository commonRepository;

    @Mock
    private AisMetaDataPersistencePort aisMetaDataPersistencePort;
    @Mock
    private PassbookUseCase passbookUseCase;
    @Mock
    private ISavingsAccountUseCase savingsAccountUseCase;
    @Mock
    private LoanAccountUseCase loanAccountUseCase;
    @Mock
    private SavingsInterestUseCase savingsInterestUseCase;
    @Mock
    private ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Test
    void contextLoad() {
        Assertions.assertNotNull(accountingService);
        Assertions.assertNotNull(commonRepository);
        Assertions.assertNotNull(aisMetaDataPersistencePort);
        Assertions.assertNotNull(passbookUseCase);
        Assertions.assertNotNull(loanAccountUseCase);
        Assertions.assertNotNull(managementProcessTrackerUseCase);
    }



    @Test
    void getProductIdSubLedgerIdMap() {
        String ledgerId = "101004-1018";
        Map<String, BigDecimal> productAmountMapBank = Map.of("CashAtBank", BigDecimal.valueOf(100));

        Mockito.when(commonRepository.getSubLedgerIdByLedgerIdAndReferenceId(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just("101004-1018-L0001-Bank"));
        Object object = (Object) PassbookResponseDTO
                .builder()
                .depositAmount(BigDecimal.valueOf(100))
                .referenceId("Bank account id")
                .build();

        Mono<Map<String, String>> productIdSubLedgerIdMapMono = accountingService.getProductIdSubLedgerIdMap(ledgerId, productAmountMapBank, object).log();

        StepVerifier
                .create(productIdSubLedgerIdMapMono)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }



    @Test
    void getAccountingJournalRequestBodyTest() {
        mockGetAisMetaDataByProcessName();
        mockGetPassbookEntries();
        mockGetProductId();
        mockGetLedgerId();
        mockGetSubLedgerId();
        mockGetSubLedgerIdForCashAtBank();
        mockGetCurrentBusinessDateForOffice();

        AccountingRequestDTO requestDTO = AccountingRequestDTO
                .builder()
                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                .managementProcessId("42449af2-14cd-4c8a-8e91-f2163b021e66")
                .mfiId("M0001")
                .loginId("Emon")
                .officeId("1018")
                .build();

        Mono<JournalRequestDTO> actualResponse = accountingService.getAccountingJournalRequestBody(requestDTO).log();

        StepVerifier
                .create(actualResponse)
                .expectSubscription()
                .assertNext(element -> assertThat(element).isInstanceOf(JournalRequestDTO.class))
                .expectComplete()
                .verify();

        // TODO mock get ledger id & sub-ledger id
    }




    void mockGetLedgerId() {


        Mockito.doReturn(Mono.just("CashOnHand")).when(commonRepository)
                .getLedgerIdByLedgerKeyAndOfficeId("CashOnHand", "1018");

        Mockito.doReturn(Mono.just("CashAtBank")).when(commonRepository)
                .getLedgerIdByLedgerKeyAndOfficeId("CashAtBank", "1018");

        Mockito.doReturn(Mono.just("CompulsorySavings")).when(commonRepository)
                .getLedgerIdByLedgerKeyAndOfficeId("CompulsorySavings", "1018");

        Mockito.doReturn(Mono.just("FixedDeposit")).when(commonRepository)
                .getLedgerIdByLedgerKeyAndOfficeId("FixedDeposit", "1018");

        Mockito.doReturn(Mono.just("TermDeposit")).when(commonRepository)
                .getLedgerIdByLedgerKeyAndOfficeId("TermDeposit", "1018");



    }


    void mockGetSubLedgerId() {

        Mockito.when(commonRepository
                        .getSubLedgerIdByLedgerIdAndProductId("CompulsorySavings", "SP-1001"))
                .thenReturn(Mono.just("CompulsorySavings - subLedger"));


        Mockito.when(commonRepository
                        .getSubLedgerIdByLedgerIdAndProductId("TermDeposit", "SP-1002"))
                .thenReturn(Mono.just("TermDeposit - subLedger"));


        Mockito.when(commonRepository
                        .getSubLedgerIdByLedgerIdAndProductId("FixedDeposit", "SP-1003"))
                .thenReturn(Mono.just("FixedDeposit - subLedger"));

    }


    void mockGetSubLedgerIdForCashAtBank() {
        Mockito.when(commonRepository
                        .getSubLedgerIdByLedgerIdAndReferenceId("CashAtBank", "bank-01"))
                .thenReturn(Mono.just("CashAtBank - subLedger"));
    }


    void mockGetProductId() {

        Mockito.doReturn(Mono.just("SP-1001")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("GS-1018-101-1002");

        Mockito.doReturn(Mono.just("SP-1001")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("GS-1018-101-1003");

        Mockito.doReturn(Mono.just("SP-1003")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("FDR-1018-104-1002");

        Mockito.doReturn(Mono.just("SP-1002")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("DPS-1018-106-1003");

        Mockito.doReturn(Mono.just("SP-1001")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("GS-1018-103-1002");

        Mockito.doReturn(Mono.just("SP-1001")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("GS-1018-103-1001");

        Mockito.doReturn(Mono.just("SP-1001")).when(savingsAccountUseCase)
                .getProductIdBySavingsAccountId("GS-1018-101-1001");

    }

    void mockGetCurrentBusinessDateForOffice() {
        Mockito.when(
                managementProcessTrackerUseCase
                        .getCurrentBusinessDateForOffice("42449af2-14cd-4c8a-8e91-f2163b021e66", "1018"))
                .thenReturn(Mono.just(LocalDate.of(2024, 2, 3)));
    }


    void mockGetPassbookEntries() {

//        @TODO CASH
        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", "CASH", null))
                .thenReturn(Flux.just(
                        PassbookResponseDTO
                            .builder()
                            .depositAmount(BigDecimal.valueOf(500))
                            .savingsAccountId("GS-1018-101-1002")
                            .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(10))
                                .savingsAccountId("GS-1018-101-1003")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(5000))
                                .savingsAccountId("FDR-1018-104-1002")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(500))
                                .savingsAccountId("DPS-1018-106-1003")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(1000))
                                .savingsAccountId("GS-1018-103-1002")
                                .build()

                        ));

//        @TODO BANK


        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", "BANK", null))
                .thenReturn(Flux.just(
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(200))
                                .savingsAccountId("GS-1018-103-1001")
                                .referenceId("bank-01")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(100))
                                .savingsAccountId("GS-1018-101-1001")
                                .referenceId("bank-01")
                                .build()
                ));

//        @TODO GS

        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", null, "GS"))
                .thenReturn(Flux.just(
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(200))
                                .savingsAccountId("GS-1018-103-1001")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(10))
                                .savingsAccountId("GS-1018-101-1003")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(500))
                                .savingsAccountId("GS-1018-101-1002")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(100))
                                .savingsAccountId("GS-1018-101-1001")
                                .build(),
                        PassbookResponseDTO
                                .builder()
                                .depositAmount(BigDecimal.valueOf(1000))
                                .savingsAccountId("GS-1018-103-1002")
                                .build()
                ));


        //        @TODO VS

        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", null, "VS"))
                .thenReturn(Flux.empty());


        //        @TODO DPS

        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", null, "DPS"))
                .thenReturn(Flux.just(PassbookResponseDTO
                        .builder()
                        .depositAmount(BigDecimal.valueOf(500))
                        .savingsAccountId("DPS-1018-106-1003")
                        .build()));



        //        @TODO FDR

        Mockito
                .when(passbookUseCase
                        .getPassbookEntriesByProcessManagementIdAndTransactionCodeAndPaymentModeAndSavingsTypeId("42449af2-14cd-4c8a-8e91-f2163b021e66", "SAVINGS_DEPOSIT", null, "FDR"))
                .thenReturn(Flux.just(PassbookResponseDTO
                        .builder()
                        .depositAmount(BigDecimal.valueOf(5000))
                        .savingsAccountId("FDR-1018-104-1002")
                        .build()));



    }


    void mockGetAisMetaDataByProcessName() {
        Mockito
                .when(aisMetaDataPersistencePort.getAisMetaDataByProcessName(Mockito.any()))
                .thenReturn(Flux.just(AisMetaData
                                .builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("CashOnHand")
                                .journalEntryType("Debit")
                                .hasSubledger("No")
                                .isAggregated("Yes")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId(null)
                                .paymentMode("CASH")
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build(),
                        AisMetaData.builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("CashAtBank")
                                .journalEntryType("Debit")
                                .hasSubledger("Yes")
                                .isAggregated("Yes")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId(null)
                                .paymentMode("BANK")
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build(),
                        AisMetaData.builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("CompulsorySavings")
                                .journalEntryType("Credit")
                                .hasSubledger("Yes")
                                .isAggregated("No")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId("GS")
                                .paymentMode(null)
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build(),
                        AisMetaData.builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("VoluntarySavings")
                                .journalEntryType("Credit")
                                .hasSubledger("Yes")
                                .isAggregated("No")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId("VS")
                                .paymentMode(null)
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build(),
                        AisMetaData.builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("TermDeposit")
                                .journalEntryType("Credit")
                                .hasSubledger("Yes")
                                .isAggregated("No")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId("DPS")
                                .paymentMode(null)
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build(),
                        AisMetaData.builder()
                                .processName(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue())
                                .ledgerKey("FixedDeposit")
                                .journalEntryType("Credit")
                                .hasSubledger("Yes")
                                .isAggregated("No")
                                .tableName("passbook")
                                .fieldNames(List.of("depositAmount"))
                                .productType("Savings")
                                .savingsTypeId("FDR")
                                .paymentMode(null)
                                .transactionCode("SAVINGS_DEPOSIT")
                                .build()
                ));
    }



}