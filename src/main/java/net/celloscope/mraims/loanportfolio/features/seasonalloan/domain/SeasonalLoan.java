package net.celloscope.mraims.loanportfolio.features.seasonalloan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonalLoan {
    // loan account
    private String oid;
    private String loanApplicationId;
    private String loanAccountId;
    private String status;
    private String memberId;
    private String loanProductId;
    private String productName;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private Integer noInstallment;
    private Integer graceDays;
    private BigDecimal installmentAmount;
    private LocalDate actualDisburseDt;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal serviceChargeRatePerPeriod;

    // managementProcessTracker
    private LocalDate businessDate;

    // serviceChargeChart
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;

    // metaproperty
    private Integer daysInYear;

    // calculated
    private Integer daysPassed;
    private BigDecimal serviceChargeRatePerDay;
    private BigDecimal totalServiceCharge;
    private BigDecimal totalAmountPayable;

    // repayment schedule
    private List<RepaymentScheduleResponseDTO> repaymentSchedule;

    //other fields
    private String managementProcessId;
    private String stagingDataId;
    private String samityId;
    private String processId;
}
