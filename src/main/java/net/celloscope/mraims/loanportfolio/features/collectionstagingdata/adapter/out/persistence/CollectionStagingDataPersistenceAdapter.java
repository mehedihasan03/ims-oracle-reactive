package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository.CollectionStagingDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository.CollectionStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionDataVerifyDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.RejectionCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.UnauthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentScheduleHistoryPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.celloscope.mraims.loanportfolio.core.util.Constants.CURRENT_VERSION;
import static net.celloscope.mraims.loanportfolio.core.util.Constants.STATUS_APPROVED;

@Slf4j
@Component
public class CollectionStagingDataPersistenceAdapter implements CollectionStagingDataPersistencePort {
    private final CollectionStagingDataRepository repository;
    private final CollectionStagingDataEditHistoryRepository editHistoryRepository;

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final TransactionalOperator rxtx;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort;
    private final LoanAccountUseCase loanAccountUseCase;

    public CollectionStagingDataPersistenceAdapter(CollectionStagingDataRepository repository,
                                                   CollectionStagingDataEditHistoryRepository editHistoryRepository,
                                                   ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                                                   ModelMapper modelMapper, TransactionalOperator rxtx, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, RepaymentScheduleHistoryPersistencePort repaymentScheduleHistoryPersistencePort, LoanAccountUseCase loanAccountUseCase) {
        this.repository = repository;
        this.editHistoryRepository = editHistoryRepository;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.repaymentScheduleHistoryPersistencePort = repaymentScheduleHistoryPersistencePort;
        this.loanAccountUseCase = loanAccountUseCase;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<List<CollectionStagingData>> saveAllCollectionData(List<CollectionStagingData> dataList, String isUploaded) {
        List<CollectionStagingDataEntity> entityList = convertDomainToEntityList(dataList, isUploaded);
        return repository.findAllBySamityId(entityList.get(0).getSamityId())
                .collectList()
                .flatMap(list -> {
                    if (list != null && !list.isEmpty() && list.stream().allMatch(item -> item.getCollectionType().equals("Regular"))) {
//                        log.info("List of Collection Staging Data: {}", list);
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is already found"));
                    } else if (list != null && !list.isEmpty() && list.stream().anyMatch(item -> item.getStagingDataId().equals(entityList.iterator().next().getStagingDataId()))) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Duplicate Collection Data found"));
//						List<String> list1 = list.stream().map(CollectionStagingDataEntity::getStagingDataId).toList();
//						List<String> list2 = entityList.stream().map(CollectionStagingDataEntity::getStagingDataId).toList();
//						if(list1.contains(list2.iterator().next())){
//							return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Duplicate Collection Data found"));
//						}
                    }
                    return Mono.just(entityList);
                })
                .switchIfEmpty(Mono.just(entityList))
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(this::convertEntityToDomainList)
                .doOnRequest(r -> log.info("saving collection payment data to database."))
                .doOnSuccess(res -> log.info("Response for payment collection data save: {}", res))
                .doOnError(e -> log.error("error occurred while saving collection payment database: {}", e.getMessage()))
                .onErrorResume(Mono::error);
    }

    List<CollectionStagingDataEntity> convertDomainToEntityList(List<CollectionStagingData> dataList, String isUploaded) {
        List<CollectionStagingDataEntity> entityList = dataList.stream()
                .map(data -> {
                    CollectionStagingDataEntity entity = modelMapper.map(data,
                            CollectionStagingDataEntity.class);
                    entity.setCurrentVersion(CURRENT_VERSION);
                    if (isUploaded.equals("No")) {
                        entity.setIsUploaded("No");
                    } else {
                        entity.setIsUploaded("Yes");
                        entity.setUploadedBy(entity.getCreatedBy());
                        entity.setUploadedOn(LocalDateTime.now());
                    }
                    entity.setCollectionStagingDataId(UUID.randomUUID().toString());
                    entity.setCreatedOn(LocalDateTime.now());
                    entity.setIsNew("Yes");
                    entity.setEditCommit("No");
                    entity.setIsSubmitted("No");
                    entity.setIsLocked("No");
                    return entity;
                })
                .collect(Collectors.toList());
        log.info("Collection Data List to Save into Database: {}", entityList);
        return entityList;
    }

    List<CollectionStagingData> convertEntityToDomainList(List<CollectionStagingDataEntity> dataList) {
        return dataList.stream()
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Integer> updateAllCollectionDataBySamityForAuthorization(String samityId, String collectionType, String approvedBy) {
        return getAllCollectionDataBy(samityId, collectionType)
                .map(entity -> {
                    entity.setApprovedBy(approvedBy);
                    entity.setApprovedOn(LocalDateTime.now());
                    entity.setStatus(STATUS_APPROVED);
                    /*entity.setEditCommit("Yes");*/
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnRequest(r -> log.info(
                        "Request received for updating authorization collection data to database"))
                .doOnError(e -> log.error(
                        "Error occurred during updating authorization collection data to database: {}",
                        e.getMessage()))
                .doOnSuccess(
                        s -> log.info("Successfully updated authorization collection data in database {}",
                                s.size()))
                .flatMap(res -> Mono.just(res.size()));
    }

    @Override
    public Flux<CollectionStagingDataEntity> getAllCollectionDataBy(String samityId, String collectionType) {
        return repository
                .findAllBySamityIdAndCollectionType(samityId, collectionType)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
                        ExceptionMessages.NO_COLLECTION_DATA_FOUND.getValue())))
                .doOnRequest(r -> log.info(
                        "Request received for getting collection data to database for samityId: {}, collectionType: {}",
                        samityId, collectionType))
                .doOnError(e -> log.error(
                        "Error occurred during getting authorization collection data to database: {}",
                        e.getMessage()))
                // .doOnComplete(() -> log.info("Successfully got collection data in database"))
                ;
    }

