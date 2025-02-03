package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookTransactionDTO {

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
}
