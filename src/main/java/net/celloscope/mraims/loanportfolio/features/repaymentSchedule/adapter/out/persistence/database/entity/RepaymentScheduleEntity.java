package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("loan_repay_schedule")
public class RepaymentScheduleEntity implements Persistable<String> {
    @Id
    private String oid;
    private String loanRepayScheduleId;
    private String loanAccountId;
    private String memberId;
    private Integer installNo;
    private LocalDate installDate;
    private LocalDate makeUpInstallDate;
    private BigDecimal beginPrinBalance;
    private BigDecimal scheduledPayment;
    private BigDecimal extraPayment;
    private BigDecimal totalPayment;
    private BigDecimal principal;
    private BigDecimal serviceCharge;
    private BigDecimal endPrinBalance;
    private BigDecimal penalty;
    private BigDecimal fees;
    private Integer paymentAttempts;
    private BigDecimal insurance;
    private LocalDate lastPostingDate;
    private BigDecimal penaltyOnHold;
    private String ifRescheduledId;
    private String mfiId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;

    private String isProvisioned;
    private String managementProcessId;


    @Override
    public String getId() {
        return this.oid;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
