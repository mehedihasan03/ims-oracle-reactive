package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter;

import net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.entity.SavingsAccountEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SavingsAccountAdapterTest {

    @Autowired
    private SavingsAccountAdapter adapter;

    @Test
    void contextLoad() {
        Assertions.assertNotNull(adapter);
    }

    @ParameterizedTest
    @MethodSource("dpsPeriodParamProvider")
    void getDPSPeriod(String installmentFrequency, Integer depositTerm, String depositTermPeriod) {
        Integer actualResponse = adapter.getDPSPeriod(installmentFrequency, depositTerm, depositTermPeriod);
        System.out.println("dpsPeriod : " + actualResponse);
    }

    private static Stream<? extends Arguments> dpsPeriodParamProvider() {
        return Stream.of(
                Arguments.of(
                        "Weekly",
                        7,
                        "Month"));
    }

    @ParameterizedTest
    @MethodSource("dpsMaturityAmountParamProvider")
    void getDPSMaturityAmount(SavingsAccountEntity savingsAccountEntity, BigDecimal interestRate, String interestRateFrequency) {
        BigDecimal actualResponse = adapter.getDPSMaturityAmount(savingsAccountEntity, interestRate, interestRateFrequency);
        System.out.println("dps maturity amount : " + actualResponse);
    }

    private static Stream<? extends Arguments> dpsMaturityAmountParamProvider() {
        return Stream.of(
                Arguments.of(
                        SavingsAccountEntity
                                .builder()
                                .savingsAmount(BigDecimal.valueOf(500))
                                .depositEvery("Weekly")
                                .depositTerm(5)
                                .depositTermPeriod("Month")
                                .build(),
                        BigDecimal.valueOf(8.00),
                        "Yearly"));
    }



}