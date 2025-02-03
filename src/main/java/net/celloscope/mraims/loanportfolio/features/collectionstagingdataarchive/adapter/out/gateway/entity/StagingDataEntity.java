package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("staging_data")
public class StagingDataEntity extends BaseToString {

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

