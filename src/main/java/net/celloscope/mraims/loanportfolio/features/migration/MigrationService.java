package net.celloscope.mraims.loanportfolio.features.migration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.IAuthorizationUseCase;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.AuthorizationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.DisbursementUseCase;
import net.celloscope.mraims.loanportfolio.features.migration.components.dpsrepaymentschedule.MigrationDPSRepaymentScheduleService;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount.MigrationLoanAccountService;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication.MigrationLoanApplicationService;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanproduct.MigrationLoanProductService;
import net.celloscope.mraims.loanportfolio.features.migration.components.managementprocesstracker.MigrationManagementProcessTrackerService;
import net.celloscope.mraims.loanportfolio.features.migration.components.member.MigrationMemberService;
import net.celloscope.mraims.loanportfolio.features.migration.components.memsmtoffprimap.MigrationMemSmtOffPriMapService;
import net.celloscope.mraims.loanportfolio.features.migration.components.office.MigrationOfficeService;
import net.celloscope.mraims.loanportfolio.features.migration.components.officeeventtracker.MigrationOfficeEventTrackerService;
import net.celloscope.mraims.loanportfolio.features.migration.components.passbook.MigrationPassbookService;
import net.celloscope.mraims.loanportfolio.features.migration.components.person.MigrationPersonService;
import net.celloscope.mraims.loanportfolio.features.migration.components.samity.MigrationSamityService;
import net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount.MigrationSavingsAccountService;
import net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccproposal.MigrationSavingsAccProposalService;
import net.celloscope.mraims.loanportfolio.features.migration.components.savingsproduct.MigrationSavingsProductService;
import net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart.MigrationServiceChargeChartService;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.MigrationStagingService;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingaccountdata.MigrationStagingAccountDataService;
import net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingdata.MigrationStagingDataService;
import net.celloscope.mraims.loanportfolio.features.migration.components.transaction.MigrationTransactionService;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.*;
import net.celloscope.mraims.loanportfolio.features.migration.interestchart.MigrationInterestChartService;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationService {

    private final TransactionalOperator rxtx;
    private final MigrationOfficeService migrationOfficeService;
    private final MigrationSamityService migrationSamityService;
    private final MigrationPersonService personService;
    private final MigrationMemberService memberService;
    private final MigrationMemSmtOffPriMapService memSmtOffPriMapService;
    private final MigrationLoanProductService loanProductService;
    private final MigrationServiceChargeChartService serviceChargeChartService;
    private final MigrationLoanApplicationService loanApplicationService;
    private final MigrationLoanAccountService loanAccountService;
    private final DisbursementUseCase disbursementUseCase;
    private final MigrationManagementProcessTrackerService managementProcessTrackerService;
    private final MigrationOfficeEventTrackerService officeEventTrackerService;
    private final IStagingDataUseCase stagingDataUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final MigrationStagingDataService stagingDataService;
    private final MigrationStagingAccountDataService stagingAccountDataService;
    private final MigrationStagingService migrationStagingService;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final IAuthorizationUseCase authorizationUseCase;
    private final MigrationTransactionService migrationTransactionService;
    private final MigrationPassbookService migrationPassbookService;
    private final MigrationSavingsProductService migrationSavingsProductService;
    private final MigrationInterestChartService interestChartService;
    private final MigrationSavingsAccProposalService savingsAccProposalService;
    private final MigrationSavingsAccountService savingsAccountService;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final PassbookUseCase passbookUseCase;
    private final MigrationDPSRepaymentScheduleService dpsRepaymentScheduleService;



    public Mono<MigratedCollectionResponseDto> migrateCutOffDateCollection(MigrationCollectionRequestDto requestDto) {
        log.info("Migration request received for {}", requestDto);
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
                                log.info("MigratedCollectionResponseDto: {}", responseDto);
                                return responseDto;
                            })
                            .flatMap(responseDto -> managementProcessTrackerService.getByOfficeId(requestDto.getOfficeId())
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Management Process Tracker Entry not found by office_id : " + requestDto.getOfficeId())))
                                    .map(managementProcessTracker -> {
                                        responseDto.setManagementProcessTracker(managementProcessTracker);
                                        log.info("MigratedCollectionResponseDto: {}", responseDto);
                                        return responseDto;
                                    })
                            )
                            .flatMap(responseDto -> officeEventTrackerService.getByOfficeId(requestDto, responseDto)
                                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No DAY_STARTED Office Event Tracker Entry found by office_id : " + requestDto.getOfficeId())))
                                    .map(officeEventTracker -> {
                                        responseDto.setOfficeEventTracker(List.of(officeEventTracker));
                                        log.info("MigratedCollectionResponseDto: {}", responseDto);
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
                                        log.info("MigratedCollectionResponseDto: {}", responseDto);
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
                                        log.info("Repeating operation because status is not Finished");
                                        return Mono.delay(Duration.ofSeconds(1));
                                    })))
                                        .flatMap(stagedResponseDto ->
                                                stagingDataService.getByManagementProcessId(stagedResponseDto.getManagementProcessTracker().getManagementProcessId())
                                                        .collectList()
                                                        .map(stagingDataEntities -> {
                                                            stagedResponseDto.setStagingData(stagingDataEntities);
                                                            log.info("MigratedCollectionResponseDto with staging data: {}", stagedResponseDto);
                                                            return stagedResponseDto;
                                                        })
                                        )
                                        .flatMap(stagedResponseDto ->
                                                stagingAccountDataService.getByManagementProcessId(stagedResponseDto.getManagementProcessTracker().getManagementProcessId())
                                                        .collectList()
                                                        .map(stagingAccountDataEntities -> {
                                                            stagedResponseDto.setStagingAccountData(stagingAccountDataEntities);
                                                            log.info("MigratedCollectionResponseDtowith staging account data: {}", stagedResponseDto);
                                                            return stagedResponseDto;
                                                        })
                                        )
                                        .flatMap(stagedResponseDto -> migrationStagingService.buildPaymentCollectionBySamityCommand(requestDto, stagedResponseDto)
                                                .map(paymentCollectionBySamityCommands -> {
                                                    stagedResponseDto.setCollections(paymentCollectionBySamityCommands);
                                                    log.info("MigratedCollectionResponseDto with collection data: {}", stagedResponseDto);
                                                    return stagedResponseDto;
                                                })
                                        )
                                        .flatMap(stagedResponseDto ->
                                                Flux.fromIterable(stagedResponseDto.getCollections())
                                                        .flatMap(paymentCollectionUseCase::collectPaymentBySamity)
                                                        .then(Mono.just(stagedResponseDto))
                                        )
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
                                        .flatMap(collectedResponseDto -> {
                                            return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getSamityId).collect(Collectors.toSet()))
                                                    .buffer(1)
                                                    .concatMap(samityIds -> authorizationUseCase.authorizeSamityList(AuthorizationRequestDTO.builder()
                                                            .officeId(requestDto.getOfficeId())
                                                            .samityIdList(samityIds)
                                                            .loginId(requestDto.getLoginId())
                                                            .mfiId(requestDto.getMfiId())
                                                            .build())
                                                    ).then(Mono.just(collectedResponseDto));
                                        })
                                        .flatMap(collectedResponseDto -> {
                                            return Flux.fromIterable(requestDto.getMembers().stream().map(MigrationMemberRequestDto::getMemberId).collect(Collectors.toSet()))
                                                    .collectList()
                                                    .flatMap(memberIds -> migrationTransactionService.getByMemberIdList(memberIds)
                                                            .collectList()
                                                            .map(transactionEntities -> {
                                                                collectedResponseDto.setTransactions(transactionEntities);
                                                                log.info("MigratedCollectionResponseDto with transaction data: {}", collectedResponseDto);
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
                                                                log.info("MigratedCollectionResponseDto with passbook data: {}", collectedResponseDto);
                                                                return collectedResponseDto;
                                                            })
                                                    );
                                        })

