package net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out;

import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AutoVoucherDetailPersistencePort {
    Flux<AutoVoucherDetail> saveAutoVoucherDetailList(List<AutoVoucherDetail> autoVoucherDetailList);
    Mono<String> saveAutoVoucherDetailListIntoHistory(List<AutoVoucherDetail> autoVoucherDetailList);
    Flux<AutoVoucherDetail> getAutoVoucherDetailByVoucherId(String voucherId);

    Mono<Boolean> deleteAutoVoucherDetailListByVoucherIdList(List<String> voucherIdList);

    Flux<AutoVoucherDetail> updateAutoVoucherDetailStatusByVoucherId(String voucherId, String loginId, String status);
}
