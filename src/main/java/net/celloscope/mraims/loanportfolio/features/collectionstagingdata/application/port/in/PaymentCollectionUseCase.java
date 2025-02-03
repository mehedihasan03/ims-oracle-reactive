package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionByFieldOfficerCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionMessageResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaymentCollectionUseCase {
    Mono<CollectionMessageResponseDTO> collectPaymentBySamityV1(PaymentCollectionBySamityCommand command);

    Mono<CollectionMessageResponseDTO> collectPaymentByFieldOfficer(PaymentCollectionByFieldOfficerCommand command);

    Mono<CollectionMessageResponseDTO> editAndUpdatePaymentBySamity(PaymentCollectionBySamityCommand command);

//    process management v2
    Mono<CollectionMessageResponseDTO> collectPaymentBySamity(PaymentCollectionBySamityCommand command);
    Mono<CollectionMessageResponseDTO> collectRebatePayment(PaymentCollectionBySamityCommand command);
    Mono<CollectionMessageResponseDTO> collectSeasonalLoanPaymentBySamity(PaymentCollectionBySamityCommand command);

    Mono<CollectionMessageResponseDTO> updateCollectionPaymentBySamity(PaymentCollectionBySamityCommand command);

    Mono<CollectionMessageResponseDTO> updateCollectionPaymentByManagementId(PaymentCollectionBySamityCommand command);

    Mono<CollectionMessageResponseDTO> submitCollectionPaymentForAuthorization(String managementProcessId, String processId, String loginId);

    Mono<List<CollectionStagingData>> removeCollectionPayment(PaymentCollectionBySamityCommand command);

}
