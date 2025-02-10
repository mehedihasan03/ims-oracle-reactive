package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.passbook")
public class PassbookTransactionEntity implements Persistable<String> {
	
	@Id
	private String oid;
	
	//	for deposit
	private BigDecimal lastDepositAmount;
	private LocalDate lastDepositDate;
	private String lastDepositType;
	
	//	for withdraw
	private BigDecimal lastWithdrawAmount;
	private LocalDate lastWithdrawDate;
	private String lastWithdrawType;
	
	@Override
	public String toString(){
		return CommonFunctions.buildGsonBuilder(this);
	}
	
	@Override
	public String getId() {
		return this.getOid();
	}
	
	@Override
	public boolean isNew() {
		boolean isNull = isNull(this.oid);
		this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
		return isNull;
	}
}
