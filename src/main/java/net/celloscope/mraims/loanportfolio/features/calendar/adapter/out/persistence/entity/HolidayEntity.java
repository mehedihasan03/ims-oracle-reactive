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
@Table("holiday")
public class HolidayEntity implements Persistable<String> {
	
	@Id
	public String oid;
	public String holidayId;
	public String calendarDayId;
	public String officeId;
	public LocalDate holidayDate;
	public String holidayType;
	public String titleEn;
	public String titleBn;
	public String mfiId;
	public String status;
	public String managementProcessId;
	
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
