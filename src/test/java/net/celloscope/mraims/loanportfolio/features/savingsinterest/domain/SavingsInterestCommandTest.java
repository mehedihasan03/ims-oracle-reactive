package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.RequiredArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums.DaysInYear;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@RequiredArgsConstructor
class SavingsInterestCommandTest {

    private final SavingsInterestCommand savingsInterestCommand = new SavingsInterestCommand();
    @Test
    void calculateDailyAccruedInterest() {
        BigDecimal interest = savingsInterestCommand.calculateDailyAccruedInterest(BigDecimal.valueOf(1800), BigDecimal.valueOf(10), "YEARLY", 8, 2, RoundingMode.HALF_UP, DaysInYear._365_FIXED.getValue(), LocalDate.of(2023,8,1));

        System.out.println("\nCalculated Accrued Interest : " + interest + "\n");
    }
}