package net.celloscope.mraims.loanportfolio.features.attendance.adapter.out.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("member_attendance")
public class MemberAttendanceEntity implements Persistable<String> {

    @Id
    private String oid;
    private String memberAttendanceId;
    private String samityId;
    private String memberId;

    private LocalDate attendanceDate;
    private String samityDay;

    private String status;
    private String mfiId;

    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
//    private LocalDateTime uploadedOn;
//    private String uploadedBy;

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
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
