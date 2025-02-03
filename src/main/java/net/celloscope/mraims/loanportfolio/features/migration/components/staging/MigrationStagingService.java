package net.celloscope.mraims.loanportfolio.features.migration.components.staging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationCollectionRequestDto;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationStagingService {
    public Mono<List<PaymentCollectionBySamityCommand>> buildPaymentCollectionBySamityCommand(MigrationCollectionRequestDto requestDto, MigratedCollectionResponseDto responseDto) {
        Map<String, List<StagingDataEntity>> stagingDataBySamityId = responseDto.getStagingData().stream()
                .collect(Collectors.groupingBy(StagingDataEntity::getSamityId));

        Map<String, List<StagingAccountDataEntity>> stagingAccountDataByMemberIdForLoan = groupStagingAccountDataByMemberId(responseDto.getStagingAccountData(),
            stagingAccountDataEntity -> stagingAccountDataEntity.getLoanAccountId() != null && !stagingAccountDataEntity.getLoanAccountId().isEmpty());

        Map<String, List<StagingAccountDataEntity>> stagingAccountDataByMemberIdForSavings = groupStagingAccountDataByMemberId(responseDto.getStagingAccountData(),
            stagingAccountDataEntity -> stagingAccountDataEntity.getSavingsAccountId() != null && !stagingAccountDataEntity.getSavingsAccountId().isEmpty());

        return Mono.just(stagingDataBySamityId.entrySet().stream().map(entry -> {
            String samityId = entry.getKey();
            List<StagingDataEntity> stagingDataList = entry.getValue();

            List<CollectionData> collectionDataListForLoan = createCollectionDataList(stagingDataList, stagingAccountDataByMemberIdForLoan, "Loan", requestDto, this::calculateLoanAmount);
            log.info("Collection Data List for Loan: {}", collectionDataListForLoan);
            List<CollectionData> collectionDataListForSavings = createCollectionDataList(stagingDataList, stagingAccountDataByMemberIdForSavings, "Savings", requestDto, this::calculateSavingsAmount);
            log.info("Collection Data List for Savings: {}", collectionDataListForSavings);
            List<CollectionData> collectionDataList = Stream.concat(collectionDataListForLoan.stream(), collectionDataListForSavings.stream()).collect(Collectors.toList());

            PaymentCollectionBySamityCommand collectionBySamity = new PaymentCollectionBySamityCommand();
            collectionBySamity.setSamityId(samityId);
            collectionBySamity.setOfficeId(requestDto.getOfficeId());
            collectionBySamity.setMfiId(requestDto.getMfiId());
            collectionBySamity.setLoginId(requestDto.getLoginId());
            collectionBySamity.setCollectionType("Special");
            collectionBySamity.setData(collectionDataList);
            return collectionBySamity;
        }).collect(Collectors.toList()));
    }

    private Map<String, List<StagingAccountDataEntity>> groupStagingAccountDataByMemberId(List<StagingAccountDataEntity> stagingAccountData, Predicate<StagingAccountDataEntity> filterCondition) {
        return stagingAccountData.stream()
            .filter(filterCondition)
            .collect(Collectors.groupingBy(StagingAccountDataEntity::getMemberId));
    }

    private List<CollectionData> createCollectionDataList(List<StagingDataEntity> stagingDataList,
                                                          Map<String, List<StagingAccountDataEntity>> stagingAccountDataByMemberId,
                                                          String accountType,
                                                          MigrationCollectionRequestDto requestDto,
                                                          BiFunction<MigrationCollectionRequestDto, StagingAccountDataEntity, BigDecimal> calculateAmountFunction) {
        return stagingDataList.stream().flatMap(stagingData -> {
            if (stagingAccountDataByMemberId.isEmpty() || stagingAccountDataByMemberId.get(stagingData.getMemberId()) == null) {
                return Stream.empty();
            }
            List<StagingAccountDataEntity> accountDataList = stagingAccountDataByMemberId.get(stagingData.getMemberId());
            return accountDataList.stream().map(accountData -> {
                CollectionData collectionData = new CollectionData();
                collectionData.setStagingDataId(stagingData.getStagingDataId());
                collectionData.setAccountType(accountType);
                collectionData.setLoanAccountId(accountType.equals("Loan") ? accountData.getLoanAccountId() : null);
                collectionData.setSavingsAccountId(accountType.equals("Savings") ? accountData.getSavingsAccountId() : null);
                collectionData.setAmount(calculateAmountFunction.apply(requestDto, accountData));
                collectionData.setPaymentMode("CASH");
                return collectionData;
            });
        }).toList();
    }

    private BigDecimal calculateLoanAmount(MigrationCollectionRequestDto requestDto, StagingAccountDataEntity accountData) {
        log.info("Calculating loan amount for member: {}", accountData.getMemberId());
        log.info("request dto Loan Information : {}", requestDto.getMembers().get(0).getLoanInformation());
        return requestDto.getMembers()
                .stream()
                .filter(member -> member.getMemberId().equals(accountData.getMemberId()) && member.getLoanInformation() != null && member.getLoanInformation().getLoanAccountId().equals(accountData.getLoanAccountId()))
                .findFirst()
                .map(member -> member.getLoanInformation().getDisbursedLoanAmount().subtract(
                            member.getLoanInformation().getLoanOutstanding())
                ).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateSavingsAmount(MigrationCollectionRequestDto requestDto, StagingAccountDataEntity accountData) {
        return requestDto.getMembers()
                .stream()
                .filter(member -> member.getMemberId().equals(accountData.getMemberId()) && member.getSavingsInformation() != null && member.getSavingsInformation().getSavingsAccountId().equals(accountData.getSavingsAccountId()))
                .findFirst()
                .map(member -> member.getSavingsInformation().getBalance()
                ).orElse(BigDecimal.ZERO);
    }

}
