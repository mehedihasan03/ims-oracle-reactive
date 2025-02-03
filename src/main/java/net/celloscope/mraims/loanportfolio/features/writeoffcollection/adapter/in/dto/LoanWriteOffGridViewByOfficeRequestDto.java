package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffGridViewByOfficeRequestDto {
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;
    private String samityId;
    private Integer limit;
    private Integer offset;
    private LocalDateTime startDate;
    private LocalDateTime endDate;


}
