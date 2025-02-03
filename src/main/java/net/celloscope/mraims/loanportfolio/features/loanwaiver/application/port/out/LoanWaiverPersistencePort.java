package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanWaiverDTO;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanWaiverPersistencePort {
    Flux<LoanWaiver> getLoanWaiverList();

    Mono<LoanWaiver> getLoanWaiverById(String id);

    Mono<LoanWaiver> saveLoanWaiver(LoanWaiver loanWaiver);

    Mono<LoanWaiver> getLoanWaiverByLoanAccountId(String loanWaiverByLoanAccountId);

    Flux<LoanWaiver> getLoanWaiverDataBySamity(String samityId);

    Mono<List<LoanWaiverEntity>> getAllLoanWaiverDataByManagementProcessId(String managementProcessId);

    Mono<String> deleteAllByManagementProcessId(String managementProcessId);

    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<LoanWaiverDTO>> getAllLoanWaiverDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateLoanWaiverDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);
}
