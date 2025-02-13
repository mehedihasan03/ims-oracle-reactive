package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("template.staging_data_history")
public class StagingDataHistoryEntity extends BaseToString implements Persistable<String> {

    @Id
    private String oid;

    private String stagingDataId;
    private String processId;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
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
        return this.oid;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}

