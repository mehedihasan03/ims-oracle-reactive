package net.celloscope.mraims.loanportfolio.features.welfarefund.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.StatusYesNo;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberSamityOfficeEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.OfficeEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity.LoanAccountDetailsEntity;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.WelfareFundUseCase;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request.WelfareFundRequestDto;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.out.WelfareFundPersistencePort;
import net.celloscope.mraims.loanportfolio.features.welfarefund.domain.WelfareFund;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class WelfareFundService implements WelfareFundUseCase {
    private final WelfareFundPersistencePort welfareFundPort;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final CommonRepository commonRepository;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final ModelMapper modelMapper;
    private final TransactionalOperator rxtx;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;

    public WelfareFundService(WelfareFundPersistencePort welfareFundPort,
                              ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
                              CommonRepository commonRepository, TransactionUseCase transactionUseCase, PassbookUseCase passbookUseCase, ModelMapper modelMapper, TransactionalOperator rxtx, SamityEventTrackerUseCase samityEventTrackerUseCase) {
        this.welfareFundPort = welfareFundPort;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.commonRepository = commonRepository;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.modelMapper = modelMapper;
        this.rxtx = rxtx;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
    }

    @Override
    public Mono<WelfareFundGridViewResponseDto> gridViewOfWelfareFundByOffice(WelfareFundRequestDto requestDto) {
        return commonRepository.getOfficeEntityByOfficeId(requestDto.getOfficeId())
                .flatMap(officeEntity -> welfareFundPort.getWelfareFundByOfficeId(requestDto.getOfficeId(), requestDto.getLimit(), requestDto.getOffset())
                        .flatMap(this::getMemberAndLoanAmountData)
                        .collectList()
                        .map(loanAccountDataList -> buildWelFareFundResponseDto(requestDto, officeEntity, loanAccountDataList)));
    }

    private Mono<LoanAccountData> getMemberAndLoanAmountData(WelfareFund welfareFund) {
        return commonRepository.getMemberInfoByLoanAccountId(welfareFund.getLoanAccountId())
                .zipWith(commonRepository.getLoanAmountByLoanAccountId(welfareFund.getLoanAccountId()))
                .map(tuple -> buildWelfareGridViewList(welfareFund, tuple.getT1(), tuple.getT2()));
    }

    private LoanAccountData buildWelfareGridViewList(WelfareFund welfareFund, MemberEntity member, BigDecimal loanAmount) {
        return LoanAccountData.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(welfareFund.getLoanAccountId())
                .loanAmount(loanAmount)
                .transactionDate(welfareFund.getTransactionDate())
                .amount(welfareFund.getAmount())
                .status(welfareFund.getStatus())
                .btnUpdateEnabled(StringUtils.equals(welfareFund.getStatus(), Status.STATUS_PENDING.getValue())
                                ? StatusYesNo.Yes.toString()
                                : StatusYesNo.No.toString()
                )
                .build();
    }

    private WelfareFundGridViewResponseDto buildWelFareFundResponseDto(WelfareFundRequestDto requestDto, OfficeEntity officeEntity, List<LoanAccountData> loanAccountDataList) {
        List<LoanAccountData> sortedLoanAccountList = loanAccountDataList.stream().sorted(Comparator.comparing(LoanAccountData::getTransactionDate).reversed()).toList();
        return WelfareFundGridViewResponseDto.builder()
                .officeId(requestDto.getOfficeId())
                .officeNameEn(officeEntity.getOfficeNameEn())
                .officeNameBn(officeEntity.getOfficeNameBn())
                .data(sortedLoanAccountList)
                .totalCount(sortedLoanAccountList.size())
                .build();
    }

    @Override
    public Mono<WelfareFundDetailsViewResponseDto> getWelfareFundDetailView(WelfareFundRequestDto requestDto) {
        return welfareFundPort.getWelfareFundByLoanAccountIdAndTransactionDate(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Data not found")))
                .flatMap(welfareFund -> commonRepository.getMemberInfoByLoanAccountId(welfareFund.getLoanAccountId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Member Data not found")))
                        .zipWith(commonRepository.getLoanAccountEntityByLoanAccountId(welfareFund.getLoanAccountId()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan Account Data not found")))
                        .flatMap(tuple -> commonRepository.getServiceChargeByLoanAccountId(welfareFund.getLoanAccountId())
                                .map(serviceCharge -> buildWelfareDetailsViewResponse(requestDto, tuple.getT1(), tuple.getT2(), welfareFund, serviceCharge))));
    }

    private WelfareFundDetailsViewResponseDto buildWelfareDetailsViewResponse(WelfareFundRequestDto requestDto, MemberEntity member, LoanAccountDetailsEntity loanAccount, WelfareFund welfareFund, BigDecimal serviceCharge) {
        return WelfareFundDetailsViewResponseDto.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(requestDto.getLoanAccountId())
                .loanProductId(loanAccount.getLoanProductId())
                .loanProductNameEn(loanAccount.getLoanProductNameEn())
                .loanProductNameBn(loanAccount.getLoanProductNameBn())
                .loanAmount(loanAccount.getLoanAmount())
                .serviceCharge(serviceCharge)
                .totalLoanAmount(loanAccount.getLoanAmount().add(serviceCharge))
                .paymentMethod(welfareFund.getPaymentMode())
                .referenceNo(welfareFund.getReferenceNo())
                .transactionDate(requestDto.getTransactionDate())
                .amount(HelperUtil.getFormattedBigDecimal(welfareFund.getAmount()))
                .status(welfareFund.getStatus())
                .btnUpdateEnabled(StringUtils.equals(welfareFund.getStatus(), Status.STATUS_PENDING.getValue()) ? StatusYesNo.Yes.toString() : StatusYesNo.No.toString())
                .build();
    }

    @Override
    public Mono<LoanAccountDetailsResponseDto> loanAccountDetailsByLoanAccountId(WelfareFundRequestDto requestDto) {
        return commonRepository.getMemberInfoByLoanAccountId(requestDto.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Member Data not found")))
                .zipWith(commonRepository.getLoanAccountEntityByLoanAccountId(requestDto.getLoanAccountId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan Account Data not found")))
                .flatMap(tuple -> commonRepository.getServiceChargeByLoanAccountId(requestDto.getLoanAccountId())
                        .map(serviceCharge -> buildLoanAccountAndMemberDetails(tuple.getT1(), tuple.getT2(), serviceCharge)));
    }

    private LoanAccountDetailsResponseDto buildLoanAccountAndMemberDetails(MemberEntity member, LoanAccountDetailsEntity loanAccount, BigDecimal serviceCharge) {
        return LoanAccountDetailsResponseDto.builder()
                .memberId(member.getMemberId())
                .memberNameEn(member.getMemberNameEn())
                .memberNameBn(member.getMemberNameBn())
                .loanAccountId(loanAccount.getLoanAccountId())
                .loanProductId(loanAccount.getLoanProductId())
                .loanProductNameEn(loanAccount.getLoanProductNameEn())
                .loanProductNameBn(loanAccount.getLoanProductNameBn())
                .loanAmount(HelperUtil.getFormattedBigDecimal(loanAccount.getLoanAmount()))
                .serviceCharge(HelperUtil.getFormattedBigDecimal(serviceCharge))
                .totalLoanAmount(HelperUtil.getFormattedBigDecimal(loanAccount.getLoanAmount().add(serviceCharge)))
                .status(loanAccount.getStatus())
                .build();
    }


    @Override
    public Mono<WelfareFundSaveResponseDto> saveCollectedWelfareFund(WelfareFundRequestDto requestDto) {
        return commonRepository.getMemberSamityOfficeInfoByLoanAccountId(requestDto.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Loan Account Data not found for this loan account id : " + requestDto.getLoanAccountId())))
                .flatMap(memberSamityOfficeEntity -> {
                    if (memberSamityOfficeEntity.getSamityId() == null) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Id not found for this loan account id : " + requestDto.getLoanAccountId()));
                    }
                    requestDto.setSamityId(memberSamityOfficeEntity.getSamityId());
                    return Mono.just(memberSamityOfficeEntity);
                })
                .filter(memberSamityOfficeEntity -> memberSamityOfficeEntity.getLoanAccountStatus().equals(Status.STATUS_ACTIVE.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account is not active. Cannot collect welfare fund.")))
                .flatMap(memberSamityOfficeEntity -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .flatMap(managementProcessTracker -> saveWelfareFundData(requestDto, managementProcessTracker))
                .map(date -> WelfareFundSaveResponseDto.builder().userMessage("Welfare Fund Collection for Loan Account Is Successful").build()))
                .as(rxtx::transactional);
    }

    private Mono<Object> saveWelfareFundData(WelfareFundRequestDto requestDto, ManagementProcessTracker managementProcessTracker) {
        return welfareFundPort.getWelfareFundByBusinessDate(requestDto.getLoanAccountId(), requestDto.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(welfareFund -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Welfare fund already collected on this business date for this account : " + requestDto.getLoanAccountId())))
                .switchIfEmpty(welfareFundPort
                        .saveCollectedFundData(buildWelfareFundForSaveCollectedData(requestDto, managementProcessTracker)));
    }

    private WelfareFund buildWelfareFundForSaveCollectedData(WelfareFundRequestDto requestDto, ManagementProcessTracker managementProcessTracker) {
        return WelfareFund.builder()
                .welfareFundDataId(String.valueOf(UUID.randomUUID()))
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .officeId(requestDto.getOfficeId())
                .loanAccountId(requestDto.getLoanAccountId())
                .amount(requestDto.getAmount())
                .paymentMode(requestDto.getPaymentMethod().toUpperCase())
                .referenceNo(requestDto.getReferenceNo())
                .samityId(requestDto.getSamityId())
                .isNew(StatusYesNo.Yes.toString())
                .currentVersion(1)
                .status(Status.STATUS_PENDING.getValue())
                .transactionDate(managementProcessTracker.getBusinessDate())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())
                .build();
    }

    @Override
    public Mono<WelfareFundSaveResponseDto> updateCollectedWelfareFund(WelfareFundRequestDto requestDto) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .flatMap(managementProcess -> welfareFundPort.getWelfareFundByBusinessDate(requestDto.getLoanAccountId(), requestDto.getOfficeId(), managementProcess.getBusinessDate()))
                .filter(welfareFund -> !HelperUtil.checkIfNullOrEmpty(welfareFund.getLoanAccountId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Not Found")))
                .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Cannot be Updated")))
                .flatMap(welfareFund -> welfareFundPort.saveCollectedFundData(buildWelfareFundForUpdate(requestDto, welfareFund)))
                .map(welfareFund -> WelfareFundSaveResponseDto.builder().userMessage("Welfare Fund Update for Loan Account Is Successful").build())
                .as(rxtx::transactional);
    }

    @Override
    public Mono<WelfareFundLoanAccountDetailViewResponseDTO> getWelfareFundDetailsForLoanAccount(WelfareFundRequestDto requestDto) {
        return commonRepository.getLoanAccountEntityByLoanAccountId(requestDto.getLoanAccountId())
                .switchIfEmpty(Mono.just(LoanAccountDetailsEntity.builder()
                        .loanAccountId(requestDto.getLoanAccountId())
                        .build()))
                .map(loanAccountEntity -> WelfareFundLoanAccountDetailViewResponseDTO.builder()
                        .loanAccountId(requestDto.getLoanAccountId())
                        .loanProductId(loanAccountEntity.getLoanProductId())
                        .loanProductNameEn(loanAccountEntity.getLoanProductNameEn())
                        .loanProductNameBn(loanAccountEntity.getLoanProductNameBn())
                        .loanAmount(loanAccountEntity.getLoanAmount())
                        .build())
                .flatMap(responseDTO -> commonRepository.getServiceChargeByLoanAccountId(responseDTO.getLoanAccountId())
                        .switchIfEmpty(Mono.just(BigDecimal.ZERO))
                        .map(serviceCharge -> {
                            if(serviceCharge == null || responseDTO.getLoanAmount() == null || serviceCharge.compareTo(BigDecimal.ZERO) == 0) {
                                return responseDTO;
                            }
                            responseDTO.setServiceCharge(serviceCharge);
                            responseDTO.setTotalLoanAmount(responseDTO.getLoanAmount().add(serviceCharge));
                            return responseDTO;
                        }))
                .flatMap(responseDTO -> commonRepository.getMemberInfoByLoanAccountId(responseDTO.getLoanAccountId())
                        .switchIfEmpty(Mono.just(MemberEntity.builder().build()))
                        .map(memberEntity -> {
                            responseDTO.setMemberId(memberEntity.getMemberId());
                            responseDTO.setMemberNameEn(memberEntity.getMemberNameEn());
                            responseDTO.setMemberNameBn(memberEntity.getMemberNameBn());
                            return responseDTO;
                        }))
                .flatMap(responseDTO -> welfareFundPort.getWelfareFundByLoanAccountId(requestDto.getLoanAccountId())
                        .switchIfEmpty(Mono.just(WelfareFund.builder()
                                .loanAccountId(responseDTO.getLoanAccountId())
                                .build()))
                        .map(welfareFund -> {
                            WelfareFundLoanAccountData fundLoanAccountData = modelMapper.map(welfareFund, WelfareFundLoanAccountData.class);
                            fundLoanAccountData.setPaymentMethod(welfareFund.getPaymentMode());
                            return fundLoanAccountData;
                        })
                        .filter(welfareFundLoanAccountData -> welfareFundLoanAccountData.getTransactionDate() != null)
                        .filter(welfareFundLoanAccountData -> welfareFundLoanAccountData.getStatus().equals(Status.STATUS_APPROVED.getValue()))
                        .collectList()
                        .map(welfareFundList -> {
                            responseDTO.setData(welfareFundList);
                            responseDTO.setTotalCount(welfareFundList.size());
                            return responseDTO;
                        }));
    }

    @Override
    public Mono<WelfareFundSaveResponseDto> authorizeWelfareFundDataByLoanAccountId(WelfareFundRequestDto requestDto) {
        return welfareFundPort.getWelfareFundByLoanAccountIdAndTransactionDate(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Not Found")))
                .doOnNext(welfareFund -> log.info("Welfare Fund Data: {}", welfareFund))
                .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Cannot be Authorized")))
                .flatMap(welfareFund -> welfareFundPort.authorizeWelfareFundData(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()), requestDto.getLoginId()))
                .flatMap(this::createTransactionAndPassbookEntryForWelfareFund)
                .map(string -> WelfareFundSaveResponseDto.builder().userMessage(string).build())
                .as(rxtx::transactional);
    }

    private Mono<String> createTransactionAndPassbookEntryForWelfareFund(WelfareFund welfareFund) {
        AtomicReference<String> memberId = new AtomicReference<>();
        return commonRepository.getMemberInfoByLoanAccountId(welfareFund.getLoanAccountId())
                .doOnNext(memberEntity -> memberId.set(memberEntity.getMemberId()))
                .flatMap(memberEntity -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(welfareFund.getOfficeId()))
                .flatMap(managementProcessTracker -> transactionUseCase.createTransactionForWelfareFund(Transaction.builder()
                        .managementProcessId(welfareFund.getManagementProcessId())
                        .processId(UUID.randomUUID().toString())
                        .transactionId(UUID.randomUUID().toString())
                        .mfiId(managementProcessTracker.getMfiId())
                        .officeId(welfareFund.getOfficeId())
                        .memberId(memberId.get())
                        .loanAccountId(welfareFund.getLoanAccountId())
                        .accountType("Loan")
                        .transactionCode(TransactionCodes.WELFARE_FUND.getValue())
                        .amount(welfareFund.getAmount())
                        .paymentMode(welfareFund.getPaymentMode())
                        .transactionDate(welfareFund.getTransactionDate())
                        .createdOn(LocalDateTime.now())
                        .createdBy(welfareFund.getCreatedBy())
                        .transactedBy(welfareFund.getCreatedBy())
                        .status(Status.STATUS_APPROVED.getValue())
                        .build()))
                .flatMap(transaction -> passbookUseCase.createPassbookEntryForWelfareFund(Passbook.builder()
                        .managementProcessId(transaction.getManagementProcessId())
                        .processId(transaction.getProcessId())
                        .transactionId(transaction.getTransactionId())
                        .transactionCode(transaction.getTransactionCode())
                        .transactionDate(transaction.getTransactionDate())
                        .mfiId(transaction.getMfiId())
                        .officeId(transaction.getOfficeId())
                        .memberId(transaction.getMemberId())
                        .welfareFundLoanAccountId(transaction.getLoanAccountId())
                        .welfareFundAmount(transaction.getAmount())
                        .paymentMode(transaction.getPaymentMode())
                        .referenceId(welfareFund.getReferenceNo())
                        .status(Status.STATUS_ACTIVE.getValue())
                        .createdBy(transaction.getCreatedBy())
                        .createdOn(LocalDateTime.now())
                        .build()))
                .map(passbook -> "Welfare Fund Authorization for Loan Account Is Successful");
    }

    @Override
    public Mono<WelfareFundSaveResponseDto> rejectWelfareFundDataByLoanAccountId(WelfareFundRequestDto requestDto) {
        return welfareFundPort.getWelfareFundByLoanAccountIdAndTransactionDate(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Not Found")))
                .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Cannot be Rejected")))
                .flatMap(welfareFund -> welfareFundPort.rejectWelfareFundData(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()), requestDto.getLoginId()))
                .map(welfareFund -> WelfareFundSaveResponseDto.builder().userMessage("Welfare Fund Rejection for Loan Account Is Successful").build())
                .as(rxtx::transactional);
    }

    @Override
    public Mono<WelfareFundGridViewResponseDto> gridViewOfWelfareFundDataByOfficeForAuthorization(WelfareFundRequestDto requestDto) {
        return commonRepository.getOfficeEntityByOfficeId(requestDto.getOfficeId())
                .flatMap(officeEntity -> welfareFundPort.getPendingWelfareFundByOfficeId(requestDto.getOfficeId())
                        .flatMap(this::getMemberAndLoanAmountData)
                        .doOnNext(loanAccountData -> loanAccountData.setBtnUpdateEnabled(null))
                        .collectList()
                        .map(loanAccountDataList -> buildWelFareFundResponseDto(requestDto, officeEntity, loanAccountDataList)));
    }

    @Override
    public Mono<WelfareFundDetailsViewResponseDto> detailViewOfWelfareFundDataByLoanAccountForAuthorization(WelfareFundRequestDto requestDto) {
        return welfareFundPort.getWelfareFundByLoanAccountIdAndTransactionDate(requestDto.getLoanAccountId(), LocalDate.parse(requestDto.getTransactionDate()))
                .doOnNext(welfareFund -> log.info("Welfare Fund Data: {}", welfareFund))
                .flatMap(welfareFund -> commonRepository.getMemberInfoByLoanAccountId(welfareFund.getLoanAccountId())
                        .doOnNext(memberEntity -> log.info("Member Data: {}", memberEntity))
                        .zipWith(commonRepository.getLoanAccountEntityByLoanAccountId(welfareFund.getLoanAccountId()))
                        .flatMap(tuple -> commonRepository.getServiceChargeByLoanAccountId(welfareFund.getLoanAccountId())
                                .map(serviceCharge -> buildWelfareDetailsViewResponse(requestDto, tuple.getT1(), tuple.getT2(), welfareFund, serviceCharge))))
                .map(responseDTO -> {
                    responseDTO.setBtnUpdateEnabled(null);
                    responseDTO.setBtnAuthorizeEnabled(responseDTO.getStatus().equals(Status.STATUS_PENDING.getValue()) ? "Yes" : "No");
                    responseDTO.setBtnRejectEnabled(responseDTO.getBtnAuthorizeEnabled());
                    return responseDTO;
                })
                .doOnError(throwable -> log.error("Error in Authorization detail View of welfare Fund: {}", throwable.getMessage()));
    }

    @Override
    public Flux<WelfareFund> getAllWelfareFundTransactionForOfficeOnABusinessDay(String managementProcessId, String officeId) {
        return welfareFundPort.getAllWelfareFundTransactionForOfficeOnABusinessDay(managementProcessId, officeId);
    }

    private WelfareFund buildWelfareFundForUpdate(WelfareFundRequestDto requestDto, WelfareFund welfareFund) {
        welfareFund.setAmount(requestDto.getAmount() == null ? welfareFund.getAmount() : requestDto.getAmount());
        welfareFund.setCurrentVersion(welfareFund.getCurrentVersion() + 1);
        welfareFund.setIsNew(StatusYesNo.No.toString());
        return welfareFund;
    }

    @Override
    public Mono<WelfareFundSaveResponseDto> resetWelfareFundData(WelfareFundRequestDto requestDto) {
        return welfareFundPort.getWelfareFundByOid(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data Not Found")))
                .filter(welfareFund -> welfareFund.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Welfare Fund Data is Already Approved")))
                .filter(welfareFund -> welfareFund.getCreatedBy().equals(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Only Creator Can Reset Welfare Fund Data")))
                .flatMap(welfareFund -> welfareFundPort.deleteWelfareFundData(requestDto.getId()))
                .map(success -> WelfareFundSaveResponseDto.builder().userMessage("Welfare Fund Reset for Loan Account Is Successful").build());
    }
}
