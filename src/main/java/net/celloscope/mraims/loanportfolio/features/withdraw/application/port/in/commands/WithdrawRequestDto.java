package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands;


import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequestDto {
    private String id;
    private BigDecimal amount;
    private String loginId;
    private String mfiId;
    private String instituteOid;
    private String userRole;
}
