package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanAdjustmentPersistencePort {
    Mono<List<LoanAdjustmentData>> createAndSaveLoanAdjustmentData(List<LoanAdjustmentData> loanAdjustmentDataList);

    Flux<LoanAdjustmentData> getLoanAdjustmentDataBySamity(String samityId);

    Mono<List<LoanAdjustmentData>> updateStatusOfLoanAdjustmentDataForAuthorization(String samityId, String loginId);

    Mono<String> updateStatusToSubmitLoanAdjustmentDataForAuthorization(String managementProcessID, String samityId, String loginId);

    Mono<String> updateStatusToSubmitLoanAdjustmentDataForAuthorizationByManagementProcessId(String managementProcessID, String processId, String loginId);

    Flux<LoanAdjustmentData> getLoanAdjustmentDataByMemberId(String memberId);

    Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataBySamityIdList(List<String> samityIdList);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<String> validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> archiveAndDeleteLoanAdjustmentData(String managementProcessId);

    Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessId(String managementProcessId);

    Mono<String> deleteAllLoanAdjustmentDataByManagementProcessId(String managementProcessId);

    Mono<String> deleteAllLoanAdjustmentDataByManagementProcessIdAndLoanAdjustmentProcessId(String managementProcessId, String loanAdjustmentProcessId);

    Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Flux<LoanAdjustmentData> getAllAdjustmentCollectionDataByLoginId(String managementProcessId, String loginId, Integer limit, Integer offset);

    Mono<Long> getCountLoanAdjustment(String managementProcessId, String loginId);

    Flux<LoanAdjustmentData> getAllAdjustmentByManagementProcessIdAndLoanAdjustmentProcessId (String managementProcessId, String loanAdjustmentProcessId);

    Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataByOid(String oid);

    Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataBySavingsAccountId(String savingsAccountId);

    Mono<LoanAdjustmentData> saveEditedData(LoanAdjustmentData loanAdjustmentData);

    Flux<LoanAdjustmentData> getLoanAdjustmentCollectionDataByOidList(List<String> oid);

    Flux<String> updateStatusOfLoanAdjustmentDataForSubmission(AdjustmentEntitySubmitRequestDto requestDto, LoanAdjustmentData loanAdjustmentData);
    Flux<LoanAdjustmentData> getLoanAdjustmentDataByManagementProcessIdAndSamity(String managementProcessId, String samityId);

    Mono<LoanAdjustmentData> getLoanAdjustmentCollectionDataByLoanAccountId(String loanAccountId);

    Mono<String> deleteLoanAdjustmentByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<Long> getCountLoanAdjustmentByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
}
