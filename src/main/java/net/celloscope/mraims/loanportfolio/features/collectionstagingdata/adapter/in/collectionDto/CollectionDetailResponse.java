package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailResponse {

    private String userMessage;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private String status;
    private AccountDetailsInfo data;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
