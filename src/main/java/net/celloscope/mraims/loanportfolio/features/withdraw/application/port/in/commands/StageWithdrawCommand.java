package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StageWithdrawCommand {
    /*private String institutionId;*/
    private String officeId;
    private String fieldOfficerId;
    private String loginId;
    private String samityId;
    private String mfiId;
    private List<WithdrawData> data;

}
