package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.lendingcategory.LendingCategory;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanaccount.LoanAccount;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanapplication.LoanApplication;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanfund.LoanFund;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct.LoanProduct;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.member.Member;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.memsmtoffprimap.MemSmtOffPriMap;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.office.Office;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.person.Person;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity.Samity;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccount.SavingsAccount;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccproposal.SavingsAccProposal;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsproduct.SavingsProduct;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.servicechargechart.ServiceChargeChart;
import net.celloscope.mraims.loanportfolio.features.migrationV3.interestchart.InterestChart;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigratedComponentsResponseDto {
    private Office office;
    private Samity samity;
    private Person person;
    private Member member;
    private MemSmtOffPriMap memSmtOffPriMap;
    private LoanFund loanFund;
    private LendingCategory lendingCategory;
    private LoanProduct loanProduct;
    private ServiceChargeChart serviceChargeChart;
    private LoanApplication loanApplication;
    private LoanAccount loanAccount;
    private SavingsProduct savingsProduct;
    private InterestChart interestChart;
    private SavingsAccProposal savingsAccProposal;
    private SavingsAccount savingsAccount;
    private ManagementProcessTrackerEntity managementProcessTracker;
    private OfficeEventTrackerEntity officeEventTracker;
    private List<RepaymentScheduleResponseDTO> repaymentSchedule;
    private DpsRepaymentScheduleResponseDTO dpsRepaymentSchedule;

    private ErrorResponseDTO errorResponse;
    private String status;
    private String message;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
