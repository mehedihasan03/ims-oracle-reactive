package net.celloscope.mraims.loanportfolio.features.withdraw.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.StageWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.StageWithdrawCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.WithdrawRequestDto;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StageWithdrawResponseDTO;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StagingWithdrawDataDTO;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out.WithdrawPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdraw.domain.Withdraw;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.Constants.STATUS_STAGED;
import static net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum.NO;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
public class StageWithdrawService implements StageWithdrawUseCase {

    private final TransactionalOperator rxtx;

    private final IStagingDataUseCase stagingDataUseCase;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final LoanAdjustmentPersistencePort adjustmentPersistencePort;
    private final WithdrawPersistencePort withdrawPersistencePort;
    private final IWithdrawStagingDataPersistencePort iWithdrawStagingDataPersistencePort;

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;

    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final ModelMapper modelMapper;
    private final Gson gson;

    private final ISavingsAccountUseCase savingsAccountUseCase;

    public StageWithdrawService(
            TransactionalOperator rxtx,
            IStagingDataUseCase stagingDataUseCase,
            CollectionStagingDataPersistencePort collectionStagingDataPersistencePort,
            LoanAdjustmentPersistencePort adjustmentPersistencePort,
            WithdrawPersistencePort withdrawPersistencePort,
            IWithdrawStagingDataPersistencePort iWithdrawStagingDataPersistencePort,
            ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
            SamityEventTrackerUseCase samityEventTrackerUseCase,
            ModelMapper modelMapper,
            ISavingsAccountUseCase savingsAccountUseCase) {
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.adjustmentPersistencePort = adjustmentPersistencePort;
        this.iWithdrawStagingDataPersistencePort = iWithdrawStagingDataPersistencePort;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.rxtx = rxtx;
        this.stagingDataUseCase = stagingDataUseCase;
        this.withdrawPersistencePort = withdrawPersistencePort;
        this.modelMapper = modelMapper;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<StageWithdrawResponseDTO> stageWithdraw(StageWithdrawCommand command) {

        return validateWithdrawRequest(command)
                .flatMapMany(aBoolean -> this.buildAndSaveWithdrawStagingData(command))
                .doOnError(throwable -> log.error("Failed to save Withdraw data : {}", throwable.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, Mono::error)
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(INTERNAL_SERVER_ERROR, e.getMessage())))
                .as(this.rxtx::transactional)
                .collectList()
                .map(response -> buildResponse());
    }

    @Override
    public Flux<StagingWithdrawDataDTO> getStagingWithdrawDataByStagingDataId(String stagingDataId) {
        return withdrawPersistencePort
                .getWithdrawStagedDataByStagingDataId(stagingDataId)
                .map(withdraw -> modelMapper.map(withdraw, StagingWithdrawDataDTO.class));
    }

