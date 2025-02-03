package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private String oid;
    private String employeeId;
    private String companyEmployeeId;
    private String personId;
    private String loginId;
    private String empNameEn;
    private String empNameBn;
    private LocalDateTime dateOfBirth;
    private String bloodGroup;
    private String religion;
    private String gender;
    private String maritalStatus;
    private String academicQualification;
    private String nationality;
    private String remarks;
    private String fatherNameEn;
    private String fatherNameBn;
    private String motherNameEn;
    private String motherNameBn;
    private String spouseNameEn;
    private String spouseNameBn;
    private String spouseContactNo;
    private String mobile;
    private String personalEmail;
    private String officialEmail;
    private String resDivisionId;
    private String resDistrictId;
    private String resUpazilaId;
    private String resUnionId;
    private String resWardVillageStreet;
    private String resPostOfficeId;
    private String resPostalCode;
    private String resAddressLine1;
    private String resAddressLine2;
    private String perDivisionId;
    private String perDistrictId;
    private String perUpazilaId;
    private String perUnionId;
    private String perWardVillageStreet;
    private String perPostOfficeId;
    private String perPostalCode;
    private String perAddressLine1;
    private String perAddressLine2;
    private String identificationType;
    private String nidNumber;
    private String smartCardIdNumber;
    private LocalDateTime nidIssueDate;
    private String nidFrontDocId;
    private String nidBackDocId;
    private String birthRegNo;
    private LocalDateTime birthIssueDate;
    private String birthRegDocId;
    private String passportNo;
    private LocalDateTime passportIssueDate;
    private LocalDateTime passportExpirationDate;
    private String passportDocId;
    private String drivingLicenseNo;
    private String drivingLicenseDocId;
    private String otherIdNo;
    private String otherDocName;
    private String otherIdDocId;
    private String photoImageId;
    private String tinNo;
    private String tinDocId;
    private String payMethod;
    private String bankId;
    private String bankBranchId;
    private String bankAccountNo;
    private String digitalPayCompId;
    private String digitalWalletNumber;
    private String roleId;
    private String emplSmtOffMapId;
    private String mfiId;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String status;
    private String migratedBy;
    private LocalDateTime migratedOn;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
