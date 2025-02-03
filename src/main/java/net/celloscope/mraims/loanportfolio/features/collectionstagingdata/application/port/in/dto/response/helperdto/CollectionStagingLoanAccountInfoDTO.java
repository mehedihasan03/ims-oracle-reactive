package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionStagingLoanAccountInfoDTO {

    //	from staging account data
    private String loanAccountId;
    private String stagingAccountDataId;
    private String productCode;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal scheduledInstallmentAmount;
    private List<InstallmentDTO> installments;
    private BigDecimal totalDue;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargePaid;
    private BigDecimal totalServiceChargeRemaining;
    private BigDecimal accountOutstanding;

    //	from collection staging data
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private String uploadedBy;
    private LocalDateTime uploadedOn;
    private String status;
    private String isCollectionCompleted;

    private LocalDate disbursementDate;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
