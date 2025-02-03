package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.commands;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
@ExtendWith(MockitoExtension.class)
public class RepaymentDatesCommandsTest {

    @Mock
    private RepaymentDatesCommands repaymentDatesCommands;

    @Test
    public void testGetRepaymentDates() {
        Mockito.when(repaymentDatesCommands.getRepaymentDates(
                List.of(),
                LocalDate.of(2025, 1, 10),
                DayOfWeek.MONDAY,
                15,
                12,
                "HALF-YEARLY",
                10, 12
        )).thenCallRealMethod();

        List<LocalDate> repaymentDates = repaymentDatesCommands.getRepaymentDates(List.of(),
                LocalDate.of(2025, 1, 10),
                DayOfWeek.MONDAY,
                15,
                12,
                "HALF-YEARLY",
                10, 12);

        repaymentDates.forEach(localDate ->
                System.out.println(localDate.getDayOfWeek() + " - " + localDate));
    }
}