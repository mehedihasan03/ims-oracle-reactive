package net.celloscope.mraims.loanportfolio.features.transaction.domain.commands;

import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberSamityOfficeEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.CollectionDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.SplitTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.StagingDataDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto.WithdrawDataDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ITransactionCommands {
    Flux<Transaction> buildTransaction(CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO);
    Flux<Transaction> buildTransactionV2(CollectionDataDTO collectionDataDTO, StagingDataDTO stagingDataDTO, SplitTransactionDTO splitTransactionDTO);
    Mono<Transaction> buildTransactionForWithdraw(WithdrawDataDTO withdrawDataDTO, StagingDataDTO stagingDataDTO, String managementProcessId, String transactionProcessId, String officeId);
    Mono<Transaction> buildTransactionForInterestDeposit(String loginId, StagingDataEntity stagingDataEntity, String savingsAccountId, BigDecimal accruedInterest, LocalDate interestPostingDate, String officeId);
    Mono<Transaction> buildTransactionForDisbursement(String disbursementLoanAccountId, BigDecimal loanAmount, LocalDate disbursementDate, String memberId, String mfiId, String loginId, String managementProcessId, String officeId, String source, MemberSamityOfficeEntity memberSamityOfficeEntity);
    Mono<Transaction> buildTransactionForFDRActivation(String savingsAccountId, BigDecimal fdrAmount, LocalDate activationDate, String memberId, String mfiId, String loginId, String samityId);
}
