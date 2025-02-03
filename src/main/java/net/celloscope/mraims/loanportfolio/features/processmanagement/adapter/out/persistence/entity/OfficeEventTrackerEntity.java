package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("office_event_tracker")
public class OfficeEventTrackerEntity implements Persistable<String> {
	
	@Id
	private String oid;
	private String managementProcessId;
	private String officeEventTrackerId;
	private String officeId;
	private String officeEvent;
	private LocalDateTime createdOn;
	private String createdBy;
	
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
