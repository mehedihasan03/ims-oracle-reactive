package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuthorizeWithdrawCommand {
    private String fieldOfficerId;
    private String samityId;
    private String mfiId;
    private String loginId;
    private String withdrawType;
}
