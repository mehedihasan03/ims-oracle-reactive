package net.celloscope.mraims.loanportfolio.features.stagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSchemeDetailDTO extends BaseToString {

    private String details;
}
