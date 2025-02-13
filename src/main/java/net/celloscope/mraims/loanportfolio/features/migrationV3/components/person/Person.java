package net.celloscope.mraims.loanportfolio.features.migrationV3.components.person;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("template.person")
public class Person {
    private String oid;
    private String personId;
    private String personNameEn;
    private String personNameBn;
    private String fatherNameEn;
    private String fatherNameBn;
    private String motherNameEn;
    private String motherNameBn;
    private String spouseNameEn;
    private String spouseNameBn;
    private String spouseContactNo;
    private LocalDate dateOfBirth;
    private String ageOnAppDate;
    private String nationality;
    private String gender;
    //    @NotNull(message = "Mobile no can not be null")
//    @NotEmpty(message = "Mobile no can not be empty")
//    @Pattern(regexp = "^01[3-9]\\d{8}$", message = "Mobile no is not valid")
    private String mobile;
    private String email;
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
    private String mfiId;

    private LocalDateTime migratedOn;
    private String migratedBy;

    private String status;

    private String createdBy;

    private LocalDateTime createdOn;

    private String updatedBy;

    private LocalDateTime updatedOn;



    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}