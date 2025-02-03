package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.dto.PassbookTransactionDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.PassbookTransactionEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IPassbookTransactionGatewayRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.gateway.IPassbookTransactionGateway;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PassbookTransactionGatewayAdapter implements IPassbookTransactionGateway {
    private final IPassbookTransactionGatewayRepository repository;
    private final ModelMapper mapper;

    public PassbookTransactionGatewayAdapter(IPassbookTransactionGatewayRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<PassbookTransactionDTO> getLastPassbookEntryForDepositAmountWithSavingsAccount(String savingsAccountId) {
        return repository.getLastPassbookEntryForDepositAmountWithSavingsAccount(savingsAccountId)
                .switchIfEmpty(Mono.just(PassbookTransactionEntity.builder().build()))
                .map(entity -> mapper.map(entity, PassbookTransactionDTO.class))
                .doOnNext(dto -> log.debug("Passbook Transaction DTO for Deposit Amount: {}", dto))
                ;
    }

    @Override
    public Mono<PassbookTransactionDTO> getLastPassbookEntryForWithdrawAmountWithSavingsAccount(String savingsAccountId) {
        return repository.getLastPassbookEntryForWithdrawAmountWithSavingsAccount(savingsAccountId)
                .switchIfEmpty(Mono.just(PassbookTransactionEntity.builder().build()))
                .map(entity -> mapper.map(entity, PassbookTransactionDTO.class))
                .doOnNext(dto -> log.debug("Passbook Transaction DTO for Withdraw Amount: {}", dto))
                ;
    }
}
