package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.api.WriteOffAccountApi;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.dto.WriteOffAccountResponse;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out.WriteOffClientPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WriteOffClientAdapter implements WriteOffClientPort {

    private final WriteOffAccountApi offAccountApi;

    public WriteOffClientAdapter(WriteOffAccountApi offAccountApi) {
        this.offAccountApi = offAccountApi;
    }


    @Override
    public Mono<WriteOffAccountResponse> getWriteOffAccountList(WriteOffCollectionAccountDataRequestDto dataRequestDto) {
        return offAccountApi.getWriteOffAccountList(dataRequestDto)
                .doOnRequest(l -> log.info("request received to get write off collection list"))
                .doOnSuccess(response -> log.debug("Successfully received response : {}", response))
                .doOnError(throwable -> log.error("Failed to get write off list Response from mra gateway : {}", throwable.getMessage()));
    }
}
