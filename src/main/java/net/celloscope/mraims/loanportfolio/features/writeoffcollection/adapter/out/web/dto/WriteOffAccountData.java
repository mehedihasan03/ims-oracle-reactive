package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WriteOffAccountData {
    private String oid;
    private String writeOffAccountId;
    private String loanClassificationChartId;
    private String loanAccountId;
    private String memberId;
    private long noOfLoanAccount;
    private BigDecimal loanAmount;
    private long loanTerm;
    private long noInstallment;
    private BigDecimal installmentAmount;
    private long lateDay;
    private Timestamp disburseDate;
    private String disburseDateStr;
    private Timestamp plannedEndDate;
    private String plannedEndDateStr;
    private BigDecimal accumulationRate;
    private BigDecimal remainingPrincipleBalance;
    private BigDecimal principlePaid;
    private BigDecimal provisionAmount;
    private String status;
    private String officeId;
    private String mfiId;
    private Timestamp migratedOn;
    private String migratedBy;
    private Timestamp createdOn;
    private String createdBy;
    private String memberNameEn;
    private String memberNameBn;
    private String officeNameEn;
    private String officeNameBn;
    private String samityNameEn;
    private String samityNameBn;

}
