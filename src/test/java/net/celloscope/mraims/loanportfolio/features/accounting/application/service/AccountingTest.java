package net.celloscope.mraims.loanportfolio.features.accounting.application.service;

import com.google.gson.Gson;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountingTest {

    @Autowired
    private AccountingService accountingService;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private Gson gson;

/*    @ParameterizedTest
    @MethodSource("paramProvider")
    void testAccounting(JournalRequestDTO requestDTO){



        webTestClient.post()
                .uri("https://mra-ims.celloscope.net/mra-ims/mfi/api/v1/journal/save?instituteOid=MRA-IMS-MFI-Oid-Template")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(gson.toJson(requestDTO)), String.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(System.out::println);
    }*/

    private static Stream<? extends Arguments> paramProvider() {

        Journal journal1 = Journal.builder()
                .description("Day End Collection Tk 26600.00 By Cash")
                .debitedAmount(BigDecimal.valueOf(26600.00))
                .creditedAmount(BigDecimal.valueOf(0.0))
                .ledgerId("101001-1001")
                .build();

        Journal journal2 = Journal.builder()
                .description("Deposit Payable Savings Account of Savings product: SP-1001")
                .debitedAmount(BigDecimal.valueOf(0.00))
                .creditedAmount(BigDecimal.valueOf(200.0))
                .ledgerId("201002-1001")
                .subledgerId("201002-1001-SP-1001")
                .build();

        Journal journal3 = Journal.builder()
                .description("Loan Receivable of Loan product: L0002")
                .debitedAmount(BigDecimal.valueOf(0.00))
                .creditedAmount(BigDecimal.valueOf(21208.60))
                .ledgerId("101004-1001")
                .subledgerId("101004-1001-L0002")
                .build();

        Journal journal4 = Journal.builder()
                .description("Service Charge Receivable of Loan product: L0002")
                .debitedAmount(BigDecimal.valueOf(0.00))
                .creditedAmount(BigDecimal.valueOf(5191.40))
                .ledgerId("101005-1001")
                .subledgerId("101005-1001-L0002")
                .build();

        List<Journal> journalList = List.of(journal1, journal2, journal3, journal4);

        return Stream.of(Arguments.of(
                JournalRequestDTO
                        .builder()
                        .journalType("Collection")
                        .description("Collection, Date: 2023-09-09")
                        .amount(BigDecimal.valueOf(26600.00))
                        .referenceNo("2023-09-09")
                        .journalProcess("Auto")
                        .createdBy("abc")
                        .mfiId("M1001")
                        .officeId("1018")
                        .journalList(journalList)
                        .build()
        ));
    }
}
