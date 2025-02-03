package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GridViewDataObject {
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private Integer totalMember;
    private String mfiId;
    private String downloadedBy;
    private LocalDateTime downloadedOn;
    private String type;
    private String uploadedBy;
    private LocalDateTime uploadedOn;
    private String status;
    private String remarks;
    private String isEditable;
    private String isCommittable;
    private BigDecimal totalCollectionAmount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}