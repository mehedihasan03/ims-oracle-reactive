package net.celloscope.mraims.loanportfolio.features.savingsclosure.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountDto;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.SavingsClosureUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto.SavingsClosureCommand;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto.SavingsClosureDto;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.out.SavingsClosurePort;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.domain.SavingsClosure;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Component
@Slf4j
public class SavingsClosureService implements SavingsClosureUseCase {

    private final SavingsClosurePort port;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final PassbookUseCase passbookUseCase;
    private final IWithdrawStagingDataUseCase iWithdrawStagingDataUseCase;
    private final TransactionUseCase transactionUseCase;
    private final TransactionalOperator rxtx;

    public SavingsClosureService(SavingsClosurePort port,
                                 ISavingsAccountUseCase savingsAccountUseCase,
                                 ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                                 PassbookUseCase passbookUseCase,
                                 IWithdrawStagingDataUseCase iWithdrawStagingDataUseCase,
                                 TransactionUseCase transactionUseCase, TransactionalOperator rxtx) {
        this.port = port;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.passbookUseCase = passbookUseCase;
        this.iWithdrawStagingDataUseCase = iWithdrawStagingDataUseCase;
        this.transactionUseCase = transactionUseCase;
        this.rxtx = rxtx;
    }

    @Override
    public Mono<SavingsClosureDto> closeSavingsAccount(SavingsClosureCommand command) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
                .flatMap(managementProcessTracker ->
                        iWithdrawStagingDataUseCase.getWithdrawStagingDataBySavingsAccountId(command.getSavingsAccountId(), managementProcessTracker.getManagementProcessId())
                                .flatMap(stagingWithdrawData -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account already has pending withdrawal!")))
                                .then(createAndSaveSavingsClosure(command, managementProcessTracker)));
    }

    private Mono<SavingsClosureDto> createAndSaveSavingsClosure(SavingsClosureCommand command, ManagementProcessTracker managementProcessTracker) {
        return savingsAccountUseCase.getSavingsAccountInfoBySavingsAccountId(command.getSavingsAccountId())
                .flatMap(savingsAccountResponseDTO -> Mono.just(savingsAccountResponseDTO.getStatus())
                        .filter(status -> !status.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account is already closed!")))
                        .then(port.getSavingsClosureBySavingsAccountId(command.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(SavingsClosure.builder().build()))
                                .flatMap(savingsClosure -> savingsClosure.getSavingsAccountId() == null || savingsClosure.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) ?
                                        Mono.just(true) :
                                        Mono.just(false)
                                )
                                .flatMap(isSavingClosureExists -> isSavingClosureExists ?
                                        passbookUseCase.getLastPassbookEntryBySavingsAccount(savingsAccountResponseDTO.getSavingsAccountId())
                                                .doOnNext(passbookResponseDTO -> log.info("Passbook fetched from db : {}", passbookResponseDTO))
                                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Passbook Entry not found!")))
                                                .flatMap(passbookResponseDTO -> calculateAndBuildSavingsClosure(passbookResponseDTO, command)) :
                                        Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account is already closed!"))
                                )
                                .map(savingsClosure -> buildSavingsClosure(savingsClosure, savingsAccountResponseDTO, command, managementProcessTracker))
                        )
                        .flatMap(port::saveSavingsClosure)
                        .map(savingsClosure -> SavingsClosureDto.builder()
                                .userMessage("Savings Account closed successfully!")
                                .data(savingsClosure)
                                .build()));
    }

    private SavingsClosure buildSavingsClosure(SavingsClosure savingsClosure, SavingsAccountDto savingsAccountResponseDTO, SavingsClosureCommand command, ManagementProcessTracker managementProcessTracker) {
        savingsClosure.setSavingsAccountId(command.getSavingsAccountId());
        savingsClosure.setAcctStartDate(savingsAccountResponseDTO.getAcctStartDate());
        savingsClosure.setAcctEndDate(managementProcessTracker.getBusinessDate());
        savingsClosure.setAcctCloseDate(managementProcessTracker.getBusinessDate());
        savingsClosure.setPaymentMode(command.getPaymentMode());
        savingsClosure.setSavingsAmount(savingsAccountResponseDTO.getSavingsAmount());

        savingsClosure.setCreatedBy(command.getLoginId());
        savingsClosure.setCreatedOn(LocalDateTime.now());
        savingsClosure.setMemberId(savingsAccountResponseDTO.getMemberId());
        savingsClosure.setMemberNameEn(savingsAccountResponseDTO.getMemberNameEn());
        savingsClosure.setMemberNameBn(savingsAccountResponseDTO.getMemberNameBn());

        savingsClosure.setSavingsApplicationId(savingsAccountResponseDTO.getSavingsApplicationId());
        savingsClosure.setSavingsProductId(savingsAccountResponseDTO.getSavingsProductId());
        savingsClosure.setSavingsProdNameEn(savingsAccountResponseDTO.getSavingsProdNameEn());

        savingsClosure.setStatus(Status.STATUS_PENDING_APPROVAL.getValue());
        savingsClosure.setOfficeId(command.getOfficeId());
        savingsClosure.setManagementProcessId(managementProcessTracker.getManagementProcessId());
        savingsClosure.setSavingsTypeId(savingsAccountResponseDTO.getSavingsTypeId());

        return savingsClosure;
    }

