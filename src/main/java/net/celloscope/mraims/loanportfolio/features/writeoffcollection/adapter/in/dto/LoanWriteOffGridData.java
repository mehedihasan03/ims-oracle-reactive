package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffGridData {
    private String oid;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private LocalDate loanWriteOffCollectionDate;
    private BigDecimal writeOffCollectionAmount;
    private BigDecimal payableAmountAfterWriteOffCollection;
    private String btnUpdateEnabled;
    private String btnSubmitEnabled;
    private String status;
}
