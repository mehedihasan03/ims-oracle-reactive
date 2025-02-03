package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum CollectionType {
	REGULAR("Regular"),
	SPECIAL("Special"),
	REBATE("Rebate"),
	WAIVER("Waiver"),
	WRITE_OFF("WriteOff"),
	ADJUSTMENT("ADJUSTMENT"),
	SINGLE("Single")
	;
	private final String value;
	CollectionType(String value){
		this.value = value;
	}
	

}
