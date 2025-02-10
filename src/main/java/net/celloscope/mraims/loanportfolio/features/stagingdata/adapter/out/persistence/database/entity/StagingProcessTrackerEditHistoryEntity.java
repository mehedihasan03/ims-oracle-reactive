package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity;

import lombok.*;
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
@Table("template.staging_process_tracker_edit_history")
public class StagingProcessTrackerEditHistoryEntity implements Persistable<String> {


    @Id
    private String oid;
    private String managementProcessId;
    private String processId;
    private String officeId;
    private String samityId;
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
    private String stagingDataIds;
    private String isDownloaded;
    private String status;
    private String remarks;
    private Integer currentVersion;
    private String exceptionMarkedBy;
    private String regeneratedBy;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

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
}
