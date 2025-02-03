package net.celloscope.mraims.loanportfolio.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMSNotificationMetaProperty {

    private int sortOrder;
    private String type;
    private String isSMSNotificationEnabled;
    private String template;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
