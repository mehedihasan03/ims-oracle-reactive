package net.celloscope.mraims.loanportfolio.features.migration.components.memsmtoffprimap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationMemSmtOffPriMapService {
    private final MigrationMemSmtOffPriMapRepository repository;

    public Mono<MemSmtOffPriMap> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository
            .findFirstByMemberIdAndOfficeIdAndSamityIdAndStatusOrderByMemSmtOffPriMapIdDesc(
                component.getMember().getMemberId(), requestDto.getOfficeId(), memberRequestDto.getSamityId(), MigrationEnums.STATUS_ACTIVE.getValue()
            ).switchIfEmpty(
                memSmtOffPriMapId(component.getMember().getMemberId(), requestDto.getMfiId(), requestDto.getOfficeId())
                    .flatMap(id -> repository.save(buildMemSmtOffPriMap(id, memberRequestDto, requestDto, component)))
                        .doOnError(throwable -> log.error("Error occurred while saving MemSmtOffPriMap: {}", throwable.getMessage()))
            ).doOnError(throwable -> log.error("Error occurred while saving MemSmtOffPriMap: {}", throwable.getMessage()));
    }

    private Mono<String> memSmtOffPriMapId(String memberId, String mfiProgramId, String mfiId) {
        return Mono.just(MigrationEnums.MEM_SMT_OFF_PRI_MAP_SHORT_NAME.getValue() + "-" + memberId);
        /*return MigrationUtils.generateId(
                repository.findFirstByOrderByMemSmtOffPriMapIdDesc(),
                MemSmtOffPriMap::getMemSmtOffPriMapId,
                MigrationEnums.MEM_SMT_OFF_PRI_MAP_SHORT_NAME.getValue(),
                MigrationEnums.MEM_SMT_OFF_PRI_MAP_SHORT_NAME.getValue() + "-%04d"
        );*/
    }

    private MemSmtOffPriMap buildMemSmtOffPriMap(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return MemSmtOffPriMap.builder()
                .oid(MigrationEnums.MEM_SMT_OFF_PRI_MAP_SHORT_NAME.getValue() + "-" + UUID.randomUUID())       // This is the primary key
                .memSmtOffPriMapId(id)     // *Required
                .memberId(component.getMember().getMemberId())              // *Required
//                .lendingCategoryId("1")
                .officeId(requestDto.getOfficeId())              // *Required
                .samityId(memberRequestDto.getSamityId())              // *Required
//                .samityGroupCode("1")
//                .primarySavingsProdId("1")
//                .primaryLoanProdId("1")
//                .mfiProgramId("1")
                .mfiId(requestDto.getMfiId())                 // *Required
//                .effectiveDate(LocalDate.now())
//                .tillDate(LocalDate.now())
//                .currentVersion("1")
//                .isNewRecord("1")
//                .approvedBy("1")
//                .approvedOn(LocalDateTime.now())
//                .isApproverRemarks("1")
//                .approverRemarks("1")
//                .remarkedBy("1")
//                .remarkedOn(LocalDateTime.now())
                .status(MigrationEnums.STATUS_ACTIVE.getValue())
                .createdBy(requestDto.getLoginId())               // *Required
                .createdOn(LocalDateTime.now())        // *Required
//                .updatedBy("1")
//                .updatedOn(LocalDateTime.now())
                .build();
    }

}
