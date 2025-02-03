package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherDetailPersistenceHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherDetailPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out.AutoVoucherDetailPersistencePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class AutoVoucherDetailPersistenceAdapter implements AutoVoucherDetailPersistencePort {
    private final AutoVoucherDetailPersistenceRepository repository;
    private final AutoVoucherDetailPersistenceHistoryRepository historyRepository;
    private final ModelMapper modelMapper;

    public AutoVoucherDetailPersistenceAdapter(AutoVoucherDetailPersistenceRepository repository, AutoVoucherDetailPersistenceHistoryRepository historyRepository, ModelMapper modelMapper) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Flux<AutoVoucherDetail> saveAutoVoucherDetailList(List<AutoVoucherDetail> autoVoucherDetailList) {
        return Mono.just(autoVoucherDetailList)
                .filter(autoVoucherDetailList1 -> !autoVoucherDetailList1.isEmpty())
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetail -> modelMapper.map(autoVoucherDetail, AutoVoucherDetailEntity.class))
                .flatMap(repository::save)
                .doOnRequest(l -> log.info("Requesting to save auto voucher detail with size : {}", autoVoucherDetailList.size()))
                .collectList()
                .doOnSuccess(autoVoucherDetailEntityList -> log.info("Auto voucher detail saved successfully with size : {}", autoVoucherDetailEntityList.size()))
                .doOnError(throwable -> log.error("Error occurred while saving auto voucher detail : {}", throwable.getMessage()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetailEntity -> modelMapper.map(autoVoucherDetailEntity, AutoVoucherDetail.class));

    }

    @Override
    public Mono<String> saveAutoVoucherDetailListIntoHistory(List<AutoVoucherDetail> autoVoucherDetailList) {
        return Mono.just(autoVoucherDetailList)
                .filter(autoVoucherDetailsList -> !autoVoucherDetailsList.isEmpty())
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetail -> {
                    AutoVoucherDetailHistoryEntity autoVoucherDetailHistoryEntity = modelMapper.map(autoVoucherDetail, AutoVoucherDetailHistoryEntity.class);
                    autoVoucherDetailHistoryEntity.setAutoVoucherDetailOid(autoVoucherDetail.getOid());
                    autoVoucherDetailHistoryEntity.setOid(null);
                    return autoVoucherDetailHistoryEntity;

                })
                .flatMap(historyRepository::save)
                .doOnRequest(l -> log.info("Requesting to save auto voucher detail history with size : {}", autoVoucherDetailList.size()))
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto voucher details history saved successfully with size : {}", autoVoucherEntityList.size()))
                .doOnError(throwable -> log.error("Error occurred while saving auto voucher details into history: {}", throwable.getMessage()))
                .then(Mono.just("Auto voucher details saved successfully into history"));
    }

    @Override
    public Flux<AutoVoucherDetail> getAutoVoucherDetailByVoucherId(String voucherId) {
        return repository.findAllByVoucherId(voucherId)
                .doOnRequest(l -> log.info("Requesting to get auto voucher detail by voucher id : {}", voucherId))
                .collectList()
                .doOnSuccess(autoVoucherDetailEntityList -> log.info("Auto voucher detail fetched successfully with size : {}", autoVoucherDetailEntityList.size()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetailEntity -> modelMapper.map(autoVoucherDetailEntity, AutoVoucherDetail.class))
                .doOnError(throwable -> log.error("Error occurred while fetching auto voucher detail : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteAutoVoucherDetailListByVoucherIdList(List<String> voucherIdList) {
        return repository.deleteAllByVoucherIdIn(voucherIdList)
                .doOnRequest(l -> log.info("Requesting to delete auto voucher detail by voucher id list : {}", voucherIdList))
                .doOnSuccess(aBoolean -> log.info("Auto voucher detail deleted successfully with voucher id list : {}", voucherIdList))
                .doOnError(throwable -> log.error("Error occurred while deleting auto voucher detail : {}", throwable.getMessage()));
    }

    @Override
    public Flux<AutoVoucherDetail> updateAutoVoucherDetailStatusByVoucherId(String voucherId, String loginId, String status) {
        return repository.findAllByVoucherId(voucherId)
                .doOnRequest(l -> log.info("Requesting to fetch auto voucher detail status by voucher id : {}", voucherId))
                .collectList()
                .doOnSuccess(autoVoucherDetailEntityList -> log.info("Auto voucher detail fetched successfully with size : {}", autoVoucherDetailEntityList.size()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetailEntity -> {
                    autoVoucherDetailEntity.setStatus(status);
                    autoVoucherDetailEntity.setUpdatedBy(loginId);
                    autoVoucherDetailEntity.setUpdatedOn(LocalDateTime.now());
                    return autoVoucherDetailEntity;
                })
                .collectList()
                .doOnSuccess(autoVoucherEntityList -> log.info("Auto voucher Detail status updated. Requesting to save to db with size : {}", autoVoucherEntityList.size()))
                .flatMapMany(repository::saveAll)
                .doOnRequest(l -> log.info("Requesting to update auto voucher detail status by voucher id : {}", voucherId))
                .collectList()
                .doOnSuccess(autoVoucherDetailEntityList -> log.info("Auto voucher detail updated successfully with size : {}", autoVoucherDetailEntityList.size()))
                .doOnError(throwable -> log.error("Error occurred while updating auto voucher detail : {}", throwable.getMessage()))
                .flatMapMany(Flux::fromIterable)
                .map(autoVoucherDetailEntity -> modelMapper.map(autoVoucherDetailEntity, AutoVoucherDetail.class))
                .doOnComplete(() -> log.info("Auto voucher detail status updated successfully by voucher id : {}", voucherId))
                ;
    }
}
