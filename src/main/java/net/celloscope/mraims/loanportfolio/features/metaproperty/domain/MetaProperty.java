package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaProperty extends BaseToString {
    private String oid;
    private String propertyId;
    private String description;
    private String status;

    private List<MetaPropertyParam> params;
}
