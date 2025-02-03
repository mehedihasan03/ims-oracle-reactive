package net.celloscope.mraims.loanportfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Slf4j
@Component
@EnableScheduling
public class LoanPortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanPortfolioApplication.class, args);
    }

}
