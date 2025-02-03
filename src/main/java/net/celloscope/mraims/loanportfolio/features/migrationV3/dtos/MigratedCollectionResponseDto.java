package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.office.Office;
import net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity.Samity;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataStatusByOfficeResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigratedCollectionResponseDto {

    private Office office;
    private Samity samity;
    private ManagementProcessTrackerEntity managementProcessTracker;
    private List<OfficeEventTrackerEntity> officeEventTracker;
    private StagingDataStatusByOfficeResponseDTO stagingDataStatus;
    private List<StagingDataEntity> stagingData;
    private List<StagingAccountDataEntity> stagingAccountData;
    private List<PaymentCollectionBySamityCommand> collections;
    private List<PaymentCollectionBySamityCommand> regularCollection;
    private List<PaymentCollectionBySamityCommand> specialCollection;
    private List<TransactionEntity> transactions;
    private List<PassbookEntity> passbooks;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
