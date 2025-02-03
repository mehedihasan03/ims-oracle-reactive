package net.celloscope.mraims.loanportfolio.features.migration.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigrationMemberRequestDto {
    private String companyMemberId;
    private String registerBookSerialId;
    private String memberName;
    private String samityId;
    private String memberId;
    private String samityDay;
    private LocalDate dateOfBirth;
    private String mobile;
    private String resDivisionId;
    private String resDistrictId;
    private String resUpazilaId;
    private String resUnionId;
    private String perDivisionId;
    private String perDistrictId;
    private String perUpazilaId;
    private String perUnionId;
    private String resAddressLine1;
    private String perAddressLine1;
    private BigDecimal gsInstallment;
    private MigrationLoanRequestDto loanInformation;
    private List<MigrationLoanRequestDto> loanInformationList;
    private MigrationSavingsRequestDto savingsInformation;
    private List<MigrationSavingsRequestDto> savingsInformationList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
