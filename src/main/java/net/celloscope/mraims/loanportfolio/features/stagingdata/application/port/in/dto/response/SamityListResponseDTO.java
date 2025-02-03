package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SamityListResponseDTO {
    private String fieldOfficerId;
    private List<Samity> data;
    private Integer count;
    private String userMessage;
}
