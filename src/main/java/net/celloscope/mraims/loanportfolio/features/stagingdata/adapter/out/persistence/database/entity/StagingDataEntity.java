package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity;

import com.google.gson.annotations.Expose;
import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("staging_data")
public class StagingDataEntity implements Persistable<String> {

    @Id
    private String oid;
    private String managementProcessId;
    private String processId;
    private String stagingDataId;

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
    private String registerBookSerialId;
    private String companyMemberId;
    private String gender;
    private String maritalStatus;
    private String spouseNameEn;
    private String spouseNameBn;
    private String fatherNameEn;
    private String fatherNameBn;

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;

    private String mfiId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private Integer totalMember;

    private LocalDateTime downloadedOn;
    private String downloadedBy;

    private LocalDateTime createdOn;
    private String createdBy;

    @Override
    public String getId() {
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
