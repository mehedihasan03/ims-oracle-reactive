package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSamityOfficeEntity {
    private String loanTypeId;
    private String samityDay;
    private Integer monthlyRepayDay;
    private String samityId;
    private String officeId;
    private String managementProcessId;
    private String loanAccountStatus;
}
