package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanfund;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("loan_fund")
public class LoanFund {
    private String oid;
    private String loanFundId;
    private String loanFundingOrganizationId;
    private String bankAccountId;
    private BigDecimal amount;
    private String fundingNotes;
    private LocalDate loanAvailabilityDate;
    private String mfiId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
