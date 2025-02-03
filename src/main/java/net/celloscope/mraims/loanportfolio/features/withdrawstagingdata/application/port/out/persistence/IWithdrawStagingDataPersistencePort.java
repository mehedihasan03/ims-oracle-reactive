package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface IWithdrawStagingDataPersistencePort {

    Mono<StagingWithdrawData> getWithdrawStagingDataBySavingsAccountId(String savingsAccountId);

    Flux<StagingWithdrawData> getWithdrawStagingDataBySamityIdAndWithdrawType(String samityId, String withdrawType);

    Mono<BigDecimal> getTotalWithdrawAmountOfASamity(String samityId);

    Flux<StagingWithdrawData> getAllWithdrawStagingDataBySamity(String samityId);

    Mono<List<StagingWithdrawData>> saveStagingWithdrawData(List<StagingWithdrawData> stagingWithdrawDataList);

    Mono<List<StagingWithdrawData>> updateWithdrawPayment(List<StagingWithdrawData> stagingWithdrawDataList, String loginId);

    Flux<StagingWithdrawData> getAllWithdrawDataForSamity(String managementProcessId, String samityId);
    Mono<List<StagingWithdrawData>> getAllWithdrawDataByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

    Mono<String> validateAndUpdateWithdrawDataForSubmission(String managementProcessId, String samityId, String loginId);

    Mono<String> lockSamityForAuthorization(String samityId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<List<StagingWithdrawData>> getWithdrawStagingDataListBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateWithdrawStagingDataForAuthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateWithdrawStagingDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<String> validateAndUpdateWithdrawStagingDataForUnauthorizationBySamityId(String managementProcessId, String samityId, String loginId);

    Flux<WithdrawStagingData> getAllWithdrawCollectionDataByLoginId(String managementProcessId, String loginId, Integer limit, Integer offset);

    Mono<WithdrawStagingData> getWithdrawCollectionDataByOid(String oid);

    Mono<StagingWithdrawData> getWithdrawCollectionDataBySavingsAccountIdAndManagementProcessId(String savingsAccountId, String managementProcessId);

    Mono<StagingWithdrawDataEditHistoryEntity> saveWithdrawEditHistory(StagingWithdrawDataEditHistoryEntity WithdrawEditHistoryEntity);

    Flux<WithdrawStagingData> getWithdrawDataByOidList(List<String> oidList);

    Mono<String> updateSubmittedWithdrawData(String loginId, String oid);

    Mono<String> deleteWithdrawData(String oid);

    Mono<Long> countWithdrawData(String managementProcessId, String employeeId);
}
