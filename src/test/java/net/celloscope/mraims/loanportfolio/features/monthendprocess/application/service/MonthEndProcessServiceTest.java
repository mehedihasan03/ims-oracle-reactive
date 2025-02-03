package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.service;

import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.request.MonthEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.MonthEndProcessResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class MonthEndProcessServiceTest {

    @Autowired
    MonthEndProcessService service;

    @Test
    void contextLoad() {
        assertNotNull(service);
    }

/*    @BeforeEach
    void setUp() {
        // Initialize CURRENT_TENANT_ID for testing
        CurrentTenantIdHolder.CURRENT_TENANT_ID = Mono.just("MRA-IMS-MFI-Oid-Template");
    }*/

    @Test
    @DisplayName("Month End Process Revert Test")
    void shouldRevertMonthEndProcess() {
        //given

        MonthEndProcessRequestDTO requestDTO = MonthEndProcessRequestDTO.builder()
                .officeId("1035")
                .build();

        //when

        Mono<MonthEndProcessResponseDTO> response = service.revertMonthEndProcess(requestDTO);

        //then

        StepVerifier
                .create(response)
                .expectSubscription()
                .assertNext(monthEndProcessResponseDTO -> assertEquals("Month End Process is Reverted Successfully", monthEndProcessResponseDTO.getUserMessage()))
                .verifyComplete();
    }

}