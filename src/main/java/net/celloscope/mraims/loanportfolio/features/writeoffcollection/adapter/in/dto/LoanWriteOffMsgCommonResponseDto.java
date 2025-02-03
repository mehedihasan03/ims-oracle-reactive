package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWriteOffMsgCommonResponseDto {
    private String userMessage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
