package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.out;

import net.celloscope.mraims.loanportfolio.features.welfarefund.domain.WelfareFund;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface WelfareFundPersistencePort {

    Flux<WelfareFund> getWelfareFundByOfficeId(String officeId, long limit, long offset);

    Mono<WelfareFund> getWelfareFundByBusinessDate(String loanAccountId, String officeId, LocalDate businessDate);

    Mono<WelfareFund> getWelfareFundByLoanAccountIdAndTransactionDate(String loanAccountId, LocalDate transactionDate);

    Mono<WelfareFund> saveCollectedFundData(WelfareFund welfareFund);

    Flux<WelfareFund> getWelfareFundByLoanAccountId(String loanAccountId);

    Mono<WelfareFund> authorizeWelfareFundData(String loanAccountId, LocalDate parse, String loginId);

    Mono<WelfareFund> rejectWelfareFundData(String loanAccountId, LocalDate parse, String loginId);

    Flux<WelfareFund> getPendingWelfareFundByOfficeId(String officeId);

    Flux<WelfareFund> getAllWelfareFundTransactionForOfficeOnABusinessDay(String managementProcessId, String officeId);

    Mono<WelfareFund> getWelfareFundByOid(String id);

    Mono<String> deleteWelfareFundData(String id);
}
