package net.celloscope.mraims.loanportfolio.features.migration.components.mfiprogram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationMfiProgramService {
    private final MigrationMfiProgramRepository repository;

    public Mono<MfiProgram> save(MfiProgram mfiProgram) {
        return repository.save(buildMfiProgram());
    }

    public Mono<MfiProgram> getMfiProgramByIdAndMfiIdAndStatus(String mfiProgramId, String mfiId, String status) {
        return repository.findFirstByMfiProgramIdAndMfiIdAndStatusOrderByCreatedOnDesc(mfiProgramId, mfiId, status);
    }

    private MfiProgram buildMfiProgram() {
        return MfiProgram.builder()
                .oid("20240225-040709-ydRlEjfgprk0JaL")
                .mfiProgramId("GP-GMC-01")
                .mfiProgramNameEn("GENERAL PROGRAM")
                .mfiProgramNameBn("GENERAL PROGRAM BN")
                .mfiProgramShortName("GP")
                .fundingCategory("GMC")
                .loanFundIds(Collections.singletonList("GF-01"))
                .mfiId("0004")
                .status("Active")
                .createdBy("mfi-admin-tanvir")
                .createdOn(LocalDateTime.parse("2024-02-25T04:07:09.557"))
                .build();
    }
}
