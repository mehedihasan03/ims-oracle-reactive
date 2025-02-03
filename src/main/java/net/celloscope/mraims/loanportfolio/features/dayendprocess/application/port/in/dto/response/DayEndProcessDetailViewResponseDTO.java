package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessTransaction;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessDetailViewResponseDTO {

    private String btnRunDayEndProcessEnabled;
    private String btnRefreshEnabled;
    private String btnDeleteEnabled;
    private String btnRetryEnabled;

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;

    private LocalDate businessDate;
    private String businessDay;

    private String status;
    private String isFinancialPeriodAvailable;
    private String remarks;
    private String userMessage;

    private DayEndProcessTransaction loanCollection;
    private DayEndProcessTransaction savingsCollection;
    private DayEndProcessTransaction savingsWithdraw;
    private DayEndProcessTransaction loanAdjustment;
    private DayEndProcessTransaction loanDisbursement;
//    private DayEndProcessTransaction accruedInterest;
    private DayEndProcessTransaction serviceChargeProvisioning;
    private DayEndProcessTransaction welfareFundCollection;
    private DayEndProcessTransaction feeCollection;
    private DayEndProcessTransaction interestDeposit;
    private DayEndProcessTransaction reverseLoanRepay;
    private DayEndProcessTransaction adjustmentLoanRepay;
    private DayEndProcessTransaction reverseSavingsDeposit;
    private DayEndProcessTransaction adjustmentSavingsDeposit;
    private DayEndProcessTransaction reverseSavingsWithdraw;
    private DayEndProcessTransaction adjustmentSavingsWithdraw;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
