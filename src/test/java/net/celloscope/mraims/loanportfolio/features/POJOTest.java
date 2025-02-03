package net.celloscope.mraims.loanportfolio.features;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

class POJOTest {

    private POJO pojo = new POJO();

    @Test
    void contextLoading() {
        Assertions.assertNotNull(pojo);
    }

//    @Test
//    void getRepaymentDates() {
//        List<LocalDate> repaymentDates = pojo.getRepaymentDates(List.of(LocalDate.of(2023,4, 12), LocalDate.of(2023,5, 13)),
//                LocalDate.of(2023,1, 10),
//                DayOfWeek.SUNDAY,
//                30,
//                12,
//                "Monthly");
//
//        System.out.println(repaymentDates);
//    }
}