    private Mono<SavingsClosure> calculateAndBuildSavingsClosure(PassbookResponseDTO lastPassbookEntry, SavingsClosureCommand command) {
        BigDecimal closingAmount = command.getClosingAmount() == null ? lastPassbookEntry.getSavgAcctEndingBalance() : command.getClosingAmount();
        BigDecimal endingBalance = lastPassbookEntry.getSavgAcctEndingBalance();
        if (closingAmount.compareTo(endingBalance) < 0) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Closing amount can not be less than balance!"));
        }
        BigDecimal closingInterest = endingBalance != null ? closingAmount.subtract(endingBalance) : BigDecimal.ZERO;
        BigDecimal totalAccruedInterDeposit = lastPassbookEntry.getTotalAccruedInterDeposit() == null ? BigDecimal.ZERO : lastPassbookEntry.getTotalAccruedInterDeposit();
        BigDecimal totalInterest = endingBalance != null ? totalAccruedInterDeposit.add(closingInterest) : BigDecimal.ZERO;

        return Mono.just(lastPassbookEntry)
                .map(lastPassbook -> SavingsClosure.builder()
                        .closingInterest(closingInterest)
                        .totalInterest(totalInterest)
                        .closingAmount(closingAmount)
                        .build());
    }

    @Override
    public Mono<SavingsClosureDto> authorizeSavingsClosure(SavingsClosureCommand command) {
        return validateSavingsAccount(command.getSavingsAccountId())
                .flatMap(valid -> valid
                        ? port.getSavingsClosureBySavingsAccountId(command.getSavingsAccountId())
                        .filter(savingsClosure -> savingsClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Closure Application Cannot be Authorized : " + command.getSavingsAccountId())))
                        .flatMap(savingsClosure -> buildTransactionAndCreatePassbookEntries(savingsClosure, command))
                        .flatMap(passbookResponseDTO -> updateSavingsClosureAndSavingsAccount(command.getSavingsAccountId(), command.getLoginId()))
                        .map(savingsClosure -> SavingsClosureDto.builder()
                                .userMessage("Savings Closure Authorization Successful.")
                                .data(savingsClosure)
                                .build())
                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Savings Closure Application not found for : " + command.getSavingsAccountId())))
                .as(this.rxtx::transactional);
    }

    private Mono<SavingsClosure> updateSavingsClosureAndSavingsAccount(String savingsAccountId, String loginId) {
        return port.updateSavingsClosureStatus(savingsAccountId, Status.STATUS_APPROVED.getValue(), loginId)
                .flatMap(savingsClosure -> savingsAccountUseCase.updateSavingsAccountStatusForSavingsClosure(savingsAccountId, savingsClosure.getAcctCloseDate(), Status.STATUS_CLOSED.getValue(), loginId)
                .doOnNext(savingsAccountResponseDTO -> log.info("Savings Account Status updated to : {}", savingsAccountResponseDTO.getStatus()))
                .doOnSuccess(responseDTO -> log.info("Savings Closure Status updated to : {}", responseDTO.getStatus()))
                .thenReturn(savingsClosure));
    }

    private Mono<PassbookResponseDTO> buildTransactionAndCreatePassbookEntries(SavingsClosure savingsClosure, SavingsClosureCommand command) {
        return buildTransactionForSavingsClosure(savingsClosure, command)
                .flatMap(transactionUseCase::createTransactionForSavingsClosure)
                .map(singleTransactionResponseDTO -> Tuples.of(singleTransactionResponseDTO, savingsClosure.getClosingInterest()))
                .flatMap(transactionInterestTuple -> {
                    if (transactionInterestTuple.getT2().compareTo(BigDecimal.ZERO) == 0) {
                        return passbookUseCase.createPassbookEntryForTermDepositClosure(
                                buildPassbookRequestDTOForSavingsClosure(transactionInterestTuple.getT1(), command, new AccruedInterestResponseDTO()));
                    }
                    return passbookUseCase.createPassbookEntryForInterestDeposit(
                                    buildPassbookRequestDTOForInterestPosting(transactionInterestTuple.getT1(), transactionInterestTuple.getT2(), command))
                            .flatMap(accruedInterestResponseDTO -> passbookUseCase.createPassbookEntryForTermDepositClosure(
                                    buildPassbookRequestDTOForSavingsClosure(transactionInterestTuple.getT1(), command, accruedInterestResponseDTO)));
                });
    }

    private PassbookRequestDTO buildPassbookRequestDTOForSavingsClosure(SingleTransactionResponseDTO transaction, SavingsClosureCommand command, AccruedInterestResponseDTO accruedInterestResponseDTO) {
        return PassbookRequestDTO
                .builder()
                .managementProcessId(transaction.getManagementProcessId())
                .processId(UUID.randomUUID().toString())
                .amount(transaction.getAmount())
                .savingsAccountId(transaction.getSavingsAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionCode(transaction.getTransactionCode())
                .loginId(command.getLoginId())
                .mfiId(transaction.getMfiId())
                .officeId(command.getOfficeId())
                .transactionDate(transaction.getTransactionDate())
                .paymentMode(transaction.getPaymentMode())
                .savgAcctBeginBalance(accruedInterestResponseDTO.getSavgAcctEndingBalance() == null ? BigDecimal.ZERO : accruedInterestResponseDTO.getSavgAcctEndingBalance())
                .totalDepositAmount(accruedInterestResponseDTO.getTotalDepositAmount() == null ? BigDecimal.ZERO : accruedInterestResponseDTO.getTotalDepositAmount())
                .totalAccruedInterDeposit(accruedInterestResponseDTO.getTotalAccruedInterDeposit() == null ? BigDecimal.ZERO : accruedInterestResponseDTO.getTotalAccruedInterDeposit())
                .build();
    }

    private PassbookRequestDTO buildPassbookRequestDTOForInterestPosting(SingleTransactionResponseDTO transactionResponseDTO, BigDecimal totalInterest, SavingsClosureCommand command) {
        PassbookRequestDTO requestDTO = PassbookRequestDTO
                .builder()
                .amount(totalInterest)
                .managementProcessId(transactionResponseDTO.getManagementProcessId())
                .processId(transactionResponseDTO.getProcessId() )
                .savingsAccountId(transactionResponseDTO.getSavingsAccountId())
                .transactionId(transactionResponseDTO.getTransactionId())
                .transactionCode(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                .mfiId(transactionResponseDTO.getMfiId())
                .loginId(command.getLoginId())
                .officeId(command.getOfficeId())
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .paymentMode(transactionResponseDTO.getPaymentMode())
                .memberId(transactionResponseDTO.getMemberId())
                .build();

        log.info("Passbook Request DTO For Interest posting : {}", requestDTO);
        return requestDTO;
    }

    Mono<Transaction> buildTransactionForSavingsClosure(SavingsClosure savingsClosure, SavingsClosureCommand command) {
        return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
                .map(managementProcessId -> Transaction
                        .builder()
                        .managementProcessId(managementProcessId)
                        .processId(UUID.randomUUID().toString())
                        .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                        .savingsAccountId(savingsClosure.getSavingsAccountId())
                        .transactionId(UUID.randomUUID().toString())
                        .memberId(savingsClosure.getMemberId())
                        .amount(savingsClosure.getClosingAmount())
                        .transactionCode(TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())
                        .mfiId(command.getMfiId())
                        .officeId(command.getOfficeId())
                        .transactionDate(savingsClosure.getAcctCloseDate())
                        .transactedBy(command.getLoginId())
                        .createdOn(LocalDateTime.now())
                        .createdBy(command.getLoginId())
                        .paymentMode(savingsClosure.getPaymentMode())
                        .status(Status.STATUS_APPROVED.getValue())
                        .build());
    }

    Mono<Boolean> validateSavingsAccount(String savingsAccountId) {
        return savingsAccountUseCase.getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(SavingsAccountResponseDTO::getStatus)
                .filter(status -> !status.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account already closed!.")))
                .then(port.checkIfSavingsClosureExistsBySavingsAccountId(savingsAccountId));
    }

    @Override
    public Mono<SavingsClosureDto> rejectSavingsClosure(SavingsClosureCommand command) {
        return port.getSavingsClosureBySavingsAccountId(command.getSavingsAccountId())
                .doOnRequest(l -> log.info("requesting to reject Savings closure application for savings account id : {}", command.getSavingsAccountId()))
                .filter(savingsClosure -> savingsClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Closure Application Cannot be Rejected : " + command.getSavingsAccountId())))
                .map(savingsClosure -> {
                    savingsClosure.setStatus(Status.STATUS_REJECTED.getValue());
                    savingsClosure.setRejectedBy(command.getLoginId());
                    savingsClosure.setRejectedOn(LocalDateTime.now());
                    savingsClosure.setRemarks(command.getRemarks());
                    return savingsClosure;
                })
                .flatMap(port::saveSavingsClosure)
                .map(savingsClosure -> SavingsClosureDto
                        .builder()
                        .userMessage("DPS Closure Rejected Successfully.")
                        .data(savingsClosure)
                        .build());
    }
}
