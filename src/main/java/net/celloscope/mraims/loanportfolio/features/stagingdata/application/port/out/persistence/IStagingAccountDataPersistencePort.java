package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence;

import java.util.List;
import java.util.Map;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.LoanAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.SavingsAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IStagingAccountDataPersistencePort {
    Flux<StagingAccountData> save(List<StagingAccountData> stagingAccountDataList);

    Flux<StagingAccountData> getStagingLoanAccountDataListByMemberId(String memberId);

    Flux<StagingAccountData> getStagingSavingsAccountDataListByMemberId(String memberId);

    Flux<LoanAccountSummeryDTO> getLoanAccountSummeryByProductCode(String samityId);

    Flux<SavingsAccountSummeryDTO> getSavingsAccountSummeryByProductCode(String samityId);

    Mono<StagingAccountData> getStagingLoanOrSavingsAccountByAccountId(String accountId);

    Mono<String> deleteStagingAccountDataByManagementProcessId(String processId);

    Mono<List<StagingAccountDataEntity>> getAllStagingAccountDataByManagementProcessId(String managementProcessId);

    Mono<String> deleteAllStagingAccountDataByManagementProcessId(String managementProcessId);

    Mono<List<String>> saveAllStagingAccountData(List<StagingAccountData> stagingAccountDataList);

    Mono<List<String>> editUpdateAndDeleteStagingAccountDataOfASamity(String processId, String samityId, List<String> memberIdList);

    Mono<StagingAccountData> getStagingAccountDataBySavingsAccountId(String savingsAccountId);

    Mono<StagingAccountData> getLoanAccountDataByLoanAccountId(String loanAccountId);

    Mono<Map<String, List<String>>> updateStagingAccountDataToEditHistoryTable(String managementProcessId,
                                                                               String processId, List<String> memberIdList);

    Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(String managementProcessId,
            List<String> memberIdList);

    Flux<StagingAccountData> getStagingAccountDataListByMemberId(String memberId);

    Flux<StagingAccountData> getStagingAccountDataBySavingsAccountIdList(List<String> savingsAccountIdList);

    Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(List<String> memberIDList);
}
