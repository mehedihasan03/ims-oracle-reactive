package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    private String memberId;
    private String status;
}
