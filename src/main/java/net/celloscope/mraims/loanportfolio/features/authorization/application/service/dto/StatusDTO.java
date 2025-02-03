package net.celloscope.mraims.loanportfolio.features.authorization.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {
    private String status;
    private String isLocked;
}
