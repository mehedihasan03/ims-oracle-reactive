package net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassbookResponse {
    private List<PassbookResponseDTO> data;
    private Integer count;
}
