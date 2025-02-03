package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessSamityResponse {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String samityType;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String collectionType;
    private BigDecimal loanCollectionAmount;
    private BigDecimal savingsCollectionAmount;
    private BigDecimal collectionAmount;
    private BigDecimal withdrawAmount;
    private BigDecimal disbursementAmount;
    private BigDecimal loanAdjustmentAmount;
    private BigDecimal savingsAdjustmentAmount;
    private BigDecimal feeCollectionAmount;

    private String status;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
