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
@Table("staging_process_tracker_history")
public class StagingProcessTrackerHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String managementProcessId;
    private String processId;
    private String officeId;
    private String samityId;
    private String stagingDataIds;
    private String status;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    private LocalDateTime archivedOn;
    private String archivedBy;

    private String samityNameEn;
    private String samityNameBn;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private String samityDay;
    private Integer totalMember;
    private Integer totalMemberStaged;
    private Integer totalAccount;
    private Integer totalAccountStaged;
    private String isDownloaded;
    private LocalDateTime downloadedOn;
    private String downloadedBy;
    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;
    private String remarks;
    private Integer currentVersion;
    private String exceptionMarkedBy;
    private String invalidatedBy;
    private LocalDateTime invalidatedOn;
    private String regeneratedBy;
    private LocalDateTime regeneratedOn;
    private String createdBy;
    private LocalDateTime createdOn;

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
