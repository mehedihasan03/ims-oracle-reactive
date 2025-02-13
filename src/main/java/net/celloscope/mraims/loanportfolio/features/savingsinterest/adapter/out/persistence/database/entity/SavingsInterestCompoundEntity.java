package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("template.savings_interest_compound")
public class SavingsInterestCompoundEntity {
   @Id
   private String oid;
   private String savingsAccountId;
   private BigDecimal compoundBalance;
   private LocalDate compoundingDate;
   private BigDecimal compoundBalanceBeforeInstallment;
   private BigDecimal compoundBalanceAfterInstallment;
   private BigDecimal accruedInterestBeforeInstallment;
   private BigDecimal accruedInterestAfterInstallment;
   private String managementProcessId;
   private LocalDateTime createdOn;
   private String createdBy;
   private LocalDateTime updatedOn;
   private String updatedBy;
   private String status;

   @Override
   public String toString() {
      return CommonFunctions.buildGsonBuilder(this);
   }
}
