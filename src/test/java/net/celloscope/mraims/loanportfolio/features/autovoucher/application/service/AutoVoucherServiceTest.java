package net.celloscope.mraims.loanportfolio.features.autovoucher.application.service;

import com.google.gson.GsonBuilder;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.FDRInterestCalculationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class AutoVoucherServiceTest {

    @InjectMocks
    AutoVoucherService autoVoucherService;

    @ParameterizedTest
    @MethodSource("aisRequestListProvider")
    void buildAutoVoucherDataMapFromAISRequest(List<JournalRequestDTO> journalRequestDTOList) {
        Map<String, List<AutoVoucherData>> voucherNameDataMap = autoVoucherService.buildAutoVoucherDataMapFromAISRequest(journalRequestDTOList);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(voucherNameDataMap));
    }


    @ParameterizedTest
    @MethodSource("commandAndMapProvider")
    void buildAutoVoucherFromCommandAndVoucherNameDataMap(AutoVoucherRequestDTO command, Map<String, List<AutoVoucherData>> voucherNameAndDataMap) {
        List<AutoVoucher> autoVoucherList = autoVoucherService.buildAutoVoucher(command, voucherNameAndDataMap);
        System.out.println(autoVoucherList);
    }

    @Test
    void convertRemarks() {
        String[] words = "PrincipalOutstanding".split("(?<!^)(?=[A-Z])");
        String ledgerName = String.join(" ", words);

        String description = "WITHDRAW".replace("_", " ") +  " - " + ledgerName;

        System.out.println(description);  // Output: LOAN ADJUSTMENT - Principal Outstanding
    }



    private static Stream<? extends Arguments> commandAndMapProvider() {
        return Stream.of(
                Arguments.of(
                        AutoVoucherRequestDTO
                                .builder()
                                .build(),
                        Map.of(
                                "Adjustment Voucher", List.of(
                                        AutoVoucherData
                                                .builder()
                                                .description("LOAN_ADJUSTMENT - PrincipalOutstanding")
                                                .debitedAmount(BigDecimal.valueOf(0))
                                                .creditedAmount(BigDecimal.valueOf(10))
                                                .ledgerId("104003-1064")
                                                .subledgerId("104003-1064-L0002")
                                                .build(),
                                        AutoVoucherData
                                                .builder()
                                                .description("LOAN_ADJUSTMENT - CompulsorySavings")
                                                .debitedAmount(BigDecimal.valueOf(10))
                                                .creditedAmount(BigDecimal.valueOf(0))
                                                .ledgerId("201012-1064")
                                                .subledgerId("201012-1064-SP-1001")
                                                .build()
                                        ),
                                "Payment Voucher", List.of(
                                        AutoVoucherData
                                                .builder()
                                                .description("WITHDRAW - CashOnHand")
                                                .debitedAmount(BigDecimal.valueOf(0))
                                                .creditedAmount(BigDecimal.valueOf(100))
                                                .ledgerId("101001-1064")
                                                .build(),
                                        AutoVoucherData
                                                .builder()
                                                .description("WITHDRAW - CompulsorySavings")
                                                .debitedAmount(BigDecimal.valueOf(100))
                                                .creditedAmount(BigDecimal.valueOf(0))
                                                .ledgerId("201012-1064")
                                                .subledgerId("201012-1064-SP-1001")
                                                .build()
                                ),
                                "Received Voucher", List.of(
                                        AutoVoucherData
                                                .builder()
                                                .description("LOAN_COLLECTION - CashOnHand")
                                                .debitedAmount(BigDecimal.valueOf(5944.0))
                                                .creditedAmount(BigDecimal.valueOf(0))
                                                .ledgerId("101001-1064")
                                                .build(),
                                        AutoVoucherData
                                                .builder()
                                                .description("LOAN_COLLECTION - ServiceChargeOutstanding")
                                                .debitedAmount(BigDecimal.valueOf(0))
                                                .creditedAmount(BigDecimal.valueOf(944.03))
                                                .ledgerId("104002-1064")
                                                .subledgerId("104002-1064-L0002")
                                                .build(),
                                        AutoVoucherData
                                                .builder()
                                                .description("SAVINGS_COLLECTION - CompulsorySavings")
                                                .debitedAmount(BigDecimal.valueOf(0))
                                                .creditedAmount(BigDecimal.valueOf(1776.0))
                                                .ledgerId("201012-1064")
                                                .subledgerId("201012-1064-SP-1001")
                                                .build(),
                                        AutoVoucherData
                                                .builder()
                                                .description("LOAN_COLLECTION - PrincipalOutstanding")
                                                .debitedAmount(BigDecimal.valueOf(0))
                                                .creditedAmount(BigDecimal.valueOf(3223.97))
                                                .ledgerId("104003-1064")
                                                .subledgerId("104003-1064-L0002")
                                                .build()

                                )
                        )));
        }


    private static Stream<? extends Arguments> aisRequestListProvider() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                JournalRequestDTO
                                        .builder()
                                        .journalType("WITHDRAW")
                                        .journalList(List.of(
                                                Journal
                                                    .builder()
                                                    .description("WITHDRAW - CashOnHand")
                                                    .debitedAmount(BigDecimal.valueOf(0))
                                                    .creditedAmount(BigDecimal.valueOf(100))
                                                    .ledgerId("101001-1064")
                                                    .build(),
                                                Journal
                                                        .builder()
                                                        .description("WITHDRAW - CompulsorySavings")
                                                        .debitedAmount(BigDecimal.valueOf(100))
                                                        .creditedAmount(BigDecimal.valueOf(0))
                                                        .ledgerId("201012-1064")
                                                        .subledgerId("201012-1064-SP-1001")
                                                        .build()
                                        ))
                                        .build(),
                                JournalRequestDTO
                                        .builder()
                                        .journalType("DISBURSEMENT")
                                        .journalList(List.of(
                                                Journal
                                                        .builder()
                                                        .description("DISBURSEMENT - CashOnHand")
                                                        .debitedAmount(BigDecimal.valueOf(0))
                                                        .creditedAmount(BigDecimal.valueOf(5000))
                                                        .ledgerId("101001-1064")
                                                        .build(),
                                                Journal
                                                        .builder()
                                                        .description("DISBURSEMENT - Principal Outstanding")
                                                        .debitedAmount(BigDecimal.valueOf(5000))
                                                        .creditedAmount(BigDecimal.valueOf(0))
                                                        .ledgerId("201012-1069")
                                                        .subledgerId("201012-1069-SP-1001")
                                                        .build()
                                        ))
                                        .build(),
                                JournalRequestDTO
                                        .builder()
                                        .journalType("LOAN_COLLECTION")
                                        .journalList(List.of(
                                                Journal
                                                        .builder()
                                                        .description("LOAN_COLLECTION - CashOnHand")
                                                        .debitedAmount(BigDecimal.valueOf(4168.00))
                                                        .creditedAmount(BigDecimal.valueOf(0))
                                                        .ledgerId("101001-1064")
                                                        .build(),
                                                Journal
                                                        .builder()
                                                        .description("LOAN_COLLECTION - ServiceChargeOutstanding")
                                                        .debitedAmount(BigDecimal.valueOf(0))
                                                        .creditedAmount(BigDecimal.valueOf(944.03))
                                                        .ledgerId("104002-1064")
                                                        .subledgerId("104002-1064-L0002")
                                                        .build(),
                                                Journal
                                                        .builder()
                                                        .description("LOAN_COLLECTION - PrincipalOutstanding")
                                                        .debitedAmount(BigDecimal.valueOf(0))
                                                        .creditedAmount(BigDecimal.valueOf(3223.97))
                                                        .ledgerId("104003-1064")
                                                        .subledgerId("104003-1064-L0002")
                                                        .build()
                                        ))
                                        .build(),
                                JournalRequestDTO
                                        .builder()
                                        .journalType("SAVINGS_COLLECTION")
                                        .journalList(List.of(
                                                Journal
                                                        .builder()
                                                        .description("SAVINGS_COLLECTION - CashOnHand")
                                                        .debitedAmount(BigDecimal.valueOf(1776))
                                                        .creditedAmount(BigDecimal.valueOf(0))
                                                        .ledgerId("101001-1064")
                                                        .build(),
                                                Journal
                                                        .builder()
                                                        .description("SAVINGS_COLLECTION - CompulsorySavings")
                                                        .debitedAmount(BigDecimal.valueOf(0))
                                                        .creditedAmount(BigDecimal.valueOf(1776))
                                                        .ledgerId("201012-1064")
                                                        .subledgerId("201012-1064-SP-1001")
                                                        .build()
                                        ))
                                        .build(),
                                JournalRequestDTO
                                        .builder()
                                        .journalType("LOAN_ADJUSTMENT")
                                        .journalList(List.of(
                                                Journal
                                                        .builder()
                                                        .description("LOAN_ADJUSTMENT - PrincipalOutstanding")
                                                        .debitedAmount(BigDecimal.valueOf(0))
                                                        .creditedAmount(BigDecimal.valueOf(10))
                                                        .ledgerId("104003-1064")
                                                        .subledgerId("104003-1064-L0002")
                                                        .build(),
                                                Journal
                                                        .builder()
                                                        .description("LOAN_ADJUSTMENT - CompulsorySavings")
                                                        .debitedAmount(BigDecimal.valueOf(10))
                                                        .creditedAmount(BigDecimal.valueOf(0))
                                                        .ledgerId("201012-1064")
                                                        .subledgerId("201012-1064-SP-1001")
                                                        .build()
                                        ))
                                        .build()
                        )));
    }

    @Test
    void buildVoucherId() {
        String voucherId = autoVoucherService.buildVoucherId("PaymentVoucher", LocalDate.now(), "1018");
        System.out.println(voucherId);
    }
}