    private Flux<Withdraw> buildAndSaveWithdrawStagingData(StageWithdrawCommand command){
        final String samityEventTrackerId = UUID.randomUUID().toString();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId())
                .flatMap(managementProcessTracker -> samityEventTrackerUseCase.insertSamityEvent(managementProcessTracker.getManagementProcessId(), samityEventTrackerId, command.getOfficeId(), command.getSamityId(), SamityEvents.WITHDRAWN.getValue(), command.getLoginId())
                        .map(samityEventTracker -> managementProcessTracker))
                .flatMapMany(managementProcessTracker -> withdrawPersistencePort.saveWithdrawStagingData(convertToDomain(command), managementProcessTracker.getManagementProcessId(), samityEventTrackerId));
    }

    private Mono<Boolean> validateWithdrawRequest(StageWithdrawCommand command) {

        return Mono.just(command)
                .filter(command1 -> !command1.getData().isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.NO_WITHDRAW_DATA_FOUND.getValue())))
                .flatMapIterable(StageWithdrawCommand::getData)
                .flatMap(withdrawData -> {
                    String savingsAccountId = withdrawData.getSavingsAccountId();
                    BigDecimal amount = withdrawData.getAmount();

                    return stagingDataUseCase
                            .getStagingDataSavingsAccountDetailBySavingsAccountId(savingsAccountId)
                            .flatMap(stagingDataSavingsAccountDetailDto -> isSavingsAccountStatusActive(savingsAccountId)
                                    .flatMap(aBoolean -> aBoolean
                                            ? Mono.just(stagingDataSavingsAccountDetailDto)
                                            : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAVINGS_ACCOUNT_IS_NOT_ACTIVE.getValue() + savingsAccountId))))
                            .flatMap(stagingDataSavingsAccountDetailDTO -> {
                                BigDecimal savingsAvailableBalance = stagingDataSavingsAccountDetailDTO.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingDataSavingsAccountDetailDTO.getSavingsAvailableBalance();
                                boolean hasSufficientBalance = savingsAvailableBalance.compareTo(amount) >= 0;

                                return hasSufficientBalance
                                        ? Mono.just(true)
                                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.INSUFFICIENT_BALANCE_FOR_WITHDRAW_IN_SAVINGS_ACCOUNT.getValue() + savingsAccountId));
                            });
                })
                .hasElements();
    }

    private Mono<Boolean> isSavingsAccountStatusActive(String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(savingsAccountDTO -> savingsAccountDTO.getStatus().equals(Status.STATUS_ACTIVE.getValue()));
    }

    private List<Withdraw> convertToDomain(StageWithdrawCommand command) {
        List<Withdraw> withdrawStagingDataList = command.getData().stream()
                .map(data -> Withdraw
                            .builder()
                            .stagingDataId(data.getStagingDataId())
                            .withdrawStagingDataId(UUID.randomUUID().toString())
                            .savingsAccountId(data.getSavingsAccountId())
                            .amount(data.getAmount())
                            .withdrawType(data.getWithdrawType())
                            .paymentMode(data.getPaymentMode())
                            .createdOn(LocalDateTime.now())
                            .createdBy(command.getLoginId())
                            .samityId(command.getSamityId())
                            .status(STATUS_STAGED)
                            .samityId(command.getSamityId())
                            .build()
                )
                .toList();
        log.info("converted datalist {}", withdrawStagingDataList);
        return withdrawStagingDataList;
    }

    private StageWithdrawResponseDTO buildResponse() {
        return StageWithdrawResponseDTO.builder()
                .userMessage("Withdraw is successfully created")
                .build();
    }

    @Override
    public Mono<StageWithdrawResponseDTO> updateWithdrawalAmount(WithdrawRequestDto requestDto) {
        return withdrawPersistencePort.getWithdrawData(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw staging data not found.")))
                .filter(withdraw -> !HelperUtil.checkIfNullOrEmpty(withdraw.getStatus()) && (withdraw.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || withdraw.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw request is not in staged or rejected state")))
                .filter(withdraw -> withdraw.getCreatedBy().equalsIgnoreCase(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw request can only be updated by the creator.")))
                .filter(data -> requestDto.getAmount() != null && requestDto.getAmount().compareTo(BigDecimal.ZERO) >= 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdrawal amount must be positive!")))
                .flatMap(withdraw -> Mono.zip(
                        Mono.just(withdraw),
                        stagingDataUseCase.getStagingAccountDataBySavingsAccountId(withdraw.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(StagingAccountData.builder().build())),
                        collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(withdraw.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(CollectionStagingData.builder().build())),
                        adjustmentPersistencePort.getLoanAdjustmentCollectionDataBySavingsAccountId(withdraw.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build()))
                ))
                .filter(tuples -> this.verifyUpdatableAmount(requestDto.getAmount(), tuples.getT2().getSavingsAvailableBalance(), tuples.getT3().getAmount(), tuples.getT4().getAmount()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.INSUFFICIENT_BALANCE_FOR_WITHDRAW_IN_SAVINGS_ACCOUNT.getValue())))
                .flatMap(tuple -> {
                    String jsonString = gson.toJson(tuple.getT1());
                    StagingWithdrawDataEditHistoryEntity withdrawHistory = buildWithdrawHistory(gson.fromJson(jsonString, Withdraw.class));
                    log.info("StagingWithdrawDataEditHistoryEntity Data for history : {}", withdrawHistory);
                    Withdraw withdraw = buildWithdrawForUpdate(requestDto, tuple.getT1());
                    log.info("Withdraw Data : {}", withdraw);
                    return iWithdrawStagingDataPersistencePort.saveWithdrawEditHistory(withdrawHistory)
                            .then(withdrawPersistencePort.updateWithdrawAmount(withdraw));
                })
                .map(withdraw -> StageWithdrawResponseDTO.builder().userMessage("Withdraw request is successfully updated").build())
                .as(rxtx::transactional)
                .doOnNext(response -> log.info("Withdraw edit response : {}", response))
                .doOnError(throwable -> log.error("Error edit withdraw data: {}", throwable.getMessage()));
    }

    private StagingWithdrawDataEditHistoryEntity buildWithdrawHistory(Withdraw withdraw) {
        StagingWithdrawDataEditHistoryEntity historyEntity = modelMapper.map(withdraw, StagingWithdrawDataEditHistoryEntity.class);
        historyEntity.setStagingWithdrawDataId(withdraw.getOid());
        historyEntity.setStagingWithdrawDataEditHistoryId(UUID.randomUUID().toString());
        historyEntity.setOid(null);
        return historyEntity;
    }

    private Withdraw buildWithdrawForUpdate(WithdrawRequestDto requestDto, Withdraw withdraw) {
        withdraw.setAmount(requestDto.getAmount());
        withdraw.setUpdatedBy(requestDto.getLoginId());
        withdraw.setUpdatedOn(LocalDateTime.now());
        withdraw.setCurrentVersion(String.valueOf(Integer.parseInt(withdraw.getCurrentVersion()) + 1));
        withdraw.setIsNew(NO.getValue());
        return withdraw;
    }

    private Boolean verifyUpdatableAmount(BigDecimal requestAmount, BigDecimal savingsAvailableBalance, BigDecimal collectionStagingAmount, BigDecimal loanAdjustmentAmount) {
        log.info("requestAmount {} savingsAvailableBalance {} collectionStagingAmount {} loanAdjustmentAmount {}", requestAmount, savingsAvailableBalance, collectionStagingAmount, loanAdjustmentAmount);
        BigDecimal savingsBalance = savingsAvailableBalance == null ? BigDecimal.ZERO : savingsAvailableBalance;
        BigDecimal collectionStagingBalance = collectionStagingAmount == null ? BigDecimal.ZERO : collectionStagingAmount;
        BigDecimal loanAdjustmentBalance = loanAdjustmentAmount == null ? BigDecimal.ZERO : loanAdjustmentAmount;
        return savingsBalance.add(collectionStagingBalance).compareTo(loanAdjustmentBalance.add(requestAmount)) >= 0;
    }
}