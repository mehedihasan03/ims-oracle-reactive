package net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in;

import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestForFeeCollectionDTO;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AutoVoucherUseCase {
    Mono<List<AutoVoucher>> createAndSaveAutoVoucherFromAISRequest(AutoVoucherRequestDTO requestDTO);
    Mono<List<AutoVoucher>> getAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId);
    Mono<List<AutoVoucher>> getAutoVoucherListByManagementProcessId(String managementProcessId);
    Mono<Boolean> deleteAutoVoucherListByManagementProcessIdAndProcessId(String managementProcessId, String processId);
    Mono<String> deleteAutoVoucherListByManagementProcessId(String managementProcessId);

    Flux<AutoVoucherDetail> getAutoVoucherDetailListByVoucherId(String voucherId);
    Mono<Boolean> updateAutoVoucherAndVoucherDetailStatus(String managementProcessId, String processId, String loginId, String status);
    Mono<Boolean> updateAutoVoucherAndVoucherDetailStatus(String managementProcessId, String loginId, String status);
    Mono<Boolean> saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(String managementProcessId, String processId);

    Mono<AutoVoucher> createAndSaveAutoVoucherForFeeCollection(AutoVoucherRequestForFeeCollectionDTO requestDTO);
    Mono<AutoVoucher> updateAutoVoucherWithAisRequest(String oid, String aisRequest);
    Mono<String> saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(List<AutoVoucher> autoVoucherList);
}
