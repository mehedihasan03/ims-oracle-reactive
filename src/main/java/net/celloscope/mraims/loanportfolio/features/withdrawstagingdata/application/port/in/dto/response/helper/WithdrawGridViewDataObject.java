package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawGridViewDataObject {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String samityDay;
    private Integer totalMember;

    private String mfiId;
    private String withdrawType;
    private String downloadedBy;
    private LocalDateTime downloadedOn;
    private String type;
    private LocalDateTime uploadedOn;
    private String uploadedBy;

    private String status;
    private BigDecimal totalWithdrawAmount;

    private String remarks;
    private String btnViewEnabled;
    private String btnEditEnabled;
    private String btnCommitEnabled;
    private String btnSubmitEnabled;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
