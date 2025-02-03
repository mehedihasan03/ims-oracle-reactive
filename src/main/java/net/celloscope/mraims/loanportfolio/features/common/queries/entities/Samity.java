package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Samity {
    public String oid ;
    public String samityId;
    public String companySamityId;
    public String samityNameEn;
    public String samityNameBn;
    public String samityType;
    public String officeId;
    public String mfiProgramId;
    public String divisionId;
    public String districtId;
    public String upazilaId;
    public String unionId;
    public String wardVillageStreet;
    public String postOfficeId;
    public String postalCode;
    public String addressLine1;
    public String addressLine2;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public String samityMeetingFrequency;
    public String samityDay;
    public String samityTime;
    public LocalDate firstMeetingDate;
    public BigDecimal maximumMember;
    public String fieldOfficerId;
    public String fieldOfficerNameEn;
    public String fieldOfficerNameBn;
    public String samityLeaderId;
    public String mfiId;
    public String registrationNo;
    public String workingArea;
    public String currentVersion;
    public String isNewRecord;
    public String approvedBy;
    public LocalDateTime approvedOn;
    public String remarkedBy;
    public LocalDateTime remarkedOn;
    public String isApproverRemarks;
    public String approverRemarks;
    public String status;
    public LocalDateTime migratedOn;
    public String migratedBy;
    public String createdBy;
    public LocalDateTime createdOn;
    public String updatedBy;
    public LocalDateTime updatedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
