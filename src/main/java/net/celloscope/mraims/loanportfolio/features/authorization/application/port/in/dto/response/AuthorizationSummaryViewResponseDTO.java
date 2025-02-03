package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationSummaryViewResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;

    private LocalDate businessDate;
    private String businessDay;

    private String isLocked;
    private String lockedBy;

    private List<String> samityIdList;

    private SamityListDTO regularCollectionSamity;
    private SamityListDTO specialCollectionSamity;
    private SamityListDTO withdrawSamity;
    private SamityListDTO loanAdjustmentSamity;
    private SamityListDTO loanRebateSamity;
    private SamityListDTO loanWaiverSamity;
    private SamityListDTO loanWriteOffCollectionSamity;

    private SummaryDTO summary;

    private String status;
    private String userMessage;
    private String btnAuthorizeEnabled;
    private String btnRejectEnabled;
    private String btnUnauthorizeEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
