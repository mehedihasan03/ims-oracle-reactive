package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.migration.components.member.Member;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeasonalLoanData {
    private String oid;
    private String loanApplicationId;
    private String loanAccountId;
    private String status;
    private String memberId;
    private String samityId;
    private String samityDay;
    private String fieldOfficerId;
    private String loanProductId;
    private String productName;
    private String loanAmount;
    private String loanTerm;
    private String noInstallment;
    private String graceDays;
    private String installmentAmount;
    private String actualDisburseDt;
    private String plannedEndDate;
    private String actualEndDate;
    private String serviceChargeRatePerPeriod;
    private String businessDate;
    private String serviceChargeRate;
    private String serviceChargeRateFreq;
    private String daysInYear;
    private String daysPassed;
    private String serviceChargeRatePerDay;
    private String totalServiceCharge;
    private String totalAmountPayable;
    private String collectionStagingDataId;
    private MemberEntity memberInformation;

    private LocalDate createdOn;
    private String btnSubmitEnabled;
    private String btnCloseEnabled;



    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
