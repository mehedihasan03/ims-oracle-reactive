package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum MetaPropertyDescriptions {

    SMS_NOTIFICATION_META_PROPERTY("SMS Notification Meta Property");

    private final String value;

    MetaPropertyDescriptions(String value) {
        this.value = value;
    }
}
