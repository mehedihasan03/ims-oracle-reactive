package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStagingDataFieldOfficerDetailViewResponse {
	
	private String officeId;
	private String fieldOfficerId;
	private String fieldOfficerNameEn;
	private String fieldOfficerNameBn;
	
	private String samityId;
	private String samityNameEn;
	private String samityNameBn;
	private String samityDay;
	private Integer totalMember;
	private String collectionType;
	private String mfiId;
	
	private BigDecimal totalLoanRecoverable;
	private BigDecimal totalLoanCollection;
	private BigDecimal serviceChargeRecoverable;
	private BigDecimal serviceChargeCollection;
	private BigDecimal totalRecoverable;
	private BigDecimal totalCollection;

	private String isCollected;
	private String collectedBy;
	private String isLocked;
	private String lockedBy;
	private String isEditable;
	private String isCommitted;
	private String committedBy;
	
	private List<MemberInfoDTO> memberList;
	
	private List<LoanAccountSummeryDTO> loanAccountSummery;
	private BigDecimal loanAccountTotalDue;
	
	private List<SavingsAccountSummeryDTO> savingsAccountSummery;
	private BigDecimal savingsAccountTotalTarget;
	
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
