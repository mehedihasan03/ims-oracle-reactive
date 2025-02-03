package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldOfficerEntity extends BaseToString {
    String fieldOfficerId;
    String fieldOfficerNameEn;
    String fieldOfficerNameBn;
}
