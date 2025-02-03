package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithAccountId {

    private String productId;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal amount;
    private List<String> accountIdList;
}
