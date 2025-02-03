package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter;

import java.time.LocalDateTime;
import java.util.*;

import lombok.experimental.Helper;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingAccountDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingAccountDataRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.LoanAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.SavingsAccountSummeryDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.DepositSchemeDetailDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.Installment;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StagingAccountDataAdapter implements IStagingAccountDataPersistencePort {

    private final IStagingAccountDataRepository repository;
    private final IStagingAccountDataEditHistoryRepository editHistoryRepository;
    private final ModelMapper mapper;
    private final Gson gson;

    public StagingAccountDataAdapter(IStagingAccountDataRepository repository,
            IStagingAccountDataEditHistoryRepository editHistoryRepository, ModelMapper mapper) {
        this.repository = repository;
        this.editHistoryRepository = editHistoryRepository;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Flux<StagingAccountData> save(List<StagingAccountData> stagingAccountDataList) {
        Flux<StagingAccountDataEntity> entityList = Flux.fromIterable(stagingAccountDataList)
                .map(s -> mapper.map(s, StagingAccountDataEntity.class))
                .map(s -> {
                    s.setStagingAccountDataId(UUID.randomUUID().toString());
                    return s;
                });
        return repository.saveAll(entityList)
                .map(this::buildInstallmentAndDepositSchemeDetailListForStagingAccountData);
    }

    @Override
    public Flux<StagingAccountData> getStagingLoanAccountDataListByMemberId(String memberId) {
        return repository.getStagingLoanAccountDataListByMemberId(memberId)
                .map(this::buildInstallmentAndDepositSchemeDetailListForStagingAccountData);
    }

    @Override
    public Flux<StagingAccountData> getStagingSavingsAccountDataListByMemberId(String memberId) {
        return repository.getStagingSavingsAccountDataListByMemberId(memberId)
                .map(this::buildInstallmentAndDepositSchemeDetailListForStagingAccountData)
                .doOnNext(s -> log.info("Staging Account Data From Adapter: {}", s));
    }

    @Override
    public Flux<LoanAccountSummeryDTO> getLoanAccountSummeryByProductCode(String samityId) {
        return repository.getLoanAccountSummeryByProductCode(samityId);
    }

    @Override
    public Flux<SavingsAccountSummeryDTO> getSavingsAccountSummeryByProductCode(String samityId) {
        return repository.getSavingsAccountSummeryByProductCode(samityId);
    }

    @Override
    public Mono<StagingAccountData> getStagingLoanOrSavingsAccountByAccountId(String accountId) {
        return repository.getStagingLoanOrSavingsAccountByAccountId(accountId)
                .map(this::buildInstallmentAndDepositSchemeDetailListForStagingAccountData)
                .doOnNext(stagingAccountData -> log.debug("Staging Account Data By Account Id: {}", stagingAccountData));
    }

    // private StagingAccountData
    // buildInstallmentsListInStagingAccountData(StagingAccountDataEntity entity){
    // StagingAccountData stagingAccountData = mapper.map(entity,
    // StagingAccountData.class);
    // List<Installment> installmentList = gson.fromJson(entity.getInstallments(),
    // ArrayList.class);
    // stagingAccountData.setInstallments(installmentList);
    // return stagingAccountData;
    // }
    //
    // private StagingAccountData
    // buildDepositSchemeDetailList(StagingAccountDataEntity entity){
    // StagingAccountData stagingAccountData = mapper.map(entity,
    // StagingAccountData.class);
    // List<DepositSchemeDetailDTO> depositSchemeDetailList =
    // gson.fromJson(entity.getDepositSchemeDetail(), ArrayList.class);
    // stagingAccountData.setDepositSchemeDetail(depositSchemeDetailList);
    // return stagingAccountData;
    // }

    private StagingAccountData buildInstallmentAndDepositSchemeDetailListForStagingAccountData(
            StagingAccountDataEntity entity) {
        StagingAccountData stagingAccountData = mapper.map(entity, StagingAccountData.class);
        List<Installment> installmentList = gson.fromJson(entity.getInstallments(), ArrayList.class);
        stagingAccountData.setInstallments(installmentList);
        List<DepositSchemeDetailDTO> depositSchemeDetailList = gson.fromJson(entity.getDepositSchemeDetail(),
                ArrayList.class);
        stagingAccountData.setDepositSchemeDetail(depositSchemeDetailList);
        return stagingAccountData;
    }

    @Override
    public Mono<String> deleteStagingAccountDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .thenReturn(managementProcessId);
    }

    @Override
    public Mono<List<StagingAccountDataEntity>> getAllStagingAccountDataByManagementProcessId(
            String managementProcessId) {
        return repository.findAllByManagementProcessId(managementProcessId)
                .collectList();
    }

    @Override
    public Mono<String> deleteAllStagingAccountDataByManagementProcessId(String managementProcessId) {
        return repository.deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Staging Account Data Deleted SuccessFully"));
    }

    @Override
    public Mono<List<String>> saveAllStagingAccountData(List<StagingAccountData> stagingAccountDataList) {
        return Flux.fromIterable(stagingAccountDataList)
                .sort(Comparator.comparing(StagingAccountData::getMemberId))
                .map(stagingAccountData -> {
                    StagingAccountDataEntity stagingAccountDataEntity = mapper.map(stagingAccountData,
                            StagingAccountDataEntity.class);
                    stagingAccountDataEntity.setCreatedOn(LocalDateTime.now());
                    stagingAccountDataEntity.setOid(null);
                    return stagingAccountDataEntity;
                })
                .collectList()
                .flatMap(entityList -> repository.saveAll(entityList)
                        .map(StagingAccountDataEntity::getStagingAccountDataId)
                        .collectList());
    }

    @Override
    public Mono<List<String>> editUpdateAndDeleteStagingAccountDataOfASamity(String processId, String samityId, List<String> memberIdList) {
        final List<String> accountIdList = new ArrayList<>();
        return repository.findAllByProcessIdAndMemberIdIn(processId, memberIdList)
                .map(entity -> {
                    StagingAccountDataEditHistoryEntity editHistoryEntity = gson.fromJson(entity.toString(), StagingAccountDataEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .collectList()
                .doOnNext(editHistoryEntityList -> {
                    editHistoryEntityList.forEach(entity -> {
                        if(!HelperUtil.checkIfNullOrEmpty(entity.getLoanAccountId())){
                            accountIdList.add(entity.getLoanAccountId());
                        } else if(!HelperUtil.checkIfNullOrEmpty(entity.getSavingsAccountId())){
                            accountIdList.add(entity.getSavingsAccountId());
                        }
                    });
                })
                .flatMap(editHistoryEntityList -> editHistoryRepository.saveAll(editHistoryEntityList)
                        .collectList())
                .flatMap(list -> repository.deleteAllByProcessIdAndMemberIdIn(processId, memberIdList))
                .then(Mono.just(accountIdList));
    }

    @Override
    public Mono<StagingAccountData> getStagingAccountDataBySavingsAccountId(String savingsAccountId) {
        return repository.findFirstBySavingsAccountIdNotNullAndSavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.just(StagingAccountDataEntity.builder().build()))
                .map(entity -> gson.fromJson(entity.toString(), StagingAccountData.class));
    }

    @Override
    public Mono<StagingAccountData> getLoanAccountDataByLoanAccountId(String loanAccountId) {
        return repository.findFirstByLoanAccountIdNotNullAndLoanAccountId(loanAccountId)
                .doOnNext(entity -> log.info("Loan Account Data By Loan Account Id: {}", entity))
//                .switchIfEmpty(Mono.just(StagingAccountDataEntity.builder().build()))
                .map(entity -> {
                    ArrayList listOfInstallments = gson.fromJson(entity.getInstallments(), ArrayList.class);
                    log.info("List Of Installments: {}", listOfInstallments);
                    entity.setInstallments(null);
                    StagingAccountData stagingAccountData = gson.fromJson(entity.toString(), StagingAccountData.class);
                    stagingAccountData.setInstallments(listOfInstallments);
                    return stagingAccountData;
                });
//                } gson.fromJson(entity.toString(), StagingAccountData.class));
    }

    @Override
    public Mono<Map<String, List<String>>> updateStagingAccountDataToEditHistoryTable(String managementProcessId,
            String processId, List<String> memberIdList) {
        return repository
                .findAllByManagementProcessIdAndProcessIdAndMemberIdIn(managementProcessId, processId, memberIdList)
                .map(accountDataEntity -> {
                    StagingAccountDataEditHistoryEntity editHistoryEntity = gson.fromJson(accountDataEntity.toString(),
                            StagingAccountDataEditHistoryEntity.class);
                    editHistoryEntity.setOid(null);
                    return editHistoryEntity;
                })
                .sort(Comparator.comparing(StagingAccountDataEditHistoryEntity::getMemberId))
                .collectList()
                .flatMapMany(editHistoryRepository::saveAll)
                .collectList()
                .map(editHistoryEntityList -> {
                    List<String> loanAccountIdList = new ArrayList<>();
                    List<String> savingsAccountIdList = new ArrayList<>();
                    editHistoryEntityList.forEach(editHistoryEntity -> {
                        if (editHistoryEntity.getLoanAccountId() != null) {
                            loanAccountIdList.add(editHistoryEntity.getLoanAccountId());
                        } else if (editHistoryEntity.getSavingsAccountId() != null) {
                            savingsAccountIdList.add(editHistoryEntity.getSavingsAccountId());
                        }
                    });
                    return Map.of("loanAccountIdList", loanAccountIdList, "savingsAccountIdList", savingsAccountIdList);
                });
    }

    @Override
    public Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(String managementProcessId,
            List<String> memberIdList) {
        return repository.findAllByManagementProcessIdAndMemberIdIn(managementProcessId, memberIdList)
                .map(entity -> {
                    entity.setInstallments(null);
                    return gson.fromJson(entity.toString(), StagingAccountData.class);
                });
    }

    @Override
    public Flux<StagingAccountData> getStagingAccountDataListByMemberId(String memberId) {
        return repository.getStagingAccountDataEntitiesByMemberId(memberId)
                .map(entity -> {
                    List<Installment> installmentList = gson.fromJson(entity.getInstallments(), ArrayList.class);
                    entity.setInstallments(null);
                    StagingAccountData stagingAccountData = gson.fromJson(entity.toString(), StagingAccountData.class);
                    stagingAccountData.setInstallments(installmentList);
                    return stagingAccountData;
                });
    }

    @Override
    public Flux<StagingAccountData> getStagingAccountDataBySavingsAccountIdList(List<String> savingsAccountIdList) {
        return repository.findAllBySavingsAccountIdIn(savingsAccountIdList)
                .map(entity -> {
                    List<Installment> installmentList = gson.fromJson(entity.getInstallments(), ArrayList.class);
                    entity.setInstallments(null);
                    StagingAccountData stagingAccountData = gson.fromJson(entity.toString(), StagingAccountData.class);
                    stagingAccountData.setInstallments(installmentList);
                    return stagingAccountData;
                });
    }

    @Override
    public Flux<StagingAccountData> getAllStagingAccountDataByMemberIdList(List<String> memberIDList) {
        return repository.findAllByMemberIdIn(memberIDList)
                .map(entity -> {
                    List<Installment> installmentList = gson.fromJson(entity.getInstallments(), ArrayList.class);
                    entity.setInstallments(null);
                    StagingAccountData stagingAccountData = gson.fromJson(entity.toString(), StagingAccountData.class);
                    stagingAccountData.setInstallments(installmentList);
                    return stagingAccountData;
                });
    }
}
