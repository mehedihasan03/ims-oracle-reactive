package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDayResponseDTO {


    private LocalDate businessDate;
    private String businessDay;
    private LocalDateTime dayStartedOn;
    private LocalDateTime dayEndedOn;

    private BigDecimal totalCollection;
    private BigDecimal totalWithdraw;
    private BigDecimal totalDisbursement;
    private BigDecimal totalLoanAdjustment;

    private String btnDeleteEnabled;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
