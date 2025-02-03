package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaPropertyParam extends BaseToString {
    private String value;
    private int sortOrder;
    private String name;
}
