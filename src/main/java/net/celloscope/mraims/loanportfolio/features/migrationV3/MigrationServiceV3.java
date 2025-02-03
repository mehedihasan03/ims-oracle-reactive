package net.celloscope.mraims.loanportfolio.features.migrationV3;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.IAuthorizationUseCase;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.AuthorizationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.DisbursementUseCase;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.dpsrepaymentschedule.MigrationDPSRepaymentScheduleServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanaccount.MigrationLoanAccountServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanapplication.MigrationLoanApplicationServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct.MigrationLoanProductServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.managementprocesstracker.MigrationManagementProcessTrackerServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.member.MigrationMemberServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.memsmtoffprimap.MigrationMemSmtOffPriMapServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.office.MigrationOfficeServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.officeeventtracker.MigrationOfficeEventTrackerServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.passbook.MigrationPassbookServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.paymentcollection.MigrationPaymentCollectionService;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.person.MigrationPersonServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity.MigrationSamityServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccount.MigrationSavingsAccountServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccproposal.MigrationSavingsAccProposalServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsproduct.MigrationSavingsProductServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.servicechargechart.MigrationServiceChargeChartServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.MigrationStagingServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingaccountdata.MigrationStagingAccountDataServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingdata.MigrationStagingDataServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.transaction.MigrationTransactionServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.*;
import net.celloscope.mraims.loanportfolio.features.migrationV3.interestchart.MigrationInterestChartServiceV3;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.PostSavingsInterestCommand;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationServiceV3 {

    private final TransactionalOperator rxtx;
    private final MigrationOfficeServiceV3 migrationOfficeService;
    private final MigrationSamityServiceV3 migrationSamityService;
    private final MigrationPersonServiceV3 personService;
    private final MigrationMemberServiceV3 memberService;
    private final MigrationMemSmtOffPriMapServiceV3 memSmtOffPriMapService;
    private final MigrationLoanProductServiceV3 loanProductService;
    private final MigrationServiceChargeChartServiceV3 serviceChargeChartService;
    private final MigrationLoanApplicationServiceV3 loanApplicationService;
    private final MigrationLoanAccountServiceV3 loanAccountService;
    private final DisbursementUseCase disbursementUseCase;
    private final MigrationManagementProcessTrackerServiceV3 managementProcessTrackerService;
    private final MigrationOfficeEventTrackerServiceV3 officeEventTrackerService;
    private final IStagingDataUseCase stagingDataUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final MigrationStagingDataServiceV3 stagingDataService;
    private final MigrationStagingAccountDataServiceV3 stagingAccountDataService;
    private final MigrationStagingServiceV3 migrationStagingService;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final IAuthorizationUseCase authorizationUseCase;
    private final MigrationTransactionServiceV3 migrationTransactionService;
    private final MigrationPassbookServiceV3 migrationPassbookService;
    private final MigrationSavingsProductServiceV3 migrationSavingsProductService;
    private final MigrationInterestChartServiceV3 interestChartService;
    private final MigrationSavingsAccProposalServiceV3 savingsAccProposalService;
    private final MigrationSavingsAccountServiceV3 savingsAccountService;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final PassbookUseCase passbookUseCase;
    private final MigrationDPSRepaymentScheduleServiceV3 dpsRepaymentScheduleService;
    private final MigrationPaymentCollectionService migrationPaymentCollectionService;
    private final SavingsInterestUseCase savingsInterestUseCase;


    public Mono<MigratedCollectionResponseDto> migrateCutOffDateCollection(MigrationCollectionRequestDto requestDto) {
        log.info("Migration request received for {}", requestDto);
        return this.generateStagingData(requestDto)
                .doOnRequest(value -> log.info("Generating Staging Data"))
                .doOnSuccess(responseDto -> log.info("Staging Data Generation successful"))
                .doOnError(throwable -> log.error("Error occurred while generating Staging Data: {}", throwable.getMessage()))
                .delayElement(Duration.ofSeconds(3))
                .flatMap(responseDto -> this.buildAndSaveCollection(responseDto, requestDto))
                .doOnRequest(value -> log.info("Building and Saving Collection Data"))
                .doOnSuccess(responseDto -> log.info("Collection Data Generation successful"))
                .doOnError(throwable -> log.error("Error occurred while generating Collection Data: {}", throwable.getMessage()))
                .delayElement(Duration.ofSeconds(3))
                .flatMap(responseDto -> this.submitAndLockSamity(responseDto, requestDto))
                .doOnRequest(value -> log.info("Submitting and Locking Samity"))
                .doOnSuccess(responseDto -> log.info("Samity Submission and Locking successful"))
                .doOnError(throwable -> log.error("Error occurred while submitting and locking Samity: {}", throwable.getMessage()))
                .delayElement(Duration.ofSeconds(3))
                .flatMap(collectionResponseDto -> this.authorizeAndPostInterest(collectionResponseDto, requestDto))
                .doOnRequest(value -> log.info("Authorizing Collection Data"))
                .doOnSuccess(responseDto -> log.info("Collection Data Authorization successful"))
                .doOnError(throwable -> log.error("Error occurred while authorizing Collection Data: {}", throwable.getMessage()))
                .doOnSuccess(responseDto -> log.info("MigratedCollectionResponseDto: {}", responseDto))
                .flatMap(responseDto -> this.insertOfficeEvents(responseDto, requestDto))
                .doOnRequest(value -> log.info("Inserting Office Events"));
    }

    private Mono<MigratedCollectionResponseDto> insertOfficeEvents(MigratedCollectionResponseDto migratedCollectionResponseDto, MigrationCollectionRequestDto requestDto) {
        return officeEventTrackerService.insertOfficeEvents(migratedCollectionResponseDto, requestDto)
                .map(officeEventTrackerEntity -> migratedCollectionResponseDto)
                .doOnSuccess(responseDto -> log.info("Office Events inserted successfully"))
                .doOnError(throwable -> log.error("Error occurred while inserting Office Events: {}", throwable.getMessage()));
    }


    private Mono<MigratedCollectionResponseDto> generateStagingData(MigrationCollectionRequestDto requestDto) {
        return Flux.fromIterable(requestDto.getMembers())
                .flatMap(this::validateRequestedMemberDataLoanInformationBeforeCollection)
                .flatMap(tuple2 -> tuple2.getT1() ? Mono.just(tuple2.getT1()) : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, tuple2.getT2())))
                .collectList()
                .flatMap(booleans ->
                        rxtx.transactional(migrationOfficeService.getOfficeById(requestDto.getOfficeId())
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Office not found by id : " + requestDto.getOfficeId())))
                                .map(office -> {
                                    MigratedCollectionResponseDto responseDto = new MigratedCollectionResponseDto();
                                    responseDto.setOffice(office);
//                                    log.info("MigratedCollectionResponseDto: {}", responseDto);
                                    return responseDto;
                                })
                                .flatMap(responseDto -> managementProcessTrackerService.getByOfficeId(requestDto.getOfficeId())
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Management Process Tracker Entry not found by office_id : " + requestDto.getOfficeId())))
                                        .map(managementProcessTracker -> {
                                            responseDto.setManagementProcessTracker(managementProcessTracker);
//                                            log.info("MigratedCollectionResponseDto: {}", responseDto);
                                            return responseDto;
                                        })
                                )
                                .flatMap(responseDto -> officeEventTrackerService.getByOfficeId(requestDto, responseDto)
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No DAY_STARTED Office Event Tracker Entry found by office_id : " + requestDto.getOfficeId())))
                                        .map(officeEventTracker -> {
                                            responseDto.setOfficeEventTracker(List.of(officeEventTracker));
//                                            log.info("MigratedCollectionResponseDto: {}", responseDto);
                                            return responseDto;
                                        })
                                )
                                .flatMap(responseDto -> stagingDataUseCase.generateStagingDataByOffice(StagingDataRequestDTO.builder()
                                                .officeId(requestDto.getOfficeId())
                                                .mfiId(requestDto.getMfiId())
                                                .loginId(requestDto.getLoginId())
                                                .build())
                                        .map(stagingData -> {
                                            responseDto.setStagingDataStatus(stagingData);
//                                            log.info("MigratedCollectionResponseDto: {}", responseDto);
                                            return responseDto;
                                        })
                                )
                                .flatMap(responseDto -> Mono.defer(() -> Mono.just(responseDto)
                                                .flatMap(dto -> stagingDataUseCase.gridViewOfStagingDataStatusByOffice(StagingDataRequestDTO.builder()
                                                                .officeId(requestDto.getOfficeId())
                                                                .mfiId(requestDto.getMfiId())
                                                                .loginId(requestDto.getLoginId())
                                                                .build())
                                                        .map(stagingDataStatusByOfficeResponseDTO -> {
                                                            responseDto.setStagingDataStatus(stagingDataStatusByOfficeResponseDTO);
//                                                log.info("MigratedCollectionResponseDto while retrying: {}", responseDto);
                                                            return responseDto;
                                                        }))
                                                .filter(dto -> Status.STATUS_FINISHED.getValue().equals(dto.getStagingDataStatus().getStatus()))  // create an empty Mono when the status is not 'Finished'
                                                .repeatWhenEmpty(flux -> flux.flatMap(x -> {
//                                                    log.info("Repeating operation because status is not Finished");
                                                    return Mono.delay(Duration.ofSeconds(1));
                                                })))
                                )
                        )
                );

    }


    private Mono<MigratedCollectionResponseDto> buildAndSaveCollection(MigratedCollectionResponseDto responseDto, MigrationCollectionRequestDto requestDto) {
        return Mono.just(responseDto)
                .flatMap(stagedResponseDto -> stagingDataService.getByManagementProcessId(stagedResponseDto.getManagementProcessTracker().getManagementProcessId())
                        .collectList()
                        .map(stagingDataEntities -> {
                            stagedResponseDto.setStagingData(stagingDataEntities);
//                            log.info("MigratedCollectionResponseDto with staging data: {}", stagedResponseDto);
                            return stagedResponseDto;
                        })
                                        )
                                        .flatMap(stagedResponseDto ->
                stagingAccountDataService.getByManagementProcessId(stagedResponseDto.getManagementProcessTracker().getManagementProcessId())
                        .collectList()
                        .map(stagingAccountDataEntities -> {
                            stagedResponseDto.setStagingAccountData(stagingAccountDataEntities);
//                            log.info("MigratedCollectionResponseDtowith staging account data: {}", stagedResponseDto);
                            return stagedResponseDto;
                        })
        )
                .flatMap(stagedResponseDto -> migrationStagingService.buildPaymentCollectionBySamityCommand(requestDto, stagedResponseDto)
                        .map(paymentCollectionBySamityCommands -> {
                            stagedResponseDto.setCollections(paymentCollectionBySamityCommands);
//                            log.info("MigratedCollectionResponseDto with collection data: {}", stagedResponseDto);
                            return stagedResponseDto;
                        })
                )
                .flatMap(stagedResponseDto ->
                                Flux.fromIterable(stagedResponseDto.getCollections())
//                                                        .flatMap(paymentCollectionUseCase::collectPaymentBySamity)
                                        .flatMap(migrationPaymentCollectionService::collectPaymentBySamity)
                                        .then(Mono.just(stagedResponseDto))
                );
    }

    private Mono<MigratedCollectionResponseDto> submitAndLockSamity(MigratedCollectionResponseDto responseDto, MigrationCollectionRequestDto requestDto) {
        return Mono.just(responseDto)
                .flatMap(collectedResponseDto -> {
            return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSamityId).collect(Collectors.toSet()))
                    .flatMap(samityId -> collectionStagingDataQueryUseCase.submitCollectionDataForAuthorizationBySamity(CollectionDataRequestDTO.builder()
                            .officeId(requestDto.getOfficeId())
                            .samityId(samityId)
                            .loginId(requestDto.getLoginId())
                            .mfiId(requestDto.getMfiId())
                            .build())
                    ).then(Mono.just(collectedResponseDto));
        })
                                        .flatMap(collectedResponseDto -> {
            return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSamityId).collect(Collectors.toSet()))
                    .collectList()
                    .flatMap(samityIds -> authorizationUseCase.lockSamityListForAuthorization(AuthorizationRequestDTO.builder()
                            .officeId(requestDto.getOfficeId())
                            .samityIdList(samityIds)
                            .loginId(requestDto.getLoginId())
                            .mfiId(requestDto.getMfiId())
                            .build())
                    ).then(Mono.just(collectedResponseDto));
        })
               /* .flatMap(collectedResponseDto -> {
                    return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSamityId).collect(Collectors.toSet()))
                            .collectList()
                            .flatMap(samityIds -> authorizationUseCase.authorizeSamityList(AuthorizationRequestDTO.builder()
                                    .officeId(requestDto.getOfficeId())
                                    .samityIdList(samityIds)
                                    .loginId(requestDto.getLoginId())
                                    .mfiId(requestDto.getMfiId())
                                    .build())
                            ).then(Mono.just(collectedResponseDto));
                })
                .flatMap(collectionResponseDto -> {
                    return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSavingsInformation).toList())
                            .flatMap(migrationSavingsRequestDto -> {
                                if (migrationSavingsRequestDto.getInterest() != null && migrationSavingsRequestDto.getInterest().compareTo(BigDecimal.ZERO) > 0) {
                                    return savingsInterestUseCase
                                            .postSavingsInterest(PostSavingsInterestCommand
                                                    .builder()
                                                    .savingsAccountId(migrationSavingsRequestDto.getSavingsAccountId())
                                                    .interestAmount(migrationSavingsRequestDto.getInterest())
                                                    .interestPostingDate(requestDto.getConfigurations().getCutOffDate())
                                                    .loginId(requestDto.getLoginId())
                                                    .officeId(requestDto.getOfficeId())
                                                    .build())
                                            .thenReturn(collectionResponseDto);
                                } else {
                                    return Mono.just(collectionResponseDto);
                                }
                            })
                            .then(Mono.just(collectionResponseDto));
                })*/
                /*.flatMap(collectedResponseDto -> {
                    return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getMemberId).collect(Collectors.toSet()))
                            .collectList()
                            .flatMap(memberIds -> migrationTransactionService.getByMemberIdList(memberIds)
                                    .collectList()
                                    .map(transactionEntities -> {
                                        collectedResponseDto.setTransactions(transactionEntities);
//                                        log.info("MigratedCollectionResponseDto with transaction data: {}", collectedResponseDto);
                                        return collectedResponseDto;
                                    })
                            );
                })
                .flatMap(collectedResponseDto -> {
                    return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getMemberId).collect(Collectors.toSet()))
                            .collectList()
                            .flatMap(memberIds -> migrationPassbookService.getByMemberIdList(memberIds)
                                    .collectList()
                                    .map(passbookEntities -> {
                                        collectedResponseDto.setPassbooks(passbookEntities);
//                                        log.info("MigratedCollectionResponseDto with passbook data: {}", collectedResponseDto);
                                        return collectedResponseDto;
                                    })
                            );
                })*/;
    }

    private Mono<MigratedCollectionResponseDto> authorizeAndPostInterest(MigratedCollectionResponseDto responseDto, MigrationCollectionRequestDto requestDto) {
        return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSamityId).collect(Collectors.toSet()))
                .collectList()
                .flatMap(samityIds -> authorizationUseCase.authorizeSamityListMigration(AuthorizationRequestDTO.builder()
                        .officeId(requestDto.getOfficeId())
                        .samityIdList(samityIds)
                        .loginId(requestDto.getLoginId())
                        .mfiId(requestDto.getMfiId())
                        .source(Constants.SOURCE_MIGRATION.getValue())
                        .build())
                ).then(Mono.just(responseDto))
            .flatMap(collectionResponseDto -> {
        return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSavingsInformation).toList())
                .flatMap(migrationSavingsRequestDto -> {
                    if (migrationSavingsRequestDto.getInterest() != null && migrationSavingsRequestDto.getInterest().compareTo(BigDecimal.ZERO) > 0) {
                        return savingsInterestUseCase
                                .postSavingsInterest(PostSavingsInterestCommand
                                        .builder()
                                        .savingsAccountId(migrationSavingsRequestDto.getSavingsAccountId())
                                        .interestAmount(migrationSavingsRequestDto.getInterest())
                                        .interestPostingDate(requestDto.getConfigurations().getCutOffDate())
                                        .loginId(requestDto.getLoginId())
                                        .officeId(requestDto.getOfficeId())
                                        .build())
                                .thenReturn(collectionResponseDto);
                    } else {
                        return Mono.just(collectionResponseDto);
                    }
                })
                .then(Mono.just(collectionResponseDto));
    });
    }

    public Flux<MigratedComponentsResponseDto> migrate(MigrationRequestDto requestDto) {
        log.info("Migration request received for Office: {}", requestDto.getOfficeId());
        return Flux.fromIterable(requestDto.getMembers())
            .concatMap(memberRequestDto -> {
                log.info("Validating member data before migration for Person: {} with company member Id: {}", memberRequestDto.getMemberNameEn(), memberRequestDto.getCompanyMemberId());
                Tuple2<Boolean, String> validationResponse = MigrationUtilsV3.validateRequestedMemberDataBeforeMigration(memberRequestDto);
                if(validationResponse.getT1()){
                    log.info("Member data validation is Successful for Person: {} with company member Id: {}", memberRequestDto.getMemberNameEn(), memberRequestDto.getCompanyMemberId());
                    return this.savePersonDataIntoDatabase(requestDto, memberRequestDto)
                            .doOnSuccess(responseDto -> log.info("Migration Successful for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                            .doOnError(throwable -> log.error("Error occurred while migrating company member Id : {} | error : {}", memberRequestDto.getCompanyMemberId(), throwable.getMessage()));
                } else {
                    log.error("Member data validation is Failed for Person: {} with company member Id: {}", memberRequestDto.getMemberNameEn(), memberRequestDto.getCompanyMemberId());
                    return Mono.just(this.buildErrorResponseForFailedMigration(memberRequestDto.getCompanyMemberId(), validationResponse.getT2()));
                }
            })
            .doOnError(throwable -> log.error("Error occurred while migrating member data: {}", throwable.getMessage()))
            .doOnNext(responseDto -> log.info("Migrated Response Dto for Member Id {} || MigratedComponentsResponseDto: {}", responseDto.getMember().getMemberId(), responseDto));
    }

    private Mono<MigratedComponentsResponseDto> savePersonDataIntoDatabase(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto){
        log.info("Saving Person Data for Company Member Id: {}", memberRequestDto.getCompanyMemberId());
        return rxtx.transactional(
                migrationOfficeService.getOfficeById(requestDto.getOfficeId())
                    .map(office -> {
                            MigratedComponentsResponseDto responseDto = new MigratedComponentsResponseDto();
                            responseDto.setOffice(office);
                            return responseDto;
                        }
                    )
                    .flatMap(responseDto ->
                            saveEntity(memberRequestDto, requestDto, responseDto,
                                    (mReqDto, reqDto) -> managementProcessTrackerService.saveOnToCutOfDate(reqDto, responseDto),
                                    MigratedComponentsResponseDto::setManagementProcessTracker)
                    )
                    .map(tuple2 -> {
                            requestDto.setManagementProcessId(tuple2.getT1().getManagementProcessTracker().getManagementProcessId());
                            requestDto.setBusinessDate(tuple2.getT1().getManagementProcessTracker().getBusinessDate());
                            return tuple2.getT1();
                        })
                    )
                    .doOnError(throwable -> log.error("Error occurred while saving Office Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(migratedComponentsResponseDto ->
                        migrationSamityService.getSamityById(memberRequestDto.getSamityId())
                            .map(samity -> {
                                migratedComponentsResponseDto.setSamity(samity);
                                return migratedComponentsResponseDto;
                            })
                    )
                    .doOnError(throwable -> log.error("Error occurred while saving Samity Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(migratedComponentsResponseDto ->
                        saveEntity(memberRequestDto, requestDto, migratedComponentsResponseDto, personService::save,
                            MigratedComponentsResponseDto::setPerson)
                    )
                        .doOnError(throwable -> log.error("Error occurred while saving Person Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> memberService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setMember)
                    )
                        .doOnError(throwable -> log.error("Error occurred while saving Member Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> memSmtOffPriMapService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setMemSmtOffPriMap)
                            .flatMap(tuple -> {
                                tuple.getT1().getMember().setMemSmtOffPriMapId(tuple.getT1().getMemSmtOffPriMap().getMemSmtOffPriMapId());
                                return memberService.update(tuple.getT1().getMember())
                                    .map(member -> {
                                        tuple.getT1().setMember(member);
                                        return tuple;
                                    });
                            }))
                        .doOnError(throwable -> log.error("Error occurred while saving MemSmtOffPriMap Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    /*.flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> managementProcessTrackerService.saveOnToCutOfDate(reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setManagementProcessTracker)
                    )*/
                    .doOnError(throwable -> log.error("Error occurred while saving ManagementProcessTracker Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> officeEventTrackerService.save(reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setOfficeEventTracker)
                    )
                    .doOnError(throwable -> log.error("Error occurred while saving OfficeEventTracker Data for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(tupleOfEntityAndComponents -> {
                        if (memberRequestDto.getLoanInformation() != null)
                            return saveLoanInformationIntoDatabase(requestDto, tupleOfEntityAndComponents.getT2(), tupleOfEntityAndComponents.getT1());
                        else return Mono.just(tupleOfEntityAndComponents);
                    })
                    .doOnError(throwable -> log.error("Error occurred while saving Loan Information for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .flatMap(tupleOfEntityAndComponents -> {
                        if (memberRequestDto.getSavingsInformation() != null)
                            return saveSavingsInformationIntoDatabase(requestDto, tupleOfEntityAndComponents.getT2(), tupleOfEntityAndComponents.getT1());
                        else return Mono.just(tupleOfEntityAndComponents);
                    })
                    .doOnError(throwable -> log.error("Error occurred while saving Savings Information for company member Id: {}", memberRequestDto.getCompanyMemberId()))
                    .map(Tuple2::getT1)
            .doOnError(throwable -> log.error("Error occurred while saving Person Data: {}", throwable.getMessage()))
            .onErrorResume(throwable -> Mono.just(this.buildErrorResponseForFailedMigration(memberRequestDto.getCompanyMemberId(), throwable.getMessage())));
    }

    private Mono<Tuple2<MigratedComponentsResponseDto, MigrationMemberRequestDto>> saveLoanInformationIntoDatabase(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto, MigratedComponentsResponseDto components) {
        return Mono.just(components).zipWith(Mono.just(memberRequestDto))
        .flatMap(tupleOfEntityAndComponents ->
            saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                (mReqDto, reqDto) -> loanProductService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                MigratedComponentsResponseDto::setLoanProduct)
        ).flatMap(tupleOfEntityAndComponents ->
            saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                (mReqDto, reqDto) -> serviceChargeChartService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                MigratedComponentsResponseDto::setServiceChargeChart)
        ).flatMap(tupleOfEntityAndComponents ->
            saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                (mReqDto, reqDto) -> loanApplicationService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                MigratedComponentsResponseDto::setLoanApplication)
        ).flatMap(tupleOfEntityAndComponents ->
            saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                (mReqDto, reqDto) -> loanAccountService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                MigratedComponentsResponseDto::setLoanAccount)
        ).flatMap(tupleOfEntityAndComponents -> {
            return validateLoanDisbursement(tupleOfEntityAndComponents.getT1())
                .flatMap(validationResponse -> {
                    if (validationResponse) {
                        return disbursementUseCase.disburseLoanMigrationV3(memberRequestDto,
                                        tupleOfEntityAndComponents.getT1().getLoanAccount().getLoanAccountId(),
                                    tupleOfEntityAndComponents.getT1().getLoanAccount().getActualDisburseDt(), requestDto.getLoginId(),
                                    requestDto.getOfficeId(), /*requestDto.getConfigurations().getServiceChargeCalculationMethod()*/ tupleOfEntityAndComponents.getT1().getLoanProduct().getInterestCalcMethod(),
                                    requestDto.getConfigurations().getCutOffDate(), getNoOfPastInstallment(tupleOfEntityAndComponents.getT2(), requestDto),
                                    tupleOfEntityAndComponents.getT2().getLoanInformation().getInstallmentAmount(),
                                    tupleOfEntityAndComponents.getT2().getLoanInformation().getDisbursedLoanAmount(),
                                    tupleOfEntityAndComponents.getT1().getLoanProduct().getRepaymentFrequency().equalsIgnoreCase("Monthly"),
                                    memberRequestDto.getLoanInformation().getLoanTerm())
                            .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, throwable.getMessage()))
                            .map(disbursementResponseDTO -> {
                                tupleOfEntityAndComponents.getT1().setRepaymentSchedule(disbursementResponseDTO.getRepaymentScheduleResponseDTOList());
                                return tupleOfEntityAndComponents.getT1();
                            }).zipWith(Mono.just(tupleOfEntityAndComponents.getT2()));
                    } else {
                        return Mono.just(tupleOfEntityAndComponents);
                    }
                });
        }).doOnError(throwable -> log.error("Error occurred while saving Loan Information: {}", throwable.getMessage()));
    }

    private Mono<Tuple2<MigratedComponentsResponseDto, MigrationMemberRequestDto>> saveSavingsInformationIntoDatabase(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto, MigratedComponentsResponseDto components) {
        return Mono.just(components).zipWith(Mono.just(memberRequestDto))
            .flatMap(tupleOfEntityAndComponents ->
                saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                    (mReqDto, reqDto) -> migrationSavingsProductService.save(tupleOfEntityAndComponents.getT2(), reqDto, tupleOfEntityAndComponents.getT1()),
                    MigratedComponentsResponseDto::setSavingsProduct)
            )
            .flatMap(tupleOfEntityAndComponents ->
                saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                    (mReqDto, reqDto) -> interestChartService.save(tupleOfEntityAndComponents.getT2(), reqDto, tupleOfEntityAndComponents.getT1()),
                    MigratedComponentsResponseDto::setInterestChart)
            )
            .flatMap(tupleOfEntityAndComponents ->
                saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                    (mReqDto, reqDto) -> savingsAccProposalService.save(tupleOfEntityAndComponents.getT2(), reqDto, tupleOfEntityAndComponents.getT1()),
                    MigratedComponentsResponseDto::setSavingsAccProposal)
            )
            .flatMap(tupleOfEntityAndComponents ->
                saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                    (mReqDto, reqDto) -> savingsAccountService.save(tupleOfEntityAndComponents.getT2(), reqDto, tupleOfEntityAndComponents.getT1()),
                    MigratedComponentsResponseDto::setSavingsAccount)
            )
            .flatMap(tupleOfEntityAndComponents -> {
                if (memberRequestDto.getSavingsInformation() != null && memberRequestDto.getSavingsInformation().getSavingsTypeId() != null &&
                        memberRequestDto.getSavingsInformation().getSavingsTypeId().equalsIgnoreCase("DPS")) {
                    return dpsRepaymentScheduleService.generateDpsRepaymentScheduleMigration(MigrationDPSRepaymentScheduleCommand
                            .builder()
                            .cutOffDate(requestDto.getConfigurations().getCutOffDate())
                            .loginId(requestDto.getLoginId())
                            .savingsAccountId(tupleOfEntityAndComponents.getT1().getSavingsAccount().getSavingsAccountId())
                            .noOfPaidInstallments(MigrationUtilsV3.calculateNoOfPaidDpsInstallment(memberRequestDto.getMemberId(), memberRequestDto.getSavingsInformation().getBalance(),
                                    memberRequestDto.getSavingsInformation().getSavingsAmount()))
                            .build())
                        .flatMap(dpsRepaymentScheduleResponseDTO -> {
                            tupleOfEntityAndComponents.getT1().setDpsRepaymentSchedule(dpsRepaymentScheduleResponseDTO);
                            return Mono.just(tupleOfEntityAndComponents.getT1()).zipWith(Mono.just(tupleOfEntityAndComponents.getT2()));
                        }).doOnError(throwable -> log.error("Error occurred while saving DPS Repayment Schedule: {}", throwable.getMessage()));
                }
                return Mono.just(tupleOfEntityAndComponents.getT1()).zipWith(Mono.just(tupleOfEntityAndComponents.getT2()));
            }).doOnError(throwable -> log.error("Error occurred while saving Savings Information: {}", throwable.getMessage()));
    }

    private Mono<Boolean> validateLoanDisbursement(MigratedComponentsResponseDto components) {
        if (components.getLoanAccount().getStatus().equals(Status.STATUS_APPROVED.getValue())) {
            return loanRepaymentScheduleUseCase
                            .getRepaymentScheduleByLoanAccountId(components.getLoanAccount().getLoanAccountId())
                            .doOnNext(repaymentScheduleResponseDTOS -> log.info(
                                    "Repayment schedule received : {}",
                                    repaymentScheduleResponseDTOS))
                            .map(repaymentScheduleResponseDTOS -> repaymentScheduleResponseDTOS.size() <= 1)
                            .switchIfEmpty(Mono.just(true));
//                    .doOnNext(objects -> log.info("Repayment schedule not found")));
        } else if (components.getLoanAccount().getStatus().equals(Status.STATUS_ACTIVE.getValue())) {
            log.error("Loan Account Status is not Approved for member_id: {}", components.getMember().getMemberId());
            return Mono.just(false);
        } else {
            log.error("Repayment Schedule Generation Validation Failed for member_id: {}", components.getMember().getMemberId());
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                    ExceptionMessages.LOAN_ACCOUNT_STATUS_NOT_APPROVED.getValue()));
        }
    }


    private Integer getNoOfPastInstallment(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto) {
        return MigrationUtilsV3.calculateNoOfPastInstallments(memberRequestDto.getMemberId(),memberRequestDto.getLoanInformation().getDisbursedLoanAmount(),
                memberRequestDto.getLoanInformation().getLoanOutstanding(), memberRequestDto.getLoanInformation().getOverDueAmount(),
                memberRequestDto.getLoanInformation().getNoInstallment(), requestDto.getConfigurations().getInstallmentAmountPrecision(),
                requestDto.getConfigurations().getRoundingMode());
    }

    private <T, R> Mono<Tuple2<R, MigrationMemberRequestDto>> saveEntity(
            MigrationMemberRequestDto memberRequestDto,
            MigrationRequestDto requestDto,
            R responseDto,
            BiFunction<MigrationMemberRequestDto, MigrationRequestDto, Mono<T>> saveFunction,
            BiConsumer<R, T> setFunction) {
        return saveFunction.apply(memberRequestDto, requestDto)
                .map(entity -> {
                    setFunction.accept(responseDto, entity);
                    return responseDto;
                })
                .zipWith(Mono.just(memberRequestDto));
    }


    private MigratedComponentsResponseDto buildErrorResponseForFailedMigration(String id, String message) {
        return MigratedComponentsResponseDto.builder()
                .status("Failed")
                .errorResponse(ErrorResponseDTO.builder()
                        .personId(id)
                        .message(message)
                        .build())
                .build();
    }

    private Mono<Tuple2<Boolean, String>> validateRequestedMemberDataLoanInformationBeforeCollection(MigrationMemberRequestDto memberRequestDto) {
        if (memberRequestDto.getLoanInformation() == null) {
            return Mono.just(Tuples.of(true, "No Loan Information found for member_id: " + memberRequestDto.getMemberId()));
        }
        return loanAccountService
            .getLoanAccountsByMemberId(memberRequestDto.getMemberId())
            .next()
            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Loan Account found for member_id: " + memberRequestDto.getMemberId())))
            .flatMap(loanAccount -> loanRepaymentScheduleUseCase
                .getRepaymentScheduleByLoanAccountId(loanAccount.getLoanAccountId())
                .map(repaymentSchedule -> {
                    if(repaymentSchedule.isEmpty()){
                        return Tuples.of(false, "No Repayment Schedule found for loan_account_id: " + loanAccount.getLoanAccountId());
                    }
                    return Tuples.of(true, "Repayment Schedule found for loan_account_id: " + loanAccount.getLoanAccountId());
                })
                .flatMap(tuple2 -> passbookUseCase.getDisbursementPassbookEntryByDisbursedLoanAccountId(loanAccount.getLoanAccountId())
                    .switchIfEmpty(Mono.just(PassbookResponseDTO.builder().build()))
                    .map(passbookEntry -> {
                        if(passbookEntry.getDisbursedLoanAccountId() == null){
                            return Tuples.of(false, "No Disbursement Passbook Entry found for loan_account_id: " + loanAccount.getLoanAccountId());
                        }
                        return tuple2;
                    })
                ));
    }
}
