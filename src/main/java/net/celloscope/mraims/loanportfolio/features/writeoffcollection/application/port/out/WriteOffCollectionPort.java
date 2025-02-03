package net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out;

import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.dto.LoanWriteOffCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain.LoanWriteOffCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface WriteOffCollectionPort {

    Mono<LoanWriteOffCollection> saveWriteOffCollection(LoanWriteOffCollection command);

    Mono<LoanWriteOffCollection> saveWriteOffCollectionHistory(LoanWriteOffCollection command);

    Mono<LoanWriteOffCollection> getWriteOffCollectionById(String oid);

    Mono<LoanWriteOffCollection> getWriteOffCollectionByLoanAccountId(String loanAccountId);

    Flux<LoanWriteOffCollection> getCollectedWriteOffDataByOfficeId(String officeId, LocalDateTime startDate, LocalDateTime endDate);

    Mono<LoanWriteOffCollection> getDetailsOfLoanWriteOffCollection(String oid);

    Mono<List<LoanWriteOffCollectionEntity>> getAllWrittenOffCollectionDataByManagementProcessId(String managementProcessId);

    Mono<String> lockSamityForAuthorization(String samityId, String managementProcessId, String loginId);

    Mono<String> unlockSamityForAuthorization(String samityId, String loginId);

    Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String loginId);

    Mono<List<LoanWriteOffCollectionDTO>> getAllLoanWriteOffCollectionDataBySamityIdList(List<String> samityIdList);

    Mono<String> validateAndUpdateLoanWriteOffCollectionDataForRejectionBySamityId(String managementProcessId, String samityId, String loginId);

    Flux<LoanWriteOffCollection> getLoanWriteOffCollectionBySamityId(String samityId, String managementProcessId);

    Mono<List<LoanWriteOffCollection>> updateStatusOfLoanWriteOffDataForAuthorization(String samityId, String loginId, String managementProcessId);
    Mono<LoanWriteOffCollection> updateLoanWriteOffDataOnUnAuthorization(LoanWriteOffCollectionDTO loanWriteOffCollection);
    Mono<String> deleteAllByManagementProcessId(String managementProcessId);
    Mono<String> deleteWriteOffCollectionDataByOid(String oid);
}
