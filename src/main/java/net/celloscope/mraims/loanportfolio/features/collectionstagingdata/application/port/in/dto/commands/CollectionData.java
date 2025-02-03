package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.PaymentDetails;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CollectionData {
    private String stagingDataId;
    private String accountType;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private PaymentDetails paymentDetails;
    private String collectionType;
    private Integer currentVersion;
    private BigDecimal targetAmount;
    private Integer dpsPendingInstallmentNo;
    private String savingsTypeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}