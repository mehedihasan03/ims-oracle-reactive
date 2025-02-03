package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.service.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.StatusYesNo;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionDetailView;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.OfficeEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.helpers.dto.LoanAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedLoanData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.LoanAdjustmentMemberGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.LoanWaiverCreateUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.LoanWaiverUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.ServiceChargeChartResponseDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.celloscope.mraims.loanportfolio.core.util.enums.CollectionType.WAIVER;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.ACCOUNT_TYPE_LOAN;
import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.PAYMENT_MODE_CASH;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanWaiverUtil {

    private final ModelMapper modelMapper;

    public Mono<Boolean> validateLoanWaiverCreateRequest(LoanWaiverCreateUpdateRequestDTO requestDto){
       return Mono.just(requestDto)
               .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getLoanAccountId()))
               .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Loan Account Id is required")))
               .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getSamityId()))
               .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Samity Id is required")))
               .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getMemberId()))
               .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Member Id is required")))
               .filter(dto -> dto.getWaivedAmount() != null && dto.getWaivedAmount().compareTo(BigDecimal.ZERO) > 0)
               .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Valid Waived Amount is required")))
               .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getCollectionType()) &&
                        (dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue()) ||
                                dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue()) ||
                                dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue())))
               .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Collection Type is required, Must be 'Cash' or 'Adjustment' or 'Combine'")))
               .map(dto -> Boolean.TRUE)
               ;
    }

    public Mono<Boolean> validateLoanWaiverUpdateRequest(LoanWaiverCreateUpdateRequestDTO requestDto){
        return Mono.just(requestDto)
                .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Id is required")))
                .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getLoanAccountId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Loan Account Id is required")))
                .filter(dto -> dto.getWaivedAmount() != null && dto.getWaivedAmount().compareTo(BigDecimal.ZERO)  > 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Valid Waived Amount is required")))
                .filter(dto -> !HelperUtil.checkIfNullOrEmpty(dto.getCollectionType()) &&
                        (dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue()) ||
                                dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue()) ||
                                dto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,"Collection Type is required, Must be 'Cash' or 'Adjustment' or 'Combine'")))
                .map(dto -> Boolean.TRUE)
                ;
    }

    public Mono<Boolean> validatePayment(LoanWaiverCreateUpdateRequestDTO request, BigDecimal totalDue) {
        return Mono.just(request)
                .filter(req -> {
                    if (req.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue())) {
                        return req.getCollectedAmountByCash().compareTo(req.getPayableAmount()) == 0;
                    } else if (req.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue())) {
                        BigDecimal totalAdjustedAmount = req.getAdjustedAccountList().stream()
                                .map(AdjustedSavingsAccount::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        return totalAdjustedAmount.compareTo(req.getPayableAmount()) == 0;
                    } else {
                        BigDecimal totalAdjustedAmount = req.getAdjustedAccountList().stream()
                                .map(AdjustedSavingsAccount::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        return totalAdjustedAmount.add(req.getCollectedAmountByCash()).compareTo(req.getPayableAmount()) == 0;
                    }
                })
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid payable amount")))
                .filter(req -> req.getWaivedAmount().add(req.getPayableAmount()).compareTo(totalDue) == 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid waived amount")))
                .map(valid -> Boolean.TRUE);
    }

    public LoanWaiver updateStatusToSubmitLoanWaiverDataForAuthorization(LoanWaiver loanWaiver, String loginId){
        loanWaiver.setEditCommit(StatusYesNo.Yes.toString());
        loanWaiver.setStatus(Status.STATUS_SUBMITTED.getValue());
        loanWaiver.setIsSubmitted(StatusYesNo.Yes.toString());
        loanWaiver.setSubmittedBy(loginId);
        loanWaiver.setSubmittedOn(LocalDateTime.now());
        loanWaiver.setEditCommit(Status.STATUS_NO.getValue());
        return loanWaiver;
    }

    public LoanWaiver updateLoanWaiverData(LoanWaiver loanWaiver,
                                           LoanWaiverCreateUpdateRequestDTO requestDto,
                                           StagingAccountData stagingAccountData){
        loanWaiver.setLockedBy(null);
        loanWaiver.setLockedOn(null);
        loanWaiver.setStatus(Status.STATUS_STAGED.getValue());
        loanWaiver.setUpdatedBy(requestDto.getLoginId());
        loanWaiver.setUpdatedOn(LocalDateTime.now());
        loanWaiver.setCurrentVersion(loanWaiver.getCurrentVersion() + 1);
        loanWaiver.setIsNew("No");
        loanWaiver.setLoanAccountId(requestDto.getLoanAccountId());
        loanWaiver.setWaivedAmount(requestDto.getWaivedAmount());
        loanWaiver.setPayableAmount(requestDto.getPayableAmount());
        loanWaiver.setPaymentMode(requestDto.getCollectionType());
        loanWaiver.setLoanInfo(convertStagingAccountDataToLoanInfo(stagingAccountData));
        loanWaiver.setEditCommit(Status.STATUS_YES.getValue());
        return loanWaiver;
    }

    private LoanAccountDetails convertStagingAccountDataToLoanInfo(StagingAccountData stagingAccountData){
        return LoanAccountDetails.builder()
                .loanAccountId(stagingAccountData.getLoanAccountId())
                .loanAmount(stagingAccountData.getLoanAmount())
                .serviceCharge(stagingAccountData.getServiceCharge())
                .totalLoanAmount(stagingAccountData.getLoanAmount())
                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()))
                .advancePaid(stagingAccountData.getTotalAdvance())
                .build();
    }

    public LoanAdjustmentRequestDTO buildLoanAdjustmentRequestDTO(LoanWaiverCreateUpdateRequestDTO requestDto,
                                                                  LoanWaiver loanWaiver){
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(loanWaiver.getManagementProcessId())
                .processId(loanWaiver.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(HelperUtil.checkIfNullOrEmpty(requestDto.getSamityId()) ? loanWaiver.getSamityId() : requestDto.getSamityId())
                .memberId(HelperUtil.checkIfNullOrEmpty(requestDto.getMemberId()) ? loanWaiver.getMemberId() : requestDto.getMemberId())
                .adjustmentType(WAIVER.getValue())
                .currentVersion(loanWaiver.getCurrentVersion())
                .data(Arrays.asList(AdjustedLoanData.builder()
                        .loanAccountId(requestDto.getLoanAccountId())
                        .adjustedAccountList(requestDto.getAdjustedAccountList().stream()
                                .map(src -> modelMapper.map(src, AdjustedAccount.class)).toList()).build()))
                .build();
    }

    public LoanAdjustmentRequestDTO buildLoanAdjustmentRequestDTOForDelete(LoanWaiverCreateUpdateRequestDTO requestDto,
                                                                  LoanWaiver loanWaiver){
        return LoanAdjustmentRequestDTO.builder()
                .managementProcessId(loanWaiver.getManagementProcessId())
                .processId(loanWaiver.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(loanWaiver.getSamityId())
                .memberId(loanWaiver.getMemberId())
                .adjustmentType(WAIVER.getValue())
                .build();
    }

    public PaymentCollectionBySamityCommand buildPaymentCollectionBySamityCommand(LoanWaiverCreateUpdateRequestDTO requestDto,
                                                                                  LoanWaiver loanWaiver) {
        return PaymentCollectionBySamityCommand.builder()
                .managementProcessId(loanWaiver.getManagementProcessId())
                .processId(loanWaiver.getProcessId())
                .mfiId(requestDto.getMfiId())
                .officeId(requestDto.getOfficeId())
                .loginId(requestDto.getLoginId())
                .samityId(HelperUtil.checkIfNullOrEmpty(requestDto.getSamityId()) ? loanWaiver.getSamityId() : requestDto.getSamityId())
                .collectionType(WAIVER.getValue())
                .data(Arrays.asList(CollectionData.builder()
                        .stagingDataId(loanWaiver.getStagingDataId())
                        .accountType(ACCOUNT_TYPE_LOAN.getValue())
                        .loanAccountId(requestDto.getLoanAccountId())
                        .amount(requestDto.getCollectedAmountByCash())
                        .paymentMode(PAYMENT_MODE_CASH.getValue())
                        .collectionType(WAIVER.getValue())
                        .currentVersion(loanWaiver.getCurrentVersion())
                        .build()))
                .build();
    }


    public LoanWaiver buildLoanWaiver(LoanWaiverCreateUpdateRequestDTO requestDto, StagingAccountData stagingAccountData, ManagementProcessTracker managementProcessTracker){
        return LoanWaiver.builder()
                .loanWaiverDataId(UUID.randomUUID().toString())
                .managementProcessId(requestDto.getManagementProcessId())
                .processId(requestDto.getProcessId())
                .samityId(requestDto.getSamityId())
                .loanAccountId(requestDto.getLoanAccountId())
                .memberId(requestDto.getMemberId())
                .waivedAmount(requestDto.getWaivedAmount())
                .payableAmount(requestDto.getPayableAmount())
                .paymentMode(requestDto.getCollectionType())
                .isNew(StatusYesNo.Yes.toString())
                .currentVersion(1)
                .status(Status.STATUS_STAGED.getValue())
                .createdBy(requestDto.getLoginId())
                .createdOn(LocalDateTime.now())
                .isSubmitted(StatusYesNo.No.toString())
                .isLocked(StatusYesNo.No.toString())
                .loanInfo(convertStagingAccountDataToLoanInfo(stagingAccountData))
                .loanWaiverDate(managementProcessTracker.getBusinessDate())
                .editCommit(Status.STATUS_YES.getValue())
                .build();
    }

    public List<SavingsAccountDetails> getSavingsAccountDetails(
            List<StagingAccountData> stagingAccountDataList) {
        return stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getSavingsProductType()) &&
                        (stagingAccountData.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_GS.getValue()) ||
                                stagingAccountData.getSavingsProductType().equalsIgnoreCase(SavingsProductType.PRODUCT_TYPE_VS.getValue())))
                .map(stagingAccountData -> SavingsAccountDetails.builder()
                        .savingsAccountId(stagingAccountData.getSavingsAccountId())
                        .savingsProductId(stagingAccountData.getSavingsProductCode())
                        .savingsProductNameEn(stagingAccountData.getSavingsProductNameEn())
                        .savingsProductNameBn(stagingAccountData.getSavingsProductNameBn())
                        .balance(stagingAccountData.getBalance())
                        .availableBalance(stagingAccountData.getSavingsAvailableBalance())
                        .build())
                .toList();
    }

    public List<LoanAccountDetails> getLoanAccountDetails(
            List<StagingAccountData> stagingAccountDataList) {
        return stagingAccountDataList.stream()
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .map(stagingAccountData -> LoanAccountDetails.builder()
                        .loanAccountId(stagingAccountData.getLoanAccountId())
                        .loanProductId(stagingAccountData.getProductCode())
                        .loanProductNameEn(stagingAccountData.getProductNameEn())
                        .loanProductNameBn(stagingAccountData.getProductNameBn())
                        .loanAmount(stagingAccountData.getLoanAmount())
                        .totalLoanAmount(stagingAccountData.getLoanAmount()
                                .add(stagingAccountData.getServiceCharge()))
                        .serviceCharge(stagingAccountData.getServiceCharge())
                        .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                        .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                        .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                        .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                        .totalPaid(stagingAccountData.getTotalPrincipalPaid()
                                .add(stagingAccountData.getTotalServiceChargePaid()))
                        .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(
                                stagingAccountData.getTotalServiceChargeRemaining()))
                        .disbursementDate(stagingAccountData.getDisbursementDate())
                        .build())
                .toList();
    }

    public LoanWaiverDetailViewResponseDTO buildLoanWaiverDetailViewListResponseDto(LoanWaiver loanWaiver,
                                                                                    MemberEntity memberEntity,
                                                                                    StagingAccountData stagingAccountData,
                                                                                    Tuple2<LoanAdjustmentMemberGridViewResponseDTO, CollectionStagingData> tuple, LoanAccountResponseDTO loanAccountResponseDTO, ServiceChargeChartResponseDTO serviceChargeChartResponseDTO, List<StagingAccountData> stagingAccountDataList){
        return LoanWaiverDetailViewResponseDTO.builder()
                .memberId(memberEntity.getMemberId())
                .memberNameEn(memberEntity.getMemberNameEn())
                .memberNameBn(memberEntity.getMemberNameBn())
                .waiverPaymentMethod(loanWaiver.getPaymentMode())
                .payableAmount(loanWaiver.getPayableAmount())
                .waivedAmount(loanWaiver.getWaivedAmount())
                .waiverDate(loanWaiver.getLoanWaiverDate())
                .status(loanWaiver.getStatus())
                .remarks(loanWaiver.getRemarks())
                .rejectedBy(loanWaiver.getRejectedBy())
                .rejectedOn(loanWaiver.getRejectedOn())
                .submittedBy(loanWaiver.getSubmittedBy())
                .submittedOn(loanWaiver.getSubmittedOn())
                .approvedBy(loanWaiver.getApprovedBy())
                .approvedOn(loanWaiver.getApprovedOn())
                .loanAccountId(stagingAccountData.getLoanAccountId())
                .loanProductId(stagingAccountData.getProductCode())
                .loanProductNameEn(stagingAccountData.getProductNameEn())
                .loanProductNameBn(stagingAccountData.getProductNameBn())
                .serviceCharge(stagingAccountData.getServiceCharge())
                .totalLoanAmount(stagingAccountData.getLoanAmount())
                .loanAmount(stagingAccountData.getLoanAmount())
                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                .serviceChargeRemaining(stagingAccountData.getTotalServiceChargeRemaining())
                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(
                        stagingAccountData.getTotalServiceChargeRemaining()))
                .totalPaid(stagingAccountData.getTotalPrincipalPaid().add(stagingAccountData.getTotalServiceChargePaid()))
                .officeId(memberEntity.getOfficeId())
                .serviceChargeRate(serviceChargeChartResponseDTO.getServiceChargeRate())
                .loanTerm(loanAccountResponseDTO.getLoanTerm())
                .installmentAmount(loanAccountResponseDTO.getInstallmentAmount())
                .noOfInstallment(loanAccountResponseDTO.getNoInstallment())
                .disbursementDate(stagingAccountData.getDisbursementDate().toString())
                .advancePaid(stagingAccountData.getTotalAdvance())
                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                .waiverDate(loanWaiver.getLoanWaiverDate())
                .samityId(loanWaiver.getSamityId())
                .collection(HelperUtil.checkIfNullOrEmpty(tuple.getT2().getOid()) ? null : CollectionDetailView.builder()
                        .oid(tuple.getT2().getOid())
                        .collectionStagingDataId(tuple.getT2().getCollectionStagingDataId())
                        .managementProcessId(tuple.getT2().getManagementProcessId())
                        .processId(tuple.getT2().getProcessId())
                        .stagingDataId(tuple.getT2().getStagingDataId())
                        .samityId(tuple.getT2().getSamityId())
                        .accountType(tuple.getT2().getAccountType())
                        .loanAccountId(tuple.getT2().getLoanAccountId())
                        .amount(tuple.getT2().getAmount())
                        .paymentMode(tuple.getT2().getPaymentMode())
                        .collectionType(tuple.getT2().getCollectionType())
                        .status(tuple.getT2().getStatus())
                        .build())
                .adjustedLoanAccountList(tuple.getT1().getAdjustedLoanAccountList() == null || tuple.getT1().getTotalCount() == 0 ? null : tuple.getT1().getAdjustedLoanAccountList())
                .savingsAccountList(getSavingsAccountDetails(stagingAccountDataList))
                .build();
    }

    public LoanWaiverDTO buildLoanWaiverDto(LoanWaiver loanWaiver, MemberEntity member, BigDecimal loanAmount, Samity samity) {

        LoanWaiverDTO loanWaiverDTO = modelMapper.map(loanWaiver, LoanWaiverDTO.class);
        loanWaiverDTO.setMemberId(member.getMemberId());
        loanWaiverDTO.setMemberNameEn(member.getMemberNameEn());
        loanWaiverDTO.setMemberNameBn(member.getMemberNameBn());
        loanWaiverDTO.setSamityId(samity.getSamityId());
        loanWaiverDTO.setSamityNameBn(samity.getSamityNameBn());
        loanWaiverDTO.setSamityNameEn(samity.getSamityNameEn());
        loanWaiverDTO.setLoanAmount(loanAmount);
        loanWaiverDTO.setBtnUpdateEnabled(
                StringUtils.isNotBlank(loanWaiver.getIsLocked())
                        && loanWaiver.getIsLocked().equalsIgnoreCase(Status.STATUS_NO.getValue())
                        && (loanWaiver.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanWaiver.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())) ?
                        Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue());
        loanWaiverDTO.setBtnSubmitEnabled(StringUtils.isNotBlank(loanWaiver.getIsSubmitted())
                && loanWaiver.getIsSubmitted().equalsIgnoreCase(Status.STATUS_YES.getValue())
                ? Status.STATUS_NO.getValue() : Status.STATUS_YES.getValue());
        return loanWaiverDTO;
    }

    public LoanWaiverGridViewResponseDTO buildLoanWaiverGridViewListResponseDto(OfficeEntity officeEntity, List<LoanWaiverDTO> loanAccountDataList, Integer totalCount) {
        return LoanWaiverGridViewResponseDTO.builder()
                .officeId(officeEntity.getOfficeId())
                .officeNameEn(officeEntity.getOfficeNameEn())
                .officeNameBn(officeEntity.getOfficeNameBn())
                .data(loanAccountDataList)
                .totalCount(totalCount)
                .build();
    }
}
