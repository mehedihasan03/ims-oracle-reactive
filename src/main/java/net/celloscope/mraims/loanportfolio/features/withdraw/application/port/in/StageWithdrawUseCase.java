package net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in;

import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.StageWithdrawCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.WithdrawRequestDto;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StageWithdrawResponseDTO;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.dto.StagingWithdrawDataDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StageWithdrawUseCase {
    Mono<StageWithdrawResponseDTO> stageWithdraw(StageWithdrawCommand command);
    Flux<StagingWithdrawDataDTO> getStagingWithdrawDataByStagingDataId(String stagingDataId);

    Mono<StageWithdrawResponseDTO> updateWithdrawalAmount(WithdrawRequestDto requestDto);
}
