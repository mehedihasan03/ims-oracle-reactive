package net.celloscope.mraims.loanportfolio.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JsonProperty {
    private String name;
    private String value;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
