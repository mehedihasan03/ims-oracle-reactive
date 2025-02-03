package net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

public interface AutoVoucherPersistencePort {
    Mono<AutoVoucher> saveAutoVoucher(AutoVoucher autoVoucher);

    Flux<Tuple2<String, AutoVoucher>> saveAutoVoucherList(List<AutoVoucher> autoVouchers);
    Mono<String> saveAutoVoucherListIntoHistory(List<AutoVoucher> autoVouchers);

    Flux<AutoVoucher> getAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<Boolean> deleteAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<Boolean> deleteAutoVoucherListByManagementProcessId(String managementProcessId);

    Flux<AutoVoucher> updateAutoVoucherStatus(String managementProcessId, String processId, String loginId, String status);
    Flux<AutoVoucher> updateAutoVoucherStatus(String managementProcessId, String loginId, String status);

    Mono<AutoVoucherEntity> updateAutoVoucherWithAisRequest(String oid, String aisRequest);

    Flux<AutoVoucher> getAutoVoucherListByManagementProcessId(String managementProcessId);
}
