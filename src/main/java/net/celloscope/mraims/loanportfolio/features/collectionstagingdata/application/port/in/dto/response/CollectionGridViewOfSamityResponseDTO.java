package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionGridViewOfSamityResponseDTO {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;

    private Integer totalMember;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String type;
    private String downloadedBy;
    private LocalDateTime downloadedOn;
    private String uploadedBy;
    private LocalDateTime uploadedOn;

    private BigDecimal loanCollection;
    private BigDecimal savingsCollection;
//    private BigDecimal totalCollection;
    private BigDecimal totalCollectionAmount;

    private String status;
    private String remarks;
    private String btnOpenEnabled;
    private String btnViewEnabled;
    private String btnEditEnabled;
    private String btnCommitEnabled;
    private String btnSubmitEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