//                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()).subscribe()
                            )
            ))



        .doOnSuccess(responseDto -> log.info("MigratedCollectionResponseDto: {}", responseDto));
    }

    public Flux<MigratedComponentsResponseDto> migrate(MigrationRequestDto requestDto) {
        log.info("Migration request received for Office: {}", requestDto.getOfficeId());
        return Flux.fromIterable(requestDto.getMembers())
            .concatMap(memberRequestDto -> {
                log.info("Validating member data before migration for Person: {} with company member Id: {}", memberRequestDto.getMemberName(), memberRequestDto.getCompanyMemberId());
                Tuple2<Boolean, String> validationResponse = MigrationUtils.validateRequestedMemberDataBeforeMigration(memberRequestDto);
                if(validationResponse.getT1()){
                    log.info("Member data validation is Successful for Person: {} with company member Id: {}", memberRequestDto.getMemberName(), memberRequestDto.getCompanyMemberId());
                    return this.savePersonDataIntoDatabase(requestDto, memberRequestDto);
                } else {
                    log.error("Member data validation is Failed for Person: {} with company member Id: {}", memberRequestDto.getMemberName(), memberRequestDto.getCompanyMemberId());
                    return Mono.just(this.buildErrorResponseForFailedMigration(memberRequestDto.getCompanyMemberId(), validationResponse.getT2()));
                }
            })
            .doOnError(throwable -> log.error("Error occurred while migrating member data: {}", throwable.getMessage()))
            .doOnNext(responseDto -> log.info("Migrated Response Dto for Member Id {} || MigratedComponentsResponseDto: {}", responseDto.getMember().getMemberId(), responseDto));
    }

    private Mono<MigratedComponentsResponseDto> savePersonDataIntoDatabase(MigrationRequestDto requestDto, MigrationMemberRequestDto memberRequestDto){
        return rxtx.transactional(
                migrationOfficeService.getOfficeById(requestDto.getOfficeId())
                    .map(office -> {
                            MigratedComponentsResponseDto responseDto = new MigratedComponentsResponseDto();
                            responseDto.setOffice(office);
                            return responseDto;
                        }
                    )
                    .flatMap(migratedComponentsResponseDto ->
                        migrationSamityService.getSamityById(memberRequestDto.getSamityId())
                            .map(samity -> {
                                migratedComponentsResponseDto.setSamity(samity);
                                return migratedComponentsResponseDto;
                            })
                    )
                    .flatMap(migratedComponentsResponseDto ->
                        saveEntity(memberRequestDto, requestDto, migratedComponentsResponseDto, personService::save,
                            MigratedComponentsResponseDto::setPerson)
                    )
                    .flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> memberService.save(mReqDto, reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setMember)
                    )
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
                    .flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> managementProcessTrackerService.saveOnToCutOfDate(reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setManagementProcessTracker)
                    ).flatMap(tupleOfEntityAndComponents ->
                        saveEntity(tupleOfEntityAndComponents.getT2(), requestDto, tupleOfEntityAndComponents.getT1(),
                            (mReqDto, reqDto) -> officeEventTrackerService.save(reqDto, tupleOfEntityAndComponents.getT1()),
                            MigratedComponentsResponseDto::setOfficeEventTracker)
                    ).flatMap(tupleOfEntityAndComponents -> {
                        if (memberRequestDto.getLoanInformation() != null)
                            return saveLoanInformationIntoDatabase(requestDto, tupleOfEntityAndComponents.getT2(), tupleOfEntityAndComponents.getT1());
                        else return Mono.just(tupleOfEntityAndComponents);
                    })
                    .flatMap(tupleOfEntityAndComponents -> {
                        if (memberRequestDto.getSavingsInformation() != null)
                            return saveSavingsInformationIntoDatabase(requestDto, tupleOfEntityAndComponents.getT2(), tupleOfEntityAndComponents.getT1());
                        else return Mono.just(tupleOfEntityAndComponents);
                    })
                    .map(Tuple2::getT1)
            )
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
                        return disbursementUseCase.disburseLoanMigration(tupleOfEntityAndComponents.getT1().getLoanAccount().getLoanAccountId(),
                                    tupleOfEntityAndComponents.getT1().getLoanAccount().getActualDisburseDt(), requestDto.getLoginId(),
                                    requestDto.getOfficeId(), requestDto.getConfigurations().getServiceChargeCalculationMethod(),
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
                            .noOfPaidInstallments(MigrationUtils.calculateNoOfPaidDpsInstallment(memberRequestDto.getMemberId(), memberRequestDto.getSavingsInformation().getBalance(),
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
        return MigrationUtils.calculateNoOfPastInstallments(memberRequestDto.getMemberId(),memberRequestDto.getLoanInformation().getDisbursedLoanAmount(),
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
