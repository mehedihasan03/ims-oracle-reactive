package net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.samity")
public class Samity {

    @Id
    private String oid;
    private String samityId;
    private String companySamityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityType;
    private String officeId;
    private String mfiProgramId;
    private String divisionId;
    private String districtId;
    private String upazilaId;
    private String unionId;
    private String wardVillageStreet;
    private String postOfficeId;
    private String postalCode;
    @Column("address_line_1")
    private String addressLine1;
    @Column("address_line_2")
    private String addressLine2;
    private double latitude;
    private double longitude;
    private String samityMeetingFrequency;
    private String samityDay;
    private String samityTime;
    private LocalDateTime firstMeetingDate;
    private int maximumMember;
    private String fieldOfficerId;
    private String samityLeaderId;
    private String mfiId;
    private String registrationNo;
    private String workingAreaId;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String status;
    private LocalDateTime migratedOn;
    private String migratedBy;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
