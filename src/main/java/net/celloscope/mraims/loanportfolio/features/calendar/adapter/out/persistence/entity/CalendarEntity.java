package net.celloscope.mraims.loanportfolio.features.calendar.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.calendar")
public class CalendarEntity implements Persistable<String> {
	
	@Id
	private String oid;
	private String calendarDayId;
	private String financialPeriodId;
	private LocalDate calendarDate;
	private Integer calendarYear;
	private Integer dayOfWeek;
	private Integer monthOfYear;
	private Integer dayOfMonth;
	private Integer dayOfYear;
	private String isWorkingDay;
	private String officeId;
	private String mfiId;
	private String status;
	
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
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
