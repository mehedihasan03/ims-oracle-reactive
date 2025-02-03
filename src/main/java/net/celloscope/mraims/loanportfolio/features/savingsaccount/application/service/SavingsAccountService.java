package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.out.PassbookPersistencePort;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.PassbookService;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.out.ISavingsAccountPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.out.TransactionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Slf4j
@Service
public class SavingsAccountService implements ISavingsAccountUseCase {

    private final ISavingsAccountPersistencePort port;
    private PassbookUseCase passbookUseCase;
    private final PassbookPersistencePort passbookPersistencePort;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final TransactionPersistencePort transactionPersistencePort;
    private final TransactionalOperator rxtx;


    public SavingsAccountService(ISavingsAccountPersistencePort port, @Lazy PassbookUseCase passbookUseCase, PassbookPersistencePort passbookPersistencePort, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, TransactionPersistencePort transactionPersistencePort, TransactionalOperator rxtx) {
        this.port = port;
        this.passbookUseCase = passbookUseCase;
        this.passbookPersistencePort = passbookPersistencePort;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.transactionPersistencePort = transactionPersistencePort;
        this.rxtx = rxtx;
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingsAccountFluxByMemberId(String memberId) {
        return port.getSavingsAccountFluxByMemberId(memberId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> getSavingsAccountDetailsBySavingsAccountId(String savingsAccountId) {
        return port
                .getSavingsAccountBySavingsAccountId(savingsAccountId)
                .doOnRequest(value -> log.info("Requesting to get savings account : {}", savingsAccountId));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, LocalDate transactionDate, String loginId) {
        return port.updateSavingsAccountStatus(savingsAccountId, status, transactionDate, loginId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountStatus(String savingsAccountId, String status, LocalDate activationDate, LocalDate closingDate) {
        return port.updateFDRSavingsAccountStatus(savingsAccountId, status, activationDate, closingDate);
    }

    @Override
    public Mono<String> getProductIdBySavingsAccountId(String savingsAccountId) {
        return port.getProductIdBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountOnMaturity(String savingsAccountId) {
        return port.updateFDRSavingsAccountOnMaturity(savingsAccountId);
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getAllFDRAccountsEligibleForInterestPosting(LocalDate lastBusinessDate, LocalDate currentBusinessDate) {
        return port.getAllFDRAccountsEligibleForInterestPosting(lastBusinessDate, currentBusinessDate, SavingsProductType.SAVINGS_TYPE_ID_FDR.getValue());
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingAccountForStagingAccountDataByMemberIdList(List<String> memberIdList) {
        return port.getSavingAccountByMemberIdList(memberIdList);
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingsAccountsByOfficeId(String officeId) {
        return port
                .getSavingsAccountsByOfficeIdAndStatus(officeId, Status.STATUS_ACTIVE.getValue())
                .doOnRequest(l -> log.info("Request received to get savings accounts by office : {} | status : {}", officeId, Status.STATUS_ACTIVE.getValue()));
    }

    @Override
    public Flux<FDRAccountDTO> getFDRSavingsAccountsByOfficeId(String officeId) {
        List<String> statusList = List.of(Status.STATUS_ACTIVE.getValue(), Status.STATUS_MATURED.getValue(), Status.STATUS_CLOSED.getValue());
        return port
                .getFDRSavingsAccountsByOfficeIdAndStatus(officeId, statusList)
                .collectList()
                .doOnNext(list -> log.info("Total FDR accounts found : {}", list.size()))
                .flatMapMany(Flux::fromIterable)
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No FDR account found.")))
                .doOnRequest(l -> log.info("Requesting to get FDR accounts by officeId : {} & status : {}", officeId, statusList))
                .doOnComplete(() -> log.info("Successfully fetched FDR accounts."));
    }

    @Override
    public Mono<FDRAccountDTO> getFDRAccountDetailsBySavingsAccountId(String savingsAccountId) {
        return port.getFDRAccountDetailsBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No FDR account found.")))
                .doOnRequest(l -> log.info("Requesting to get FDR account details by account id : {}", savingsAccountId))
                .doOnSuccess(fdrAccountDTO -> log.info("Successfully fetched FDR account details."));
    }

    @Override
    public Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeId(String officeId) {
        List<String> statusList = List.of(Status.STATUS_ACTIVE.getValue(), Status.STATUS_MATURED.getValue(), Status.STATUS_CLOSED.getValue());
        return port
                .getDPSSavingsAccountsByOfficeId(officeId, statusList)
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No DPS account found.")))
                .doOnRequest(l -> log.info("Requesting to get DPS accounts by officeId : {} & status : {}", officeId, statusList))
                .doOnComplete(() -> log.info("Successfully fetched DPS accounts."));
    }

    @Override
    public Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndSearchText(String officeId, String searchText) {
        List<String> statusList = List.of(Status.STATUS_ACTIVE.getValue(), Status.STATUS_MATURED.getValue(), Status.STATUS_CLOSED.getValue());
        return port
                .getDPSSavingsAccountsByOfficeIdAndSearchText(officeId, searchText, statusList)
                .doOnRequest(l -> log.info("Requesting to get DPS accounts by officeId : {} & searchText: {} & status : {}", officeId, searchText, statusList))
                .doOnComplete(() -> log.info("Successfully fetched DPS accounts."));
    }

    @Override
    public Mono<DPSAccountDTO> getDPSAccountDetailsBySavingsAccountId(String savingsAccountId) {
        return port.getDPSAccountDetailsBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("Requesting to get DPS account details by account id : {}", savingsAccountId.trim()))
//                .doOnNext(dpsAccountDTO -> log.info("DPS account : {}", dpsAccountDTO))
                .doOnSuccess(dpsAccountDTO -> log.info("Successfully fetched DPS account details."))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No DPS account found.")))
                .flatMap(dpsAccountDTO -> passbookPersistencePort
                        .getLastPassbookEntryBySavingsAccountId(dpsAccountDTO.getSavingsAccountId())
                        .switchIfEmpty(passbookPersistencePort.getLastPassbookEntryBySavingsAccountOid(dpsAccountDTO.getSavingsAccountOid()))
                        .switchIfEmpty(Mono.just(Passbook.builder().build()))
                        .map(passbook -> {
                            if (passbook.getSavgAcctEndingBalance() == null) {
                                dpsAccountDTO.setBalance(BigDecimal.ZERO);
                            } else {
                                dpsAccountDTO.setBalance(passbook.getSavgAcctEndingBalance());
                            }
                            return dpsAccountDTO;
                        }));

    }

    @Override
    public Mono<SavingsAccountDto> getSavingsAccountInfoBySavingsAccountId(String savingsAccountId) {
        return port.getSavingsAccountInfoBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account found.")))
                .flatMap(savingsAccountDto -> passbookPersistencePort.getLastPassbookEntryBySavingsAccountId(savingsAccountDto.getSavingsAccountId())
                        .switchIfEmpty(passbookPersistencePort.getLastPassbookEntryBySavingsAccountOid(savingsAccountDto.getSavingsAccountOid()))
                        .switchIfEmpty(Mono.just(Passbook.builder().build()))
                        .map(passbook -> {
                            if (passbook.getSavgAcctEndingBalance() == null) {
                                savingsAccountDto.setBalance(BigDecimal.ZERO);
                            } else {
                                savingsAccountDto.setBalance(passbook.getSavgAcctEndingBalance());
                            }
                            return savingsAccountDto;
                        }));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, String loginId) {
        return port.updateSavingsAccountStatus(savingsAccountId, status, loginId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatusForSavingsClosure(String savingsAccountId, LocalDate closingDate, String status, String loginId) {
        return port.updateSavingsAccountStatusForSavingsClosure(savingsAccountId, closingDate, status, loginId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountInterestPostingDatesAndStartDateEndDate(List<LocalDate> interestPostingDates, String savingsAccountId, LocalDate acctStartDate, LocalDate acctEndDate, String loginId) {
        return port.updateSavingsAccountInterestPostingDates(interestPostingDates, savingsAccountId, acctStartDate, acctEndDate, loginId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRDPSAccountMaturityAmount(String savingsAccountId, BigDecimal maturityAmount) {
        return port.updateFDRDPSAccountMaturityAmount(savingsAccountId, maturityAmount);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountBalance(String savingsAccountId, BigDecimal balance, String status) {
        return port
                .updateSavingsAccountBalance(savingsAccountId, balance, status)
                .doOnSuccess(savingsAccountResponseDTO -> log.info("Successfully updated savings account: {} | balance : {}", savingsAccountId, balance));
    }

    @Override
    public Mono<SavingsAccountActivationResponseDto> activateSavingsAccountWIthOpeningBalance(SavingsAccountActivationRequestDto requestDto) {
        String processId = UUID.randomUUID().toString();
        return port.getSavingsAccountBySavingsAccountId(requestDto.getSavingsAccountId())
                .doOnSuccess(savingsAccount -> log.info("Savings account found : {}", savingsAccount))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Savings account not found.")))
                .flatMap(savingsAccount -> validateSavingsAccount(savingsAccount, requestDto))
                .doOnSuccess(savingsAccount -> log.info("Savings account validated : {}", savingsAccount))
                .flatMap(savingsAccount -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId()).zipWith(Mono.just(savingsAccount)))
                .doOnSuccess(tuple -> log.info("Management process tracker found : {}", tuple.getT1()))
                .flatMap(managementProcessTrackerAndSavingsAccount ->
                        transactionPersistencePort.saveSingleTransactionToDB(buildTransactionForSavingsAccountActivation(requestDto, managementProcessTrackerAndSavingsAccount.getT1(), processId))
//                                .flatMap(singleTransactionResponseDTO -> passbookUseCase.createPassbookEntryForSavings(buildPassbookRequestDTOForSavingsAccountActivation(managementProcessTrackerAndSavingsAccount.getT2(), singleTransactionResponseDTO, requestDto.getOfficeId())))
                                .flatMap(singleTransactionResponseDTO -> passbookUseCase.createPassbookEntryForSavings(buildPassbookRequestDTOForSavingsAccountActivation(singleTransactionResponseDTO)))
                                .then(port.updateSavingsAccountStatus(requestDto.getSavingsAccountId(), Status.STATUS_ACTIVE.getValue(), requestDto.getLoginId()))
                )
                .as(rxtx::transactional)
                .doOnSuccess(savingsAccountResponseDTO -> log.info("Savings account activated successfully."))
                .map(savingsAccountResponseDTO -> SavingsAccountActivationResponseDto.builder()
                        .userMessage("Savings account activated successfully.")
                        .savingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                        .build())
                .doOnError(throwable -> log.error("Error occurred while activating savings account with opening balance: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while activating savings account with opening balance.")))
                .onErrorResume(ExceptionHandlerUtil.class::isInstance, Mono::error);
    }

//    private Passbook buildPassbookRequestDTOForSavingsAccountActivation(SavingsAccountResponseDTO savingsAccountResponseDTO, SingleTransactionResponseDTO transactionResponseDTO, String officeId) {
//        return Passbook.builder()
//                .managementProcessId(transactionResponseDTO.getManagementProcessId())
//                .processId(transactionResponseDTO.getProcessId())
//                .savingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
//                .transactionDate(transactionResponseDTO.getTransactionDate())
//                .transactionId(transactionResponseDTO.getTransactionId())
//                .transactionCode(transactionResponseDTO.getTransactionCode())
//                .mfiId(transactionResponseDTO.getMfiId())
//                .officeId(officeId)
//                .memberId(savingsAccountResponseDTO.getMemberId())
//                .passbookNumber("Passbook-" + savingsAccountResponseDTO.getMemberId())
//                .depositAmount(transactionResponseDTO.getAmount())
//                .savgAcctBeginBalance(BigDecimal.ZERO)
//                .savgAcctEndingBalance(transactionResponseDTO.getAmount())
//                .savingsAvailableBalance(transactionResponseDTO.getAmount().subtract(savingsAccountResponseDTO.getMinBalance()).compareTo(BigDecimal.ZERO) > 0
//                        ? transactionResponseDTO.getAmount().subtract(savingsAccountResponseDTO.getMinBalance())
//                        : BigDecimal.ZERO)
//                .createdOn(LocalDateTime.now())
//                .createdBy(transactionResponseDTO.getLoginId())
//                .status(Status.STATUS_ACTIVE.getValue())
//                .totalDepositAmount(transactionResponseDTO.getAmount())
//                .paymentMode(transactionResponseDTO.getPaymentMode())
//                .savingsAccountOid(savingsAccountResponseDTO.getOid())
//                .createdBy(transactionResponseDTO.getLoginId())
//                .build();
//    }

    private PassbookRequestDTO buildPassbookRequestDTOForSavingsAccountActivation(SingleTransactionResponseDTO transactionResponseDTO) {
        return PassbookRequestDTO
                .builder()
                .amount(transactionResponseDTO.getAmount())
                .managementProcessId(transactionResponseDTO.getManagementProcessId() != null
                        ? transactionResponseDTO.getManagementProcessId()
                        : null)
                .processId(transactionResponseDTO.getProcessId() != null
                        ? transactionResponseDTO.getProcessId()
                        : null)
                .savingsAccountId(transactionResponseDTO.getSavingsAccountId())
                .transactionId(transactionResponseDTO.getTransactionId())
                .transactionCode(transactionResponseDTO.getTransactionCode())
                .mfiId(transactionResponseDTO.getMfiId())
                .loginId(transactionResponseDTO.getLoginId())
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .paymentMode(transactionResponseDTO.getPaymentMode())
                .memberId(transactionResponseDTO.getMemberId())
                .build();
    }

    private Transaction buildTransactionForSavingsAccountActivation(SavingsAccountActivationRequestDto requestDto, ManagementProcessTracker managementProcessTracker, String processId) {
        return Transaction
                .builder()
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .processId(processId)
                .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                .savingsAccountId(requestDto.getSavingsAccountId())
                .transactionId(UUID.randomUUID().toString())
                .memberId(requestDto.getMemberId())
                .amount(requestDto.getOpeningBalance())
                .transactionCode(TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .transactionDate(managementProcessTracker.getBusinessDate())
                .transactedBy(requestDto.getLoginId())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())
                .paymentMode(PAYMENT_MODE_CASH.getValue())
                .status(Status.STATUS_APPROVED.getValue())
                .build();
    }

    private Mono<SavingsAccountResponseDTO> validateSavingsAccount(SavingsAccountResponseDTO savingsAccount, SavingsAccountActivationRequestDto requestDto) {
        return Mono.just(savingsAccount)
                .doOnNext(savingsAccountResponseDTO -> log.info("Validating savings account : {}", savingsAccountResponseDTO))
                .filter(savingsAccountResponseDTO -> savingsAccountResponseDTO.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings account is not approved.")))
                .filter(savingsAccountResponseDTO -> savingsAccountResponseDTO.getOpeningBalance().compareTo(BigDecimal.ZERO) > 0 && savingsAccountResponseDTO.getOpeningBalance().compareTo(requestDto.getOpeningBalance()) == 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Opening balance is not valid.")))
                .doOnError(throwable -> log.error("Error occurred while validating savings account : {}", throwable.getMessage()));
    }
}
