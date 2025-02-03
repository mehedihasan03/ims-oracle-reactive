package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;

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

    //v3
    private String memberNameEn;
    private String memberNameBn;
    private String fatherNameEn;
    private String fatherNameBn;
    private String motherNameEn;
    private String motherNameBn;
    private String maritalStatus;
    private String spouseName;
    private String occupation;
    private String gender;
    private String nidNumber;
    private String smartCardIdNo;
    private String birthRegNo;
    private String tinNo;
    private String passportNo;
    private LocalDate passportExpirationDate;
    private String nationality;
    private String academicQualification;
    private String remarks;
    private BigDecimal noOfDependent;
    private String mfiProgramId;
    private String religion;
    private String bloodGroup;


    private MigrationLoanRequestDto loanInformation;
    private List<MigrationLoanRequestDto> loanInformationList;
    private MigrationSavingsRequestDto savingsInformation;
    private List<MigrationSavingsRequestDto> savingsInformationList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
