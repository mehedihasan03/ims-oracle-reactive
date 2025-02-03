package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DpsRepaymentSchedule {
    private String dpsRepaymentScheduleId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private String memberId;
    private String samityId;
    private Integer repaymentNo;
    private LocalDate repaymentDate;
    private String dayOfWeek;
    private BigDecimal repaymentAmount;
    private String isCompoundingDate;
    private String isPostingDate;
    private String mfiId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;

}
