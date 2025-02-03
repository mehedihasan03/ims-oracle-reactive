package net.celloscope.mraims.loanportfolio.features.transaction.domain.commands;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberSamityOfficeEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.CollectionDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.SplitTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.StagingDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.WithdrawDataDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TransactionCommands implements ITransactionCommands {

    @Override
    public Flux<Transaction> buildTransaction(CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO) {

        String transactionCode = collectionDataDTO.getAccountType().equals(ACCOUNT_TYPE_LOAN.getValue())
                ? TRANSACTION_CODE_LOAN_REPAY.getValue()
                : TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue();

        return Flux.just(
                Transaction
                        .builder()
                        .managementProcessId(stagingDataDTO.getManagementProcessId())
                        .stagingDataId(collectionDataDTO.getStagingDataId())
                        .accountType(collectionDataDTO.getAccountType())
                        .transactionId(UUID.randomUUID().toString())
                        .processId(stagingDataDTO.getProcessId())
                        .memberId(stagingDataDTO.getMemberId())
                        .loanAccountId(collectionDataDTO.getLoanAccountId() == null ? null
                                : collectionDataDTO.getLoanAccountId())
                        .savingsAccountId(collectionDataDTO.getSavingsAccountId() == null ? null
                                : collectionDataDTO.getSavingsAccountId())
                        .amount(collectionDataDTO.getAmount())
                        .collectionType(collectionDataDTO.getCollectionType())
                        .transactionCode(transactionCode)
                        .paymentMode(collectionDataDTO.getPaymentMode())
                        .mfiId(stagingDataDTO.getMfiId())
                        .transactionDate(stagingDataDTO.getBusinessDate())
                        .transactedBy(collectionDataDTO.getCreatedBy())
                        .createdOn(collectionDataDTO.getApprovedOn())
                        .createdBy(collectionDataDTO.getApprovedBy())
                        .status(Status.STATUS_APPROVED.getValue())
                        .samityId(stagingDataDTO.getSamityId())
                        .build());
    }

    @Override
    public Flux<Transaction> buildTransactionV2(CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO,
            SplitTransactionDTO splitTransactionDTO) {
        log.info("buildTransactionV2 called");
        List<BigDecimal> listOfPaidAmountPrincipalSC = splitTransactionIntoPrincipalAndSC(collectionDataDTO.getAmount(),
                splitTransactionDTO);
        log.info("listOfPaidAmountPrincipalSC : {}", listOfPaidAmountPrincipalSC);
        return buildTransactionForLoanRepay(listOfPaidAmountPrincipalSC, collectionDataDTO, stagingDataDTO);
    }

    private Flux<Transaction> buildTransactionForLoanRepay(List<BigDecimal> listOfPaidAmountPrincipalSC,
            CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO) {
        List<String> transactionCodeList = List.of(TRANSACTION_CODE_LOAN_REPAY.getValue(),
                TRANSACTION_CODE_LOAN_REPAY_PRIN.getValue(), TRANSACTION_CODE_LOAN_REPAY_SC.getValue());
        List<Transaction> transactionList = new ArrayList<>();
        for (int i = 0; i < listOfPaidAmountPrincipalSC.size(); i++) {
            transactionList.add(buildTransactionForLoanRepay(collectionDataDTO, stagingDataDTO,
                    transactionCodeList.get(i), listOfPaidAmountPrincipalSC.get(i)));
        }

//        log.info("TransactionList : {}", transactionList);
        return Flux.fromIterable(transactionList);
    }

    private Transaction buildTransactionForLoanRepay(CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO,
            String transactionCode, BigDecimal amount) {
        return Transaction
                .builder()
                .managementProcessId(stagingDataDTO.getManagementProcessId())
                .stagingDataId(collectionDataDTO.getStagingDataId())
                .accountType(collectionDataDTO.getAccountType())
                .transactionId(UUID.randomUUID().toString())
                .processId(stagingDataDTO.getProcessId())
                .memberId(stagingDataDTO.getMemberId())
                .loanAccountId(
                        collectionDataDTO.getLoanAccountId() == null ? null : collectionDataDTO.getLoanAccountId())
                .savingsAccountId(collectionDataDTO.getSavingsAccountId() == null ? null
                        : collectionDataDTO.getSavingsAccountId())
                .amount(amount)
                .collectionType(collectionDataDTO.getCollectionType())
                .transactionCode(transactionCode)
                .paymentMode(collectionDataDTO.getPaymentMode())
                .mfiId(stagingDataDTO.getMfiId())
//                .transactionDate(collectionDataDTO.getCreatedOn().toLocalDate())
                .transactionDate(stagingDataDTO.getBusinessDate())
                .transactedBy(collectionDataDTO.getCreatedBy())
                // TODO: 7/4/23 from where will these values be provided?
                /*
                 * .postedOn()
                 * .postedBy()
                 */
                .createdOn(collectionDataDTO.getApprovedOn())
                .createdBy(collectionDataDTO.getApprovedBy())
                .status(Status.STATUS_APPROVED.getValue())
                /*
                 * .updatedOn()
                 * .updatedBy()
                 * .deleted()
                 */
                .build();

    }

    private List<BigDecimal> splitTransactionIntoPrincipalAndSC(BigDecimal paidAmount,
            SplitTransactionDTO splitTransactionDTO) {
        BigDecimal beginPrinBalance = splitTransactionDTO.getBeginPrinBalance() == null
                ? splitTransactionDTO.getLoanAmount()
                : splitTransactionDTO.getBeginPrinBalance();
        BigDecimal serviceChargePerPeriod = splitTransactionDTO.getServiceChargeRatePerPeriod();
        BigDecimal installmentAmount = splitTransactionDTO.getInstallmentAmount();
        BigDecimal remainingAmount = paidAmount;
        BigDecimal principalPaid = BigDecimal.ZERO;
        BigDecimal scPaid = BigDecimal.ZERO;

        while (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal serviceChargeAmount = CommonFunctions.round((beginPrinBalance.multiply(serviceChargePerPeriod)),
                    2, RoundingMode.HALF_UP);
            BigDecimal principalAmount;
            if (remainingAmount.compareTo(installmentAmount) > 0) {
                principalAmount = installmentAmount.subtract(serviceChargeAmount);
            } else {
                principalAmount = remainingAmount.subtract(serviceChargeAmount);
            }

            if (remainingAmount.compareTo(serviceChargeAmount) < 0) {
                scPaid = scPaid.add(remainingAmount);
            } else {
                scPaid = scPaid.add(serviceChargeAmount);
                principalPaid = principalPaid.add(principalAmount);
            }

            remainingAmount = remainingAmount.subtract(installmentAmount);
            remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;
            beginPrinBalance = beginPrinBalance.subtract(principalAmount);
        }

        return List.of(paidAmount, principalPaid, scPaid);

    }

    @Override
    public Mono<Transaction> buildTransactionForWithdraw(WithdrawDataDTO withdrawDataDTO,
                                                         StagingDataDTO stagingDataDTO, String managementProcessId, String transactionProcessId, String officeId) {
        return Mono.just(
                Transaction
                        .builder()
                        .managementProcessId(managementProcessId)
                        .stagingDataId(withdrawDataDTO.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                        .transactionId(UUID.randomUUID().toString())
                        .processId(transactionProcessId != null ? transactionProcessId : stagingDataDTO.getProcessId())
                        .memberId(stagingDataDTO.getMemberId())
                        .savingsAccountId(withdrawDataDTO.getSavingsAccountId())
                        .amount(withdrawDataDTO.getAmount())
                        .withdrawType(withdrawDataDTO.getWithdrawType())
                        .transactionCode(TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())
                        .paymentMode(withdrawDataDTO.getPaymentMode())
                        .mfiId(stagingDataDTO.getMfiId())
                        .transactionDate(stagingDataDTO.getBusinessDate())
                        .transactedBy(withdrawDataDTO.getCreatedBy())
                        .createdOn(withdrawDataDTO.getApprovedOn())
                        .createdBy(withdrawDataDTO.getApprovedBy())
                        .status(Status.STATUS_APPROVED.getValue())
                        .collectionType(withdrawDataDTO.getWithdrawType())
                        .officeId(officeId)
                        .samityId(stagingDataDTO.getSamityId())
                        .build());
    }

    @Override
    public Mono<Transaction> buildTransactionForInterestDeposit(String loginId, StagingDataEntity stagingData,
                                                                String savingsAccountId, BigDecimal accruedInterest, LocalDate interestPostingDate, String officeId) {
        return Mono.just(Transaction
                .builder()
                .stagingDataId(stagingData.getStagingDataId())
                .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                .transactionId(UUID.randomUUID().toString())
                .processId(stagingData.getProcessId())
                .managementProcessId(stagingData.getManagementProcessId())
                .memberId(stagingData.getMemberId())
                .savingsAccountId(savingsAccountId)
                .amount(accruedInterest)
                .transactionCode(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                .mfiId(stagingData.getMfiId())
                .transactionDate(interestPostingDate)
                .transactedBy(loginId)
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .paymentMode(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                .status(Status.STATUS_APPROVED.getValue())
                .officeId(officeId)
                .samityId(stagingData.getSamityId())
                .build());
    }

    @Override
    public Mono<Transaction> buildTransactionForDisbursement(String disbursementLoanAccountId, BigDecimal loanAmount, LocalDate disbursementDate, String memberId, String mfiId, String loginId, String managementProcessId, String officeId, String source, MemberSamityOfficeEntity memberSamityOfficeEntity) {
        return Mono.just(Transaction
                .builder()
                .accountType(ACCOUNT_TYPE_LOAN.getValue())
                .transactionId(UUID.randomUUID().toString())
                .memberId(memberId)
                .loanAccountId(disbursementLoanAccountId)
                .amount(loanAmount)
                .transactionCode(TRANSACTION_CODE_LOAN_DISBURSEMENT.getValue())
                .mfiId(mfiId)
                .transactionDate(disbursementDate)
                .transactedBy(loginId)
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .paymentMode(PAYMENT_MODE_CASH.getValue())
                .status(Status.STATUS_APPROVED.getValue())
                .managementProcessId(managementProcessId)
                .processId(UUID.randomUUID().toString())
                .officeId(officeId)
                .source(source)
                .samityId(memberSamityOfficeEntity.getSamityId())
                .build());
    }

    @Override
    public Mono<Transaction> buildTransactionForFDRActivation(String savingsAccountId, BigDecimal fdrAmount,
            LocalDate activationDate, String memberId, String mfiId, String loginId, String samityId) {
        return Mono.just(Transaction
                .builder()
                .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                .transactionId(UUID.randomUUID().toString())
                .memberId(memberId)
                .savingsAccountId(savingsAccountId)
                .amount(fdrAmount)
                .transactionCode(TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue())
                .mfiId(mfiId)
                .transactionDate(activationDate)
                .transactedBy(loginId)
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .paymentMode(PAYMENT_MODE_CASH.getValue())
                .status(Status.STATUS_APPROVED.getValue())
                .samityId(samityId)
                .build());
    }
}