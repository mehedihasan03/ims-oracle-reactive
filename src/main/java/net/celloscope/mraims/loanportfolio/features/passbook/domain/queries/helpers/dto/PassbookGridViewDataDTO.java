package net.celloscope.mraims.loanportfolio.features.passbook.domain.queries.helpers.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookGridViewDataDTO {
    private String officeId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private Integer totalMember;
    private Integer passbookCount;
}
