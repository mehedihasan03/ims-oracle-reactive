package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.service;

import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request.DayEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.DayEndProcessResponseDTO;
import org.junit.jupiter.api.Assertions;
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
class DayEndProcessTrackerServiceTest {
    @Autowired
    DayEndProcessTrackerService service;

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
    @DisplayName("AIS Day End Process Revert Test")
    void shouldRevertDayEndProcessForAIS() {
        //given
        DayEndProcessRequestDTO requestDTO = DayEndProcessRequestDTO.builder()
                .mfiId("M1001")
                .officeId("1035")
                .build();
        //when
        Mono<DayEndProcessResponseDTO> response = service.revertDayEndProcessByAISForOffice(requestDTO);
        //then
        StepVerifier
                .create(response)
                .expectSubscription()
                .assertNext(dayEndProcessResponseDTO -> Assertions.assertEquals("Day End Process is Reverted Successfully", dayEndProcessResponseDTO.getUserMessage()))
                .verifyComplete();
    }

    @Test
    @DisplayName("MIS Day End Process Revert Test")
    void shouldRevertDayEndProcessForMIS() {
        //given
        DayEndProcessRequestDTO requestDTO = DayEndProcessRequestDTO.builder()
                .mfiId("M1001")
                .officeId("1035")
                .build();
        //when
        Mono<DayEndProcessResponseDTO> response = service.revertDayEndProcessByMISForOffice(requestDTO);
        //then
        StepVerifier
                .create(response)
                .expectSubscription()
                .assertNext(dayEndProcessResponseDTO -> Assertions.assertEquals("Day End Process is Reverted Successfully", dayEndProcessResponseDTO.getUserMessage()))
                .verifyComplete();
    }
}