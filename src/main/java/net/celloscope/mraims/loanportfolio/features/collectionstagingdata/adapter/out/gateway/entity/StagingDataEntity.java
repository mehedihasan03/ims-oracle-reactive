package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("template.staging_data")
public class StagingDataEntity {
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
}
