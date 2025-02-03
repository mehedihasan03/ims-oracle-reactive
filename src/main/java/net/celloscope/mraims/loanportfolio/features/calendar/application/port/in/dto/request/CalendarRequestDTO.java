package net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CalendarRequestDTO {
	
	private String officeId;
	private LocalDate currentBusinessDate;
}
