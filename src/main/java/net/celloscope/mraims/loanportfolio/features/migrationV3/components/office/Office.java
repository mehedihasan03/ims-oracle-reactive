package net.celloscope.mraims.loanportfolio.features.migrationV3.components.office;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.office")
public class Office {

    @Id
    private String oid;
    private String officeId;
    private String comOfficeId;
    private String officeNameEn;
    private String officeNameBn;
    private String officeTypeId;
    private String mfiProgramId;
    private String email;
    private String mobile;
    private String phone;
    private String regionId;
    private String zoneId;
    private String areaId;
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
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String latitude;
    private String longitude;
    private String officePhotoId;
    private String mfiId;
    private String status;
    private String migratedBy;
    private LocalDateTime migratedOn;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
