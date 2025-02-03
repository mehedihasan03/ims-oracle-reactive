package net.celloscope.mraims.loanportfolio.features.withdraw.application.service;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.AccountingUseCase;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.AuthorizeWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.AuthorizeWithdrawCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.AuthorizeWithdrawResponseDTO;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out.WithdrawPersistencePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Predicate;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@Slf4j
public class AuthorizeWithdrawService implements AuthorizeWithdrawUseCase {

    private final TransactionalOperator rxtx;
    private final WithdrawPersistencePort withdrawPersistencePort;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final CommonRepository commonRepository;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final AccountingUseCase accountingUseCase;

    public AuthorizeWithdrawService(
            TransactionalOperator rxtx,
            WithdrawPersistencePort withdrawPersistencePort,
            TransactionUseCase transactionUseCase,
            PassbookUseCase passbookUseCase, CommonRepository commonRepository, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, AccountingUseCase accountingUseCase) {
        this.commonRepository = commonRepository;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.accountingUseCase = accountingUseCase;
        this.rxtx = rxtx;
        this.withdrawPersistencePort = withdrawPersistencePort;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
    }

    @Override
    public Mono<AuthorizeWithdrawResponseDTO> authorizeWithdraw(AuthorizeWithdrawCommand command) {
        return withdrawPersistencePort
                .updateAllWithdrawStagedDataBy(command.getSamityId(), command.getWithdrawType(), command.getLoginId())
                .doOnRequest(r -> log.info("Request Received for withdraw authorization in service"))
                .doOnError(throwable -> log.error("Failed to save withdraw data : {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, ExceptionMessages.SOMETHING_WENT_WRONG.getValue())))
                .flatMapMany(integer -> transactionUseCase.createTransactionForWithdrawBySamityId(UUID.randomUUID().toString(), command.getSamityId(), UUID.randomUUID().toString(), "office-Id")
                    .flatMapMany(transactionResponseDTO -> Flux.fromIterable(transactionResponseDTO.getTransactionList())
                        .flatMap(transaction -> passbookUseCase.createPassbookEntryForSavingsWithdraw(buildPassbookRequestDTO(transaction, command)))))
                .collectList()
                /*.flatMap(passbookResponseDTOList -> commonRepository
                        .getSamityBySamityID(command.getSamityId())
                        .doOnRequest(l -> log.info("requesting to get samity entity by samity id : {}", command.getSamityId()))
                        .doOnNext(samity -> log.info("Got Office id for samity : {}", samity.officeId))
                        .flatMap(samity -> managementProcessTrackerUseCase
                                .getLastManagementProcessIdForOffice(samity.officeId)
                                .doOnNext(s -> log.info("last management process id received : {}", s))
                                .flatMap(managementProcessId -> accountingUseCase
                                        .getAccountingJournalBody(buildAccountingRequestDTO(managementProcessId, command, samity.officeId))
                                        .doOnRequest(l -> log.info("requesting to get accounting journal body for withdraw.")))
                                .thenReturn(passbookResponseDTOList)))*/
                .as(this.rxtx::transactional)
                .map(passbookResponseDTOList -> buildResponse(command.getSamityId()));
    }

    private PassbookRequestDTO buildPassbookRequestDTO(Transaction transaction, AuthorizeWithdrawCommand command) {
        PassbookRequestDTO passbookRequestDTO =
                PassbookRequestDTO
                .builder()
                .amount(transaction.getAmount())
                .loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
                .savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
                .transactionId(transaction.getTransactionId())
                .transactionCode(transaction.getTransactionCode())
                .loginId(command.getLoginId())
                .transactionDate(transaction.getTransactionDate())
                .paymentMode(transaction.getPaymentMode())
                .samityId(transaction.getSamityId())
                .build();

        log.info("built PassbookRequestDTO : {}", passbookRequestDTO);
        return passbookRequestDTO;
    }

    private AccountingRequestDTO buildAccountingRequestDTO(String managementProcessId, AuthorizeWithdrawCommand command, String officeId) {
        AccountingRequestDTO accountingRequestDTO = AccountingRequestDTO
                .builder()
                .managementProcessId(managementProcessId)
                .processName(AisMetaDataEnum.PROCESS_NAME_WITHDRAW.getValue())
                .mfiId(command.getMfiId())
                .loginId(command.getLoginId())
                .officeId(officeId)
                .build();

        log.info("built AccountingRequestDTO for withdraw : {}", accountingRequestDTO);
        return accountingRequestDTO;
    }

    private AuthorizeWithdrawResponseDTO buildResponse(String samityId) {
        return AuthorizeWithdrawResponseDTO
                .builder()
                .userMessage("Authorization of Withdraw is successful for SamityId : " + samityId)
                .build();
    }
}
