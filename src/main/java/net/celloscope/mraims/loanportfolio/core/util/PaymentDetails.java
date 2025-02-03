package net.celloscope.mraims.loanportfolio.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {

    private String bankName;
    private String bankBranch;
    private String bankAccountNo;
    private String bankAccountName;

    private String mfsAccountNo;
    private String mfsName;

    private String trxId;
    private String chequeNo;
    private String referenceNo;

    private String paymentDate;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
