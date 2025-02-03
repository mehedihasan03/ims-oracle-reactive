package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum DateTimeFormatterPattern {
	DATE_TIME("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
	;
	private final String value;
	DateTimeFormatterPattern(String value){
		this.value = value;
	}
	

}
