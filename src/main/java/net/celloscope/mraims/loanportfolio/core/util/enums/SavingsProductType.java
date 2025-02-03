package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum SavingsProductType {
	PRODUCT_TYPE_GS("GS"),
	PRODUCT_TYPE_VS("VS"),
	PRODUCT_TYPE_DPS("DPS"),
	PRODUCT_TYPE_FDR("FDR"),

	SAVINGS_TYPE_ID_DPS("DPS"),
	SAVINGS_TYPE_ID_FDR("FDR")
	;
	
	private final String value;
	
	SavingsProductType(String value){
		this.value = value;
	}
}
