package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("office")
public class OfficeEntity implements Persistable<String> {

    @Id
    private String oid;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private String officeTypeId;
    private String mfiProgramId;
    private String email;
    private String mobile;
    private String phone;

    @Override
    public String getId() {
        return this.oid;
    }

    public void setId(String id) {
        this.oid = id;
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
