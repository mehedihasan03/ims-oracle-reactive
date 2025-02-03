package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStagingSavingsAccountInfoDTO {
	
	private String savingsAccountId;
	private String savingsProductCode;
	private String savingsProductNameEn;
	private String savingsProductNameBn;
	private String savingsProductType;
	private BigDecimal targetAmount;
	private BigDecimal balance;
	private BigDecimal savingsAvailableBalance;
	private BigDecimal totalDeposit;
	private BigDecimal totalWithdraw;
	
	private BigDecimal lastDepositAmount;
	private LocalDateTime lastDepositDate;
	private String lastDepositType;
	
	private BigDecimal lastWithdrawAmount;
	private LocalDateTime lastWithdrawDate;
	private String lastWithdrawType;
	
	private BigDecimal accruedInterestAmount;
	private List<DepositSchemeDetailDTO> depositSchemeDetails;

	//	from collection staging data
	private BigDecimal amount;
	private String paymentMode;
	private String collectionType;
	private String uploadedBy;
	private LocalDateTime uploadedOn;
	private String status;
	private String isCollectionCompleted;
	private String savingsTypeId;
	private int dpsPendingInstallmentNo;
	
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
