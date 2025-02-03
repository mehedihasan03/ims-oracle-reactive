package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepaymentSchedule {
    private String oid;
    private String loanRepayScheduleId;
    private String managementProcessId;
    private String loanAccountId;
    private String memberId;
    private Integer installNo;
    private LocalDate installDate;
    private LocalDate makeUpInstallDate;
    private BigDecimal beginPrinBalance;
    private BigDecimal scheduledPayment;
    private BigDecimal extraPayment;
    private BigDecimal totalPayment;
    private BigDecimal principal;
    private BigDecimal serviceCharge;
    private BigDecimal endPrinBalance;
    private BigDecimal penalty;
    private BigDecimal fees;
    private Integer paymentAttempts;
    private BigDecimal insurance;
    private LocalDate lastPostingDate;
    private BigDecimal penaltyOnHold;
    private String ifRescheduledId;
    private String mfiId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String isProvisioned;

    private String loanRepayScheduleOid;
    private String dayOfWeek;
    private BigDecimal serviceChargeRatePerPeriod;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