    @Override
    public Flux<CollectionStagingDataEntity> getAllCollectionData(String samityId, String collectionType) {
        return repository
                .findAllBySamityIdAndCollectionType(samityId, collectionType)
                // .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No
                // collection data found in Database")))
                .doOnRequest(r -> log.debug(
                        "Request received for getting collection data to database for samityId: {}, collectionType: {}",
                        samityId, collectionType))
                .doOnError(e -> log.error(
                        "Error occurred during getting authorization collection data to database: {}",
                        e.getMessage()))
                // .doOnComplete(() -> log.info("Successfully got collection data in database"))
                ;
    }

    @Override
    public Flux<CollectionStagingData> getCollectionStagingDataByStagingDataId(String stagingDataId) {
        return repository
                .findByStagingDataId(stagingDataId)
                .map(collectionStagingDataEntity -> modelMapper.map(collectionStagingDataEntity,
                        CollectionStagingData.class))
                .doOnNext(success -> log.info(
                        "CollectionStagingDataEntity fetched from Db for stagingDataId - {}",
                        stagingDataId))
                .doOnError(
                        throwable -> log.error(
                                "Error while fetching CollectionStagingDataEntity from Db\nReason - {}",
                                throwable.getMessage()));
    }

    @Override
    public Flux<CollectionStagingData> getCollectionStagingDataByLocanOrSavingsAccount(String account) {
        return null;
    }

