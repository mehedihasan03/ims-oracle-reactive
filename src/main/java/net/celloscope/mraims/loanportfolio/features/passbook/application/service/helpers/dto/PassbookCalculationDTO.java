package net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassbookCalculationDTO {
    Boolean isSCFullyPaid;
    Boolean isPrincipalFullyPaid;
    PassbookDTO passbookDTO;
    BigDecimal scPaid;
    BigDecimal prinPaid;
    BigDecimal scRemainForThisInst;
    BigDecimal prinRemainForThisInst;
    BigDecimal remainingAmount;
}
