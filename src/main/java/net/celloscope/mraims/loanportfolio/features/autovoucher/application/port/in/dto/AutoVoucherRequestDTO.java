package net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoVoucherRequestDTO {
    private String managementProcessId;
    private String processId;
    private String officeId;
    private String mfiId;
    private LocalDate businessDate;
    private String loginId;
    private List<JournalRequestDTO> aisRequestList;
}
