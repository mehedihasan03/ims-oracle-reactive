package net.celloscope.mraims.loanportfolio.features.feecollection.application.service;

import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestForFeeCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.dto.request.FeeCollectionUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class FeeCollectionServiceTest {

    @Autowired
    private FeeCollectionUseCase feeCollectionUseCase;

    @Autowired
    private AutoVoucherUseCase autoVoucherUseCase;

    @Test
    void contextLoads() {
    }

/*    @BeforeEach
    void setUp() {
        // Initialize CURRENT_TENANT_ID for testing
        CurrentTenantIdHolder.INSTITUTE_OID = Mono.just("MRA-IMS-MFI-Oid-Template");
    }*/

    @Test
    public void testUpdateFeeCollectionAndCreateAutoVoucher() throws Exception {

        String officeId = "1064";
        Mono<AutoVoucher> autoVoucherMono = feeCollectionUseCase.updateNullableFeeCollectionByOfficeId
                        (new FeeCollectionUpdateRequestDTO(officeId, "MRA-IMS-MFI-Oid-Template"))
                .flatMap(updatedCollections -> autoVoucherUseCase.createAndSaveAutoVoucherForFeeCollection(buildAutoVoucherRequest(updatedCollections)));

        StepVerifier
                .create(autoVoucherMono)
                .expectSubscription()
                .assertNext(autoVoucher -> {
                    assertThat(autoVoucher.getVoucherId()).isNotBlank();
                })
                .verifyComplete();

    }


    private AutoVoucherRequestForFeeCollectionDTO buildAutoVoucherRequest(List<FeeCollection> updatedFeeCollections) {
        AutoVoucherRequestForFeeCollectionDTO request = AutoVoucherRequestForFeeCollectionDTO.builder()
                .mfiId("MFI-123")
                .officeId("1064")
                .loginId("service")
                .feeCollectionList(updatedFeeCollections)
                .build();

        return request;
    }
}