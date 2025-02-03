package net.celloscope.mraims.loanportfolio.core.tenantmanagement.util;

public enum TenantIdEnum {

    TENANT_ID("instituteOid");
    private final String value;

    TenantIdEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
