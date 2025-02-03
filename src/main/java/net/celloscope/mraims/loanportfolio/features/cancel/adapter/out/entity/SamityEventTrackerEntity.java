package net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("samity_event_tracker")
public class SamityEventTrackerEntity implements Persistable<String> {
	@Id
	private String oid;
	private String managementProcessId;
	private String samityEventTrackerId;
	private String officeId;
	private String samityId;
	private String samityEvent;
	private String remarks;
	private LocalDateTime createdOn;
	private String createdBy;
	
	@Override
	public String getId() {
		return this.oid;
	}
	
	@Override
	public boolean isNew() {
		boolean isNull = Objects.isNull(this.oid);
		this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
		return isNull;
	}
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
