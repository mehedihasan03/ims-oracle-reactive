package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoEntity extends BaseToString {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
    private String stagingDataId;
}
