package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteOffAccountResponse {

    private String status;
    private String code;
    private String message;
    private String messageCode;
    private List<WriteOffAccountData> data;
    private long count;

    @Override
    public String toString() {
        return CommonFunctions.buildGson(this).toJson(this);
    }

}
