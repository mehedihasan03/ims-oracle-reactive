package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("staging_data_history")
public class StagingDataHistoryEntity  implements Persistable<String> {

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

    private LocalDateTime archivedOn;
    private String archivedBy;

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
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
