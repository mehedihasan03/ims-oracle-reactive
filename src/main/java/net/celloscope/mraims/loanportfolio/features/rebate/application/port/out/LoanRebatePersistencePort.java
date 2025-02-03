package net.celloscope.mraims.loanportfolio.features.rebate.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRebatePersistencePort {
    Mono<LoanRebate> saveLoanRebate(LoanRebate loanRebate);
    Mono<LoanRebate> getLoanRebateByOid(String oid);
    Mono<LoanRebate> updateLoanRebate(LoanRebate loanRebate);
    Flux<LoanRebate> getLoanRebateDataByOfficeId(String officeId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<List<LoanRebateEntity>> getAllLoanRebateDataByManagementProcessId(String managementProcessId);
    Mono<String>deleteAllByManagementProcessId(String managementProcessId);
    Flux<LoanRebateDTO> getLoanRebateDataBySamityId(String samityId, String managementProcessId);
    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);
    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);
    Mono<List<LoanRebateDTO>> getAllLoanRebateDataBySamityIdList(List<String> samityIdList);
    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<String> validateAndUpdateLoanRebateDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);
    Mono<List<LoanRebateDTO>> updateStatusOfLoanRebateDataForAuthorization(String samityId, String loginId, String managementProcessId);

    Mono<LoanRebateDTO> updateLoanRebateDataOnUnAuthorization(LoanRebateDTO loanRebateDTO);

    Mono<String> deleteLoanRebateByOid(String oid);

    Mono<LoanRebate> getLoanRebateByLoanAccountId(String loanAccountId);
}
