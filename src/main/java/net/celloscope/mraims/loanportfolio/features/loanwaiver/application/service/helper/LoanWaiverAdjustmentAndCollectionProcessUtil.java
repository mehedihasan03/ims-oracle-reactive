package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.service.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.CollectionTypeChange;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.LoanAdjustmentMemberGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.LoanAdjustmentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.LoanWaiverCreateUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.LoanWaiverSubmitRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanWaiverAdjustmentAndCollectionProcessUtil {

    private final PaymentCollectionUseCase paymentCollectionUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;
    private final LoanWaiverUtil loanWaiverUtil;

    public Mono<Tuple2<LoanAdjustmentResponseDTO, CollectionMessageResponseDTO>> createLoanAdjustmentAndPaymentCollection(
            LoanWaiverCreateUpdateRequestDTO requestDto, LoanWaiver saveLoanWaiver) {
        Boolean isAdjustmentOrCombineForAdjustment =
                !HelperUtil.checkIfNullOrEmpty(requestDto.getCollectionType()) &&
                        (requestDto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue()) ||
                                requestDto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue()));

        Boolean isCashOrCombineForCollection =
                !HelperUtil.checkIfNullOrEmpty(requestDto.getCollectionType()) &&
                        (requestDto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue()) ||
                                requestDto.getCollectionType().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue()));

        return Mono.zip(
                isAdjustmentOrCombineForAdjustment ? createLoanAdjustment(saveLoanWaiver, requestDto) : Mono.just(new LoanAdjustmentResponseDTO()),
                isCashOrCombineForCollection ? createCollectionPayment(saveLoanWaiver, requestDto) : Mono.just(new CollectionMessageResponseDTO())
        );
    }

    public Mono<String> updateLoanAdjustmentAndPaymentCollection(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto,
                                                                 String originalType) {
        originalType = originalType.trim().toUpperCase();
        String newType = requestDto.getCollectionType().trim().toUpperCase();

        switch (CollectionTypeChange.valueOf(originalType + "_TO_" + newType)) {
            case CASH_TO_CASH:
                return this.updateCollectionPayment(loanWaiver, requestDto)
                        .map(data -> "Collection Payment Data Updated Successfully");
            case CASH_TO_ADJUSTMENT:
                return this.removeCollectionPayment(loanWaiver, requestDto)
                        .then(this.createLoanAdjustment(loanWaiver, requestDto))
                        .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Created Successfully");
            case CASH_TO_COMBINE:
                return this.updateCollectionPayment(loanWaiver, requestDto)
                        .then(this.createLoanAdjustment(loanWaiver, requestDto))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Created Successfully");
            case ADJUSTMENT_TO_CASH:
                return this.removeLoanAdjustment(loanWaiver, requestDto)
                        .then(this.createCollectionPayment(loanWaiver, requestDto))
                        .map(data -> "Loan Adjustment Data Deleted and Collection Payment Data Created Successfully");
            case ADJUSTMENT_TO_ADJUSTMENT:
                return this.updateLoanAdjustment(loanWaiver, requestDto)
                        .map(data -> "Loan Adjustment Data Updated Successfully");
            case ADJUSTMENT_TO_COMBINE:
                return this.updateLoanAdjustment(loanWaiver, requestDto)
                        .then(this.createCollectionPayment(loanWaiver, requestDto))
                        .map(data -> "Loan Adjustment Data Updated and Collection Payment Data Created Successfully");
            case COMBINE_TO_CASH:
                return this.updateCollectionPayment(loanWaiver, requestDto)
                        .then(this.removeLoanAdjustment(loanWaiver, requestDto))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Deleted Successfully");
            case COMBINE_TO_ADJUSTMENT:
                return this.updateLoanAdjustment(loanWaiver, requestDto)
                        .then(this.removeCollectionPayment(loanWaiver, requestDto))
                        .map(data -> "Collection Payment Data Removed and Loan Adjustment Data Updated Successfully");
            case COMBINE_TO_COMBINE:
                return this.updateCollectionPayment(loanWaiver, requestDto)
                        .then(this.updateLoanAdjustment(loanWaiver, requestDto))
                        .map(data -> "Collection Payment Data Updated and Loan Adjustment Data Updated Successfully");
            default:
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Unsupported Collection Type Change"));
        }
    }

    public Mono<Tuple2<LoanAdjustmentResponseDTO, CollectionMessageResponseDTO>> submitLoanAdjustmentAndPaymentCollection(
            LoanWaiverSubmitRequestDTO requestDto, LoanWaiver loanWaiver) {
        Boolean isAdjustmentOrCombineForAdjustment =
                !HelperUtil.checkIfNullOrEmpty(loanWaiver.getPaymentMode()) &&
                        (loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue()) ||
                                loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue()));

        Boolean isCashOrCombineForCollection =
                loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue()) ||
                        loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue());

        return Mono.zip(
                isAdjustmentOrCombineForAdjustment ? loanAdjustmentUseCase.submitLoanAdjustmentDataForAuthorization(loanWaiver.getManagementProcessId(), loanWaiver.getProcessId(), requestDto.getLoginId())
                        : Mono.just(new LoanAdjustmentResponseDTO()),
                isCashOrCombineForCollection ? paymentCollectionUseCase.submitCollectionPaymentForAuthorization(loanWaiver.getManagementProcessId(), loanWaiver.getProcessId(), requestDto.getLoginId())
                        : Mono.just(new CollectionMessageResponseDTO())
        );
    }


    public Mono<Tuple2<LoanAdjustmentMemberGridViewResponseDTO, CollectionStagingData>> getLoanAdjustmentAndPaymentCollection(
            LoanWaiver loanWaiver) {
        Mono<CollectionStagingData> collectionStagingDataMono = Mono.just(new CollectionStagingData());
        Mono<LoanAdjustmentMemberGridViewResponseDTO> adjustedLoanAccountListMono = Mono.just(new LoanAdjustmentMemberGridViewResponseDTO());
        if (loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_CASH.getValue()) ||
                loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue())) {
            collectionStagingDataMono = collectionStagingDataQueryUseCase.getCollectionStagingDataByLoanAccountId(loanWaiver.getLoanAccountId(), loanWaiver.getManagementProcessId(), loanWaiver.getProcessId(), String.valueOf(loanWaiver.getCurrentVersion()))
                    .doOnNext(stagingAccountData -> log.info("Retrieved staging collection data: {}", stagingAccountData))
                    .doOnError(stagingAccountData -> log.error("Error retrieving staging collection data: {}", stagingAccountData));
        }

        if (loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_ADJUSTMENT.getValue()) ||
                loanWaiver.getPaymentMode().equalsIgnoreCase(Constants.COLLECTION_TYPE_COMBINE.getValue())) {
            adjustedLoanAccountListMono = loanAdjustmentUseCase.getAdjustedLoanAccountListByManagementProcessId(LoanAdjustmentRequestDTO.builder()
                            .managementProcessId(loanWaiver.getManagementProcessId())
                            .processId(loanWaiver.getProcessId())
                            .memberId(loanWaiver.getMemberId())
                            .currentVersion(loanWaiver.getCurrentVersion()).build())
                    .doOnNext(loanAdjustmentData -> log.info("Retrieved adjusted account data: {}", loanAdjustmentData))
                    .doOnError(loanAdjustmentData -> log.error("Error retrieving adjusted account data: {}", loanAdjustmentData));
        }
        return Mono.zip(adjustedLoanAccountListMono, collectionStagingDataMono);
    }

    private Mono<CollectionMessageResponseDTO> createCollectionPayment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return paymentCollectionUseCase.collectPaymentBySamity(
                loanWaiverUtil.buildPaymentCollectionBySamityCommand(requestDto, loanWaiver));
    }

    private Mono<CollectionMessageResponseDTO> updateCollectionPayment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return paymentCollectionUseCase.updateCollectionPaymentByManagementId(
                loanWaiverUtil.buildPaymentCollectionBySamityCommand(requestDto, loanWaiver));
    }

    private Mono<List<CollectionStagingData>> removeCollectionPayment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return paymentCollectionUseCase.removeCollectionPayment(
                loanWaiverUtil.buildPaymentCollectionBySamityCommand(requestDto, loanWaiver));
    }

    private Mono<LoanAdjustmentResponseDTO> createLoanAdjustment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return loanAdjustmentUseCase.createLoanAdjustmentForMember(
                loanWaiverUtil.buildLoanAdjustmentRequestDTO(requestDto, loanWaiver));
    }

    private Mono<LoanAdjustmentResponseDTO> updateLoanAdjustment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return loanAdjustmentUseCase.updateLoanAdjustmentForMember(
                loanWaiverUtil.buildLoanAdjustmentRequestDTO(requestDto, loanWaiver));
    }

    private Mono<LoanAdjustmentResponseDTO> removeLoanAdjustment(LoanWaiver loanWaiver, LoanWaiverCreateUpdateRequestDTO requestDto) {
        return loanAdjustmentUseCase.deleteLoanAdjustmentAndSaveToHistoryForMember(
                loanWaiverUtil.buildLoanAdjustmentRequestDTOForDelete(requestDto, loanWaiver));
    }
}
