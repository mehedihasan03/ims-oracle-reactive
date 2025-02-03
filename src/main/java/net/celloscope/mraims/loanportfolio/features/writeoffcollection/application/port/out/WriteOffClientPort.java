package net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out;

import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.dto.WriteOffAccountResponse;
import reactor.core.publisher.Mono;

public interface WriteOffClientPort {
    Mono<WriteOffAccountResponse> getWriteOffAccountList(WriteOffCollectionAccountDataRequestDto dataRequestDto);
}
