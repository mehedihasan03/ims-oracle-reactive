package net.celloscope.mraims.loanportfolio.features.migrationV3.components.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.member")
public class Member implements Persistable<String> {

    @Id
    private String oid;
    private String memberApplicationId;
    private String memberId;
    private String companyMemberId;
    private String memSmtOffPriMapId;
    private String personId;
    private String memberNameEn;
    private String memberNameBn;
    private LocalDate dateOfBirth;
    private String ageOnAppDate;
    private String gender;
    private String religion;
    private String nationality;
    private String bloodGroup;
    private String academicQualification;
    private String occupation;
    private String incomeSource;
    private String maritalStatus;
    private String spouseNameEn;
    private String spouseNameBn;
    private String spouseContactNo;
    private String fatherNameEn;
    private String fatherNameBn;
    private String motherNameEn;
    private String motherNameBn;
    private String remarks;
    private BigDecimal noOfDependents;
    private String mobile;
    private String email;
    private String emergencyContactPerson;
    private String emergencyContactNumber;
    private String resDivisionId;
    private String resDistrictId;
    private String resUpazilaId;
    private String resUnionId;
    private String resWardVillageStreet;
    private String resPostOfficeId;
    private String resPostalCode;
    @Column("res_address_line_1")
    private String resAddressLine1;
    @Column("res_address_line_2")
    private String resAddressLine2;
    private String perDivisionId;
    private String perDistrictId;
    private String perUpazilaId;
    private String perUnionId;
    private String perWardVillageStreet;
    private String perPostOfficeId;
    private String perPostalCode;
    @Column("per_address_line_1")
    private String perAddressLine1;
    @Column("per_address_line_2")
    private String perAddressLine2;
    private String passbookNumber;
    private String identificationType;
    private String nidNumber;
    private String smartCardIdNumber;
    private LocalDate nidIssueDate;
    private String nidFrontDocId;
    private String nidBackDocId;
    private String birthRegNo;
    private LocalDate birthIssueDate;
    private String birthRegDocId;
    private String passportNo;
    private LocalDate passportIssueDate;
    private LocalDate passportExpirationDate;
    private String passportDocId;
    private String drivingLicenseNo;
    private String drivingLicenseDocId;
    private String otherIdNo;
    private String otherDocName;
    private String otherIdDocId;
    private String photoImageId;
    private String tinNo;
    private String tinDocId;
    private String loginId;
    private LocalDateTime loginEnabledOn;
    private String memberPassword;
    /*private String microEnName;
    private String microLegalForm;*/
    private BigDecimal gsInstallment;
    private String mfiId;
    private String submittedBy;
    private LocalDateTime submittedOn;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String status;
    private LocalDateTime migratedOn;
    private String migratedBy;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String registerBookSerialId;
    private String mfiProgramId;

    private LocalDate businessDate;
    private String managementProcessId;

    @Override
    public String getId() {
        return this.oid;
    }

    public void setId(String id) {
        this.oid = id;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
