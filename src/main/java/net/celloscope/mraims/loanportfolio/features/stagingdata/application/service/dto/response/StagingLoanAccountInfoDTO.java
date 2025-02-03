package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.Installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingLoanAccountInfoDTO extends BaseToString {

    private String oid;
    private String loanAccountId;
    private String stagingAccountDataId;

    private String productCode;
    private String productNameEn;
    private String productNameBn;

    private List<Installment> installments;

    private BigDecimal totalDue;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargePaid;
    private BigDecimal totalServiceChargeRemaining;
    private BigDecimal accountOutstanding;

    private LocalDate disbursementDate;
    private BigDecimal scheduledInstallmentAmount;
}
