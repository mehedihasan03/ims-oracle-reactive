package net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocatePaymentDTO {
    List<Passbook> passbookList;
    BigDecimal remainingAmount;
    Integer installmentNo;
}
