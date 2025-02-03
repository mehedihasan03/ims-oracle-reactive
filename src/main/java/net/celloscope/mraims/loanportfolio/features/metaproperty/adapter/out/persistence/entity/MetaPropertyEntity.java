package net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("metaproperty")
public class MetaPropertyEntity extends BaseToString {

    @Id
    private String oid;

    @Column
    private String propertyId;

    @Column
    private String description;

    @Column
    private String status;

    @Column("property_value")
    private String parameters;
}
