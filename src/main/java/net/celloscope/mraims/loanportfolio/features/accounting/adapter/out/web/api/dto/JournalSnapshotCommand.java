package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalSnapshotCommand {
    private String managementProcessId;
    private String officeId;
    private String loginId;
    private String businessDate;
    private String mfiId;
}
