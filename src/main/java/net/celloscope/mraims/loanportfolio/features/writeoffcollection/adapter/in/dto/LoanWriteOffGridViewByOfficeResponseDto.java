package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffGridViewByOfficeResponseDto {
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanWriteOffGridData> data;
    private Integer totalCount;

}