    @Override
    public Mono<Long> getCountOfCollectionStagingDataByAccountIdList(List<String> account) {
        return repository
                .countAllByLoanAccountIdIn(account)
                .flatMap(aLong -> repository
                        .countAllBySavingsAccountIdIn(account)
                        .zipWith(Mono.just(aLong)))
                .map(tuple2Mono -> tuple2Mono.getT1() + tuple2Mono.getT2());
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountId(String loanAccountId) {
        return repository.findCollectionStagingDataEntityByLoanAccountId(loanAccountId)
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class));
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataBySavingsAccountId(String savingsAccountId) {
        return repository.findCollectionStagingDataEntityBySavingsAccountId(savingsAccountId)
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class));
    }

    @Override
    public Mono<CollectionDataVerifyDTO> getCollectionDataToVerifyPayment(CollectionDataVerifyDTO verifyDTO) {
        return repository
                .getSamityDayFromSamityId(verifyDTO.getSamityId())
                .map(samityDay -> {
                    verifyDTO.setSamityDay(samityDay);
                    return verifyDTO;
                })
                .doOnNext(c -> log.info("SamityDay: {} for samity: {}", c.getSamityDay(),
                        c.getSamityId()))
                .flatMap(c -> {
                    if (!verifyDTO.getStagingDataIdForLoanList().isEmpty()
                            && !verifyDTO.getLoanAccountIdList().isEmpty()) {
                        return repository
                                .getCollectionDataCountForLoanAccountToVerifyPayment(
                                        verifyDTO.getSamityId(),
                                        verifyDTO.getStagingDataIdForLoanList(),
                                        verifyDTO.getLoanAccountIdList())
                                .doOnNext(count -> log.info(
                                        "Total Staging data found For Loan Account in database: {}",
                                        count))
                                .switchIfEmpty(Mono.just(0))
                                .map(count -> {
                                    verifyDTO.setTotalCount(
                                            verifyDTO.getTotalCount()
                                                    + count);
                                    return verifyDTO;
                                });
                    } else {
                        return Mono.just(verifyDTO);
                    }
                })
                .flatMap(c -> {
                    if (!verifyDTO.getStagingDataIdForSavingsList().isEmpty()
                            && !verifyDTO.getSavingsAccountIdList().isEmpty()) {
                        return repository
                                .getCollectionDataCountForSavingsAccountToVerifyPayment(
                                        verifyDTO.getSamityId(),
                                        verifyDTO.getStagingDataIdForSavingsList(),
                                        verifyDTO.getSavingsAccountIdList())
                                .doOnNext(count -> log
                                        .info("Total Staging data found For Savings Account in database: {}",
                                                count))
                                .switchIfEmpty(Mono.just(0))
                                .map(count -> {
                                    verifyDTO.setTotalCount(
                                            verifyDTO.getTotalCount()
                                                    + count);
                                    return verifyDTO;
                                });
                    } else {
                        return Mono.just(verifyDTO);
                    }
                })
                .flatMap(collectionDataVerifyDTO -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(collectionDataVerifyDTO.getOfficeId()))
                .map(managementProcessTracker -> {
                    String currentDay = managementProcessTracker.getBusinessDay();
                    if (verifyDTO.getSamityDay().equalsIgnoreCase(currentDay)) {
                        verifyDTO.setCollectionType(CollectionType.REGULAR.getValue());
                    } else {
                        verifyDTO.setCollectionType(CollectionType.SPECIAL.getValue());
                    }

                    return verifyDTO;
                })
                .doOnNext(c -> log.debug("CollectionDataVerifyDTO After Database check: {}", c));
    }

    @Override
    public Mono<BigDecimal> getTotalCollectionBySamity(String samityId) {
        return repository.getTotalCollectionBySamityId(samityId)
                .switchIfEmpty(Mono.just(BigDecimal.valueOf(0.0)));
    }

    @Override
    public Mono<List<CollectionStagingData>> editUpdateAllCollectionData(List<CollectionStagingData> dataList,
                                                                         String loginId) {
        return repository.findAllBySamityId(dataList.get(0).getSamityId())
                .collectList()
                .flatMap(existingEntities -> {


                    // Combine new entries Flux and existing entities processing
                    return Flux.fromIterable(existingEntities)
                            .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) && entity.getEditCommit().equals("No"))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Requested For Authorization")))
                            .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getStatus()) && !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Already Authorized")))
                            .flatMap(entity -> {
                                CollectionStagingDataEditHistoryEntity newEntity = gson.fromJson(entity.toString(), CollectionStagingDataEditHistoryEntity.class);
                                newEntity.setOid(null);
                                newEntity.setCollectionStagingDataEditHistoryId(UUID.randomUUID().toString());
                                return editHistoryRepository.save(newEntity).map(editEntity -> entity);
                            })
                            .map(entity -> {
                                dataList.forEach(data -> {
                                    if (data.getAccountType().equals("Loan") && !HelperUtil.checkIfNullOrEmpty(entity.getLoanAccountId()) && entity.getLoanAccountId().equals(data.getLoanAccountId())) {
                                        entity.setAmount(data.getAmount());
                                    } else if (data.getAccountType().equals("Savings") && !HelperUtil.checkIfNullOrEmpty(entity.getSavingsAccountId()) && entity.getSavingsAccountId().equals(data.getSavingsAccountId())) {
                                        entity.setAmount(data.getAmount());
                                    }
                                });

                                entity.setLockedBy(null);
                                entity.setLockedOn(null);
                                entity.setUpdatedBy(loginId);
                                entity.setUpdatedOn(LocalDateTime.now());
                                entity.setCurrentVersion(String.valueOf(Integer.parseInt(entity.getCurrentVersion()) + 1));
                                entity.setIsNew("No");
                                return entity;
                            })
                            .collectList()
                            .flatMap(list -> Flux.fromIterable(list)
                                    .flatMap(collectionStagingDataEntity -> repository.conditionalUpdate(
                                                    collectionStagingDataEntity.getOid(), collectionStagingDataEntity.getAmount(), loginId)
                                            .map(integer -> modelMapper.map(collectionStagingDataEntity, CollectionStagingData.class)))
                                    .collectList()
                            );

                })
                .doOnNext(list -> log.debug("Updated Collection Staging data List: {}", list))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update collection Data")));
    }

	/* TODO: CHECK FOR CONFLICT RESOLUTION

	@Override
	public Mono<List<CollectionStagingData>> editUpdateCollectionDataByManagementProcessId(List<CollectionStagingData> dataList,
																						   String managementProcessId,
																						   String processId,
																						   String loginId) {
		log.info("manageId: {}, processId: {}", managementProcessId, processId);
		return repository.findAllByManagementProcessIdAndProcessIdOrderBySamityId(managementProcessId, processId)
				.doOnNext(list -> log.info("Collection Staging data List: {}", list))
				.filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) && entity.getEditCommit().equals("Yes"))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Requested For Authorization")))
				.filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getStatus()) && !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()) && !entity.getStatus().equals(Status.STATUS_SUBMITTED.getValue()))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Already Authorized")))
				.filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getCreatedBy()) && entity.getCreatedBy().equalsIgnoreCase(loginId))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Login Id Mismatch With CreatedBy")))
				.flatMap(entity -> saveCollectionStagingDataToHistory(entity)
						.map(historyEntity -> entity))
				.map(entity -> {
					dataList.forEach(data -> {
						entity.setStagingDataId(data.getStagingDataId());
						entity.setAccountType(data.getAccountType());
						entity.setLoanAccountId(data.getLoanAccountId());
						entity.setSavingsAccountId(data.getSavingsAccountId());
						entity.setAmount(data.getAmount());
						entity.setCollectionType(data.getCollectionType());
						entity.setPaymentMode(data.getPaymentMode());
						entity.setCurrentVersion(String.valueOf(data.getCurrentVersion()));
					});
					entity.setLockedBy(null);
					entity.setLockedOn(null);
					entity.setUpdatedBy(loginId);
					entity.setUpdatedOn(LocalDateTime.now());
					entity.setIsNew("No");
					return entity;
				})
				.collectList()
				.doOnNext(list -> log.info("Updated Collection Staging data List: {}", list))
				.flatMap(list -> repository.saveAll(list)
						.map(entity -> modelMapper.map(entity, CollectionStagingData.class))
						.collectList())
				.as(this.rxtx::transactional)
				.doOnNext(list -> log.debug("Updated Collection Staging data List: {}", list))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to update collection Data")));
	}*/

    @Override
    public Mono<List<CollectionStagingData>> editUpdateCollectionDataByManagementProcessId(List<CollectionStagingData> dataList,
                                                                                           String managementProcessId,
                                                                                           String processId,
                                                                                           String loginId) {
        log.info("manageId: {}, processId: {}", managementProcessId, processId);
        return repository.findAllByManagementProcessIdAndProcessIdOrderBySamityId(managementProcessId, processId)
                .doOnNext(list -> log.info("Collection Staging data List: {}", list))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) && entity.getEditCommit().equals("No"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Requested For Authorization")))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getStatus()) && !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()) && !entity.getStatus().equals(Status.STATUS_SUBMITTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Already Authorized")))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getCreatedBy()) && entity.getCreatedBy().equalsIgnoreCase(loginId))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Login Id Mismatch With CreatedBy")))
                .flatMap(entity -> saveCollectionStagingDataToHistory(entity)
                        .map(historyEntity -> entity))
                .map(entity -> {
                    dataList.forEach(data -> {
                        entity.setStagingDataId(data.getStagingDataId());
                        entity.setAccountType(data.getAccountType());
                        entity.setLoanAccountId(data.getLoanAccountId());
                        entity.setSavingsAccountId(data.getSavingsAccountId());
                        entity.setAmount(data.getAmount());
                        entity.setCollectionType(data.getCollectionType());
                        entity.setPaymentMode(data.getPaymentMode());
                        entity.setCurrentVersion(String.valueOf(data.getCurrentVersion()));
                    });
                    entity.setLockedBy(null);
                    entity.setLockedOn(null);
                    entity.setUpdatedBy(loginId);
                    entity.setUpdatedOn(LocalDateTime.now());
                    entity.setIsNew("No");
                    return entity;
                })
                .collectList()
                .doOnNext(list -> log.info("Updated Collection Staging data List: {}", list))
                .flatMap(list -> repository.saveAll(list)
                        .map(entity -> modelMapper.map(entity, CollectionStagingData.class))
                        .collectList())
                .as(this.rxtx::transactional)
                .doOnNext(list -> log.debug("Updated Collection Staging data List: {}", list))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update collection Data")));
    }

    @Override
    public Mono<List<CollectionStagingData>> removeCollectionData(String managementProcessId, String processId) {
        return repository.findAllByManagementProcessIdAndProcessIdOrderBySamityId(managementProcessId, processId)
                .doOnNext(list -> log.info("Collection Staging data by managementProcessId and processId: {}, {}", managementProcessId, processId))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) && entity.getEditCommit().equals("No"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Requested For Authorization")))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getStatus()) && !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()) && !entity.getStatus().equals(Status.STATUS_SUBMITTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Already Authorized")))
                .flatMap(entity -> saveCollectionStagingDataToHistory(entity)
                        .flatMap(historyEntity -> repository.deleteAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                                .thenReturn(gson.fromJson(entity.toString(), CollectionStagingData.class))))
                .collectList()
                .as(this.rxtx::transactional)
                .doOnNext(list -> log.info("Collection Staging data List after removing: {}", list));
    }


    private Mono<CollectionStagingDataEntity> saveCollectionStagingDataToHistory(CollectionStagingDataEntity collectionStagingDataEntity) {
        return Mono.just(collectionStagingDataEntity)
                .flatMap(entity -> {
                    CollectionStagingDataEditHistoryEntity newEntity = gson.fromJson(entity.toString(), CollectionStagingDataEditHistoryEntity.class);
                    newEntity.setOid(null);
                    newEntity.setCollectionStagingDataEditHistoryId(UUID.randomUUID().toString());
                    return editHistoryRepository.save(newEntity)
                            .map(editEntity -> modelMapper.map(editEntity, CollectionStagingDataEntity.class));
                });
    }

    @Override
    public Mono<String> updateStatusToSubmitCollectionDataForAuthorizationByManagementProcessId(String managementProcessId,
                                                                                                String processId,
                                                                                                String loginId) {
        return repository.findAllByManagementProcessIdAndProcessIdOrderBySamityId(managementProcessId, processId)
                .doOnNext(list -> log.info("Collection Staging data by mpId abd pId: {}", list))
                .collectList()
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Locked for Authorization")))
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                            entity.setIsSubmitted("Yes");
                            entity.setEditCommit("No");
                            entity.setSubmittedBy(loginId);
                            entity.setSubmittedOn(LocalDateTime.now());
                        })
                        .toList())
                .doOnNext(list -> log.info("Collection Staging data to save for submit: {}", list))
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> {
                    log.info("Collection Staging data after updating status to submit: {}", entityList);
                    return "Collection Data Updated to Submit For Authorization";
                });
    }

    @Override
    public Mono<Integer> lockCollectionBySamity(String samityId, String lockedBy) {
        return repository.findAllBySamityId(samityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data found for samity: " + samityId)))
                .collectList()
                .filter(collectionStagingDataEntityList -> collectionStagingDataEntityList.stream().allMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getEditCommit()) || item.getEditCommit().equalsIgnoreCase("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is already committed for Authorization for samity: " + samityId)))
                .flatMap(entityList -> this.validateCollectionDataBeforeLockOrUnlock(samityId,
                        entityList, "Lock"))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setLockedBy(lockedBy);
                        entity.setLockedOn(LocalDateTime.now());
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnRequest(r -> log.info("Request received to lock collection in database"))
                .doOnError(e -> log.error("Error occurred during locking collection in database: {}",
                        e.getMessage()))
                .doOnSuccess(s -> log.info("Successfully locked collection data in database {}",
                        s.size()))
                .flatMap(res -> Mono.just(res.size()));
    }

    @Override
    public Mono<Integer> unlockCollectionBySamity(String samityId) {
        return repository.findAllBySamityId(samityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data found for samity: " + samityId)))
                .collectList()
                .flatMap(entityList -> this.validateCollectionDataBeforeLockOrUnlock(samityId,
                        entityList, "Unlock"))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setLockedBy(null);
                        entity.setLockedOn(null);
                    });
                    return entityList;
                })
                .doOnNext(list -> log.info("List of Unlock Collection Entity Before DB Save: {}", list))
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("List of Unlock Collection Entity After DB Save: {}", list))
                .doOnRequest(r -> log.info("Request received to unlock collection in database"))
                .doOnError(e -> log.error("Error occurred during unlocking collection in database: {}",
                        e.getMessage()))
                .doOnSuccess(s -> log.info("Successfully unlocked collection data in database {}",
                        s.size()))
                .flatMap(res -> Mono.just(res.size()));
    }

    @Override
    public Mono<Integer> rejectCollectionBySamity(RejectionCollectionCommand command) {
        return repository.findAllBySamityId(command.getSamityId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "NO collection Data found")))
                .filter(entity -> HelperUtil.checkIfNullOrEmpty(entity.getLockedBy()))
                .switchIfEmpty(
                        Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Locked and cannot commit for rejection")))
                .filter(entity -> !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Already Authorized")))
                .filter(entity -> !HelperUtil.checkIfNullOrEmpty(entity.getEditCommit())
                        && entity.getEditCommit().equals("Yes"))
                .switchIfEmpty(
                        Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is not committed")))
                .filter(entity -> !entity.getStatus().equals(Status.STATUS_REJECTED.getValue()))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Already Rejected")))
                .collectList()
                .flatMap(list -> repository.getCountOfCollectionDataBySamityId(command.getSamityId())
                        .filter(count -> count != null && count == list.size())
                        .map(count -> list))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setStatus(Status.STATUS_REJECTED.getValue());
                        entity.setRejectedBy(command.getLoginId());
                        entity.setRejectedOn(LocalDateTime.now());
                        entity.setRemarks(command.getRemarks());
                        entity.setEditCommit("No");
                    });
                    return entityList;
                })
                .flatMap(entityList -> repository.saveAll(entityList)
                        .collectList())
                // .flatMap(entityList ->
                // repository.getCountOfCollectionDataBySamityId(command.getSamityId())
                // .filter(count -> count != null && count > 0 && count == entityList.size()))
                .map(List::size)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to reject and update collection Data")))
                .doOnError(throwable -> log.error("Failed to reject Collection Data: {}",
                        throwable.getMessage()));

    }

    @Override
    public Flux<CollectionStagingDataEntity> editCommitForCollectionDataBySamity(CollectionDataRequestDTO command) {

        return repository.findAllBySamityId(command.getSamityId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data Found")))
                .filter(entity -> !entity.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Already Authorized")))
                .filter(entity -> HelperUtil.checkIfNullOrEmpty(entity.getEditCommit()) || !entity.getEditCommit().equals("Yes"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is already committed")))
                .map(entity -> {
                    entity.setEditCommit("Yes");
                    entity.setIsSubmitted("Yes");
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setSubmittedBy(command.getLoginId());
                    entity.setSubmittedOn(LocalDateTime.now());
                    return entity;
                })
                .collectList()
                .filter(collectionStagingDataEntityList -> collectionStagingDataEntityList.stream().allMatch(item -> HelperUtil.checkIfNullOrEmpty(item.getLockedBy()) || item.getEditCommit().equalsIgnoreCase("")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Locked and cannot commit for Authorization for samity: " + command.getSamityId())))
                .flatMapMany(repository::saveAll);
    }

    @Override
    public Mono<List<String>> getStagingDataIdListBySamity(String samityId) {
        return repository.getStagingDataIdListBySamity(samityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data Found")))
                .collectList()
                .doOnNext(stringList -> log.debug("Staging Data Id List for unauthorized collection: {}", stringList));
    }

    @Override
    public Mono<UnauthorizeCollectionCommand> unauthorizeBySamity(UnauthorizeCollectionCommand command) {
        return repository.findAllBySamityId(command.getSamityId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data Found")))
                .filter(entity -> entity.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is not Authorized yet")))
                .collectList()
//                @TODO: update collection staging data for unauthorize
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setStatus(Status.STATUS_STAGED.getValue());
                        entity.setApprovedBy(null);
                        entity.setApprovedOn(null);
                        entity.setEditCommit("No");
                    });
                    return entityList;
                })
                .doOnNext(list -> log.debug("Collection Data List For Samity: {} is {}", command.getSamityId(), list))
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(list -> command);
    }

    @Override
    public Mono<List<CollectionStagingData>> getAllCollectionDataBySamityId(String samityId) {
        return repository.findAllBySamityId(samityId)
                .map(entity -> gson.fromJson(entity.toString(), CollectionStagingData.class))
                .collectList();
    }

    @Override
    public Mono<List<CollectionStagingDataEntity>> getAllCollectionDataByManagementProcessId(String managementProcessId) {
        return repository.findAllByManagementProcessIdOrderBySamityId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<String> deleteAllCollectionDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Collection Data Deleted Successfully"));
    }

    @Override
    public Flux<CollectionStagingData> getAllCollectionDataBySamity(String samityId) {
        return repository.findAllBySamityIdOrderByCreatedOn(samityId)
                .map(collectionStagingDataEntity -> gson.fromJson(collectionStagingDataEntity.toString(), CollectionStagingData.class));
    }

    @Override
    public Mono<CollectionStagingData> getOneCollectionBySamity(String samityId) {
        return repository.findDistinctFirstBySamityId(samityId)
                .switchIfEmpty(Mono.just(CollectionStagingDataEntity.builder().build()))
                .map(collectionStagingDataEntity -> gson.fromJson(collectionStagingDataEntity.toString(), CollectionStagingData.class));
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
        return repository.findAllBySamityId(samityId)
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndLockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No Collection Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return repository.findAllBySamityId(samityId)
                .collectList()
                .flatMap(list -> {
                    if (!list.isEmpty()) {
                        return this.validateAndUnlockSamity(list, samityId, loginId);
                    }
                    return Mono.just(samityId)
                            .doOnNext(id -> log.info("No Collection Data for Samity: {}", id));
                });
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
        return repository.getSamityIdListLockedByUserForAuthorization(lockedBy)
                .distinct(String::chars)
                .collectList();
    }

    @Override
    public Mono<List<CollectionStagingData>> saveAllCollectionDataToDatabase(List<CollectionStagingData> collectionStagingDataList) {
        return Mono.just(collectionStagingDataList)
                .map(list -> list.stream().map(collectionStagingData -> gson.fromJson(collectionStagingData.toString(), CollectionStagingDataEntity.class)).toList())
                .doOnNext(entityList -> log.info("Collection Data to Save into Database: {}", entityList))
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> collectionStagingDataList);
    }

    @Override
    public Mono<List<CollectionStagingData>> getCollectionStagingDataBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdIn(samityIdList)
                .map(entity -> gson.fromJson(entity.toString(), CollectionStagingData.class))
                .collectList();
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .doOnNext(collectionStagingDataList -> log.info("Samity Id: {}, Total Collection Data: {}", samityId, collectionStagingDataList.size()))
                .flatMap(entityList -> {
                    if (!entityList.isEmpty()) {
                        return this.validateAndUpdateCollectionDataForAuthorization(samityId, entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .collectList()
                .doOnNext(collectionStagingDataList -> log.info("Samity Id: {}, Total Collection Data: {}", samityId, collectionStagingDataList.size()))
                .flatMap(entityList -> {
                    if (!entityList.isEmpty()) {
                        return this.validateAndUpdateCollectionDataForRejection(samityId, entityList, loginId);
                    }
                    return Mono.just(samityId);
                });
    }

    private Mono<String> validateAndUpdateCollectionDataForRejection(String samityId, List<CollectionStagingDataEntity> collectionStagingDataEntityList, String loginId) {
        return this.checkIfAllCollectionDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(collectionStagingDataEntityList)
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_REJECTED.getValue());
                            entity.setRejectedBy(loginId);
                            entity.setRejectedOn(LocalDateTime.now());
                            entity.setIsSubmitted("No");
                            entity.setSubmittedBy(null);
                            entity.setSubmittedOn(null);
                            entity.setIsLocked("No");
                            entity.setLockedBy(null);
                            entity.setLockedOn(null);
                        })
                        .toList())
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Collection Data Updated For Rejection"))
                .map(entityList -> samityId);
    }

    @Override
    public Flux<CollectionStagingData> getAllCollectionDataBySamityIdList(List<String> samityIdList) {
        return repository.findAllBySamityIdIn(samityIdList)
                .map(entity -> gson.fromJson(entity.toString(), CollectionStagingData.class));
    }

    @Override
    public Mono<String> validateAndUpdateCollectionDataForSubmission(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                // submit only regular collection data which is not locked
                .filter(entity -> entity.getCollectionType().equalsIgnoreCase(CollectionType.REGULAR.getValue()) || entity.getCollectionType().equalsIgnoreCase(CollectionType.SPECIAL.getValue()))
                .collectList()
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Locked For Authorization")))
                .flatMapIterable(entityList -> entityList)
                .map(entity -> {
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setSubmittedBy(loginId);
                    entity.setSubmittedOn(LocalDateTime.now());
                    entity.setIsSubmitted("Yes");
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> "Collection is successfully updated for submission");
    }

    @Override
    public Mono<String> validateAndUpdateCollectionStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId) {
        return repository.findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Collection Data is Found for Samity")))
                .filter(data -> data.getIsLocked().equals("Yes"))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Not Locked for Unauthorization")))
                .filter(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Not Authorized")))
                .flatMap(collectionStagingDataEntity -> {
                    if (collectionStagingDataEntity.getCollectionType().equalsIgnoreCase(CollectionType.SINGLE.getValue())) {
                        return repaymentScheduleHistoryPersistencePort
                                .getAllRepaymentScheduleHistoryByManagementProcessIdAndLoanAccountId(managementProcessId, collectionStagingDataEntity.getLoanAccountId())
                                .collectList()
                                .flatMap(repaymentScheduleHistory -> repaymentScheduleHistory.isEmpty()
                                        ? loanAccountUseCase.updateLoanAccountStatus(collectionStagingDataEntity.getLoanAccountId(), Status.STATUS_ACTIVE.getValue())
                                            .thenReturn(collectionStagingDataEntity)
                                        : loanRepaymentScheduleUseCase.revertRepaymentScheduleByManagementProcessIdAndLoanAccountId(managementProcessId, collectionStagingDataEntity.getLoanAccountId())
                                        .doOnRequest(r -> log.info("Request received to revert repayment schedule for loan account id : {} and managementProcessId : {}", collectionStagingDataEntity.getLoanAccountId(), managementProcessId))
                                        .doOnSuccess(s -> log.info("Successfully reverted repayment schedule for loan account id : {} and managementProcessId : {}", collectionStagingDataEntity.getLoanAccountId(), managementProcessId))
                                        .doOnError(e -> log.error("Error occurred during reverting repayment schedule for loan account id : {} and managementProcessId : {} with error : {}", collectionStagingDataEntity.getLoanAccountId(), managementProcessId, e.getMessage()))
                                        .flatMap(aBoolean -> loanAccountUseCase.updateLoanAccountStatus(collectionStagingDataEntity.getLoanAccountId(), Status.STATUS_ACTIVE.getValue()))
                                        .thenReturn(collectionStagingDataEntity));
                    }
                    return Mono.just(collectionStagingDataEntity);
                })
                .map(entity -> {
//                    entity.setStatus(Status.STATUS_UNAUTHORIZED.getValue());
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setApprovedBy(null);
                    entity.setApprovedOn(null);
                    entity.setIsLocked("No");
                    entity.setLockedBy(null);
                    entity.setLockedOn(null);
                    return entity;
                })
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> "Collection Data is successfully updated for Unauthorization");
    }

    private Mono<String> validateAndUpdateCollectionDataForAuthorization(String samityId, List<CollectionStagingDataEntity> collectionStagingDataEntityList, String loginId) {
        return this.checkIfAllCollectionDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(collectionStagingDataEntityList)
                .map(entityList -> entityList.stream()
                        .peek(entity -> {
                            entity.setStatus(Status.STATUS_APPROVED.getValue());
                            entity.setApprovedBy(loginId);
                            entity.setApprovedOn(LocalDateTime.now());
                            entity.setIsLocked("No");
                            entity.setLockedBy(null);
                            entity.setLockedOn(null);
                        })
                        .toList())
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.info("Samity Collection Data Updated For Authorization"))
                .map(entityList -> samityId);
    }

    private Mono<List<CollectionStagingDataEntity>> checkIfAllCollectionDataIsSubmittedAndLockedForAuthorizationOrRejectionProcess(List<CollectionStagingDataEntity> collectionStagingDataEntityList) {
        return Mono.just(collectionStagingDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(collectionStagingData -> collectionStagingData.getStatus().equals(Status.STATUS_SUBMITTED.getValue()) || collectionStagingData.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is not Submitted for Authorization")))
                .filter(entityList -> entityList.stream().allMatch(collectionStagingData -> collectionStagingData.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is not Locked for Authorization")))
                .doOnNext(entityList -> log.info("Samity Collection Data Validated For Authorization Process"));
    }

	/* TODO: CHECK FOR CONFLICT RESOLUTION
	private Mono<String> validateAndLockSamity(List<CollectionStagingDataEntity> collectionStagingDataEntityList, String samityId, String loginId){
		return Mono.just(collectionStagingDataEntityList)
				.filter(entityList -> entityList.stream().filter(collectionStagingDataEntity -> Strings.isNotNullAndNotEmpty(collectionStagingDataEntity.getIsSubmitted())).allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Not Submitted")))
				.filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
				.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Already Locked")))
				.map(entityList -> {
					entityList.forEach(entity -> {
						entity.setIsLocked("Yes");
						entity.setLockedBy(loginId);
						entity.setLockedOn(LocalDateTime.now());
					});
					return entityList;
				})
				.flatMapMany(repository::saveAll)
				.collectList()
				.doOnNext(list -> log.info("Samity {} Collections are Locked for Authorization", samityId))
				.map(list -> samityId);
	}*/

    private Mono<String> validateAndLockSamity(List<CollectionStagingDataEntity> collectionStagingDataEntityList, String samityId, String loginId) {
        return Mono.just(collectionStagingDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Already Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked("Yes");
                        entity.setLockedBy(loginId);
                        entity.setEditCommit("No");
                        entity.setLockedOn(LocalDateTime.now());
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Collections are Locked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<String> validateAndUnlockSamity(List<CollectionStagingDataEntity> collectionStagingDataEntityList, String samityId, String loginId) {
        return Mono.just(collectionStagingDataEntityList)
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsSubmitted().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Not Submitted")))
                .filter(entityList -> entityList.stream().allMatch(entity -> entity.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with Id " + samityId + " Collection is Not Locked")))
                .map(entityList -> {
                    entityList.forEach(entity -> {
                        entity.setIsLocked("No");
                        entity.setLockedBy(null);
                        entity.setLockedOn(null);
                    });
                    return entityList;
                })
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(list -> log.info("Samity {} Collections are Unlocked for Authorization", samityId))
                .map(list -> samityId);
    }

    private Mono<List<CollectionStagingDataEntity>> validateCollectionDataBeforeLockOrUnlock(String samityId, List<CollectionStagingDataEntity> entityList, String type) {
        return Mono.just(entityList)
                .filter(list -> (type.equals("Lock")
                        && list.stream().allMatch(entity -> HelperUtil
                        .checkIfNullOrEmpty(entity.getLockedBy())))
                        || type.equals("Unlock"))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Already Locked")))
                .filter(list -> (type.equals("Unlock")
                        && list.stream().noneMatch(entity -> HelperUtil
                        .checkIfNullOrEmpty(entity.getLockedBy()))
                        || type.equals("Lock")))
                .switchIfEmpty(Mono
                        .error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Already Unlocked")))
                .filter(list -> list.stream()
                        .noneMatch(entity -> entity.getStatus()
                                .equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(
                        Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data is Already Authorized")))
                .flatMap(list -> repository.getCountOfCollectionDataBySamityId(samityId)
                        .filter(count -> count == list.size())
                        .map(count -> list))
                .switchIfEmpty(Mono.error(
                        new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                "Collection Data verification failed")));
    }

    @Override
    public Mono<CollectionStagingData> getCollectionStagingDataByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId, String processId) {
        return repository.findFirstByLoanAccountIdAndManagementProcessIdAndProcessIdOrderByCreatedOnDesc(loanAccountId, managementProcessId, processId)
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class));
    }

    @Override
    public Flux<CollectionStagingData> getAllCollectionStagingDataByManagementProcessIdAndSamityId(String managementProcessId, String samityId) {
        return repository
                .findAllByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .mapNotNull(entity -> modelMapper.map(entity, CollectionStagingData.class))
                .doOnNext(data -> log.info("Found CollectionStagingData: {}", data))
                .doOnError(e -> log.error("Error occurred while fetching CollectionStagingData: {}", e.getMessage()));
    }

    @Override
    public Mono<List<CollectionStagingData>> saveAllCollectionStagingDataIntoEditHistory(List<CollectionStagingData> data, String loginId) {
        return Flux.fromIterable(data)
                .mapNotNull(collectionStagingData -> {
                            CollectionStagingDataEditHistoryEntity editHistoryEntity = modelMapper.map(collectionStagingData, CollectionStagingDataEditHistoryEntity.class);
//                            editHistoryEntity.setUpdatedBy(loginId);
//                            editHistoryEntity.setUpdatedOn(LocalDateTime.now());
                            return editHistoryEntity;
                        })
                .collectList()
                .doOnNext(editHistoryRepository::saveAll)
                .doOnSuccess(editHistoryData -> log.info("Collection Staging Data saved into edit history"))
                .doOnError(throwable -> log.error("Error saving Collection Staging Data into edit history: {}", throwable.getMessage()))
                .thenReturn(data);
    }

    @Override
    public Mono<Void> deleteAllCollectionStagingData(List<CollectionStagingData> data) {
        return Flux.fromIterable(data)
                .mapNotNull(collectionStagingData ->
                        modelMapper.map(collectionStagingData, CollectionStagingDataEntity.class))
                .collectList()
                .flatMap(repository::deleteAll)
                .doOnSuccess(d -> log.info("Collection Staging Data successfully deleted"))
                .doOnError(throwable -> log.error("Error deleting Collection Staging Data: {}", throwable.getMessage()));
    }

	@Override
	public Flux<CollectionStagingData> getAllCollectionStagingDataByManagementProcessIdAndLoginId(String managementProcessId, String collectionType, String fieldOfficerId, Integer limit, Integer offset) {
		return repository.findCollectionStagingDataByCollectionType(managementProcessId, collectionType, fieldOfficerId, limit, offset)
				.doOnNext(entity -> log.info("Collection Staging data by collection type : {}", entity))
				.map(entity -> modelMapper.map(entity, CollectionStagingData.class));
	}

	@Override
	public Mono<CollectionStagingData> getCollectionStagingDataByOid(String oid) {
		return repository.findByOid(oid)
				.doOnNext(entity -> log.info("Collection Staging data by Oid: {}", entity))
				.map(entity -> modelMapper.map(entity, CollectionStagingData.class));
	}

    @Override
    public Mono<Void> deleteSpecialCollectionStagingDataByOid(String oid) {
        return repository.deleteByOid(oid)
                .doOnSuccess(d -> log.info("Special Collection Staging Data successfully deleted"))
                .doOnError(throwable -> log.error("Error deleting Special Collection Staging Data: {}", throwable.getMessage()));
    }



    @Override
    public Mono<CollectionStagingData> saveCollectionStagingData(CollectionStagingData collectionStagingData) {
        return Mono.just(collectionStagingData)
                .map(data -> modelMapper.map(data, CollectionStagingDataEntity.class))
                .flatMap(repository::save)
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class));
    }


    @Override
    public Mono<String> validateAndUpdateCollectionDataForSubmissionByOid(String loginId, String oid) {
        return repository.findByOid(oid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data is Locked For Authorization")))
                .map(entity -> {
                    entity.setStatus(Status.STATUS_SUBMITTED.getValue());
                    entity.setSubmittedBy(loginId);
                    entity.setSubmittedOn(LocalDateTime.now());
                    entity.setIsSubmitted("Yes");
                    return entity;
                })
                .flatMap(repository::save)
                .map(entityList -> "Collection is successfully updated for submission");
    }

    @Override
    public Flux<CollectionStagingData> getAllCollectionDataByOidList(List<String> oid) {
        return repository.findAllByOidIn(oid)
                .map(entity -> modelMapper.map(entity, CollectionStagingData.class))
                .doOnNext(data -> log.info("Collection Staging data by for submit: {}", data));
    }

    @Override
    public Mono<Long> collectionStagingDataCount(String managementProcessId, String collectionType, String fieldOfficerId) {
        log.info("Getting Count of Collection Staging data by managementProcessId and fieldOfficerId : {} {}", managementProcessId, fieldOfficerId);
        return repository.countStagingCollectionData(managementProcessId, collectionType, fieldOfficerId)
                .doOnNext(count -> log.info("Count of Collection Staging data : {}", count));
    }

    @Override
    public Mono<String> deleteCollectionDataByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return repository.deleteAllByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnRequest(r -> log.info("Request received to delete collection data by managementProcessId and processId"))
                .doOnNext(s -> log.info("Successfully deleted collection data by managementProcessId and processId"))
                .doOnError(e -> log.error("Error occurred during deleting collection data by managementProcessId and processId: {}", e.getMessage()))
                .then(Mono.just("Collection Data Deleted Successfully"));
    }

    @Override
    public Mono<Long> collectionStagingDataCount(String managementProcessId, String samityId) {
        return repository.countByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .doOnNext(count -> log.info("Count of Collection Staging data : {}", count));
    }
}
